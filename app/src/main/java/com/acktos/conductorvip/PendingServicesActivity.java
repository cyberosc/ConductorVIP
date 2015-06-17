package com.acktos.conductorvip;

import java.io.IOException;
import java.util.ArrayList;

import com.acktos.conductorvip.adapters.MyServicesAdapter;
import com.acktos.conductorvip.adapters.RequestsAdapter;
import com.acktos.conductorvip.broadcast.AlarmReceiver;
import com.acktos.conductorvip.controllers.BillController;
import com.acktos.conductorvip.controllers.CarController;
import com.acktos.conductorvip.controllers.ServiceController;
import com.acktos.conductorvip.entities.Service;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PendingServicesActivity extends Activity {

	public final static String TAG="conductorvip_debug";

	//Google Register ID
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String SENDER_ID = "70360033582";
	String regid;

	//UI references
	private ListView listPendingServices;
	private ArrayList<Service> pendingServices;
	private MyServicesAdapter mAdapter;
	private TextView pendingMessage;
    private ButtonRectangle connectedButton;
	
	//Attempt get web service
	private boolean attempt=false;
	
	//components
	ServiceController serviceController;
	CarController carController;
	AlarmReceiver alarmReceiver;
	Context context;
	GoogleCloudMessaging gcm;

	//failed bills
	private ArrayList<String> failedBills;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		setContentView(R.layout.activity_pending_services);
		context = getApplicationContext();

		// Check device for Play Services APK. If check succeeds, proceed with
		//  GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		listPendingServices=(ListView) findViewById(R.id.list_pending_services);
		pendingMessage=(TextView) findViewById(R.id.pending_status_message);
        connectedButton=(ButtonRectangle) findViewById(R.id.btn_connect);
		
		//components initialized
		serviceController=new ServiceController(this);
		carController=new CarController(this);
        alarmReceiver=new AlarmReceiver();

		//set adapter to listView
		pendingServices=new ArrayList<Service>();
		new RequestsAdapter(this, pendingServices);
		
		mAdapter= new MyServicesAdapter(this,pendingServices);
		listPendingServices.setAdapter(mAdapter);

		//set click handler to listView
		listPendingServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

				if(!attempt){
					Object item=parent.getItemAtPosition(position);
					Service service=(Service)item;

					//String serviceString=service.toJson();

					if(BillController.isFailedBill(failedBills,service.id)){

						SendBillDialog sendBillDialog =
								SendBillDialog.newInstance(service.id);
						sendBillDialog.show(getFragmentManager(), SendBillDialog.TAG_DIALOG);

					} else if(service.state.equals(Service.STATE_FINISHED)){

						Intent i=new Intent(PendingServicesActivity.this,BillDetailActivity.class);
						i.putExtra(Service.KEY_ID, service.id);
						PendingServicesActivity.this.startActivity(i);

					}else if(!service.state.equals(Service.STATE_FINISHED) &&
							!service.state.equals(Service.STATE_CANCELED)&&
							!service.state.equals(Service.STATE_PENDING_FOR_BILL)){

						ServiceDialog serviceDialogFragment =
								ServiceDialog.newInstance(service.id,service.address,service.customerPhone,service.customer);
						serviceDialogFragment.show(getFragmentManager(), ServiceDialog.TAG_DIALOG);

					}
					
				}

			}
		});


        // update color button state
        updateConnectedUI();

        // set alarm for periodical position updates if the driver is connected
        if(carController.getConnectedState()){
            alarmReceiver.setAlarm(this);
        }

	}

	@Override
	protected void onStart(){
		failedBills= BillController.getFailedList(this);
		super.onStart();
	}

	@Override
	protected void onResume() {

        super.onResume();
		checkPlayServices();
		GetMyServicesTask pendingTask=new GetMyServicesTask();
		pendingTask.execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_navigation, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action buttons
		switch(item.getItemId()) {

			case R.id.logout:
				String messageLogout;
				if(logout()){
					messageLogout=getString(R.string.msg_logout_success);


					Intent iLogout=new Intent(this,LoginActivity.class);
					startActivity(iLogout);
					finish();
				}else{
					messageLogout=getString(R.string.msg_logout_failed);
				}

				Toast.makeText(this,messageLogout,Toast.LENGTH_LONG).show();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private boolean logout(){

        boolean connectedState=carController.getConnectedState();

        if(!connectedState){
            if(deleteFile(LoginActivity.FILE_CAR_PROFILE)){

                return true;
            }else{
                return false;
            }
        }else{
            DialogFragment newFragment = new DisconnectDialog();
            newFragment.show(getFragmentManager(), "disconnectDialog");
            return false;
        }



	}

    public void changeConnectedState(View view){

        boolean connectedState=carController.getConnectedState();

        // driver is connected, start disconnected process
        if(connectedState){
            (new DisconnectedTask()).execute();
        }
        // driver is disconnect, start periodical send-position alarm
        else{
            alarmReceiver.setAlarm(this);
            carController.setConnectedState(true);
            updateConnectedUI();
        }

    }

    /**
     * Change color and text of the connected button
     */
    private void updateConnectedUI(){

        boolean connectedState;
        connectedState=carController.getConnectedState();

        if(connectedState){
            connectedButton.setBackgroundColor(getResources().getColor(R.color.green_primary));
            connectedButton.setText(getString(R.string.connected_state));
        }else{
            connectedButton.setBackgroundColor(getResources().getColor(R.color.red_primary));
            connectedButton.setText(getString(R.string.disconnected_state));
        }
    }

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * If result is empty, the app needs to register.
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	public String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		final String registrationId = prefs.getString(PROPERTY_REG_ID, "");

		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}else{
			Log.i(TAG,"Registration id:"+registrationId);
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing registration ID is not guaranteed to work with
		// the new app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}else{
			new AsyncTask<Void,Void,Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					sendRegistrationIdToBackend(registrationId);
					return null;
				}

			}.execute(null, null, null);
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		//Persists the registration ID in shared preferences.
		return getSharedPreferences(PendingServicesActivity.class.getSimpleName(),Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void,Void,String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// save registerId in backend
					sendRegistrationIdToBackend(regid);

					// save regiterId in shared preferences
					storeRegistrationId(context, regid);

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i(TAG,msg);
			}

		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend(String registerId) {

		(new CarController(this)).saveRegistrationId(registerId);

	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private class GetMyServicesTask extends AsyncTask<Void, Void, ArrayList<Service>>{

		@Override
		protected ArrayList<Service> doInBackground(Void... args) {
			
			ArrayList<Service> result=new ArrayList<Service>();
			
			if(!attempt){
				attempt=true;
				result=serviceController.getMyServices();
				
			}
			
			return result;
			
		}

		@Override
		protected void onPostExecute(ArrayList<Service> result) {
			super.onPostExecute(result);
			
			
			if(result!=null){
				
				if(result.size()>0){
					listPendingServices.setVisibility(View.VISIBLE);
					pendingMessage.setVisibility(View.GONE);
					pendingServices.clear();
					pendingServices.addAll(result);
					//requestAdapter.notifyDataSetChanged();
					mAdapter.notifyDataSetChanged();
				}else{
					listPendingServices.setVisibility(View.GONE);
					pendingMessage.setText(getString(R.string.msg_my_services_not_found));
					pendingMessage.setVisibility(View.VISIBLE);
				}
				
				
			}else{
				listPendingServices.setVisibility(View.GONE);
				pendingMessage.setText(getString(R.string.msg_connection_error));
				pendingMessage.setVisibility(View.VISIBLE);
				
			}
			attempt=false;
			setProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}
		
	}

    /**
     * send disconnected state to server in background
     */

    private class DisconnectedTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result=carController.disconnectedFromServer();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result){
                carController.setConnectedState(false);
                // update btn color
                updateConnectedUI();

                // cancel periodical send-position  alarm
                alarmReceiver.cancelAlarm(PendingServicesActivity.this);

            }else{
                Toast.makeText(PendingServicesActivity.this,getString(R.string.msg_failed_disconnect),Toast.LENGTH_LONG).show();
            }

            setProgressBarIndeterminateVisibility(false);

        }
    }

    public static class DisconnectDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.msg_confirm_disconnect)
                    .setPositiveButton(android.R.string.ok, null);
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
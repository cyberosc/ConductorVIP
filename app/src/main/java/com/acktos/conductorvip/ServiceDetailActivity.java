package com.acktos.conductorvip;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.acktos.conductorvip.controllers.CarController;
import com.acktos.conductorvip.services.AcceptIntentService;
import com.acktos.conductorvip.services.AssignDriverService;
import com.acktos.conductorvip.services.ChosenDriverService;
import com.acktos.conductorvip.services.GcmIntentService;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.gcm.GoogleCloudMessaging;


public class ServiceDetailActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private GoogleCloudMessaging gcm;

    private String address;
    private String distance;
    private String serviceId;
    private String driverId;

    private Boolean cancelable=true;
    private Boolean acceptable=true;

    private TextView txtAddress;
    private TextView txtDistance;
    private Button btnAccept;
    private Button btnCancel;
    private Drawable drawableRedButton;
    private Drawable drawableGreenButton;
    private ProgressBarIndeterminate progressBar;
    private SnackBar snackbar;
    private ActionBar actionBar;

    public static final String LBL_BTN_SNACK="ACEPTAR";
    private CarController carController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        actionBar=getActionBar();

        //get driver id
        carController=new CarController(this);
        driverId=carController.getCarId();

        //get GCM instance
        gcm = GoogleCloudMessaging.getInstance(this);

        Bundle extras=getIntent().getExtras();
        try{
            address=extras.getString(GcmIntentService.KEY_ADDRESS);
            distance=extras.getString(GcmIntentService.KEY_DISTANCE);
            serviceId=extras.getString(GcmIntentService.KEY_SERVICE_ID);

            Log.i(TAG, "serviceId en OnCreate:" + serviceId);
            Log.i(TAG, "distance en OnCreate:" + distance);
            Log.i(TAG, "address en OnCreate:" + address);


        }catch (RuntimeException e){
           e.printStackTrace();
        }

        if(serviceId!=null){
            actionBar.setTitle(getString(R.string.service)+": "+serviceId);
        }


        txtAddress=(TextView)findViewById(R.id.txt_value_address);
        txtDistance=(TextView)findViewById(R.id.txt_value_distance);
        btnAccept=(Button)findViewById(R.id.btn_accept);
        btnCancel=(Button)findViewById(R.id.btn_cancel);
        progressBar=(ProgressBarIndeterminate)findViewById(R.id.progress_bar);

        txtAddress.setText(address);
        txtDistance.setText(ChosenDriverService.getReadableDistance(distance));

        drawableGreenButton=getResources().getDrawable(R.drawable.circle_button_green);
        drawableRedButton=getResources().getDrawable(R.drawable.circle_button_red);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entry onPause");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "entry onResume");
        registerReceiver(receiver, new IntentFilter(AssignDriverService.NOTIFICATION));
    }

    public void acceptService(View view){

        if(acceptable){
            Log.i(TAG, "click on acceptService");

            acceptable=false;
            cancelable=false;

            //update UI
            progressBar.setVisibility(View.VISIBLE);
            changeStateButton(false, btnAccept);
            changeStateButton(false, btnCancel);

            Intent driverAcceptService=new Intent(this,AcceptIntentService.class);
            driverAcceptService.putExtra(GcmIntentService.KEY_SERVICE_ID,serviceId);
            driverAcceptService.putExtra(GcmIntentService.KEY_DRIVER_ID,driverId);
            driverAcceptService.putExtra(GcmIntentService.KEY_ACTION_ASSIGN, Activity.RESULT_OK);

            startService(driverAcceptService);

        }

    }

    public void cancelService(View view){

        if(cancelable) {

            Log.i(TAG, "click on acceptService");
            acceptable=false;
            cancelable=false;

            //update UI
            progressBar.setVisibility(View.VISIBLE);
            changeStateButton(false, btnAccept);
            changeStateButton(false, btnCancel);

            Intent driverAcceptService=new Intent(this,AcceptIntentService.class);
            driverAcceptService.putExtra(GcmIntentService.KEY_SERVICE_ID, serviceId);
            driverAcceptService.putExtra(GcmIntentService.KEY_DRIVER_ID, driverId);
            driverAcceptService.putExtra(GcmIntentService.KEY_ACTION_ASSIGN, Activity.RESULT_CANCELED);

            startService(driverAcceptService);
        }

    }

    private void changeStateButton(Boolean state, Button button){


        if(state){
            if(button.getId()==R.id.btn_accept){
                button.setBackground(drawableGreenButton);
            }
            if(button.getId()==R.id.btn_cancel){
                button.setBackground(drawableRedButton);
            }
        }else{

            Drawable drawableDisableButton=getResources().getDrawable(R.drawable.circle_button_disable);
            button.setBackground(drawableDisableButton);
        }

    }


    //create a broadcast for assign driver messages
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "entry broadcast Receiver serviceDetailActivity");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                int resultCode = bundle.getInt(GcmIntentService.KEY_SUCCESS_ASSIGN);
                String feedback= bundle.getString(GcmIntentService.KEY_MESSAGE);

                Log.i(TAG, "resultCode:" + resultCode);
                Log.i(TAG, "feedBack:" + feedback);

                updateUIMessage(resultCode, feedback);

            }
        }
    };

    public void updateUIMessage(final int resultCode,String feedback){

        progressBar.setVisibility(View.GONE);
        btnAccept.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);


        snackbar = new SnackBar(this, feedback, LBL_BTN_SNACK, new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(resultCode==Activity.RESULT_OK){
                    Intent i=new Intent(ServiceDetailActivity.this,PendingServicesActivity.class);
                    startActivity(i);
                }else{

                }
                Log.i(TAG, "click snackBar button");
                finish();
            }
        } );

        snackbar.setIndeterminate(true);
        snackbar.setColorButton(Color.WHITE);
        snackbar.show();

    }

}

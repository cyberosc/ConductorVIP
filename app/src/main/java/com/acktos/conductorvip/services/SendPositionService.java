package com.acktos.conductorvip.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.acktos.conductorvip.controllers.CarController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This service is launched to update a periodical position into REST API.
 */
public class SendPositionService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private final String TAG = getClass().getSimpleName();
    String message;

    //utilities
    private GoogleApiClient mGoogleApiClient;// Google client to interact with Google API
    private CarController carController;


    //ATTRIBUTES
    private Location mLastLocation;

    //Constants
    public static final String KEY_SERVICE_ID="service_id";
    public static final String KEY_COORDINATES="coordinates";



    public SendPositionService() {
        super("SendPositionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Entry to onCreate");

        //initialize last location
        mLastLocation=null;
        carController=new CarController(this);

    }

    @Override
    public void onHandleIntent(Intent intent) {

        Log.i(TAG, "Entry to onHandleIntent sendPosition");

        Bundle extras = intent.getExtras();

            if (checkPlayServices()) {
                buildGoogleApiClient();
                if (mGoogleApiClient != null) {

                    mGoogleApiClient.connect();
                    message="200";

                }else{
                    message="GoogleApiClient connect was not called";
                }
            } else {

                message="No valid Google Play Services APK found.";
            }

    }



    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
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
                return false;
            } else {

                return false;
            }
        }

        Log.i(TAG, "This device supported google play services.");
        return true;
    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Method to display the location on UI
     * */
    private void getLocation() {

        Log.i(TAG, "Entry to getLocation");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {

            mLastLocation=location;
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.i(TAG, "coordinates:" + latitude + ", " + longitude);

        } else {

            Log.i(TAG, "Couldn't get the location. Make sure location is enabled on the device");
        }
    }


    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        Log.i(TAG, "I'm connected with Google API client");
        getLocation();
        (new SendPositionTask()).execute();

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.i(TAG, "The connection with googleApiClient was suspended");
        mGoogleApiClient.connect();
    }

    public void sendPosition(){
        if(mLastLocation!=null){

            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            carController.sendPosition(latitude+","+longitude);
        }

    }

    class SendPositionTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            sendPosition();
            return null;
        }
    }


}

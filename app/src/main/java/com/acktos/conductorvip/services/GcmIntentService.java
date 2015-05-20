package com.acktos.conductorvip.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.acktos.conductorvip.PendingServicesActivity;
import com.acktos.conductorvip.broadcast.GcmBroadcastReceiver;
import com.acktos.conductorvip.controllers.CarController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GcmIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private final String TAG = getClass().getSimpleName();
    private GoogleCloudMessaging gcm;
    String message;

    //utilities
    private GoogleApiClient mGoogleApiClient;// Google client to interact with Google API
    private CarController carController;


    //ATTRIBUTES
    private Intent intent;
    private Location mLastLocation;
    private String serviceId;
    private String driverId;
    private String serviceCoordinates;

    /*this variable checks that there is no problem with the connection to GoogleApiClient
     and not send messages in onConnected and connectionFailed.*/
    private boolean truncateMessage=false;


    //Constants

    public static final String KEY_DRIVER_ID="driver_id";
    public static final String KEY_SERVICE_ID="service_id";
    public static final String KEY_COORDINATES="coordinates";
    public static final String KEY_SPEED="speed";
    public static final String KEY_ALTITUDE="altitude";
    public static final String KEY_DISTANCE_TO_SERVICE="distance_to_service";
    public static final String KEY_ACCURACY="accuracy";
    public static final String KEY_CURRENT_TIME="current_time";
    public static final String KEY_MESSAGE_TYPE="msg_type";
    public static final String KEY_SUCCESS_ASSIGN="success_assign";
    public static final String KEY_MESSAGE="message_feedback";
    public static final String TYPE_DISTANCE_TO_SERVICE="dis_to_serv";
    public static final String TYPE_CHOSEN_DRIVER="chosen_driver";
    public static final String TYPE_DRIVER_ACCEPT="driver_accept";
    public static final String TYPE_DRIVER_CANCEL="driver_cancel";
    public static final String TYPE_ASSIGN_DRIVER="assign_driver";
    public static final String KEY_AGENT="agent";
    public static final String KEY_ADDRESS="address";
    public static final String KEY_DISTANCE="distance";

    // from accept or cancel service
    public static final String KEY_ACTION_ASSIGN="action_assign";


    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Entry to onCreate");

        gcm = GoogleCloudMessaging.getInstance(this);
        //initialize last location
        mLastLocation=null;
        carController=new CarController(this);
        driverId=carController.getCarId();

    }

    @Override
    public void onHandleIntent(Intent intent) {

        Log.i(TAG, "Entry to onHandleIntent");
        this.intent=intent;
        Bundle extras = intent.getExtras();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        Log.i("extras", extras.toString());
        String messageType = gcm.getMessageType(intent);

        Log.i(TAG, "message type:" + messageType);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
            * Filter messages based on message type. Since it is likely that GCM
            * will be extended in the future with new message types, just ignore
            * any message types you're not interested in, or that you don't
            * recognize.
            */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                message="Message type send error: " + extras.toString();
                truncateMessage=true;
               // sendMessage();
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {

                message="Deleted messages on server: " +extras.toString();
                truncateMessage=true;
               // sendMessage();
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                if (checkPlayServices()) {
                    buildGoogleApiClient();
                    if (mGoogleApiClient != null) {
                        // Post notification of received message.
                        //sendNotification("Received: " + extras.toString());
                        Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                        this.serviceCoordinates =extras.getString(KEY_COORDINATES);
                        this.serviceId=extras.getString(KEY_SERVICE_ID);

                        mGoogleApiClient.connect();
                        message="200";

                    }else{
                        message="GoogleApiClient connect was not called";
                    }
                } else {
                    message="No valid Google Play Services APK found.";
                }
            }
        }else{

            truncateMessage=true;
            message="Extras information not found";
        }

        Log.i(TAG, "message:" + message);
        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

    //send message to CSS server
    private void sendMessage(){

        Log.i(TAG, "Entry to sendMessage");

        String coordinates="";
        String accuracy="";
        String distanceToService="";
        String altitude="";
        String speed="";
        String currentTime="";

        getLocation();

        if(mLastLocation!=null){
            String lat= Double.toString(mLastLocation.getLatitude());
            String lng= Double.toString(mLastLocation.getLongitude());

            coordinates=lat+","+lng;
            accuracy= Double.toString(mLastLocation.getAccuracy());
            altitude= Double.toString(mLastLocation.getAltitude());
            speed= Double.toString(mLastLocation.getSpeed());

            float[] results=new float[3];// result for distance between two coordinates.

            // calculates distance between two points.
            Location serviceLocation=getLocationFromString(serviceCoordinates);

            Location.distanceBetween(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(),
                    serviceLocation.getLatitude(),
                    serviceLocation.getLongitude(), results);
            try{
                distanceToService= Float.toString(results[0]);
            }catch (ArrayIndexOutOfBoundsException e){
                distanceToService="0";
            }

        }else{
            Log.i(TAG, "Last location is null");
        }

        //get current timestamp in seconds
        Long timeMillis = System.currentTimeMillis()/1000;
        currentTime = timeMillis.toString();

        Bundle data = new Bundle();

        data.putString(KEY_DRIVER_ID ,this.driverId);
        data.putString(KEY_SERVICE_ID ,this.serviceId);
        data.putString(KEY_COORDINATES ,coordinates);
        data.putString(KEY_AGENT, Build.BRAND+" "+ Build.MODEL+" Android "+ Build.VERSION.RELEASE);
        data.putString(KEY_ACCURACY ,accuracy);
        data.putString(KEY_ALTITUDE ,altitude);
        data.putString(KEY_SPEED ,speed);
        data.putString(KEY_DISTANCE_TO_SERVICE ,distanceToService);
        data.putString(KEY_CURRENT_TIME ,currentTime);
        data.putString(KEY_MESSAGE,message);
        data.putString(KEY_MESSAGE_TYPE,TYPE_DISTANCE_TO_SERVICE);

        Log.i(TAG, data.toString());

        (new SendGcmMessage()).execute(data);

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
                Log.i(TAG, "This device NOT supported google play services.");
                return false;
            }
        }

        Log.i(TAG, "This device supported google play services.");
        return true;
    }

    protected synchronized void buildGoogleApiClient() {

        Log.i(TAG, "Entry to buildGoogleApiClient");
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

    public static Location getLocationFromString(String coordinates){

        Log.i("serviceCoordinates:", coordinates);
        Location location=new Location("");
        try{

            double lat= Double.parseDouble(coordinates.split(",")[0]);
            double lng= Double.parseDouble(coordinates.split(",")[1]);
            location=new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);

        }catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        if(!truncateMessage){
            //sendMessage();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        Log.i(TAG, "I'm connected with Google API client");
        if(!truncateMessage){
            sendMessage();
        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.i(TAG, "The connection with googleApiClient was suspended");
        mGoogleApiClient.connect();
    }

    private class SendGcmMessage extends AsyncTask<Bundle,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Bundle... params) {

            Long timeToLive= 0L;
            Random r = new Random();
            int rand = (r.nextInt(100) + 1);
            String id = TYPE_DISTANCE_TO_SERVICE+"-"+Build.ID+"-"+rand;

            try {
                gcm.send(PendingServicesActivity.SENDER_ID + "@gcm.googleapis.com", id,timeToLive,params[0]);
                Log.i(TAG, "Message sent");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            GcmBroadcastReceiver.completeWakefulIntent(intent);
            stopSelf();
        }
    }
}

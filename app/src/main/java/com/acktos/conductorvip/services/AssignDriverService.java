package com.acktos.conductorvip.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This service sends a message to warn that service assignment was successful or invalid service
 * to this driver.
 */
public class AssignDriverService extends IntentService {

    public static final String NOTIFICATION = "com.acktos.gcmclient";
    private final String TAG = getClass().getSimpleName();

    public AssignDriverService() {
        super("AssignDriverService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "entry OnHandleIntent AssignDriverService");
        Bundle extras=intent.getExtras();
        if(extras!=null){

            Intent assignIntent = new Intent(NOTIFICATION);
            String successAssign=extras.getString(GcmIntentService.KEY_SUCCESS_ASSIGN);
            String feedback=extras.getString(GcmIntentService.KEY_MESSAGE);

            Log.i(TAG, "successAssign:" + successAssign);
            Log.i(TAG, "feedback:" + feedback);

            if(successAssign.equals("true")){
                Log.i(TAG, "entry successAssign true");
                assignIntent.putExtra(GcmIntentService.KEY_SUCCESS_ASSIGN, Activity.RESULT_OK);
            }else{
                Log.i(TAG, "entry successAssign false");
                assignIntent.putExtra(GcmIntentService.KEY_SUCCESS_ASSIGN, Activity.RESULT_CANCELED);
            }

            assignIntent.putExtra(GcmIntentService.KEY_MESSAGE,feedback);

            sendBroadcast(assignIntent);
            Log.i(TAG, "send Broadcast assignDriver");
        }

    }


}

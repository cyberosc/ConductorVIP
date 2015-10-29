package com.acktos.conductorvip.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.acktos.conductorvip.PendingServicesActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This service is launched when the driver accepts the request for a service through upstream message.
 */

public class AcceptIntentService extends IntentService {

    private final String TAG = getClass().getSimpleName();
    private GoogleCloudMessaging gcm;

    //Attributes
    private String driverId;
    private String serviceId;
    private int actionAssign;


    public AcceptIntentService() {
        super("ChosenDriverService");
        gcm = GoogleCloudMessaging.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Log.i(TAG, "entry onHandleIntent ChosenDriver");

        Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {  // has effect of un-parcelling Bundle

            driverId = extras.getString(GcmIntentService.KEY_DRIVER_ID);
            serviceId = extras.getString(GcmIntentService.KEY_SERVICE_ID);
            actionAssign = extras.getInt(GcmIntentService.KEY_ACTION_ASSIGN);

            //Log.i(TAG, "serviceId:" + serviceId);
            //Log.i(TAG, "driverId:" + driverId);

            Bundle data = new Bundle();

            data.putString(GcmIntentService.KEY_DRIVER_ID ,this.driverId);
            data.putString(GcmIntentService.KEY_SERVICE_ID ,this.serviceId);
            data.putString(GcmIntentService.KEY_AGENT, Build.BRAND+" "+ Build.MODEL+" Android "+ Build.VERSION.RELEASE);

            String messageType="";
            if(actionAssign== Activity.RESULT_OK){
                 messageType=GcmIntentService.TYPE_DRIVER_ACCEPT;
            }
            if(actionAssign== Activity.RESULT_CANCELED){
                messageType=GcmIntentService.TYPE_DRIVER_CANCEL;
            }

            data.putString(GcmIntentService.KEY_MESSAGE_TYPE,messageType);

            Long timeToLive= 0L;

            Random r = new Random();
            int rand = (r.nextInt(100) + 1);
            String id = GcmIntentService.TYPE_DISTANCE_TO_SERVICE+"-"+Build.ID+"-"+rand;

            try {
                gcm.send(PendingServicesActivity.SENDER_ID + "@gcm.googleapis.com", id, timeToLive,data);
                Log.i(TAG, "driver accept message sent");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}

package com.acktos.conductorvip.broadcast;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.acktos.conductorvip.services.AssignDriverService;
import com.acktos.conductorvip.services.ChosenDriverService;
import com.acktos.conductorvip.services.GcmIntentService;

/**
 * Created by OSCAR ACKTOS on 06/03/2015.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(getClass().getSimpleName(), "Entry onReceive GCM broadcast");
        Log.i(getClass().getSimpleName(), intent.getExtras().toString());

        // find what service will handle the intent.
        Bundle extras=intent.getExtras();
        String messageType=null;
        messageType=extras.getString(GcmIntentService.KEY_MESSAGE_TYPE);
        if(messageType!=null){
            ComponentName comp=findServiceForHandleMessage(context, messageType);

            // ComponentName comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }


    }

    private ComponentName findServiceForHandleMessage(Context context, String messageType){

        ComponentName comp=null;
        if(messageType.equals(GcmIntentService.TYPE_DISTANCE_TO_SERVICE)){
            comp = new ComponentName(context.getPackageName(),GcmIntentService.class.getName());
        }
        if(messageType.equals(GcmIntentService.TYPE_CHOSEN_DRIVER)){
            comp = new ComponentName(context.getPackageName(),ChosenDriverService.class.getName());
        }
        if(messageType.equals(GcmIntentService.TYPE_ASSIGN_DRIVER)){
            comp = new ComponentName(context.getPackageName(),AssignDriverService.class.getName());
        }

        return comp;
    }
}

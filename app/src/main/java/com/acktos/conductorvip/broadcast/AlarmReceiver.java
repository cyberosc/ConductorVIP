package com.acktos.conductorvip.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import com.acktos.conductorvip.services.SendPositionService;

/**
 * Created by Acktos on 27/05/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String DEBUG_TAG="debug receiver services";
    private AlarmManager mAlarm;
    private PendingIntent alarmIntent;
    public static final int REQUEST_CODE_PENDING=111;
    private static final int ALARM_TYPE = AlarmManager.ELAPSED_REALTIME;

    public static final int FIVE_SEC_MILLIS = 5000;
    public static final long FIVE_MINUTES_MILLIS=1000*60*5;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, SendPositionService.class);
        context.startService(service);

    }

    public void setAlarm(Context context){

        mAlarm=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(context,AlarmReceiver.class);
        alarmIntent= PendingIntent.getBroadcast(context, REQUEST_CODE_PENDING, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarm.setInexactRepeating(ALARM_TYPE,
                SystemClock.elapsedRealtime() + FIVE_SEC_MILLIS,  FIVE_MINUTES_MILLIS, alarmIntent);

        ComponentName receiver = new ComponentName(context, BootCompletedReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context){

        if (mAlarm!= null) {
            mAlarm.cancel(alarmIntent);
        }

        ComponentName receiver = new ComponentName(context, BootCompletedReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}

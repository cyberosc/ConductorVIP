package com.acktos.conductorvip.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver{
	
	private final String TAG = getClass().getSimpleName();;
	AlarmReceiver alarmReceiver=new AlarmReceiver();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
			alarmReceiver.setAlarm(context);
        }
		
	}

}

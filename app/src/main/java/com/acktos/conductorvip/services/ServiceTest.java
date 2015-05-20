package com.acktos.conductorvip.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;



public class ServiceTest extends IntentService {

	public ServiceTest() {
		super("ServiceTest");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(android.R.drawable.arrow_up_float)
		        .setContentTitle("Conductor VIP")
		        .setContentText("el proceso eta activo!");
		        
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(1, mBuilder.build());
		
	}

}

package com.acktos.conductorvip.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.acktos.conductorvip.R;
import com.acktos.conductorvip.ServiceDetailActivity;
import com.acktos.conductorvip.broadcast.GcmBroadcastReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ChosenDriverService extends IntentService {

    private final String TAG = getClass().getSimpleName();
    private GoogleCloudMessaging gcm;
    private NotificationManager mNotificationManager;

    //Attributes

    private String serviceId;
    private String address;
    private String distance;


    //sound attributes
    private AudioManager audioManager;
    private SoundPool soundPool;
    float actVolume, maxVolume, volume;
    private int soundID;
    boolean plays = false;
    boolean loaded = false;
    public static final int LIGHT_DURATION_ON=500;
    public static final int LIGHT_DURATION_OFF=400;
    public static final int NOTICATION_NEW_SERVICE_ID=45;
    private final static int MAX_VOLUME = 100;

    public ChosenDriverService() {
        super("ChosenDriverService");

    }

    @Override
    public void onCreate() {

        gcm = GoogleCloudMessaging.getInstance(this);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        volume = maxVolume;

        /*soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.i(TAG,"sound load completed");
                loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.newservicevoice2, 1);*/
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "entry onHandleIntent ChosenDriver");
        //this.intent=intent;
        Bundle extras = intent.getExtras();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        //Log.i(TAG,"message type:"+messageType);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle

            Log.i(TAG, "extras not empty");

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(TAG, "Message type send error: " + extras.toString());

                // sendMessage();
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {

                Log.i(TAG, "Deleted messages on server: " + extras.toString());

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                this.serviceId =extras.getString(GcmIntentService.KEY_SERVICE_ID);
                this.address =extras.getString(GcmIntentService.KEY_ADDRESS);
                this.distance=extras.getString(GcmIntentService.KEY_DISTANCE);


                Log.i(TAG, "send Notification");

                //sendNotification(serviceId, distance, address);

                //playSound();

                Intent i=new Intent(this,ServiceDetailActivity.class);
                i.putExtra(GcmIntentService.KEY_ADDRESS,address);
                i.putExtra(GcmIntentService.KEY_DISTANCE, distance);
                i.putExtra(GcmIntentService.KEY_SERVICE_ID, serviceId);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(i);

            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String serviceId,String distance,String address) {

        String message=getReadableDistance(distance)+" - "+address;
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent serviceDetail=new Intent(this,ServiceDetailActivity.class);
        serviceDetail.putExtra(GcmIntentService.KEY_ADDRESS,address);
        serviceDetail.putExtra(GcmIntentService.KEY_DISTANCE,distance);
        serviceDetail.putExtra(GcmIntentService.KEY_SERVICE_ID,serviceId);

        Log.i(TAG, "serviceId en sendNotification:" + serviceId);
        Log.i(TAG, "distance en sendNotification:" + distance);
        Log.i(TAG, "address en sendNotification:" + address);


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, serviceDetail, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_vip_car)
                        .setContentTitle("Estas cerca a un servicio.")
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setLights(Color.rgb(255, 255, 255), LIGHT_DURATION_ON, LIGHT_DURATION_OFF)
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTICATION_NEW_SERVICE_ID, mBuilder.build());
        Log.i(TAG, "sound play");

    }

    public static String getReadableDistance(String meters){

        if(meters!=null){
            try{
                int disMeters= Integer.parseInt(meters);
                if(disMeters>=1000){

                    double km= disMeters/1000;
                    String val = km+"";
                    BigDecimal big = new BigDecimal(val);
                    big = big.setScale(2, RoundingMode.HALF_UP);

                    return big+ " Km";
                }else{
                    return meters+" Metros";
                }
            }catch(NumberFormatException e){
                e.getMessage();
                return "Distancia desconocida";
            }

        }else{
            return "Distancia desconocida";
        }

    }

    private void playSound(){

        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.newservicevoice2);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 90) / Math.log(MAX_VOLUME)));
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                mediaPlayer.start();

            }
        });
    }

}

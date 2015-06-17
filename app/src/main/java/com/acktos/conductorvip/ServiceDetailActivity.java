package com.acktos.conductorvip;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acktos.conductorvip.controllers.CarController;
import com.acktos.conductorvip.entities.Service;
import com.acktos.conductorvip.services.AcceptIntentService;
import com.acktos.conductorvip.services.AssignDriverService;
import com.acktos.conductorvip.services.ChosenDriverService;
import com.acktos.conductorvip.services.GcmIntentService;
import com.acktos.conductorvip.util.DateTimeUtils;
import com.acktos.conductorvip.util.HoloCircularProgressBar;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.regex.Pattern;


public class ServiceDetailActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private GoogleCloudMessaging gcm;

    private String address;
    private String distance;
    private String serviceId;
    private String driverId;
    private String serviceType;
    private String pickupDate;
    private int resultCode; // code return by ccs connection
    private String ccsFeedback;// message return from ccs connection

    private Boolean cancelable=true;
    private Boolean acceptable=true;

    private PowerManager.WakeLock mWakeLock;
    private MediaPlayer mediaPlayer;

    private TextView txtTitle;
    private TextView txtAddress;
    private TextView txtDistance;
    private Button btnAccept;
    private Button btnCancel;
    private Drawable drawableRedButton;
    private Drawable drawableGreenButton;
    private ProgressBarIndeterminate progressBar;
    private SnackBar snackbar;
    private ActionBar actionBar;
    private TextView countingView;
    private LinearLayout ccsLayout;
    private TextView txtCCSMessage;
    private ButtonRectangle btnCcs;

    private CarController carController;

    //constants
    private static final int WAKELOCK_TIMEOUT = 60 * 1000;
    private static final long ttl=20000;
    private final static int MAX_VOLUME = 100;

    //Circular progressBar animator
    private HoloCircularProgressBar mHoloCircularProgressBar;
    private ObjectAnimator mProgressBarAnimator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        //initialize ccs result attributes
        resultCode=0;
        ccsFeedback=null;

        actionBar=getActionBar();
        actionBar.hide();
        setupSound();

        //get UI references
        txtAddress=(TextView)findViewById(R.id.txt_value_address);
        txtDistance=(TextView)findViewById(R.id.txt_value_distance);
        txtTitle=(TextView)findViewById(R.id.title_rutaplus);
        btnAccept=(Button)findViewById(R.id.btn_accept);
        btnCancel=(Button)findViewById(R.id.btn_cancel);
        progressBar=(ProgressBarIndeterminate)findViewById(R.id.progress_bar);
        countingView = (TextView) findViewById(R.id.counting);
        ccsLayout=(LinearLayout) findViewById(R.id.message_ccs_layout);
        txtCCSMessage=(TextView) findViewById(R.id.txt_ccs_message);
        btnCcs=(ButtonRectangle) findViewById(R.id.btn_ccs);


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

            //split address field
            String[] addressField=address.split(Pattern.quote("|"));

            try{

                Log.i("address field",address);
                address=addressField[0];
                Log.i("address field 0:",addressField[0]);
                serviceType=addressField[1];
                Log.i("address field 1:",addressField[1]);
                pickupDate=addressField[2];
                Log.i("address field 2:",addressField[2]);


                if(serviceType.equals(Service.KEY_RESERVED)){
                    //change date to human read format
                    pickupDate= DateTimeUtils.toLatinDate(pickupDate);
                    txtTitle.setText(getString(R.string.reserved)+" "+pickupDate);
                }


            }catch (ArrayIndexOutOfBoundsException e){
                e.getMessage();
            }


            //Log.i(TAG, "serviceId en OnCreate:" + serviceId);
            //Log.i(TAG, "distance en OnCreate:" + distance);
            //Log.i(TAG, "address en OnCreate:" + address);


        }catch (RuntimeException e){
           e.printStackTrace();
        }

        if(serviceId!=null){
            actionBar.setTitle(getString(R.string.service)+": "+serviceId);
        }


        txtAddress.setText(address);
        txtDistance.setText(ChosenDriverService.getReadableDistance(distance));

        drawableGreenButton=getResources().getDrawable(R.drawable.circle_button_green);
        drawableRedButton=getResources().getDrawable(R.drawable.circle_button_red);

        //setup circular progressBar
        mHoloCircularProgressBar = (HoloCircularProgressBar) findViewById(R.id.circularProgressBar);
        mHoloCircularProgressBar.setProgressColor(getResources().getColor(R.color.green_progress));
        mHoloCircularProgressBar.setProgressBackgroundColor(R.color.gray_progress_background);
        mHoloCircularProgressBar.setMarkerProgress(1f);
        setUpProgressBar(mHoloCircularProgressBar,1f, ttl);

        //Ensure wakelock release
        Runnable releaseWakelock = new Runnable() {

            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        };

        new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entry onPause");
        unregisterReceiver(receiver);


        /*if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }*/

        if(mediaPlayer != null) {
            stopSound();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "entry onResume");
        registerReceiver(receiver, new IntentFilter(AssignDriverService.NOTIFICATION));


        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((
                    PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
                   /**/ TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.i(TAG, "Wakelock aquired!!");
        }

    }

    private void setUpProgressBar(final HoloCircularProgressBar circularProgressBar, final float progress, final long duration) {

        mProgressBarAnimator = ObjectAnimator.ofFloat(circularProgressBar, "progress", progress);
        mProgressBarAnimator.setDuration(duration);
        mProgressBarAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {

                float progressBarStatus = circularProgressBar.getProgress();

                if (progressBarStatus == 1) {

                    stopSound();
                    finish();

                }
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {

                //initialProgressTime = System.currentTimeMillis();

            }
        });
        mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                circularProgressBar.setProgress((Float) animation.getAnimatedValue());
                Float contadorfloat = (Float) animation.getAnimatedValue();

                contadorfloat = contadorfloat * 20;
                Float countdown = 20-contadorfloat;

                String time = String.format("%.0f", countdown);
                countingView.setText(time);
            }
        });
        circularProgressBar.setMarkerProgress(progress);
        mProgressBarAnimator.start();

        //mProgressBarAnimator.setCurrentPlayTime();
    }


    private void setupSound(){

        mediaPlayer = MediaPlayer.create(this, R.raw.alarmarutaplus);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);

        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 90) / Math.log(MAX_VOLUME)));
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                mediaPlayer.start();

            }
        });
    }

    private void stopSound(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    public void acceptService(View view){

        if(acceptable){
            Log.i(TAG, "click on acceptService");

            acceptable=false;
            cancelable=false;

            //stop sound alarm
            stopSound();

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

            //stop sound alarm
            stopSound();

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

                //Log.i(TAG, "resultCode:" + resultCode);
                //Log.i(TAG, "feedBack:" + feedback);

                updateUIMessage(resultCode, feedback);

            }
        }
    };

    public void updateUIMessage(final int resultCode,String feedback){

        this.resultCode=resultCode;
        this.ccsFeedback=feedback;

        //stop animation
        mProgressBarAnimator.cancel();

        txtCCSMessage.setText(feedback);
        progressBar.setVisibility(View.GONE);
        btnAccept.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        ccsLayout.setVisibility(View.VISIBLE);

    }

    public void acceptCCSMessage(View view){

        if(resultCode!=0 && ccsFeedback!=null){

            if(resultCode==Activity.RESULT_OK){
                Intent i=new Intent(ServiceDetailActivity.this,PendingServicesActivity.class);
                startActivity(i);
            }else{
                finish();
            }
        }

        ccsLayout.setVisibility(View.GONE);
    }

}

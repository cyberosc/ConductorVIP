package com.acktos.conductorvip;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class ResultServiceActivity extends Activity {
	
	public static final String TAG_RESULT_MESSAGE="result_message";
	public static final String TAG_TOTAL="com.acktos.conductorvip.TOTAL";
	public static final String TAG_TOTAL_TIME="com.acktos.conductorvip.TOTAL_TIME";
	public static final String TAG_TOTAL_KM="com.acktos.conductorvip.TOTAL_KM";
	public static final String TAG_TOTAL_SPEED="com.acktos.conductorvip.TOTAL_SPEED";
	public static final String TAG_SUCCESS="success";
	
	//UI references
	private TextView txtResultMessage;
	private ImageView imgResult;
	private TextView txtTotal;
	private TextView txtTotalKm;
	private TextView txtTotalMin;
	private TextView txtTotalSpeed;
	
	//attributes
	private boolean success;
	private String total;
	private String totalKm;
	private String totalMin;
	private String totalSpeed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_service);
		
		//Initialize UI
		txtResultMessage=(TextView) findViewById(R.id.txt_result_message);
		txtTotal=(TextView) findViewById(R.id.txt_total);
		txtTotalKm=(TextView) findViewById(R.id.txt_total_km);
		txtTotalMin=(TextView) findViewById(R.id.txt_total_time);
		txtTotalSpeed=(TextView) findViewById(R.id.txt_total_speed);
		imgResult=(ImageView) findViewById(R.id.img_result);
		
		// Hide action bar
		ActionBar actionBar=getActionBar();
		actionBar.hide();
		
		Bundle extras=getIntent().getExtras();
		String resultMessage=extras.getString(TAG_RESULT_MESSAGE);
		success=extras.getBoolean(TAG_SUCCESS);
		
		if(!TextUtils.isEmpty(resultMessage)){
			txtResultMessage.setText(resultMessage);
		}
		
		//Result image
		if(!success){
			imgResult.setImageDrawable(getResources().getDrawable(R.drawable.failedflat));
		}else{
			total=extras.getString(TAG_TOTAL);
			totalMin=extras.getString(TAG_TOTAL_TIME);
			totalKm=extras.getString(TAG_TOTAL_KM);
			totalSpeed=extras.getString(TAG_TOTAL_SPEED);
			
			txtTotal.setText("$ "+total);
			txtTotalKm.setText(totalKm+" Km");
			txtTotalMin.setText(totalMin+" Minutos");
			txtTotalSpeed.setText(totalSpeed+" Km/h");
		}
		
	}

	// invoked by btn back services
	public void backToServices(View view){
		
		Intent i=new Intent(this,PendingServicesActivity.class);
		startActivity(i);
		finish();
	}


}

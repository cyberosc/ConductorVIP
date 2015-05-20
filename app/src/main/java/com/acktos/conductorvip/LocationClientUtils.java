package com.acktos.conductorvip;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.android.LocationUtils;
import com.acktos.conductorvip.entities.Service;


public class LocationClientUtils {

	Context context;
	
	//Utils
	InternalStorage storage;

	//shared preferences
	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;

	// keys for save bill data in shared preferences
	public static final String KEY_DISTANCE="com.acktos.conductorvip.DISTANCE";
	public static final String KEY_START_TIME="com.acktos.conductorvip.START_TIME";
	public static final String KEY_END_LOCATION="com.acktos.conductorvip.END_LOCATION";
	public static final String KEY_SERVICE_ID=Service.KEY_ID;
	public static final String KEY_PICK_UP_ADDRESS=Service.KEY_ADDRESS;
	public static final String KEY_STATE_TRACKING="com.acktos.conductorvip.STATE_TRACKING";
	public static final String KEY_FROM_BACKGROUND="com.acktos.conductorvip.FROM_BACKGROUND";


	//this file save  coordinates from a unique service 
	public static final String FILE_TRACK="com.acktos.conductorvip.TRACK_SERVICE";

	//this file save IDs of failed services
	public static final String FILE_FAILED_BILLS="com.acktos.conductorvip.FAILED_BILLS";

	//this file save info of file service temporally
	public static final String FILE_BILLS="com.acktos.conductorvip.BILL";

	//tracking states
	public static final int STARTED=72;
	public static final int COMPLETED=73;
	public static final int PENDING_FOR_BILL=74;
	public static final int PAUSED=75;

	// Accuracy sensor
	public static int GOOD_ACCURACY=2;
	public static int EXCELENT_ACCURACY=1;

	public static int sumaCoordenadas=0;


	public LocationClientUtils(Context context){

		this.context=context;
		
		storage=new InternalStorage(context);
		mPrefs = context.getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		mEditor = mPrefs.edit();
	}

	public void saveDistance(float distance){
		mEditor.putFloat(KEY_DISTANCE, distance);
		mEditor.commit();
	}

	public void saveStartTime(long timestamp){
		mEditor.putLong(KEY_START_TIME, timestamp);
		mEditor.commit();
	}

	public void saveEndLocation(Location location){

		if(location!=null){
			mEditor.putString(KEY_END_LOCATION, location.getLatitude()+","+location.getLongitude());
			mEditor.commit();
		}else{
			Log.e("saveEndLocation error","end location is null");
		}
	}

	public void saveServiceId(String serviceId){

		if(serviceId!=null){
			mEditor.putString(KEY_SERVICE_ID, serviceId);
			mEditor.commit();

			Log.i(KEY_SERVICE_ID,serviceId);
		}else{
			Log.e("saveServiceId error","service id is null");
		}
	}

	public void savePickUpAddress(String address){

		if(address!=null){
			mEditor.putString(KEY_PICK_UP_ADDRESS, address);
			mEditor.commit();
			Log.i(KEY_PICK_UP_ADDRESS,address);
		}else{
			Log.e("savePickUpAddress error","address is null");
		}
	}

	public void saveStateTracking(int state){

		mEditor.putInt(KEY_STATE_TRACKING, state);
		mEditor.commit();
	}

	public String getServiceId(){

		return  mPrefs.getString(KEY_SERVICE_ID,"");
	}

	public String getPickUpAddress(){

		return mPrefs.getString(KEY_PICK_UP_ADDRESS,"");

	}
	
	public Float getDistance(){

		return  mPrefs.getFloat(KEY_DISTANCE,0);
	}
	
	public Long getStartTime(){

		return  mPrefs.getLong(KEY_START_TIME,0);
	}
	
	public String getEndLocation(){

		return  mPrefs.getString(KEY_END_LOCATION,"");
	}
	
	public int getStateTracking(){
		return  mPrefs.getInt(KEY_STATE_TRACKING,0);
	}
	
	public boolean isFileTrackEmpty(String serviceId){
		
		boolean success =true;
		if(storage.isFileExists(FILE_TRACK+"_"+serviceId)){
			
			String coordinates=storage.readFile(FILE_TRACK+"_"+serviceId);	
			if(!TextUtils.isEmpty(coordinates)){
				success=false;
			}
		}
		return success;
	}
	
	public boolean checkSensors(Location location){

		float accuracy=location.getAccuracy();
		float speed=location.getSpeed();

		if(accuracy<=LocationUtils.MIN_ACCURACY && speed>=LocationUtils.STOP_SPEED){
		//if(accuracy<=LocationUtils.MIN_ACCURACY ){
			return true;
		}else{
			return false;
		}
		
	}
	
	public static float calculateDistanceBetween(Location startPoint, Location endPoint){

		float distance =0;

		android.location.Location startLocation=new android.location.Location("");
		android.location.Location endLocation=new android.location.Location("");

		startLocation.set(startPoint);
		endLocation.set(endPoint);

		distance=startLocation.distanceTo(endLocation);

		return distance;
	}
	
	public static Location locationFromString(String coordinates){
		
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

}

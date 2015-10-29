package com.acktos.conductorvip;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.android.LocationUtils;
import com.acktos.conductorvip.controllers.BillController;
import com.acktos.conductorvip.entities.Bill;
import com.acktos.conductorvip.entities.Service;
import com.acktos.conductorvip.services.TrackService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MapServiceActivity extends FragmentActivity implements
	GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener, LocationListener {


	//Google play services
	private static final String TAG = "LocationServiceDebug";

	private static final String KEY_IN_RESOLUTION = "is_in_resolution";

	/**
	 * Request code for auto Google Play Services error resolution.
	 */
	protected static final int REQUEST_CODE_RESOLUTION = 1;
	//Google API client.
	private GoogleApiClient mGoogleApiClient;
	/**
	 * Determines if the client is in a resolution state, and
	 * waiting for resolution intent to return.
	 */
	private boolean mIsInResolution;


	// google maps
	private GoogleMap mMap;

	// request to connect to Location Services
	private LocationRequest mLocationRequest;

	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;

	// Handle to a SharedPreferences editor
	SharedPreferences.Editor mEditor;

	//internal storage
	InternalStorage storage;

	// Utils
	LocationClientUtils locationClientUtils;

	//indicator for location changes
	boolean locationUpdatesEnabled = false;

	//values for tracking process
	private Location lastLocation=null;
	private Location prevLocation=null;
	private float currentZoom;
	private float accumulatedDistance=0;
	private long startTime=0;
	private long endTime=0;
	private boolean attemptSendTracking;
	private String finalAddress;
	private String serviceId;
	private String addressService;
	private String cutomerPhone;
	private boolean fromBackground=false;


	//UI widgets
	private TextView txtAccuracy;
	private TextView txtSpeed;
	private Button btnStartTrack;
	private TextView txtStatus;
	private TextView txtAddressMap;
	private ProgressDialog progress;

	//Bill instance
	private Bill bill;

	//tracking state
	private int state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		//get resolution state of google API client connection from saved instance state
		if (savedInstanceState != null) {
			mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
		}

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_map_service);
		setProgressBarIndeterminateVisibility(false);

		//Initialize UI
		txtAccuracy=(TextView) findViewById(R.id.txt_accuracy);
		txtSpeed=(TextView) findViewById(R.id.txt_speed);
		txtStatus=(TextView) findViewById(R.id.txt_status);
		btnStartTrack=(Button) findViewById(R.id.btn_start_track);
		txtAddressMap=(TextView) findViewById(R.id.txt_address_map);

		// Open Shared Preferences
		mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

		// Get an editor
		mEditor = mPrefs.edit();

		//setup location updates request
		createLocationRequest();

		//set mUpdatesRequest to false
		mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
		mEditor.commit();

		//set initial zoom
		mEditor.putFloat(LocationUtils.KEY_CURRENT_ZOOM,LocationUtils.DEFAULT_ZOOM);
		mEditor.commit();

		//Initialize internal storage
		storage=new InternalStorage(this);

		//get extras
		Bundle extras=getIntent().getExtras();
		serviceId=extras.getString(Service.KEY_ID);
		addressService=extras.getString(Service.KEY_ADDRESS);
		cutomerPhone=extras.getString(Service.KEY_PHONE);
		fromBackground=extras.getBoolean(LocationClientUtils.KEY_FROM_BACKGROUND);

		//save extras on shared preferences
		locationClientUtils = new LocationClientUtils(this);
		locationClientUtils.saveServiceId(serviceId);
		locationClientUtils.savePickUpAddress(addressService);

		//set state
		state=LocationClientUtils.PAUSED;
		locationClientUtils.saveStateTracking(LocationClientUtils.PAUSED);

		//set actionBar title
		ActionBar actionBar=getActionBar();
		actionBar.setTitle(getString(R.string.title_tracking)+" "+serviceId);
	}

	/**
	 * Called when the Activity is made visible.
	 * A connection to Play Services need to be initiated as
	 * soon as the activity is visible. Registers {@code ConnectionCallbacks}
	 * and {@code OnConnectionFailedListener} on the
	 * activities itself.
	 */
	@Override
	protected void onStart() {

		super.onStart();
		Log.i("life cycle", "on Resume state:" + state);
		// display status
		txtStatus.setText(getString(R.string.status_connecting_google));

		//display pickup address
		txtAddressMap.setText(getString(R.string.lbl_pickup_address) + " " + addressService);

		buildGoogleApiClient();
		mGoogleApiClient.connect();
	}
	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("life cycle","on Resume state:"+state);
		
		
		if(fromBackground || (locationClientUtils.getStateTracking()==LocationClientUtils.STARTED)){
			Intent i=new Intent(this, TrackService.class);  
			stopService(i);
		}
		
		// If the app already has a setting for getting location updates, get it
		if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
			locationUpdatesEnabled = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

			// Otherwise, turn off location updates until requested
		} else {
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
			mEditor.commit();
		}

		//setup map and location client if not exists
		setUpMapIfNeeded();

		//if need restore map from background
		if(ifRestoreMapFromBackground()){

			Log.i("onResume()","restore from background");

			state=locationClientUtils.getStateTracking();
			locationUpdatesEnabled=true;
			reDrawTrack();
		}

		//change color btn track
		updateBtnTrack();

		//log current zoom
		Log.i("onresume()", "zoom:" + currentZoom);
	}

	/**
	 * Called when the Activity is no longer visible at all.
	 * Stop updates and disconnect.
	 */
	@Override
	public void onStop() {


		Log.i("life cycle", "on Stop");

		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}

		// if the service is already started
		if(state==LocationClientUtils.STARTED){

			// If the client is connected
			if (mGoogleApiClient.isConnected()) {
				stopLocationUpdates();
			}

			// After disconnect() is called, the client is considered "dead".
			mGoogleApiClient.disconnect();

			// run tracking in background
			Intent i=new Intent(this, TrackService.class);  
			startService(i);
		}

		//save current zoom
		mEditor.putFloat(LocationUtils.KEY_CURRENT_ZOOM, currentZoom);
		mEditor.commit();
		Log.i("onStop()", "zoom:" + currentZoom);

		super.onStop();
	}

	@Override
	public void onPause() {

		Log.i("life cycle", "on Pause");

		// Save the current setting for updates
		mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, locationUpdatesEnabled);
		mEditor.commit();

		super.onPause();
	}

	@Override
	protected void onDestroy() {

		Log.i("MapService", "onDestroy()");

		super.onDestroy();
	}

	@Override
	public void onBackPressed() {

		if(state!=LocationClientUtils.STARTED){
			super.onBackPressed();
		}else{
			Toast.makeText(this,getString(R.string.notif_leave_track_map),Toast.LENGTH_LONG).show();
		}
	}

	protected synchronized void buildGoogleApiClient() {

		Log.i(TAG,"Entry to buildGoogleApiClient");
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}
	/**
	 * Saves the resolution state.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
	}

	/**
	 * Handles Google Play Services resolution callbacks.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CODE_RESOLUTION:
				retryConnecting();
				break;
		}
	}

	private void retryConnecting() {
		mIsInResolution = false;
		if (!mGoogleApiClient.isConnecting()) {
			mGoogleApiClient.connect();
		}
	}

	/**
	 * Called when {@code mGoogleApiClient} is connected.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "GoogleApiClient connected");
		// display status to UI
		txtStatus.setText(getString(R.string.status_success_connection));

		Location lastKnowLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

		moveCameraToLocation(lastKnowLocation);

		if(fromBackground || (locationClientUtils.getStateTracking()==LocationClientUtils.STARTED)){
			reStartTracking();
		}
	}

	/**
	 * Called when {@code mGoogleApiClient} connection is suspended.
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "GoogleApiClient connection suspended");
		retryConnecting();
	}

	/**
	 * Called when {@code mGoogleApiClient} is trying to connect but failed.
	 * Handle {@code result.getResolution()} if there is a resolution
	 * available.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// Show a localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(
					result.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							retryConnecting();
						}
					}).show();
			return;
		}
		// If there is an existing resolution error being displayed or a resolution
		// activity has started before, do nothing and wait for resolution
		// progress to be completed.
		if (mIsInResolution) {
			return;
		}
		mIsInResolution = true;
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (IntentSender.SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
			retryConnecting();
		}
	}

    /**
     * Updates action button to start or stop tracking.
     */
	private void updateBtnTrack(){

		Log.i("updatebtntrack","mupdatesreuest:"+Boolean.toString(locationUpdatesEnabled));

		if(locationUpdatesEnabled){
			btnStartTrack.setBackgroundColor(Color.RED);
			btnStartTrack.setText(getString(R.string.lbl_btn_end_track));
		}else{
			btnStartTrack.setBackgroundColor(Color.rgb(141,174,13));
			btnStartTrack.setText(getString(R.string.lbl_btn_start_track));
		}

		//If there is an attempt in course
		if(attemptSendTracking){
			btnStartTrack.setEnabled(false);
		}else{
			btnStartTrack.setEnabled(true);
		}
	}

	private void setUpMapIfNeeded() {

		if (mMap == null) {

			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_service_track)).getMap();
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
	}

    /**
     * Setup {@code mLocationRequest}
     */
	protected void createLocationRequest() {

		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
	}

	protected void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	}

    /**
     * Moves map camera map to a specific location.
     * @param location
     */
	private void moveCameraToLocation(Location location){

		float cameraZoom=mMap.getMaxZoomLevel()-2;
		LatLng point;

		if(location!=null){
			if (mPrefs.contains(LocationUtils.KEY_CURRENT_ZOOM)) {
				currentZoom = mPrefs.getFloat(LocationUtils.KEY_CURRENT_ZOOM, cameraZoom);
				cameraZoom=currentZoom;

			} else {
				mEditor.putFloat(LocationUtils.KEY_CURRENT_ZOOM,cameraZoom);
				mEditor.commit();
			}

			point=new LatLng(location.getLatitude(), location.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, cameraZoom));
		}

	}

	/**
	 * Report location updates to the UI.
	 * @param location The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {
		
		Log.i(this.toString(),"onLocationChanged:"+location.toString());
		
		//update UI with sensor information
		publishSensors(location);

		//if tracking come from background
		if(ifRestoreMapFromBackground()){

			if(mPrefs.contains(LocationClientUtils.KEY_END_LOCATION)){
				String coordinates=mPrefs.getString(LocationClientUtils.KEY_END_LOCATION,"");
				prevLocation=LocationClientUtils.locationFromString(coordinates);
				
			}
		}

		// check if accuracy is good and if there are three point at least
		if(checkSensorForDrawLine(location)){
			
			//dismiss gps progress
			dismissProgressGPS();

			if(prevLocation==null){

				prevLocation=location;
				markStartLocation(prevLocation);				
				setTrackCoordinate(prevLocation);
				
			}else{

				if(lastLocation==null){
					lastLocation=location;

				}else if(prevLocation!=null && lastLocation!=null){
					prevLocation=lastLocation;
					lastLocation=location;
				}

				setTrackCoordinate(lastLocation);
				drawLine(prevLocation,lastLocation);

				//get and save distance on shared preferences 

				accumulatedDistance=locationClientUtils.getDistance();

				accumulatedDistance+=LocationClientUtils.calculateDistanceBetween(prevLocation, lastLocation);
				locationClientUtils.saveDistance(accumulatedDistance);

				//adjust zoom camera
				if(!pointInScreen(lastLocation)){
					//Log.i("zoom camera","Se encuentra en el area");
					adjustZoomCamera();
				}
			}
		}

	}

    /**
     * checks if the accuracy is good to draw a line.
     * @param location
     * @return
     */
	private boolean checkSensorForDrawLine(Location location){

		boolean success=false;
		float accuracy=location.getAccuracy();
		float speed=location.getSpeed();
		
		if(accuracy<=LocationUtils.MIN_ACCURACY && speed>=LocationUtils.STOP_SPEED){
			
			success= true;

		}else if(accuracy<=LocationUtils.MAX_ACCURACY && prevLocation==null){
			
			success= true;
		}

		return success;
	}
	/** update UI with sensor information */
	private void publishSensors(Location location){
		
		float accuracy=location.getAccuracy();
		float speed=location.getSpeed();
		
		if(accuracy<=(LocationUtils.MIN_ACCURACY-6)){
			txtStatus.setText(getString(R.string.status_excelent_gps_signal)+" : "+accuracy+"m");
		}else if(accuracy<=LocationUtils.MIN_ACCURACY){
			txtStatus.setText(getString(R.string.status_good_gps_signal)+" : "+accuracy+"m");
		}else{
			txtStatus.setText(getString(R.string.status_bad_gps_signal)+" : "+accuracy+"m");
		}

		// set sensor information to UId
		txtAccuracy.setText(LocationUtils.getDistanceKm(accumulatedDistance)+" Km");
		txtSpeed.setText(LocationUtils.getSpeed(speed)+" km/h");
	}
	
	private boolean ifRestoreMapFromBackground(){

		if (mPrefs.contains(LocationClientUtils.KEY_SERVICE_ID)) {

			if( locationClientUtils.getServiceId().equals(serviceId)
					&& (fromBackground || locationClientUtils.getStateTracking()==LocationClientUtils.STARTED)
					&& (!locationClientUtils.isFileTrackEmpty(serviceId))){

				return true;
			}
		} 
		return false;
	}

    /**
     * Draws a line to connect two points on the map.
     * @param pointStart
     * @param pointEnd
     */
	private void drawLine(Location pointStart,Location pointEnd){

		PolylineOptions options=new PolylineOptions();
		options.add(new LatLng(pointStart.getLatitude(),pointStart.getLongitude()),
				new LatLng(pointEnd.getLatitude(),pointEnd.getLongitude())).color(getResources().getColor(R.color.blue_primary));

		mMap.addPolyline(options);
		//Log.i(DEBUG_TAG,"draw line to:"+pointStart.toString()+" from:"+pointEnd.toString());
	}

    /**
     * Gets all save locations to draw complete route.
     */
	private void reDrawTrack(){

		String coordinates;
		String[] poolCoordinates;
		ArrayList<LatLng> points=new ArrayList<LatLng>();
		PolylineOptions options=new PolylineOptions();

		if(storage.isFileExists(LocationClientUtils.FILE_TRACK+"_"+serviceId)){

			coordinates=storage.readFile(LocationClientUtils.FILE_TRACK+"_"+serviceId);
			
			Log.i(this.toString()+"reDrawTrack()","coordinates"+coordinates);
			poolCoordinates=coordinates.split(";");

			for(int i=0; i<poolCoordinates.length; i++){

				try{

					if(i==0){
						Location startLocation=LocationClientUtils.locationFromString(poolCoordinates[i]);
						markStartLocation(startLocation);
					}

					String lat=poolCoordinates[i].split(",")[0];
					String lng=poolCoordinates[i].split(",")[1];

					LatLng latLng=new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

					points.add(latLng);

				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
				}

			}

			if(points.size()>=2){

				options.addAll(points).color(getResources().getColor(R.color.blue_primary));
			}

			mMap.addPolyline(options);
		}
	}

    /**
     * Calculates if  a location is within the screen.
     * @param location
     * @return
     */
	private boolean pointInScreen(Location location){

		boolean inScreen=true;

		VisibleRegion visibleRegion= mMap.getProjection().getVisibleRegion();

		if(visibleRegion!=null){

			double cameraLeftLng=visibleRegion.farLeft.longitude;
			//Log.i("camera left",cameraLeftLng+"");
			double cameraRightLng=visibleRegion.farRight.longitude;
			//Log.i("camera right",cameraRightLng+"");
			double cameraTopLat=visibleRegion.farLeft.latitude;
			//Log.i("camera top",cameraTopLat+"");
			double cameraBottomLat=visibleRegion.nearLeft.latitude;
			//Log.i("camera bottom",cameraBottomLat+"");

			double currentLat=location.getLatitude();
			//Log.i("current lat",currentLat+"");
			double currentLng=location.getLongitude();
			//Log.i("current lng",currentLng+"");

			if(currentLng<cameraLeftLng || currentLng>cameraRightLng || currentLat<cameraBottomLat || currentLat>cameraTopLat){
				inScreen=false;
			}
		}

		return inScreen;
	}

    /**
     * Increases the zoom map wheh the route is too long.
     */
	private void adjustZoomCamera(){

		mMap.moveCamera(CameraUpdateFactory.zoomOut());

		currentZoom=mMap.getCameraPosition().zoom;
		mEditor.putFloat(LocationUtils.KEY_CURRENT_ZOOM,currentZoom);
		mEditor.commit();

		Log.i("adjustZoomCamera()","zoom:"+mPrefs.getFloat(LocationUtils.KEY_CURRENT_ZOOM, 30));

	}

    /**
     * Saves this location into internal storage.
     * @param location
     */
	private void setTrackCoordinate(Location location){


		if(location!=null){
			

			String coordinates=location.getLatitude()+","+location.getLongitude();

			//save Final Location into shares preferences
			locationClientUtils.saveEndLocation(location);

			// saves this coordinate into file
			String oldCoordinates=storage.readFile(LocationClientUtils.FILE_TRACK+"_"+serviceId);

			if(!TextUtils.isEmpty(oldCoordinates)){
				oldCoordinates=oldCoordinates+";";
			}
            storage.saveFile(LocationClientUtils.FILE_TRACK + "_" + serviceId, oldCoordinates+coordinates);

		}
	}

	private void markStartLocation(Location location){

		LatLng latlng=LocationUtils.getLatLng(location);
		mMap.addMarker(new MarkerOptions().position(latlng));

	}

	private void markEndLocation(Location location){

		if(location!=null){
			LatLng latlng=LocationUtils.getLatLng(location);
			mMap.addMarker(new MarkerOptions()
					.position(latlng)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		}

	}
	/** 
	 * Invoked by the "btn_start_track" button
	 * @param v
	 */
	public void changeStateTrack(View v){

		if(locationUpdatesEnabled){

			DialogFragment finishDialog = new FinishServiceDialogFragment();
			finishDialog.show(getSupportFragmentManager(), "finishDialog");

		}else{
			startTracking();
		}
		updateBtnTrack();

	}

    /**
     * Stop periodical updates and gets route information.
     */
	private void stopTracking(){

			if(mGoogleApiClient.isConnected()){

				markEndLocation(lastLocation);

				// update status tracking process
				txtStatus.setText(getString(R.string.status_end_track));

				locationUpdatesEnabled = false;
				stopLocationUpdates();
				mGoogleApiClient.disconnect();

				mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, locationUpdatesEnabled);
				mEditor.putFloat(LocationUtils.KEY_ACCUMULATED_DISTANCE, 0);
				mEditor.commit();

				//stop tracking time
				endTime=System.currentTimeMillis();

				//get address from google in Asynctask
				String finalLocationString=locationClientUtils.getEndLocation();
				(new GetAddressTask(this)).execute(LocationClientUtils.locationFromString(finalLocationString));


			}else{
                txtStatus.setText(getString(R.string.status_waiting_connection));
			}




	}


    /**
     * Start a periodical updates and setup initial tracking to trace the route.
     */
	private void startTracking(){

		if(mGoogleApiClient.isConnected()){

			//Change state to start track
			Log.i("startTrack", "change state to start track");
			state=LocationClientUtils.STARTED;
			locationClientUtils.saveStateTracking(LocationClientUtils.STARTED);

			//show progress to expect good gps signal
            showProgressGPS();

			//create coordinate files
            storage.saveFile(LocationClientUtils.FILE_TRACK + "_" + serviceId, "");
			//Log.i("startTrack()","debug file"+storage.readFile(LocationClientUtils.FILE_TRACK+"_"+serviceId));

			// update status tracking process
            txtStatus.setText(getString(R.string.status_getting_gps_signal));

			//restart values
			locationUpdatesEnabled = true;
			accumulatedDistance=0;
			locationClientUtils.saveDistance(accumulatedDistance);


			//start periodical updates
            startLocationUpdates();

			// update shared preferences
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, locationUpdatesEnabled);
			mEditor.commit();

			//save start time
			startTime=System.currentTimeMillis();
			locationClientUtils.saveStartTime(startTime);

		}else{
			txtStatus.setText(getString(R.string.status_waiting_connection));
			mGoogleApiClient.connect();
		}

	}


    /**
     * Resumes tracking when app back to the background
     */
	private void reStartTracking(){



		if(mGoogleApiClient.isConnected()){

			//Change state to start track
			Log.i("startTrack","change state to re-start track");
			state=LocationClientUtils.STARTED;
			locationClientUtils.saveStateTracking(LocationClientUtils.STARTED);

			// update status tracking process
            txtStatus.setText(getString(R.string.status_getting_gps_signal));

			//restart values
			locationUpdatesEnabled = true;

			//start periodical updates
            startLocationUpdates();

			// update shared preferences
			mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, locationUpdatesEnabled);
			mEditor.commit();

		}else{
			txtStatus.setText(getString(R.string.status_waiting_connection));
			mGoogleApiClient.connect();
		}


	}

    /**
     * Send bill attempt.
     */
	private void sendBill(){


		//stop time of tracking service
		String startTimeString=Long.toString(locationClientUtils.getStartTime());
		String endTimeString=Long.toString(endTime);

		//get filename
		File trackFile=getFileStreamPath(LocationClientUtils.FILE_TRACK+"_"+serviceId);

		//debug accumulated distance
		Log.i("stopTrack","accumulated distance:"+accumulatedDistance);

		if(accumulatedDistance<=1){
			accumulatedDistance=1;
		}

		//get last camera position
		String cameraPosition=mMap.getCameraPosition().zoom+"/"+
				mMap.getCameraPosition().target.latitude+","+mMap.getCameraPosition().target.longitude;


		//create Bill object for save on Internet
		bill=new Bill();
		bill.id="null";
		bill.distance=Math.round(locationClientUtils.getDistance())+"";

		Log.i("stopTrack","distancia redondeada:"+Math.round(accumulatedDistance)+"");

		bill.time=startTimeString+","+endTimeString;
		//bill.endLocation=finalLocation.getLatitude()+","+finalLocation.getLongitude();
		bill.endLocation=locationClientUtils.getEndLocation();

		bill.endAddress=finalAddress;
		bill.file=LocationClientUtils.FILE_TRACK+"_"+serviceId;
		bill.serviceId=serviceId;
		bill.trackFile=storage.readFile(trackFile.getName())+"*"+cameraPosition;

		//save bill in Internet from asynchronous task
		BillTrackTask billTracktask=new BillTrackTask(trackFile,cameraPosition);
		billTracktask.execute(bill);

		state=LocationClientUtils.COMPLETED;
		locationClientUtils.saveStateTracking(LocationClientUtils.COMPLETED);
	}

    /**
     * Add bill to bill list that could not sent to REST API.
     * @param bill
     */
	private void addFailBill(Bill bill){

		JSONArray jsonArray;
		if(storage.isFileExists(LocationClientUtils.FILE_FAILED_BILLS)){

			try{
				String bills=storage.readFile(LocationClientUtils.FILE_FAILED_BILLS);

				jsonArray=new JSONArray(bills);
				jsonArray.put(bill.serviceId);

				storage.saveFile(LocationClientUtils.FILE_FAILED_BILLS, jsonArray.toString());

				addFileBill(bill);

			}catch(JSONException e){
				e.printStackTrace();
			}

		}else{
			jsonArray=new JSONArray();
			jsonArray.put(bill.serviceId);

			storage.saveFile(LocationClientUtils.FILE_FAILED_BILLS, jsonArray.toString());

			addFileBill(bill);
		}

	}

    /**
     * Add bill to internal storage.
     * @param bill
     */
	private void addFileBill(Bill bill){

		if(bill!=null){
			String billString=bill.toJson();
            storage.saveFile(LocationClientUtils.FILE_BILLS + "_" + bill.serviceId, billString);

		}

	}


	/**
	 * Async task to send bill to REST API.
	 */
	private class BillTrackTask extends AsyncTask <Bill,Void,Bill>{

		//private File trackFile;
		//private String cameraPosition;

		public BillTrackTask(File trackFile,String cameraPosition){
			//this.trackFile=trackFile;
			//this.cameraPosition=cameraPosition;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			attemptSendTracking=true;
			//updateBtnTrack();

		}

		@Override
		protected void onPostExecute(Bill billResponse) {

			super.onPostExecute(billResponse);

			boolean success=false;
			String resultMessage="";
			
			Intent i=new Intent(MapServiceActivity.this,ResultServiceActivity.class);
			
			if(billResponse!=null){

				//resultMessage=getString(R.string.msg_success_result_service);
				resultMessage=billResponse.paymentMessage; //send payment response message to result activity

				if(billResponse.paymentResult.equals(Bill.KEY_PAYMENT_RESULT_OK)){
					success=true;
				}

				state=LocationClientUtils.COMPLETED;
				locationClientUtils.saveStateTracking(LocationClientUtils.COMPLETED);

				//freeing space from internal storage if the service was updated successful
				if(storage.isFileExists(LocationClientUtils.FILE_TRACK+"_"+serviceId)){
					deleteFile(LocationClientUtils.FILE_TRACK+"_"+serviceId);
				}
				
				//put bill info
				i.putExtra(ResultServiceActivity.TAG_TOTAL, billResponse.billPrice);
				i.putExtra(ResultServiceActivity.TAG_TOTAL_KM, billResponse.billDistance);
				i.putExtra(ResultServiceActivity.TAG_TOTAL_TIME, billResponse.billTime);
				i.putExtra(ResultServiceActivity.TAG_TOTAL_SPEED, billResponse.billSpeed);

			}else{
				resultMessage=getString(R.string.msg_failed_result_service);

				// add bill info to failed bills file
				addFailBill(bill);

			}

			attemptSendTracking=false;
			

			i.putExtra(ResultServiceActivity.TAG_RESULT_MESSAGE, resultMessage);
			i.putExtra(ResultServiceActivity.TAG_SUCCESS, success);

			setProgressBarIndeterminateVisibility(false);
			startActivity(i);
			updateBtnTrack();
			finish();
		}

		@Override
		protected Bill doInBackground(Bill... bill) {

			//boolean success=false;
			Bill billResponse;

			/*String startTimeString=Long.toString(startTime);
			String endTimeString=Long.toString(endTime);*/

			BillController billController=new BillController(MapServiceActivity.this);
			billResponse=billController.addBill(bill[0]);
			return billResponse;

		}

	}
	/**
	 * An AsyncTask that get address in the background.
	 */
	protected class GetAddressTask extends AsyncTask<Location, Void, String> {


		Context localContext;

		public GetAddressTask(Context context) {

			super();
			localContext = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected String doInBackground(Location... params) {
			/*
			 * Get a new geocoding service instance, set for localized addresses. This example uses
			 * android.location.Geocoder, but other geocoders that conform to address standards
			 * can also be used.
			 */
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());


			Location location = params[0];
			List <Address> addresses = null;

			// Try to get an address for the current location. Catch IO or network problems.
			try {

				/*
				 * Call the synchronous getFromLocation() method with the latitude and
				 * longitude of the current location. Return at most 1 address.
				 */
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1
						);

			} catch (IOException exception1) {

				Log.e("GetAddressTask", "IO exception getFromLocation");
				exception1.printStackTrace();
				return (getString(R.string.notif_adress_not_found));

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = getString(
						R.string.illegal_argument_exception,
						location.getLatitude(),
						location.getLongitude()
						);
				// Log the error and print the stack trace
				Log.e("GetAddressTask", errorString);
				exception2.printStackTrace();
				return (getString(R.string.notif_adress_not_found));
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {


				Address address = addresses.get(0);

				String addressText = getString(R.string.address_output_string,

						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ?
								address.getAddressLine(0) : "",

								// Locality is usually a city
								address.getLocality()
						);

				return addressText;

			} else {
				return getString(R.string.notif_adress_not_found);
			}
		}

		@Override
		protected void onPostExecute(String address) {

			setProgressBarIndeterminateVisibility(false);
			finalAddress=address;
			sendBill();
		}
	}

	public static class FinishServiceDialogFragment extends DialogFragment {

		private MapServiceActivity mListener;
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.msg_want_finish_service)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mListener.stopTracking();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FinishServiceDialogFragment.this.getDialog().cancel();
				}
			});
			return builder.create();
		}

		public void onAttach(Activity activity) {
			super.onAttach(activity);
			try {
				// Instantiate the NoticeDialogListener so we can send events to the host
				mListener = (MapServiceActivity) activity;

			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement NoticeDialogListener");
			}
		}
	}


	/**
	 * Show a dialog to waiting a gps good signal.
	 */
	private void showProgressGPS(){
		if (progress == null || !progress.isShowing()){
			progress = new ProgressDialog(this);
			progress.setCancelable(false);
			progress.setCanceledOnTouchOutside(false);
			progress.setMessage(getString(R.string.msg_waiting_for_estable_gps));				
			progress.show();
		}
	}

	private void dismissProgressGPS(){

		if(progress!=null){
			progress.dismiss();
			progress = null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_call, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action buttons
		switch(item.getItemId()) {
			case R.id.call_customer_item:

				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", cutomerPhone, null));
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
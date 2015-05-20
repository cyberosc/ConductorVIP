/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acktos.conductorvip.android;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.acktos.conductorvip.R;
import com.google.android.gms.maps.model.LatLng;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {
	
	//ACCURACY VALUES
	public static final float  MIN_ACCURACY=12.0f;
	public static final float MAX_ACCURACY=16.0f;
	public static final float STOP_SPEED=0.5f;
	
    // Debugging tag for the application
    public static final String APPTAG = "LocationSample";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES ="com.acktos.conductorvip.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED ="com.acktos.conductorvip.KEY_UPDATES_REQUESTED";
    
    
    //key for storing accumulated distance
    public static final String KEY_ACCUMULATED_DISTANCE="com.acktos.conductorvip.KEY_ACCUMULATED_DISTANCE";
    
    //key for save current zoom map
    public static final String KEY_CURRENT_ZOOM="com.acktos.conductorvip.KEY_CURRENT_ZOOM";
    
    //default zoom
    public static final float DEFAULT_ZOOM=15;
    

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    
    // Meters  per kilometer
    public static final int METERS_PER_KILOMETER = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 5;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return context.getString(
                    R.string.latitude_longitude,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }
    
    
    public static LatLng getLatLng(Location location){
    	
    	if(location !=null){
    		return new LatLng(location.getLatitude(),location.getLongitude());
    	}else{
    		return null;
    	}
    }
    
    public static float getSpeed(float speed){
    	
    	int speedKm;
    	speedKm=(int) (speed*3600)/1000;
    	return speedKm;
    }
    
    public static float getDistanceKm(float distance){
    	try{
    		//Log.i("result kilometers",(float) Math.rint((distance/METERS_PER_KILOMETER)*100)/100 +"");
    		return (float) Math.rint((distance/METERS_PER_KILOMETER)*100)/100;
    		
    	}catch(ArithmeticException e){
    		return 0;
    		
    	}
    	
    }
}

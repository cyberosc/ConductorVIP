package com.acktos.conductorvip.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import com.acktos.conductorvip.LoginActivity;
import com.acktos.conductorvip.PendingServicesActivity;
import com.acktos.conductorvip.R;
import com.acktos.conductorvip.android.Encrypt;
import com.acktos.conductorvip.android.HttpRequest;
import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.entities.Car;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class CarController {

	public final static String TAG="conductorvip_debug";
	private Context context;
	private static final String TOKEN="ee8099de39d5167fe135baf92fa0df1c";
	private static final String RESPONSE_SUCCESS_CODE="200";
	private static final String RESPONSE_TAG="response";
	private static final String FIELDS_TAG="fields";
	public static final String KEY_ENCRYPT="encrypt";

	public CarController(Context context){
		this.context=context;
	}

	public Car carLogin(String email,String pswrd){

		Car carResult=null;

		Log.i("pswrd:",email);
		Log.i("pswrd:",pswrd);
		
		String encrypt=Encrypt.md5(email+pswrd+TOKEN);

		HttpRequest httpRequest=new HttpRequest(context.getString(R.string.url_car_login));

		httpRequest.setParam(Car.KEY_EMAIL,email);
		httpRequest.setParam(Car.KEY_PSWRD, pswrd);
		httpRequest.setParam(KEY_ENCRYPT, encrypt);

		String responseData=httpRequest.postRequest();

		if(responseData!=null){
			Log.i("debug login data",responseData);
			try {
				
				carResult=new Car();
				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					Log.i("login result","verdadero");
					
					JSONObject fieldsObject=jsonObject.getJSONObject(FIELDS_TAG);
					carResult.id=fieldsObject.getString(Car.KEY_ID);
					carResult.email=fieldsObject.getString(Car.KEY_EMAIL);
					carResult.cc=fieldsObject.getString(Car.KEY_CC);
					carResult.name=fieldsObject.getString(Car.KEY_NAME);
					carResult.plate=fieldsObject.getString(Car.KEY_PLATE);
					
				}else{
					Log.i("login result","falso");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return carResult;
	} 
	
	public boolean profileExists(){
		
		boolean exists=false;
		InternalStorage storage=new InternalStorage(context);
		if(storage.isFileExists(LoginActivity.FILE_CAR_PROFILE)){
			
			try {
				String profileString=storage.readFile(LoginActivity.FILE_CAR_PROFILE);
				Log.i("debug file profile",profileString);
				JSONObject jsonObject=new JSONObject(profileString);
				if(!TextUtils.isEmpty(jsonObject.getString(Car.KEY_ID))){
					exists=true;
				}else{
					exists=false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return exists;
			}
		}
		return exists;
	}
	
	public String getCarId(){
		
		String carId="";
		InternalStorage storage=new InternalStorage(context);
		
		if(storage.isFileExists(LoginActivity.FILE_CAR_PROFILE)){
			
			String contentFile=storage.readFile(LoginActivity.FILE_CAR_PROFILE);
			
			if(!TextUtils.isEmpty(contentFile)){
				try{
					JSONObject jsonObject=new JSONObject(contentFile);
					if(!jsonObject.isNull(Car.KEY_ID)){
						carId=jsonObject.getString(Car.KEY_ID);
					}
				}catch(JSONException e){
					e.printStackTrace();
					Log.e("getCarId()","error json");
				}
				
			}
		}
		
		return carId;
	}

	public void saveRegistrationId(String registerId){

		String carId;
		carId=getCarId();
		String userAgent=Build.BRAND+" "+Build.MODEL+" Android "+Build.VERSION.RELEASE;

		String encrypt=Encrypt.md5(carId+registerId+userAgent+TOKEN);

		HttpRequest httpRequest=new HttpRequest(context.getString(R.string.url_set_register_id));

		httpRequest.setParam(Car.KEY_ID,carId);
		httpRequest.setParam(Car.KEY_MOBILE_ID, registerId);
		httpRequest.setParam(Car.KEY_USER_AGENT, userAgent);
		httpRequest.setParam(KEY_ENCRYPT, encrypt);


		String responseData=httpRequest.postRequest();

		if(responseData!=null){
			Log.i(TAG,"response register id:"+responseData);
			try {

				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					Log.i(TAG,"save register id successful");

				}else{
					Log.i(TAG,"save register id failed");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
}
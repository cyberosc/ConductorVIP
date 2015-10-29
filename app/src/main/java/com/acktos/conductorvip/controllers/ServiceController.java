package com.acktos.conductorvip.controllers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.acktos.conductorvip.R;
import com.acktos.conductorvip.android.Encrypt;
import com.acktos.conductorvip.android.HttpRequest;
import com.acktos.conductorvip.entities.Car;
import com.acktos.conductorvip.entities.Service;

/**
 * Class for handle all REST API connections related to the {@link Service} entity
 * and performing data processing before delivery to presentation.
 */
public class ServiceController {

	private Context context;
	private static final String TOKEN="ee8099de39d5167fe135baf92fa0df1c";
	private static final String RESPONSE_SUCCESS_CODE="200";
	private static final String RESPONSE_TAG="response";
	
	//internal states
	public static final int IN_TRACKING=72;
	public static final int COMPLETED=73;
	public static final int PENDING_FOR_BILL=74;
	public static final int PENDING_FOR_START=75;

	/**
	 * Public constructor thorough context reference.
	 * @param context
	 */
	public ServiceController(Context context){
		this.context=context;
	}

    @Deprecated
	public ArrayList<Service> getAvailableRequest(){

		ArrayList<Service> services=null;


		HttpRequest httpRequest=new HttpRequest(context.getString(R.string.url_get_service_requests));

		String serviceId="null";
		CarController carController=new CarController(context);
		String carId=carController.getCarId();
		String myServices="null";

		httpRequest.setParam(Service.KEY_ID,serviceId);
		httpRequest.setParam(Service.KEY_CAR_ID, carId);
		httpRequest.setParam(Service.KEY_MY_SERVICES, myServices);

		String encrypt=Encrypt.md5(serviceId+carId+myServices+TOKEN);
		httpRequest.setParam(Service.KEY_ENCRYPT, encrypt);

		String responseData=httpRequest.postRequest();
		if(responseData!=null){

			
			Log.i("debug response request",responseData);
			try {
				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					services=new ArrayList<Service>();
					JSONArray jsonArray=jsonObject.getJSONArray("fields");


					for(int i=0;i<jsonArray.length();i++){
						JSONObject itemObject=jsonArray.getJSONObject(i);
						services.add(addItemService(itemObject));
					}

				}
			} catch (JSONException e) {
				e.getMessage();
			}
		}

		return services;
	}

    /**
     * Get services assigned to this driver.
     * @return {@code ArrayList<Service> services}
     */
	public ArrayList<Service> getMyServices(){

		ArrayList<Service> services=null;

		HttpRequest httpRequest=new HttpRequest(context.getString(R.string.url_get_my_services));

		//get car id
		CarController carController=new CarController(context);
		String carId=carController.getCarId();
		String myServices="1";
		String serviceId="null";

		httpRequest.setParam(Service.KEY_ID,serviceId);
		httpRequest.setParam(Service.KEY_CAR_ID, carId);
		httpRequest.setParam(Service.KEY_MY_SERVICES, myServices);

		String encrypt=Encrypt.md5(serviceId+carId+myServices+TOKEN);
		httpRequest.setParam(Service.KEY_ENCRYPT, encrypt);

		String responseData=httpRequest.postRequest();
		if(responseData!=null){

			
			Log.i("debug response request",responseData);
			try {
				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					
					services=new ArrayList<Service>();
					JSONArray jsonArray=jsonObject.getJSONArray("fields");
					//Log.i("tamaño jsonArray",jsonArray.length()+"");
					for(int i=0;i<jsonArray.length();i++){
						JSONObject itemObject=jsonArray.getJSONObject(i);
						services.add(addItemService(itemObject));
						//Log.i("debug item object",itemObject.toString(1));
					}
					//Log.i("debug arrayList",services.toString());
				}
			} catch (JSONException e) {
				e.getMessage();
			}
		}

		return services;
	}

    /**
     * Add a service object to list
     * @param jsonObject
     * @return {@link Service}
     */

	private Service addItemService(JSONObject jsonObject){

		Service service=new Service();
		try {
			service.jsonToObject(jsonObject);
		} catch (JSONException e) {
			e.getMessage();
		}
		return service;
	}

    /**
     * Updates assign driver field to a request service, through REST API.
     * @param serviceId
     * @param state
     * @return true if process was successfully, otherwise false.
     */
	public boolean takeService(String serviceId,String state){

		boolean success=false;

		//get mobile id
		CarController carController=new CarController(context);
		String mobileId=carController.getCarId();

		//String mobileId="1";
		//Log.i("service id:",serviceId);
		String encrypt=Encrypt.md5(serviceId+mobileId+state+TOKEN);

		Log.i("params take service:",serviceId+mobileId+state+TOKEN);
		HttpRequest httpRequest=new HttpRequest(context.getString(R.string.url_take_service));

		httpRequest.setParam(Service.KEY_ID,serviceId);
		httpRequest.setParam(Service.KEY_CAR_ID, mobileId);
		httpRequest.setParam(Service.KEY_STATE, state);
		httpRequest.setParam(Service.KEY_ENCRYPT, encrypt);
		


		String responseData=httpRequest.postRequest();

		if(responseData!=null){
			Log.i("debug take service data",responseData);
			try {
				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					Log.i("debug take service","verdadero");
					success= true;
				}else{
					Log.i("debug take service","falso");
					success= false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return success;
	} 

	public static ArrayList<Service> fromJsonArray(String jsonString){

		ArrayList<Service> services=null;

		try{
			JSONArray jsonArray=new JSONArray(jsonString);
			services=new ArrayList<Service>();

			if(jsonArray.length()>0){

				for(int i=0; i<jsonArray.length();i++){

					JSONObject jsonObject=jsonArray.getJSONObject(i);

					Service itemService=new Service();
					itemService.serviceFromJson(jsonObject);
					services.add(itemService);
				}
			}

		}catch(JSONException e){
			Log.e("fromJsonArray","parse json error");
			e.printStackTrace();
		}

		return services;
	}
}

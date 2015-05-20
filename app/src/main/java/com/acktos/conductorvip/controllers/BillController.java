package com.acktos.conductorvip.controllers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.acktos.conductorvip.LocationClientUtils;
import com.acktos.conductorvip.MapServiceActivity;
import com.acktos.conductorvip.R;
import com.acktos.conductorvip.android.Encrypt;
import com.acktos.conductorvip.android.HttpRequest;
import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.entities.Bill;
import com.acktos.conductorvip.entities.Service;

public class BillController {

	private Context context;
	private static final String TOKEN="ee8099de39d5167fe135baf92fa0df1c";
	private static final String RESPONSE_SUCCESS_CODE="200";
	private static final String RESPONSE_TAG="response";
	private static final String FIELDS_TAG="fields";

	public BillController(Context context){
		this.context=context;
	}

	public Bill addBill(Bill bill){

		HttpRequest httpPost=new HttpRequest(context.getString(R.string.url_add_bill));


		httpPost.setParam(Bill.KEY_ID, bill.id);
		httpPost.setParam(Bill.KEY_DISTANCE, bill.distance);
		httpPost.setParam(Bill.KEY_TIME, bill.time);
		httpPost.setParam(Bill.KEY_END_LOCATION, bill.endLocation);
		httpPost.setParam(Bill.KEY_END_ADDRESS, bill.endAddress);
		httpPost.setParam(Bill.KEY_FILE, bill.file);
		httpPost.setParam(Bill.KEY_SERVICE_ID, bill.serviceId);
		httpPost.setParam(Bill.KEY_FILE_TRACK, bill.trackFile);

		String encrypt=Encrypt.md5(bill.id+bill.distance+bill.time+bill.endLocation+bill.endAddress+bill.file+bill.serviceId+TOKEN);
		Log.i("debug encrypt service",
				bill.id+"*"+
						bill.distance+"*"+
						bill.time+"*"+
						bill.endLocation+"*"+
						bill.endAddress+"*"+
						bill.file+"*"+
						bill.serviceId+"*"+
						"set coordenadas:"+bill.trackFile+"*"+
						TOKEN);
		httpPost.setParam(Bill.KEY_ENCRYPT, encrypt);

		Bill billResponse=null;
		
		String responseData=httpPost.postRequest();
		Log.i("response add bill","response add bill"+responseData);
		if(responseData!=null){
			
			try {
				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);

				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					
					billResponse=new Bill();
					JSONObject jsonObjectBill=jsonObject.getJSONObject(FIELDS_TAG);
					Log.i(this.toString()+"addBill()",jsonObjectBill.toString(1));
					billResponse.jsonToBill(jsonObjectBill);
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}	
		}
		
		return billResponse;
	}
	
	public Bill getBillDetail(String serviceId){
		
		Bill bill=null;
		
		
		HttpRequest httpRequest=new HttpRequest(context.getString(R.string.url_get_bill_detail));

		httpRequest.setParam(Service.KEY_SERVICE,serviceId);

		String encrypt=Encrypt.md5(serviceId+TOKEN);
		httpRequest.setParam(Service.KEY_ENCRYPT, encrypt);

		String responseData=httpRequest.postRequest();
		if(responseData!=null){

			
			Log.i("getBillDetail","response data:"+responseData);
			try {
				JSONObject jsonObject=new JSONObject(responseData);
				String responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					bill=new Bill();
					JSONObject jsonObjectBill=jsonObject.getJSONObject("fields");
					Log.i("getBillDetail 2",jsonObjectBill.toString(1));
					bill.jsonToBill(jsonObjectBill);
					Log.i("getBillDetail 1","bill:");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return bill;
	}

	public static ArrayList<String> getFailedList(Context context){

		ArrayList<String> failedBills=new ArrayList<String>();
		InternalStorage storage=new InternalStorage(context);

		if(storage.isFileExists(LocationClientUtils.FILE_FAILED_BILLS)){

			//Log.i("debug adapter","el archivo si existe:"+storage.readFile(MapServiceActivity.FILE_FAILED_BILLS));
			String billFailedString=storage.readFile(LocationClientUtils.FILE_FAILED_BILLS);

			try {
				JSONArray jsonArray=new JSONArray(billFailedString);
				//failedBills=new ArrayList<String>();
				for(int i=0; i<jsonArray.length();i++){

					failedBills.add(jsonArray.getString(i));
				}

				//Log.i("debug list","bill list:"+failedBills.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			Log.e("getFailedList","El archivo no existe");
		}

		return failedBills;
	}

	public static boolean isFailedBill(ArrayList<String> billFailed,String serviceId){

		boolean isFailed=false;
		isFailed=billFailed.contains(serviceId);
		return isFailed;
	}
}

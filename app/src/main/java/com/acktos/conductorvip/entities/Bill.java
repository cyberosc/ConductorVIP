package com.acktos.conductorvip.entities;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * A simple DAO class for encapsulating an entity  through the REST API.
 * Representing a bill that is generated when the service has finished.
 */

public class Bill {

    /**Unique bill ID*/
	public String id;

    /**Distance covered during operation*/
	public String distance;

    /**Elapsed time during operation*/
	public String time;

    /**Coordinates where the operation ends*/
	public String endLocation;

    /**Address where operation ends*/
	public String endAddress;

    /**File name that containing all route coordinates*/
	public String file;

    /**Reference to {@link Service}*/
	public String serviceId;

    /** String that containing all route coordinates*/
	public String trackFile;

    /** Computed distance by server*/
	public String billDistance;

    /** Computed time by server*/
	public String billTime;

    /** Final of operation*/
	public String billPrice;

    /**Computed speed by server*/
	public String billSpeed;

    /**Computed minutes by server*/
	public String billMinute;

    /**Computed kilometers by server*/
	public String billKilometer;

    /**Current operation rate*/
	public String billRate;

    /**Current operation increase*/
	public String billIncrease;

    /**Payment result attributes*/
    public String paymentResult;

    /**Payment result message*/
    public String paymentMessage;
	
	
	public static final String KEY_ID="id";
	public static final String KEY_DISTANCE="distancia";
	public static final String KEY_TIME="tiempo";
	public static final String KEY_END_LOCATION="geolocation";
	public static final String KEY_END_ADDRESS="address";
	public static final String KEY_FILE="archivo";
	public static final String KEY_SERVICE_ID="servicio";
	public static final String KEY_ENCRYPT="encrypt";
	public static final String KEY_FILE_TRACK="tracking";
	public static final String KEY_PAYMENT_RESULT="payment";
    public static final String KEY_PAYMENT_RESULT_OK="ok";
	public static final String KEY_PAYMENT_MESSAGE="message_payment";
	
	@Override
	public String toString(){
		return toJson();
	}
	
	public String toJson(){
		return "{\"id\":\""+id+
				"\",\"distance\":\""+distance+
				"\",\"time\":\""+time+"\","+ 
				"\"endLocation\":\""+endLocation+
				"\",\"endAddress\":\""+endAddress+
				"\",\"file\":\""+file+
				"\",\"serviceId\":\""+serviceId+
				"\",\"trackFile\":\""+trackFile+
				"\",\"billDistance\":\""+billDistance+
				"\",\"billTime\":\""+billTime+"\","+ 
				"\"billPrice\":\""+billPrice+
				"\",\"billSpeed\":\""+billSpeed+
				"\",\"billMinute\":\""+billMinute+
				"\",\"billKilometer\":\""+billKilometer+
				"\",\"billRate\":\""+billRate+
				"\",\"billIncrease\":\""+billIncrease+
				"\",\"billPrice\":\""+billPrice+
				"\",\""+KEY_PAYMENT_RESULT+"\":\""+paymentResult+
				"\",\""+KEY_PAYMENT_MESSAGE+"\":\""+paymentMessage+"\"}";
	}

	public void ToObject(String jsonString) throws JSONException{

		JSONObject jsonObject=new JSONObject(jsonString);
		Log.i("debug json object",jsonObject.toString(1));
		billFromJson(jsonObject);	

	}
	public void jsonToObject(JSONObject jsonObject) throws JSONException{

		id=jsonObject.getString("id");
		distance=jsonObject.getString("distancia");
		time=jsonObject.getString("tiempo");
		endLocation=jsonObject.getString("geolocation");
		endAddress=jsonObject.getString("address");
		file=jsonObject.getString("archivo");
		serviceId=jsonObject.getString("servicio");
		billDistance=jsonObject.getString("distancia");
		billTime=jsonObject.getString("servicio");
		billPrice=jsonObject.getString("total");
		billSpeed=jsonObject.getString("velocidad");
		billMinute=jsonObject.getString("valor_minuto");
		billKilometer=jsonObject.getString("valor_km");
		billRate=jsonObject.getString("tarifa");
		billIncrease=jsonObject.getString("incremento");
        paymentResult=jsonObject.getString(KEY_PAYMENT_RESULT);
        paymentMessage=jsonObject.getString(KEY_PAYMENT_MESSAGE);

			
	}
	
	public void jsonToBill(JSONObject jsonObject) throws JSONException{
		
		//Log.i("jsonToBill()",jsonObject.toString(1));
		try{
			if(!jsonObject.isNull("distancia")) billDistance=jsonObject.getString("distancia");
			if(!jsonObject.isNull("tiempo")) billTime=jsonObject.getString("tiempo");
			if(!jsonObject.isNull("total")) billPrice=jsonObject.getString("total");
			if(!jsonObject.isNull("velocidad")) billSpeed=jsonObject.getString("velocidad");
			if(!jsonObject.isNull("valor_min")) billMinute=jsonObject.getString("valor_minuto");
			if(!jsonObject.isNull("valor_km")) billKilometer=jsonObject.getString("valor_km");
			if(!jsonObject.isNull("tarifa")) billRate=jsonObject.getString("tarifa");
			if(!jsonObject.isNull("incremento")) billIncrease=jsonObject.getString("incremento");
            if(!jsonObject.isNull(KEY_PAYMENT_RESULT)) paymentResult=jsonObject.getString(KEY_PAYMENT_RESULT);
            if(!jsonObject.isNull(KEY_PAYMENT_MESSAGE)) paymentMessage=jsonObject.getString(KEY_PAYMENT_MESSAGE);

			
		}catch(Exception e){
			e.printStackTrace();
		}
		
			
	}
	
	
	public void billFromJson(JSONObject jsonObject) throws JSONException{
		
		id=jsonObject.getString("id");
		distance=jsonObject.getString("distance");
		time=jsonObject.getString("time");
		endLocation=jsonObject.getString("endLocation");
		endAddress=jsonObject.getString("endAddress");
		file=jsonObject.getString("file");
		serviceId=jsonObject.getString("serviceId");
		trackFile=jsonObject.getString("trackFile");
		
	}
}

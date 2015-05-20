package com.acktos.conductorvip.entities;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Bill {
	
	public String id;
	public String distance;
	public String time;
	public String endLocation;
	public String endAddress;
	public String file;
	public String serviceId;
	public String trackFile;
	
	public String billDistance;
	public String billTime;
	public String billPrice;
	public String billSpeed;
	public String billMinute;
	public String billKilometer;
	public String billRate;
	public String billIncrease;
	
	
	public static final String KEY_ID="id";
	public static final String KEY_DISTANCE="distancia";
	public static final String KEY_TIME="tiempo";
	public static final String KEY_END_LOCATION="geolocation";
	public static final String KEY_END_ADDRESS="address";
	public static final String KEY_FILE="archivo";
	public static final String KEY_SERVICE_ID="servicio";
	public static final String KEY_ENCRYPT="encrypt";
	public static final String KEY_FILE_TRACK="tracking";
	
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
				"\",\"billPrice\":\""+billPrice+"\"}";
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
			
	}
	
	public void jsonToBill(JSONObject jsonObject) throws JSONException{
		
		Log.i("jsonToBill()",jsonObject.toString(1));
		try{
			if(!jsonObject.isNull("distancia")) billDistance=jsonObject.getString("distancia");
			if(!jsonObject.isNull("tiempo")) billTime=jsonObject.getString("tiempo");
			if(!jsonObject.isNull("total")) billPrice=jsonObject.getString("total");
			if(!jsonObject.isNull("velocidad")) billSpeed=jsonObject.getString("velocidad");
			if(!jsonObject.isNull("valor_min")) billMinute=jsonObject.getString("valor_minuto");
			if(!jsonObject.isNull("valor_km")) billKilometer=jsonObject.getString("valor_km");
			if(!jsonObject.isNull("tarifa")) billRate=jsonObject.getString("tarifa");
			if(!jsonObject.isNull("incremento")) billIncrease=jsonObject.getString("incremento");
			
			
			Log.i(this.toString()+"jsonToBill()","distancia:"+billDistance+
					"tiempo:"+billTime+
					"speed:"+billSpeed+
					"total:"+billPrice);
			
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

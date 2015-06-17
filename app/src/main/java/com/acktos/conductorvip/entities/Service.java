package com.acktos.conductorvip.entities;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Service {
	
	public String id;
	public String address;
	public String carId;
	public String driver;
	public String state;
	public String customer;
	public String date;
	public String immediate;
	public String customerPhone;
	
	public static final String KEY_ID="id";
	public static final String KEY_ADDRESS="direccion";
	public static final String KEY_CAR_ID="movil";
	public static final String KEY_PHONE="celular";
	public static final String KEY_DRIVER="conductor";
	public static final String KEY_STATE="estado";
	public static final String KEY_CUSTOMER="cliente";
	public static final String KEY_DATE="fecha_recogida";
	public static final String KEY_IMMEDIATE="inmediato";
	public static final String KEY_RESERVED="Reservado";
	public static final String KEY_ENCRYPT="encrypt";
	public static final String KEY_SERVICE="servicio";
	public static final String KEY_MY_SERVICES="misservicios";

	//states 
	public static final String STATE_APROVED="Aprobado";
	public static final String STATE_PENDING="Pendiente";
	public static final String STATE_FINISHED="Completado";
	public static final String STATE_CANCELED="Cancelado";
	public static final String STATE_DRIVER_ARRIVED="Llego el conductor";
	public static final String STATE_PENDING_FOR_BILL="Pendiente por FACTURAR";
	public static final String STATE_PENDING_FOR_START="Esperando tu llegada al lugar";
	public static final String TRANSLATE_STATE_PENDING_FOR_START="Esperando a iniciar servicio";


	
	//code states
	public static final String CODE_APROVED="2";
	public static final String CODE_DRIVER_ARRIVED="9";
	
	
	public String toJson(){
		return "{\"id\":\""+id+"\"," +
				"\"address\":\""+address+"\"," +
				"\"carId\":\""+carId+"\","+
				"\"driver\":\""+driver+"\"," +
				"\"state\":\""+state+"\"," +
				"\"customer\":\""+customer+ "\"," +
				"\"date\":\""+date+"\"," +
				"\"customerPhone\":\""+customerPhone+"\"," +
				"\"immediate\":\""+immediate+"\"}";
	}

	public void ToObject(String jsonString) throws JSONException{

		JSONObject jsonObject=new JSONObject(jsonString);
		jsonToObject(jsonObject);	

	}
	public void jsonToObject(JSONObject jsonObject) throws JSONException{

		id=jsonObject.getString(KEY_ID);
		address=jsonObject.getString(KEY_ADDRESS);
		carId=jsonObject.getString(KEY_CAR_ID);
		driver=jsonObject.getString(KEY_DRIVER);
		state=jsonObject.getString(KEY_STATE);
		customer=jsonObject.getString(KEY_CUSTOMER);
		date=jsonObject.getString(KEY_DATE);
		immediate=jsonObject.getString(KEY_IMMEDIATE);
		customerPhone=jsonObject.getString(KEY_PHONE);
		
			
	}
	
	
	public void serviceFromJson(JSONObject jsonObject) throws JSONException{
		
		id=jsonObject.getString("id");
		address=jsonObject.getString("address");
		carId=jsonObject.getString("carId");
		driver=jsonObject.getString("driver");
		state=jsonObject.getString("state");
		customer=jsonObject.getString("customer");
		date=jsonObject.getString("date");
		immediate=jsonObject.getString("immediate");
		customerPhone=jsonObject.getString("customerPhone");
		
	}
}

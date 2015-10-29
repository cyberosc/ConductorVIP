package com.acktos.conductorvip.entities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple DAO class for encapsulating an entity  through the REST API.
 * It represents a driver who performs a service.
 */

public class Car {


    /**Unique car id*/
	public String id;

    /**Driver email*/
	public String email;

    /**Driver identification*/
	public String cc;

    /**Driver name*/
	public String name;

    /**Vehicle plate*/
	public String plate;
	
	
	public static final String KEY_ID="id";
	public static final String KEY_EMAIL="email";
	public static final String KEY_CC="cc";
	public static final String KEY_NAME="nombre";
	public static final String KEY_PLATE="placa";
	public static final String KEY_PSWRD="pswrd";
	public static final String KEY_MOBILE_ID="mobile_id";
	public static final String KEY_COORDINATES="coordenadas";
	public static final String KEY_CURRENT_DATE="horacoordenada";
	public static final String KEY_USER_AGENT="user_agent";

	
	public String toJson(){
		return "{\"id\":\""+id+"\",\"email\":\""+email+"\",\"cc\":\""+cc+"\","+ 
				"\"name\":\""+name+"\",\"plate\":\""+plate+"\"}";
	}

	public void ToObject(String jsonString) throws JSONException{

		JSONObject jsonObject=new JSONObject(jsonString);
		Log.i("debug json object",jsonObject.toString(1));
		jsonToObject(jsonObject);	

	}
	public void jsonToObject(JSONObject jsonObject) throws JSONException{

		id=jsonObject.getString(KEY_ID);
		email=jsonObject.getString(KEY_EMAIL);
		cc=jsonObject.getString(KEY_CC);
		name=jsonObject.getString(KEY_NAME);
		plate=jsonObject.getString(KEY_PLATE);
				
	}
	
	public void serviceFromJson(JSONObject jsonObject) throws JSONException{
		
		id=jsonObject.getString("id");
		email=jsonObject.getString("email");
		cc=jsonObject.getString("cc");
		name=jsonObject.getString("name");
		plate=jsonObject.getString("plate");
		
	}
}

/*
 * Layer to network connection 1 July 2014 
 */
package com.acktos.conductorvip.android;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;


public class HttpRequest {
	
	private String url;
	private HttpURLConnection conection;
	private MultipartEntity paramsEntity;
	private boolean isFile=false;
	
	
	
	public HttpRequest(String url) {
		setUrl(url);
		paramsEntity=new MultipartEntity();
	}
	
	public String request(){
		String result="";
		InputStream in=connect();
		result=readStream(in);
		return result;
	}
	
	public InputStream connect(){
		try {
			URL url =new URL(this.url);
			conection=(HttpURLConnection) url.openConnection();
			conection.connect();
			InputStream in =new BufferedInputStream(url.openStream()); 
			return in;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String postRequest(){
		
		String responseData=null;
		HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
	    HttpConnectionParams.setSoTimeout(httpParams, 20000);
	    HttpClient httpclient = new DefaultHttpClient(httpParams);
	    
	 
	    try {
	    	HttpPost httpPost=new HttpPost(url);
	        httpPost.setEntity(paramsEntity);
	        if(isFile){
	        	//httpPost.setHeader("Content-type", "multipart/form-data");	
	        }
	        
	    	//httpPost.setHeader("Accept", "*/*");
	        //httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
	        //httpPost.setHeader("Content-Encoding", "gzip, deflate, compress");
	    	HttpResponse response = httpclient.execute(httpPost);
	    	if(response!=null){
                InputStream in = response.getEntity().getContent();
                responseData=readStream(in);
            }
	    	
	    } catch (UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
       

	    return responseData;
	}
	
	// add a param for POST request, modified for null values;
	public void setParam(String name,String value){
		try {
			if(value!=null){
				paramsEntity.addPart(name, new StringBody(value));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void setfile(String name,File file){
		if(file!=null){
			paramsEntity.addPart(name, new FileBody(file));
			isFile=true;
		}	
	}
	
	public String readStream(InputStream in){
		
		String result="";
		BufferedReader buffer=new  BufferedReader(new InputStreamReader(in));
		
		
		String s="";
		try {
			while((s=buffer.readLine())!=null){
				result+=s;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(buffer!=null){
				try {
					buffer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public void setUrl(String url){
		this.url=url;
	}
	
	private class PhotoUploadResponseHandler implements ResponseHandler<Object> {

	    @Override
	    public Object handleResponse(HttpResponse response)
	            throws ClientProtocolException, IOException {

	        HttpEntity r_entity = response.getEntity();
	        String responseString = EntityUtils.toString(r_entity);
	        Log.d("UPLOAD", responseString);

	        return null;
	    }

	}
	

}

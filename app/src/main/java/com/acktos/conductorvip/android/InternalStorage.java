package com.acktos.conductorvip.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

public class InternalStorage {

	private Context context;

	public InternalStorage(Context context){
		this.context=context;
	}

	public boolean saveFile(String filename, String content){

		try {
			FileOutputStream outputStream;
			outputStream= context.openFileOutput(filename,Context.MODE_PRIVATE);
			outputStream.write(content.getBytes());
			outputStream.close();
			return true;
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			Log.e("Error", "File not found exception");
		} catch (IOException e) {
			//e.printStackTrace();
			Log.e("Error", "IOException");
		}
		return false;
	}
	
	public String readFile(String filename){
		
		String data=null;
		try {
			BufferedReader inputReader=new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
			String inputString;
			StringBuffer stringBuffer=new StringBuffer();

			while((inputString=inputReader.readLine())!=null){
				stringBuffer.append(inputString);
			}
			data=stringBuffer.toString();
			
		} catch (FileNotFoundException e) {
			Log.e("Error file not found",filename+" not found in internal storage");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Error IO exception","IO exception try load"+filename);
			e.printStackTrace();
		}
		finally {

		}
		return data;
	}
	
	public boolean isFileExists(String filename){
		
		File file=context.getFileStreamPath(filename);
		if(file.exists()){
			return true;
		}else{
			return false;
		}
		
	}
}



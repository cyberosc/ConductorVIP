package com.acktos.conductorvip;

import java.util.ArrayList;

import com.acktos.conductorvip.adapters.BillDetailAdapter;
import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.controllers.BillController;
import com.acktos.conductorvip.entities.Bill;
import com.acktos.conductorvip.entities.Service;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

public class BillDetailActivity extends Activity {
	
	private String serviceId;
	private BillController billController;
	private BillDetailAdapter billDetailAdapter;
	private ArrayList<String> billDetailsValues;
	
	//UI references
	private ListView billDetailView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		setContentView(R.layout.activity_bill_detail);
		
		billController=new BillController(this);
		
		//get service id
		Bundle extras=getIntent().getExtras();
		serviceId=extras.getString(Service.KEY_ID);
		//serviceId="6";
		
		//UI references
		billDetailView=(ListView)findViewById(R.id.list_bill_details);
		
		//set actionBar title
		ActionBar actionBar=getActionBar();
		actionBar.setTitle(getString(R.string.title_activity_bill_detail)+" "+serviceId);
		
		//set adapter
		billDetailsValues=new ArrayList<String>();
		billDetailAdapter=new BillDetailAdapter(this, billDetailsValues);
		billDetailView.setAdapter(billDetailAdapter);
		
		InternalStorage storage=new InternalStorage(this);
		
		GetBillDetailTask getBillDetail=new GetBillDetailTask();
		getBillDetail.execute(serviceId);
	}
	
	
	
	
	private class GetBillDetailTask extends AsyncTask<String,Void,Bill>{
		
		@Override
		protected void onPostExecute(Bill billResult) {
			setProgressBarIndeterminateVisibility(false);
			
			if(billResult!=null){
				
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billDistance);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billTime);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billSpeed);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billMinute);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billKilometer);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billRate);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billIncrease);
				Log.i("onpostExecute-getBillDetailTask","distance:"+billResult.billPrice);
		
				
				billDetailsValues.add(billResult.billDistance);
				billDetailsValues.add(billResult.billTime);
				billDetailsValues.add(billResult.billSpeed);
				billDetailsValues.add(billResult.billMinute);
				billDetailsValues.add(billResult.billKilometer);
				billDetailsValues.add(billResult.billRate);
				billDetailsValues.add(billResult.billIncrease);
				billDetailsValues.add(billResult.billPrice);
				
				billDetailAdapter.notifyDataSetChanged();
			}
			
			super.onPostExecute(billResult);
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected Bill doInBackground(String... args) {
			return billController.getBillDetail(serviceId);
		}
		
	}
}

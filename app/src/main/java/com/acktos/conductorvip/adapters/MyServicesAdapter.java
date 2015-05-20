package com.acktos.conductorvip.adapters;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.acktos.conductorvip.MapServiceActivity;
import com.acktos.conductorvip.R;
import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.controllers.BillController;
import com.acktos.conductorvip.controllers.ServiceController;
import com.acktos.conductorvip.entities.Service;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyServicesAdapter extends BaseAdapter {
	
	
	private ArrayList<Service> services;
	private LayoutInflater layoutInflater;
	private ArrayList<String> billFailed;
	private Context context;
	
	
	public MyServicesAdapter(Context context, ArrayList<Service> services){

		this.services=services;
		layoutInflater=LayoutInflater.from(context);
		this.context=context;
		
		billFailed=BillController.getFailedList(context);
		
	}
	@Override
	public int getCount() {
		return services.size();
	}

	@Override
	public Object getItem(int position) {
		return services.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) {
			//convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
			convertView = layoutInflater.inflate(R.layout.my_services_item, null);
			holder = new ViewHolder();
			holder.date = (TextView) convertView.findViewById(R.id.txt_my_date);
			holder.title = (TextView) convertView.findViewById(R.id.txt_my_address);
			holder.subtitle = (TextView) convertView.findViewById(R.id.txt_my_customer);
			holder.id = (TextView) convertView.findViewById(R.id.txt_my_service_id);
			holder.state=(TextView) convertView.findViewById(R.id.txt_my_state);
			
	
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Service service = services.get(position);
		
		holder.title.setText(service.address);
		holder.subtitle.setText(service.customer);
		holder.id.setText(service.id);
		holder.date.setText(service.date);
		
		if(BillController.isFailedBill(billFailed,service.id)){

			holder.state.setText(Service.STATE_PENDING_FOR_BILL);

		}else if(service.state.equals(Service.STATE_CANCELED)){

			holder.state.setText(Service.STATE_CANCELED);

		}else if(service.state.equals(Service.STATE_DRIVER_ARRIVED)){

			holder.state.setText(Service.TRANSLATE_STATE_PENDING_FOR_START);

		}else if(service.state.equals(Service.STATE_APROVED)){

			holder.state.setText(Service.STATE_PENDING_FOR_START);

		}else if(service.state.equals(Service.STATE_FINISHED)){

			holder.state.setText(Service.STATE_FINISHED);
		}
		
		if(service.immediate.equals("1")){
			
			holder.date.setText(context.getString(R.string.now));
		}else{
			holder.date.setText(service.date);
		}

		return convertView;
	}
	

	static class ViewHolder {
		TextView title;
		TextView subtitle;
		TextView date;
		TextView id;
		TextView state;
	}
	
	
}
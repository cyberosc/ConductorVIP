package com.acktos.conductorvip.adapters;

import java.util.ArrayList;

import com.acktos.conductorvip.R;
import com.acktos.conductorvip.entities.Service;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RequestsAdapter extends BaseAdapter {
	
	
	private ArrayList<Service> services;
	private LayoutInflater layoutInflater;
	
	public RequestsAdapter(Context context, ArrayList<Service> services){

		this.services=services;
		layoutInflater=LayoutInflater.from(context);
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
			convertView = layoutInflater.inflate(R.layout.service_list_item, null);
			holder = new ViewHolder();
			holder.color = (View) convertView.findViewById(R.id.color_service);
			holder.title = (TextView) convertView.findViewById(R.id.txt_address);
			holder.subtitle = (TextView) convertView.findViewById(R.id.txt_customer);
			holder.id = (TextView) convertView.findViewById(R.id.txt_service_id);
			
	
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Service service = (Service)services.get(position);
		
		holder.title.setText(service.address);
		holder.subtitle.setText(service.customer);
		holder.id.setText(service.id);
		
		if(service.immediate.equals("0")){
			
			holder.color.setBackgroundColor(Color.rgb(250, 177, 7));
		}else{
			holder.color.setBackgroundColor(Color.rgb(28, 212, 28));
		}

		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView subtitle;
		View color;
		TextView id;
	}
	
	
}


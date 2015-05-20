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

public class BillDetailAdapter extends BaseAdapter {
	
	
	private ArrayList<String> bills;
	private LayoutInflater layoutInflater;
	private String[] titles;
	private String[] measurementUnits;
	
	public BillDetailAdapter(Context context, ArrayList<String> bills){

		this.bills=bills;
		layoutInflater=LayoutInflater.from(context);
		titles=context.getResources().getStringArray(R.array.bill_titles);
		measurementUnits=context.getResources().getStringArray(R.array.measurement_units);
		
	}
	@Override
	public int getCount() {
		return bills.size();
	}

	@Override
	public Object getItem(int position) {
		return bills.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) {
			
			convertView = layoutInflater.inflate(R.layout.bill_detail_item, null);
			holder = new ViewHolder();
			
			holder.title = (TextView) convertView.findViewById(R.id.txt_bill_title);
			holder.value = (TextView) convertView.findViewById(R.id.txt_bill_value);
	
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String value = bills.get(position);
	
		holder.title.setText(titles[position]);
		holder.value.setText(value+" "+measurementUnits[position]);

		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView value;
	}
	
}


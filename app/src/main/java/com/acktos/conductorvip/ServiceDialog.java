package com.acktos.conductorvip;

/**
 * Created by OSCAR ACKTOS on 06/05/2015.
 */

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acktos.conductorvip.controllers.ServiceController;
import com.acktos.conductorvip.entities.Service;


public class ServiceDialog extends DialogFragment {

    public final static String TAG_DIALOG = "service_dialog";
    private static final String TAG = "LocationServiceDebug";
    private View rootView;
    private ProgressBar progressBar;
    private TextView txtAddress;
    private TextView txtMessageDialog;
    private TextView messageView;

    private String customerName;
    private String serviceId;
    private String address;
    private String phone;

    //private OnDataChangeListener dataChangelistener;

    public static ServiceDialog newInstance(String serviceId, String address, String phone, String customerName) {

        ServiceDialog serviceDialog = new ServiceDialog();

        // add argument to new dialog instance.
        Bundle args = new Bundle();
        args.putString(Service.KEY_ID, serviceId);
        args.putString(Service.KEY_ADDRESS, address);
        args.putString(Service.KEY_PHONE, phone);
        args.putString(Service.KEY_CUSTOMER, customerName);
        serviceDialog.setArguments(args);

        return serviceDialog;
    }

    /*public interface OnDataChangeListener{
        public void onDataChangeCarList(ArrayList<Car> cars);
    }*/

   /* @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof OnDataChangeListener){
            dataChangelistener=(OnDataChangeListener) activity;
        }else{
            throw new ClassCastException(activity.toString()+" must implement OnDataChangeListener");
        }
    }*/

    @Override
    public Dialog onCreateDialog(Bundle SaveInstanceState) {

        serviceId = getArguments().getString(Service.KEY_ID);
        address = getArguments().getString(Service.KEY_ADDRESS);
        phone = getArguments().getString(Service.KEY_PHONE);
        customerName = getArguments().getString(Service.KEY_CUSTOMER);

        Log.i(TAG, "serviceId:" + serviceId);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.service_dialog, null);

        txtAddress = (TextView) rootView.findViewById(R.id.txt_address_dialog);
        txtMessageDialog = (TextView) rootView.findViewById(R.id.message_service_dialog);
        messageView = (TextView) rootView.findViewById(R.id.result_service_dialog);
        progressBar = (ProgressBar) rootView.findViewById(R.id.driver_here_progress);

        txtMessageDialog.setText(String.format(getString(R.string.msg_service_dialog), customerName));
        txtAddress.setText(address);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(getString(R.string.create_update_car));
        builder.setView(rootView);
        builder.setPositiveButton(getString(R.string.driver_here), null);

        builder.setNegativeButton(getString(R.string.call), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        progressBar.setVisibility(View.VISIBLE);
                        messageView.setVisibility(View.VISIBLE);

                        DriverArrivedTask driverArrivedTask = new DriverArrivedTask(progressBar, messageView, getActivity());
                        driverArrivedTask.execute(serviceId);

                    }
                });

            }
        });

        return alertDialog;
    }

    private class DriverArrivedTask extends AsyncTask<String, Void, Boolean> {

        private ServiceController serviceController;
        private ProgressBar progressBar;
        private TextView messageDialog;
        private Activity context;


        public DriverArrivedTask(ProgressBar progressBar, TextView messageDialog, Activity context) {

            this.progressBar = progressBar;
            this.messageDialog = messageDialog;
            this.context = context;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);
            messageDialog.setVisibility(View.GONE);

            Fragment fragmentServiceDialog = context.getFragmentManager().findFragmentByTag(TAG_DIALOG);

            if (success) {
                if (fragmentServiceDialog != null) {
                    ServiceDialog serviceDialog = (ServiceDialog) fragmentServiceDialog;
                    serviceDialog.dismiss();
                }

                Intent i = new Intent(getActivity(), MapServiceActivity.class);

                i.putExtra(Service.KEY_ID, serviceId);
                i.putExtra(Service.KEY_ADDRESS, address);
                i.putExtra(Service.KEY_PHONE,phone);
                startActivity(i);

            } else {
                Toast.makeText(context, context.getString(R.string.notif_driver_arrived_failed), Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(success);
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            messageDialog.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {


            boolean result = false;
            serviceController = new ServiceController(context);
            result = serviceController.takeService(params[0], Service.CODE_DRIVER_ARRIVED);
            return result;
        }

    }

}


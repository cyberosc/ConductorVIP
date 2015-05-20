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

import com.acktos.conductorvip.android.InternalStorage;
import com.acktos.conductorvip.controllers.BillController;
import com.acktos.conductorvip.controllers.ServiceController;
import com.acktos.conductorvip.entities.Bill;
import com.acktos.conductorvip.entities.Service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SendBillDialog extends DialogFragment {

    public final static String TAG_DIALOG = "send_bill_dialog";
    private static final String TAG = "SendBillDialogDebug";
    private View rootView;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView message;
    private Boolean attemptSendBill=false;
    private InternalStorage storage;


    private String serviceId;


    //private OnDataChangeListener dataChangelistener;

    public static SendBillDialog newInstance(String serviceId) {

        SendBillDialog sendBillDialog = new SendBillDialog();

        // add argument to new dialog instance.
        Bundle args = new Bundle();
        args.putString(Service.KEY_ID, serviceId);
        sendBillDialog.setArguments(args);

        return sendBillDialog;
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

        storage = new InternalStorage(getActivity());
        serviceId = getArguments().getString(Service.KEY_ID);

        Log.i(TAG, "serviceId:" + serviceId);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.send_bill_dialog, null);

        progressBar = (ProgressBar) rootView.findViewById(R.id.send_bill_progress);
        progressText = (TextView) rootView.findViewById(R.id.progress_text_send_bill);
        message = (TextView) rootView.findViewById(R.id.message_send_bill);

        message.setText(getString(R.string.msg_send_bill_dialog).toString());



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(getString(R.string.create_update_car));
        builder.setView(rootView);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel,null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        progressBar.setVisibility(View.VISIBLE);
                        progressText.setVisibility(View.VISIBLE);

                        SendBillTask sendBillTask = new SendBillTask(progressBar, progressText, getActivity());
                        sendBillTask.execute(serviceId);

                    }
                });

            }
        });

        return alertDialog;
    }

    // delete this service from file of bill failed services
    private void clearFailedBill(String serviceId) {

        boolean isFind = false;

        String failedBillsString = storage.readFile(LocationClientUtils.FILE_FAILED_BILLS);

        if (!TextUtils.isEmpty(failedBillsString)) {
            try {
                JSONArray jsonArray = new JSONArray(failedBillsString);
                JSONArray newJsonArray = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {

                    if (jsonArray.getString(i).equals(serviceId)) {
                        isFind = true;

                    } else {
                        newJsonArray.put(jsonArray.getString(i));
                    }
                }

                if (isFind) {
                    storage.saveFile(LocationClientUtils.FILE_FAILED_BILLS, newJsonArray.toString());
                    //Log.i("clearfailedbills","new file:"+newJsonArray.toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendBillTask extends AsyncTask<String, Void, Boolean> {

        private BillController billController;
        private ProgressBar progressBar;
        private TextView messageDialog;
        private Activity context;


        public SendBillTask(ProgressBar progressBar, TextView messageDialog, Activity context) {

            this.progressBar = progressBar;
            this.messageDialog = messageDialog;
            this.context = context;

            billController=new BillController(getActivity());
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);
            messageDialog.setVisibility(View.GONE);

            Fragment fragmentSendBillDialog = context.getFragmentManager().findFragmentByTag(TAG_DIALOG);

            if (success) {
                if (fragmentSendBillDialog != null) {
                    SendBillDialog sendBillDialog = (SendBillDialog) fragmentSendBillDialog;
                    sendBillDialog.dismiss();
                }

                Toast.makeText(getActivity(),getString(R.string.notif_send_bill_success),Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getActivity(),getString(R.string.notif_send_bill_failed),Toast.LENGTH_LONG).show();
            }

            attemptSendBill=false;
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

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean result = false;
            Bill billResponse = null;
            Bill bill = new Bill();

            //retrieve bill info from file
            String billString = storage.readFile(LocationClientUtils.FILE_BILLS + "_" + params[0]);

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(billString);

                bill.billFromJson(jsonObject);


                if (!attemptSendBill) {
                    attemptSendBill = true;
                    billResponse = billController.addBill(bill);
                }

                if (billResponse != null) {
                    result = true;
                    //free system resources
                    getActivity().deleteFile(LocationClientUtils.FILE_TRACK + "_" + params[0]);
                    getActivity().deleteFile(LocationClientUtils.FILE_BILLS + "_" + params[0]);

                    // clear failed bill
                    clearFailedBill(params[0]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

    }


}


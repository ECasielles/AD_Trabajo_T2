package com.mercacortex.ad_trabajo_t2.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.mercacortex.ad_trabajo_t2.MyIntentService;

import java.io.File;

import cz.msebera.android.httpclient.Header;


public class ServiceAsyncHttpResponseHandler extends IntentService {

    public ServiceAsyncHttpResponseHandler() {
        super("ServiceAsyncHttpResponseHandler");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String origin = bundle.getString(MyIntentService.INTENT_DATA_SOURCE);
            String destination = bundle.getString(MyIntentService.INTENT_DATA_DESTINATION);
            File downloadFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), destination);

            AsyncHttpClient cliente = new AsyncHttpClient();
            FileAsyncHttpResponseHandler handler = new FileAsyncHttpResponseHandler(downloadFile) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    Intent newIntent = new Intent(MyIntentService.INTENT_ACTION_FAILURE);
                    sendBroadcast(newIntent);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, File response) {
                    Intent newIntent = new Intent(MyIntentService.INTENT_ACTION_SUCCESS);
                    sendBroadcast(newIntent);
                }
            };

            cliente.get(origin, handler);
        }
    }
}

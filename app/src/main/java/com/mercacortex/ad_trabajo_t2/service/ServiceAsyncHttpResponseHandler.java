package com.mercacortex.ad_trabajo_t2.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.mercacortex.ad_trabajo_t2.MyIntentService;

import java.io.File;

import cz.msebera.android.httpclient.Header;


public class ServiceAsyncHttpResponseHandler extends IntentService {

    private static final String TAG = "Service";

    public ServiceAsyncHttpResponseHandler() {
        super("ServiceAsyncHttpResponseHandler");
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            final String origin = bundle.getString(MyIntentService.INTENT_DATA_SOURCE);
            final String destination = bundle.getString(MyIntentService.INTENT_DATA_DESTINATION);
            File downloadFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), destination);

            //Hay que hacer una llamada SÍNCRONA
            //porque el servicio debe esperar la respuesta del remoto
            SyncHttpClient cliente = new SyncHttpClient();

            //Los parámetros deben estar bien definidos
            //para que no se coma la batería del teléfono
            cliente.setTimeout(2000);
            cliente.setMaxRetriesAndTimeout(3, 5000);

            cliente.get(origin, new FileAsyncHttpResponseHandler(downloadFile) {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                    Intent newIntent = new Intent(MyIntentService.INTENT_ACTION_FAILURE);
                    newIntent.putExtra(MyIntentService.INTENT_DATA_SOURCE, origin);
                    LocalBroadcastManager.getInstance(ServiceAsyncHttpResponseHandler.this)
                            .sendBroadcast(newIntent);
                }
                @Override
                public void onSuccess(int statusCode, Header[] headers, File response) {
                    Intent newIntent = new Intent(MyIntentService.INTENT_ACTION_SUCCESS);
                    newIntent.putExtra(MyIntentService.INTENT_DATA_SOURCE, origin);
                    LocalBroadcastManager.getInstance(ServiceAsyncHttpResponseHandler.this)
                            .sendBroadcast(newIntent);
                }
            });
        }
    }
}

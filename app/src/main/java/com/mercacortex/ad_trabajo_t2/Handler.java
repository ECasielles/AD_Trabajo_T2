package com.mercacortex.ad_trabajo_t2;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * Created by usuario on 15/02/18.
 */

public class Handler extends FileAsyncHttpResponseHandler {

    public Handler(File file) {
        super(file);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, File file) {

    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

    }
}

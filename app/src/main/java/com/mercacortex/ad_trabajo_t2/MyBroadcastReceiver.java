package com.mercacortex.ad_trabajo_t2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * SÓLO RECIBE NOTIFICACIONES. No se comunica con la activity
 * por ser menos eficiente. Una activity implementará
 * su propio BroadcastReceiver local.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "MyBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MyIntentService.INTENT_ACTION_FAILURE:
                Log.d(TAG, "Descarga sin éxito");
                break;
            case MyIntentService.INTENT_ACTION_SUCCESS:
                Log.d(TAG, "Descarga con éxito");
                break;
        }
    }

}

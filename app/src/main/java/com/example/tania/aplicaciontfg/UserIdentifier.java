package com.example.tania.aplicaciontfg;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class UserIdentifier {
    private static final int REQUEST_READ_PHONE_STATE = 0;
    private String imei;

    public UserIdentifier(Context contexto) {
        TelephonyManager telephonyManager = (TelephonyManager) contexto.getSystemService(contexto.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        Log.d("RecogerIMEI",telephonyManager.getDeviceId());
    }

    public String getImei() {
        return imei;
    }
}

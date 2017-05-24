package com.example.tania.aplicaciontfg;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.altbeacon.beacon.service.BeaconService;

public class MainActivity extends Activity {
    protected static final String TAG = "TFGAPLICATIONMON";
    BluetoothAdapter mBluetoothAdapter;
    Intent intentBeaconService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "estoy en onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "estoy en onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "estoy en onResume");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 0;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "estoy en onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "estoy en onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "estoy en onDestroy");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void iniciarServicio(View view) {
        Log.i(TAG, "estoy en iniciarServicio");
        Button p1_button = (Button)findViewById(R.id.button2);
        p1_button.setText("Parar");
        if (isMyServiceRunning(ExampleService.class)) {
            terminarServicio(view);
            p1_button.setText("Iniciar");
        }
        intentBeaconService = new Intent(this, ExampleService.class);
        startService(intentBeaconService);
    }

    public void terminarServicio(View view) {
        Log.i(TAG, "estoy en terminarServicio");
        //Intent intent = new Intent(this, ExampleService.class);
        stopService(intentBeaconService);
    }

}
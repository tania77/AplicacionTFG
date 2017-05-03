package com.example.tania.aplicaciontfg;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;
    UserIdentifier infoUsuario;
    //ServerCommunication server;
    TextView textView2;
    double distancia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "estoy en onCreate");
        infoUsuario = new UserIdentifier(this);
        //server = new ServerCommunication();
        textView2 = (TextView) findViewById(R.id.distancia);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 0;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //TextView textView = (TextView) findViewById(R.id.imei);
        //textView.setText(infoUsuario.getImei());
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "estoy en onBeaconServiceConnect");
        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.i(TAG, "estoy en didRangeBeaconsInRegion");
                if (beacons.size() > 0) {
                    distancia = beacons.iterator().next().getDistance();
                    Log.i(TAG, "The first beacon I see tiene ID: "+ distancia +" meters away.");

                    MainActivity.super.runOnUiThread(new Runnable() {
                        public void run()
                        {
                            textView2.setText(Double.toString(distancia));
                        }
                    });

                }
            }
        });
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
                try {
                    Log.i(TAG, "addmonitornotiefier--didenterregion");
                    beaconManager.startRangingBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
                } catch (RemoteException e) {    }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
                Log.i(TAG, "el IMEI en didDetermineStateForRegion es: " + infoUsuario.getImei());

            }
        });





        try {
            Log.i(TAG, "el IMEI en el try es: " + infoUsuario.getImei());
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    }

    }

    @Override
    protected void onRestart() {
        super.onRestart();Log.i(TAG, "estoy en onRestart");

        onBeaconServiceConnect();
    }

}

package com.example.tania.aplicaciontfg;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.BeaconService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;

public class ExampleService extends Service implements BeaconConsumer {
    protected static final String TAG = "TFGAPLICATIONSERV";
    private BeaconManager beaconManager;
    BackgroundPowerSaver backgroundPowerSaver;
    private double distancia;
    private String strdistancia;
    Region regionMonitoring;
    Region regionRanging;
    ServerCommunication serv;
    UserIdentifier infoUsuario;

    public ExampleService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "estoy en onCreate");
        serv = new ServerCommunication();
        infoUsuario = new UserIdentifier(this);
        regionMonitoring = new Region("myMonitoringUniqueId", null, null, null);
        regionRanging = new Region("myRangingUniqueId", null, null, null);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.setBackgroundBetweenScanPeriod(50000l);
        beaconManager.bind(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "estoy en onDestroy");
        try {
            beaconManager.stopMonitoringBeaconsInRegion(regionMonitoring);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            beaconManager.stopRangingBeaconsInRegion(regionRanging);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);

                serv.sendPostToServer("0001", infoUsuario.getImei());

//                RestTemplate restTemplate = new RestTemplate();
//                String url = "https://tfg-tania77.c9users.io/";
//                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//                Log.i(TAG, response.getStatusCode().toString());
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(regionMonitoring);
        } catch (RemoteException e) {
        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");

                    distancia =  beacons.iterator().next().getDistance();
                    strdistancia = Double.toString(distancia);

                    Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), strdistancia, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(regionRanging);
        } catch (RemoteException e) {}
    }
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder result = new StringBuilder();
                    URL url = new URL("https://tfg-tania77.c9users.io:8080/algo");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("idPuerta", "0001");
                    jsonParam.put("idUsuario", "352836063139035");;

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    conn.disconnect();

                    Log.i(TAG, String.valueOf(conn.getResponseCode()));
                    Log.i(TAG , result.toString());

                } catch (ProtocolException e1) {
                    e1.printStackTrace();
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });

        thread.start();
    }
}

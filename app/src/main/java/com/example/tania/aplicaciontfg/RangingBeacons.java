package com.example.tania.aplicaciontfg;


import android.content.Context;

import org.altbeacon.beacon.BeaconManager;

public class RangingBeacons {
    private BeaconManager beaconManager;
    public RangingBeacons(Context context) {
        beaconManager = BeaconManager.getInstanceForApplication(context);
    }
}

package com.fsgtech.detectme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by jomari on 10/17/2017.
 */

public class RequestUserPermission {

    private Activity activity;

    private static int REQUEST_PERMISSION = 1;
    private static String [] PERMISSION_LOCATION = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public RequestUserPermission(Activity activity) {
        this.activity = activity;
    }

    public void verifyLocationPermissions() {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_LOCATION,
                    REQUEST_PERMISSION
            );
        }
    }
}

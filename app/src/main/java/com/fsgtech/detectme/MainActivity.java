package com.fsgtech.detectme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsgtech.detectme.credentials.EmailAuthentication;
import com.fsgtech.detectme.emailsending.SendMailTask;
import com.fsgtech.detectme.listeners.MyLocationListener;
import com.fsgtech.detectme.listeners.MyPhoneStateListener;
import com.fsgtech.detectme.receiver.NetworkChangeReceiver;
import com.jaredrummler.android.device.DeviceName;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  {

    public static Context context;

    TextView textView;
    Button button;
    ListView listView;

    String manufacturer, model, deviceName, codename, name;

    final static int REQUEST_LOCATION = 199;

    MyPhoneStateListener myPhoneStateListener;
    MyLocationListener myLocationListener;

    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> desc = new ArrayList<>();

    Handler handler = new Handler();
    Runnable refresh;

    ArrayAdapterDisplay listViewArrayAdapter;

    NetworkChangeReceiver networkChangeReceiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        networkChangeReceiver =  new NetworkChangeReceiver();
        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        listView = (ListView) findViewById(R.id.listview);
        button = (Button) findViewById(R.id.sendButton);

        RequestUserPermission requestUserPermission = new RequestUserPermission(this);
        requestUserPermission.verifyLocationPermissions();

        myPhoneStateListener = new MyPhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        myLocationListener = new MyLocationListener(this);
        myLocationListener.buildGoogleApiClient();

        final DeviceName.DeviceInfo deviceInfo = DeviceName.getDeviceInfo(context);
        manufacturer = deviceInfo.manufacturer;
        name = deviceInfo.marketName;
        model = deviceInfo.model;
        codename = deviceInfo.codename;
        deviceName = deviceInfo.getName();

        if (checkNetworkStatus(context).equals("Not Connected")) {
//            showSettingsAlert();
        }

        refresh = new Runnable() {
            public void run() {
                // Do something
                if (!checkNetworkStatus(context).equals("Not Connected")) {
                    if (MyLocationListener.loc) {
                        title.clear();
                        desc.clear();
                        resultOfAll();
                        listViewArrayAdapter = new ArrayAdapterDisplay(context, title, desc);
                        listView.setAdapter(listViewArrayAdapter);
                    } else {
//                        Toast.makeText(context, "GPS is turn off. Please turn it on.", Toast.LENGTH_SHORT).show();
                        Log.e("Detect Me:", "Location is off");
                    }

                } else {
//                    Toast.makeText(context, "Not connected to internet. Please make sure you have internet connection.", Toast.LENGTH_SHORT).show();
                    Log.e("Detect Me: ", "No internet connection");
                }
                handler.postDelayed(refresh, 2000);
            }
        };
        handler.post(refresh);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            sendEmail();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        myLocationListener.connGAC();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myLocationListener.dconnGAC();
        finish();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult()", Integer.toString(resultCode));

        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        Toast.makeText(context, "Location enabled by user!", Toast.LENGTH_LONG).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(context, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //If user presses allow
//                    Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        if (checkNetworkStatus(context).equals("Not Connected")){
            alertDialog.setTitle("Network Settings");
            alertDialog.setMessage("Network connection is off. Do you want to turn it on?");
            alertDialog.setPositiveButton("Wifi Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Mobile Data Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    context.startActivity(intent);
                }
            });
            alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        // Showing Alert Message
        alertDialog.show();
    }

    public String checkNetworkStatus(Context context) {

        String networkStatus = "";
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Check Wifi
        final android.net.NetworkInfo wifi = manager.getActiveNetworkInfo();
        //Check for mobile data
        final android.net.NetworkInfo mobile = manager.getActiveNetworkInfo();


        if (wifi != null || mobile != null) {
            if (wifi.getType() == ConnectivityManager.TYPE_WIFI) {
                networkStatus = "Wifi";
            } else if (mobile.getType() == ConnectivityManager.TYPE_MOBILE) {
                networkStatus = "Mobile Data";
            } else {
                networkStatus = "Not Connected";
            }
        } else {
            networkStatus = "Not Connected";
        }

        return networkStatus;
    }

    public String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            //2G
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return  "CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";
            //3G
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVD0_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVD0_A";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVD0_B";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            //4G
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            default:
                return "Unknown";
        }
    }

    public String getNetworkOperatorName(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tm.getNetworkOperatorName();
        return networkOperator;
    }

    public void resultOfAll(){
        title.add("Phone Model");
        desc.add("Name: " + name + "\n" +
                "Model: " + model + "\n" +
                "Code Name: " + codename + "\n" +
                "Device Name: " + deviceName + "\n" +
                "Manufacturer: " + manufacturer + "\n");

        title.add("Network Information");
        desc.add("Data Source: " + checkNetworkStatus(context) + "\n" +
                "Cell Network Operator: " + getNetworkOperatorName(context) + "\n" +
                "Cell Data: " + getNetworkClass(context) + "\n" +
                "Signal Strength: " + myPhoneStateListener.getSignalStrengthValue() + "\n");

        title.add("Location Information");
        desc.add("Latitude: " + myLocationListener.getLatitude() + "\n" +
                "Longitude: " + myLocationListener.getLongitude() + "\n" +
                "Accuracy: (meters) " + String.format(Locale.US, "%.2f", myLocationListener.getAccuracy()) + "\n");


    }

    public void sendEmail(){
        EmailAuthentication EA = new EmailAuthentication();
        String toEmail = "jomari.benito@digipay.ph";
        String emailSubject = "DIGIPAY - Detect Me ";

        ArrayList<String> col = new ArrayList<String>();
        for (int i = 0 ; i<=listView.getChildCount() - 1; i++) {
            View view = listView.getChildAt(i);
            String getTitle = ((TextView) view.findViewById(R.id.textView)).getText().toString();
            String getDesc = ((TextView) view.findViewById(R.id.textView2)).getText().toString();

            col.add(getTitle);
            col.add(getDesc);
        }

        StringBuilder sb = new StringBuilder();
        for(String str : col){
            sb.append(str).append("\n"); //separating contents using semi colon
        }
        String emailBody = sb.toString();


        new SendMailTask(MainActivity.this).execute(EA.getEMAIL(),
                EA.getACCESS(), toEmail, emailSubject, emailBody);
    }
}

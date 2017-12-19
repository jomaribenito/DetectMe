package com.fsgtech.detectme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.fsgtech.detectme.ConnectivityDialog;
import com.fsgtech.detectme.MainActivity;

/**
 * Created by jomari on 12/18/2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final MainActivity mainActivity = new MainActivity();
        String status = mainActivity.checkNetworkStatus(context);

        Toast.makeText(context, status + "", Toast.LENGTH_SHORT).show();
        if (status.equals("Not Connected")) {
            Intent i = new Intent(context, ConnectivityDialog.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}

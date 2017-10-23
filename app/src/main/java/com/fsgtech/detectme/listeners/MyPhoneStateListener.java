package com.fsgtech.detectme.listeners;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

/**
 * Created by jomari on 10/19/2017.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    private int signalStrengthValue;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        if (signalStrength.isGsm()) {
            if (signalStrength.getGsmSignalStrength() != 99)
                signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
            else
                signalStrengthValue = signalStrength.getGsmSignalStrength();
        } else {
            signalStrengthValue = signalStrength.getCdmaDbm();
        }
    }

    public int getSignalStrengthValue() {
        return signalStrengthValue;
    }
}

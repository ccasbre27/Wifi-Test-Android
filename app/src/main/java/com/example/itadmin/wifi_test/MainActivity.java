package com.example.itadmin.wifi_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiManager mWifiManager;
    WifiReceiver mWifiReceiver;
    List<ScanResult> wifiList;
    TextView txtvSignal;

    /*

    http://androidxref.com/4.2_r1/xref/frameworks/base/wifi/java/android/net/wifi/WifiWatchdogStateMachine.java#103

    http://stackoverflow.com/questions/13932724/getting-wifi-signal-strength-in-android

    http://stackoverflow.com/questions/18831442/how-to-get-signal-strength-of-connected-wifi-android


     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtvSignal = (TextView) findViewById(R.id.txtvSignal);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }
        mWifiReceiver = new WifiReceiver();
        IntentFilter mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, mIntentFilter);
        mWifiManager.startScan();
    }

    public void onPause() {
        unregisterReceiver(mWifiReceiver);
        super.onPause();
    }

    public void onResume() {
        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            int state = mWifiManager.getWifiState();
            int maxLevel = 5;
            if (state == WifiManager.WIFI_STATE_ENABLED) {
                // Get Scanned results in an array List
                wifiList = mWifiManager.getScanResults();
                // Iterate on the list
                for (ScanResult result : wifiList) {
                    //The level of each wifiNetwork from 0-5
                    int level = WifiManager.calculateSignalLevel(
                            result.level,maxLevel);
                    String SSID = result.SSID;
                    String capabilities = result.capabilities;
                    // TODO add your own code.

                    txtvSignal.setText(txtvSignal.getText().toString() + level);
                }
            }
        }
    }

}

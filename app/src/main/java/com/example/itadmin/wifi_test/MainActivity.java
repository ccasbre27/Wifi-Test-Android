package com.example.itadmin.wifi_test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity
{

    WifiManager mWifiManager;
    WifiReceiver mWifiReceiver;
    List<ScanResult> wifiList;
    TextView txtvSignal;

    /*

    http://androidxref.com/4.2_r1/xref/frameworks/base/wifi/java/android/net/wifi/WifiWatchdogStateMachine.java#103

    http://stackoverflow.com/questions/13932724/getting-wifi-signal-strength-in-android

    http://stackoverflow.com/questions/18831442/how-to-get-signal-strength-of-connected-wifi-android

    Mobile data
    http://stackoverflow.com/questions/18399364/get-signal-strength-of-wifi-and-mobile-data

    http://mobilemerit.com/android-app-for-detecting-wifi-signal-with-source-code/

     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtvSignal = (TextView) findViewById(R.id.txtvSignal);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // se verifica si el wifi está habilitado
        if (!mWifiManager.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }

        mWifiReceiver = new WifiReceiver();

        // se registra el receiver
        // IntentFilter mIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        IntentFilter mIntentFilter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, mIntentFilter);

        mWifiManager.startScan();
    }

    public void onPause()
    {
        unregisterReceiver(mWifiReceiver);
        super.onPause();
    }

    public void onResume()
    {
        //registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver
    {

        // Es disparado cada vez que la señal del wifi cambia
        public void onReceive(Context c, Intent intent)
        {
            int state = mWifiManager.getWifiState();

            // cantidad de rayas
            int maxLevel = 5;

            if (state == WifiManager.WIFI_STATE_ENABLED)
            {

                final WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
                //System.out.println("Connection Info " + connectionInfo.toString() + " " + connectionInfo.getRssi());


                String value = "";

                // Excellent >-50 dBm
                if (connectionInfo.getRssi() > - 50)
                {
                    value = " Excellent ";
                }
                // Good -50 to -60 dBm
                else if (connectionInfo.getRssi() > -60)
                {
                    value = " Good ";
                }
                // Fair -60 to -70 dBm
                else if (connectionInfo.getRssi() > -70)
                {
                    value = " Fair ";
                }
                // Weak < -70 dBm
                else if (connectionInfo.getRssi() < -70)
                {
                    value = " Weak ";
                }
                else
                {
                    value = "";
                }

                txtvSignal.setText( connectionInfo.getSSID() + ": " + connectionInfo.getRssi() + " " + value + "\n" );

            }
        }
    }

}



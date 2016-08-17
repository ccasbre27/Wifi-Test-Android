package com.example.itadmin.wifi_test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    WifiManager mWifiManager;

    ImageView imgvSkypeLogo;
    TextView txtvDiagnosticMessage;
    TextView txtvSignal;
    ImageView imgvWifiSignal;
    TextView txtvWifiSignal;
    ImageView imgvInternetSignal;
    TextView txtvInternetSignal;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // referencia los componentes
        imgvSkypeLogo = (ImageView) findViewById(R.id.imgvSkype);
        txtvDiagnosticMessage = (TextView) findViewById(R.id.txtvDiagnosticMessage);
        txtvSignal = (TextView) findViewById(R.id.txtvSignal);
        imgvWifiSignal = (ImageView) findViewById(R.id.imgvWifi);
        txtvWifiSignal = (TextView) findViewById(R.id.txtvWifiStatus);
        imgvInternetSignal = (ImageView) findViewById(R.id.imgvInternet);
        txtvInternetSignal = (TextView) findViewById(R.id.txtvInternetStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // click listener de la imagen
        imgvSkypeLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetActivity();


                // delay
                new CountDownTimer(3000, 1000) {
                    public void onFinish() {
                        getWifiStatus();
                        getInternetStatus();

                        txtvDiagnosticMessage.setText(R.string.diagnostic_message);
                        progressBar.setVisibility(View.GONE);
                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


            }
        });


        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void getWifiStatus()
    {

        // se verifica si el wifi está habilitado
        if (!mWifiManager.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            mWifiManager.startScan();

            if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
            {

                WifiInfo connectionInfo = mWifiManager.getConnectionInfo();

                String value = "";

                // Excellent >-50 dBm
                if (connectionInfo.getRssi() > - 50)
                {
                    value = " Excellent ";

                    imgvWifiSignal.setImageResource(R.drawable.good_wifi);
                    imgvSkypeLogo.setImageResource(R.drawable.good_skype);
                    txtvWifiSignal.setText(R.string.ok);

                }
                // Good -50 to -60 dBm
                else if (connectionInfo.getRssi() > -60)
                {
                    value = " Good ";
                    imgvWifiSignal.setImageResource(R.drawable.good_wifi);
                    imgvSkypeLogo.setImageResource(R.drawable.good_skype);
                    txtvWifiSignal.setText(R.string.ok);

                }
                // Fair -60 to -70 dBm
                else if (connectionInfo.getRssi() > -70)
                {
                    value = " Fair ";
                    imgvWifiSignal.setImageResource(R.drawable.low_wifi);
                    imgvSkypeLogo.setImageResource(R.drawable.low_skype);
                    txtvWifiSignal.setText(R.string.low);

                }
                // Weak < -70 dBm
                else if (connectionInfo.getRssi() < -70)
                {
                    value = " Weak ";
                    imgvWifiSignal.setImageResource(R.drawable.weak_wifi);
                    imgvSkypeLogo.setImageResource(R.drawable.weak_skype);
                    txtvWifiSignal.setText(R.string.weak);

                }
                else
                {
                    value = "";
                }

                txtvSignal.setText( connectionInfo.getSSID() + ": " + connectionInfo.getRssi() + " " + value + "\n" );
            }



        }
    }

    private void getInternetStatus()
    {
        if (isNetworkAvailable())
        {
            imgvInternetSignal.setImageResource(R.drawable.good_internet);
            txtvInternetSignal.setText(R.string.ok);
        }
        else
        {
            imgvInternetSignal.setImageResource(R.drawable.weak_internet);
            txtvInternetSignal.setText(R.string.weak);
        }
    }


    private void resetActivity()
    {
        imgvSkypeLogo.setImageResource(R.drawable.default_skype);

        imgvWifiSignal.setImageResource(R.drawable.default_wifi);
        txtvWifiSignal.setText(R.string.unknown);
        imgvInternetSignal.setImageResource(R.drawable.default_internet);
        txtvInternetSignal.setText(R.string.unknown);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        txtvDiagnosticMessage.setText(R.string.diagnosting_message);
    }


}



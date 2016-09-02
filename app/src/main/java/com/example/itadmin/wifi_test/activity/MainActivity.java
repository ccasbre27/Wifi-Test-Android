package com.example.itadmin.wifi_test.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.itadmin.wifi_test.R;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
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
    TextView txtvInstructionMessage;
    TextView txtvHelpLink;
    DeviceBandwidthSampler mDeviceBandwidthSampler;

    final String mURL = "http://lyncdiscover.hpe.com/dialin";

    int mTries = 0;

    ConnectionQuality CONNECTION_QUALITY = ConnectionQuality.UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // referencia los componentes
        imgvSkypeLogo = (ImageView) findViewById(R.id.imgvSkype);
        txtvDiagnosticMessage = (TextView) findViewById(R.id.txtvDiagnosticMessage);
        txtvSignal = (TextView) findViewById(R.id.txtvSignal);
        imgvWifiSignal = (ImageView) findViewById(R.id.imgvWifi);
        txtvWifiSignal = (TextView) findViewById(R.id.txtvWifiStatus);
        imgvInternetSignal = (ImageView) findViewById(R.id.imgvInternet);
        txtvInternetSignal = (TextView) findViewById(R.id.txtvInternetStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtvInstructionMessage = (TextView) findViewById(R.id.txtvInstructionMessage);
        txtvHelpLink = (TextView) findViewById(R.id.txtvHelpLink);

        txtvHelpLink.setText(Html.fromHtml("Didn't work? <font color=#36B2E4>Try another options</font>"));

        txtvDiagnosticMessage.setText(Html.fromHtml("Tap to<br><big><b>Start</b></big><br>Diagnostic"));

        // click listener de la imagen
        imgvSkypeLogo.setOnClickListener(this);
        txtvHelpLink.setOnClickListener(this);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.txtvHelpLink:

                Intent intent = new Intent(MainActivity.this,HelpActivity.class);

                switch (CONNECTION_QUALITY)
                {
                    case UNKNOWN:
                        intent.putExtra("quality","UNKNOWN");
                        break;

                    case POOR:
                        intent.putExtra("quality","POOR");
                        break;

                    case MODERATE:
                        intent.putExtra("quality","MODERATE");
                        break;

                }
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

                break;

            case R.id.imgvSkype:

                resetActivity();

                new CheckInternetAsyncTask().execute();

                break;
        }
    }

    // verifica si hay conexión a internet
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void checkConnections(final ConnectionQuality internetQuality)
    {
        // delay
        new CountDownTimer(3000, 1000) {
            public void onFinish() {

                // se obtiene la información de la conexión a internet y wifi
                ConnectionQuality wifiQuality = getWifiStatus();
                setInternetQuality(internetQuality);
                //wifiStatus = CONNECTION_STATUS.LOW;


                // se verifica si el valor de wifi es menor, si es así entonces se pasa dicho enum para que establezca
                // la imagen de skype con el valor que le corresponde.
                // en otro escenario internet status es menor por lo que de igual manera se envía
                // finalmente queda que wifiStatus e internetStatus sean iguales de manera que es irrelevante el valor que se envíe
                if (wifiQuality.ordinal() < internetQuality.ordinal())
                {
                    updateCloudLogo(wifiQuality);
                }
                else
                {
                    updateCloudLogo(internetQuality);
                }

                // se reestablece el mensaje inicial
                txtvDiagnosticMessage.setText(R.string.diagnostic_message);
                progressBar.setVisibility(View.GONE);


            }

            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();
    }

    // obtiene el estado del wifi y devuelve un enum con dicho valor
    private ConnectionQuality getWifiStatus()
    {

        ConnectionQuality connectionQuality = ConnectionQuality.UNKNOWN;

        // se verifica si el wifi está habilitado
        if (!mWifiManager.isWifiEnabled())
        {
            connectionQuality = ConnectionQuality.POOR;
            txtvWifiSignal.setText(R.string.disconnected);

            imgvWifiSignal.setImageResource(R.drawable.wifi_default);
        }
        else
        {
            // se inicia el scan
            mWifiManager.startScan();

            // se obtiene la red a la que estamos conectados
            WifiInfo connectionInfo = mWifiManager.getConnectionInfo();

            String value = "";

            // Excellent >-50 dBm
            if (connectionInfo.getRssi() > - 50)
            {
                value = "Excellent";
                connectionQuality = ConnectionQuality.EXCELLENT;

                imgvWifiSignal.setImageResource(R.drawable.wifi_good);
                txtvWifiSignal.setText(R.string.excellent);
                txtvWifiSignal.setTextColor(getResources().getColor(R.color.connection_good, null));

            }
            // Good -50 to -60 dBm
            else if (connectionInfo.getRssi() > - 60)
            {
                value = "Good";
                connectionQuality = ConnectionQuality.GOOD;

                imgvWifiSignal.setImageResource(R.drawable.wifi_good);
                txtvWifiSignal.setText(R.string.good);
                txtvWifiSignal.setTextColor(getResources().getColor(R.color.connection_good, null));

            }
            // LOW -60 to -70 dBm
            else if (connectionInfo.getRssi() > -70)
            {
                value = "Moderate ";
                connectionQuality = ConnectionQuality.MODERATE;
                imgvWifiSignal.setImageResource(R.drawable.wifi_moderate);
                txtvWifiSignal.setText(R.string.moderate);
                txtvWifiSignal.setTextColor(getResources().getColor(R.color.connection_low, null));

            }
            // Weak < -70 dBm
            else if (connectionInfo.getRssi() < -70)
            {
                value = "Poor";
                connectionQuality = ConnectionQuality.POOR;
                imgvWifiSignal.setImageResource(R.drawable.wifi_poor);
                txtvWifiSignal.setText(R.string.poor);
                txtvWifiSignal.setTextColor(getResources().getColor(R.color.connection_weak, null));

            }

            txtvSignal.setText( connectionInfo.getSSID() + ": " + connectionInfo.getRssi() + " " + value + "\n" );

        }

        return connectionQuality;

    }

    // obtiene el estado del internet y devuelve un enum con dicho valor
    private void setInternetQuality(ConnectionQuality internetQuality)
    {
        switch (internetQuality)
        {
            case EXCELLENT:
                imgvInternetSignal.setImageResource(R.drawable.internet_good);
                txtvInternetSignal.setText(R.string.excellent);
                txtvInternetSignal.setTextColor(Color.parseColor(Integer.toHexString(getResources().getColor(R.color.connection_good, null) & 0x00ffffff)));
                break;

            case GOOD:
                imgvInternetSignal.setImageResource(R.drawable.internet_good);
                txtvInternetSignal.setText(R.string.good);
                txtvInternetSignal.setTextColor(getResources().getColor(R.color.connection_good, null));
                break;

            case MODERATE:
                imgvInternetSignal.setImageResource(R.drawable.internet_moderate);
                txtvInternetSignal.setText(R.string.moderate);
                txtvInternetSignal.setTextColor(getResources().getColor(R.color.connection_low, null));
                break;

            case POOR:
                imgvInternetSignal.setImageResource(R.drawable.internet_poor);
                txtvInternetSignal.setText(R.string.poor);
                txtvInternetSignal.setTextColor(getResources().getColor(R.color.connection_weak, null));
                break;

            case UNKNOWN:
                imgvInternetSignal.setImageResource(R.drawable.internet_default);
                txtvInternetSignal.setText(R.string.unknown);
                txtvInternetSignal.setTextColor(getResources().getColor(R.color.gray, null));
                break;


        }

        // check if the internet is disabled
        if(!isNetworkAvailable())
        {
            imgvInternetSignal.setImageResource(R.drawable.internet_default);
            txtvInternetSignal.setText(R.string.disconnected);
            txtvInternetSignal.setTextColor(getResources().getColor(R.color.connection_weak, null));

        }
    }

    private void updateCloudLogo(ConnectionQuality status)
    {
        int resourceId = R.drawable.cloud_default;

        // según el valor que tenga se actualiza la imagen de skype
        switch (status)
        {
            case UNKNOWN:
                resourceId = R.drawable.cloud_default;
                txtvInstructionMessage.setText(R.string.unknown_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case POOR:
                resourceId = R.drawable.cloud_poor;
                txtvInstructionMessage.setText(R.string.weak_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

                CONNECTION_QUALITY = ConnectionQuality.POOR;
                break;

            case MODERATE:
                resourceId = R.drawable.cloud_moderate;
                txtvInstructionMessage.setText(R.string.low_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

                CONNECTION_QUALITY = ConnectionQuality.MODERATE;
                break;

            case GOOD:
            case EXCELLENT:
                resourceId = R.drawable.cloud_good;
                txtvInstructionMessage.setText(R.string.good_connection_instruction);

                break;
        }

        Glide.with(this)
                .load(resourceId)
                .into(imgvSkypeLogo);
    }


    private void resetActivity()
    {
        Glide.with(this)
                .load(R.drawable.cloud_default)
                .into(imgvSkypeLogo);

        imgvWifiSignal.setImageResource(R.drawable.wifi_default);
        txtvWifiSignal.setText(R.string.unknown);
        imgvInternetSignal.setImageResource(R.drawable.internet_default);
        txtvInternetSignal.setText(R.string.unknown);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        txtvDiagnosticMessage.setText(R.string.diagnosting_message);

        txtvSignal.setText("");

        txtvInstructionMessage.setText(R.string.loading_instruction);

        txtvHelpLink.setVisibility(View.INVISIBLE);
    }

    // descargar json
    class CheckInternetAsyncTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            mDeviceBandwidthSampler.startSampling();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // Open a stream to download the image from our URL.
                URLConnection connection = new URL(mURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                InputStream input = connection.getInputStream();
                try {
                    byte[] buffer = new byte[1024];

                    // -1 indica que no hay bytes para leer
                    while (input.read(buffer) != -1) {
                    }

                } finally {
                    input.close();
                }

            } catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDeviceBandwidthSampler.stopSampling();

            ConnectionQuality connectionQuality = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();

            // Retry for up to 5 times until we find a ConnectionClass.
            if (connectionQuality == ConnectionQuality.UNKNOWN && mTries < 5) {
                mTries++;
                new CheckInternetAsyncTask().execute();
            }
            else
            {

                checkConnections(connectionQuality);
            }

        }
    }

}



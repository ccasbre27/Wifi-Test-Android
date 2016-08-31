package com.example.itadmin.wifi_test.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
                    updateSkypeLogo(wifiQuality);
                }
                else
                {
                    updateSkypeLogo(internetQuality);
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

            imgvWifiSignal.setImageResource(R.drawable.weak_wifi);
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

                imgvWifiSignal.setImageResource(R.drawable.good_wifi);
                txtvWifiSignal.setText(R.string.excellent);

            }
            // Good -50 to -60 dBm
            else if (connectionInfo.getRssi() > - 60)
            {
                value = "Good";
                connectionQuality = ConnectionQuality.GOOD;

                imgvWifiSignal.setImageResource(R.drawable.good_wifi);
                txtvWifiSignal.setText(R.string.good);

            }
            // LOW -60 to -70 dBm
            else if (connectionInfo.getRssi() > -70)
            {
                value = "Moderate ";
                connectionQuality = ConnectionQuality.MODERATE;
                imgvWifiSignal.setImageResource(R.drawable.low_wifi);
                txtvWifiSignal.setText(R.string.moderate);

            }
            // Weak < -70 dBm
            else if (connectionInfo.getRssi() < -70)
            {
                value = "Poor";
                connectionQuality = ConnectionQuality.POOR;
                imgvWifiSignal.setImageResource(R.drawable.weak_wifi);
                txtvWifiSignal.setText(R.string.poor);

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
                imgvInternetSignal.setImageResource(R.drawable.good_internet);
                txtvInternetSignal.setText(R.string.excellent);
                break;

            case GOOD:
                imgvInternetSignal.setImageResource(R.drawable.good_internet);
                txtvInternetSignal.setText(R.string.good);
                break;

            case MODERATE:
                imgvInternetSignal.setImageResource(R.drawable.low_internet);
                txtvInternetSignal.setText(R.string.moderate);
                break;

            case POOR:
                imgvInternetSignal.setImageResource(R.drawable.weak_internet);
                txtvInternetSignal.setText(R.string.poor);
                break;

            case UNKNOWN:
                imgvInternetSignal.setImageResource(R.drawable.default_internet);
                txtvInternetSignal.setText(R.string.unknown);
                break;


        }

        // check if the internet is disabled
        if(!isNetworkAvailable())
        {
            imgvInternetSignal.setImageResource(R.drawable.weak_internet);
            txtvInternetSignal.setText(R.string.disconnected);
        }
    }

    private void updateSkypeLogo(ConnectionQuality status)
    {
        // según el valor que tenga se actualiza la imagen de skype
        switch (status)
        {
            case UNKNOWN:
                imgvSkypeLogo.setImageResource(R.drawable.default_skype);
                txtvInstructionMessage.setText(R.string.unknown_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case POOR:
                imgvSkypeLogo.setImageResource(R.drawable.weak_skype);
                txtvInstructionMessage.setText(R.string.weak_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

                CONNECTION_QUALITY = ConnectionQuality.POOR;
                break;

            case MODERATE:
                imgvSkypeLogo.setImageResource(R.drawable.low_skype);
                txtvInstructionMessage.setText(R.string.low_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

                CONNECTION_QUALITY = ConnectionQuality.MODERATE;
                break;

            case GOOD:
            case EXCELLENT:
                imgvSkypeLogo.setImageResource(R.drawable.good_skype);
                txtvInstructionMessage.setText(R.string.good_connection_instruction);

                break;
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



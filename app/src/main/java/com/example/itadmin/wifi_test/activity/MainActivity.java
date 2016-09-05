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
import android.widget.RelativeLayout;
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

    ImageView imgvMainLogo;
    TextView txtvDiagnosticMessage;
    ImageView imgvWifiSignal;
    TextView txtvWifiSignal;
    ImageView imgvInternetSignal;
    TextView txtvInternetSignal;
    ProgressBar progressBar;
    TextView txtvRecommendationMessage;
    TextView txtvHelpLink;
    View vTopLine;
    ImageView imgvAlert;
    TextView txtvAlertMessage;
    View vBottomLine;
    DeviceBandwidthSampler mDeviceBandwidthSampler;
    RelativeLayout rlAlert;

    final String mURL = "http://lyncdiscover.hpe.com/dialin";

    int mTries = 0;

    ConnectionQuality CONNECTION_QUALITY = ConnectionQuality.UNKNOWN;

    CustomConnectionQuality customConnectionQuality;

    final String colorConnectionGood = "#6BA92B";
    final String colorConnectionModerate = "#FAB040";
    final String colorConnectionPoor = "#ED1B22";
    final String colorGray = "#808080";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // referencia los componentes
        imgvMainLogo = (ImageView) findViewById(R.id.imgvSkype);
        txtvDiagnosticMessage = (TextView) findViewById(R.id.txtvDiagnosticMessage);
        imgvWifiSignal = (ImageView) findViewById(R.id.imgvWifi);
        txtvWifiSignal = (TextView) findViewById(R.id.txtvWifiStatus);
        imgvInternetSignal = (ImageView) findViewById(R.id.imgvInternet);
        txtvInternetSignal = (TextView) findViewById(R.id.txtvInternetStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtvRecommendationMessage = (TextView) findViewById(R.id.txtvRecommendationMessage);
        txtvHelpLink = (TextView) findViewById(R.id.txtvHelpLink);

        vTopLine = findViewById(R.id.vTopLine);
        imgvAlert = (ImageView) findViewById(R.id.imgvAlert);
        txtvAlertMessage = (TextView) findViewById(R.id.txtvAlertMessage);
        vBottomLine = findViewById(R.id.vBottomLine);

        rlAlert = (RelativeLayout) findViewById(R.id.rlAlert);

        txtvHelpLink.setText(Html.fromHtml("Didn't work? <font color=#36B2E4>Try another options</font>"));

        // click listener de la imagen
        imgvMainLogo.setOnClickListener(this);
        txtvHelpLink.setOnClickListener(this);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();

        customConnectionQuality = new CustomConnectionQuality();

        Glide.with(this).load(R.drawable.cloud_default).into(imgvMainLogo);
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.txtvHelpLink:

                Intent intent = new Intent(MainActivity.this,HelpActivity.class);

                intent.putExtra("quality",customConnectionQuality);
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
        new CountDownTimer(2000, 1000) {
            public void onFinish() {

                // se obtiene la información de la conexión a internet y wifi
                ConnectionQuality wifiQuality = getWifiStatus();
                setInternetQuality(internetQuality);
                //wifiStatus = CONNECTION_STATUS.LOW;

                customConnectionQuality.wifiQuality = wifiQuality;
                customConnectionQuality.internetQuality = internetQuality;

                updateUI(wifiQuality,internetQuality);

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
                txtvWifiSignal.setTextColor(Color.parseColor(colorConnectionGood));

            }
            // Good -50 to -60 dBm
            else if (connectionInfo.getRssi() > - 60)
            {
                value = "Good";
                connectionQuality = ConnectionQuality.GOOD;

                imgvWifiSignal.setImageResource(R.drawable.wifi_good);
                txtvWifiSignal.setText(R.string.good);
                txtvWifiSignal.setTextColor(Color.parseColor(colorConnectionGood));

            }
            // LOW -60 to -70 dBm
            else if (connectionInfo.getRssi() > -70)
            {
                value = "Moderate ";
                connectionQuality = ConnectionQuality.MODERATE;
                imgvWifiSignal.setImageResource(R.drawable.wifi_moderate);
                txtvWifiSignal.setText(R.string.moderate);
                txtvWifiSignal.setTextColor(Color.parseColor(colorConnectionModerate));

            }
            // Weak < -70 dBm
            else if (connectionInfo.getRssi() < -70)
            {
                value = "Poor";
                connectionQuality = ConnectionQuality.POOR;
                imgvWifiSignal.setImageResource(R.drawable.wifi_poor);
                txtvWifiSignal.setText(R.string.poor);
                txtvWifiSignal.setTextColor(Color.parseColor(colorConnectionPoor));

            }

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
                txtvInternetSignal.setTextColor(Color.parseColor(colorConnectionGood));
                break;

            case GOOD:
                imgvInternetSignal.setImageResource(R.drawable.internet_good);
                txtvInternetSignal.setText(R.string.good);
                txtvInternetSignal.setTextColor(Color.parseColor(colorConnectionGood));
                break;

            case MODERATE:
                imgvInternetSignal.setImageResource(R.drawable.internet_moderate);
                txtvInternetSignal.setText(R.string.moderate);
                txtvInternetSignal.setTextColor(Color.parseColor(colorConnectionModerate));
                break;

            case POOR:
                imgvInternetSignal.setImageResource(R.drawable.internet_poor);
                txtvInternetSignal.setText(R.string.poor);
                txtvInternetSignal.setTextColor(Color.parseColor(colorConnectionPoor));
                break;

            case UNKNOWN:
                imgvInternetSignal.setImageResource(R.drawable.internet_default);
                txtvInternetSignal.setText(R.string.unknown);
                txtvInternetSignal.setTextColor(Color.parseColor(colorGray));
                break;


        }

        // check if the internet is disabled
        if(!isNetworkAvailable())
        {
            imgvInternetSignal.setImageResource(R.drawable.internet_default);
            txtvInternetSignal.setText(R.string.disconnected);
            txtvInternetSignal.setTextColor(Color.parseColor(colorGray));

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
                txtvRecommendationMessage.setText(R.string.unknown_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case POOR:
                resourceId = R.drawable.cloud_poor;
                txtvRecommendationMessage.setText(R.string.weak_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

                CONNECTION_QUALITY = ConnectionQuality.POOR;
                break;

            case MODERATE:
                resourceId = R.drawable.cloud_moderate;
                txtvRecommendationMessage.setText(R.string.low_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

                CONNECTION_QUALITY = ConnectionQuality.MODERATE;
                break;

            case GOOD:
            case EXCELLENT:
                resourceId = R.drawable.cloud_good;
                txtvRecommendationMessage.setText(R.string.good_connection_instruction);

                break;
        }

        Glide.with(this)
                .load(resourceId)
                .into(imgvMainLogo);
    }

    private void updateUI(ConnectionQuality wifiQuality, ConnectionQuality internetQuality)
    {
        rlAlert.setVisibility(View.VISIBLE);
        int resourceMainLogoId = R.drawable.cloud_default;

        switch (wifiQuality)
        {
            case EXCELLENT:
            case GOOD:

                switch (internetQuality)
                {
                    case EXCELLENT:
                    case GOOD:
                        // SfB Experience indicator: Good
                        resourceMainLogoId = R.drawable.cloud_good;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionGood));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionGood));
                        imgvAlert.setImageResource(R.drawable.alert_good);
                        txtvAlertMessage.setText(R.string.sm_gw_gi);
                        txtvRecommendationMessage.setText(R.string.rm_gw_gi);
                        break;

                    case MODERATE:
                        // SfB Experience indicator: Warning !
                        resourceMainLogoId = R.drawable.cloud_moderate;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        imgvAlert.setImageResource(R.drawable.alert_moderate);
                        txtvAlertMessage.setText(R.string.sm_gw_mi);
                        txtvRecommendationMessage.setText(R.string.rm_gw_mi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        resourceMainLogoId = R.drawable.cloud_poor;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        imgvAlert.setImageResource(R.drawable.alert_poor);
                        txtvAlertMessage.setText(R.string.sm_gw_pi);
                        txtvRecommendationMessage.setText(R.string.rm_gw_pi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case UNKNOWN:
                        break;
                }

                break;

            case MODERATE:

                switch (internetQuality)
                {
                    case EXCELLENT:
                    case GOOD:
                        // SfB Experience indicator: Warning !
                        resourceMainLogoId = R.drawable.cloud_moderate;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        imgvAlert.setImageResource(R.drawable.alert_moderate);
                        txtvAlertMessage.setText(R.string.sm_mw_gi);
                        txtvRecommendationMessage.setText(R.string.rm_mw_gi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case MODERATE:
                        // SfB Experience indicator: Warning !
                        resourceMainLogoId = R.drawable.cloud_moderate;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        imgvAlert.setImageResource(R.drawable.alert_moderate);
                        txtvAlertMessage.setText(R.string.sm_mw_mi);
                        txtvRecommendationMessage.setText(R.string.rm_mw_mi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        resourceMainLogoId = R.drawable.cloud_poor;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        imgvAlert.setImageResource(R.drawable.alert_poor);
                        txtvAlertMessage.setText(R.string.sm_mw_pi);
                        txtvRecommendationMessage.setText(R.string.rm_mw_pi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case UNKNOWN:
                        break;
                }

                break;

            case POOR:

                switch (internetQuality)
                {
                    case EXCELLENT:
                    case GOOD:
                        break;

                    case MODERATE:
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        resourceMainLogoId = R.drawable.cloud_poor;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        imgvAlert.setImageResource(R.drawable.alert_poor);
                        txtvAlertMessage.setText(R.string.sm_pw_pi);
                        txtvRecommendationMessage.setText(R.string.rm_pw_pi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case UNKNOWN:
                        break;
                }

                break;

            case UNKNOWN:

                switch (internetQuality)
                {
                    case EXCELLENT:
                    case GOOD:
                        // SfB Experience indicator: Good
                        resourceMainLogoId = R.drawable.cloud_good;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionGood));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionGood));
                        imgvAlert.setImageResource(R.drawable.alert_good);
                        txtvAlertMessage.setText(R.string.sm_dw_gi);
                        txtvRecommendationMessage.setText(R.string.rm_dw_gi);
                        break;

                    case MODERATE:
                        // SfB Experience indicator: Warning !
                        resourceMainLogoId = R.drawable.cloud_moderate;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionModerate));
                        imgvAlert.setImageResource(R.drawable.alert_moderate);
                        txtvAlertMessage.setText(R.string.sm_dw_mi);
                        txtvRecommendationMessage.setText(R.string.rm_dw_mi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        resourceMainLogoId = R.drawable.cloud_poor;
                        vTopLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorConnectionPoor));
                        imgvAlert.setImageResource(R.drawable.alert_poor);
                        txtvAlertMessage.setText(R.string.sm_dw_pi);
                        txtvRecommendationMessage.setText(R.string.rm_dw_pi);
                        txtvHelpLink.setVisibility(View.VISIBLE);
                        txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                        break;

                    case UNKNOWN:
                        resourceMainLogoId = R.drawable.cloud_default;
                        vTopLine.setBackgroundColor(Color.parseColor(colorGray));
                        vBottomLine.setBackgroundColor(Color.parseColor(colorGray));
                        imgvAlert.setImageResource(R.drawable.alert_default);
                        txtvAlertMessage.setText(R.string.sm_dw_di);
                        txtvRecommendationMessage.setText(R.string.rm_dw_di);
                        break;
                }

                break;
        }


        // se establece la imagen del logo principal
        Glide.with(this)
                .load(resourceMainLogoId)
                .into(imgvMainLogo);
    }

    private void resetActivity()
    {
        Glide.with(this)
                .load(R.drawable.cloud_default)
                .into(imgvMainLogo);

        imgvWifiSignal.setImageResource(R.drawable.wifi_default);
        txtvWifiSignal.setText(R.string.unknown);
        txtvWifiSignal.setTextColor(Color.parseColor(colorGray));
        imgvInternetSignal.setImageResource(R.drawable.internet_default);
        txtvInternetSignal.setText(R.string.unknown);
        txtvInternetSignal.setTextColor(Color.parseColor(colorGray));

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        txtvDiagnosticMessage.setText(R.string.diagnosting_message);

        txtvRecommendationMessage.setText(R.string.loading_instruction);

        rlAlert.setVisibility(View.GONE);


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



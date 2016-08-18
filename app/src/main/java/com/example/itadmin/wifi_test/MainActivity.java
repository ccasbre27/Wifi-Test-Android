package com.example.itadmin.wifi_test;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    private enum CONNECTION_STATUS
    {
        DEFAULT,
        DISCONNECTED,
        WEAK,
        LOW,
        GOOD
    }

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

    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.txtvHelpLink:

                Intent intent = new Intent(MainActivity.this,HelpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

                break;

            case R.id.imgvSkype:

                resetActivity();

                // delay
                new CountDownTimer(3000, 1000) {
                    public void onFinish() {

                        // se obtiene la información de la conexión a internet y wifi
                        CONNECTION_STATUS wifiStatus = getWifiStatus();
                        CONNECTION_STATUS internetStatus = getInternetStatus();

                        //wifiStatus = CONNECTION_STATUS.LOW;

                        // se verifica si el valor de wifi es menor, si es así entonces se pasa dicho enum para que establezca
                        // la imagen de skype con el valor que le corresponde.
                        // en otro escenario internet status es menor por lo que de igual manera se envía
                        // finalmente queda que wifiStatus e internetStatus sean iguales de manera que es irrelevante el valor que se envíe
                        if (wifiStatus.ordinal() < internetStatus.ordinal())
                        {
                            updateSkypeLogo(wifiStatus);
                        }
                        else
                        {
                            updateSkypeLogo(internetStatus);
                        }

                        // se reestablece el mensaje inicial
                        txtvDiagnosticMessage.setText(R.string.diagnostic_message);
                        progressBar.setVisibility(View.GONE);


                    }

                    public void onTick(long millisUntilFinished) {
                        // millisUntilFinished    The amount of time until finished.
                    }
                }.start();


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

    // obtiene el estado del wifi y devuelve un enum con dicho valor
    private CONNECTION_STATUS getWifiStatus()
    {

        CONNECTION_STATUS connectionStatus = CONNECTION_STATUS.DEFAULT;

        // se verifica si el wifi está habilitado
        if (!mWifiManager.isWifiEnabled())
        {
            connectionStatus = CONNECTION_STATUS.DISCONNECTED;
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
            // Good -50 to -60 dBm
            if (connectionInfo.getRssi() > - 60)
            {
                value = "GOOD";
                connectionStatus = CONNECTION_STATUS.GOOD;

                imgvWifiSignal.setImageResource(R.drawable.good_wifi);
                txtvWifiSignal.setText(R.string.ok);

            }
            // LOW -60 to -70 dBm
            else if (connectionInfo.getRssi() > -70)
            {
                value = "LOW";
                connectionStatus = CONNECTION_STATUS.LOW;
                imgvWifiSignal.setImageResource(R.drawable.low_wifi);
                txtvWifiSignal.setText(R.string.low);

            }
            // Weak < -70 dBm
            else if (connectionInfo.getRssi() < -70)
            {
                value = "Weak";
                connectionStatus = CONNECTION_STATUS.WEAK;
                imgvWifiSignal.setImageResource(R.drawable.weak_wifi);
                txtvWifiSignal.setText(R.string.weak);

            }

            txtvSignal.setText( connectionInfo.getSSID() + ": " + connectionInfo.getRssi() + " " + value + "\n" );

        }

        return connectionStatus;

    }

    // obtiene el estado del internet y devuelve un enum con dicho valor
    private CONNECTION_STATUS getInternetStatus()
    {
        CONNECTION_STATUS connectionStatus = CONNECTION_STATUS.DEFAULT;

        // se verifica si hay conexión a internet
        if (isNetworkAvailable())
        {
            // se establecen los valores de conexión a internet activa
            connectionStatus = CONNECTION_STATUS.GOOD;
            imgvInternetSignal.setImageResource(R.drawable.good_internet);
            txtvInternetSignal.setText(R.string.ok);
        }
        else
        {
            // se establecen los valores de conexión a internet desactivada
            connectionStatus = CONNECTION_STATUS.DISCONNECTED;
            imgvInternetSignal.setImageResource(R.drawable.weak_internet);
            txtvInternetSignal.setText(R.string.disconnected);
        }

        return connectionStatus;
    }

    private void updateSkypeLogo(CONNECTION_STATUS status)
    {
        // según el valor que tenga se actualiza la imagen de skype
        switch (status)
        {
            case DISCONNECTED:
            case WEAK:
                imgvSkypeLogo.setImageResource(R.drawable.weak_skype);
                txtvInstructionMessage.setText(R.string.weak_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case LOW:
                imgvSkypeLogo.setImageResource(R.drawable.low_skype);
                txtvInstructionMessage.setText(R.string.low_connection_instruction);
                txtvHelpLink.setVisibility(View.VISIBLE);
                txtvHelpLink.setMovementMethod(LinkMovementMethod.getInstance());
                break;

            case GOOD:
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


}



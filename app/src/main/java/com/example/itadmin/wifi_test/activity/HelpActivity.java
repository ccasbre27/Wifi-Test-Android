package com.example.itadmin.wifi_test.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.itadmin.wifi_test.R;
import com.facebook.network.connectionclass.ConnectionQuality;

public class HelpActivity extends AppCompatActivity {

    TextView txtvHelpTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtvHelpTitle = (TextView) findViewById(R.id.txtvHelpTitle);


        CustomConnectionQuality connectionQuality  = (CustomConnectionQuality) getIntent().getParcelableExtra("quality");

        updateUI(connectionQuality.wifiQuality,connectionQuality.internetQuality);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        //noinspection SimplifiableIfStatement
        switch(item.getItemId())
        {
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI(ConnectionQuality wifiQuality, ConnectionQuality internetQuality)
    {
        switch (wifiQuality)
        {
            case EXCELLENT:
            case GOOD:

                switch (internetQuality)
                {
                    case EXCELLENT:
                    case GOOD:
                        // SfB Experience indicator: Good

                        break;

                    case MODERATE:
                        // SfB Experience indicator: Warning !
                        txtvHelpTitle.setText(R.string.rm2_gw_mi);
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        txtvHelpTitle.setText(R.string.rm2_gw_pi);
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
                        break;

                    case MODERATE:
                        // SfB Experience indicator: Warning !
                        txtvHelpTitle.setText(R.string.rm2_mw_mi);
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        txtvHelpTitle.setText(R.string.rm2_mw_pi);
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
                        txtvHelpTitle.setText(R.string.rm2_pw_mi);
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        txtvHelpTitle.setText(R.string.rm2_pw_pi);
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
                        break;

                    case MODERATE:
                        // SfB Experience indicator: Warning !
                        txtvHelpTitle.setText(R.string.rm2_dw_mi);
                        break;

                    case POOR:
                        // SfB Experience indicator:  Bad !
                        txtvHelpTitle.setText(R.string.rm2_dw_pi);
                        break;

                    case UNKNOWN:
                        break;
                }

                break;
        }


    }
}

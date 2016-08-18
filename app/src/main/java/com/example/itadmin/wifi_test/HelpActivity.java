package com.example.itadmin.wifi_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setHomeButtonEnabled(true);

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
}

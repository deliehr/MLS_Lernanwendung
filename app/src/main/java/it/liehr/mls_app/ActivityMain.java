package it.liehr.mls_app;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.*;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import Comprehensive.App;

/**
 * Activity class main. First activity in app, shown at startup.
 *
 * @author Dominik Liehr
 * @version 0.01
 */
public class ActivityMain extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // main config
        // set hashmap file path
        App.urlHashMapFilePath = getFilesDir() + "urls.hashmap";

        // app start switch
        SharedPreferences preferences = this.getSharedPreferences("appStartSwitch", Context.MODE_PRIVATE);
        String autoSwitch = preferences.getString("appStartSwitch", "false");

        if(autoSwitch.equals("true")) {
            ((Button) this.findViewById(R.id.btnLearn)).performClick();
        }

        // TODO: 29.04.2017 file structure cleaning
        // if package contains wrong files and directories... or wrong file/dir structure
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // region button onclick
    public void btnConfigOnClick(View view) {
        // start config activity
        Intent intent = new Intent(this, ActivityConfig.class);
        startActivity(intent);
    }

    public void btnSelectionOnClick(View view) {
        // start selection activity
        Intent intent = new Intent(this, ActivitySelection.class);
        startActivity(intent);
    }

    public void btnStatisticOnClick(View view) {
        // start statistic activity
        Intent intent = new Intent(this, ActivityStatistic.class);
        startActivity(intent);
    }
    // endregion

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_global, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemExitApplication:
                this.finishAffinity();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
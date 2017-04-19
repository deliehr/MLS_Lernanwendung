package it.liehr.mls_app;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import Comprehensive.Application;

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
        Application.urlHashMapFilePath = getFilesDir() + "urls.hashmap";


        // evaluation
        try {

            Map<String, String> packageUrlsHashMap = new HashMap<>();


            // create new entry in internal hash map
            packageUrlsHashMap.put("evaluation_local", "http://192.168.0.200/aufgaben_evaluation.zip");
            packageUrlsHashMap.put("evaluation_online", "http://idragon.de/evaluation/aufgaben_evaluation.zip");
            //packageUrlsHashMap.put("debug_local_1", "http://192.168.0.200/aufgaben_debug.zip");
            //packageUrlsHashMap.put("debug_local_2", "http://192.168.0.200/aufgaben_debug2.zip");

            FileOutputStream fileOutputStream = new FileOutputStream(Application.urlHashMapFilePath);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(packageUrlsHashMap);
            objectOutputStream.close();

        } catch (java.io.FileNotFoundException fnfe) {

        } catch (Exception e) {

        }
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
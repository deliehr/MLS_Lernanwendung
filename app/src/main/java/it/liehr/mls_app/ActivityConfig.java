package it.liehr.mls_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Comprehensive.Application;
import Comprehensive.DatabaseHelper;
import Comprehensive.DownloadZipPackage;

import Comprehensive.DatabaseHelper.TableNames;

public class ActivityConfig extends AppCompatActivity {
    private static String mSelectedUrlKey;
    private static Context mContext;
    private static SharedPreferences mPreferences;
    private Map<String, String> packageUrlsHashMap = new HashMap<>();
    private static View mPopupView;
    private static PopupWindow mPopupWindow;

    // region specific listener
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        private int valueToStore = 3;

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            // min value 1
            this.valueToStore = i;

            if(valueToStore < 1) {
                // i == 0
                valueToStore = 1;
                seekBar.setProgress(1);
            }

            // show selected value
            ((TextView) findViewById(R.id.tvRepeatingThresholdValue)).setText(String.valueOf(valueToStore));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // save to shared preferences
            try {
                SharedPreferences.Editor spe = ActivityConfig.mPreferences.edit();
                spe.putInt("repeatingThreshold", valueToStore);
                spe.commit();
            } catch (Exception e) {
                Toast.makeText(ActivityConfig.mContext, R.string.activity_config_error_save_shared_preferences + ". " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private ListView.OnItemClickListener listViewClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                // define popup window
                ActivityConfig.mPopupView = getLayoutInflater().inflate(R.layout.popup_package_source, null);
                ActivityConfig.mPopupWindow = new PopupWindow(ActivityConfig.mPopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ActivityConfig.mPopupWindow.setBackgroundDrawable(new ColorDrawable());
                ActivityConfig.mPopupWindow.setOutsideTouchable(true);
                //popupWindow.setFocusable(true);

                // selected item
                String selectedItem = adapterView.getAdapter().getItem(i).toString();
                ActivityConfig.mSelectedUrlKey = selectedItem.split(": ")[0];

                TextView tv = (TextView) ActivityConfig.mPopupView.findViewById(R.id.tvPopupPackage);
                tv.setText(tv.getText() + ": " + selectedItem);

                // show popup windows
                ActivityConfig.mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            } catch (Exception e) {
                Log.e("Error", "ActivityConfig listViewClickListener newOnItemClickListener onItemClick(): " + e.getMessage());
            }
        }
    };
    // endregion

    // region button on click listener
    public void btnAddPackageSource(View view) {
        String description = ((EditText) findViewById(R.id.etSourcename)).getText().toString();
        String packageUrl = ((EditText) findViewById(R.id.etSourceUrl)).getText().toString();

        if(!description.isEmpty() && !packageUrl.isEmpty()) {
            // check for blank space, replace with _
            if(description.contains(" ")) {
                description = description.replace(" ", "_");

                // info for user
                Toast.makeText(this, "Leerzeichen durch _ ersetzt", Toast.LENGTH_SHORT);
            }

            writeToHashmapFile(description, packageUrl);
        }
    }

    public void btnDeletePackageSource(View view) {
        // delete from hashmap
        this.deleteHashmapEntry();

        // close popup
        ActivityConfig.mPopupWindow.dismiss();
    }

    public void btnImportPackageSource(View view) {
        Button usedButton = (Button) view;
        usedButton.setEnabled(false);

        Button deleteButton = (Button) mPopupView.findViewById(R.id.btnDeletePackageSoure);
        deleteButton.setEnabled(false);

        TextView tv = (TextView) ActivityConfig.mPopupView.findViewById(R.id.tvImportStatus);
        String url = packageUrlsHashMap.get(mSelectedUrlKey);

        String targetZipFile = getFilesDir() + Application.relativeTmpDownloadFile;
        String targetDir = getFilesDir() + Application.relativeExtractDataDirectory;

        DownloadZipPackage download = new Comprehensive.DownloadZipPackage(url, targetZipFile, targetDir, this, tv);
        download.execute();
    }

    public void btnRemoveAllAssessments(View view) {
        // remove all assessments
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase database = helper.getReadableDatabase();

        try {
            String[] tables = new String[] {TableNames.RElATED, TableNames.SUPPORT, TableNames.OBJECT, TableNames.ASSESSMENT_ITEM};

            for(String table:tables) {
                database.delete(table, null, null);
            }

            Toast.makeText(this, R.string.activity_config_reset_message_remove_all_assessments, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Error", "Activity config: Removing all assessments: " + e.getMessage());
        }
    }

    public void btnResetStatistics(View view) {
        // remove all assessments
        DatabaseHelper helper = new DatabaseHelper(this);

        try {
            helper.getReadableDatabase().delete(DatabaseHelper.TableNames.STATISTIC, null, null);
            Toast.makeText(this, R.string.activity_config_reset_message_reset_statistic, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Error", "Activity config: Reset statistic: " + e.getMessage());
        }
    }

    public void btnAppReset(View view) {
// remove all assessments
        DatabaseHelper helper = new DatabaseHelper(this);

        try {
            //delete app files
            List<File> theBigBang = new ArrayList<File>();
            File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME);
            theBigBang.add(new File(db.getAbsolutePath()));
            theBigBang.add(new File(db.getAbsolutePath() + "-journal"));
            theBigBang.add(new File(this.getFilesDir() + "/zip/"));
            theBigBang.add(new File(this.getFilesDir() + "/tmp/"));
            theBigBang.add(new File(this.getFilesDir() + "/extract/"));
            theBigBang.add(new File(this.getFilesDir() + "/fileurls.hashmap"));
            for(File f:theBigBang) {
                Comprehensive.Application.deleteRecursive(new File(f.getAbsolutePath()));
            }

            Toast.makeText(this, R.string.activity_config_reset_message_reset_application, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Error", "Activity config: Removing all assessments: " + e.getMessage());
        }
    }
    // endregion

    // region hash map functionalities
    private void readUrlHashmapFile() {
        // iterate stored urls, hash map is serializable
        File urlHashMapFile = new File(Application.urlHashMapFilePath);

        // check if file exists
        try {
            if(!urlHashMapFile.exists()) {
                // if hash map file not exists, create
                urlHashMapFile.createNewFile();
            }
        } catch (Exception e) {
            (Toast.makeText(this, R.string.activity_config_error_cannot_create_url_hashmap_file + " (" + e.getMessage() + ")", Toast.LENGTH_SHORT)).show();
        }

        // read file
        try {
            FileInputStream fis = new FileInputStream(Application.urlHashMapFilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.packageUrlsHashMap = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (java.io.FileNotFoundException fnfe) {
            // file not found
        } catch (java.io.EOFException eofe) {
            // end of file exception
            Log.e("Error", "empty?");
        } catch (Exception e) {
            Log.e("Error", "Error while trying to read url hashmap file: " + e.getMessage());
        }

        // try to show all entries
        this.showHashmapEntries();
    }

    private void writeToHashmapFile(String key, String value) {
        // create new entry in internal hash map
        this.packageUrlsHashMap.put(key, value);

        // show entry in listview
        this.showHashmapEntries();

        // write changes to file
        writeHashmap();
    }

    private void writeHashmap() {
        // write changed to file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Application.urlHashMapFilePath);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(this.packageUrlsHashMap);
            objectOutputStream.close();
        } catch (java.io.FileNotFoundException fnfe) {

        } catch (Exception e) {

        }
    }

    private void showHashmapEntries() {
        // show all hash map entries
        List<String> list = new ArrayList<String>();

        Iterator iterator = this.packageUrlsHashMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            list.add(pair.getKey() + ": " + pair.getValue());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        ListView lv = (ListView) findViewById(R.id.listViewPackageUrls);
        lv.setAdapter(adapter);
    }

    private void deleteHashmapEntry() {
        try {
            // first remove
            this.packageUrlsHashMap.remove(ActivityConfig.mSelectedUrlKey);

            // write changes
            this.writeHashmap();

            // show
            this.showHashmapEntries();
        } catch (Exception e) {
            Log.e("Error", "Trying to delete hash map entry: " + e.getMessage());
            Toast.makeText(this, R.string.activity_config_error_delete_hashmap_entry, Toast.LENGTH_SHORT).show();
        }
    }
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get context
        ActivityConfig.mContext = this;

        // get shared preferences
        ActivityConfig.mPreferences = this.getSharedPreferences("repeatingThreshold", Context.MODE_PRIVATE);

        // set seekbar listener
        ((SeekBar) findViewById(R.id.sbRepeatingThreshold)).setOnSeekBarChangeListener(this.seekBarChangeListener);

        // set listview listener
        ((ListView) findViewById(R.id.listViewPackageUrls)).setOnItemClickListener(this.listViewClickListener);

        // set seekbar value from shared preferences
        try {
            int value = ActivityConfig.mPreferences.getInt("repeatingThreshold", 3);
            ((SeekBar) findViewById(R.id.sbRepeatingThreshold)).setProgress(value);
            ((TextView) findViewById(R.id.tvRepeatingThresholdValue)).setText(String.valueOf(value));
        } catch (Exception e) {
            Log.e("Error", "Setting seekbar value from shared preferences: " + e.getMessage());
        }

        // read hashmap file
        this.readUrlHashmapFile();
    }

    // region menu (dot points)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemExitApplication:
                this.finishAffinity();
                return true;

            case R.id.menuItemMainMenu:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // endregion
}
package it.liehr.mls_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.regex.Pattern;

import Comprehensive.App;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ActivityQrCodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        //setContentView(R.layout.activity_qr_code_scanner);
        setContentView(mScannerView);
    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        String readingResult = rawResult.getText();
        String barCodeMethod = rawResult.getBarcodeFormat().toString();

        if(barCodeMethod.equals("QR_CODE")) {
            // check whats incoming
            if(!readingResult.equals("")) {
                // regex match
                if(Pattern.matches(App.regularExpressionPackageTitleAndUrl, readingResult)) {
                    String[] parts = readingResult.split(":");

                    ActivityConfig activityConfig = ActivityConfig.activityConfigObject;
                    activityConfig.writeToHashmapFile(parts[0], parts[1] + ":" + parts[2]);

                    mScannerView.stopCamera();
                    this.finish();
                } else {
                    Toast.makeText(this, this.getString(R.string.activity_config_message_regex_mismatch), Toast.LENGTH_LONG).show();
                    Log.i("pattern", "NO match");
                }
            }
        }

        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }
}

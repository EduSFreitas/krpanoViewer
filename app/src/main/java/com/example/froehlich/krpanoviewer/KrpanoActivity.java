package com.example.froehlich.krpanoviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.hoan.dsensor_master.DProcessedSensor;
import com.hoan.dsensor_master.DSensor;
import com.hoan.dsensor_master.DSensorEvent;
import com.hoan.dsensor_master.DSensorManager;
import com.hoan.dsensor_master.interfaces.DProcessedEventListener;

import java.io.IOException;

public class KrpanoActivity extends AppCompatActivity  implements DProcessedEventListener {

    // INSTANCE OF ANDROID WEB SERVER
    private AndroidWebServer androidWebServer;

    // WebView
    private WebView mWebView;

    // Facing Direction
    private double mFacing = Double.NaN;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.krpano_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_align:
                Toast.makeText(this.getApplicationContext(), "Ausrichtung: " + mFacing, Toast.LENGTH_LONG).show();
                // Nach Norden ausrichten
                mWebView.evaluateJavascript("krpano.set('view.hlookat', "+ mFacing +");", null);
                mWebView.evaluateJavascript("krpano.call('plugin[skin_gyro].resetSensor("+ mFacing +");');", null);
                mWebView.evaluateJavascript("krpano.call('webvr.resetSensor("+ mFacing +");');", null);
                return true;
            case R.id.item2:
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krpano);
        mWebView = (WebView) findViewById(R.id.activity_webview);

        // initialize webserver
        initializeWebserver();



        //mWebView.loadUrl("http://localhost:8080/krpano/index.html"); // offline
        //mWebView.loadUrl("http://vr.wtr-architekten.de/krpanoViewer/"); // online
        mWebView.loadUrl("http://vr.wtr-architekten.de/karlsruhe/index.html"); // online



        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());

    }

    // FULLSCREEN
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        /*if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);} */
    }

    @Override
    protected void onResume(){
        int flag = DSensorManager.startDProcessedSensor(this, DProcessedSensor.TYPE_3D_COMPASS, this);
        if ((flag & DSensorManager.TYPE_MAGNETIC_FIELD_NOT_AVAILABLE) != 0) {
            // error_no_magnetic_field_sensor
        } else if ((flag & DSensorManager.TYPE_GRAVITY_NOT_AVAILABLE) != 0
                && (flag & DSensorManager.TYPE_ACCELEROMETER_NOT_AVAILABLE) != 0) {
            // error_no_accelerometer_sensor);
        }


        super.onResume();
    }

    @Override
    protected void onPause() {
        DSensorManager.stopDSensor();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if(null !=androidWebServer) androidWebServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initializeWebserver(){

        // Websettings
        WebSettings webSettings = mWebView.getSettings();

        // Javascript Enable
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Access Settings
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        AndroidWebServer androidWebServer = new AndroidWebServer();
        androidWebServer.start(this.getApplicationContext());
    }

    public void showOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.krpano_menu, popup.getMenu());
        popup.show();
    }


    @Override
    public void onProcessedValueChanged(DSensorEvent dSensorEvent) {
        if (Float.isNaN(dSensorEvent.values[0])) {
            // Device is not flat no compass value
        } else {
            int valueInDegree = (int) Math.round(Math.toDegrees(dSensorEvent.values[0]));
            if (valueInDegree < 0) {
                valueInDegree = (valueInDegree + 360) % 360;
            }
            mFacing = valueInDegree;
            //Log.d("Ausrichtung", "Ausrichtung: " +  valueInDegree);
        }
    }
}

package com.example.froehlich.krpanoviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.froehlich.krpanoviewer.bearing.BearingToNorthProvider;
import java.io.IOException;

public class KrpanoActivity extends AppCompatActivity  implements BearingToNorthProvider.ChangeEventListener{

    // INSTANCE OF ANDROID WEB SERVER
    private AndroidWebServer androidWebServer;

    // WebView
    private WebView mWebView;

    // Bearing
    private BearingToNorthProvider mBearingProvider;
    private double mBearing = Double.NaN;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.krpano_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_align:
                //mWebView.evaluateJavascript("autorotate()", null);
                //mWebView.reload();


                // Nach Norden ausrichten
                mWebView.evaluateJavascript("krpano.set('view.hlookat', "+ mBearing +");", null);
                mWebView.evaluateJavascript("krpano.call('plugin[skin_gyro].resetSensor("+ mBearing +");');", null);
                mWebView.evaluateJavascript("krpano.call('webvr.resetSensor("+ mBearing +");');", null);
                //mWebView.evaluateJavascript("alert('Bearing: " + mBearing + "')", null);
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

        mBearingProvider = new BearingToNorthProvider(this);
        mBearingProvider.setChangeEventListener(this);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        AndroidWebServer androidWebServer = new AndroidWebServer();

        androidWebServer.start(this.getApplicationContext());

        //mWebView.loadUrl("http://localhost:8080/krpano/index.html"); // offline
        //mWebView.loadUrl("http://vr.wtr-architekten.de/krpanoViewer/"); // online
        mWebView.loadUrl("http://vr.wtr-architekten.de/karlsruhe/index.html"); // online



        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());

    }

    // FULLSCREEN
    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }*/

    @Override
    protected void onResume(){
        super.onResume();
        mBearingProvider.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBearingProvider.stop();
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

    @Override
    public void onBearingChanged(double bearing) {
        mBearing = bearing;
    }

    public void showOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.krpano_menu, popup.getMenu());
        popup.show();
    }


}

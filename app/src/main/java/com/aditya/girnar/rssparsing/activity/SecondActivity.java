package com.aditya.girnar.rssparsing.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.aditya.girnar.rssparsing.R;

/**
 * Created by Aditya on 2/17/2016.
 */

public class SecondActivity extends Activity {

    private WebView webView;
    private ProgressBar progress;
    protected static final String TAG = SecondActivity.class.getCanonicalName();
    private Context mContext;
    public static final String URL_STRING = "URL_STRING";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        webView = (WebView) findViewById(R.id.player_detail_web_view);
        progress = (ProgressBar) findViewById(R.id.web_progress_bar);

        mContext = SecondActivity.this;
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setUserAgentString("(Android; Mobile) Chrome");
        webView.setWebViewClient(new MyWebViewClient());

        Intent getIntent = getIntent();
        String urlToLoad = getIntent.getStringExtra(SecondActivity.URL_STRING);

        webView.loadUrl(urlToLoad);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);
            Log.v(TAG, "onPageFinished");
            progress.setVisibility(View.GONE);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            Log.v(TAG, "onPageStarted");
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v(TAG, "shouldOverrideUrlLoading");
            return super.shouldOverrideUrlLoading(view, url);

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.v(TAG, "onReceivedError");
            if (mContext != null) {
                if (isNetworkAvailable(mContext)) {

                    webView.loadData(
                            getResources().getString(R.string.error_msg_html),
                            "text/html", "UTF-8");
                } else {
                    webView.loadData(
                            getResources()
                                    .getString(R.string.internet_msg_html),
                            "text/html", "UTF-8");
                }
            }

        }
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState() == NetworkInfo.State.CONNECTED
                    || connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState() == NetworkInfo.State.CONNECTING) {
                return true;
            } else if (connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .getState() == NetworkInfo.State.CONNECTED
                    || connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .getState() == NetworkInfo.State.CONNECTING) {

                return true;
            } else
                return false;
        } catch (Exception e) {
            return false;
        }

    }

    private WebChromeClient chromeClient = new WebChromeClient() {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            Log.v(TAG, "onShowCustomView");
            if (view instanceof FrameLayout) {
                FrameLayout frame = (FrameLayout) view;
                if (frame.getFocusedChild() instanceof VideoView) {
                    VideoView video = (VideoView) frame.getFocusedChild();
                    frame.removeView(video);
                    frame.addView(video);
                    video.start();
                }
            }
        }

    };

}

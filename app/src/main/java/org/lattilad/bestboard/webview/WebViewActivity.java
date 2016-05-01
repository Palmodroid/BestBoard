package org.lattilad.bestboard.webview;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Simple webview to show html text
 * https://developer.chrome.com/multidevice/webview/gettingstarted
 */
public class WebViewActivity extends Activity
    {
    private WebView webView;

    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        // This progress bar is deprecated!! APPCOMPAT should be used!!
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        webView = new WebView( this );

        setContentView(webView);

        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);




        webView.getSettings().setJavaScriptEnabled(true);
        // webView.loadUrl("http://www.google.com");

        final Activity activity = this;

        webView.setWebChromeClient(new WebChromeClient()
        {
        public void onProgressChanged(WebView view, int progress)
            {
            // Activities and WebViews measure progress with different scales.
            // The progress meter will automatically disappear when we reach 100%
            // activity.setTitle("Loading...");
            activity.setProgress(progress * 100);
            }
        });


        final String searchText = "ERROR";

        webView.setWebViewClient(new WebViewClient()
        {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
            Toast.makeText(WebViewActivity.this, description, Toast.LENGTH_SHORT).show();
            }

        // http://stackoverflow.com/a/32720167
        @Override
        public void onPageFinished(WebView view, String url)
            {
            if (searchText != null && !searchText.equals(""))
                {
                webView.findAllAsync(searchText);

/*              int i = webView.findAll(searchText);
                Toast.makeText(getApplicationContext(), "Found " + i + " results !", Toast.LENGTH_SHORT).show();
                try
                    {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, true);
                    }
                catch (Throwable ignored) { }
*/
                }
            }
        });

        webView.getSettings().setBuiltInZoomControls(true);

/*      // webView.getSettings().setAllowFileAccess(true); // true by default
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
            Log.d("TAG", "No SDCARD");
            }
        else
            {
            webView.loadUrl("file://"+Environment.getExternalStorageDirectory()+"/_bestboard/coat.log");
            }
*/

//        webView.loadUrl("file:///android_asset/help.html");

        Uri data = getIntent().getData();

        if (data != null)
            {
            webView.loadUrl(data.toString());
            }
        // String customHtml = "<html><body><h1>Hello, WebView</h1></body></html>";
        else
            {
            webView.loadData(getIntent().toString(), "text/html", "UTF-8");
            }




        }

    @Override
    public void onBackPressed()
        {
        if(webView.canGoBack() )
            {
            webView.goBack();
            }
        else
            {
            super.onBackPressed();
            }
        }

/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
        {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack())
            {
            webView.goBack();
            return true;
            }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
        }
*/
    }

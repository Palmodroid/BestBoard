package org.lattilad.bestboard.webview;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.lattilad.bestboard.R;

import java.io.File;

/**
 * Simple webview to show html text
 * https://developer.chrome.com/multidevice/webview/gettingstarted
 */
public class WebViewActivity extends Activity
    {
    // Extra of Intents
    static final public String ASSET = "ASSET";
    static final public String FILE = "FILE";
    static final public String WORK = "WORK";

    static final public String SEARCH = "SEARCH";

    private WebView webView;
    private ProgressBar progressBar;
    private RelativeLayout toolLayout;
    private EditText filter;


    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.web_view_activity);

        webView = (WebView) findViewById(R.id.webView);
        toolLayout = (RelativeLayout) findViewById(R.id.tool);
        progressBar = (ProgressBar) findViewById(R.id.progress); // default max value = 100
        filter = (EditText) findViewById(R.id.filter);

        findViewById(R.id.back).setOnClickListener(
                new View.OnClickListener()
                {
                @Override
                public void onClick(View v)
                    {
                    webView.findNext(false); // goBack();
                    }
                });
        findViewById(R.id.forth).setOnClickListener(
                new View.OnClickListener()
                {
                @Override
                public void onClick(View v)
                    {
                    webView.findNext( true ); // goForward();
                    }
                });

        // It is needed only for real web-pages
        // webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.setWebChromeClient(
                new WebChromeClient()
                {
                public void onProgressChanged(WebView view, int progress)
                    {
                    progressBar.setProgress(progress); // default max value = 100
                    }
                });

        webView.setWebViewClient(
                new WebViewClient()
                        {
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
                            {
                            Toast.makeText(WebViewActivity.this, description, Toast.LENGTH_SHORT).show();
                            }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon)
                            {
                            progressBar.setVisibility(View.VISIBLE);
                            }

                        // http://stackoverflow.com/a/32720167
                        @Override
                        public void onPageFinished(WebView view, String url)
                            {
                            progressBar.setVisibility( View.GONE );

                            String searchText = filter.getText().toString();
                            if ( !searchText.equals("") )
                                {
                                webView.findAllAsync(searchText);
                                }
                            }
                        });

        String string = null;
        Uri uri;

        // extra: ASSET
        if ( ( string = getIntent().getStringExtra(ASSET) ) != null )
            {
            string = "file:///android_asset/" + string;
            }

        // extra: WORK
        else if ( ( string = getIntent().getStringExtra(WORK) ) != null &&
                Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ))
            {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String directoryName =
                    sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                            getString( R.string.descriptor_directory_default ));
            File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

            string = Uri.fromFile( new File( directoryFile, string ) ).toString();
            }

        // extra: FILE
        else if ( ( string = getIntent().getStringExtra(FILE) ) != null &&
                Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ))
            {
            string = Uri.fromFile( new File( Environment.getExternalStorageDirectory(), string ) ).toString();
            }

        // data: uri
        else if ( ( uri = getIntent().getData() ) != null )
            {
            string = uri.toString();
            }

        if ( string != null )
            {
            webView.loadUrl( string );
            }
        else
            {
            String customHtml = "<html><body>No uri can be found!</body></html>";
            webView.loadData( customHtml, "text/html", "UTF-8");
            }

        // extra: SEARCH
        if ( ( string = getIntent().getStringExtra(SEARCH) ) != null )
            {
            filter.setText( string );
            toolLayout.setVisibility( View.VISIBLE );
            }
        else
            {
            toolLayout.setVisibility( View.GONE );
            }

        }

    /**
     * BACK functions like BACK on the toolbar, but at the last step this BACK can exit
     */
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

/*  This method is not necessary, onBackPressed is better
    @Override
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

    // options menu contains only a dummy menu entry
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view_menu, menu);
        return true;
        }

    // option menu is be never displayed,
    // but menu button toggles toolbar visibility
    // this way no real menu button is needed - all menu activations will toggle the toolbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
        {
        super.onPrepareOptionsMenu(menu);
        toolLayout.setVisibility( toolLayout.getVisibility() == View.VISIBLE ?
                View.GONE : View.VISIBLE );
        return false;
        }

    // This is not needed, because there is only a dummy entry
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
/*
        int id = item.getItemId();

        switch (id)
            {
            case R.id.dummy_menu:
                Toast.makeText(getApplicationContext(), "About menu item pressed", Toast.LENGTH_SHORT).show();
                break;
            }
*/
        return super.onOptionsItemSelected(item);
        }

    }

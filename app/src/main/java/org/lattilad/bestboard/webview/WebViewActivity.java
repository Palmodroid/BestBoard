package org.lattilad.bestboard.webview;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;

import java.io.File;

/**
 * Simple webview to show html text
 * https://developer.chrome.com/multidevice/webview/gettingstarted
 *
 * WebView should be retained between activity starts BUT!
 * WebView contains the context, so simple fragment retention will leak.
 * Activity should be retained.
 * http://www.devahead.com/blog/2012/01/preserving-the-state-of-an-android-webview-on-screen-orientation-change/
 */
public class WebViewActivity extends Activity
    {
    // Retained layout elements
    private WebView webView;
    private boolean searchBarEnabled = false;
    private String retainedSearchText = "";

    // Changing layout elements
    private FrameLayout webViewFrame;
    private ProgressBar progressBar;
    private RelativeLayout searchBar;
    private EditText searchText;

    // Extra of Intents
    static final public String ASSET = "ASSET";
    static final public String FILE = "FILE";
    static final public String WORK = "WORK";

    static final public String SEARCH = "SEARCH";


    protected void initContentView( )
        {
        Scribe.locus(Debug.WEBVIEW);

        setContentView(R.layout.web_view_activity);

        webViewFrame = (FrameLayout)findViewById(R.id.webViewFrame);
        progressBar = (ProgressBar) findViewById(R.id.progress); // default max value = 100
        progressBar.setVisibility( View.GONE );
        // Cannot be sure, if page is loaded, so no progress bar is shown after config change
        searchBar = (RelativeLayout) findViewById(R.id.tool);
        searchText = (EditText) findViewById(R.id.filter);

        // Initialize the WebView at new starts
        if (webView == null)
            {
            // Create the webview
            webView = new WebView(this);
            webView.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(true);

            // Load the starting page
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
                webView.loadUrl(string);
                }
            else
                {
                String customHtml = "<html><body>No uri can be found!</body></html>";
                webView.loadData(customHtml, "text/html", "UTF-8");
                }

            // extra: SEARCH
            if ( ( string = getIntent().getStringExtra(SEARCH) ) != null && !string.equals("") )
                {
                retainedSearchText = string;
                searchBarEnabled = true;
                }
            }

        // Attach the WebView to its placeholder
        webViewFrame.addView(webView);

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
                    webView.findNext(true); // goForward();
                    }
                });

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
                    Scribe.locus(Debug.WEBVIEW);

                    progressBar.setVisibility(View.VISIBLE);
                    }

                // http://stackoverflow.com/a/32720167
                @Override
                public void onPageFinished(WebView view, String url)
                    {
                    Scribe.locus(Debug.WEBVIEW);

                    progressBar.setVisibility(View.GONE);

                    String searchText = WebViewActivity.this.searchText.getText().toString();
                    if (!searchText.equals(""))
                        {
                        webView.findAllAsync(searchText);
                        }
                    }
                });

        searchText.addTextChangedListener(
                new TextWatcher()
                {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                    webView.findAllAsync( s.toString() );
                    }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {}
                });

        searchBar.setVisibility( searchBarEnabled ? View.VISIBLE : View.GONE );
        searchText.setText( retainedSearchText );
        }


    protected void onCreate(Bundle savedInstanceState)
        {
        Scribe.locus(Debug.WEBVIEW);

        super.onCreate(savedInstanceState);
        initContentView();
        }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
        {
        Scribe.locus(Debug.WEBVIEW);

        if (webView != null)    webViewFrame.removeView(webView);
        retainedSearchText = searchText.getText().toString();

        super.onConfigurationChanged(newConfig);

        initContentView();
        }


    @Override
    protected void onSaveInstanceState(Bundle outState)
        {
        Scribe.locus(Debug.WEBVIEW);

        super.onSaveInstanceState(outState);

        // Save the state of the WebView
        webView.saveState(outState);
        }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
        {
        Scribe.locus(Debug.WEBVIEW);

        super.onRestoreInstanceState(savedInstanceState);

        // Restore the state of the WebView
        webView.restoreState(savedInstanceState);
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
        {
        if ( keyCode == KeyEvent.KEYCODE_MENU )
            {
            if (searchBarEnabled)
                {
                searchBarEnabled = false;
                searchBar.setVisibility(View.GONE);
                }
            else
                {
                searchBarEnabled = true;
                searchBar.setVisibility(View.VISIBLE);
                }
            return true;
            }
        return super.onKeyDown(keyCode, event);
        }
    }

package org.lattilad.bestboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.SoftBoardParser;
import org.lattilad.bestboard.parser.SoftBoardParser.SoftBoardParserListener;
import org.lattilad.bestboard.prefs.PrefsActivity;
import org.lattilad.bestboard.prefs.PrefsFragment;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.webview.WebViewActivity;

import java.io.File;


public class SoftBoardService extends InputMethodService implements
        SoftBoardParserListener,
        SharedPreferences.OnSharedPreferenceChangeListener
    {
    /**
     * Name of coat descriptor file in working directory - moved to preferences
     * Temporary storage for file name - it is needed in softBoardParserFinished() once more
     */
    public String coatFileName;

    /** Fullscreen is not implemented yet!! */
    private boolean denyFullScreen = true;

    /** Text for warningText field of noKeyboardView, if no softboard is active */
    private String warning = null;

    /** Parser runs as an asyncTask, returning softBoardData in softBoardParserFinished() */
    private SoftBoardParser softBoardParser;

    /** Receives MEDIA_MOUNTED broadcast, if SD_CARD is not ready */
    private BroadcastReceiver receiver = null;

    /** Connection to the text-processor part */
    private SoftBoardProcessor softBoardProcessor = null;

    /** Just to store a non-active softBoardData instance */
    private SoftBoardData storedSoftBoardData = null;

    /**
     * Service is notified if it needs to react preference changes.
     * Preference PREFS_COUNTER is incremented, and preference PREFS_TYPE identifies enterAction.
     * It is not necessary to check other changes.
     * @param sharedPrefs shared preferences
     * @param key key which is changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
        {
        if ( key.equals( PrefsFragment.PREFS_COUNTER ) )
            {
            switch ( sharedPrefs.getInt( PrefsFragment.PREFS_TYPE, -1 ) )
                {
                case PrefsFragment.PREFS_ACTION_RELOAD:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to reload descriptor." );
                    startSoftBoardParser( null );
                    break;

                case PrefsFragment.PREFS_ACTION_RECALCULATE:
                    Scribe.note(Debug.SERVICE, "SERVICE: get notification to recalculate descriptor.");
                    if ( softBoardProcessor != null)
                        {
                        softBoardProcessor.getSoftBoardData().readPreferences();
                        softBoardProcessor.getSoftBoardData().boardTable.invalidateCalculations( false );
                        softBoardProcessor.getLayoutView().requestLayout();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_REDRAW:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to redraw descriptor." );
                    if ( softBoardProcessor != null)
                        {
                        softBoardProcessor.getSoftBoardData().readPreferences();
                        softBoardProcessor.getSoftBoardData().boardTable.invalidateCalculations( true );
                        softBoardProcessor.getLayoutView().requestLayout();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_REFRESH:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to refresh preferences." );
                    if ( softBoardProcessor != null)
                        {
                        softBoardProcessor.getSoftBoardData().readPreferences();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_CLEAR_SPEDOMETER:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to clear spedometer data." );
                    if ( softBoardProcessor != null)
                        {
                        softBoardProcessor.getSoftBoardData().characterCounter.clear();
                        softBoardProcessor.getSoftBoardData().buttonCounter.clear();
                        softBoardProcessor.getSoftBoardData().showTiming();
                        }
                    break;

/*                case PrefsFragment.PREFS_ACTION_STORE_DATA:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to store softBoardData state." );
                    if ( softBoardProcessor != null)
                        {
                        storedSoftBoardData = softBoardProcessor.getSoftBoardData();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_RECALL_DATA:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to restore softBoardData state." );
                    if ( softBoardProcessor != null && storedSoftBoardData != null )
                        {
                        // softBoardProcessor.changeSoftBoardData( storedSoftBoardData );

                        softBoardProcessor = new SoftBoardProcessor( this, storedSoftBoardData );
                        softBoardProcessor.initInput();
                        }
                    break;
*/

                // TEST MODE cannot be set from here, because of endless loop!
                // if softboardmode is NOT stored, it will be stored
                // reload is started
                case PrefsFragment.PREFS_ACTION_TEST_LOAD:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to load test mode." );
                    if ( softBoardProcessor != null && storedSoftBoardData == null )
                        {
                        storedSoftBoardData = softBoardProcessor.getSoftBoardData();
                        }
                    startSoftBoardParser( null );

                    break;

                // TEST MODE cannot be set from here, because of endless loop!
                // if softboardmode is stored, it will be restored
                // otherwise reload is started
                case PrefsFragment.PREFS_ACTION_TEST_RETURN:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to return from test mode." );
                    if ( storedSoftBoardData != null )
                        {
                        // softBoardProcessor.changeSoftBoardData( storedSoftBoardData );
                        // it is a better idea to start a new softBoardProcessor
                        softBoardProcessor = new SoftBoardProcessor( this, storedSoftBoardData );
                        storedSoftBoardData = null; // In MAIN MODE this should be cleared
                        softBoardProcessor.initInput();
                        }
                    else
                        {
                        startSoftBoardParser( null );
                        }
                    break;

                default:
                    Scribe.error( "SERVICE: preference Action type is invalid!" );
                }
            }
        }


    /**
     * Log should be checked regularly.
     * Service could live longer, it is not enough to check only during creation.
     * onWindowHidden() or onFinishInput() could be a good place.
     */
    @Override
    public void onWindowHidden()
        {
        super.onWindowHidden();

        Scribe.checkLogFileLength(); // Primary log will log several runs
        }


    /**
     * This is the simplest InputMethodService implementation.
     * We just need a simple View. If user "clicks" on it then the InputMethodPicker will show up.
     */
    public View noKeyboardView()
        {
        Scribe.locus(Debug.SERVICE);

        View noKeyboardView = getLayoutInflater().inflate(R.layout.service_nokeyboard, null);
        noKeyboardView.setOnClickListener( new View.OnClickListener()
        {
        @Override
        public void onClick( View view )
            {
            Intent intent = new Intent( SoftBoardService.this, PrefsActivity.class);
            //intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent);

            // Keyboard picker is always available above Ver 4.0, so we use settings instead of picker
            // InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
            // imm.showInputMethodPicker();
            }
        } );

        // Warning comes from the onCreate method
        if ( warning != null )
            {
            Scribe.debug( Debug.SERVICE,  "Warning text has changed: " + warning );
            TextView warningText = (TextView) noKeyboardView.findViewById( R.id.warning_text );
            warningText.setText( warning );
            }
        else
            {
            Scribe.debug( Debug.SERVICE,  "Warning text is empty!");
            }

        return noKeyboardView;
        }


    /**
     * SoftBoard service starts here.
     * First parsing is started, too.
     */
    @Override
    public void onCreate()
        {
        // This should be called at every starting point
        Ignition.start(this);

        Scribe.title( "SOFT-LAYOUT-SERVICE HAS STARTED" );
        Scribe.locus( Debug.SERVICE );

        super.onCreate();

        // Connect to preferences
        PreferenceManager.getDefaultSharedPreferences( this ).registerOnSharedPreferenceChangeListener(this);

        // Start the first parsing
        startSoftBoardParser( null );
        }


    /**
     * SoftBoard service finishes here
     */
    @Override
    public void onDestroy()
        {
        Scribe.locus( Debug.SERVICE );
        Scribe.title("SOFT-LAYOUT-SERVICE HAS FINISHED");

        super.onDestroy();

        // Release preferences
        PreferenceManager.getDefaultSharedPreferences( this ).unregisterOnSharedPreferenceChangeListener(this);

        // Stop any ongoing parsing
        if ( softBoardParser != null)   softBoardParser.cancel(false);

        // Release receiver
        if ( receiver != null ) unregisterReceiver(receiver);

        // Service finishes here, no need to null these pointers
        }


    private boolean isTestMode()
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( this );
        boolean testMode = sharedPrefs.getBoolean(getString(R.string.test_mode_key),
                getResources().getBoolean(R.bool.test_mode_default));
        return testMode;
        }

    private void stopTestMode()
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( this );
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean( getString(R.string.test_mode_key), false );
        editor.apply();
        }

    /**
     * Stops any previous parsing, and starts a new parse.
     * As a result a completely new soft-layout will be created.
     */
    public void startSoftBoardParser( final String coatFileName )
        {
        // sd-card is not ready, waiting for MEDIA_MOUNTED broadcast
        if ( !Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ))
            {
            Scribe.note( Debug.SERVICE, "Waiting for sd-card..." );

            warning = "Be patient! SD_CARD is not ready yet!";
            Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();
            setInputView( noKeyboardView() );

            if ( receiver == null )
                {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                filter.addDataScheme("file");

                receiver = new BroadcastReceiver()
                {
                @Override
                public void onReceive(Context context, Intent intent)
                    {
                    SoftBoardService.this.startSoftBoardParser( coatFileName );
                    }
                };

                registerReceiver(receiver, filter);
                }

            return;
            }

        // sd-card is ready - release receiver if started
        if ( receiver != null )
            {
            warning = "SD_CARD is ready, BestBoard is loading. Please, wait! ";
            Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();
            setInputView( noKeyboardView() );

            unregisterReceiver(receiver);
            receiver = null;
            }

        Scribe.note(Debug.SERVICE, "Parsing has started.");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String directoryName =
                sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                        getString( R.string.descriptor_directory_default ));
        File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

        if ( coatFileName == null )
            {
            if ( !isTestMode() )
                {
                this.coatFileName =
                        sharedPrefs.getString(getString(R.string.descriptor_file_key),
                                getString(R.string.descriptor_file_default));
                storedSoftBoardData = null; // In MAIN MODE this should be cleared
                }
            else
                {
                this.coatFileName =
                        sharedPrefs.getString(getString(R.string.test_file_key),
                                getString(R.string.test_file_default));
                }
            }
        else
            {
            // preferences are NOT changed, so draft will bring back to the main keyboard!
            this.coatFileName = coatFileName;
            }

        // Any previous parsing should stop now
        if ( softBoardParser != null )  softBoardParser.cancel(false);

        Toast.makeText( this, "Parsing of <" + this.coatFileName + "> has been started! Be patient!", Toast.LENGTH_LONG ).show();
        softBoardParser = new SoftBoardParser(this, directoryFile, this.coatFileName );
        softBoardParser.execute();

        // SoftBoard returns in softBoardParserFinished() after parsing
        }


    /**
     * Soft-layout is first displayed. This will be called after orientation change.
     * Originally noKeyboardView is displayed.
     * If soft-layout is ready, then it will be returned.
     * @return view of the current keyboard
     */
    @Override
    public View onCreateInputView()
        {
        Scribe.locus(Debug.SERVICE);

        if (softBoardProcessor == null)
            {
            Scribe.note(Debug.SERVICE, "Soft-layout is not ready yet, no-keyboard-view will be displayed.");
            warning = "BestBoard is loading. Please, wait! ";

            return noKeyboardView();
            }
        else
            {
            Scribe.note(Debug.SERVICE, "Soft-layout ready, it will be displayed initially.");
            return softBoardProcessor.onCreateInputView();
            }
        }


    /**
     * If denyFullScreen is true then fullscreen will be never allowed.
     * Otherwise system decides (fullscreen will be allowed in landscape, if editor allows it)
     * !! Full-screen is not implemented yet !!
     */
    @Override
    public boolean onEvaluateFullscreenMode()
        {
        if ( denyFullScreen )
            return false;
        else
            return super.onEvaluateFullscreenMode();
        }


    /**
     * Parsing of soft-layout finished, but soft-layout cannot be displayed because of critical errors.
     * @param errorInfo id of the critical error
     */
    @Override
    public void softBoardParserCriticalError(int errorInfo)
        {
        Scribe.locus(Debug.SERVICE);

        switch ( errorInfo )
            {
            case SoftBoardParser.CRITICAL_FILE_NOT_FOUND_ERROR:
                warning = "Critical error! Could not find coat file! Please, check preferences!";
                break;
            case SoftBoardParser.CRITICAL_IO_ERROR:
                warning = "Critical error! Could not read sd-card!";
                break;
            case SoftBoardParser.CRITICAL_NOT_VALID_FILE_ERROR:
                warning = "Critical error! Coat file is not valid! Please, correct it, or a choose an other coat file in the preferences!";
                break;
            case SoftBoardParser.CRITICAL_PARSING_ERROR:
                warning = "Critical error! No layout is defined in coat file! Please, correct it!";
                break;
            // no warning is necessary for CANCEL
            default:
                warning = "Process is cancelled!";
            }
        // Generating a new view with the warning
        Scribe.debug( Debug.SERVICE, warning );
        setInputView(noKeyboardView());
        // Warning should be shown! Keyboard can be hidden
        Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();

        softBoardParser = null;

        if ( !isTestMode() )
            {
            Intent intent = new Intent(this, PrefsActivity.class);
            //intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(intent);
            }
        else
            {
            stopTestMode(); // Critical error in test file will return to main file
            startSoftBoardParser( null );
            }
        }


    /**
     * Soft-layout starts to work here.
     * Parsing has finished, new soft-layout and new boardView is set.
     * @param softBoardData newly generated class containing all softboard data
     * @param errorCount    number of non-critical errors (error messages can be found in the log)
     */
    @Override
    public void softBoardParserFinished(SoftBoardData softBoardData, int errorCount)
        {
        Scribe.locus(Debug.SERVICE);

        // non-critical errors
        if ( errorCount != 0 )
            {
            // Generating a new view
            warning = "Parsing of <" + coatFileName + "> has finished with " +
                    errorCount + " errors.\n" +
                    "Please, check log-file for details!";
            Scribe.debug( Debug.SERVICE,  warning );
            // Warning should be shown!
            Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();

            if ( isTestMode() )
                {
                // Display coat.log
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.WORK, Debug.coatLogFileName);
                intent.putExtra(WebViewActivity.SEARCH, "ERROR");
                //intent.setData(Uri.parse("http://lattilad.org/"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addFlags( Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
                //intent.addFlags( Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY );
                //  intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //intent.addFlags( Intent.FLAG_FROM_BACKGROUND );
                startActivity(intent);
                }
            }
        // parsing finished without errors, but this is not the first parsing!
        else if ( this.softBoardProcessor != null )
            {
            // Generating a new view
            warning = "Parsing of <" + coatFileName + "> has finished.";
            Scribe.debug( Debug.SERVICE,  warning );
            // Warning should be shown!
            Toast.makeText( this, warning, Toast.LENGTH_SHORT ).show();
            }

        softBoardParser = null;

        softBoardProcessor = new SoftBoardProcessor( this, softBoardData );
        softBoardProcessor.initInput();
        }


    /**
     * lastCharacter and calculatedPositions are set based on EditorInfo data.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting)
        {
        super.onStartInput(attribute, restarting);
        Scribe.locus(Debug.SERVICE);

        if ( softBoardProcessor != null )
            softBoardProcessor.initInput();
        }


    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd)
        {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        if ( softBoardProcessor != null )
            softBoardProcessor.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                    candidatesStart, candidatesEnd);
        }


    /*
	 * Hard-keyboard and other outer sources can be controlled here
	 */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
        {
        Scribe.note(Debug.SERVICE, "External hard key is DOWN: " + keyCode);
        return super.onKeyDown(keyCode, event);
        }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
        {
        Scribe.note(Debug.SERVICE, "External hard key is UP: " + keyCode);
        return super.onKeyUp(keyCode, event);
        }
    }


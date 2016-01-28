package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import dancingmoon.bestboard.SoftBoardData.SoftBoardListener;
import dancingmoon.bestboard.buttons.PacketText;
import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.parser.SoftBoardParser;
import dancingmoon.bestboard.parser.SoftBoardParser.SoftBoardParserListener;
import dancingmoon.bestboard.prefs.PrefsFragment;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.server.Connection;
import dancingmoon.bestboard.server.TextAfterCursor;
import dancingmoon.bestboard.server.TextBeforeCursor;
import dancingmoon.bestboard.states.LayoutStates;
import dancingmoon.bestboard.states.CapsState;


public class SoftBoardService extends InputMethodService implements
        SoftBoardParserListener,
        SoftBoardListener,
        Connection,
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

    /** Data structure for the whole softBoard */
    private SoftBoardData softBoardData = null;

    /** There is only ONE boardView for the whole softBoard, generated in softBoardParserFinished() */
    private LayoutView layoutView;

    @Override
    public LayoutView getLayoutView()
        {
        return layoutView;
        }

    /** Temporary variable to store string to be send */
    private StringBuilder sendBuilder = new StringBuilder();

    @Override
    public SoftBoardListener getSoftBoardDataListener()
        {
        return this;
        }

    /**
     * Service is notified if it needs to react preference changes.
     * Preference PREFS_COUNTER is incremented, and preference PREFS_TYPE identifies action.
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
                    startSoftBoardParser();
                    break;

                case PrefsFragment.PREFS_ACTION_RECALCULATE:
                    Scribe.note( Debug.SERVICE, "SERVICE: get notification to recalculate descriptor." );
                    if ( softBoardData != null)
                        {
                        softBoardData.readPreferences();
                        softBoardData.boardLinks.invalidateCalculations( false );
                        layoutView.requestLayout();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_REDRAW:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to redraw descriptor." );
                    if ( softBoardData != null)
                        {
                        softBoardData.readPreferences();
                        softBoardData.boardLinks.invalidateCalculations( true );
                        layoutView.requestLayout();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_REFRESH:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to refresh preferences." );
                    if ( softBoardData != null)
                        {
                        softBoardData.readPreferences();
                        }
                    break;

                default:
                    Scribe.error( "SERVICE: preference action type is invalid!" );
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
        Scribe.locus( Debug.SERVICE );

        View noKeyboardView = getLayoutInflater().inflate(R.layout.service_nokeyboard, null);
        noKeyboardView.setOnClickListener( new View.OnClickListener()
        {
        @Override
        public void onClick( View view )
            {
            InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.showInputMethodPicker();
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
        PreferenceManager.getDefaultSharedPreferences( this ).registerOnSharedPreferenceChangeListener( this );

        // Start the first parsing
        startSoftBoardParser();
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
        PreferenceManager.getDefaultSharedPreferences( this ).unregisterOnSharedPreferenceChangeListener( this );

        // Stop any ongoing parsing
        if ( softBoardParser != null)   softBoardParser.cancel(false);
        }


    /**
     * Stops any previous parsing, and starts a new parse.
     * As a result a completely new soft-layout will be created.
     */
    public void startSoftBoardParser()
        {
        Scribe.note( Debug.SERVICE, "Parsing has started." );

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String directoryName =
                sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                        getString( R.string.descriptor_directory_default ));
        File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

        coatFileName =
                sharedPrefs.getString( getString( R.string.descriptor_file_key ),
                        getString( R.string.descriptor_file_default ));
        File coatFileFile = new File( directoryFile, coatFileName );

        // Any previous parsing should stop now
        if ( softBoardParser != null )  softBoardParser.cancel(false);

        softBoardParser = new SoftBoardParser(this, coatFileFile );
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

        if (softBoardData == null)
            {
            Scribe.note(Debug.SERVICE, "Soft-layout is not ready yet, no-keyboard-view will be displayed.");
            return noKeyboardView();
            }
        else
            {
            Scribe.note( Debug.SERVICE, "Soft-layout ready, it will be displayed initially.");

            // setting index is not necessary
            softBoardData.boardLinks.setOrientation();

            // boardView should be saved
            ViewGroup parent = (ViewGroup) getLayoutView().getParent();
            if (parent != null) 
                {
                parent.removeView( getLayoutView() );
                }

            getLayoutView().setLayout(softBoardData.boardLinks.getActiveBoard());

            return getLayoutView();
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
                warning = "Critical error!";
            }
        // Generating a new view with the warning
        Scribe.debug( Debug.SERVICE, warning );
        setInputView(noKeyboardView());
        // Warning should be shown! Keyboard can be hidden
        Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();

        softBoardParser = null;
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
            }
        // parsing finished without errors, but this is not the first parsing!
        else if ( this.softBoardData != null )
            {
            // Generating a new view
            warning = "Parsing of <" + coatFileName + "> has finished.";
            Scribe.debug( Debug.SERVICE,  warning );
            // Warning should be shown!
            Toast.makeText( this, warning, Toast.LENGTH_SHORT ).show();
            }

        this.softBoardData = softBoardData;
        
        // Orientation should be checked, but index is 0 by default.
        // No setIndex() is needed
        softBoardData.boardLinks.setOrientation();

        layoutView = new LayoutView( this );
        layoutView.setLayout(softBoardData.boardLinks.getActiveBoard());
        
        setInputView(layoutView);

        softBoardParser = null;

        initInput();
        }


    /**
     ** TEXT-PROCESSING PART
     **/


    /**
     * FALSE: getText... methods are disabled - no communication is enabled
     */
    private boolean retrieveTextEnabled = true;

    /**
     * FALSE: stored text and calculated position are used; cursor position is checked after time-limit
     * TRUE: text around cursor is always re-read, stored text/cursor position are not used
     */
    // private boolean heavyCheckEnabled = false;
    private boolean storeTextEnabled = true;

    /** public access is needed by Connection */
    public boolean isStoreTextEnabled()
        {
        return storeTextEnabled;
        }

    /** realCursorPosition value if text is selected */
    private int TEXT_SELECTED = Integer.MIN_VALUE;

    /** Cursor position presented by the editor */
    private int realCursorPosition;

    /** Cursor position calculated by the softkeyboard */
    private int calculatedCursorPosition = 0;

    /** Lastly sent string. Null if undo is not possible */
    private String undoString;

    private static final long NEVER = Long.MAX_VALUE;

    private static final long ALWAYS = 0L;

    // -> CHAT
    private long checkEnabledAfter = ALWAYS;

    private int elongationPeriod;

/*
? heavyCheckEnabled
realCursorPosition= ; // -1 text selected
calculatedCursorPosition= ;
checkTimeLimit= ; // 0L - always allowed (no limit)  Long.MAX_VALUE - never allowed
undoString= ;
textBeforeCursor. ;
textAfterCursor. ;
 */

    /** Text before the cursor is stored in textBeforeCursor. Text is provided by IMS */
    private TextBeforeCursor textBeforeCursor = new TextBeforeCursor( this );

    public TextBeforeCursor getTextBeforeCursor()
        {
        return textBeforeCursor;
        }

    /** Text after the cursor is stored in textBeforeCursor. Text is provided by IMS */
    private TextAfterCursor textAfterCursor = new TextAfterCursor( this );

    public TextAfterCursor getTextAfterCursor()
        {
        return textAfterCursor;
        }


    /**
     * lastCharacter and calculatedPositions are set based on EditorInfo data.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting)
        {
        super.onStartInput(attribute, restarting);
        Scribe.locus( Debug.SERVICE );

        initInput();
        }


    /**
     * Initializes a new text session.
     * This happens at each EditText start, and at each uncontrolled cursor movements
     */
    private void initTextSession()
        {
        // no undo at start
        undoString = null;

        // surrounding text is changed
        textBeforeCursor.invalidate();
        textAfterCursor.invalidate();

        if ( softBoardData != null && softBoardData.textSessionSetsMetastates )
            {
            layoutView.type();
            ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                    .setAutoCapsState(CapsState.AUTOCAPS_OFF);
            layoutView.invalidate();
            }

        }


    public void initInput()
        {
        if (softBoardData != null)
            {
            Scribe.title( "Editor session started" );

            EditorInfo editorInfo = getCurrentInputEditorInfo();
            Scribe.debug( Debug.TEXT, "Start: " + editorInfo.initialSelStart + ", end: " + editorInfo.initialSelEnd);

            // position of the cursor
            calculatedCursorPosition = editorInfo.initialSelStart;

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( this );
            String retrieveTextPreference = sharedPrefs.getString(getString(R.string.editing_retrieve_text_key),
                    getString(R.string.editing_retrieve_text_default));

            if ( retrieveTextPreference.startsWith("E") )
                {
                retrieveTextEnabled = true;
                Scribe.debug(Debug.CURSOR, "Editing (retrieve text) is enabled (forced)");
                }
            else if ( retrieveTextPreference.startsWith("D") )
                {
                retrieveTextEnabled = false;
                Scribe.debug(Debug.CURSOR, "Editing (retrieve text) is disabled (forced)");
                }
            else
                {
                retrieveTextEnabled = (calculatedCursorPosition >= 0);
                Scribe.debug(Debug.CURSOR, "Editing is set automatically. Retrieve text: " + retrieveTextEnabled);
                }

            String storeTextPreference = sharedPrefs.getString(getString(R.string.editing_store_text_key),
                    getString(R.string.editing_store_text_default));

            if ( storeTextPreference.startsWith("E") )
                {
                storeTextEnabled = true;
                Scribe.debug(Debug.CURSOR, "Editing (store text) is enabled (forced), LIGHT check.");
                }
            else if ( storeTextPreference.startsWith("D") )
                {
                storeTextEnabled = false;
                Scribe.debug(Debug.CURSOR, "Editing (store text) is disabled (forced), HEAVY check.");
                }
            else
                {
                int inputType = editorInfo.inputType & InputType.TYPE_MASK_CLASS;

                if ( inputType == InputType.TYPE_CLASS_TEXT || inputType == 0 )
                    {
                    storeTextEnabled = true;
                    Scribe.debug(Debug.CURSOR, "Editing (store text) is enabled automatically. LIGHT CHECK" );
                    }
                else
                    {
                    storeTextEnabled = false;
                    Scribe.debug(Debug.CURSOR, "Editing (store text) is disabled automatically. HEAVY CHECK" );
                    }
                }

            elongationPeriod = sharedPrefs.getInt( PrefsFragment.EDITING_ELONGATION_PERIOD_INT_KEY, 0 );
            Scribe.debug( Debug.CURSOR, "Elongation period: " + elongationPeriod );

            if ( editorInfo.initialSelStart != editorInfo.initialSelEnd )
                {
                // text is selected
                realCursorPosition = TEXT_SELECTED;
                Scribe.debug( Debug.CURSOR, "Text is selected");
                }
            else
                {
                // text is not selected
                realCursorPosition = editorInfo.initialSelStart;
                Scribe.debug(Debug.CURSOR, "Cursor position: " + realCursorPosition);
                }

            // pressed hard-keys are released
            softBoardData.layoutStates.resetSimulatedMetaButtons();

            // enter's title is set
            softBoardData.setAction(editorInfo.imeOptions);

            initTextSession();

            // set autocaps state depending on field behavior and cursor position
            if ( calculatedCursorPosition == 0 && editorInfo.initialCapsMode != 0 )
                {
                ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                        .setAutoCapsState(CapsState.AUTOCAPS_ON);
                }
            }
        }


    /**
     * This is the most important part:
     * Here we can control whether cursor/selection was changed without our knowledge.
     * Implemented send... methods should set calculatedPosition.
     * Stored text is invalidated if new position does not match calculatedPosition
     * If text is selected, then all stored-text functions are disabled
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd)
        {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        Scribe.locus(Debug.TEXT);

        // Cursor movement checking is independent from selection.
        // Cursor position is the start of the selection.
        if ( calculatedCursorPosition != newSelStart )
            {
            if ( System.nanoTime() > checkEnabledAfter )
                {
                Scribe.debug( Debug.CURSOR, "Cursor is moving. Calculated position does not match: " + calculatedCursorPosition );
                calculatedCursorPosition = newSelStart;
                initTextSession();
                }
            else
                {
                Scribe.debug( Debug.CURSOR, "Text processing is not yet finished. Calculated position does not match: " + calculatedCursorPosition );
                }
            }

        // Text is NOT selected...
        if ( newSelStart == newSelEnd )
            {
            Scribe.debug( Debug.CURSOR, "Real cursor position: " + newSelStart );
            realCursorPosition = newSelStart;
            }

        // Text is selected
        else // newSelStart != newSelEnd
            {
            Scribe.debug( Debug.CURSOR, "Cursor position: Text is selected, cursor position is invalidated!");
            realCursorPosition = TEXT_SELECTED;
            calculatedCursorPosition = newSelStart;

            initTextSession();
            }
        }


    /**
     * This method is called by BoardView.MainTouchBow constructor
     * Only if light check is active:
     * if real and calculated positions are different, then stored text is invalidated
     */
    public void checkAtBowStart()
        {
        Scribe.locus(Debug.CURSOR);

        checkEnabledAfter = ALWAYS;

        if ( storeTextEnabled )
            {
            if (realCursorPosition != calculatedCursorPosition)
                {
                Scribe.debug(Debug.CURSOR, "LIGHT CHECK: Cursor positions doesn't match at the start of the bow!" +
                        " Calculated: " + calculatedCursorPosition +
                        " Real: " + realCursorPosition);

                calculatedCursorPosition = realCursorPosition;
                initTextSession();
                }
            else
                {
                Scribe.debug(Debug.CURSOR, "LIGHT CHECK: Cursor positions match. Position: " + realCursorPosition);
                }
            }
        else
            {
            Scribe.debug(Debug.CURSOR, "HEAVY CHECK: Cursor position check is not needed.");
            }
        }

    public void checkAtStrokeEnd()
        {
        Scribe.locus( Debug.CURSOR );

        if ( checkEnabledAfter == NEVER )
            {
            checkEnabledAfter = System.nanoTime() + (long)elongationPeriod * 1000000L;
            Scribe.debug( Debug.CURSOR, "Check is enabled after: " + checkEnabledAfter );
            }
        }

    /**
     * Text (string) can be sent ONLY through this method
     * CalculatedPosition is calculated
     * String is sent to the editor (commitText)
     * String is sent to stored-text
     * @param inputConnection input connection - CANNOT BE NULL!
     * @param string string to send - CANNOT BE NULL!
     */
    private void sendString(InputConnection inputConnection, String string)
        {
        Scribe.debug(Debug.TEXT, "String to send: [" + string + "], length: " + string.length());


        undoString = string;
        calculatedCursorPosition += string.length();
        checkEnabledAfter = NEVER;
        if ( storeTextEnabled )
            {
            Scribe.debug(Debug.TEXT, "LIGHT CHECK: Text before cursor is updated.");
            textBeforeCursor.sendString(string);
            textAfterCursor.invalidate();
            }
        inputConnection.commitText(string, 1);

        Scribe.debug(Debug.CURSOR, "String. Calculated cursor position: " + calculatedCursorPosition);
        }


    /**
     * Text can be deleted before cursor ONLY through this method
     * CalculatedPosition is calculated
     * Delete is sent to the editor
     * Delete is sent to stored-text
     * @param inputConnection input connection - CANNOT BE NULL!
     * @param length number of java characters to delete before cursor
     */
    private void sendDeleteBeforeCursor(InputConnection inputConnection, int length)
        {
        Scribe.debug(Debug.TEXT, "Chars to delete before cursor: " + length);

        if ( length > 0 )
            {
            undoString = null;
            calculatedCursorPosition -= length;
            checkEnabledAfter = NEVER;
            if ( storeTextEnabled )
                {
                Scribe.debug(Debug.TEXT, "LIGHT CHECK: Text before cursor is updated (deleted).");
                textBeforeCursor.sendDelete(length);
                }
            inputConnection.deleteSurroundingText(length, 0);
            }

        Scribe.debug(Debug.CURSOR, "Deleted. Calculated cursor position: " + calculatedCursorPosition);
        }


    private void sendDeleteAfterCursor(InputConnection inputConnection, int length)
        {
        Scribe.debug(Debug.TEXT, "Chars to delete after cursor: " + length);

        if ( length > 0 )
            {
            undoString = null;
            // calculatedCursorPosition does not change
            checkEnabledAfter = NEVER;
            if ( storeTextEnabled )
                {
                Scribe.debug(Debug.TEXT, "LIGHT CHECK: Text after cursor is updated (deleted).");
                textAfterCursor.sendDelete(length);
                }
            inputConnection.deleteSurroundingText( 0, length );
            }

        Scribe.debug(Debug.CURSOR, "Deleted. Calculated cursor position: " + calculatedCursorPosition);
        }


    private int sendDeleteSpacesBeforeCursor( InputConnection inputConnection )
        {
        int space;

        textBeforeCursor.reset();
        for ( space = 0; textBeforeCursor.read() == ' '; space++ ) ;

        sendDeleteBeforeCursor(inputConnection, space);

        Scribe.debug(Debug.TEXT, "Spaces deleted before cursor: " + space);
        return space;
        }


    private int sendDeleteSpacesAfterCursor( InputConnection inputConnection )
        {
        int space;

        textAfterCursor.reset();
        for ( space = 0; textAfterCursor.read() == ' '; space++ ) ;

        sendDeleteAfterCursor(inputConnection, space);

        Scribe.debug(Debug.SERVICE, "Spaces deleted after cursor: " + space);
        return space;
        }


    public boolean undoLastString()
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = getCurrentInputConnection();
        if ( ic != null && undoString != null )
            {
            if ( storeTextEnabled )
                {
                sendDeleteBeforeCursor(ic, undoString.length());
                undoString = null;
                return true;
                }
            else
                {
                if (textBeforeCursor.compare(undoString))
                    {
                    Scribe.debug(Debug.TEXT, "HEAVY CHECK: Text undo: string matches!");
                    sendDeleteBeforeCursor(ic, undoString.length());
                    undoString = null;
                    return true;
                    }
                else
                    {
                    Scribe.debug(Debug.TEXT, "HEAVY CHECK: Text undo: string does NOT match!");
                    }
                }
            }
        return false;
        }


    @Override
    public void sendString( String string, int autoSpace )
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();

            if ( (autoSpace & PacketText.ERASE_SPACES_BEFORE) != 0 )
                {
                sendDeleteSpacesBeforeCursor( ic );
                }
            if ( (autoSpace & PacketText.ERASE_SPACES_AFTER) != 0 )
                {
                sendDeleteSpacesAfterCursor( ic );
                }

            sendBuilder.setLength(0);
            if ( (autoSpace & PacketText.AUTO_SPACE_BEFORE) != 0 )
                {
                textBeforeCursor.reset();
                if ( !isWhiteSpace( textBeforeCursor.read()) )
                    sendBuilder.append(' ');
                }

            sendBuilder.append(string);

            if ( (autoSpace & PacketText.AUTO_SPACE_AFTER) != 0 )
                {
                textAfterCursor.reset();
                if ( !isSpace( textAfterCursor.read()) )
                    sendBuilder.append(' ');
                }

            sendString(ic, sendBuilder.toString());

            ic.endBatchEdit();
            }
        }


    public void changeStringBeforeCursor( String string )
        {
        changeStringBeforeCursor(string.length(), string);
        }


    public void changeStringBeforeCursor( int length, String string )
        {
        Scribe.locus(Debug.SERVICE);

        if ( realCursorPosition >= 0 )
            {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                sendDeleteBeforeCursor(ic, length);
                sendString(ic, string);
                }
            }
        }


    // Modify needs it!
    public int deleteSpacesBeforeCursor()
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            return sendDeleteSpacesBeforeCursor( ic );
            }

        return 0;
        }


    // DO BACKSPACE
    public void deleteCharBeforeCursor(int n)
        {
        Scribe.locus(Debug.SERVICE);

        // text is selected
        if ( realCursorPosition == TEXT_SELECTED )
            {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                sendString(ic, "");
                }
            }

        // text is not selected
        else
            {
            textBeforeCursor.reset();
            int data = textBeforeCursor.read();
            int l = 1;

            Scribe.debug( Debug.TEXT, "Character before cursor: " + Integer.toHexString(data));
            if (data < 0)
                {
                Scribe.debug(Debug.TEXT, "No more text to delete, hard DEL is sent!");

                softBoardData.layoutStates.forceBinaryHardState( 0x15 );
                sendKeyDownUp( KeyEvent.KEYCODE_DEL );
                softBoardData.layoutStates.clearBinaryHardState();

                return;
                }
            else if ((data & 0xFC00) == 0xDC00 )
                {
                Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                l++;
                }
            else if ((data & 0xFC00) == 0xD800 )
                {
                Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                }

            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                sendDeleteBeforeCursor(ic, l);
                }
            }
        }


    // DO DELETE
    public void deleteCharAfterCursor(int n)
        {
        Scribe.locus(Debug.SERVICE);

        // text is selected
        if ( realCursorPosition == TEXT_SELECTED )
            {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                sendString(ic, "");
                }
            }

        // text is not selected
        else
            {
            textAfterCursor.reset();
            int data = textAfterCursor.read();
            int l = 1;

            Scribe.debug( Debug.TEXT, "Character after cursor: " + Integer.toHexString(data));
            if (data < 0)
                {
                Scribe.debug(Debug.TEXT, "No more text to delete, hard FORWARD_DEL is sent!");

                softBoardData.layoutStates.forceBinaryHardState( 0x15 );
                sendKeyDownUp( KeyEvent.KEYCODE_FORWARD_DEL );
                softBoardData.layoutStates.clearBinaryHardState();

                return;
                }
            else if ((data & 0xFC00) == 0xD800 )
                {
                Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                l++;
                }
            else if ((data & 0xFC00) == 0xDC00 )
                {
                Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                }

            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                sendDeleteAfterCursor(ic, l);
                }
            }
        }


    /**
     * Simulates key-event
     * @param downTime time, when key was pressed
     * @param eventTime time, when key was released
     * @param keyEventAction KeyEvent.ACTION_DOWN or KeyEvent.ACTION_UP
     * @param keyEventCode android keyCode
     * @return true in success, false if no InputConnection is available
     */
    private boolean sendKeyEvent( long downTime, long eventTime,
                             int keyEventAction, int keyEventCode )
        {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            {
            Scribe.error("Cannot get input connection!");
            return false;
            }

        return ic.sendKeyEvent(new KeyEvent(
                downTime,               // this key originally went down
                eventTime,              // this event happened (downTime in ACTION_DOWN)
                keyEventAction,         // ACTION_DOWN or ACTION_UP
                keyEventCode,           // android keyCode
                0,                      // repeat is not implemented
                softBoardData.layoutStates.getAndroidMetaState(),
                // meta-state in android format
                KeyCharacterMap.VIRTUAL_KEYBOARD,
                // device id FIX
                keyEventCode,           // android keyCode as scan-code
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE,
                InputDevice.SOURCE_TOUCHSCREEN));
        }


    /**
     * Simulates key-down event
     * @param downTime time, when key was pressed
     * @param keyEventCode android keyCode
     * @return true in success, false if no InputConnection is available
     */
    public boolean sendKeyDown( long downTime, int keyEventCode )
        {
        Scribe.debug(Debug.SERVICE, keyEventCode + " hard button is down!");

        return sendKeyEvent( downTime, downTime, KeyEvent.ACTION_DOWN, keyEventCode );
        }


    /**
     * Simulates key-up event
     * @param downTime time, when key was pressed
     * @param eventTime time, when key was released
     * @param keyEventCode android keyCode
     * @return true in success, false if no InputConnection is available
     */
    public boolean sendKeyUp( long downTime, long eventTime, int keyEventCode )
        {
        Scribe.debug(Debug.SERVICE, keyEventCode + " hard button is up!");

        return sendKeyEvent( downTime, eventTime, KeyEvent.ACTION_UP, keyEventCode );
        }


    /**
     * Simulates key-down/key-up sequence
     * Input-connection availability is not returned!
     * @param keyEventCode android keyCode
     */
    public void sendKeyDownUp( int keyEventCode )
        {
        Scribe.debug( Debug.SERVICE, keyEventCode + " hard button is down-up!");

        long downTime = SystemClock.uptimeMillis();
        if (sendKeyDown( downTime, keyEventCode ))
            sendKeyUp( downTime, SystemClock.uptimeMillis(), keyEventCode );
        // ACTION_UP will be sent only, if ACTION_DOWN was successfully sent
        }


    /**
     * Gets text before cursor - needed only by StoredText
     * Text can be retrieved only, if text is NOT selected
     * @param n number of java chars to get
     * @return text or null, if no text is available (or text is selected)
     */
    public CharSequence getTextBeforeCursor( int n )
        {
        if (retrieveTextEnabled)
            {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                return ic.getTextBeforeCursor(n, 0);
                }
            }
        return null;
        }


    /**
     * Gets text after cursor - needed only by StoredText
     * Text can be retrieved only, if text is NOT selected
     * @param n number of java chars to get
     * @return text or null, if no text is available (or text is selected)
     */
    public CharSequence getTextAfterCursor( int n )
        {
        if (retrieveTextEnabled)
            {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                {
                return ic.getTextAfterCursor(n, 0);
                }
            }
        return null;
        }

    public boolean isWhiteSpace( int ch )
        {
        return ch == ' ' || ch == '\n' || ch == '\t';
        }

    public boolean isSpace( int ch )
        {
        return ch == ' ';
        }


	/*
	 * Hard-keyboard and other outer sources can be controlled here
	 */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
        {
        Scribe.note( Debug.SERVICE, "External hard key is DOWN: " + keyCode);
        return super.onKeyDown(keyCode, event);
        }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
        {
        Scribe.note( Debug.SERVICE, "External hard key is UP: " + keyCode);
        return super.onKeyUp(keyCode, event);
        }

    }


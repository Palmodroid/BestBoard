package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import dancingmoon.bestboard.SoftBoardParser.SoftBoardParserListener;
import dancingmoon.bestboard.buttons.PacketText;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.CapsState;


public class SoftBoardService extends InputMethodService implements
        SoftBoardParserListener,
        SoftBoardListener,
        StoredText.Connection,
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
    private BoardView boardView;

    @Override
    public BoardView getBoardView()
        {
        return boardView;
        }

    /** Text around the cursor is stored in storedText. Text is provided by IMS */
    private StoredText storedText = new StoredText( this );

    public StoredText getStoredText()
        {
        return storedText;
        }

    /**
     * cursor position calculated by our IMS. If this not corresponds with real position,
     * then somebody else changed it (and text around cursor is changed, too)!
     * In the case of selection the start of the selection is stored.
     * -1 - text is selected
     */
    private int calculatedPosition = 0;

    /**
     * true, if there weren't any cursor changes since the last command,
     * so last command can be undone.
     */
    private boolean undoEnabled = false;

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
                        softBoardData.linkState.invalidateCalculations( false );
                        boardView.requestLayout();
                        }
                    break;

                case PrefsFragment.PREFS_ACTION_REDRAW:
                    Scribe.note( Debug.SERVICE,  "SERVICE: get notification to redraw descriptor." );
                    if ( softBoardData != null)
                        {
                        softBoardData.readPreferences();
                        softBoardData.linkState.invalidateCalculations( true );
                        boardView.requestLayout();
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
        Scribe.locus(Debug.SERVICE);

        View noKeyboardView = getLayoutInflater().inflate(R.layout.service_nokeyboard, null);
        noKeyboardView.setOnClickListener(new View.OnClickListener()
        {
        @Override
        public void onClick(View view)
            {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showInputMethodPicker();
            }
        });

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

        Scribe.title("SOFT-BOARD-SERVICE HAS STARTED");
        Scribe.locus(Debug.SERVICE);

        super.onCreate();

        // Connect to preferences
        PreferenceManager.getDefaultSharedPreferences( this ).registerOnSharedPreferenceChangeListener(this);

        // Start the first parsing
        startSoftBoardParser();
        }


    /**
     * SoftBoard service finishes here
     */
    @Override
    public void onDestroy()
        {
        Scribe.locus(Debug.SERVICE);
        Scribe.title("SOFT-BOARD-SERVICE HAS FINISHED");

        super.onDestroy();

        // Release preferences
        PreferenceManager.getDefaultSharedPreferences( this ).unregisterOnSharedPreferenceChangeListener(this);

        // Stop any ongoing parsing
        if ( softBoardParser != null)   softBoardParser.cancel(false);
        }


    /**
     * Stops any previous parsing, and starts a new parse.
     * As a result a completely new soft-board will be created.
     */
    public void startSoftBoardParser()
        {
        Scribe.note(Debug.SERVICE, "Parsing has started.");

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
     * Soft-board is first displayed. This will be called after orientation change.
     * Originally noKeyboardView is displayed.
     * If soft-board is ready, then it will be returned.
     * @return view of the current keyboard
     */
    @Override
    public View onCreateInputView()
        {
        Scribe.locus(Debug.SERVICE);

        if (softBoardData == null)
            {
            Scribe.note(Debug.SERVICE, "Soft-board is not ready yet, no-keyboard-view will be displayed.");
            return noKeyboardView();
            }
        else
            {
            Scribe.note( Debug.SERVICE, "Soft-board ready, it will be displayed initially.");

            // setting index is not necessary
            softBoardData.linkState.setOrientation();

            // boardView should be saved
            ViewGroup parent = (ViewGroup) getBoardView().getParent();
            if (parent != null) 
                {
                parent.removeView( getBoardView() );
                }

            getBoardView().setBoard(softBoardData.linkState.getActiveBoard());

            return getBoardView();
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
     * Parsing of soft-board finished, but soft-board cannot be displayed because of critical errors.
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
                warning = "Critical error! No board is defined in coat file! Please, correct it!";
                break;
            // no warning is necessary for CANCEL
            default:
                warning = "Critical error!";
            }
        // Generating a new view with the warning
        Scribe.debug(Debug.SERVICE, warning);
        setInputView(noKeyboardView());
        // Warning should be shown! Keyboard can be hidden
        Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();

        softBoardParser = null;
        }


    /**
     * Soft-board starts to work here.
     * Parsing has finished, new soft-board and new boardView is set.
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
        softBoardData.linkState.setOrientation();

        boardView = new BoardView( this );        
        boardView.setBoard(softBoardData.linkState.getActiveBoard());
        
        setInputView( boardView );

        softBoardParser = null;

        initInput();
        }


    /**
     ** TEXT-PROCESSING PART
     **/


    public void initInput()
        {
        if ( softBoardData != null )
            {
            EditorInfo editorInfo = getCurrentInputEditorInfo();

            // position of the cursor
            calculatedPosition = editorInfo.initialSelStart;

            // surrounding text is changed
            storedText.invalidate();

            // pressed hard-keys are released
            softBoardData.boardStates.resetSimulatedMetaButtons();

            // enter's title is set
            softBoardData.setAction(editorInfo.imeOptions);

            if ( calculatedPosition == 0 && editorInfo.initialCapsMode != 0 )
                {
                ((CapsState) softBoardData.boardStates.metaStates[BoardStates.META_CAPS])
                        .setAutoCapsState( CapsState.AUTOCAPS_ON );
                getBoardView().invalidate();
                }
            }
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
     * This is the most important part:
     * Here we can control whether cursor/selection was changed without our knowledge.
     * Implemented send... methods should set calculatedPosition.
     * Stored text is verified only (string will be added) if new position matches calculatedPosition
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

        // undo is only enabled, if confirmPositionChange() validates it
        undoEnabled = false;

        // Text is NOT selected...
        if ( newSelStart == newSelEnd )
            {
            int positionChange = newSelStart-oldSelStart;

            // ...and cursor position is on the calculated position
            if ( newSelStart == calculatedPosition )
                {
                Scribe.debug( Debug.TEXT, "Calculated position is correct. Cursor position: " + newSelStart +
                        ", relative: " + positionChange );

                undoEnabled = storedText.confirmPositionChange( positionChange );
                }
            else
                {
                calculatedPosition = newSelStart;

                // ...and cursor position is incorrect:
                // it was an external movement
                if ( positionChange < 0 )
                    {
                    Scribe.debug( Debug.TEXT,
                            "Cursor is moving backwards. PreText is deleted, postText is invalidated. Cursor position: " + newSelStart +
                                    ", relative: " + positionChange );
                    storedText.preTextDelete( -positionChange );
                    storedText.postTextInvalidate();
                    }
                else
                    {
                    Scribe.debug( Debug.TEXT,
                            "Cursor is moving forwards. StoredText is invalidated. Cursor position: " + newSelStart +
                                    ", relative: " + positionChange );
                    storedText.invalidate();
                    }
                }
            }

        // Text is selected
        else // newSelStart != newSelEnd
            {
            Scribe.debug( Debug.TEXT, "Text is selected, calculated position is invalidated!");
            storedText.invalidate(); //lastCharacter = TEXT_SELECTED;
            calculatedPosition = -1;
            }
        }


    /**
     * Text (string) can be sent ONLY through this method
     * String is sent to the editor (commitText)
     * String is booked in stored-text
     * (string will be added by onUpdateSelection after verifying position changes)
     * CalculatedPosition is calculated
     * UndoEnabled is set to false,
     * (it will be enabled after verifying position changes)
     * @param inputConnection input connection - CANNOT BE NULL!
     * @param string string to send - CANNOT BE NULL!
     */
    private void sendPreTextString(InputConnection inputConnection, String string)
        {
        Scribe.debug(Debug.TEXT, "String to send: [" + string + "], length: " + string.length());

        inputConnection.commitText(string, 1);
        storedText.bookPreTextString(string);
        calculatedPosition += string.length();
        undoEnabled = false;
        }


    /**
     * Text can be deleted before cursor ONLY through this method
     * Delete is sent to the editor
     * Delete is booked in stored-text
     * (stored text will be deleted by onUpdateSelection after verifying position changes)
     * CalculatedPosition is calculated
     * UndoEnabled is set to false - no undo is possible after delete
     * @param inputConnection input connection - CANNOT BE NULL!
     * @param length number of java characters to delete before cursor
     */
    private void sendPreTextDelete(InputConnection inputConnection, int length)
        {
        Scribe.debug(Debug.TEXT, "Chars to delete before cursor: " + length );

        inputConnection.deleteSurroundingText(length, 0);
        storedText.bookPreTextDelete(length);
        calculatedPosition -= length;
        undoEnabled = false;
        }

    private void sendPreTextDeleteThanString(InputConnection inputConnection, int length, String string)
        {
        Scribe.debug(Debug.TEXT, "Chars to delete before cursor: " + length +
                ", than string to send: [" + string + "], length: " + string.length());

        // inputConnection.beginBatchEdit();
        inputConnection.deleteSurroundingText(length, 0);
        inputConnection.commitText(string, 1);
        // inputConnection.endBatchEdit();

        calculatedPosition -= length;
        calculatedPosition += string.length();

        storedText.bookPreTextDeleteThanString( length, string );
        undoEnabled = false;
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

        return ic.sendKeyEvent( new KeyEvent(
                downTime,               // this key originally went down
                eventTime,              // this event happened (downTime in ACTION_DOWN)
                keyEventAction,         // ACTION_DOWN or ACTION_UP
                keyEventCode,           // android keyCode
                0,                      // repeat is not implemented
                softBoardData.boardStates.getAndroidMetaState(),
                // meta-state in android format
                KeyCharacterMap.VIRTUAL_KEYBOARD,
                // device id FIX
                keyEventCode,           // android keyCode as scan-code
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE,
                InputDevice.SOURCE_TOUCHSCREEN ) );
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

        return sendKeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyEventCode);
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


    @Override
    public void sendString( String string, int autoSpace )
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            if ( (autoSpace & PacketText.ERASE_SPACES_BEFORE) != 0 )
                {
                deleteSpacesBeforeCursor();
                }
            if ( (autoSpace & PacketText.ERASE_SPACES_AFTER) != 0 )
                {
                deleteSpacesAfterCursor();
                }

            sendBuilder.setLength(0);
            if ( (autoSpace & PacketText.AUTO_SPACE_BEFORE) != 0 )
                {
                storedText.preTextReaderReset();
                if ( !isWhiteSpace( storedText.preTextRead()) )
                    sendBuilder.append(' ');
                }

            sendBuilder.append(string);

            if ( (autoSpace & PacketText.AUTO_SPACE_AFTER) != 0 )
                {
                storedText.postTextReaderReset();
                if ( !isSpace( storedText.postTextRead()) )
                    sendBuilder.append(' ');
                }

            String sendString = sendBuilder.toString();

            sendPreTextString(ic, sendString);
            }
        }


    public void changeStringBeforeCursor( String string )
        {
        changeStringBeforeCursor( string.length(), string );
        }


    public void changeStringBeforeCursor( int length, String string )
        {
        Scribe.locus( Debug.SERVICE );

        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            sendPreTextDeleteThanString(ic, length, string);
            }
        }


    public int deleteSpacesBeforeCursor()
        {
        int space;

        storedText.preTextReaderReset();
        for ( space = 0; storedText.preTextRead() == ' '; space++ ) ;

        deleteTextBeforeCursor(space);

        Scribe.debug( Debug.SERVICE, "Spaces deleted before cursor: " + space);
        return space;
        }

    public int deleteSpacesAfterCursor()
        {
        int space;

        storedText.postTextReaderReset();
        for ( space = 0; storedText.postTextRead() == ' '; space++ ) ;

        deleteTextAfterCursor(space);

        Scribe.debug( Debug.SERVICE,  "Spaces deleted after cursor: " + space );
        return space;
        }

    public void deleteTextBeforeCursor( int n )
        {
        Scribe.locus( Debug.SERVICE );

        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            sendPreTextDelete(ic, n);
            }
        }

    public void deleteTextAfterCursor( int n )
        {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            storedText.postTextDelete(n);
            ic.deleteSurroundingText(0, n);
            }
        }


    /**
     * Gets text before cursor - needed only by StoredText
     * Text can be retrieved only, if text is NOT selected
     * @param n number of java chars to get
     * @return text or null, if no text is available (or text is selected)
     */
    public CharSequence getTextBeforeCursor( int n )
        {
        if ( calculatedPosition >= 0 )
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
        if ( calculatedPosition >= 0 )
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


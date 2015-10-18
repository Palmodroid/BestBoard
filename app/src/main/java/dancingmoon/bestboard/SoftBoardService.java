package dancingmoon.bestboard;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.os.SystemClock;
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


public class SoftBoardService extends InputMethodService implements
        SoftBoardParserListener,
        SoftBoardListener,
        StoredText.Connection
    {
    /** Working directory */
    public static final String WORKING_DIRECTORY = "_bestboard";

    /** Name of coat descriptor file in working directory */
    public static String coatFileName = "coat.txt"; // .descriptor

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

    /** Text around the cursor is stored in storedText. Text is provided by IMS */
    private StoredText storedText = new StoredText( this );

    /**
     * cursor position calculated by our IMS. If this not corresponds with real position,
     * then somebody else changed it (and text around cursor is changed, too)!
     * In the case of selection the start of the selection is stored.
     */
    private int calculatedPosition = 0;

    /** Temporary variable to store string to be send */
    private StringBuilder sendBuilder = new StringBuilder();




    /**
     * This is the simplest InputMethodService implementation.
     * We just need a simple View. If user "clicks" on it then the InputMethodPicker will show up.
     */
    public View noKeyboardView()
        {
        Scribe.locus();

        View noKeyboardView = getLayoutInflater().inflate(R.layout.service_nokeyboard, null);
        noKeyboardView.setOnClickListener( new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                InputMethodManager imm = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
                imm.showInputMethodPicker();
/*
// Just for debuging: click will re-read coat descriptor file
Scribe.title("RE-START SOFT-BOARD-SERVICE");
if ( softBoardParser != null )
    softBoardParser.cancel(false);
File directory = new File( Environment.getExternalStorageDirectory(), WORKING_DIRECTORY );
File coat = new File( directory, coatFileName );
softBoardParser = new SoftBoardParser(SoftBoardService.this, coat );
softBoardParser.execute();
*/
                }
            } );

        // Warning comes from the onCreate method
        if ( warning != null )
            {
            Scribe.debug( "Warning text has changed: " + warning );
            TextView warningText = (TextView) noKeyboardView.findViewById( R.id.warning_text );
            warningText.setText( warning );
            }
        else
            {
            Scribe.debug( "Warning text is empty!");
            }

        return noKeyboardView;
        }
        
        
    @Override
    public View onCreateInputView()
        {
        Scribe.locus();

        if (softBoardData != null)
            {
            // setting index is not necessary
            softBoardData.linkState.setOrientation();

            // boardView = new BoardView( this );
            // -- OR --
            ViewGroup parent = (ViewGroup) boardView.getParent();
            if (parent != null) 
                {
                parent.removeView( boardView );
                }

            boardView.setBoard( softBoardData.linkState.getActiveBoard() );

            return boardView;
            }
        return noKeyboardView();
        }

    @Override
    public BoardView getBoardView()
        {
        return boardView;
        }

    @Override
    public void onCreate()
        {
        Debug.initScribe( this );
        Scribe.locus();

        super.onCreate();

        Scribe.title("START SOFT-BOARD-SERVICE");
        File directory = new File( Environment.getExternalStorageDirectory(), WORKING_DIRECTORY );
        File coat = new File( directory, coatFileName );
        softBoardParser = new SoftBoardParser(this, coat );
        softBoardParser.execute();
        }

    // !!!! THIS IS JUST DRAFT !!!!
    public void __restart__()
        {
        Scribe.title(" *** RESTART - JUST DRAFT *** ");
        File directory = new File( Environment.getExternalStorageDirectory(), WORKING_DIRECTORY );
        File coat = new File( directory, coatFileName );
        softBoardParser = new SoftBoardParser(this, coat );
        softBoardParser.execute();

        // Other parameters, orientation, setaction etc. ???????????????
        }

    @Override
    public void onDestroy()
        {
        Scribe.locus();

        super.onDestroy();

        if ( softBoardParser != null)
            {
            softBoardParser.cancel(false);
            }
        }

    /**
     * If denyFullScreen is true then fullscreen will be never allowed.
     * Otherwise system decides (fullscreen will be allowed in landscape, if editor allows it)
     */
    @Override
    public boolean onEvaluateFullscreenMode()
        {
        if ( denyFullScreen )
            return false;
        else
            return super.onEvaluateFullscreenMode();
        }


    @Override
    public SoftBoardListener getSoftBoardDataListener()
        {
        return this;
        }

    /**
     * Callback method is called after background task has finished without critical errors.
     * @param softBoardData newly generated class containing all softboard data
     * @param errorCount    number of non-critical errors (error messages can be found in the log)
     */
    @Override
    public void softBoardParserFinished(SoftBoardData softBoardData, int errorCount)
        {
        Scribe.locus();

        // non-critical errors
        if ( errorCount != 0 )
            {
            // Generating a new view
            warning = "Parsing of <" + coatFileName + "> has finished with " +
                    errorCount + " errors.\n" +
                    "Please, check log-file for details!";
            Scribe.debug( warning );
            // Warning should be shown!
            Toast.makeText( this, warning, Toast.LENGTH_LONG ).show();
            }
        // parsing finished without errors, but this is not the first parsing!
        else if ( this.softBoardData != null )
            {
            // Generating a new view
            warning = "Parsing of <" + coatFileName + "> has finished.";
            Scribe.debug( warning );
            // Warning should be shown!
            Toast.makeText( this, warning, Toast.LENGTH_SHORT ).show();
            }

        this.softBoardData = softBoardData;
        
        // Orientation should be checked, but index is 0 by default.
        // No setIndex() is needed
        softBoardData.linkState.setOrientation();

        boardView = new BoardView( this );        
        boardView.setBoard( softBoardData.linkState.getActiveBoard() );
        
        setInputView( boardView );

        softBoardParser = null;
        }

    /**
     * Callback method is called after critical error has stopped the background task.
     * @param errorInfo id of the critical error
     */
    @Override
    public void softBoardParserCriticalError(int errorInfo)
        {
        Scribe.locus();

        // Generating a new view
        warning = "Critical error happened!";
        Scribe.debug ( warning );
        setInputView( noKeyboardView() );

        softBoardParser = null;
        }


    /**
     ** TEXT-PROCESSING PART
     **/


    public StoredText getStoredText()
        {
        return storedText;
        }

    /**
     * lastCharacter and calculatedPositions are set based on EditorInfo data.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting)
        {
        super.onStartInput( attribute, restarting );
        Scribe.locus();

        if ( softBoardData != null )
            {
            softBoardData.boardStates.resetSimulatedMetaButtons();

            calculatedPosition = attribute.initialSelStart;

            // even in restarting text could be changed
            storedText.invalidate();

            softBoardData.setAction( attribute.imeOptions );
            }

        // Scribe.debug( " - position: " + calculatedPosition );
        }


    /**
     * This is the most important part:
     * Here we can control whether cursor/selection was changed without our knowledge.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd)
        {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        // Scribe.locus();

        // Text is NOT selected...
        if ( newSelStart == newSelEnd )
            {
            // ...and cursor position is correct
            if ( newSelEnd == calculatedPosition )
                {
                // if text was previously selected then last character is not known
                //if ( lastCharacter == TEXT_SELECTED )
                //    lastCharacter = UNKNOWN;

                // UNKNOWN or valid character values are CORRECT;
                }
            // ...and cursor position is incorrect
            else // if ( newSelStart != calculatedPosition )
                {
                // Text was modified without our knowledge
                // Correct inner variables!
                // Scribe.debug(" - Cursor position was changed, last character is not known!");
                storedText.invalidate(); //lastCharacter = UNKNOWN;
                calculatedPosition = newSelStart;
                }
            }

        // Text is selected
        else // newSelStart != newSelEnd
            {
            // Scribe.debug(" - Text selected, last character N/A!");
            storedText.invalidate(); //lastCharacter = TEXT_SELECTED;
            calculatedPosition = newSelStart;
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
            Scribe.error( "Cannot get input connection!" );
            return false;
            }

        return ic.sendKeyEvent(new KeyEvent(
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
        Scribe.debug( keyEventCode + " hard button is down!" );

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
        Scribe.debug( keyEventCode + " hard button is up!" );

        return sendKeyEvent( downTime, eventTime, KeyEvent.ACTION_UP, keyEventCode );
        }


    /**
     * Simulates key-down/key-up sequence
     * Input-connection availability is not returned!
     * @param keyEventCode android keyCode
     */
    public void sendKeyDownUp( int keyEventCode )
        {
        Scribe.debug(keyEventCode + " hard button is down-up!");

        long downTime = SystemClock.uptimeMillis();
        if (sendKeyDown( downTime, keyEventCode ))
            sendKeyUp( downTime, SystemClock.uptimeMillis(), keyEventCode );
        // ACTION_UP will be sent only, if ACTION_DOWN was successfully sent
        }


    @Override
    public void sendString( String string, int autoSpace )
        {
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

            ic.commitText( sendString, 1 );
            storedText.preTextType( sendString );
            calculatedPosition += sendString.length();
            }
        }


    public void changeStringBeforeCursor( String string )
        {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();
            ic.deleteSurroundingText( string.length(), 0 );
            storedText.preTextDelete( string.length() );
            storedText.preTextType( string );
            ic.commitText( string, 1 );
            ic.endBatchEdit();
            }
        // calculated position will not change
        }


    public int deleteSpacesBeforeCursor()
        {
        int space;

        storedText.preTextReaderReset();
        for ( space = 0; storedText.preTextRead() == ' '; space++ ) ;

        deleteTextBeforeCursor( space );

        Scribe.debug("Spaces deleted before cursor: " + space);
        return space;
        }

    public int deleteSpacesAfterCursor()
        {
        int space;

        storedText.postTextReaderReset();
        for ( space = 0; storedText.postTextRead() == ' '; space++ ) ;

        deleteTextAfterCursor( space );

        Scribe.debug( "Spaces deleted after cursor: " + space );
        return space;
        }

    public void deleteTextBeforeCursor( int n )
        {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            storedText.preTextDelete( n );
            ic.deleteSurroundingText( n, 0 );

            calculatedPosition -= n;
            }
        }

    public void deleteTextAfterCursor( int n )
        {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            {
            storedText.postTextDelete( n );
            ic.deleteSurroundingText( 0, n );
            }
        }

    public CharSequence getTextBeforeCursor( int n )
        {
        InputConnection ic = getCurrentInputConnection();
        return ic != null ? ic.getTextBeforeCursor( n, 0 ) : null;
        }

    public CharSequence getTextAfterCursor( int n )
        {
        InputConnection ic = getCurrentInputConnection();
        return ic != null ? ic.getTextAfterCursor( n, 0 ) : null;
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
        //Scribe.locus();
        //Scribe.note(" - keyCode: " + keyCode);
        // TODO Auto-generated method stub
        return super.onKeyDown(keyCode, event);
        }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
        {
        //Scribe.locus();
        //Scribe.note(" - keyCode: " + keyCode);
        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
        }

    }


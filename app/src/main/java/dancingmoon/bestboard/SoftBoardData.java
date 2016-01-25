package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;
import android.view.inputmethod.EditorInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dancingmoon.bestboard.buttons.TitleDescriptor;
import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.modify.Modify;
import dancingmoon.bestboard.prefs.PrefsFragment;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.server.TextBeforeCursor;
import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.LinkState;


public class SoftBoardData
    {
    /**
     ** VARIABLES NEEDED BY THE SOFTBOARD
     ** (Variables are initialized with default values)
     **/

    /**
     * The firstly defined non-wide board
     * It is the default board if no other board is set
     */
    public Board firstBoard = null;

    /** Softboard's name */
    public String name = "";

    /** Softboard's version */
    public int version = 1;

    /** Softboard's author */
    public String author = "";

    /** Softboard's tags */
    public List<String> tags = new ArrayList<>();

    /** Softboard's short description */
    public String description = "";

    /** File name of softboard's document (should be in the same directory) - if available */
    public File docFile = null;

    /** Full URI of softboard's document - if available */
    public String docUri = "";

    /** Locale */
    public Locale locale = Locale.getDefault(); // or Locale.US - which is always available

    /** Color of pressed meta-keys */
    public int metaColor = Color.CYAN;

    /** Color of locked meta-keys */
    public int lockColor = Color.BLUE;

    /** Color of autocaps */
    public int autoColor = Color.MAGENTA;

    /** Color of touched keys */
    public int touchColor = Color.RED;

    /** Color of stroke */
    public int strokeColor = Color.MAGENTA & 0x77FFFFFF;


    /**
     ** PREFERENCES - stored in softBoardData, because these variables affect all boards
     ** Preferences are read by readPreferences() at start and at change, because:
     ** - some of them needed frequently
     ** - numeric preferences are stored as string
     **/
    public void readPreferences()
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( softBoardListener.getApplicationContext() );

        hideTop = ( sharedPrefs.getBoolean( softBoardListener.getApplicationContext().getString( R.string.drawing_hide_upper_key ), false)) ? 1 : 0;

        hideBottom = ( sharedPrefs.getBoolean( softBoardListener.getApplicationContext().getString( R.string.drawing_hide_lower_key ), false)) ? 1 : 0;

        heightRatioPermil = sharedPrefs.getInt( PrefsFragment.DRAWING_HEIGHT_RATIO_INT_KEY, 0);

        landscapeOffsetPermil = sharedPrefs.getInt( PrefsFragment.DRAWING_LANDSCAPE_OFFSET_INT_KEY, 0);

        outerRimPermil = sharedPrefs.getInt( PrefsFragment.DRAWING_OUTER_RIM_INT_KEY, 0 );

        monitorRow = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.drawing_monitor_row_key),
                false);

        longBowCount = sharedPrefs.getInt(PrefsFragment.TOUCH_LONG_COUNT_INT_KEY, 0);

        pressBowCount = sharedPrefs.getInt( PrefsFragment.TOUCH_PRESS_COUNT_INT_KEY, 0 );

        pressBowThreshold = (float)sharedPrefs.getInt( PrefsFragment.TOUCH_PRESS_THRESHOLD_INT_KEY, 0 ) / 1000f;

        stayBowTime = sharedPrefs.getInt( PrefsFragment.TOUCH_STAY_TIME_INT_KEY, 0 ); // * 1000000;

        repeatTime = sharedPrefs.getInt( PrefsFragment.TOUCH_REPEAT_TIME_INT_KEY, 0 ); //  * 1000000;

        displayTouch = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.cursor_touch_allow_key),
                true);

        displayStroke = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString( R.string.cursor_stroke_allow_key ),
                true);

        textSessionSetsMetastates = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString( R.string.editing_text_session_key ),
                true);
        }


    /**
     * Hide grids from the top of the board - VALUE IS NOT VERIFIED!
     * 0 - no hide
     * 1 - hide one quarter (one grid) from the top row
     * 2 - hide one half (two grids) from the top row
     */
    public int hideTop = 0;

    /**
     * Hide grids from the bottom of the board - VALUE IS NOT VERIFIED!
     * 0 - no hide
     * 1 - hide one quarter (one grid) from the bottom row
     * 2 - hide one half (two grids) from the bottom row
     */
    public int hideBottom = 0;

    /**
     * Maximal screen height ratio which can be occupied by the board
     */
    public int heightRatioPermil;

    /**
     * Offset for non-wide boards in landscape mode
     * (percent of the free area) - VALUE IS NOT VERIFIED!
     */
    public int landscapeOffsetPermil;

    /**
     * Size of the outer rim on buttons.
     * Touch movement (stroke) will not fire from the outer rim, but touch down will do.
     */
    public int outerRimPermil;

    /**
     * Switches monitor row at the bottom
     */
    public boolean monitorRow;

    /**
     * Length of CIRCLE to start secondary function
     */
    public int longBowCount;

    /**
     * Number of HARD PRESSES to start secondary function
     */
    public int pressBowCount;

    /**
     * Threshold for HARD PRESS - 1000 = 1.0f
     */
    public float pressBowThreshold;

    /**
     * Time of STAY to start secondary function - millisec
     */
    public int stayBowTime;

    /**
     * Time to repeat (repeat rate) - millisec
     */
    public int repeatTime;

    /**
     * Background of the touched key is changed or not
     */
    public boolean displayTouch = true;

    /**
     * Stroke is displayed or not
     */
    public boolean displayStroke = true;

    /**
     * New text session behaves as a key stroke, and sets meta states accordingly (or not)
     */
    public boolean textSessionSetsMetastates = true;


    /**
     ** STATES NEEDED BY THE SOFTBOARD
     **/

    public BoardStates boardStates;

    // defined in constructor, because SoftBoardDataListener is needed
    public LinkState linkState;

    /**
     * Action of the enter key defined by imeOptions of onStartInput's EditorInfo
     * Available values are defined by the next constants
     */
    public int action = 0;

    public static final int ACTION_UNSPECIFIED = 0;
    public static final int ACTION_NONE = 1;
    public static final int ACTION_GO = 2;
    public static final int ACTION_SEARCH = 3;
    public static final int ACTION_SEND = 4;
    public static final int ACTION_NEXT = 5;
    public static final int ACTION_DONE = 6;
    public static final int ACTION_PREVIOUS = 7;
    public static final int ACTION_MULTILINE = 8;


    /** Action-titles displayed by the enter-key */
    // Number of actionTitles should be ACTION_TITLES!
    public String[] actionTitles = {
            "???",
            "---",
            "GO",
            "SRCH",
            "SEND",
            "NEXT",
            "DONE",
            "PREV",
            "CR" };


    /**
     * DATA NEEDED BY MODIFY
     */

    public LongSparseArray<Modify> modify = new LongSparseArray<>();

    /**
     * INTERFACE - CONNECTION FOR SENDING KEYS
     */

    public interface SoftBoardListener
        {
        /**
         * Get application context.
         * This method is defined by both Service and Activity.
         * @return application context
         */
        Context getApplicationContext();
        // THIS IS NOT NEEDED (MAYBE) IF BOARD and BOARDVIEW IS DIVIDED
        // UseState.checkOrientation() needs context
        // readPreferences() need context

        boolean sendKeyDown( long downTime, int keyEventCode );
        boolean sendKeyUp( long downTime, long eventTime, int keyEventCode );
        void sendKeyDownUp( int keyEventCode );

        void sendString( String string, int autoSpace );
        // UseState needs this to change board
        public boolean undoLastString();

        BoardView getBoardView();

        TextBeforeCursor getTextBeforeCursor();

        void checkAtBowStart();
        void checkAtStrokeEnd();

        void deleteCharBeforeCursor(int n);
        void deleteCharAfterCursor(int n);

        int deleteSpacesBeforeCursor();
        void changeStringBeforeCursor( String string );
        void changeStringBeforeCursor( int length, String string );

        boolean sendDefaultEditorAction(boolean fromEnterKey);

        // JUST A DRAFT !! DO NOT USE IT !!
        public void startSoftBoardParser();
        }

    public SoftBoardListener softBoardListener;


    /**
     ** STARTING (CONSTRUCTOR) AND ENDING OF PARSING PHASE
     **/

    /**
     * Constructor
     * Definies default TitleSlots
     * @param softBoardListener to connect with service
     */
    public SoftBoardData( SoftBoardListener softBoardListener )
        {
        this.softBoardListener = softBoardListener;

        // static variables should be deleted!!
        TitleDescriptor.setTypeface( null );

        
        boardStates = new BoardStates( softBoardListener );

        linkState = new LinkState( softBoardListener );

        // This could go into parsingFinished()
        readPreferences();
        }


   /**
     ** SETTERS AND GETTERS CALLED DURING RUNTIME
     **/


    /**
     * Set action of the enter key defined by imeOptions.
     * !! Could be changed to a direct equation?
     * @param imeOptions provided by EditorInfo of onStartInput
     */
    public void setAction( int imeOptions )
        {
        if ( (imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0)
            {
            // !! Just for checking input fields - it should be NONE ??
            action = ACTION_MULTILINE;
            Scribe.debug( Debug.DATA, "Ime action: MULTILINE because of NO ENTER ACTION flag." );
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_NONE) != 0)
            {
            action = ACTION_NONE;
            Scribe.debug( Debug.DATA, "Ime action: NONE.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_GO) != 0)
            {
            action = ACTION_GO;
            Scribe.debug( Debug.DATA, "Ime action: GO.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_SEARCH) != 0)
            {
            action = ACTION_SEARCH;
            Scribe.debug( Debug.DATA, "Ime action: SEARCH.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_SEND) != 0)
            {
            action = ACTION_SEND;
            Scribe.debug( Debug.DATA, "Ime action: SEND.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_NEXT) != 0)
            {
            action = ACTION_NEXT;
            Scribe.debug( Debug.DATA, "Ime action: NEXT.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_DONE) != 0)
            {
            action = ACTION_DONE;
            Scribe.debug( Debug.DATA, "Ime action: DONE.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_PREVIOUS) != 0)
            {
            action = ACTION_PREVIOUS;
            Scribe.debug( Debug.DATA, "Ime action: PREVIOUS.");
            }
        else // EditorInfo.IME_ACTION_UNSPECIFIED
            {
            // !! Just for checking input fields - it should be NONE ??
            action = ACTION_UNSPECIFIED;
            Scribe.debug( Debug.DATA, "Ime action: UNSPECIFIED, because action is not known.");
            }
        }


    public String getActionTitle()
        {
        return actionTitles[ action ];
        }


    public boolean isActionSupplied()
        {
        return ( action >= ACTION_GO && action <= ACTION_PREVIOUS );
        }



    }

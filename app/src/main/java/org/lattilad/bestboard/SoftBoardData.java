package org.lattilad.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;
import android.view.inputmethod.EditorInfo;

import org.lattilad.bestboard.buttons.TitleDescriptor;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.modify.Modify;
import org.lattilad.bestboard.prefs.PrefsFragment;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.server.TextBeforeCursor;
import org.lattilad.bestboard.states.BoardTable;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.utils.TimeCounter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SoftBoardData
    {
    /**
     ** VARIABLES NEEDED BY THE SOFTBOARD
     ** (Variables are initialized with default values)
     **/

    /**
     * The firstly defined non-wide layout
     * It is the default layout if no other layout is set
     */
    public Layout firstLayout = null;

    /**
     * Softboard's name
     */
    public String name = "";

    /**
     * Softboard's version
     */
    public int version = 1;

    /**
     * Softboard's author
     */
    public String author = "";

    /**
     * Softboard's tags
     */
    public List<String> tags = new ArrayList<>();

    /**
     * Softboard's short description
     */
    public String description = "";

    /**
     * File name of softboard's document (should be in the same directory) - if available
     */
    public File docFile = null;

    /**
     * Full URI of softboard's document - if available
     */
    public String docUri = "";

    /**
     * Locale
     */
    public Locale locale = Locale.getDefault(); // or Locale.US - which is always available

    /**
     * Color of pressed meta-keys
     */
    public int metaColor = Color.CYAN;

    /**
     * Color of locked meta-keys
     */
    public int lockColor = Color.BLUE;

    /**
     * Color of autocaps
     */
    public int autoColor = Color.MAGENTA;

    /**
     * Color of touched keys
     */
    public int touchColor = Color.RED;

    /**
     * Color of stroke
     */
    public int strokeColor = Color.MAGENTA & 0x77FFFFFF;


    /**
     * * PREFERENCES - stored in softBoardData, because these variables affect all boards
     * * Preferences are read by readPreferences() at start and at change, because:
     * * - some of them needed frequently
     * * - numeric preferences are stored as string
     **/
    public void readPreferences()
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(softBoardListener.getApplicationContext());

        hideTop = (sharedPrefs.getBoolean(softBoardListener.getApplicationContext().getString(R.string.drawing_hide_upper_key), false)) ? 1 : 0;

        hideBottom = (sharedPrefs.getBoolean(softBoardListener.getApplicationContext().getString(R.string.drawing_hide_lower_key), false)) ? 1 : 0;

        heightRatioPermil = sharedPrefs.getInt(PrefsFragment.DRAWING_HEIGHT_RATIO_INT_KEY, 0);

        landscapeOffsetPermil = sharedPrefs.getInt(PrefsFragment.DRAWING_LANDSCAPE_OFFSET_INT_KEY, 0);

        outerRimPermil = sharedPrefs.getInt(PrefsFragment.DRAWING_OUTER_RIM_INT_KEY, 0);

        monitorRow = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.drawing_monitor_row_key),
                false);

        // limit is not stored, it is set immediately
        int limit = sharedPrefs.getInt(PrefsFragment.DRAWING_SPEDOMETER_LIMIT_INT_KEY, 3000);
        characterCounter.setPeriodLimit( limit );
        buttonCounter.setPeriodLimit( limit );

        longBowCount = sharedPrefs.getInt(PrefsFragment.TOUCH_LONG_COUNT_INT_KEY, 0);

        pressBowCount = sharedPrefs.getInt(PrefsFragment.TOUCH_PRESS_COUNT_INT_KEY, 0);

        pressBowThreshold = (float) sharedPrefs.getInt(PrefsFragment.TOUCH_PRESS_THRESHOLD_INT_KEY, 0) / 1000f;

        stayBowTime = sharedPrefs.getInt(PrefsFragment.TOUCH_STAY_TIME_INT_KEY, 0); // * 1000000;

        repeatTime = sharedPrefs.getInt(PrefsFragment.TOUCH_REPEAT_TIME_INT_KEY, 0); //  * 1000000;

        displayTouch = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.cursor_touch_allow_key),
                true);

        displayStroke = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.cursor_stroke_allow_key),
                true);

        textSessionSetsMetastates = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.editing_text_session_key),
                true);
        }


    /**
     * Hide grids from the top of the layout - VALUE IS NOT VERIFIED!
     * 0 - no hide
     * 1 - hide one quarter (one grid) from the top row
     * 2 - hide one half (two grids) from the top row
     */
    public int hideTop = 0;

    /**
     * Hide grids from the bottom of the layout - VALUE IS NOT VERIFIED!
     * 0 - no hide
     * 1 - hide one quarter (one grid) from the bottom row
     * 2 - hide one half (two grids) from the bottom row
     */
    public int hideBottom = 0;

    /**
     * Maximal screen height ratio which can be occupied by the layout
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
     * * STATES NEEDED BY THE SOFTBOARD
     **/

    public LayoutStates layoutStates;

    // defined in constructor, because SoftBoardDataListener is needed
    public BoardTable boardTable;

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


    /**
     * Action-titles displayed by the enter-key
     */
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
            "CR"};

    /**
     * Text of the monitor row
     */
    private String monitorString = "MONITOR";

    /**
     * Counters of sent characters and buttons
     */
    public TimeCounter characterCounter = new TimeCounter();
    public TimeCounter buttonCounter = new TimeCounter();

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
        // THIS IS NOT NEEDED (MAYBE) IF LAYOUT and BOARDVIEW IS DIVIDED
        // UseState.checkOrientation() needs context
        // readPreferences() need context
        // vibration needs context

        boolean sendKeyDown( long downTime, int keyEventCode );
        boolean sendKeyUp( long downTime, long eventTime, int keyEventCode );
        void sendKeyDownUp(int keyEventCode);

        void sendString( String string, int autoSpace );
        // UseState needs this to change layout
        public boolean undoLastString();

        LayoutView getLayoutView();

        TextBeforeCursor getTextBeforeCursor();

        void checkAtBowStart();
        void checkAtStrokeEnd();

        void deleteCharBeforeCursor(int n);
        void deleteCharAfterCursor(int n);

        int deleteSpacesBeforeCursor();
        void changeStringBeforeCursor( String string );
        void changeStringBeforeCursor( int length, String string );

        boolean sendDefaultEditorAction(boolean fromEnterKey);

        void startSoftBoardParser();
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
        TitleDescriptor.setTypeface(null);

        layoutStates = new LayoutStates( softBoardListener );

        boardTable = new BoardTable( softBoardListener );

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


    public void setMonitorString( String string )
        {
        monitorString = string;
        }


    public String getMonitorString()
        {
        return monitorString;
        }


    public void showTiming()
        {
        int characterVelocity = characterCounter.getVelocity();
        int buttonVelocity = buttonCounter.getVelocity();

        StringBuilder builder = new StringBuilder();
        if ( characterVelocity > 0 )
            {
            builder.append(characterVelocity).append(" c/m, ");
            }
        else
            {
            builder.append("- ");
            }

        if ( buttonVelocity > 0 )
            {
            builder.append(buttonVelocity).append(" b/m");
            }
        else
            {
            builder.append("-");
            }
        setMonitorString( builder.toString() );
        }
    }

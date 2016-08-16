package org.lattilad.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;
import android.view.inputmethod.EditorInfo;

import org.lattilad.bestboard.abbreviation.Abbrevs;
import org.lattilad.bestboard.buttons.TitleDescriptor;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.modify.Modify;
import org.lattilad.bestboard.prefs.PrefsFragment;
import org.lattilad.bestboard.scribe.Scribe;
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
        characterCounter.setPeriodLimit(limit);
        buttonCounter.setPeriodLimit(limit);

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

        vibrationAllowed = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.cursor_vibration_allow_key),
                true);

        vibrationFirst = sharedPrefs.getInt(PrefsFragment.CURSOR_VIBRATION_FIRST_INT_KEY, 0);

        vibrationSecond = sharedPrefs.getInt(PrefsFragment.CURSOR_VIBRATION_SECOND_INT_KEY, 0);

        vibrationRepeat = sharedPrefs.getInt(PrefsFragment.CURSOR_VIBRATION_REPEAT_INT_KEY, 0);

        textSessionSetsMetastates = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.editing_text_session_key),
                true);

        gridTitle = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString(R.string.debug_grid_title_key),
                false);

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
     * Vibration is allowed or not
     */
    public boolean vibrationAllowed = true;

    /**
     * Vibration length for primary events
     */
    public int vibrationFirst;

    /**
     * Vibration length for secondary events
     */
    public int vibrationSecond;

    /**
     * Vibration length for repeated events
     */
    public int vibrationRepeat;

    /**
     * New text session behaves as a key stroke, and sets meta states accordingly (or not)
     */
    public boolean textSessionSetsMetastates = true;

    /**
     * Draws debug grid onto the layout
     */
    public boolean gridTitle = true;


    /**
     * STATES NEEDED BY THE SOFTBOARD
     **/

    public LayoutStates layoutStates;

    public BoardTable boardTable;

    public SoftBoardShow softBoardShow;

    public Abbrevs abbreviations;


    /*
     * Whether auto functions are enabled
     * AUTOSPACE - check is needed only in
     *      SoftBoardProcessor.sendString( String string, int autoSpace )
     * AUTOCAPS - it should work as enabled, but without effects:
     *      PacketText.send()
     *      ButtonMeta.drawButtonChangingPart(Canvas canvas)
     *
     */
    public boolean autoFuncEnabled = true;

    /**
     * Action of the enter key defined by imeOptions of onStartInput's EditorInfo
     * Available values are defined by the next constants
     */
    public int enterAction = 0;

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
     * Text of the monitor row
     */
    private String monitorString = "MONITOR";

    /**
     * Counters of sent characters and buttons
     */
    public TimeCounter characterCounter = new TimeCounter();
    public TimeCounter buttonCounter = new TimeCounter();

    /**
     * Connection to the system vibrator
     */
    private Vibrator vibrator;

    public final static int VIBRATE_PRIMARY = 1;
    public final static int VIBRATE_SECONDARY = 2;
    public final static int VIBRATE_REPETED = 3;

    /**
     * DATA NEEDED BY MODIFY
     */

    public LongSparseArray<Modify> modify = new LongSparseArray<>();

    /**
     * INTERFACE - CONNECTION FOR SENDING KEYS
     */


    public SoftBoardListener softBoardListener;


    /**
     ** STARTING (CONSTRUCTOR) AND ENDING OF PARSING PHASE
     **/

    /**
     * Constructor is called BEFORE parsing
     * Connect() is called AFTER parsing, when SoftBoardProcessor starts
     */
    public SoftBoardData( )
        {
        // static variables should be deleted!!
        TitleDescriptor.setTypeface(null);

        layoutStates = new LayoutStates();
        boardTable = new BoardTable();
        softBoardShow = new SoftBoardShow( this );
        abbreviations = new Abbrevs();
        }

    /**
     * SoftBoardParser finishes data population
     * Defines default TitleSlots
     * @param softBoardListener to connect with service
     */
    public void connect(SoftBoardListener softBoardListener)
        {
        this.softBoardListener = softBoardListener;

        layoutStates.connect( softBoardListener );

        boardTable.connect( softBoardListener );

        // Get instance of Vibrator from current Context
        vibrator = (Vibrator) softBoardListener.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

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
    public void setEnterAction(int imeOptions)
        {
        if ( (imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0)
            {
            // !! Just for checking input fields - it should be NONE ??
            enterAction = ACTION_MULTILINE;
            Scribe.debug( Debug.DATA, "Ime action: MULTILINE because of NO ENTER ACTION flag." );
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_NONE) != 0)
            {
            enterAction = ACTION_NONE;
            Scribe.debug( Debug.DATA, "Ime action: NONE.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_GO) != 0)
            {
            enterAction = ACTION_GO;
            Scribe.debug( Debug.DATA, "Ime action: GO.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_SEARCH) != 0)
            {
            enterAction = ACTION_SEARCH;
            Scribe.debug( Debug.DATA, "Ime action: SEARCH.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_SEND) != 0)
            {
            enterAction = ACTION_SEND;
            Scribe.debug( Debug.DATA, "Ime action: SEND.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_NEXT) != 0)
            {
            enterAction = ACTION_NEXT;
            Scribe.debug( Debug.DATA, "Ime action: NEXT.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_DONE) != 0)
            {
            enterAction = ACTION_DONE;
            Scribe.debug( Debug.DATA, "Ime action: DONE.");
            }
        else if ( (imeOptions & EditorInfo.IME_ACTION_PREVIOUS) != 0)
            {
            enterAction = ACTION_PREVIOUS;
            Scribe.debug( Debug.DATA, "Ime action: PREVIOUS.");
            }
        else // EditorInfo.IME_ACTION_UNSPECIFIED
            {
            // !! Just for checking input fields - it should be NONE ??
            enterAction = ACTION_UNSPECIFIED;
            Scribe.debug( Debug.DATA, "Ime action: UNSPECIFIED, because action is not known.");
            }
        }


    public boolean isActionSupplied()
        {
        return ( enterAction >= ACTION_GO && enterAction <= ACTION_PREVIOUS );
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


    public void vibrate( int type )
        {
        if ( vibrationAllowed )
            {
            int length;

            switch (type)
                {
                case VIBRATE_PRIMARY:
                    length = vibrationFirst;
                    break;

                case VIBRATE_SECONDARY:
                    length = vibrationSecond;
                    break;

                case VIBRATE_REPETED:
                    length = vibrationRepeat;
                    break;

                default:
                    return;
                }

            vibrator.vibrate((long) length);
            }

        }

    }

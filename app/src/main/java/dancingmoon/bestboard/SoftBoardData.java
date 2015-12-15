package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dancingmoon.bestboard.buttons.Button;
import dancingmoon.bestboard.buttons.ButtonDouble;
import dancingmoon.bestboard.buttons.ButtonEnter;
import dancingmoon.bestboard.buttons.ButtonLink;
import dancingmoon.bestboard.buttons.ButtonMeta;
import dancingmoon.bestboard.buttons.ButtonModify;
import dancingmoon.bestboard.buttons.ButtonPacket;
import dancingmoon.bestboard.buttons.ButtonSpaceTravel;
import dancingmoon.bestboard.buttons.Packet;
import dancingmoon.bestboard.buttons.PacketFunction;
import dancingmoon.bestboard.buttons.PacketKey;
import dancingmoon.bestboard.buttons.PacketText;
import dancingmoon.bestboard.buttons.TitleDescriptor;
import dancingmoon.bestboard.modify.Modify;
import dancingmoon.bestboard.modify.ModifyChar;
import dancingmoon.bestboard.modify.ModifyText;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.server.TextBeforeCursor;
import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.CapsState;
import dancingmoon.bestboard.states.LinkState;
import dancingmoon.bestboard.utils.ExtendedMap;
import dancingmoon.bestboard.utils.ExternalDataException;
import dancingmoon.bestboard.utils.SinglyLinkedList;
import dancingmoon.bestboard.utils.Trilean;


public class SoftBoardData
    {
    /**
     ** TEMPORARY CLASSES NEEDED ONLY BY THE PARSING PHASE
     **/

    /**
     * Temporary data for creating boards
     * Button position is stored for every Board independently.
     * After finishing, these supplementary data is not needed any more,
     * board itself will be part of the use-key's data.
     * Data can be reached directly within SoftBoardData
     */
    private class BoardPlan
        {
        private Board board;

        private int cursorRow = 0;
        private int cursorColumn = 0;
        private int cursorDefaultColumn = 0;

        private boolean transform = false;

        private BoardPlan(Board board)
            {
            this.board = board;
            }

        // toString is needed only for debugging
        public String toString()
            {
            StringBuilder result = new StringBuilder();
            result.append("C:").append(cursorColumn);
            result.append("/R:").append(cursorRow);
            return result.toString();
            }

        public int getTransformedCursorColumn()
            {
            // rowsAlignOffset:
            // 1 if first (0) row starts with WHOLE button on the left
            // 0 if first (0) row starts with HALF button on the left
            //              0 if EVEN is half
            // cursorRow % 2:
            // 1 in 1, 3, 5...; 0 in 0, 2, 4...
            //              0 if row is EVEN
            if ( transform && (cursorRow % 2 == board.rowsAlignOffset) )
                return cursorColumn + 1;
            else
                return cursorColumn;
            }
        }

    /**
     * Temporary data for title-slots
     * Title-slots describe the position and size of a title on the buttons.
     * Data can be reached directly within SoftBoardData
     * DefaultTitleSlot (0L initially) will be used instead of missing data.
     */
    private class Slot
        {
        private int xOffset;
        private int yOffset;
        private int size;
        private boolean bold;
        private boolean italics;
        private int color;

        private Slot( int xOffset,
                      int yOffset,
                      int size,
                      boolean bold,
                      boolean italics,
                      int color )
            {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.size = size;
            this.bold = bold;
            this.italics = italics;
            this.color = color;
            }

        // toString is needed only for debugging
        public String toString()
            {
            StringBuilder result = new StringBuilder();
            result.append("XO:").append(xOffset);
            result.append("/YO:").append(yOffset);
            result.append("/S:").append(size);
            result.append("/C:").append(Integer.toHexString(color));
            if (bold)
                result.append("/B");
            if (italics)
                result.append("/I");
            return result.toString();
            }
        }

    /**
     ** TEMPORARY VARIABLES NEEDED ONLY BY THE PARSING PHASE
     ** (?? Put them in a separate inner class, which can be freed later ??)
     **/

    /**
     * Tokenizer is needed for messaging during data-load.
     * It should be cleared, after data-load is ready.
     */
    private Tokenizer tokenizer;

    /** Board's default background */
    private int defaultBoardColor = Color.LTGRAY;

    /** Button's default background */
    private int defaultButtonColor = Color.LTGRAY;

    /** Title's default position */
    private long defaultSlot = 0L;

    /** Map of temporary boardPlans, identified by code of keywords */
    private Map<Long, BoardPlan> boardPlans = new HashMap<>();

    /** List of active (included) boardPlans */
    private Set<BoardPlan> includedBoardPlans = new HashSet<>();

    /** Map of temporary Slots, identified by code of keywords */
    private Map<Long, Slot> Slots = new HashMap<>();


    /**
     ** VARIABLES NEEDED BY THE SOFTBOARD
     ** (Variables are initialized with default values)
     **/

    /**
     * The firstly defined non-wide board
     * It is the default board if no other board is set
     */
    private Board firstBoard = null;

    /** Softboard's name */
    private String name = "";

    /** Softboard's version */
    private int version = 1;

    /** Softboard's author */
    private String author = "";

    /** Softboard's tags */
    private List<String> tags = new ArrayList<>();

    /** Softboard's short description */
    private String description = "";

    /** File name of softboard's document (should be in the same directory) - if available */
    private File docFile = null;

    /** Full URI of softboard's document - if available */
    private String docUri = "";

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
                softBoardListener.getApplicationContext().getString( R.string.drawing_monitor_row_key ),
                false );

        longBowCount = sharedPrefs.getInt( PrefsFragment.TOUCH_LONG_COUNT_INT_KEY, 0 );

        pressBowCount = sharedPrefs.getInt( PrefsFragment.TOUCH_PRESS_COUNT_INT_KEY, 0 );

        pressBowThreshold = (float)sharedPrefs.getInt( PrefsFragment.TOUCH_PRESS_THRESHOLD_INT_KEY, 0 ) / 1000f;

        stayBowTime = sharedPrefs.getInt( PrefsFragment.TOUCH_STAY_TIME_INT_KEY, 0 ) * 1000000;

        repeatTime = sharedPrefs.getInt( PrefsFragment.TOUCH_REPEAT_TIME_INT_KEY, 0 ) * 1000000;



        displayTouch = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString( R.string.cursor_touch_allow_key ),
                true);

        displayStroke = sharedPrefs.getBoolean(
                softBoardListener.getApplicationContext().getString( R.string.cursor_stroke_allow_key ),
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
     * Time of STAY to start secondary function - nanosec
     */
    public int stayBowTime;

    /**
     * Time to repeat (repeat rate) - nanosec
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
     * @param tokenizer to use for messages
     * @param softBoardListener to connect with service
     */
    public SoftBoardData( SoftBoardListener softBoardListener, Tokenizer tokenizer )
        {
        this.softBoardListener = softBoardListener;
        this.tokenizer = tokenizer;

        // static variables should be deleted!!
        TitleDescriptor.setTypeface( null );

        
        boardStates = new BoardStates( softBoardListener );

        linkState = new LinkState( softBoardListener );

        Slots.put(
                defaultSlot,
                new Slot( 0, 250, 1200, false, false, Color.BLACK ) );

        // This could go into parsingFinished()
        readPreferences();
        }

    /**
     * Tokenizer is not needed after the parsing process
     */
    public void parsingFinished() throws ExternalDataException
        {
        if ( firstBoard == null )
            {
            throw new ExternalDataException("No board!");
            }

        if ( linkState.isFirstBoardMissing() )
            {
            linkState.setLinkBoardTable( 0, firstBoard );
            tokenizer.error( R.string.data_primary_board_missing );
            }

        tokenizer = null;
        }


    /**
     ** SETTERS AND GETTERS CALLED DURING RUNTIME
     **/


    /**
     * Set action of the enter key defined by imeOptions.
     * !! Could be changed to a direct equalation?
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
    /**
     ** SETTERS CALLED ONLY BY PARSING PHASE
     **/

    /** Set softboard's name */
    public void setName( Object stringParameter )
        {
        this.name = (String) stringParameter;
        tokenizer.note(R.string.data_name, this.name );
        }

    /** Set softboard's version */
    public void setVersion( Object intParameter )
        {
        this.version = (int)intParameter;
        tokenizer.note(R.string.data_version, String.valueOf(this.version));
        }

    /** Set softboard's author */
    public void setAuthor( Object stringParameter )
        {
        this.author = (String)stringParameter;
        tokenizer.note(R.string.data_author, this.author );
        }

    /** Add softboard's tags */
    public void addTags( List<Object> stringListParameter )
        {
        // PARAMETER_STRING_LIST gives only non-null String items
        for (Object item: stringListParameter)
            {
            tags.add( (String) item );
            tokenizer.note( R.string.data_tags, (String) item );
            }
        }

    /** Set softboard's short description */
    public void setDescription( Object stringParameter )
        {
        this.description = (String)stringParameter;
        tokenizer.note(R.string.data_description, this.description );
        }

    /**
     * Set file name of softboard's document (should be in the same directory) - if available
     * DocFile is not checked, just stored !!
     */
    public void setDocFile( Object fileParameter )
        {
        this.docFile = (File)fileParameter;
        tokenizer.note(R.string.data_docfile, this.docFile.toString() );
        }

    /**
     * Set full URI of softboard's document - if available
     * DocUri is not checked, just stored !!
     */
    public void setDocUri( Object stringParameter )
        {
        this.docUri = (String)stringParameter;
        tokenizer.note(R.string.data_docuri, this.docUri );
        }

    /**
     * Set softboard's locale
     * Locale is not checked, just set !!
     */
    public void setLocale( ExtendedMap<Long, Object> parameters )
        {
        String language = (String)parameters.get( Commands.TOKEN_LANGUAGE, "" );
        String country = (String)parameters.get( Commands.TOKEN_COUNTRY, "" );
        String variant = (String)parameters.get( Commands.TOKEN_VARIANT, "" );

        locale = new Locale( language, country, variant);
        tokenizer.note(R.string.data_locale, String.valueOf(locale) );
        }

    /**
     * Set default values
     */
    public void setDefault( ExtendedMap<Long, Object> parameters )
        {
        Object temp;

        temp = parameters.get( Commands.TOKEN_BOARDCOLOR );
        if (temp != null)
            {
            defaultBoardColor = (int)temp;
            tokenizer.note(R.string.data_default_boardcolor,
                    Integer.toHexString( defaultBoardColor ));
            }

        temp = parameters.get( Commands.TOKEN_BUTTONCOLOR );
        if (temp != null)
            {
            defaultButtonColor = (int)temp;
            tokenizer.note(R.string.data_default_buttoncolor,
                    Integer.toHexString( defaultButtonColor ));
            }

        temp = parameters.get( Commands.TOKEN_SLOT );
        if ( temp != null )
            {
            if ( Slots.containsKey( temp ))
                {
                defaultSlot = (long)temp;
                tokenizer.note(R.string.data_default_titleslot,
                        Tokenizer.regenerateKeyword( defaultSlot ));
                }
            else
                tokenizer.error(R.string.data_titleslot_invalid,
                        Tokenizer.regenerateKeyword((long)temp));
            }
        }

    /** Set color of touched meta keys */
    public void setMetaColor(Object colorParameter)
        {
        this.metaColor = (int)colorParameter;
        tokenizer.note(R.string.data_metacolor, Integer.toHexString( this.metaColor));
        }

    /** Set color of locked meta keys */
    public void setLockColor(Object colorParameter)
        {
        this.lockColor = (int)colorParameter;
        tokenizer.note(R.string.data_lockcolor, Integer.toHexString( this.lockColor));
        }

    /** Set color of locked meta keys */
    public void setAutoColor(Object colorParameter)
        {
        this.autoColor = (int)colorParameter;
        tokenizer.note(R.string.data_autocolor, Integer.toHexString( this.autoColor));
        }

    /** Set color of touched button */
    public void setTouchColor(Object colorParameter)
        {
        this.touchColor = (int)colorParameter;
        tokenizer.note(R.string.data_touchcolor, Integer.toHexString( this.touchColor));
        }

    /** Set color of stroke */
    public void setStrokeColor(Object colorParameter)
        {
        this.strokeColor = (int)colorParameter;
        tokenizer.note(R.string.data_strokecolor, Integer.toHexString( this.strokeColor));
        }

    /** Set typeface of title font from file */
    public void setTypeface( Object fileParameter )
        {
        try
            {
            Typeface typeface = Typeface.createFromFile( (File)fileParameter );
            tokenizer.note( R.string.data_typeface, typeface.toString() );
            TitleDescriptor.setTypeface( typeface );
            }
        catch (Exception e)
            {
            tokenizer.error(R.string.data_typeface_missing, fileParameter.toString());
            }
        }

    /** Set entertitle */
    public void setEnterTitle( Object textParameter )
        {
        this.actionTitles[ACTION_MULTILINE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_entertitle, this.actionTitles[ACTION_MULTILINE] );
        }

    /** Set gotitle */
    public void setGoTitle( Object textParameter )
        {
        this.actionTitles[ACTION_GO] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_gotitle, this.actionTitles[ACTION_GO] );
        }

    /** Set searchtitle */
    public void setSearchTitle( Object textParameter )
        {
        this.actionTitles[ACTION_SEARCH] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_searchtitle, this.actionTitles[ACTION_SEARCH] );
        }

    /** Set sendtitle */
    public void setSendTitle( Object textParameter )
        {
        this.actionTitles[ACTION_SEND] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_sendtitle, this.actionTitles[ACTION_SEND] );
        }

    /** Set nexttitle */
    public void setNextTitle( Object textParameter )
        {
        this.actionTitles[ACTION_NEXT] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_nexttitle, this.actionTitles[ACTION_NEXT] );
        }

    /** Set donetitle */
    public void setDoneTitle( Object textParameter )
        {
        this.actionTitles[ACTION_DONE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_donetitle, this.actionTitles[ACTION_DONE] );
        }

    /** Set prevtitle */
    public void setPrevTitle( Object textParameter )
        {
        this.actionTitles[ACTION_PREVIOUS] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_prevtitle, this.actionTitles[ACTION_PREVIOUS] );
        }

    /** Set nonetitle */
    public void setNoneTitle( Object textParameter )
        {
        this.actionTitles[ACTION_NONE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_nonetitle, this.actionTitles[ACTION_NONE] );
        }

    /** Set unknowntitle */
    public void setUnknownTitle( Object textParameter )
        {
        this.actionTitles[ACTION_UNSPECIFIED] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_unknowntitle, this.actionTitles[ACTION_UNSPECIFIED] );
        }

    public void addSlot( ExtendedMap<Long, Object> parameters )
        {
        Long id = (Long)parameters.get( Commands.TOKEN_ID );
        if (id == null)
            {
            tokenizer.error("ADDSLOT", R.string.data_slot_no_id );
            return;
            }

        // default TitleSlot cannot be null, it is checked on selection
        Slot dts = Slots.get( defaultSlot );
        int xOffset = (int)parameters.get( Commands.TOKEN_XOFFSET, dts.xOffset );
        int yOffset = (int)parameters.get( Commands.TOKEN_YOFFSET, dts.yOffset );
        int size = (int)parameters.get( Commands.TOKEN_SIZE, dts.size );
        boolean bold = (boolean)parameters.get( Commands.TOKEN_BOLD, dts.bold );
        boolean italics = (boolean)parameters.get( Commands.TOKEN_ITALICS, dts.italics );
        int color = (int)parameters.get( Commands.TOKEN_COLOR, dts.color );

        Slot slot = new Slot( xOffset, yOffset, size, bold, italics, color );
        Slots.put( id, slot );
        tokenizer.note( Tokenizer.regenerateKeyword( (long)id),
                R.string.data_slot_added,
                slot.toString());
        }

    public void addBoard( ExtendedMap<Long, Object> parameters )
        {
        Object temp;

        Long id;

        int halfColumns; // obligate parameter
        int rows; // obligate parameter
        boolean wide; // default false
        boolean oddRowsAligned; // default: EVENS ALIGNED
        int color; // default: defaultBoardColor
        Trilean[] metaStates = new Trilean[ BoardStates.META_STATES_SIZE]; // default: IGNORED

        id = (Long)parameters.get( Commands.TOKEN_ID );
        if (id == null)
            {
            tokenizer.error( "ADDBOARD", R.string.data_board_no_id );
            return;
            }

        temp = parameters.get( Commands.TOKEN_HALFCOLUMNS );
        if (temp != null)
            {
            halfColumns = (int) temp;
            // if HALFCOLUMNS is available, then COLUMNS is not checked !!
            }
        else
            {
            // if HALFCOLUMNS is missing, try COLUMNS!
            temp = parameters.get(Commands.TOKEN_COLUMNS);
            if (temp != null)
                {
                // One half column is added as standard
                halfColumns = (int) temp * 2 + 1;
                }
            else
                {
                tokenizer.error(Tokenizer.regenerateKeyword((long) id),
                        R.string.data_columns_missing);
                return;
                }
            }

        temp = parameters.get( Commands.TOKEN_ROWS );
        if (temp == null)
            {
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_rows_missing );
            return;
            }
        rows = (int)temp;

        wide = (boolean)parameters.get( Commands.TOKEN_WIDE, false );

        oddRowsAligned = true; // default: ODDS_ALIGNED
        long alignFlag = (long)parameters.get( Commands.TOKEN_ALIGN, -1L );
        if ( alignFlag == Commands.TOKEN_ODDS )
            ; // default remains
        else if ( alignFlag == Commands.TOKEN_EVENS )
            oddRowsAligned = false;
        else if ( alignFlag != -1L )
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_align_bad_parameter );

        color = (int)parameters.get( Commands.TOKEN_COLOR, defaultBoardColor);

        // missing token (null) is interpreted as IGNORE
        metaStates[ BoardStates.META_SHIFT ] =
                Trilean.valueOf((Boolean)parameters.get( Commands.TOKEN_FORCESHIFT ));
        metaStates[ BoardStates.META_CTRL ] =
                Trilean.valueOf((Boolean)parameters.get( Commands.TOKEN_FORCECTRL ));
        metaStates[ BoardStates.META_ALT ] =
                Trilean.valueOf((Boolean)parameters.get( Commands.TOKEN_FORCEALT ));
        metaStates[ BoardStates.META_CAPS ] =
                Trilean.valueOf((Boolean)parameters.get( Commands.TOKEN_FORCECAPS ));

        try
            {
            Board board = new Board(this, halfColumns, rows, oddRowsAligned, wide, color, metaStates );

            // needed only by debugging purposes
            board.setBoardId( id );
            
            // exclude all boards
            setCursorNone();
            // but the new board which will be included, cursor set to the first position
            BoardPlan boardPlan = new BoardPlan(board);
            boardPlans.put( id, boardPlan );
            includedBoardPlans.add( boardPlan );

            tokenizer.note( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_board_added,
                    board.toString());

            // the first non-wide board is stored
            if ( firstBoard == null && !wide )
                {
                firstBoard = board;
                }

            }
        catch (ExternalDataException ede)
            {
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_board_error );
            }
        }

    public void setCursorNone()
        {
        includedBoardPlans.clear();
        tokenizer.note( R.string.data_boards_excluded );
        }

    public void setCursor(ExtendedMap<Long, Object> parameters)
        {
        // No other parameters are allowed with NONE
        if ( parameters.containsKey( Commands.TOKEN_NONE ) )
            {
            setCursorNone();
            if ( parameters.size() != 2 )
                {
                tokenizer.error( "CURSOR", R.string.data_none_parameters_not_allowed );
                }
            return;
            }

        Long boardId = (Long)parameters.get( Commands.TOKEN_BOARD );
        if (boardId == null)
            {
            tokenizer.error( "CURSOR", R.string.data_board_missing );
            return;
            }

        int column = (int)parameters.get( Commands.TOKEN_COLUMN, 0 );
        int row = (int)parameters.get( Commands.TOKEN_ROW, 0 );
        boolean also = (boolean)parameters.get( Commands.TOKEN_ALSO, false );
        boolean transform = (boolean)parameters.get( Commands.TOKEN_TRANSFORM, false );

        // exclude all boards, if also is not given
        if ( !also )
            setCursorNone();

        BoardPlan boardPlan = boardPlans.get( boardId );
        if ( boardPlan == null )
            {
            tokenizer.error( Tokenizer.regenerateKeyword( (long)boardId),
                    R.string.data_no_board );
            return;
            }

        includedBoardPlans.add( boardPlan );
        boardPlan.cursorColumn = column;
        boardPlan.cursorDefaultColumn = column;
        boardPlan.cursorRow = row;
        boardPlan.transform = transform;

        tokenizer.note( Tokenizer.regenerateKeyword( (long)boardId),
                R.string.data_cursor_set,
                boardPlan.toString() );
        }

    public void next()
        {
        for (BoardPlan includedBoardPlan : includedBoardPlans)
            {
            includedBoardPlan.cursorColumn++;
            }
        }

    public void nextRow()
        {
        for (BoardPlan includedBoardPlan : includedBoardPlans)
            {
            includedBoardPlan.cursorRow++;
            includedBoardPlan.cursorColumn = includedBoardPlan.cursorDefaultColumn;
            }
        }

    public void skip( Object intParameter )
        {
        for (BoardPlan includedBoardPlan : includedBoardPlans)
            {
            includedBoardPlan.cursorColumn += (int)intParameter;
            }
        }

    public void skipRow( Object intParameter )
        {
        for (BoardPlan includedBoardPlan : includedBoardPlans)
            {
            includedBoardPlan.cursorRow += (int)intParameter + 1;
            // because nextrow is missing, while next is part of the button
            includedBoardPlan.cursorColumn = includedBoardPlan.cursorDefaultColumn;
            }
        }

    public Button createButtonFunction(ExtendedMap<Long, Object> parameters)
        {
        Button buttonFunction = null;
        Object temp;

        int counter = 0;

        // SEND contains the parameters of a virtual PACKET command
        Packet packet = packet( parameters );
        if (packet != null )
            {
            Packet secondaryPacket = (Packet)parameters.get( Commands.TOKEN_SECOND );

            if ( secondaryPacket != null )
                {
                buttonFunction = new ButtonDouble(packet, secondaryPacket);
                Scribe.debug(Debug.DATA, "Double Packet is defined");
                }
            else
                {
                buttonFunction = new ButtonPacket(packet, parameters.containsKey(Commands.TOKEN_REPEAT));
                Scribe.debug(Debug.DATA, "Simple Packet is defined");
                }
            counter++;
            }

        temp = parameters.get( Commands.TOKEN_LINK );
        if (temp != null)
            {
            counter++;
            buttonFunction = new ButtonLink( (int)temp,
                    parameters.containsKey(Commands.TOKEN_LOCK) );
            // invalid index - (int)temp - means go back to previous board
            }

        temp = parameters.get( Commands.TOKEN_META );
        if (temp != null)
            {
            counter++;
            int meta = -1;

            if ( (long)temp == Commands.TOKEN_CAPS )
                meta = BoardStates.META_CAPS;
            else if ( (long)temp == Commands.TOKEN_SHIFT )
                meta = BoardStates.META_SHIFT;
            else if ( (long)temp == Commands.TOKEN_CTRL )
                meta = BoardStates.META_CTRL;
            else if ( (long)temp == Commands.TOKEN_ALT )
                meta = BoardStates.META_ALT;

            // ButtonMeta constructor will not accept any non-valid parameter

            try
                {
                buttonFunction = new ButtonMeta( meta,
                        parameters.containsKey( Commands.TOKEN_LOCK) );
                }
            catch (ExternalDataException e)
                {
                tokenizer.error("META", R.string.data_meta_unknown_meta_state);
                }
            }

        if ( parameters.containsKey( Commands.TOKEN_SPACETRAVEL ) )
            {
            counter++;
            // Packet with default cannot be null!
            buttonFunction = new ButtonSpaceTravel( packet(parameters, " ") );
            }

        temp = parameters.get( Commands.TOKEN_MODIFY );
        if (temp != null)
            {
            counter++;
            buttonFunction = new ButtonModify( (long)temp,
                    parameters.containsKey( Commands.TOKEN_REVERSE ));
            }

        if ( parameters.containsKey( Commands.TOKEN_ENTER ) )
            {
            counter++;

            // Packet with default cannot be null!
            PacketKey packetKey = packetKey( parameters, 0x10000 + KeyEvent.KEYCODE_ENTER ); // Or: '\n'
            PacketText packetText = packetText( parameters, "\n" );

            buttonFunction = new ButtonEnter( packetKey, packetText, parameters.containsKey( Commands.TOKEN_REPEAT) );
            }

        if ( counter > 1 )
            {
            tokenizer.error("SEND", R.string.data_send_one_allowed );
            }
        else if (buttonFunction == null )  // OR (counter == 0)
            {
            tokenizer.error("SEND", R.string.data_send_missing);
            // Buttons function was not set - button will give an error message
            // It will return null
            }

        return buttonFunction;
        }

    /** if no default key is given for packetKey */
    public static final int NO_DEFAULT_KEY = -1;


    /**
     * Creates packetKey from parameters.
     * KEY parameter is used,
     * defaultKey is used if KEY parameter is missing, (NO_DEFAULT_KEY)
     * null is returned if both are missing.
     * @param parameters Key packet parameters (KEY, SETSHIFT, SETCTRL, SETALT)
     * @param defaultKey default key (if KEY is missing) or NO_DEFAULT_KEY
     * @return Key packet or null
     */
    public PacketKey packetKey( ExtendedMap<Long, Object> parameters, int defaultKey )
        {
        PacketKey packet = null;
        int temp;

        temp = (int)parameters.get( Commands.TOKEN_KEY, NO_DEFAULT_KEY );

        if ( temp == NO_DEFAULT_KEY )             // KEY token is missing
            {
            temp = defaultKey;         // use default instead of KEY
            }
        else if ( defaultKey != NO_DEFAULT_KEY ) // both TEXT and default -> override default
            {
            tokenizer.error("PACKET", R.string.data_send_packet_key_override );
            }

        if ( temp != NO_DEFAULT_KEY )
            {
            // TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT feldolgozása
            packet = new PacketKey( this, temp,
                    BoardStates.generateBinaryHardState( parameters ));
            }

        return packet;
        }


    /**
     * Creates packetText from parameters.
     * TEXT parameter is used,
     * defaultText is used if TEXT parameter is missing,
     * null is returned if both are missing.
     * @param parameters Text packet parameters
     * (TEXT, AUTOCAPS ON, OFF, HOLD, WAIT, STRINGCAPS)
     * @param defaultText default text (if TEXT is missing) or null
     * @return Text packet or null
     */
    public PacketText packetText( ExtendedMap<Long, Object> parameters, String defaultText )
        {
        PacketText packet = null;
        Object temp;

        temp = parameters.get(Commands.TOKEN_TEXT);

        if ( temp == null )             // TEXT token is missing
            {
            temp = defaultText;         // use default instead of TEXT
            }
        else if ( defaultText != null ) // both TEXT and default -> override default
            {
            tokenizer.error("PACKET", R.string.data_send_packet_text_override );
            }

        if (temp != null)
            {
            long autoFlag;

            int autoCaps = CapsState.AUTOCAPS_OFF;
            int autoSpace = 0;

            autoFlag = (long)parameters.get( Commands.TOKEN_AUTOCAPS, -1L );
            if ( autoFlag == Commands.TOKEN_ON )
                autoCaps = CapsState.AUTOCAPS_ON;
            else if ( autoFlag == Commands.TOKEN_HOLD )
                autoCaps = CapsState.AUTOCAPS_HOLD;
            else if ( autoFlag == Commands.TOKEN_WAIT )
                autoCaps = CapsState.AUTOCAPS_WAIT;
            else if ( autoFlag == Commands.TOKEN_OFF )
                ; // default remains
            else if ( autoFlag != -1L )
                tokenizer.error("PACKET", R.string.data_autocaps_bad_parameter );

            autoFlag = (long)parameters.get( Commands.TOKEN_AUTOSPACE, -1L );
            if ( autoFlag == Commands.TOKEN_BEFORE )
                autoSpace = PacketText.AUTO_SPACE_BEFORE;
            else if ( autoFlag == Commands.TOKEN_AFTER )
                autoSpace = PacketText.AUTO_SPACE_AFTER;
            else if ( autoFlag == Commands.TOKEN_AROUND )
                autoSpace = PacketText.AUTO_SPACE_BEFORE | PacketText.AUTO_SPACE_AFTER;
            else if ( autoFlag != -1L )
                tokenizer.error("PACKET", R.string.data_autospace_bad_parameter );

            autoFlag = (long)parameters.get( Commands.TOKEN_ERASESPACES, -1L );
            if ( autoFlag == Commands.TOKEN_BEFORE )
                autoSpace |= PacketText.ERASE_SPACES_BEFORE;
            else if ( autoFlag == Commands.TOKEN_AFTER )
                autoSpace |= PacketText.ERASE_SPACES_AFTER;
            else if ( autoFlag == Commands.TOKEN_AROUND )
                autoSpace |= PacketText.ERASE_SPACES_BEFORE | PacketText.ERASE_SPACES_AFTER;
            else if ( autoFlag != -1L )
                tokenizer.error("PACKET", R.string.data_erasespaces_bad_parameter );

            if (temp instanceof Character)
                {
                packet = new PacketText( this, (Character)temp, autoCaps,
                        autoSpace );
                }
            else // if (temp instanceof String)
                {
                packet = new PacketText( this, (String)temp, autoCaps,
                        parameters.containsKey( Commands.TOKEN_STRINGCAPS), autoSpace );
                }
            }

        return packet;
        }


    /**
     * Creates packetFunction from parameters.
     * DO parameter is used
     * null is returned if DO is missing.
     * PacketFunction has not got any default value!
     * @param parameters Function packet parameters (DO)
     * @return Function packet or null
     */
    public PacketFunction packetFunction( ExtendedMap<Long, Object> parameters )
        {
        PacketFunction packet = null;
        long temp;

        temp = (long)parameters.get( Commands.TOKEN_DO, -1L );

        if ( temp != -1L )
            {
            try
                {
                packet = new PacketFunction( this, temp );
                }
            catch ( ExternalDataException e )
                {
                tokenizer.error( "PACKET", R.string.data_send_function_invalid );
                }
            }

        return packet;
        }


    /**
     * Create text or key or function packet from parameters.
     * @param parameters for text or key or function packet
     * @return the created packet, or null if no TEXT or KEY or DO parameter is given
     */
    public Packet packet( ExtendedMap<Long, Object> parameters )
        {
        Packet packet;

        packet = packetText( parameters, null );

        if ( packet == null )
            packet = packetKey( parameters, NO_DEFAULT_KEY );

        if ( packet == null )
            packet = packetFunction( parameters );

        return packet;
        }


    /**
     * Create text or key packet from parameters.
     * @param parameters for text or key packet
     * @param defaultKey is used if both TEXT and KEY parameters are missing
     * @return the created packet, or null if both parameters and default key is missing
     * returned packet is always valid, if defaultKey is not NO_DEFAULT_KEY
     */
    public Packet packet( ExtendedMap<Long, Object> parameters, int defaultKey )
        {
        Packet packet;

        packet = packetText( parameters, null );

        if ( packet == null )
            packet = packetKey( parameters, defaultKey );

        return packet;
        }


    /**
     * Create key or text packet from parameters.
     * @param parameters for key or text packet
     * @param defaultText is used if both KEY and TEXT parameters are missing
     * @return the created packet, or null if both parameters and default text is missing
     * returned packet is always valid, if defaultText is not null
     */
    public Packet packet( ExtendedMap<Long, Object> parameters, String defaultText )
        {
        Packet packet;

        packet = packetKey( parameters, NO_DEFAULT_KEY );

        if ( packet == null )
            packet = packetText( parameters, defaultText );

        return packet;
        }


    public SinglyLinkedList<TitleDescriptor> addTitle( ExtendedMap<Long, Object> parameters )
        {
        // !! http://stackoverflow.com/questions/509076/how-do-i-address-unchecked-cast-warnings
        // Maybe better to avoid Unchecked cast warnings

        // Check the result of previous ADDTITLE parameter-commands
        SinglyLinkedList<TitleDescriptor> titles =
                (SinglyLinkedList<TitleDescriptor>)
                        parameters.get( Commands.TOKEN_ADDTITLE, new SinglyLinkedList<TitleDescriptor>() );

        // text is optional, if it is null, then button's function will be used
        String text = null;
        Object temp = parameters.get( Commands.TOKEN_TEXT );
        if ( temp != null )
            {
            text = SoftBoardParser.stringFromText( temp );
            }

        Slot ts = null;

        Long titleSlotId = (Long)parameters.get( Commands.TOKEN_SLOT );
        if (titleSlotId != null) // SLOT is definied
            {
            ts = Slots.get(titleSlotId);
            if (ts == null)
                tokenizer.error( text != null ? text : "ADDTITLE",
                        R.string.data_titleslot_invalid,
                        Tokenizer.regenerateKeyword( titleSlotId ));
            }

        if (ts == null) // default SLOT should be used
            ts = Slots.get( defaultSlot );
        // default TitleSlot cannot be null, it is checked on selection

        int xOffset = (int)parameters.get( Commands.TOKEN_XOFFSET, ts.xOffset );
        int yOffset = (int)parameters.get( Commands.TOKEN_YOFFSET, ts.yOffset );
        int size = (int)parameters.get( Commands.TOKEN_SIZE, ts.size );
        boolean bold = (boolean)parameters.get( Commands.TOKEN_BOLD, ts.bold );
        boolean italics = (boolean)parameters.get( Commands.TOKEN_ITALICS, ts.italics );
        int color = (int)parameters.get( Commands.TOKEN_COLOR, ts.color );

        titles.add( new TitleDescriptor( text, xOffset, yOffset, size, bold, italics, color ) );

        return titles;
        }

    public void setButton(ExtendedMap<Long, Object> parameters)
        {
        Button button;

        // "SEND" parameters could be found among "BUTTON"-s parameters
        // For testing reasons SEND remains...
        Object temp = parameters.get( Commands.TOKEN_SEND );
        if ( temp != null )
            {
            button = (Button) temp;
            }
        // ...but if SEND is missing, then parameters are submitted directly
        else
            {
            button = createButtonFunction(parameters);
            }

        if ( button == null )
            {
            tokenizer.error( "BUTTON", R.string.data_button_function_missing);
            button = new Button();
            }

        button.setColor((int) parameters.get(Commands.TOKEN_COLOR, defaultButtonColor));
        
        // if no titles are added, then addTitle will add one based on default titleSlot
        // an empty parameter list is needed
        SinglyLinkedList<TitleDescriptor> titles =
                (SinglyLinkedList<TitleDescriptor>)parameters.get( Commands.TOKEN_ADDTITLE,
                        addTitle( new ExtendedMap<Long, Object>(0)) );

        // if title text is null, code should be used
        // button id can be created from the titles (and from the code)
        StringBuilder buttonNameBuilder = new StringBuilder();
        for ( TitleDescriptor title : titles )
            {
            title.checkText( button.getString() );
            buttonNameBuilder.insert( 0, title.getText() ).insert( 0,'/');
            }
        buttonNameBuilder.setCharAt( 0, '\"');
        String buttonName = buttonNameBuilder.append('\"').toString();

        button.setTitles( titles );

        for (BoardPlan includedBoardPlan : includedBoardPlans)
            {
            try
                {
                if (includedBoardPlan.board.addButton(
                        includedBoardPlan.getTransformedCursorColumn(),
                        includedBoardPlan.cursorRow,
                        button.clone() ))
                    {
                    if ( parameters.containsKey( Commands.TOKEN_OVERWRITE ) )
                        {
                        tokenizer.note( R.string.data_button_overwritten,
                                includedBoardPlan.toString() );
                        }
                    else
                        {
                        tokenizer.error( R.string.data_button_overwritten,
                                includedBoardPlan.toString() );
                        }
                    }
                tokenizer.note( buttonName, R.string.data_button_added,
                        includedBoardPlan.toString() );
                }
            catch (ExternalDataException ede)
                {
                tokenizer.error( buttonName, R.string.data_button_error,
                        includedBoardPlan.toString()  );
                }
            }

        // Button can call next without explicitly writing it.
        next();
        }

    public void addLink( ExtendedMap<Long, Object> parameters )
        {
        Integer index = (Integer)parameters.get( Commands.TOKEN_INDEX );
        if (index == null)
            {
            tokenizer.error("ADDLINK", R.string.data_addlink_no_index );
            return;
            }
            
        // BOARD is given, no other parameters are checked
        Long boardId = (Long)parameters.get( Commands.TOKEN_BOARD );
        if (boardId != null)
            {
            BoardPlan boardPlan = boardPlans.get( boardId );
            if ( boardPlan == null )
                {
                tokenizer.error( "BOARD", R.string.data_no_board,
                        Tokenizer.regenerateKeyword( (long)boardId));
                return;
                }

            // !! Common try/catch could be used !!
            // !! Overwritten entry could be checked (setLinkBoardTable returns true if entry is overwritten) !!
            try
                {
                linkState.setLinkBoardTable( index, boardPlan.board );

                tokenizer.note( index.toString(), R.string.data_addlink_board_set,
                               Tokenizer.regenerateKeyword( (long)boardId));
                }
            catch (ExternalDataException e)
                {
                tokenizer.error("ADDLINK", R.string.data_addlink_invalid_index, index.toString());
                }
            }

        // no BOARD is given, so PORTRAIT AND LANDSCAPE is needed
        // BOTH parameters are checked completely before
        else
            {
            BoardPlan boardPlan;

            Long portraitId = (Long)parameters.get( Commands.TOKEN_PORTRAIT );
            Board portrait = null;

            if ( portraitId != null )
                {
                boardPlan = boardPlans.get( portraitId );
                if ( boardPlan != null )
                    {
                    portrait = boardPlan.board;
                    }
                else
                    {
                    tokenizer.error( "PORTRAIT", R.string.data_no_board,
                            Tokenizer.regenerateKeyword( (long)portraitId));
                    }
                }
            else
                {
                tokenizer.error( index.toString(), R.string.data_addlink_portrait_missing );
                }

            Long landscapeId = (Long)parameters.get( Commands.TOKEN_LANDSCAPE );
            Board landscape = null;

            if ( landscapeId != null )
                {
                boardPlan = boardPlans.get( landscapeId );
                if ( boardPlan != null )
                    {
                    landscape = boardPlan.board;
                    }
                else
                    {
                    tokenizer.error( "LANDSCAPE", R.string.data_no_board,
                            Tokenizer.regenerateKeyword( (long)landscapeId));
                    }
                }
            else
                {
                tokenizer.error( index.toString(), R.string.data_addlink );
                }

            // only if both parameters are ok
            if ( portrait != null && landscape != null )
                {
                // !! Common try/catch could be used !!
                // !! Overwritten entry could be checked (setLinkBoardTable returns true if entry is overwritten) !!
                try
                    {
                    linkState.setLinkBoardTable( index, portrait, landscape );

                    tokenizer.note( index.toString(), R.string.data_addlink_board_set,
                                   Tokenizer.regenerateKeyword( (long)portraitId) +
                                   "/" +
                                   Tokenizer.regenerateKeyword( (long)landscapeId));
                    }
                catch (ExternalDataException e)
                    {
                    tokenizer.error("ADDLINK", R.string.data_addlink_invalid_index, index.toString());
                    }
                }

            }
        }


    private List< List<Object> > tempRolls = new ArrayList< List<Object> >();

    /**
     * Helper method to collect ADDROLL stringListParameters
     * To avoid this "multiple" type parameters should be implemented
     * @param stringListParameter roll (list of strings) to store temporarily
     */
    public void addRollHelper( List<Object> stringListParameter )
        {
        tempRolls.add( stringListParameter );
        }


    public void addModify( ExtendedMap<Long, Object> parameters )
        {
        Long id;

        id = (Long) parameters.get( Commands.TOKEN_ID );
        if ( id == null )
            {
            tokenizer.error("ADDMODIFY", R.string.data_modify_no_id );
            return;
            }

        Modify mod = null;
        boolean empty = true;
        int counter = 0;

        // ADDROLL-s were used!
        if ( tempRolls.size() > 0)
            {
            empty = true;
            counter ++;

            mod = new ModifyText( softBoardListener,
                    parameters.containsKey( Commands.TOKEN_IGNORESPACE ) );

            for ( List<Object> roll : tempRolls )
                {
                if ( ((ModifyText)mod).addStringRoll( roll ))
                    empty = false;
                }

            // tempRolls were added, temporary storage is cleared
            tempRolls.clear();
            }

        // ROLLS is used!
        List<Object> rolls = (List) parameters.get( Commands.TOKEN_ROLLS );
        if ( rolls != null )
            {
            empty = true;
            counter++;

            mod = new ModifyChar( softBoardListener,
                    parameters.containsKey( Commands.TOKEN_IGNORESPACE ) );

            // PARAMETER_STRING_LIST gives only non-null String items
            for ( Object roll : rolls )
                {
                if ( ((ModifyChar)mod).addCharacterRoll( (String) roll ) )
                    empty = false;
                }
            }

        if (counter > 1)
            {
            tokenizer.error( Tokenizer.regenerateKeyword( id ),
                    R.string.data_modify_one_allowed );
            }

        // No roll could be added!
        if ( empty )
            {
            tokenizer.error( Tokenizer.regenerateKeyword( id ),
                    R.string.data_modify_no_rolls );
            return;
            }

        if ( modify.get( id ) != null )
            {
            tokenizer.error( Tokenizer.regenerateKeyword( id ),
                    R.string.data_modify_overwritten );
            }

        modify.put( id, mod );

        tokenizer.note( Tokenizer.regenerateKeyword( id ),
                R.string.data_modify_added );
        }

    }

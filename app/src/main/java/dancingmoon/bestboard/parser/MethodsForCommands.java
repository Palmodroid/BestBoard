package dancingmoon.bestboard.parser;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.KeyEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dancingmoon.bestboard.Board;
import dancingmoon.bestboard.R;
import dancingmoon.bestboard.SoftBoardData;
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
import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.modify.Modify;
import dancingmoon.bestboard.modify.ModifyChar;
import dancingmoon.bestboard.modify.ModifyText;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.CapsState;
import dancingmoon.bestboard.utils.Bit;
import dancingmoon.bestboard.utils.ExtendedMap;
import dancingmoon.bestboard.utils.ExternalDataException;
import dancingmoon.bestboard.utils.KeyValuePair;
import dancingmoon.bestboard.utils.SinglyLinkedList;
import dancingmoon.bestboard.utils.Trilean;

/**
 * Methods to create SoftBoardData from coat descriptor
 */
public class MethodsForCommands
    {
    /**
     * Tokenizer (from SoftBoardParser) is needed for messaging during data-load.
     * It should be cleared, after data-load is ready.
     */
    private Tokenizer tokenizer;

    /**
     * Defaults (from SoftBoardParser) is needed for messaging during data-load.
     * It should be cleared, after data-load is ready.
     */
    private ExtendedMap< Long, ExtendedMap< Long, Object>> defaults;

    /**
     * SoftBoardData will be populated during the parsing process
     */
    private SoftBoardData softBoardData;

    public MethodsForCommands( SoftBoardData softBoardData, SoftBoardParser softBoardParser )
        {
        this.softBoardData = softBoardData;
        this.tokenizer = softBoardParser.getTokenizer();
        this.defaults = softBoardParser.getDefaults();
        }

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

        private BoardPlan(Board board)
            {
            this.board = board;
            }

        }

    /**
     * Temporary data to create buttons
     */
    private class ButtonPlan
        {
        private String buttonName;
        private Button button;
        }


    /**
     * Buttons can be extended after definition
     */
    private class ButtonExtension
        {
        private Integer color = null;
        private ArrayList<KeyValuePair> titleList = null;
        }


    public void createDefaults()
        {

        ExtendedMap<Long, Object> defaultTitle = new ExtendedMap<>();
        defaultTitle.put( Commands.TOKEN_YOFFSET, 250 ); // PARAMETER_INT
        defaultTitle.put( Commands.TOKEN_SIZE, 1200 ); // PARAMETER_INT
        defaultTitle.put( Commands.TOKEN_COLOR, Color.BLACK ); // PARAMETER_COLOR (int)

        defaults.put( Commands.TOKEN_ADDTITLE, defaultTitle );

        }

    /**
     ** TEMPORARY VARIABLES NEEDED ONLY BY THE PARSING PHASE
     **/


    /** if no default key is given for packetKey */
    public static final int NO_DEFAULT_KEY = -1;


    /** Board's default background */
    public static final int DEFAULT_BOARD_COLOR = Color.LTGRAY;

    /** Button's default background */
    public static final int DEFAULT_BUTTON_COLOR = Color.LTGRAY;

    /** Map of temporary boardPlans, identified by code of keywords */
    public Map<Long, BoardPlan> boardPlans = new HashMap<>();


    /**
     ** SETTERS CALLED ONLY BY PARSING PHASE
     **/

    /** Set softboard's name */
    public void setName( Object stringParameter )
        {
        softBoardData.name = (String) stringParameter;
        tokenizer.note(R.string.data_name, softBoardData.name );
        }

    /** Set softboard's version */
    public void setVersion( Object intParameter )
        {
        softBoardData.version = (int)intParameter;
        tokenizer.note(R.string.data_version, String.valueOf(softBoardData.version));
        }

    /** Set softboard's author */
    public void setAuthor( Object stringParameter )
        {
        softBoardData.author = (String)stringParameter;
        tokenizer.note(R.string.data_author, softBoardData.author );
        }

    /** Add softboard's tags */
    public void addTags( List<Object> stringListParameter )
        {
        // PARAMETER_STRING_LIST gives only non-null String items
        for (Object item: stringListParameter)
            {
            softBoardData.tags.add( (String) item );
            tokenizer.note( R.string.data_tags, (String) item );
            }
        }

    /** Set softboard's short description */
    public void setDescription( Object stringParameter )
        {
        softBoardData.description = (String)stringParameter;
        tokenizer.note(R.string.data_description, softBoardData.description );
        }

    /**
     * Set file name of softboard's document (should be in the same directory) - if available
     * DocFile is not checked, just stored !!
     */
    public void setDocFile( Object fileParameter )
        {
        softBoardData.docFile = (File)fileParameter;
        tokenizer.note(R.string.data_docfile, softBoardData.docFile.toString() );
        }

    /**
     * Set full URI of softboard's document - if available
     * DocUri is not checked, just stored !!
     */
    public void setDocUri( Object stringParameter )
        {
        softBoardData.docUri = (String)stringParameter;
        tokenizer.note(R.string.data_docuri, softBoardData.docUri );
        }

    /**
     * Set softboard's locale
     * Locale is not checked, just set !!
     */
    public void setLocale( ExtendedMap<Long, Object> parameters )
        {
        String language = (String)parameters.remove(Commands.TOKEN_LANGUAGE, "");
        String country = (String)parameters.remove(Commands.TOKEN_COUNTRY, "");
        String variant = (String)parameters.remove(Commands.TOKEN_VARIANT, "");

        softBoardData.locale = new Locale( language, country, variant);
        tokenizer.note(R.string.data_locale, String.valueOf(softBoardData.locale) );
        }

    /** Set color of touched meta keys */
    public void setMetaColor(Object colorParameter)
        {
        softBoardData.metaColor = (int)colorParameter;
        tokenizer.note(R.string.data_metacolor, Integer.toHexString( softBoardData.metaColor));
        }

    /** Set color of locked meta keys */
    public void setLockColor(Object colorParameter)
        {
        softBoardData.lockColor = (int)colorParameter;
        tokenizer.note(R.string.data_lockcolor, Integer.toHexString( softBoardData.lockColor));
        }

    /** Set color of locked meta keys */
    public void setAutoColor(Object colorParameter)
        {
        softBoardData.autoColor = (int)colorParameter;
        tokenizer.note(R.string.data_autocolor, Integer.toHexString( softBoardData.autoColor));
        }

    /** Set color of touched button */
    public void setTouchColor(Object colorParameter)
        {
        softBoardData.touchColor = (int)colorParameter;
        tokenizer.note(R.string.data_touchcolor, Integer.toHexString( softBoardData.touchColor));
        }

    /** Set color of stroke */
    public void setStrokeColor(Object colorParameter)
        {
        softBoardData.strokeColor = (int)colorParameter;
        tokenizer.note(R.string.data_strokecolor, Integer.toHexString( softBoardData.strokeColor));
        }

    /** Set typeface of title font from file */
    public void setTypeface( Object fileParameter )
        {
        try
            {
            Typeface typeface = Typeface.createFromFile( (File)fileParameter );
            tokenizer.note( R.string.data_typeface, typeface.toString() );
            TitleDescriptor.setTypeface(typeface);
            }
        catch (Exception e)
            {
            tokenizer.error(R.string.data_typeface_missing, fileParameter.toString());
            }
        }

    /** Set entertitle */
    public void setEnterTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_MULTILINE] = SoftBoardParser.stringFromText(textParameter);
        tokenizer.note(R.string.data_entertitle, softBoardData.actionTitles[SoftBoardData.ACTION_MULTILINE] );
        }

    /** Set gotitle */
    public void setGoTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_GO] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_gotitle, softBoardData.actionTitles[SoftBoardData.ACTION_GO] );
        }

    /** Set searchtitle */
    public void setSearchTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_SEARCH] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_searchtitle, softBoardData.actionTitles[SoftBoardData.ACTION_SEARCH] );
        }

    /** Set sendtitle */
    public void setSendTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_SEND] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_sendtitle, softBoardData.actionTitles[SoftBoardData.ACTION_SEND] );
        }

    /** Set nexttitle */
    public void setNextTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_NEXT] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_nexttitle, softBoardData.actionTitles[SoftBoardData.ACTION_NEXT] );
        }

    /** Set donetitle */
    public void setDoneTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_DONE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_donetitle, softBoardData.actionTitles[SoftBoardData.ACTION_DONE] );
        }

    /** Set prevtitle */
    public void setPrevTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_PREVIOUS] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_prevtitle, softBoardData.actionTitles[SoftBoardData.ACTION_PREVIOUS] );
        }

    /** Set nonetitle */
    public void setNoneTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_NONE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_nonetitle, softBoardData.actionTitles[SoftBoardData.ACTION_NONE] );
        }

    /** Set unknowntitle */
    public void setUnknownTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_UNSPECIFIED] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_unknowntitle, softBoardData.actionTitles[SoftBoardData.ACTION_UNSPECIFIED] );
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

        id = (Long)parameters.remove( Commands.TOKEN_ID );
        if (id == null)
            {
            tokenizer.error( "ADDBOARD", R.string.data_board_no_id );
            return;
            }

        temp = parameters.remove( Commands.TOKEN_HALFCOLUMNS );
        if (temp != null)
            {
            halfColumns = (int) temp;
            // if HALFCOLUMNS is available, then COLUMNS is not checked !!
            }
        else
            {
            // if HALFCOLUMNS is missing, try COLUMNS!
            temp = parameters.remove(Commands.TOKEN_COLUMNS);
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

        temp = parameters.remove( Commands.TOKEN_ROWS );
        if (temp == null)
            {
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_rows_missing );
            return;
            }
        rows = (int)temp;

        wide = (boolean)parameters.remove(Commands.TOKEN_WIDE, false);

        oddRowsAligned = true; // default: ODDS_ALIGNED
        long alignFlag = (long)parameters.remove(Commands.TOKEN_ALIGN, -1L);
        if ( alignFlag == Commands.TOKEN_ODDS )
            ; // default remains
        else if ( alignFlag == Commands.TOKEN_EVENS )
            oddRowsAligned = false;
        else if ( alignFlag != -1L )
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_align_bad_parameter );

        color = (int)parameters.remove(Commands.TOKEN_COLOR, DEFAULT_BOARD_COLOR);

        // missing token (null) is interpreted as IGNORE
        metaStates[ BoardStates.META_SHIFT ] =
                Trilean.valueOf((Boolean)parameters.remove( Commands.TOKEN_FORCESHIFT ));
        metaStates[ BoardStates.META_CTRL ] =
                Trilean.valueOf((Boolean) parameters.remove(Commands.TOKEN_FORCECTRL));
        metaStates[ BoardStates.META_ALT ] =
                Trilean.valueOf((Boolean)parameters.remove( Commands.TOKEN_FORCEALT ));
        metaStates[ BoardStates.META_CAPS ] =
                Trilean.valueOf((Boolean)parameters.remove( Commands.TOKEN_FORCECAPS ));

        try
            {
            Board board = new Board(softBoardData, halfColumns, rows, oddRowsAligned, wide, color, metaStates );

            // needed only by debugging purposes
            board.setBoardId( id );

            // but the new board which will be included, cursor set to the first position
            BoardPlan boardPlan = new BoardPlan(board);
            boardPlans.put(id, boardPlan);

            tokenizer.note( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_board_added,
                    board.toString());

            // the first non-wide board is stored
            if ( softBoardData.firstBoard == null && !wide )
                {
                softBoardData.firstBoard = board;
                }

            }
        catch (ExternalDataException ede)
            {
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_board_error );
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
            Packet secondaryPacket = (Packet)parameters.remove( Commands.TOKEN_SECOND );

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

        temp = parameters.remove( Commands.TOKEN_LINK );
        if (temp != null)
            {
            counter++;
            buttonFunction = new ButtonLink( (int)temp,
                    parameters.containsKey(Commands.TOKEN_LOCK) );
            // invalid index - (int)temp - means go back to previous board
            }

        temp = parameters.remove( Commands.TOKEN_META );
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

        temp = parameters.remove(Commands.TOKEN_MODIFY);
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

        temp = (int)parameters.remove(Commands.TOKEN_KEY, NO_DEFAULT_KEY);

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
            packet = new PacketKey( softBoardData, temp,
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

        temp = parameters.remove(Commands.TOKEN_TEXT);

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

            autoFlag = (long)parameters.remove(Commands.TOKEN_AUTOCAPS, -1L);
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

            autoFlag = (long)parameters.remove(Commands.TOKEN_AUTOSPACE, -1L);
            if ( autoFlag == Commands.TOKEN_BEFORE )
                autoSpace = PacketText.AUTO_SPACE_BEFORE;
            else if ( autoFlag == Commands.TOKEN_AFTER )
                autoSpace = PacketText.AUTO_SPACE_AFTER;
            else if ( autoFlag == Commands.TOKEN_AROUND )
                autoSpace = PacketText.AUTO_SPACE_BEFORE | PacketText.AUTO_SPACE_AFTER;
            else if ( autoFlag != -1L )
                tokenizer.error("PACKET", R.string.data_autospace_bad_parameter );

            autoFlag = (long)parameters.remove(Commands.TOKEN_ERASESPACES, -1L);
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
                packet = new PacketText( softBoardData, (Character)temp, autoCaps,
                        autoSpace );
                }
            else // if (temp instanceof String)
                {
                packet = new PacketText( softBoardData, (String)temp, autoCaps,
                        (boolean)parameters.remove( Commands.TOKEN_STRINGCAPS, false), autoSpace );
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

        temp = (long)parameters.remove(Commands.TOKEN_DO, -1L);

        if ( temp != -1L )
            {
            try
                {
                packet = new PacketFunction( softBoardData, temp );
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

        packet = packetText(parameters, null);

        if ( packet == null )
            packet = packetKey( parameters, NO_DEFAULT_KEY );

        if ( packet == null )
            packet = packetFunction(parameters);

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

        packet = packetText(parameters, null);

        if ( packet == null )
            packet = packetKey(parameters, defaultKey);

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

        packet = packetKey(parameters, NO_DEFAULT_KEY);

        if ( packet == null )
            packet = packetText(parameters, defaultText);

        return packet;
        }


    public TitleDescriptor addTitle( ExtendedMap<Long, Object> parameters )
        {
        // !! http://stackoverflow.com/questions/509076/how-do-i-address-unchecked-cast-warnings
        // Maybe better to avoid Unchecked cast warnings

        // text is optional, if it is null, then button's getString() function will be used
        String text = null;
        Object temp = parameters.remove(Commands.TOKEN_TEXT);
        if ( temp != null )
            {
            text = SoftBoardParser.stringFromText( temp );
            }

        int xOffset = (int)parameters.remove(Commands.TOKEN_XOFFSET, 0);
        int yOffset = (int)parameters.remove(Commands.TOKEN_YOFFSET, 0);
        int size = (int)parameters.remove(Commands.TOKEN_SIZE, 1000);
        boolean bold = (boolean)parameters.remove(Commands.TOKEN_BOLD, false);
        boolean italics = (boolean)parameters.remove(Commands.TOKEN_ITALICS, false);
        int color = (int)parameters.remove(Commands.TOKEN_COLOR, Color.BLACK);

        return new TitleDescriptor( text, xOffset, yOffset, size, bold, italics, color );
        }


    /**
     * Inserts button parameters into absolute positions
     * @param parameters
     */
    public void setBlock(ExtendedMap<Long, Object> parameters)
        {
        Long boardId = (Long)parameters.remove( Commands.TOKEN_BOARD );
        if (boardId == null)
            {
            tokenizer.error( "CURSOR", R.string.data_board_missing );
            return;
            }

        // starting (HOME) positions
        int homeArrayColumn = (int)parameters.remove(Commands.TOKEN_COLUMN, 1) -1;
        int homeArrayRow = (int)parameters.remove(Commands.TOKEN_ROW, 1) -1;

        // TOKEN_BUTTON is a group code
        // SetSignedBit states, that this will be a multiple parameter (ArrayList of KeyValuePairs)
        ArrayList<KeyValuePair> actionList = (ArrayList<KeyValuePair>)parameters.remove(
                Bit.setSignedBitOn( Commands.TOKEN_BUTTON ) );

        if ( actionList == null )
            {
            Scribe.error( Debug.PARSER, "BLOCK: is EMPTY!!");
            return;
            }

        BoardPlan boardPlan = boardPlans.get( boardId );
        if ( boardPlan == null )
            {
            Scribe.error( Debug.PARSER, "BLOCK: board is missing!!");
            return;
            }
        Board board = boardPlan.board;

        // automatic movement
        boolean autoMove = false;

        // button positions
        int arrayColumn = homeArrayColumn;
        int arrayRow = homeArrayRow;

        // beginning of the line in the actual row
        int crArrayColumn = homeArrayColumn;

        for ( KeyValuePair action : actionList )
            {
            if ( action.getKey() == Commands.TOKEN_BUTTON )
                {
                if ( autoMove ) arrayColumn ++;

                try
                    {
                    if (board.addButton(
                            arrayColumn,
                            arrayRow,
                            ((ButtonPlan) action.getValue()).button.clone()))
                        {
                        if ((boolean)parameters.remove(Commands.TOKEN_OVERWRITE, false))
                            {
                            tokenizer.note(R.string.data_button_overwritten,
                                    boardPlan.toString());
                            }
                        else
                            {
                            tokenizer.error(R.string.data_button_overwritten,
                                    boardPlan.toString());
                            }
                        }
                    tokenizer.note( ((ButtonPlan)action.getValue()).buttonName, R.string.data_button_added,
                            boardPlan.toString());
                    }
                catch (ExternalDataException ede)
                    {
                    tokenizer.error( ((ButtonPlan)action.getValue()).buttonName, R.string.data_button_error,
                            boardPlan.toString());
                    }

                autoMove = true;
                continue;
                }
            else if ( action.getKey() == Commands.TOKEN_EXTEND )
                {
                if ( autoMove ) arrayColumn ++;

                Button button = null;
                try
                    {
                    button = board.getButton( arrayColumn, arrayRow );
                    }
                catch ( ExternalDataException ede)
                    {
                    ; // Nothing to do, button remains null, which elevates an error
                    }

                if ( button == null )
                    {
                    tokenizer.error( R.string.data_button_not_exist );
                    }
                else
                    {
                    ButtonExtension buttonExtension = (ButtonExtension)action.getValue();

                    if ( buttonExtension.color != null )
                        {
                        button.setColor( buttonExtension.color );
                        tokenizer.note( R.string.data_button_color_changed );
                        }

                    if ( buttonExtension.titleList != null )
                        {
                        SinglyLinkedList<TitleDescriptor> titles =
                                new SinglyLinkedList<>( button.getTitles() );

                        for ( KeyValuePair title : buttonExtension.titleList )
                            {
                            TitleDescriptor titleDescriptor =
                                    ((TitleDescriptor)title.getValue()).copy();
                            titleDescriptor.checkText( button.getString() );
                            titles.add( titleDescriptor );
                            }

                        button.setTitles(titles);
                        tokenizer.note(R.string.data_button_color_changed);
                        }
                    }

                autoMove = true;
                continue;
                }

            // all others are moving commands, no autoMove is needed
            autoMove = false;

            if ( action.getKey() == Commands.TOKEN_CRL )
                {
                if ( (arrayRow + board.rowsAlignOffset) % 2 == 0 )
                    crArrayColumn--;
                arrayColumn = crArrayColumn;
                arrayRow++;
                }
            else if ( action.getKey() == Commands.TOKEN_CRR )
                {
                if ( (arrayRow + board.rowsAlignOffset) % 2 == 1 )
                    crArrayColumn++;
                arrayColumn = crArrayColumn;
                arrayRow++;
                }
            else if ( action.getKey() == Commands.TOKEN_HOME )
                {
                arrayColumn = homeArrayColumn;
                arrayRow = homeArrayRow;
                crArrayColumn = homeArrayColumn;
                }
            else if ( action.getKey() == Commands.TOKEN_L )
                {
                arrayColumn--;
                }
            else if ( action.getKey() == Commands.TOKEN_R )
                {
                arrayColumn++;
                }
            else if ( action.getKey() == Commands.TOKEN_DL )
                {
                if ( (arrayRow + board.rowsAlignOffset) % 2 == 0 )
                    arrayColumn--;
                arrayRow++;
                }
            else if ( action.getKey() == Commands.TOKEN_DR )
                {
                if ( (arrayRow + board.rowsAlignOffset) % 2 == 1 )
                    arrayColumn++;
                arrayRow++;
                }
            else if ( action.getKey() == Commands.TOKEN_UL )
                {
                if ( (arrayRow + board.rowsAlignOffset) % 2 == 0 )
                    arrayColumn--;
                arrayRow--;
                }
            else if ( action.getKey() == Commands.TOKEN_UR )
                {
                if ( (arrayRow + board.rowsAlignOffset) % 2 == 1 )
                    arrayColumn++;
                arrayRow--;
                }
            else if ( action.getKey() == Commands.TOKEN_FINDFREE )
                {
                int occupied;
                while ( ( occupied = board.checkButton( arrayColumn, arrayRow )) != Board.POSITION_WHOLE_HEXAGON
                        && occupied != Board.POSITION_INVALID )
                    {
                    if (occupied == Board.POSITION_LINE_ENDED)
                        {
                        arrayRow++;
                        arrayColumn = 0;
                        }
                    else
                        {
                        arrayColumn++;
                        }
                    }
                }
            // this is a skip (int)
            else if (action.getKey() == Commands.TOKEN_SKIP )
                {
                arrayColumn += (int)action.getValue() + ( (int)action.getValue() < 0 ? -1 : 1 );
                }
            }
        }


    /**
     * Creates temporary button data, which is copied into button position in setBlock
     * @param parameters
     * @return
     */
    public ButtonPlan setButton(ExtendedMap<Long, Object> parameters)
        {
        ButtonPlan buttonPlan = new ButtonPlan();

        // "SEND" parameters could be found among "BUTTON"-s parameters
        // For testing reasons SEND remains...
        Object temp = parameters.remove( Commands.TOKEN_SEND );
        if ( temp != null )
            {
            buttonPlan.button = (Button) temp;
            }
        // ...but if SEND is missing, then parameters are submitted directly
        else
            {
            buttonPlan.button = createButtonFunction(parameters);
            }

        if ( buttonPlan.button == null )
            {
            tokenizer.error( "BUTTON", R.string.data_button_function_missing);
            buttonPlan.button = new Button();
            }

        buttonPlan.button.setColor((int) parameters.remove(Commands.TOKEN_COLOR, DEFAULT_BUTTON_COLOR));

        // TOKEN_ADDTITLE is a group code
        // SetSignedBit states, that this will be a multiple parameter (ArrayList of KeyValuePairs)
        ArrayList<KeyValuePair> titleList = (ArrayList<KeyValuePair>)parameters.remove(
                Bit.setSignedBitOn( Commands.TOKEN_ADDTITLE ) );

        SinglyLinkedList<TitleDescriptor> titles = new SinglyLinkedList<>();

        if ( titleList != null )
            {
            for (KeyValuePair title : titleList)
                {
                titles.add((TitleDescriptor) title.getValue());
                }
            }
        else
            {
            // if no titles are added, then addTitle will add one based on default titleSlot
            // if no default exist (impossible situation!) then an empty parameter list is needed
            ExtendedMap<Long, Object> defaultTitle;
            if (defaults.containsKey(Commands.TOKEN_ADDTITLE))
                {
                defaultTitle = (ExtendedMap<Long, Object>) (defaults.get(Commands.TOKEN_ADDTITLE)).clone();
                }
            else
                {
                defaultTitle = new ExtendedMap<Long, Object>(0);
                }
            titles.add(addTitle(defaultTitle));
            }

        // if title text is null, code should be used
        // button id can be created from the titles (and from the code)
        StringBuilder buttonNameBuilder = new StringBuilder();
        for ( TitleDescriptor title : titles )
            {
            title.checkText(buttonPlan.button.getString());
            buttonNameBuilder.insert( 0, title.getText() ).insert( 0,'/');
            }
        buttonNameBuilder.setCharAt(0, '\"');
        buttonPlan.buttonName = buttonNameBuilder.append('\"').toString();

        buttonPlan.button.setTitles(titles);

        return buttonPlan;
        }


    public ButtonExtension extendButton( ExtendedMap<Long, Object> parameters )
        {
        ButtonExtension buttonExtension = new ButtonExtension();

        buttonExtension.color = (Integer) parameters.remove( Commands.TOKEN_COLOR );

        buttonExtension.titleList = (ArrayList<KeyValuePair>) parameters
                .remove(Bit.setSignedBitOn(Commands.TOKEN_ADDTITLE));

        return buttonExtension;
        }


    public void addLink( ExtendedMap<Long, Object> parameters )
        {
        Integer index = (Integer)parameters.remove( Commands.TOKEN_INDEX );
        if (index == null)
            {
            tokenizer.error("ADDLINK", R.string.data_addlink_no_index );
            return;
            }

        // BOARD is given, no other parameters are checked
        Long boardId = (Long)parameters.remove( Commands.TOKEN_BOARD );
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
                softBoardData.linkState.setLinkBoardTable( index, boardPlan.board );

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

            Long portraitId = (Long)parameters.remove( Commands.TOKEN_PORTRAIT );
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

            Long landscapeId = (Long)parameters.remove( Commands.TOKEN_LANDSCAPE );
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
                    softBoardData.linkState.setLinkBoardTable( index, portrait, landscape );

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

        id = (Long) parameters.remove( Commands.TOKEN_ID );
        if ( id == null )
            {
            tokenizer.error("ADDMODIFY", R.string.data_modify_no_id );
            return;
            }

        Modify mod = null;
        boolean empty = true;
        int counter = 0;

        // ADDROLL-s are used!
        if ( tempRolls.size() > 0)
            {
            empty = true;
            counter ++;

            mod = new ModifyText( softBoardData.softBoardListener,
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
        List<Object> rolls = (List) parameters.remove( Commands.TOKEN_ROLLS );
        if ( rolls != null )
            {
            empty = true;
            counter++;

            mod = new ModifyChar( softBoardData.softBoardListener,
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

        if ( softBoardData.modify.get( id ) != null )
            {
            tokenizer.error( Tokenizer.regenerateKeyword( id ),
                    R.string.data_modify_overwritten );
            }

        softBoardData.modify.put( id, mod );

        tokenizer.note( Tokenizer.regenerateKeyword( id ),
                R.string.data_modify_added );
        }

    }

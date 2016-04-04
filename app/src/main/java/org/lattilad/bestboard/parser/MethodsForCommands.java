package org.lattilad.bestboard.parser;

import android.graphics.Color;
import android.view.KeyEvent;

import org.lattilad.bestboard.Layout;
import org.lattilad.bestboard.R;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.buttons.Button;
import org.lattilad.bestboard.buttons.ButtonAlternate;
import org.lattilad.bestboard.buttons.ButtonDouble;
import org.lattilad.bestboard.buttons.ButtonEnter;
import org.lattilad.bestboard.buttons.ButtonList;
import org.lattilad.bestboard.buttons.ButtonMainTouch;
import org.lattilad.bestboard.buttons.ButtonMeta;
import org.lattilad.bestboard.buttons.ButtonModify;
import org.lattilad.bestboard.buttons.ButtonSingle;
import org.lattilad.bestboard.buttons.ButtonSpaceTravel;
import org.lattilad.bestboard.buttons.ButtonSwitch;
import org.lattilad.bestboard.buttons.Packet;
import org.lattilad.bestboard.buttons.PacketCombine;
import org.lattilad.bestboard.buttons.PacketFunction;
import org.lattilad.bestboard.buttons.PacketKey;
import org.lattilad.bestboard.buttons.PacketText;
import org.lattilad.bestboard.buttons.PacketTextTime;
import org.lattilad.bestboard.buttons.TitleDescriptor;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.modify.Modify;
import org.lattilad.bestboard.modify.ModifyChar;
import org.lattilad.bestboard.modify.ModifyText;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.utils.Bit;
import org.lattilad.bestboard.utils.ExtendedMap;
import org.lattilad.bestboard.utils.ExternalDataException;
import org.lattilad.bestboard.utils.KeyValuePair;
import org.lattilad.bestboard.utils.SinglyLinkedList;
import org.lattilad.bestboard.utils.Trilean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
     * Buttons can be extended after definition
     */
    private class ButtonExtension
        {
        private Integer color = null;

        private ArrayList<KeyValuePair> titleList = null;

        // Can be TOKEN_DOUBLE TOKEN_ALTERNATE OR TOKEN_LIST
        private Long type = null;
        // SECONDARY packet (DOUBLE OR ALTERNATE)
        // List of ADD packets (LIST)
        private Object data = null;

        private boolean onCircle = false;
        private boolean onStay = false;
        }


    public void createDefaults()
        {
        /*
        ExtendedMap<Long, Object> defaultTitle = new ExtendedMap<>();
        defaultTitle.put( Commands.TOKEN_YOFFSET, 250 ); // PARAMETER_INT
        defaultTitle.put( Commands.TOKEN_SIZE, 1200 ); // PARAMETER_INT
        defaultTitle.put( Commands.TOKEN_COLOR, Color.BLACK ); // PARAMETER_COLOR (int)

        defaults.put( Commands.TOKEN_ADDTITLE, defaultTitle );
        */
        }


    /**
     ** TEMPORARY VARIABLES NEEDED ONLY BY THE PARSING PHASE
     **/

    /** if no default key is given for packetKey */
    public static final int NO_DEFAULT_KEY = -1;

    /** Layout's default background */
    public static final int DEFAULT_LAYOUT_COLOR = Color.LTGRAY;

    /** Button's default background */
    public static final int DEFAULT_BUTTON_COLOR = Color.LTGRAY;

    /** Map of temporary layouts, identified by code of keywords */
    public Map<Long, Layout> layouts = new HashMap<>();

    /** Typeface will be set at the and of parseSoftBoard, both for titledescriptor and layouts */
    public File typefaceFile = null;


    /**
     ** SETTERS CALLED ONLY BY PARSING PHASE
     **/

    /** Set softboard's name * NAME (string) */
    public void setName( Object stringParameter )
        {
        softBoardData.name = (String) stringParameter;
        tokenizer.note(R.string.data_name, softBoardData.name );
        }

    /** Set softboard's version * VERSION (int) */
    public void setVersion( Object intParameter )
        {
        softBoardData.version = (int)intParameter;
        tokenizer.note(R.string.data_version, String.valueOf(softBoardData.version));
        }

    /** Set softboard's author * AUTHOR (string) */
    public void setAuthor( Object stringParameter )
        {
        softBoardData.author = (String)stringParameter;
        tokenizer.note(R.string.data_author, softBoardData.author );
        }

    /** Add softboard's tags * ADDTAGS (string-list) */
    public void addTags( List<Object> stringListParameter )
        {
        // PARAMETER_STRING_LIST gives only non-null String items
        for (Object item: stringListParameter)
            {
            softBoardData.tags.add( (String) item );
            tokenizer.note( R.string.data_tags, (String) item );
            }
        }

    /** Set softboard's short description * DESCRIPTION (string) */
    public void setDescription( Object stringParameter )
        {
        softBoardData.description = (String)stringParameter;
        tokenizer.note(R.string.data_description, softBoardData.description );
        }

    /**
     * Set file name of softboard's document (should be in the same directory) - if available
     * DocFile is not checked, just stored !!
     * DOCFILE (file)
     */
    public void setDocFile( Object fileParameter )
        {
        softBoardData.docFile = (File)fileParameter;
        tokenizer.note(R.string.data_docfile, softBoardData.docFile.toString() );
        }

    /**
     * Set full URI of softboard's document - if available
     * DocUri is not checked, just stored !!
     * DOCURI (string)
     */
    public void setDocUri( Object stringParameter )
        {
        softBoardData.docUri = (String)stringParameter;
        tokenizer.note(R.string.data_docuri, softBoardData.docUri );
        }

    /**
     * Set softboard's locale
     * Locale is not checked, just set !!
     * LOCALE ( LANGUAGE (string) TOKEN_COUNTRY (string) TOKEN_VARIANT (string) )
     */
    public void setLocale( ExtendedMap<Long, Object> parameters )
        {
        String language = (String)parameters.remove(Commands.TOKEN_LANGUAGE, "");
        String country = (String)parameters.remove(Commands.TOKEN_COUNTRY, "");
        String variant = (String)parameters.remove(Commands.TOKEN_VARIANT, "");

        softBoardData.locale = new Locale( language, country, variant);
        tokenizer.note(R.string.data_locale, String.valueOf(softBoardData.locale) );
        }

    /** Set color of touched meta keys * METACOLOR (color) */
    public void setMetaColor(Object colorParameter)
        {
        softBoardData.metaColor = (int)colorParameter;
        tokenizer.note(R.string.data_metacolor, Integer.toHexString( softBoardData.metaColor));
        }

    /** Set color of locked meta keys * LOCKCOLOR (color) */
    public void setLockColor(Object colorParameter)
        {
        softBoardData.lockColor = (int)colorParameter;
        tokenizer.note(R.string.data_lockcolor, Integer.toHexString( softBoardData.lockColor));
        }

    /** Set color of locked meta keys * AUTOCOLOR (color) */
    public void setAutoColor(Object colorParameter)
        {
        softBoardData.autoColor = (int)colorParameter;
        tokenizer.note(R.string.data_autocolor, Integer.toHexString( softBoardData.autoColor));
        }

    /** Set color of touched button * TOUCHCOLOR (color) */
    public void setTouchColor(Object colorParameter)
        {
        softBoardData.touchColor = (int)colorParameter;
        tokenizer.note(R.string.data_touchcolor, Integer.toHexString( softBoardData.touchColor));
        }

    /** Set color of stroke * STROKECOLOR (color) */
    public void setStrokeColor(Object colorParameter)
        {
        softBoardData.strokeColor = (int)colorParameter;
        tokenizer.note(R.string.data_strokecolor, Integer.toHexString( softBoardData.strokeColor));
        }

    /** Set typeface of title font from file * TITLEFONT (file) */
    public void setTypeface( Object fileParameter )
        {
        typefaceFile = (File)fileParameter;
        /* This should be performed at the end of the parsing process - softBoardParser
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
        */
        }

    /** Set entertitle * ENTERTITLE (text) */
    public void setEnterTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_MULTILINE] = SoftBoardParser.stringFromText(textParameter);
        tokenizer.note(R.string.data_entertitle, softBoardData.actionTitles[SoftBoardData.ACTION_MULTILINE] );
        }

    /** Set gotitle * GOTITLE (text) */
    public void setGoTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_GO] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_gotitle, softBoardData.actionTitles[SoftBoardData.ACTION_GO] );
        }

    /** Set searchtitle * SEARCHTITLE (text) */
    public void setSearchTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_SEARCH] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_searchtitle, softBoardData.actionTitles[SoftBoardData.ACTION_SEARCH] );
        }

    /** Set sendtitle * SENDTITLE (text) */
    public void setSendTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_SEND] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_sendtitle, softBoardData.actionTitles[SoftBoardData.ACTION_SEND] );
        }

    /** Set nexttitle * NEXTTITLE (text) */
    public void setNextTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_NEXT] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_nexttitle, softBoardData.actionTitles[SoftBoardData.ACTION_NEXT] );
        }

    /** Set donetitle * DONETITLE (text) */
    public void setDoneTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_DONE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_donetitle, softBoardData.actionTitles[SoftBoardData.ACTION_DONE] );
        }

    /** Set prevtitle * PREVTITLE (text) */
    public void setPrevTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_PREVIOUS] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_prevtitle, softBoardData.actionTitles[SoftBoardData.ACTION_PREVIOUS] );
        }

    /** Set nonetitle * NONETITLE (text) */
    public void setNoneTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_NONE] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_nonetitle, softBoardData.actionTitles[SoftBoardData.ACTION_NONE] );
        }

    /** Set unknowntitle * UNKNOWNTITLE (text) */
    public void setUnknownTitle( Object textParameter )
        {
        softBoardData.actionTitles[SoftBoardData.ACTION_UNSPECIFIED] = SoftBoardParser.stringFromText( textParameter );
        tokenizer.note(R.string.data_unknowntitle, softBoardData.actionTitles[SoftBoardData.ACTION_UNSPECIFIED] );
        }


    /**
     * Adds a new board - portrait-landscape layout pair
     * ID (keyword) - obligatory board id
     * LAYOUT (keyword) - layout to use in both modes OR
     * PORTRAIT (keyword) - layout to use in portrait mode AND
     * LANDSCAPE (keyword) - layout to use in landscape mode
     * LOCK - (flag) board always starts as locked
     * ROOT - (flag) set as root board (root board is always locked)
     */
    public void addBoard( ExtendedMap<Long, Object> parameters )
        {
        Long id = (Long)parameters.remove( Commands.TOKEN_ID );
        if (id == null)
            {
            tokenizer.error( "ADDBOARD", R.string.data_board_no_id);
            return;
            }

        boolean root = (boolean)parameters.remove( Commands.TOKEN_START, false );
        boolean locked = (boolean)parameters.remove( Commands.TOKEN_LOCK, root );

        // LAYOUT is given, no other parameters are checked
        Long layoutId = (Long)parameters.remove( Commands.TOKEN_LAYOUT );

        if (layoutId != null)
            {
            Layout layout = layouts.get( layoutId );
            if ( layout == null )
                {
                tokenizer.error( "LAYOUT", R.string.data_no_layout,
                        Tokenizer.regenerateKeyword( (long)layoutId));
                return;
                }

            if ( softBoardData.boardTable.addBoard(id, layout, locked) )
                {
                tokenizer.error(Tokenizer.regenerateKeyword(id), R.string.data_board_id_overwritten);
                }

            tokenizer.note( Tokenizer.regenerateKeyword(id), R.string.data_board_id_set,
                    Tokenizer.regenerateKeyword( (long)layoutId));
            }

        // no LAYOUT is given, so PORTRAIT AND LANDSCAPE is needed
        // BOTH parameters are checked completely before
        else
            {
            Long portraitId = (Long)parameters.remove( Commands.TOKEN_PORTRAIT );
            Layout portrait = null;

            if ( portraitId != null )
                {
                portrait = layouts.get( portraitId );
                if ( portrait == null )
                    {
                    tokenizer.error( "PORTRAIT", R.string.data_no_layout,
                            Tokenizer.regenerateKeyword( (long)portraitId));
                    }
                }
            else
                {
                tokenizer.error( Tokenizer.regenerateKeyword(id), R.string.data_board_portrait_missing);
                }

            Long landscapeId = (Long)parameters.remove( Commands.TOKEN_LANDSCAPE );
            Layout landscape = null;

            if ( landscapeId != null )
                {
                landscape = layouts.get( landscapeId );
                if ( landscape == null )
                    {
                    tokenizer.error( "LANDSCAPE", R.string.data_no_layout,
                            Tokenizer.regenerateKeyword( (long)landscapeId));
                    }
                }
            else
                {
                tokenizer.error( Tokenizer.regenerateKeyword(id), R.string.data_board_landscape_missing);
                }

            // only if both parameters are ok
            if ( portrait == null || landscape == null )
                return;

            if ( softBoardData.boardTable.addBoard(id, portrait, landscape, locked) )
                {
                tokenizer.error(Tokenizer.regenerateKeyword(id), R.string.data_board_id_overwritten);
                }
            }

        if (root)
            {
            // root remains always "root" and always locked
            softBoardData.boardTable.defineRootBoard( id );
            }
        }


    /**
     * Adds a new layout
     * ID (keyword) - obligatory layout id
     * HALFCOLUMNS (int) or COLUMNS (int) - obligatory layout width
     * ROWS (int) - obligatory layout height
     * HEXAGONAL - (flag) currently all layouts are hexagonal
     * WIDE - (flag) default: non-wide. Wide layouts can be displayed only in landscape mode
     * ALIGN (keyword: ODDS or EVENS) - default: ODDS. Odds or even rows start whit whole hexagons.
     * COLOR (color) - default: light-gray
     * FORCESHIFT (boolean) FORCECTRL (boolean) FORCEALT  (boolean) FORCECAPS (boolean) -
     * Trilean values, default: not-given-value. Otherwise META state is forced on or off, depending on the value.
     * ASBOARD - new board is generated with this layout under the same id
     */
    public void addLayout( ExtendedMap<Long, Object> parameters )
        {
        Object temp;

        Long id;

        int halfColumns; // obligate parameter
        int rows; // obligate parameter
        boolean wide; // default false
        boolean oddRowsAligned; // default: EVENS ALIGNED
        int color; // default: defaultBoardColor
        Trilean[] metaStates = new Trilean[ LayoutStates.META_STATES_SIZE ]; // default: IGNORED

        id = (Long)parameters.remove( Commands.TOKEN_ID );
        if (id == null)
            {
            tokenizer.error( "ADDLAYOUT", R.string.data_layout_no_id);
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

        // optional parameter should be deleted
        parameters.remove( Commands.TOKEN_HEXAGONAL );

        wide = (boolean)parameters.remove(Commands.TOKEN_WIDE, false);

        oddRowsAligned = true; // default: ODDS
        long alignFlag = (long)parameters.remove(Commands.TOKEN_ALIGN, -1L);
        if ( alignFlag == Commands.TOKEN_ODDS )
            ; // default remains
        else if ( alignFlag == Commands.TOKEN_EVENS )
            oddRowsAligned = false;
        else if ( alignFlag != -1L )
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_align_bad_parameter );

        color = (int)parameters.remove(Commands.TOKEN_COLOR, DEFAULT_LAYOUT_COLOR);

        // missing token (null) is interpreted as IGNORE
        metaStates[ LayoutStates.META_SHIFT ] =
                Trilean.valueOf((Boolean)parameters.remove( Commands.TOKEN_FORCESHIFT ));
        metaStates[ LayoutStates.META_CTRL ] =
                Trilean.valueOf((Boolean) parameters.remove(Commands.TOKEN_FORCECTRL));
        metaStates[ LayoutStates.META_ALT ] =
                Trilean.valueOf((Boolean)parameters.remove( Commands.TOKEN_FORCEALT ));
        metaStates[ LayoutStates.META_CAPS ] =
                Trilean.valueOf((Boolean)parameters.remove( Commands.TOKEN_FORCECAPS ));

        try
            {
            Layout layout = new Layout(softBoardData, halfColumns, rows, oddRowsAligned, wide, color, metaStates );

            // needed only by debugging purposes
            layout.setLayoutId(id);

            if ( layouts.put(id, layout) != null )
                {
                tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                        R.string.data_layout_overwritten );
                }

            tokenizer.note( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_layout_added,
                    layout.toString());

            // the first non-wide layout is stored
            if ( softBoardData.firstLayout == null && !wide )
                {
                softBoardData.firstLayout = layout;
                }

            // ASBOARD - a new board is defined, same id, only one layout
            if ( (boolean)parameters.remove( Commands.TOKEN_ASBOARD, false ) )
                {
                // !! check if non-wide !!
                parameters.put( Commands.TOKEN_ID, id );
                parameters.put( Commands.TOKEN_LAYOUT, id);
                addBoard( parameters );
                }

            }
        catch (ExternalDataException ede)
            {
            tokenizer.error( Tokenizer.regenerateKeyword( (long)id),
                    R.string.data_layout_error);
            }
        }


    /**
     * Inserts button parameters into absolute positions
     * @param parameters
     */
    public void setBlock(ExtendedMap<Long, Object> parameters)
        {
        Long layoutId = (Long)parameters.remove( Commands.TOKEN_LAYOUT );
        if (layoutId == null)
            {
            tokenizer.error( "CURSOR", R.string.data_layout_missing);
            return;
            }

        // starting (HOME) positions - rows should be adjusted after getting layout
        int homeArrayColumn = (int)parameters.remove(Commands.TOKEN_COLUMN, 1) -1;
        int homeArrayRow = (int)parameters.remove(Commands.TOKEN_ROW, 1);

        // TOKEN_BUTTON is a group code
        // SetSignedBit states, that this will be a multiple parameter (ArrayList of KeyValuePairs)
        ArrayList<KeyValuePair> actionList = (ArrayList<KeyValuePair>)parameters.remove(
                Bit.setSignedBitOn( Commands.TOKEN_BUTTON ) );

        if ( actionList == null )
            {
            Scribe.error( Debug.PARSER, "BLOCK: is EMPTY!!");
            return;
            }

        Layout layout = layouts.get( layoutId );
        if ( layout == null )
            {
            Scribe.error(Debug.PARSER, "BLOCK: layout is missing!!");
            return;
            }

        // ROW: negative values should be counted from the bottom
        if ( homeArrayRow < 0 )
            {
            homeArrayRow = layout.layoutHeightInHexagons + homeArrayRow;
            }
        else
            {
            homeArrayRow--;
            }

        // automatic movement
        boolean autoMove = false;

        // button positions
        int arrayColumn = homeArrayColumn;
        int arrayRow = homeArrayRow;

        Scribe.debug( Debug.BLOCK, "Block starts at column: " + arrayColumn + ", row: " + arrayRow );

        // beginning of the line in the actual row
        int crArrayColumn = homeArrayColumn;

        for ( KeyValuePair action : actionList )
            {
            // Every type of button contains the same Button type value
            if ( action.getValue() instanceof Button )
                {
                if ( autoMove )
                    {
                    arrayColumn ++;
                    Scribe.debug( Debug.BLOCK, "Automove to next column.");
                    }

                Scribe.debug( Debug.BLOCK, "BUTTON. Column: " + arrayColumn + ", row: " + arrayRow );

                try
                    {
                    if (layout.addButton(
                            arrayColumn,
                            arrayRow,
                            ((Button) action.getValue()).clone()))
                        {
                        if ((boolean)parameters.remove(Commands.TOKEN_OVERWRITE, false))
                            {
                            tokenizer.note(R.string.data_button_overwritten,
                                    layout.toString());
                            }
                        else
                            {
                            tokenizer.error(R.string.data_button_overwritten,
                                    layout.toString());
                            }
                        }
                    tokenizer.note( ((Button)action.getValue()).name, R.string.data_button_added,
                            layout.toString());
                    }
                catch (ExternalDataException ede)
                    {
                    tokenizer.error( ((Button)action.getValue()).name, R.string.data_button_error,
                            layout.toString());
                    }

                autoMove = true;
                continue;
                }
            else if ( action.getKey() == Commands.TOKEN_EXTEND )
                {
                if ( autoMove )
                    {
                    arrayColumn ++;
                    Scribe.debug( Debug.BLOCK, "Automove to next column.");
                    }

                Scribe.debug( Debug.BLOCK, "EXTEND. Column: " + arrayColumn + ", row: " + arrayRow );

                Button button = null;
                try
                    {
                    button = layout.getButton( arrayColumn, arrayRow );
                    }
                catch ( ExternalDataException ede)
                    {
                    ; // Nothing to do, button remains null, which elevates an error
                    }

                if ( button == null )
                    {
                    tokenizer.error( "EXTEND", R.string.data_button_not_exist );
                    }
                else
                    {
                    ButtonExtension buttonExtension = (ButtonExtension)action.getValue();

                    //
                    if ( buttonExtension.type != null )
                        {
                        boolean error = true;

                        if (button instanceof ButtonSingle)
                            {
                            if (buttonExtension.type == Commands.TOKEN_DOUBLE)
                                {
                                button = ((ButtonSingle) button).extendToDouble((Packet) buttonExtension.data);
                                }
                            else if (buttonExtension.type == Commands.TOKEN_ALTERNATE)
                                {
                                button = ((ButtonSingle) button).extendToAlternate((Packet) buttonExtension.data);
                                }
                            else if (buttonExtension.type == Commands.TOKEN_LIST)
                                {
                                button = ((ButtonSingle) button).extendToList();
                                }
                            // button should be written back
                            try
                                {
                                layout.addButton(arrayColumn, arrayRow, button);
                                error = false;
                                } catch (ExternalDataException e)
                                {
                                ; // positions cannot be invalid
                                }
                            }

                        if (buttonExtension.type == Commands.TOKEN_LIST && button instanceof ButtonList)
                            {
                            for (KeyValuePair packet : (ArrayList<KeyValuePair>) buttonExtension.data)
                                {
                                ((ButtonList) button).addPacket((Packet) packet.getValue());
                                }
                            error = false;
                            }

                        if (error)
                            {
                            tokenizer.error("EXTEND", R.string.data_button_extended_invalid);
                            }
                        }

                    if ( buttonExtension.color != null )
                        {
                        button.setColor( buttonExtension.color );
                        tokenizer.note("EXTEND", R.string.data_button_color_changed);
                        }

                    if ( buttonExtension.titleList != null )
                        {
                        SinglyLinkedList<TitleDescriptor> titles =
                                new SinglyLinkedList<>( button.getTitles() );

                        TitleDescriptor titleDescriptor;
                        for ( KeyValuePair title : buttonExtension.titleList )
                            {
                            titleDescriptor = ((TitleDescriptor)title.getValue());
                            if ( titleDescriptor.getText() == null )
                                {
                                titleDescriptor = titleDescriptor.copy();
                                titleDescriptor.checkText( button.getString());
                                }
                            titles.add( titleDescriptor );
                            }

                        button.setTitles(titles);
                        tokenizer.note( "EXTEND", R.string.data_button_titles_extended);
                        }

                    if ( button instanceof ButtonMainTouch )
                        {
                        if ( buttonExtension.onStay )
                            {
                            ((ButtonMainTouch) button).setOnStay();
                            tokenizer.note("EXTEND", R.string.data_button_extended_onstay);
                            }
                        else if ( buttonExtension.onCircle )
                            {
                            ((ButtonMainTouch) button).setOnCircle();
                            tokenizer.note("EXTEND", R.string.data_button_extended_oncircle);
                            }
                        }
                    }

                autoMove = true;
                continue;
                }
            else if ( action.getKey() == Commands.TOKEN_FINDFREE )
                {
                if ( autoMove )
                    {
                    arrayColumn ++;
                    Scribe.debug( Debug.BLOCK, "Automove to next column.");
                    }

                int occupied;
                while ( ( occupied = layout.checkButton( arrayColumn, arrayRow )) != Layout.POSITION_WHOLE_HEXAGON
                        && occupied != Layout.POSITION_INVALID )
                    {
                    if (occupied == Layout.POSITION_LINE_ENDED)
                        {
                        Scribe.debug( Debug.BLOCK, "Line ended. Column: " + arrayColumn + ", row: " + arrayRow );
                        arrayRow++;
                        arrayColumn = 0;
                        }
                    else
                        {
                        Scribe.debug( Debug.BLOCK, "Button occupied or half. Column: " + arrayColumn + ", row: " + arrayRow );
                        arrayColumn++;
                        }
                    }
                Scribe.debug( Debug.BLOCK, "Free at column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_CRL )
                {
                if ( (arrayRow + layout.rowsAlignOffset) % 2 == 0 )
                    crArrayColumn--;
                arrayColumn = crArrayColumn;
                arrayRow++;
                Scribe.debug( Debug.BLOCK, "CRL. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_CRR )
                {
                if ( (arrayRow + layout.rowsAlignOffset) % 2 == 1 )
                    crArrayColumn++;
                arrayColumn = crArrayColumn;
                arrayRow++;
                Scribe.debug( Debug.BLOCK, "CRR. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_HOME )
                {
                arrayColumn = homeArrayColumn;
                arrayRow = homeArrayRow;
                crArrayColumn = homeArrayColumn;
                Scribe.debug( Debug.BLOCK, "HOME. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_L )
                {
                arrayColumn--;
                Scribe.debug( Debug.BLOCK, "L. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_R )
                {
                arrayColumn++;
                Scribe.debug( Debug.BLOCK, "R. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_DL )
                {
                if ( (arrayRow + layout.rowsAlignOffset) % 2 == 0 )
                    arrayColumn--;
                arrayRow++;
                Scribe.debug( Debug.BLOCK, "DL. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_DR )
                {
                if ( (arrayRow + layout.rowsAlignOffset) % 2 == 1 )
                    arrayColumn++;
                arrayRow++;
                Scribe.debug( Debug.BLOCK, "DR. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_UL )
                {
                if ( (arrayRow + layout.rowsAlignOffset) % 2 == 0 )
                    arrayColumn--;
                arrayRow--;
                Scribe.debug( Debug.BLOCK, "UL. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            else if ( action.getKey() == Commands.TOKEN_UR )
                {
                if ( (arrayRow + layout.rowsAlignOffset) % 2 == 1 )
                    arrayColumn++;
                arrayRow--;
                Scribe.debug( Debug.BLOCK, "UR. Column: " + arrayColumn + ", row: " + arrayRow );
                }
            // this is a skip (int)
            else if (action.getKey() == Commands.TOKEN_SKIP )
                {
                // if autoMove==true, then button was set in this position.
                // if autoMove==false, then this position is still empty
                arrayColumn += (int)action.getValue();
                if ( autoMove )
                    arrayColumn += ( (int)action.getValue() < 0 ? -1 : 1 );
                Scribe.debug( Debug.BLOCK, "Skip + " + (int)action.getValue() + ". Column: " + arrayColumn + ", row: " + arrayRow );
                }

            // BUTTON and EXTEND set autoMove = true and CONTINUE to the next command
            // but all others are moving commands, no autoMove is needed
            autoMove = false;
            }
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

        temp = (int)parameters.remove(Commands.TOKEN_SEND, NO_DEFAULT_KEY);

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
            // TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT
            packet = new PacketKey( softBoardData, temp,
                    LayoutStates.generateBinaryHardState(parameters));
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
            temp = parameters.remove( Commands.TOKEN_TIME );
            }
          
        if ( temp == null )             // TEXT and TIME token is missing
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

            autoFlag = (long)parameters.remove(Commands.TOKEN_ERASESPACE, -1L);
            if ( autoFlag == Commands.TOKEN_BEFORE )
                autoSpace |= PacketText.ERASE_SPACES_BEFORE;
            else if ( autoFlag == Commands.TOKEN_AFTER )
                autoSpace |= PacketText.ERASE_SPACES_AFTER;
            else if ( autoFlag == Commands.TOKEN_AROUND )
                autoSpace |= PacketText.ERASE_SPACES_BEFORE | PacketText.ERASE_SPACES_AFTER;
            else if ( autoFlag != -1L )
                tokenizer.error("PACKET", R.string.data_erasespaces_bad_parameter );

            if (temp instanceof String)
                {
                packet = new PacketText( softBoardData, (String)temp, autoCaps,
                (boolean)parameters.remove( Commands.TOKEN_STRINGCAPS, false), autoSpace );
                }
            else if (temp instanceof Character)
                {
                packet = new PacketText( softBoardData, (Character)temp, autoCaps,
                        autoSpace );
                }
            else // if (temp instanceof Boolean )
            	{
                packet = new PacketTextTime( softBoardData, 
                		(String)parameters.remove(Commands.TOKEN_FORMAT), 
                        autoCaps, autoSpace );
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
            {
            packet = packetFunction(parameters);
            }
        if ( packet == null )
            {
            packet = packetKey(parameters, NO_DEFAULT_KEY);
            }
        else if ( parameters.remove(Commands.TOKEN_COMBINE) != null )
            {
            PacketKey packetKey;

            if ( (packetKey = packetKey(parameters, NO_DEFAULT_KEY)) != null )
                {
                packet = new PacketCombine( softBoardData, packet, packetKey);
                }
            else
                {
                tokenizer.error("COMBINE", R.string.data_packet_combine_missing );
                }
            }

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

    public Packet firstPacket( ExtendedMap<Long, Object> parameters )
        {
        Packet packet = (Packet) parameters.remove(Commands.TOKEN_FIRST);
        return (packet != null) ? packet : packet(parameters);
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

        return new TitleDescriptor(text, xOffset, yOffset, size, bold, italics, color );
        }


    public Button setSwitch( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Switch Button is defined");

        Long boardId = (Long) parameters.remove( Commands.TOKEN_BOARD );
        if ( boardId == null )   return setEmpty( parameters );

        // 'BACK' is a special token: go back to previous board
        return completeButton(
                new ButtonSwitch(boardId, parameters.remove(Commands.TOKEN_LOCK) != null),
                parameters);

        // !! Check non-used SWITCH buttons !!
        }

    public Button setMeta( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Meta Button is defined");

        int meta;

        if ( parameters.remove(Commands.TOKEN_CAPS) != null)
            {
            meta = LayoutStates.META_CAPS;
            }
        else if ( parameters.remove(Commands.TOKEN_SHIFT) != null)
            {
            meta = LayoutStates.META_SHIFT;
            }
        else if ( parameters.remove(Commands.TOKEN_CTRL) != null)
            {
            meta = LayoutStates.META_CTRL;
            }
        else if ( parameters.remove(Commands.TOKEN_ALT) != null)
            {
            meta = LayoutStates.META_ALT;
            }
        else
            {
            tokenizer.error("META", R.string.data_meta_unknown_meta_state);
            return setEmpty( parameters );
            }

        // ButtonMeta constructor will not accept any non-valid parameter
        try
            {
            return completeButton( new ButtonMeta( meta,
                    parameters.remove( Commands.TOKEN_LOCK ) != null ),
                    parameters );
            }
        catch (ExternalDataException e)
            {
            // this is not possible
            return setEmpty( parameters );
            }
        }

    public Button setSpaceTravel( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Space Travel Button is defined");

        // Packet with default cannot be null!
        return completeMainTouchButton( new ButtonSpaceTravel( packet(parameters, " ") ),
                parameters);
        }

    public Button setEnter( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Enter Button is defined");

        // Packet with default cannot be null!
        return completeMainTouchButton( new ButtonEnter(
                packetKey( parameters, 0x10000 + KeyEvent.KEYCODE_ENTER), // Or: '\n'
                packetText( parameters, "\n" ),
                parameters.remove( Commands.TOKEN_REPEAT ) != null ),
                parameters);
        }

    public Button setModify( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Modify Button is defined");

        Long rollId = (Long) parameters.remove( Commands.TOKEN_ROLL );
        if ( rollId == null )   return setEmpty( parameters );

        return completeMainTouchButton(
                new ButtonModify( rollId, parameters.remove( Commands.TOKEN_REVERSE ) != null),
                parameters);
        }

    public Button setList( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "List Button is defined");

        // SetSignedBit states, that this will be a multiple parameter (ArrayList of KeyValuePairs)
        ArrayList<KeyValuePair> packetList = (ArrayList<KeyValuePair>)parameters.remove(
                Bit.setSignedBitOn( Commands.TOKEN_ADD ) );

        if ( packetList == null || packetList.isEmpty() )
            return setEmpty( parameters );

        ButtonList buttonList = new ButtonList();

        for ( KeyValuePair packet: packetList)
            {
            buttonList.addPacket( (Packet)packet.getValue() );
            }

        return completeMainTouchButton( buttonList, parameters);
        }

    public Button setAlternate( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Alternate Button is defined");

        Packet firstPacket = firstPacket( parameters );
        Packet secondPacket = (Packet)parameters.remove( Commands.TOKEN_SECOND );
        if ( firstPacket == null || secondPacket == null )
            return setEmpty( parameters );

        return completeMainTouchButton(new ButtonAlternate(firstPacket, secondPacket), parameters);
        }

    public Button setDouble( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Double Button is defined");

        Packet firstPacket = firstPacket( parameters );
        Packet secondPacket = (Packet)parameters.remove( Commands.TOKEN_SECOND );
        if ( firstPacket == null || secondPacket == null )
            return setEmpty( parameters );

        return completeMainTouchButton(new ButtonDouble(firstPacket, secondPacket), parameters);
        }

    public Button setSingle( ExtendedMap<Long, Object> parameters )
        {
        Scribe.debug(Debug.DATA, "Simple Button is defined");

        Packet packet = firstPacket( parameters );
        if ( packet == null )   return setEmpty( parameters );

        int second = ButtonSingle.CAPITAL; // default behavior
        if ( parameters.remove(Commands.TOKEN_TWIN) != null )
            second = ButtonSingle.TWIN;
        else if ( parameters.remove(Commands.TOKEN_REPEAT) != null )
            second = ButtonSingle.REPEAT;
        else
            parameters.remove(Commands.TOKEN_CAPITAL); // should be removed, too

        return completeMainTouchButton(
                new ButtonSingle(packet, second),
                parameters);
        }

    public Button setButton( ExtendedMap<Long, Object> parameters )
        {
        // Do not consume, just check SECOND package!
        return parameters.containsKey(Commands.TOKEN_SECOND) ?
                setDouble( parameters ) : setSingle( parameters );
        }

    public Button setEmpty( ExtendedMap<Long, Object> parameters )
        {
        tokenizer.error("BUTTON", R.string.data_button_function_missing);
        return completeButton( new Button(), parameters);
        }

    public Button completeMainTouchButton( ButtonMainTouch button, ExtendedMap<Long, Object> parameters )
        {
        if (parameters.remove(Commands.TOKEN_ONCIRCLE) != null)
            {
            button.setOnCircle();
            }
        else if (parameters.remove(Commands.TOKEN_ONSTAY) != null)
            {
            button.setOnStay();
            }

        return completeButton( button, parameters);
        }

    public Button completeButton( Button button, ExtendedMap<Long, Object> parameters )
        {
        button.setColor((int) parameters.remove(Commands.TOKEN_COLOR, DEFAULT_BUTTON_COLOR));

        // TOKEN_ADDTITLE is a group code
        // SetSignedBit states, that this will be a multiple parameter (ArrayList of KeyValuePairs)
        ArrayList<KeyValuePair> titleList = (ArrayList<KeyValuePair>)parameters.remove(
                Bit.setSignedBitOn( Commands.TOKEN_ADDTITLE ) );

        // !! This part after this point could be nicer !!
        SinglyLinkedList<TitleDescriptor> titles = new SinglyLinkedList<>();
        TitleDescriptor titleDescriptor;

        if ( titleList != null )
            {
            for (KeyValuePair title : titleList)
                {
                titleDescriptor = (TitleDescriptor) title.getValue();
                if ( titleDescriptor.getText() == null )
                    {
                    // TitleDescriptor is not completed, depends on the value of the buttons
                    titleDescriptor = titleDescriptor.copy();
                    titleDescriptor.checkText( button.getString() );
                    }
                // Completed titleDescriptor, so one instance is enough for all buttons
                titles.add(titleDescriptor);
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
            titleDescriptor = addTitle(defaultTitle);
            titleDescriptor.checkText( button.getString() );
            titles.add( titleDescriptor );
            }

        // if title text is null, code should be used
        // button id can be created from the titles (and from the code)
        StringBuilder buttonNameBuilder = new StringBuilder();
        for ( TitleDescriptor title : titles )
            {
            buttonNameBuilder.insert( 0, title.getText() ).insert( 0,'/');
            }
        buttonNameBuilder.setCharAt(0, '\"');
        button.name = buttonNameBuilder.append('\"').toString();

        button.setTitles(titles);

        return button;
        }


    public ButtonExtension extendButton( ExtendedMap<Long, Object> parameters )
        {
        ButtonExtension buttonExtension = new ButtonExtension();

        buttonExtension.color = (Integer) parameters.remove( Commands.TOKEN_COLOR );

        buttonExtension.titleList = (ArrayList<KeyValuePair>) parameters
                .remove(Bit.setSignedBitOn(Commands.TOKEN_ADDTITLE));

        // SECOND can be ALTERNATE or (DOUBLE), where DOUBLE is not obligatory
        if ( (buttonExtension.data = parameters.remove( Commands.TOKEN_SECOND )) != null )
            {
            // if DOUBLE exists (with or without ALTERNATE)
            // or DOUBLE NOT exists (and NO ALTERNATE)
            if (parameters.remove(Commands.TOKEN_TODOUBLE) != null ||
                    parameters.remove(Commands.TOKEN_TOALTERNATE) == null)
                {
                buttonExtension.type = Commands.TOKEN_DOUBLE;
                }
            else
                {
                buttonExtension.type = Commands.TOKEN_ALTERNATE;
                }
            }
        // ADD can be (LIST), where LIST is not obligatory
        else if ( (buttonExtension.data = parameters.remove( Bit.setSignedBitOn(Commands.TOKEN_ADD) )) != null )
            {
            parameters.remove( Commands.TOKEN_TOLIST );
            buttonExtension.type = Commands.TOKEN_LIST;
            }

        if ( (parameters.remove( Commands.TOKEN_ONSTAY )) != null )
            {
            buttonExtension.onStay = true;
            }
        else if ( (parameters.remove( Commands.TOKEN_ONCIRCLE )) != null )
            {
            buttonExtension.onCircle = true;
            }

        return buttonExtension;
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
        boolean ignorespace = (parameters.remove( Commands.TOKEN_IGNORESPACE ) != null);

        // ADDROLL-s are used!
        if ( tempRolls.size() > 0)
            {
            empty = true;
            counter ++;

            mod = new ModifyText( softBoardData.softBoardListener, ignorespace );

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

            mod = new ModifyChar( softBoardData.softBoardListener, ignorespace );

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


    public void setMonitor( ExtendedMap<Long, Object> parameters )
        {
        Long layoutId;

        // layout id is obligatory
        if ( (layoutId = (Long)parameters.remove( Commands.TOKEN_LAYOUT )) == null )
            return;

        Layout layout = layouts.get( layoutId );
        if ( layout == null )
            {
            tokenizer.error( "LAYOUT", R.string.data_no_layout,
                    Tokenizer.regenerateKeyword( (long)layoutId));
            return;
            }

        int size = (int)parameters.remove(Commands.TOKEN_SIZE, 1000);
        boolean bold = (boolean)parameters.remove(Commands.TOKEN_BOLD, false);
        boolean italics = (boolean)parameters.remove(Commands.TOKEN_ITALICS, false);
        int color = (int)parameters.remove(Commands.TOKEN_COLOR, Color.BLACK);

        layout.setMonitorText( size, bold, italics, color );
        }

    }

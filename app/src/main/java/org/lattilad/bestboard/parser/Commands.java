package org.lattilad.bestboard.parser;

import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.ExtendedMap;


/**
 * COAT language contains so called parameter-commands.
 * This class stores the data needed for parsing these parameter-commands.
 * Only SoftBoardParser uses this class.
 */
public class Commands
    {
    // Coat version is checked independently at SoftBoardParser.parseSoftBoard()
    public final static long COAT_VERSION = 1000L;
    public static final long TOKEN_COAT = 0xac842L;

    // Special code for first level commands
    public final static long ADDSOFTBOARD = 0x10000L;

    public static final long TOKEN_DEFAULT = 0x7ffa8362fL;
    public static final long TOKEN_LET = 0x1726fL;
    public static final long TOKEN_CHANGE = 0x3388a1fbL;

    // Token codes for complex parameter-commands - POSITIVE VALUES !!
    public static final long TOKEN_NAME = 0x12ff90L;
    public static final long TOKEN_VERSION = 0x12c1c6d964L;
    public static final long TOKEN_AUTHOR = 0x2cc6c114L;
    public static final long TOKEN_ADDTAGS = 0x630918ad6L;
    public static final long TOKEN_DESCRIPTION = 0xe4e74ed03ae9f2L;
    public static final long TOKEN_DOCFILE = 0x828ab14c4L;
    public static final long TOKEN_DOCURI = 0x38749e2bL;

    public static final long TOKEN_LOCALE = 0x598508fdL;
    public static final long TOKEN_LANGUAGE = 0x1d6843c69dbL;
    public static final long TOKEN_COUNTRY = 0x791c65f9aL;
    public static final long TOKEN_VARIANT = 0x12b1368887L;

    public static final long TOKEN_METACOLOR = 0x478ed1032506L;
    public static final long TOKEN_LOCKCOLOR = 0x45300a4413a0L;
    public static final long TOKEN_AUTOCOLOR = 0x229aefe38af0L;
    public static final long TOKEN_TOUCHCOLOR = 0xdb3133d220b23L;
    public static final long TOKEN_STROKECOLOR = 0x1ec12db0915dd7dL;
    public static final long TOKEN_TITLEFONT = 0x5e4418d4118dL;

    public static final long TOKEN_ENTERTITLE = 0x6c2ce8db60a6dL;
    public static final long TOKEN_GOTITLE = 0x9f55d6b8aL;
    public static final long TOKEN_SEARCHTITLE = 0x1e4efcbab851f3eL;
    public static final long TOKEN_SENDTITLE = 0x5ab62a425134L;
    public static final long TOKEN_NEXTTITLE = 0x4ac3553b67f9L;
    public static final long TOKEN_DONETITLE = 0x2ba804d9335cL;
    public static final long TOKEN_PREVTITLE = 0x523afaf96e23L;
    public static final long TOKEN_NONETITLE = 0x4b9a25bff026L;
    public static final long TOKEN_UNKNOWNTITLE = 0x4ba5296e5766a85dL;

    public static final long TOKEN_ADDLAYOUT = 0x211993f479afL;
    public static final long TOKEN_ID = 0x102a6L;
    public static final long TOKEN_HEXAGONAL = 0x379824b9b62fL;
    public static final long TOKEN_WIDE = 0x1a1dd0L;
    public static final long TOKEN_HALFCOLUMNS = 0x1274de67161b456L;
    public static final long TOKEN_COLUMNS = 0x790ca4223L;
    public static final long TOKEN_ROWS = 0x166362L;
    public static final long TOKEN_ALIGN = 0x12f9733L;
    public static final long TOKEN_COLOR = 0x16b2be3L;

    public static final long TOKEN_ADDBOARD = 0xe502ed0208L;
    // public static final long TOKEN_ID = 0x102a6L;
    public static final long TOKEN_LAYOUT = 0x5805f907L;
    public static final long TOKEN_LANDSCAPE = 0x44010ff937b3L;
    public static final long TOKEN_PORTRAIT = 0x2375cbe8760L;



    public static final long TOKEN_XOFFSET = 0x141b96a3d1L;
    public static final long TOKEN_YOFFSET = 0x14b484849aL;
    public static final long TOKEN_SIZE = 0x17098aL;

    public static final long TOKEN_BLOCK = 0x14c4fa3L;

    public static final long TOKEN_HOME = 0xea740L;
    public static final long TOKEN_SKIP = 0x1711d2L;
    public static final long TOKEN_CRL = 0x14427L;
    public static final long TOKEN_CRR = 0x1442dL;
    public static final long TOKEN_UL = 0x1046aL;
    public static final long TOKEN_UR = 0x10470L;
    public static final long TOKEN_L = 0x10014L;
    public static final long TOKEN_R = 0x1001aL;
    public static final long TOKEN_DL = 0x101f5L;
    public static final long TOKEN_DR = 0x101fbL;
    public static final long TOKEN_FINDFREE = 0x156ad0179aeL;

    public static final long TOKEN_ODDS = 0x13d439L;
    public static final long TOKEN_EVENS = 0x1a9a13dL;

    public static final long TOKEN_FORCECAPS = 0x320dfec8a852L;
    public static final long TOKEN_FORCESHIFT = 0x73c05d4ab24e6L;
    public static final long TOKEN_FORCECTRL = 0x320dfec90e30L;
    public static final long TOKEN_FORCEALT = 0x15a52ff7113L;

    // public static final long TOKEN_BOARD = 0x14e5880L;
    public static final long TOKEN_COLUMN = 0x34597767L;
    public static final long TOKEN_ROW = 0x193faL;

    public static final long TOKEN_OVERWRITE = 0x4f61843c6a0fL;

    public static final long TOKEN_BUTTON = 0x30e91c11L;

    public static final long TOKEN_EXTEND = 0x3da4e6fdL;

    public static final long TOKEN_SEND = 0x16f269L;
    public static final long TOKEN_SECOND = 0x7556168dL;

    public static final long TOKEN_TEXT = 0x17b9c8L;
    public static final long TOKEN_KEY = 0x16d1bL;
    public static final long TOKEN_DO = 0x101f8L;

    public static final long TOKEN_DELETE = 0x375d443cL;
    public static final long TOKEN_BACKSPACE = 0x240879d29871L;

    public static final long TOKEN_DRAFT = 0x189da4dL;

    public static final long TOKEN_SETTINGS = 0x273bad5bcccL;

    public static final long TOKEN_META = 0x125016L;

    public static final long TOKEN_CAPS = 0xa7f8eL;
    public static final long TOKEN_CTRL = 0xae56cL;
    public static final long TOKEN_ALT = 0x1389fL;
    public static final long TOKEN_SHIFT = 0x32f4092L;

    public static final long TOKEN_LINK = 0x119ec9L;

    public static final long TOKEN_LOCK = 0x11bd48L;

    public static final long TOKEN_AUTOCAPS = 0xef6e451a57L;
    public static final long TOKEN_ON = 0x1038eL;
    public static final long TOKEN_OFF = 0x18291L;
    public static final long TOKEN_WAIT = 0x19f3d0L;
    public static final long TOKEN_HOLD = 0xea71aL;

    public static final long TOKEN_STRINGCAPS = 0xd4c9a99e4004bL;

    public static final long TOKEN_SPACETRAVEL = 0x1ea02b357b37bacL;

    public static final long TOKEN_AUTOSPACE = 0x229af1ada341L;
    public static final long TOKEN_ERASESPACES = 0xfbc52200a3a61dL;

    public static final long TOKEN_AFTER = 0x12b2e92L;
    public static final long TOKEN_BEFORE = 0x2f14a094L;
    public static final long TOKEN_AROUND = 0x2c6d5e42L;

    public static final long TOKEN_REPEAT = 0x713dd0a6L;

    public static final long TOKEN_MODIFY = 0x5da813adL;

    public static final long TOKEN_ENTER = 0x1a3c13eL;

    public static final long TOKEN_ADDTITLE = 0xe504eb848aL;
    // public static final long TOKEN_TEXT = 0x17b9c8L;
    public static final long TOKEN_BOLD = 0xa03ecL;
    public static final long TOKEN_ITALICS = 0xb39c66ee7L;
    public static final long TOKEN_NONBOLD = 0xe232d779aL;
    public static final long TOKEN_NONITALICS = 0xaed49d766321dL;
    // public static final long TOKEN_COLOR = 0x16b2be3L;

    public static final long TOKEN_INDEX = 0x215cf78L;

    public static final long TOKEN_ADDMODIFY = 0x211999969455L;
    // public static final long TOKEN_ID = 0x102a6L;
    public static final long TOKEN_ROLLS = 0x3182194L;
    public static final long TOKEN_ADDROLL = 0x630904aacL;
    public static final long TOKEN_IGNORESPACE = 0x13b2fae0bc2c2ceL;
    public static final long TOKEN_REVERSE = 0x105e77189aL;

    public static final long TOKEN_STOP = 0x1742d1L;


    // Complex parameter types - ABOVE POSITIVE 0xFFFF (Tokenizer.TOKEN_CODE_SHIFT)
    // Complex parameter - Multiple modifier - BELOW NEGATIVE 0xFFFF
    // Should be given as PARAMETER_... | PARAMETER_MOD_MULTIPLE

    public final static long PARAMETER_MOD_MULTIPLE = 0x8000000000000000L;

    // ONE PARAMETER types cannot be negative, not to mix with MULTIPLE Flag

    // One parameter types - POSITIVE VALUES, ORDER IS IMPORTANT !! (4 bit reserved)
    public final static long PARAMETER_BOOLEAN = 1L;   // Returned as Boolean (false==0, true==anything else)
    public final static long PARAMETER_CHAR = 2L;      // Returned as Character (unsigned 16 bit)
    public final static long PARAMETER_COLOR = 3L;     // Returned as Integer (unsigned 32 bit)
    public final static long PARAMETER_INT = 4L;       // Returned as Integer (signed 32 bit)
    public final static long PARAMETER_LONG = 5L;      // Returned as Long (signed 64 bit)

    public final static long PARAMETER_FILE = 6L;      // Returned as String
    public final static long PARAMETER_STRING = 7L;    // Returned as String

    public final static long PARAMETER_TEXT = 8L;      // Returned as String OR Character
                                                       // Further type-checking is needed after return!!

    public final static long PARAMETER_KEYWORD = 9L;   // Returned as Long (signed 64 bit)

    // One parameter - List modifier (same as one parameter, but bit 5 is 1) RESERVED TILL 0x1F!
    // Parameters are returned as ArrayList<Object>
    // Should be given as PARAMETER_... | PARAMETER_MOD_LIST

    public final static long PARAMETER_MOD_LIST = 0x10L;

    // Flag parameter - POSITIVE VALUES, ABOVE LIST AND BELOW NO-PARAMETER TYPES !!
    public final static long PARAMETER_FLAG = 0x20L;        // Stores Boolean.TRUE
    public final static long PARAMETER_FLAG_FALSE = 0x21L;  // Stores Boolean.FALSE

    // Default parameter - POSITIVE VALUES, ABOVE LIST AND BELOW NO-PARAMETER TYPES !!
    public final static long PARAMETER_DEFAULT = 0x41L;

    // Label parameter - POSITIVE VALUES, ABOVE LIST AND BELOW NO-PARAMETER TYPES !!
    public final static long PARAMETER_LABEL = 0x42L;

    // Label parameter - POSITIVE VALUES, ABOVE LIST AND BELOW NO-PARAMETER TYPES !!
    // Same as label, but change existing label without error
    public final static long PARAMETER_CHANGE_LABEL = 0x43L;

    // Special "messages" are not real parameters, but messages to the parser
    // Messages - POSITIVE VALUES, ABOVE ONE AND BELOW NO-PARAMETER TYPES !!
    public final static long MESSAGE_STOP = 0x80L;

    // No parameters - MOST POSITIVE!!
    public final static long NO_PARAMETERS = 0xFFL;

    // These tokens (parameter-commands) can be defined as labels
    public final static long[] ALLOWED_AS_LABEL = new long[]{
            TOKEN_ADDLAYOUT,
            TOKEN_BLOCK,
            TOKEN_SEND,
            TOKEN_BUTTON,
            TOKEN_EXTEND,
            TOKEN_ADDTITLE };

    // These tokens (parameter-commands) can be defined as labels
    public final static long[] ALLOWED_AS_DEFAULT = new long[]{
            TOKEN_ADDLAYOUT,
            TOKEN_SEND,
            TOKEN_BUTTON,
            TOKEN_EXTEND,
            TOKEN_ADDTITLE };

    /**
     * Parameter-commands are stored in an unmodifiable hash-HashMap (LIST)
     * as long token-code key (the command itself) and as Data value (command's data) pairs.
     */
    private static Map<Long, Data> LIST = createDataMap();


    private static void add( long tokenCode, long groupCode, long[] params, String methodName )
        {
        if ( LIST.put( tokenCode, new Data(groupCode, params, methodName )) != null )
            {
            Scribe.error("Please, check COMMANDS! " + tokenCode +
                    " [" + Tokenizer.regenerateKeyword(tokenCode) + "] has multiple definitions!");
            }
        }

    private static void add( long tokenCode, long[] params, String methodName )
        {
        add(tokenCode, tokenCode, params, methodName);
        }

    private static void add( long tokenCode, long groupCode, long[] params)
        {
        add( tokenCode, groupCode, params, null );
        }

    private static void add( long tokenCode, long[] params )
        {
        add( tokenCode, tokenCode, params, null );
        }

    /**
     * Static map initialization is done as suggested by http://stackoverflow.com/a/509016
     * ((it avoids anonymous class,
     *  it makes creation of map more explicit,
     *  it makes map unmodifiable,
     *  as MY_MAP is constant, I would name it like constant))
     * @return the initialized map
     *
     * Initialization is changed to a more convenient way:
     * Several "add" methods populate the LIST map (temporarily),
     * than an unmodifiable map is returned.
     */
    public static Map<Long, Data> createDataMap()
        {
        Scribe.locus( Debug.COMMANDS );

        LIST = new HashMap<>();

        // KEY: Code (long) of Parameter-command
        // VALUE (new DATA object)
        // - array (long) of allowed parameters for this command
        //      AT LEAST FIRST ITEM IS NEEDED (NO_PARAMETERS if there are no parameters allowed)
        // - method to call in SoftBoardClass (method should have a map parameter)
        add(ADDSOFTBOARD, new long[]{
                TOKEN_DEFAULT,
                TOKEN_LET,
                TOKEN_CHANGE,

                TOKEN_NAME,
                TOKEN_VERSION,
                TOKEN_AUTHOR,
                TOKEN_ADDTAGS,
                TOKEN_DESCRIPTION,
                TOKEN_DOCFILE,
                TOKEN_DOCURI,
                TOKEN_LOCALE,
                TOKEN_METACOLOR,
                TOKEN_LOCKCOLOR,
                TOKEN_AUTOCOLOR,
                TOKEN_TOUCHCOLOR,
                TOKEN_STROKECOLOR,
                TOKEN_TITLEFONT,

                TOKEN_ENTERTITLE,
                TOKEN_GOTITLE,
                TOKEN_SEARCHTITLE,
                TOKEN_SENDTITLE,
                TOKEN_NEXTTITLE,
                TOKEN_DONETITLE,
                TOKEN_PREVTITLE,
                TOKEN_NONETITLE,
                TOKEN_UNKNOWNTITLE,

                TOKEN_ADDLAYOUT,
                TOKEN_ADDBOARD,

                TOKEN_BLOCK,

                TOKEN_ADDMODIFY,

                TOKEN_STOP
        });

        add(TOKEN_DEFAULT, new long[]{PARAMETER_DEFAULT});
        add(TOKEN_LET, new long[]{PARAMETER_LABEL});
        add(TOKEN_CHANGE, new long[]{PARAMETER_CHANGE_LABEL});

        add(TOKEN_NAME, new long[]{PARAMETER_STRING}, "setName");
        add(TOKEN_VERSION, new long[]{PARAMETER_INT}, "setVersion");
        add(TOKEN_AUTHOR, new long[]{PARAMETER_STRING}, "setAuthor");
        add(TOKEN_ADDTAGS, new long[]{(PARAMETER_STRING | PARAMETER_MOD_LIST)}, "addTags");
        add(TOKEN_DESCRIPTION, new long[]{PARAMETER_STRING}, "setDescription");
        add(TOKEN_DOCFILE, new long[]{PARAMETER_FILE}, "setDocFile");
        add(TOKEN_DOCURI, new long[]{PARAMETER_STRING}, "setDocUri");

        add(TOKEN_LOCALE, new long[]{
                TOKEN_LANGUAGE, TOKEN_COUNTRY, TOKEN_VARIANT}, "setLocale");
        add(TOKEN_LANGUAGE, new long[]{PARAMETER_STRING});
        add(TOKEN_COUNTRY, new long[]{PARAMETER_STRING});
        add(TOKEN_VARIANT, new long[]{PARAMETER_STRING});

        add(TOKEN_METACOLOR, new long[]{PARAMETER_COLOR}, "setMetaColor");
        add(TOKEN_LOCKCOLOR, new long[]{PARAMETER_COLOR}, "setLockColor");
        add(TOKEN_AUTOCOLOR, new long[]{PARAMETER_COLOR}, "setAutoColor");
        add(TOKEN_TOUCHCOLOR, new long[]{PARAMETER_COLOR}, "setTouchColor");
        add(TOKEN_STROKECOLOR, new long[]{PARAMETER_COLOR}, "setStrokeColor");
        add(TOKEN_TITLEFONT, new long[]{PARAMETER_FILE}, "setTypeface");

        add(TOKEN_ENTERTITLE, new long[]{PARAMETER_TEXT}, "setEnterTitle");
        add(TOKEN_GOTITLE, new long[]{PARAMETER_TEXT}, "setGoTitle");
        add(TOKEN_SEARCHTITLE, new long[]{PARAMETER_TEXT}, "setSearchTitle");
        add(TOKEN_SENDTITLE, new long[]{PARAMETER_TEXT}, "setSendTitle");
        add(TOKEN_NEXTTITLE, new long[]{PARAMETER_TEXT}, "setNextTitle");
        add(TOKEN_DONETITLE, new long[]{PARAMETER_TEXT}, "setDoneTitle");
        add(TOKEN_PREVTITLE, new long[]{PARAMETER_TEXT}, "setPrevTitle");
        add(TOKEN_NONETITLE, new long[]{PARAMETER_TEXT}, "setNoneTitle");
        add(TOKEN_UNKNOWNTITLE, new long[]{PARAMETER_TEXT}, "setUnknownTitle");

        add(TOKEN_ADDLAYOUT, new long[]{
                TOKEN_ID, TOKEN_HEXAGONAL, TOKEN_WIDE,
                TOKEN_COLUMNS, TOKEN_HALFCOLUMNS, TOKEN_ROWS,
                TOKEN_ALIGN, TOKEN_COLOR,
                TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT}, "addLayout" );

        add(TOKEN_ID, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_HEXAGONAL, new long[]{NO_PARAMETERS}); // Useless parametercommand - just for clearer readability
        add(TOKEN_WIDE, new long[]{PARAMETER_FLAG});
        add(TOKEN_COLUMNS, new long[]{PARAMETER_INT});
        add(TOKEN_HALFCOLUMNS, new long[]{PARAMETER_INT});
        add(TOKEN_ROWS, new long[]{PARAMETER_INT});
        add(TOKEN_ALIGN, new long[]{PARAMETER_KEYWORD} );
        add(TOKEN_COLOR, new long[]{PARAMETER_COLOR});

        add(TOKEN_FORCECAPS, new long[]{PARAMETER_BOOLEAN});
        add(TOKEN_FORCESHIFT, new long[]{PARAMETER_BOOLEAN});
        add(TOKEN_FORCECTRL, new long[]{PARAMETER_BOOLEAN});
        add(TOKEN_FORCEALT, new long[]{PARAMETER_BOOLEAN});

        add(TOKEN_ADDBOARD, new long[]{
                TOKEN_ID,
                TOKEN_LAYOUT,
                TOKEN_PORTRAIT, TOKEN_LANDSCAPE}, "addBoard");
        // add(TOKEN_ID, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_LAYOUT, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_PORTRAIT, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_LANDSCAPE, new long[]{PARAMETER_KEYWORD});





        add(TOKEN_BLOCK, new long[]{
                        TOKEN_LAYOUT,
                        TOKEN_COLUMN,
                        TOKEN_ROW,
                        TOKEN_BUTTON | PARAMETER_MOD_MULTIPLE,
                        TOKEN_L | PARAMETER_MOD_MULTIPLE,
                        TOKEN_R | PARAMETER_MOD_MULTIPLE,
                        TOKEN_DL | PARAMETER_MOD_MULTIPLE,
                        TOKEN_DR | PARAMETER_MOD_MULTIPLE,
                        TOKEN_UL | PARAMETER_MOD_MULTIPLE,
                        TOKEN_UR | PARAMETER_MOD_MULTIPLE,
                        TOKEN_CRL | PARAMETER_MOD_MULTIPLE,
                        TOKEN_CRR | PARAMETER_MOD_MULTIPLE,
                        TOKEN_FINDFREE | PARAMETER_MOD_MULTIPLE,
                        TOKEN_SKIP | PARAMETER_MOD_MULTIPLE,
                        TOKEN_HOME | PARAMETER_MOD_MULTIPLE,
                        TOKEN_EXTEND | PARAMETER_MOD_MULTIPLE },
                "setBlock");

        // add(TOKEN_LAYOUT, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_COLUMN, new long[]{PARAMETER_INT});
        add(TOKEN_ROW, new long[]{PARAMETER_INT});

        add(TOKEN_L, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_R, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_DL, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_DR, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_UL, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_UR, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_CRL, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_CRR, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_FINDFREE, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});
        add(TOKEN_SKIP, TOKEN_BUTTON, new long[]{PARAMETER_INT});
        add(TOKEN_HOME, TOKEN_BUTTON, new long[]{PARAMETER_FLAG});

        add(TOKEN_BUTTON, TOKEN_BUTTON, new long[]{
                        TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,

                        TOKEN_TEXT,
                        TOKEN_DO,

                        TOKEN_COLOR,

                        TOKEN_AUTOCAPS,
                        TOKEN_STRINGCAPS,
                        TOKEN_ERASESPACES,
                        TOKEN_AUTOSPACE,

                        TOKEN_KEY,
                        TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT,

                        TOKEN_REPEAT,

                        TOKEN_META,
                        TOKEN_LINK,
                        TOKEN_SPACETRAVEL,

                        TOKEN_LOCK,

                        TOKEN_MODIFY,
                        TOKEN_REVERSE,

                        TOKEN_ENTER,

                        TOKEN_OVERWRITE,

                        TOKEN_SECOND,
                        TOKEN_SEND},
                // SEND remains only because label's purposes,
                // parameters could be given directly to BUTTON
                "setButton");

        add(TOKEN_EXTEND, TOKEN_BUTTON, new long[]{
                        TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,
                        TOKEN_COLOR },
                // SEND remains only because label's purposes,
                // parameters could be given directly to BUTTON
                "extendButton");

        add(TOKEN_ADDTITLE, new long[]{
                TOKEN_TEXT,
                TOKEN_XOFFSET, TOKEN_YOFFSET, TOKEN_SIZE,
                TOKEN_BOLD, TOKEN_NONBOLD, TOKEN_ITALICS, TOKEN_NONITALICS,
                TOKEN_COLOR }, "addTitle");
        add(TOKEN_XOFFSET, new long[]{PARAMETER_INT} );
        add(TOKEN_YOFFSET, new long[]{PARAMETER_INT} );
        add(TOKEN_SIZE, new long[]{PARAMETER_INT} );
        add(TOKEN_BOLD, TOKEN_BOLD, new long[]{PARAMETER_FLAG} );
        add(TOKEN_NONBOLD, TOKEN_BOLD, new long[]{PARAMETER_FLAG_FALSE} );
        add(TOKEN_ITALICS, TOKEN_ITALICS, new long[]{PARAMETER_FLAG} );
        add(TOKEN_NONITALICS, TOKEN_ITALICS, new long[]{PARAMETER_FLAG_FALSE} );
        // COLOR




        add(TOKEN_OVERWRITE, new long[]{PARAMETER_FLAG});

        add(TOKEN_SEND, new long[]{
                        TOKEN_TEXT,
                        TOKEN_AUTOCAPS,
                        TOKEN_STRINGCAPS,
                        TOKEN_ERASESPACES,
                        TOKEN_AUTOSPACE,

                        TOKEN_KEY,
                        TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT,

                        TOKEN_DO,

                        TOKEN_REPEAT,

                        TOKEN_META,
                        TOKEN_LINK,
                        TOKEN_SPACETRAVEL,

                        TOKEN_LOCK,

                        TOKEN_MODIFY,
                        TOKEN_REVERSE,

                        TOKEN_ENTER},
                "createButtonFunction");

        add(TOKEN_SECOND, new long[]{
                        TOKEN_TEXT,
                        TOKEN_AUTOCAPS,
                        TOKEN_STRINGCAPS,
                        TOKEN_ERASESPACES,
                        TOKEN_AUTOSPACE,

                        TOKEN_KEY,
                        TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT,

                        TOKEN_DO },
                "packet");

        add(TOKEN_TEXT, new long[]{PARAMETER_TEXT});
        add(TOKEN_KEY, new long[]{PARAMETER_INT});
        add(TOKEN_DO, new long[]{PARAMETER_KEYWORD});

        add(TOKEN_META, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_LINK, new long[]{PARAMETER_INT});

        add(TOKEN_LOCK, new long[]{PARAMETER_FLAG});

        add(TOKEN_AUTOCAPS, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_STRINGCAPS, new long[]{PARAMETER_FLAG});

        add(TOKEN_REPEAT, new long[]{PARAMETER_FLAG});

        add(TOKEN_SPACETRAVEL, new long[]{PARAMETER_FLAG} );

        // TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT are already defined

        add(TOKEN_AUTOSPACE, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_ERASESPACES, new long[]{PARAMETER_KEYWORD});

        add(TOKEN_MODIFY, new long[]{PARAMETER_KEYWORD});
        add(TOKEN_REVERSE, new long[]{PARAMETER_FLAG});

        add(TOKEN_ENTER, new long[]{PARAMETER_FLAG});

        add(TOKEN_ADDMODIFY, new long[]{
                TOKEN_ID, TOKEN_ADDROLL, TOKEN_ROLLS,
                TOKEN_IGNORESPACE }, "addModify" );
        // TOKEN_ID is already defined
        // !! addRollHelper functionality should be avoided !!
        // "Multiple" type parameters are needed
        add(TOKEN_ADDROLL, new long[]{(PARAMETER_STRING | PARAMETER_MOD_LIST) }, "addRollHelper");
        add(TOKEN_ROLLS, new long[]{(PARAMETER_STRING | PARAMETER_MOD_LIST)});
        add(TOKEN_IGNORESPACE, new long[]{PARAMETER_FLAG});

        add(TOKEN_STOP, new long[]{MESSAGE_STOP} );

//        add(TOKEN_, new long[]{ }, "" );

        return Collections.unmodifiableMap( LIST );
        }

    public static Data get( long commandCode ) throws InvalidKeyException
        {
        Data data = LIST.get( commandCode );
        if ( data != null )
            return data;
        throw new InvalidKeyException("Key doesn't exist!");
        }


    /**
     ** Tokens of the predefined labels
     **/

    // public static final long TOKEN_ON = 0x38fL;
    // public static final long TOKEN_OFF = 0x8292L;
    public static final long TOKEN_TRUE = 0x17fecfL;
    public static final long TOKEN_FALSE = 0x1b62527L;

    // Hard-key mnemonics
    public static final long TOKEN_KEYUNKNOWN = 0x96bb8b22835daL;
    public static final long TOKEN_KEYSOFTLEFT = 0x15c91860fd50beeL;
    public static final long TOKEN_KEYSOFTRIGHT = 0x326108604a556a5eL;
    public static final long TOKEN_KEYHOME = 0xc304dfe9cL;
    public static final long TOKEN_KEYBACK = 0xc30490f24L;
    public static final long TOKEN_KEYCALL = 0xc3049d64fL;
    public static final long TOKEN_KEYENDCALL = 0x96baf22792dbdL;
    public static final long TOKEN_KEY0 = 0x10c50bL;
    public static final long TOKEN_KEY1 = 0x10c50cL;
    public static final long TOKEN_KEY2 = 0x10c50dL;
    public static final long TOKEN_KEY3 = 0x10c50eL;
    public static final long TOKEN_KEY4 = 0x10c50fL;
    public static final long TOKEN_KEY5 = 0x10c510L;
    public static final long TOKEN_KEY6 = 0x10c511L;
    public static final long TOKEN_KEY7 = 0x10c512L;
    public static final long TOKEN_KEY8 = 0x10c513L;
    public static final long TOKEN_KEY9 = 0x10c514L;
    public static final long TOKEN_KEYSTAR = 0xc30569829L;
    public static final long TOKEN_KEYNUMBER = 0x412e6536be4eL;
    public static final long TOKEN_KEYDPADUP = 0x412e3b497bebL;
    public static final long TOKEN_KEYDPADDOWN = 0x15c903b06928cbcL;
    public static final long TOKEN_KEYDPADLEFT = 0x15c903b069883bbL;
    public static final long TOKEN_KEYDPADRIGHT = 0x3260d887f495baffL;
    public static final long TOKEN_KEYDPADCENTR = 0x3260d887f2e5d495L;
    public static final long TOKEN_KEYVOLUP = 0x1c2fcb226eeL;
    public static final long TOKEN_KEYVOLDOWN = 0x96bb94f4d0fc7L;
    public static final long TOKEN_KEYPOWER = 0x1c2fc06c9cdL;
    public static final long TOKEN_KEYCAMERA = 0x412e3583b62eL;
    public static final long TOKEN_KEYCLEAR = 0x1c2fa905323L;
    public static final long TOKEN_KEYA = 0x10c515L;
    public static final long TOKEN_KEYB = 0x10c516L;
    public static final long TOKEN_KEYC = 0x10c517L;
    public static final long TOKEN_KEYD = 0x10c518L;
    public static final long TOKEN_KEYE = 0x10c519L;
    public static final long TOKEN_KEYF = 0x10c51aL;
    public static final long TOKEN_KEYG = 0x10c51bL;
    public static final long TOKEN_KEYH = 0x10c51cL;
    public static final long TOKEN_KEYI = 0x10c51dL;
    public static final long TOKEN_KEYJ = 0x10c51eL;
    public static final long TOKEN_KEYK = 0x10c51fL;
    public static final long TOKEN_KEYL = 0x10c520L;
    public static final long TOKEN_KEYM = 0x10c521L;
    public static final long TOKEN_KEYN = 0x10c522L;
    public static final long TOKEN_KEYO = 0x10c523L;
    public static final long TOKEN_KEYP = 0x10c524L;
    public static final long TOKEN_KEYQ = 0x10c525L;
    public static final long TOKEN_KEYR = 0x10c526L;
    public static final long TOKEN_KEYS = 0x10c527L;
    public static final long TOKEN_KEYT = 0x10c528L;
    public static final long TOKEN_KEYU = 0x10c529L;
    public static final long TOKEN_KEYV = 0x10c52aL;
    public static final long TOKEN_KEYW = 0x10c52bL;
    public static final long TOKEN_KEYX = 0x10c52cL;
    public static final long TOKEN_KEYY = 0x10c52dL;
    public static final long TOKEN_KEYZ = 0x10c52eL;
    public static final long TOKEN_KEYCOMMA = 0x1c2fa92d12dL;
    public static final long TOKEN_KEYPERIOD = 0x412e6bb5690cL;
    public static final long TOKEN_KEYALTLEFT = 0x96bacb84e0d52L;
    public static final long TOKEN_KEYALTRIGHT = 0x15c8ff6a3d29dd2L;
    public static final long TOKEN_KEYSHLEFT = 0x412e786cca8fL;
    public static final long TOKEN_KEYSHRIGHT = 0x96bb76843f7a3L;
    public static final long TOKEN_KEYTAB = 0x545659bdL;
    public static final long TOKEN_KEYSPACE = 0x1c2fc5ce480L;
    public static final long TOKEN_KEYSYM = 0x545657e7L;
    public static final long TOKEN_KEYEXPLORER = 0x15c9056274ff808L;
    public static final long TOKEN_KEYENVELOPE = 0x15c90504607427dL;
    public static final long TOKEN_KEYENTER = 0x1c2facb618aL;
    public static final long TOKEN_KEYDEL = 0x545604cbL;
    public static final long TOKEN_KEYGRAVE = 0x1c2fb0747adL;
    public static final long TOKEN_KEYMINUS = 0x1c2fbac2dfcL;
    public static final long TOKEN_KEYEQUAL = 0x1c2facdb7e0L;
    public static final long TOKEN_KEYLBRACKET = 0x15c90e3c28949a3L;
    public static final long TOKEN_KEYRBRACKET = 0x15c916860d237f1L;
    public static final long TOKEN_KEYBACKSLASH = 0x3260d0da179a8adcL;
    public static final long TOKEN_KEYSEMICOLON = 0x3261078741e84efdL;
    public static final long TOKEN_KEYAPOSTROPH = 0x3260cefb1f92b260L;
    public static final long TOKEN_KEYSLASH = 0x1c2fc59cf5fL;
    public static final long TOKEN_KEYAT = 0x2487c4aL;
    public static final long TOKEN_KEYNUM = 0x54563c96L;
    public static final long TOKEN_KEYHOOK = 0xc304dfeecL;
    public static final long TOKEN_KEYFOCUS = 0x1c2fae867c0L;
    public static final long TOKEN_KEYPLUS = 0xc30541eafL;
    public static final long TOKEN_KEYMENU = 0xc3051a6a8L;
    public static final long TOKEN_KEYNOTIFY = 0x412e6490b74eL;
    public static final long TOKEN_KEYSEARCH = 0x412e780ec447L;
    public static final long TOKEN_KEYPLAYPAUSE = 0x3260fe85a2b1c86cL;
    public static final long TOKEN_KEYSTOP = 0xc30569a2dL;
    public static final long TOKEN_KEYNEXT = 0xc30526df6L;
    public static final long TOKEN_KEYPREV = 0xc30543c78L;
    public static final long TOKEN_KEYREWIND = 0x412e73fd7be2L;
    public static final long TOKEN_KEYFFORWARD = 0x15c90617d86f796L;
    public static final long TOKEN_KEYMUTE = 0xc3051fd06L;
    public static final long TOKEN_KEYPAGEUP = 0x412e6b3a704fL;
    public static final long TOKEN_KEYPAGEDOWN = 0x15c913b661d7780L;
    public static final long TOKEN_KEYPICTSYM = 0x96bb59ff83d17L;
    public static final long TOKEN_KEYCHARSET = 0x96badd786f794L;
    public static final long TOKEN_KEYBUTTONA = 0x96bad7675450fL;
    public static final long TOKEN_KEYBUTTONB = 0x96bad76754510L;
    public static final long TOKEN_KEYBUTTONC = 0x96bad76754511L;
    public static final long TOKEN_KEYBUTTONX = 0x96bad76754526L;
    public static final long TOKEN_KEYBUTTONY = 0x96bad76754527L;
    public static final long TOKEN_KEYBUTTONZ = 0x96bad76754528L;
    public static final long TOKEN_KEYBUTTONL1 = 0x15c90121ecefce7L;
    public static final long TOKEN_KEYBUTTONR1 = 0x15c90121ecefdc5L;
    public static final long TOKEN_KEYBUTTONL2 = 0x15c90121ecefce8L;
    public static final long TOKEN_KEYBUTTONR2 = 0x15c90121ecefdc6L;
    public static final long TOKEN_KEYBUTTHUMBL = 0x3260d29e7303cedbL;
    public static final long TOKEN_KEYBUTTHUMBR = 0x3260d29e7303cee1L;
    public static final long TOKEN_KEYBUTSTART = 0x15c90121eb5fcbcL;
    public static final long TOKEN_KEYBUTSELECT = 0x3260d29e6e84c9d3L;
    public static final long TOKEN_KEYBUTMODE = 0x96bad766fda96L;
    public static final long TOKEN_KEYESC = 0x54560c21L;
    public static final long TOKEN_KEYFWDEL = 0x1c2faee99aaL;
    public static final long TOKEN_KEYCTRLLEFT = 0x15c90279717197fL;
    public static final long TOKEN_KEYCTRLRIGHT = 0x3260d5b8d6e16053L;
    public static final long TOKEN_KEYCAPSLOCK = 0x15c901c35f39883L;
    public static final long TOKEN_KEYSCROLLOCK = 0x3261075e239676fcL;
    public static final long TOKEN_KEYMETALEFT = 0x15c90fbafff7389L;
    public static final long TOKEN_KEYMETARIGHT = 0x3260f460707663c5L;
    public static final long TOKEN_KEYFUNCTION = 0x15c906a6da5eb35L;
    public static final long TOKEN_KEYSYSRQ = 0x1c2fc643bbeL;
    public static final long TOKEN_KEYBREAK = 0x1c2fa785d59L;
    public static final long TOKEN_KEYMOVEHOME = 0x15c9101b1fdf3cbL;
    public static final long TOKEN_KEYMOVEEND = 0x96bb3f00eb12cL;
    public static final long TOKEN_KEYINS = 0x545620dcL;
    public static final long TOKEN_KEYFORWARD = 0x96bafc128fca3L;
    public static final long TOKEN_KEYPLAY = 0xc30541bd1L;
    public static final long TOKEN_KEYPAUSE = 0x1c2fbfbeefeL;
    public static final long TOKEN_KEYCLOSE = 0x1c2fa908b2aL;
    public static final long TOKEN_KEYEJECT = 0x1c2fac7f997L;
    public static final long TOKEN_KEYREC = 0x54564fa0L;
    public static final long TOKEN_KEYF1 = 0x2487ce7L;
    public static final long TOKEN_KEYF2 = 0x2487ce8L;
    public static final long TOKEN_KEYF3 = 0x2487ce9L;
    public static final long TOKEN_KEYF4 = 0x2487ceaL;
    public static final long TOKEN_KEYF5 = 0x2487cebL;
    public static final long TOKEN_KEYF6 = 0x2487cecL;
    public static final long TOKEN_KEYF7 = 0x2487cedL;
    public static final long TOKEN_KEYF8 = 0x2487ceeL;
    public static final long TOKEN_KEYF9 = 0x2487cefL;
    public static final long TOKEN_KEYF10 = 0x54560d87L;
    public static final long TOKEN_KEYF11 = 0x54560d88L;
    public static final long TOKEN_KEYF12 = 0x54560d89L;
    public static final long TOKEN_KEYNUMLOCK = 0x96bb4a0cd6f6fL;
    public static final long TOKEN_KEYNUM0 = 0xc3052c1d2L;
    public static final long TOKEN_KEYNUM1 = 0xc3052c1d3L;
    public static final long TOKEN_KEYNUM2 = 0xc3052c1d4L;
    public static final long TOKEN_KEYNUM3 = 0xc3052c1d5L;
    public static final long TOKEN_KEYNUM4 = 0xc3052c1d6L;
    public static final long TOKEN_KEYNUM5 = 0xc3052c1d7L;
    public static final long TOKEN_KEYNUM6 = 0xc3052c1d8L;
    public static final long TOKEN_KEYNUM7 = 0xc3052c1d9L;
    public static final long TOKEN_KEYNUM8 = 0xc3052c1daL;
    public static final long TOKEN_KEYNUM9 = 0xc3052c1dbL;
    public static final long TOKEN_KEYNUMDIV = 0x412e6536c998L;
    public static final long TOKEN_KEYNUMSTAR = 0x96bb4a0d2f2f4L;
    public static final long TOKEN_KEYNUMMINUS = 0x15c911b3da54d53L;
    public static final long TOKEN_KEYNUMPLUS = 0x96bb4a0d0797aL;
    public static final long TOKEN_KEYNUMPERIOD = 0x3260f8eff4b6f09fL;
    public static final long TOKEN_KEYNUMCOMMA = 0x15c911b3c8bf084L;
    public static final long TOKEN_KEYNUMENTER = 0x15c911b3cc480e1L;
    public static final long TOKEN_KEYNUMEQUAL = 0x15c911b3cc6d737L;
    public static final long TOKEN_KEYNUMLPAR = 0x96bb4a0cd7485L;
    public static final long TOKEN_KEYNUMRPAR = 0x96bb4a0d217b3L;
    public static final long TOKEN_KEYVOLMUTE = 0x96bb94f54242aL;
    public static final long TOKEN_KEYINFO = 0xc304ebe27L;
    public static final long TOKEN_KEYCHUP = 0xc3049fd0fL;
    public static final long TOKEN_KEYCHDOWN = 0x412e36452640L;
    public static final long TOKEN_KEYZOOMIN = 0x412e962631c1L;
    public static final long TOKEN_KEYZOOMOUT = 0x96bbbb361523fL;
    public static final long TOKEN_KEYTV = 0x2487f0bL;
    public static final long TOKEN_KEYWIN = 0x54566afcL;
    public static final long TOKEN_KEYGUIDE = 0x1c2fb09c172L;
    public static final long TOKEN_KEYDVR = 0x54560746L;
    public static final long TOKEN_KEYBOOKMARK = 0x15c900e738e0141L;
    public static final long TOKEN_KEYCAPTIONS = 0x15c901c360de17cL;
    public static final long TOKEN_KEYSETTINGS = 0x15c918050659a68L;
    public static final long TOKEN_KEYTVPOWER = 0x96bb83adb167dL;
    public static final long TOKEN_KEYTVINPUT = 0x96bb83a11feecL;
    public static final long TOKEN_KEYSTBPOWER = 0x15c9188fb825275L;
    public static final long TOKEN_KEYSTBINPUT = 0x15c9188fab93ae4L;
    public static final long TOKEN_KEYAVRPOWER = 0x15c8ffc94a4f66dL;
    public static final long TOKEN_KEYAVRINPUT = 0x15c8ffc93dbdedcL;
    public static final long TOKEN_KEYPRGRED = 0x412e6d20db79L;
    public static final long TOKEN_KEYPRGGREEN = 0x15c91458e2d30bdL;
    public static final long TOKEN_KEYPRGYELLOW = 0x3260ff0dd55bb8f1L;
    public static final long TOKEN_KEYPRGBLUE = 0x96bb5c58f82c3L;
    public static final long TOKEN_KEYAPPSWITCH = 0x3260cefbb8cf8213L;
    public static final long TOKEN_KEYBUTTON1 = 0x96bad76754506L;
    public static final long TOKEN_KEYBUTTON2 = 0x96bad76754507L;
    public static final long TOKEN_KEYBUTTON3 = 0x96bad76754508L;
    public static final long TOKEN_KEYBUTTON4 = 0x96bad76754509L;
    public static final long TOKEN_KEYBUTTON5 = 0x96bad7675450aL;
    public static final long TOKEN_KEYBUTTON6 = 0x96bad7675450bL;
    public static final long TOKEN_KEYBUTTON7 = 0x96bad7675450cL;
    public static final long TOKEN_KEYBUTTON8 = 0x96bad7675450dL;
    public static final long TOKEN_KEYBUTTON9 = 0x96bad7675450eL;
    public static final long TOKEN_KEYBUTTON10 = 0x15c90121ecefa02L;
    public static final long TOKEN_KEYBUTTON11 = 0x15c90121ecefa03L;
    public static final long TOKEN_KEYBUTTON12 = 0x15c90121ecefa04L;
    public static final long TOKEN_KEYBUTTON13 = 0x15c90121ecefa05L;
    public static final long TOKEN_KEYBUTTON14 = 0x15c90121ecefa06L;
    public static final long TOKEN_KEYBUTTON15 = 0x15c90121ecefa07L;
    public static final long TOKEN_KEYBUTTON16 = 0x15c90121ecefa08L;
    public static final long TOKEN_KEYLANGUAGE = 0x15c90e319cc4777L;
    public static final long TOKEN_KEYMANNER = 0x412e5ed9b6aeL;
    public static final long TOKEN_KEY3D = 0x2487b37L;
    public static final long TOKEN_KEYCONTACTS = 0x15c90248ac5889aL;
    public static final long TOKEN_KEYCALENDAR = 0x15c901c23dc20a5L;
    public static final long TOKEN_KEYMUSIC = 0x1c2fbb58d49L;
    public static final long TOKEN_KEYCALC = 0xc3049d646L;
    public static final long TOKEN_KEYKAKU = 0xc3050051bL;
    public static final long TOKEN_KEYEISU = 0xc304b8dddL;
    public static final long TOKEN_KEYMUHENKAN = 0x15c91050db84cf2L;
    public static final long TOKEN_KEYHENKAN = 0x412e4aa183a6L;
    public static final long TOKEN_KEYSWKANA = 0x412e7a18e682L;
    public static final long TOKEN_KEYYEN = 0x5456751aL;
    public static final long TOKEN_KEYRO = 0x2487ebaL;
    public static final long TOKEN_KEYKANA = 0xc30500576L;
    public static final long TOKEN_KEYASSIST = 0x412e2f46fa40L;
    public static final long TOKEN_KEYBRGDOWN = 0x96bad688ed3ddL;
    public static final long TOKEN_KEYBRGUP = 0x1c2fa786af4L;
    public static final long TOKEN_KEYMATRACE = 0x96bb3b601fce3L;
    public static final long TOKEN_KEYSLEEP = 0x1c2fc59e2c5L;
    public static final long TOKEN_KEYWAKE = 0xc30594b67L;
    public static final long TOKEN_KEYPAIR = 0xc3053e21fL;
    public static final long TOKEN_KEYMEDIATOP = 0x15c90fb6eba5ec2L;
    public static final long TOKEN_KEY11 = 0x2487ae1L;
    public static final long TOKEN_KEY12 = 0x2487ae2L;
    public static final long TOKEN_KEYCHLAST = 0x412e364b09bcL;
    public static final long TOKEN_KEYTVDATA = 0x412e7e190039L;
    public static final long TOKEN_KEYVOICEASST = 0x326111f73da6cbd5L;
    public static final long TOKEN_KEYTVRADIO = 0x96bb83b09113fL;
    public static final long TOKEN_KEYTVTEXT = 0x412e7e257414L;
    public static final long TOKEN_KEYTVNUMENT = 0x15c91a079e5e518L;
    public static final long TOKEN_KEYTVTERRANA = 0x32610c34f2207697L;
    public static final long TOKEN_KEYTVTERRDIA = 0x32610c34f22085e9L;
    public static final long TOKEN_KEYTVSAT = 0x1c2fc7e80a6L;
    public static final long TOKEN_KEYTVSATBS = 0x96bb83b25fec1L;
    public static final long TOKEN_KEYTVSATCS = 0x96bb83b25fee6L;
    public static final long TOKEN_KEYTVSATSERV = 0x32610c3448e43846L;
    public static final long TOKEN_KEYTVNETWORK = 0x32610c315cca8c90L;
    public static final long TOKEN_KEYTVANTCABL = 0x32610c29bdd95167L;
    public static final long TOKEN_KEYTVHDMI1 = 0x96bb839ed9976L;
    public static final long TOKEN_KEYTVHDMI2 = 0x96bb839ed9977L;
    public static final long TOKEN_KEYTVHDMI3 = 0x96bb839ed9978L;
    public static final long TOKEN_KEYTVHDMI4 = 0x96bb839ed9979L;
    public static final long TOKEN_KEYTVCMPSIT1 = 0x32610c2aeb2d1f7eL;
    public static final long TOKEN_KEYTVCMPSIT2 = 0x32610c2aeb2d1f7fL;
    public static final long TOKEN_KEYTVCOMP1 = 0x96bb839671e43L;
    public static final long TOKEN_KEYTVCOMP2 = 0x96bb839671e44L;
    public static final long TOKEN_KEYTVVGA1 = 0x412e7e270711L;
    public static final long TOKEN_KEYTVAUMIX = 0x96bb839328eacL;
    public static final long TOKEN_KEYTVAUMIXUP = 0x32610c29da04ff93L;
    public static final long TOKEN_KEYTVAUMIXDN = 0x32610c29da04fd1cL;
    public static final long TOKEN_KEYTVZOOMMOD = 0x32610c38b0af5fcfL;
    public static final long TOKEN_KEYTVCONTMNU = 0x32610c2af338fdc6L;
    public static final long TOKEN_KEYTVMEDCNTX = 0x32610c30c203a271L;
    public static final long TOKEN_KEYTVTIMER = 0x96bb83b48a199L;
    public static final long TOKEN_KEYHELP = 0xc304dc908L;


    /**
     ** Values of the predefined labels
     **/

    /**
     * Creates a new Labels class, which can be filled with default values.
     * Called by SoftBoardParser.parseSoftBoard()
     * @return the new Labels class
     */
    public static Labels createLabels()
        {
        Labels labels = new Labels();

        labels.add( TOKEN_ON, -1L );
        labels.add( TOKEN_OFF, 0L );
        labels.add( TOKEN_TRUE, -1L );
        labels.add( TOKEN_FALSE, 0 );

        // Hard-key labels
        labels.add( TOKEN_KEYUNKNOWN, 0x10000L + 0L );
        labels.add( TOKEN_KEYSOFTLEFT, 0x10000L + 1L );
        labels.add( TOKEN_KEYSOFTRIGHT, 0x10000L + 2L );
        labels.add( TOKEN_KEYHOME, 0x10000L + 3L );
        labels.add( TOKEN_KEYBACK, 0x10000L + 4L );
        labels.add( TOKEN_KEYCALL, 0x10000L + 5L );
        labels.add( TOKEN_KEYENDCALL, 0x10000L + 6L );
        labels.add( TOKEN_KEY0, 0x10000L + 7L );
        labels.add( TOKEN_KEY1, 0x10000L + 8L );
        labels.add( TOKEN_KEY2, 0x10000L + 9L );
        labels.add( TOKEN_KEY3, 0x10000L + 10L );
        labels.add( TOKEN_KEY4, 0x10000L + 11L );
        labels.add( TOKEN_KEY5, 0x10000L + 12L );
        labels.add( TOKEN_KEY6, 0x10000L + 13L );
        labels.add( TOKEN_KEY7, 0x10000L + 14L );
        labels.add( TOKEN_KEY8, 0x10000L + 15L );
        labels.add( TOKEN_KEY9, 0x10000L + 16L );
        labels.add( TOKEN_KEYSTAR, 0x10000L + 17L );
        labels.add( TOKEN_KEYNUMBER, 0x10000L + 18L );
        labels.add( TOKEN_KEYDPADUP, 0x10000L + 19L );
        labels.add( TOKEN_KEYDPADDOWN, 0x10000L + 20L );
        labels.add( TOKEN_KEYDPADLEFT, 0x10000L + 21L );
        labels.add( TOKEN_KEYDPADRIGHT, 0x10000L + 22L );
        labels.add( TOKEN_KEYDPADCENTR, 0x10000L + 23L );
        labels.add( TOKEN_KEYVOLUP, 0x10000L + 24L );
        labels.add( TOKEN_KEYVOLDOWN, 0x10000L + 25L );
        labels.add( TOKEN_KEYPOWER, 0x10000L + 26L );
        labels.add( TOKEN_KEYCAMERA, 0x10000L + 27L );
        labels.add( TOKEN_KEYCLEAR, 0x10000L + 28L );
        labels.add( TOKEN_KEYA, 0x10000L + 29L );
        labels.add( TOKEN_KEYB, 0x10000L + 30L );
        labels.add( TOKEN_KEYC, 0x10000L + 31L );
        labels.add( TOKEN_KEYD, 0x10000L + 32L );
        labels.add( TOKEN_KEYE, 0x10000L + 33L );
        labels.add( TOKEN_KEYF, 0x10000L + 34L );
        labels.add( TOKEN_KEYG, 0x10000L + 35L );
        labels.add( TOKEN_KEYH, 0x10000L + 36L );
        labels.add( TOKEN_KEYI, 0x10000L + 37L );
        labels.add( TOKEN_KEYJ, 0x10000L + 38L );
        labels.add( TOKEN_KEYK, 0x10000L + 39L );
        labels.add( TOKEN_KEYL, 0x10000L + 40L );
        labels.add( TOKEN_KEYM, 0x10000L + 41L );
        labels.add( TOKEN_KEYN, 0x10000L + 42L );
        labels.add( TOKEN_KEYO, 0x10000L + 43L );
        labels.add( TOKEN_KEYP, 0x10000L + 44L );
        labels.add( TOKEN_KEYQ, 0x10000L + 45L );
        labels.add( TOKEN_KEYR, 0x10000L + 46L );
        labels.add( TOKEN_KEYS, 0x10000L + 47L );
        labels.add( TOKEN_KEYT, 0x10000L + 48L );
        labels.add( TOKEN_KEYU, 0x10000L + 49L );
        labels.add( TOKEN_KEYV, 0x10000L + 50L );
        labels.add( TOKEN_KEYW, 0x10000L + 51L );
        labels.add( TOKEN_KEYX, 0x10000L + 52L );
        labels.add( TOKEN_KEYY, 0x10000L + 53L );
        labels.add( TOKEN_KEYZ, 0x10000L + 54L );
        labels.add( TOKEN_KEYCOMMA, 0x10000L + 55L );
        labels.add( TOKEN_KEYPERIOD, 0x10000L + 56L );
        labels.add( TOKEN_KEYALTLEFT, 0x10000L + 57L );
        labels.add( TOKEN_KEYALTRIGHT, 0x10000L + 58L );
        labels.add( TOKEN_KEYSHLEFT, 0x10000L + 59L );
        labels.add( TOKEN_KEYSHRIGHT, 0x10000L + 60L );
        labels.add( TOKEN_KEYTAB, 0x10000L + 61L );
        labels.add( TOKEN_KEYSPACE, 0x10000L + 62L );
        labels.add( TOKEN_KEYSYM, 0x10000L + 63L );
        labels.add( TOKEN_KEYEXPLORER, 0x10000L + 64L );
        labels.add( TOKEN_KEYENVELOPE, 0x10000L + 65L );
        labels.add( TOKEN_KEYENTER, 0x10000L + 66L );
        labels.add( TOKEN_KEYDEL, 0x10000L + 67L );
        labels.add( TOKEN_KEYGRAVE, 0x10000L + 68L );
        labels.add( TOKEN_KEYMINUS, 0x10000L + 69L );
        labels.add( TOKEN_KEYEQUAL, 0x10000L + 70L );
        labels.add( TOKEN_KEYLBRACKET, 0x10000L + 71L );
        labels.add( TOKEN_KEYRBRACKET, 0x10000L + 72L );
        labels.add( TOKEN_KEYBACKSLASH, 0x10000L + 73L );
        labels.add( TOKEN_KEYSEMICOLON, 0x10000L + 74L );
        labels.add( TOKEN_KEYAPOSTROPH, 0x10000L + 75L );
        labels.add( TOKEN_KEYSLASH, 0x10000L + 76L );
        labels.add( TOKEN_KEYAT, 0x10000L + 77L );
        labels.add( TOKEN_KEYNUM, 0x10000L + 78L );
        labels.add( TOKEN_KEYHOOK, 0x10000L + 79L );
        labels.add( TOKEN_KEYFOCUS, 0x10000L + 80L );
        labels.add( TOKEN_KEYPLUS, 0x10000L + 81L );
        labels.add( TOKEN_KEYMENU, 0x10000L + 82L );
        labels.add( TOKEN_KEYNOTIFY, 0x10000L + 83L );
        labels.add( TOKEN_KEYSEARCH, 0x10000L + 84L );
        labels.add( TOKEN_KEYPLAYPAUSE, 0x10000L + 85L );
        labels.add( TOKEN_KEYSTOP, 0x10000L + 86L );
        labels.add( TOKEN_KEYNEXT, 0x10000L + 87L );
        labels.add( TOKEN_KEYPREV, 0x10000L + 88L );
        labels.add( TOKEN_KEYREWIND, 0x10000L + 89L );
        labels.add( TOKEN_KEYFFORWARD, 0x10000L + 90L );
        labels.add( TOKEN_KEYMUTE, 0x10000L + 91L );
        labels.add( TOKEN_KEYPAGEUP, 0x10000L + 92L );
        labels.add( TOKEN_KEYPAGEDOWN, 0x10000L + 93L );
        labels.add( TOKEN_KEYPICTSYM, 0x10000L + 94L );
        labels.add( TOKEN_KEYCHARSET, 0x10000L + 95L );
        labels.add( TOKEN_KEYBUTTONA, 0x10000L + 96L );
        labels.add( TOKEN_KEYBUTTONB, 0x10000L + 97L );
        labels.add( TOKEN_KEYBUTTONC, 0x10000L + 98L );
        labels.add( TOKEN_KEYBUTTONX, 0x10000L + 99L );
        labels.add( TOKEN_KEYBUTTONY, 0x10000L + 100L );
        labels.add( TOKEN_KEYBUTTONZ, 0x10000L + 101L );
        labels.add( TOKEN_KEYBUTTONL1, 0x10000L + 102L );
        labels.add( TOKEN_KEYBUTTONR1, 0x10000L + 103L );
        labels.add( TOKEN_KEYBUTTONL2, 0x10000L + 104L );
        labels.add( TOKEN_KEYBUTTONR2, 0x10000L + 105L );
        labels.add( TOKEN_KEYBUTTHUMBL, 0x10000L + 106L );
        labels.add( TOKEN_KEYBUTTHUMBR, 0x10000L + 107L );
        labels.add( TOKEN_KEYBUTSTART, 0x10000L + 108L );
        labels.add( TOKEN_KEYBUTSELECT, 0x10000L + 109L );
        labels.add( TOKEN_KEYBUTMODE, 0x10000L + 110L );
        labels.add( TOKEN_KEYESC, 0x10000L + 111L );
        labels.add( TOKEN_KEYFWDEL, 0x10000L + 112L );
        labels.add( TOKEN_KEYCTRLLEFT, 0x10000L + 113L );
        labels.add( TOKEN_KEYCTRLRIGHT, 0x10000L + 114L );
        labels.add( TOKEN_KEYCAPSLOCK, 0x10000L + 115L );
        labels.add( TOKEN_KEYSCROLLOCK, 0x10000L + 116L );
        labels.add( TOKEN_KEYMETALEFT, 0x10000L + 117L );
        labels.add( TOKEN_KEYMETARIGHT, 0x10000L + 118L );
        labels.add( TOKEN_KEYFUNCTION, 0x10000L + 119L );
        labels.add( TOKEN_KEYSYSRQ, 0x10000L + 120L );
        labels.add( TOKEN_KEYBREAK, 0x10000L + 121L );
        labels.add( TOKEN_KEYMOVEHOME, 0x10000L + 122L );
        labels.add( TOKEN_KEYMOVEEND, 0x10000L + 123L );
        labels.add( TOKEN_KEYINS, 0x10000L + 124L );
        labels.add( TOKEN_KEYFORWARD, 0x10000L + 125L );
        labels.add( TOKEN_KEYPLAY, 0x10000L + 126L );
        labels.add( TOKEN_KEYPAUSE, 0x10000L + 127L );
        labels.add( TOKEN_KEYCLOSE, 0x10000L + 128L );
        labels.add( TOKEN_KEYEJECT, 0x10000L + 129L );
        labels.add( TOKEN_KEYREC, 0x10000L + 130L );
        labels.add( TOKEN_KEYF1, 0x10000L + 131L );
        labels.add( TOKEN_KEYF2, 0x10000L + 132L );
        labels.add( TOKEN_KEYF3, 0x10000L + 133L );
        labels.add( TOKEN_KEYF4, 0x10000L + 134L );
        labels.add( TOKEN_KEYF5, 0x10000L + 135L );
        labels.add( TOKEN_KEYF6, 0x10000L + 136L );
        labels.add( TOKEN_KEYF7, 0x10000L + 137L );
        labels.add( TOKEN_KEYF8, 0x10000L + 138L );
        labels.add( TOKEN_KEYF9, 0x10000L + 139L );
        labels.add( TOKEN_KEYF10, 0x10000L + 140L );
        labels.add( TOKEN_KEYF11, 0x10000L + 141L );
        labels.add( TOKEN_KEYF12, 0x10000L + 142L );
        labels.add( TOKEN_KEYNUMLOCK, 0x10000L + 143L );
        labels.add( TOKEN_KEYNUM0, 0x10000L + 144L );
        labels.add( TOKEN_KEYNUM1, 0x10000L + 145L );
        labels.add( TOKEN_KEYNUM2, 0x10000L + 146L );
        labels.add( TOKEN_KEYNUM3, 0x10000L + 147L );
        labels.add( TOKEN_KEYNUM4, 0x10000L + 148L );
        labels.add( TOKEN_KEYNUM5, 0x10000L + 149L );
        labels.add( TOKEN_KEYNUM6, 0x10000L + 150L );
        labels.add( TOKEN_KEYNUM7, 0x10000L + 151L );
        labels.add( TOKEN_KEYNUM8, 0x10000L + 152L );
        labels.add( TOKEN_KEYNUM9, 0x10000L + 153L );
        labels.add( TOKEN_KEYNUMDIV, 0x10000L + 154L );
        labels.add( TOKEN_KEYNUMSTAR, 0x10000L + 155L );
        labels.add( TOKEN_KEYNUMMINUS, 0x10000L + 156L );
        labels.add( TOKEN_KEYNUMPLUS, 0x10000L + 157L );
        labels.add( TOKEN_KEYNUMPERIOD, 0x10000L + 158L );
        labels.add( TOKEN_KEYNUMCOMMA, 0x10000L + 159L );
        labels.add( TOKEN_KEYNUMENTER, 0x10000L + 160L );
        labels.add( TOKEN_KEYNUMEQUAL, 0x10000L + 161L );
        labels.add( TOKEN_KEYNUMLPAR, 0x10000L + 162L );
        labels.add( TOKEN_KEYNUMRPAR, 0x10000L + 163L );
        labels.add( TOKEN_KEYVOLMUTE, 0x10000L + 164L );
        labels.add( TOKEN_KEYINFO, 0x10000L + 165L );
        labels.add( TOKEN_KEYCHUP, 0x10000L + 166L );
        labels.add( TOKEN_KEYCHDOWN, 0x10000L + 167L );
        labels.add( TOKEN_KEYZOOMIN, 0x10000L + 168L );
        labels.add( TOKEN_KEYZOOMOUT, 0x10000L + 169L );
        labels.add( TOKEN_KEYTV, 0x10000L + 170L );
        labels.add( TOKEN_KEYWIN, 0x10000L + 171L );
        labels.add( TOKEN_KEYGUIDE, 0x10000L + 172L );
        labels.add( TOKEN_KEYDVR, 0x10000L + 173L );
        labels.add( TOKEN_KEYBOOKMARK, 0x10000L + 174L );
        labels.add( TOKEN_KEYCAPTIONS, 0x10000L + 175L );
        labels.add( TOKEN_KEYSETTINGS, 0x10000L + 176L );
        labels.add( TOKEN_KEYTVPOWER, 0x10000L + 177L );
        labels.add( TOKEN_KEYTVINPUT, 0x10000L + 178L );
        labels.add( TOKEN_KEYSTBPOWER, 0x10000L + 179L );
        labels.add( TOKEN_KEYSTBINPUT, 0x10000L + 180L );
        labels.add( TOKEN_KEYAVRPOWER, 0x10000L + 181L );
        labels.add( TOKEN_KEYAVRINPUT, 0x10000L + 182L );
        labels.add( TOKEN_KEYPRGRED, 0x10000L + 183L );
        labels.add( TOKEN_KEYPRGGREEN, 0x10000L + 184L );
        labels.add( TOKEN_KEYPRGYELLOW, 0x10000L + 185L );
        labels.add( TOKEN_KEYPRGBLUE, 0x10000L + 186L );
        labels.add( TOKEN_KEYAPPSWITCH, 0x10000L + 187L );
        labels.add( TOKEN_KEYBUTTON1, 0x10000L + 188L );
        labels.add( TOKEN_KEYBUTTON2, 0x10000L + 189L );
        labels.add( TOKEN_KEYBUTTON3, 0x10000L + 190L );
        labels.add( TOKEN_KEYBUTTON4, 0x10000L + 191L );
        labels.add( TOKEN_KEYBUTTON5, 0x10000L + 192L );
        labels.add( TOKEN_KEYBUTTON6, 0x10000L + 193L );
        labels.add( TOKEN_KEYBUTTON7, 0x10000L + 194L );
        labels.add( TOKEN_KEYBUTTON8, 0x10000L + 195L );
        labels.add( TOKEN_KEYBUTTON9, 0x10000L + 196L );
        labels.add( TOKEN_KEYBUTTON10, 0x10000L + 197L );
        labels.add( TOKEN_KEYBUTTON11, 0x10000L + 198L );
        labels.add( TOKEN_KEYBUTTON12, 0x10000L + 199L );
        labels.add( TOKEN_KEYBUTTON13, 0x10000L + 200L );
        labels.add( TOKEN_KEYBUTTON14, 0x10000L + 201L );
        labels.add( TOKEN_KEYBUTTON15, 0x10000L + 202L );
        labels.add( TOKEN_KEYBUTTON16, 0x10000L + 203L );
        labels.add( TOKEN_KEYLANGUAGE, 0x10000L + 204L );
        labels.add( TOKEN_KEYMANNER, 0x10000L + 205L );
        labels.add( TOKEN_KEY3D, 0x10000L + 206L );
        labels.add( TOKEN_KEYCONTACTS, 0x10000L + 207L );
        labels.add( TOKEN_KEYCALENDAR, 0x10000L + 208L );
        labels.add( TOKEN_KEYMUSIC, 0x10000L + 209L );
        labels.add( TOKEN_KEYCALC, 0x10000L + 210L );
        labels.add( TOKEN_KEYKAKU, 0x10000L + 211L );
        labels.add( TOKEN_KEYEISU, 0x10000L + 212L );
        labels.add( TOKEN_KEYMUHENKAN, 0x10000L + 213L );
        labels.add( TOKEN_KEYHENKAN, 0x10000L + 214L );
        labels.add( TOKEN_KEYSWKANA, 0x10000L + 215L );
        labels.add( TOKEN_KEYYEN, 0x10000L + 216L );
        labels.add( TOKEN_KEYRO, 0x10000L + 217L );
        labels.add( TOKEN_KEYKANA, 0x10000L + 218L );
        labels.add( TOKEN_KEYASSIST, 0x10000L + 219L );
        labels.add( TOKEN_KEYBRGDOWN, 0x10000L + 220L );
        labels.add( TOKEN_KEYBRGUP, 0x10000L + 221L );
        labels.add( TOKEN_KEYMATRACE, 0x10000L + 222L );
        labels.add( TOKEN_KEYSLEEP, 0x10000L + 223L );
        labels.add( TOKEN_KEYWAKE, 0x10000L + 224L );
        labels.add( TOKEN_KEYPAIR, 0x10000L + 225L );
        labels.add( TOKEN_KEYMEDIATOP, 0x10000L + 226L );
        labels.add( TOKEN_KEY11, 0x10000L + 227L );
        labels.add( TOKEN_KEY12, 0x10000L + 228L );
        labels.add( TOKEN_KEYCHLAST, 0x10000L + 229L );
        labels.add( TOKEN_KEYTVDATA, 0x10000L + 230L );
        labels.add( TOKEN_KEYVOICEASST, 0x10000L + 231L );
        labels.add( TOKEN_KEYTVRADIO, 0x10000L + 232L );
        labels.add( TOKEN_KEYTVTEXT, 0x10000L + 233L );
        labels.add( TOKEN_KEYTVNUMENT, 0x10000L + 234L );
        labels.add( TOKEN_KEYTVTERRANA, 0x10000L + 235L );
        labels.add( TOKEN_KEYTVTERRDIA, 0x10000L + 236L );
        labels.add( TOKEN_KEYTVSAT, 0x10000L + 237L );
        labels.add( TOKEN_KEYTVSATBS, 0x10000L + 238L );
        labels.add( TOKEN_KEYTVSATCS, 0x10000L + 239L );
        labels.add( TOKEN_KEYTVSATSERV, 0x10000L + 240L );
        labels.add( TOKEN_KEYTVNETWORK, 0x10000L + 241L );
        labels.add( TOKEN_KEYTVANTCABL, 0x10000L + 242L );
        labels.add( TOKEN_KEYTVHDMI1, 0x10000L + 243L );
        labels.add( TOKEN_KEYTVHDMI2, 0x10000L + 244L );
        labels.add( TOKEN_KEYTVHDMI3, 0x10000L + 245L );
        labels.add( TOKEN_KEYTVHDMI4, 0x10000L + 246L );
        labels.add( TOKEN_KEYTVCMPSIT1, 0x10000L + 247L );
        labels.add( TOKEN_KEYTVCMPSIT2, 0x10000L + 248L );
        labels.add( TOKEN_KEYTVCOMP1, 0x10000L + 249L );
        labels.add( TOKEN_KEYTVCOMP2, 0x10000L + 250L );
        labels.add( TOKEN_KEYTVVGA1, 0x10000L + 251L );
        labels.add( TOKEN_KEYTVAUMIX, 0x10000L + 252L );
        labels.add( TOKEN_KEYTVAUMIXUP, 0x10000L + 253L );
        labels.add( TOKEN_KEYTVAUMIXDN, 0x10000L + 254L );
        labels.add( TOKEN_KEYTVZOOMMOD, 0x10000L + 255L );
        labels.add( TOKEN_KEYTVCONTMNU, 0x10000L + 256L );
        labels.add( TOKEN_KEYTVMEDCNTX, 0x10000L + 257L );
        labels.add( TOKEN_KEYTVTIMER, 0x10000L + 258L );
        labels.add( TOKEN_KEYHELP, 0x10000L + 259L );

        return labels;
        }


    /**
     * Commands's data consists of:
     * long groupCode - parameter is stored under groupCode
     * (Single params: only one parameter is allowed under one groupCode,
     *  Multiple params: all parameters are listed together under the same groupCode)
     * long parameters[] - allowed parameters (token-codes of allowed parameter-commands) or
     * special parameters as negative values.
     * Array cannot be null or empty; at least one item is needed!
     * NO_PARAMETERS value means: no parameter needed
     * Method method - method of SoftBoardData, which should be called
     * (method can be null: no method is called)
     */
    public static class Data
        {
        private long groupCode;
        private long params[];
        private Method method;

        private Data( long groupCode, long[] params, String methodName )
            {
            this.groupCode = groupCode;
            this.params = params;

            // if no method to call;
            if ( methodName == null )
                {
                method = null;
                return;
                }

            // Different types of methods should be called - according to parameter type
            // Check SoftBoardParser.parseComplexParameter, third part!
            // ?? Some kind of is...Type() or getMainType() methods could be helpful ??

            try
                {
                // Parameter-command has COMPLEX parameters - forwardParamers
                if (getParameterType() >= Tokenizer.TOKEN_CODE_SHIFT || getParameterType() < 0x0L )
                    method = MethodsForCommands.class.getDeclaredMethod(methodName, ExtendedMap.class);
                // Parameter-command has ONE parameter - result
                else if (getParameterType() <= Commands.PARAMETER_KEYWORD)
                    method = MethodsForCommands.class.getDeclaredMethod(methodName, Object.class);
                // Parameter-command has LIST parameter - result
                else if (getParameterType() <= (Commands.PARAMETER_KEYWORD | Commands.PARAMETER_MOD_LIST))
                    method = MethodsForCommands.class.getDeclaredMethod(methodName, List.class);
                // Parameter-command has LABEL parameter
                else if (getParameterType() == Commands.PARAMETER_LABEL)
                // Parameter-command has NO parameters - no parameters
                    method = null;
                else // FLAG or NO parameters
                    method = MethodsForCommands.class.getDeclaredMethod(methodName);
                }
            catch (NoSuchMethodException e)
                {
                method = null;
                Scribe.error("Method " + methodName + " is missing in MethodsForCommands!");
                }
            }

        /**
         * First allowed parameter determines the type of the parameter list
         * The first allowed parameter cannot be null!
         * NO_PARAMETERS is needed if there are no allowed parameters
         * @return type of parameter-list
         */
        public long getParameterType()
            {
            return params[0];
            // !! Size check could be performed,
            // and return NO_PARAMETERS in case of empty/missing params array !!
            }

        /**
         * Allowed parameters for the parameter-list
         * @return allowed parameters
         */
        public long[] getAllowedParameters()
            {
            return params;
            }

        /**
         * If this command will call a method in SoftBoardData class after getting parameter-list
         * @return true if method is supplied
         */
        public boolean hasMethodToCall()
            {
            return method != null;
            }

        /**
         * Get the method to call in SoftBoardData class.
         * Parameters are forwarded as a Map to the method.
         * @return the method to call
         */
        public Method getMethod()
            {
            return method;
            }

        /**
         * Commands are stored under group-code in the complex parameters.
         * @return group code
         */
        public long getGroupCode()
            {
            return groupCode;
            }
        }

    }

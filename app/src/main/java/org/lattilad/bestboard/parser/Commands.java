package org.lattilad.bestboard.parser;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.ArrayUtils;
import org.lattilad.bestboard.utils.ExtendedMap;

import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public static final long TOKEN_DEFINE = 0x3758b594L;

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
    public static final long TOKEN_FONT = 0xd1bbaL;

    public static final long TOKEN_SHOWTITLES = 0xd260affc0215fL;
    public static final long TOKEN_ENTERTEXT = 0x2ec74ff10e17L;
    public static final long TOKEN_GOTEXT = 0x44e7bdb0L;
    public static final long TOKEN_SEARCHTEXT = 0xd1b3cdb202a54L;
    public static final long TOKEN_SENDTEXT = 0x273a047df92L;
    public static final long TOKEN_NEXTTEXT = 0x205477f09b3L;
    public static final long TOKEN_DONETEXT = 0x12e0df8ec9aL;
    public static final long TOKEN_PREVTEXT = 0x238f207a215L;
    public static final long TOKEN_NONETEXT = 0x20b15c7cf1cL;
    public static final long TOKEN_UNKNOWNTEXT = 0x20b61fc10341947L;
    public static final long TOKEN_AUTOFUNCON = 0x50064b8d2462fL;
    public static final long TOKEN_AUTOFUNCOFF = 0xb90e8eb64023d6L;

    public static final long TOKEN_ADDBOARD = 0xe502ed0208L;
    public static final long TOKEN_ID = 0x102a6L;
    public static final long TOKEN_LAYOUT = 0x5805f907L;
    public static final long TOKEN_LANDSCAPE = 0x44010ff937b3L;
    public static final long TOKEN_PORTRAIT = 0x2375cbe8760L;
    public static final long TOKEN_LOCK = 0x11bd48L;
    public static final long TOKEN_START = 0x3385de2L;

    public static final long TOKEN_ADDLAYOUT = 0x211993f479afL;
    // public static final long TOKEN_ID = 0x102a6L;
    public static final long TOKEN_HEXAGONAL = 0x379824b9b62fL;
    public static final long TOKEN_WIDE = 0x1a1dd0L;
    public static final long TOKEN_HALFCOLUMNS = 0x1274de67161b456L;
    public static final long TOKEN_COLUMNS = 0x790ca4223L;
    public static final long TOKEN_ROWS = 0x166362L;
    public static final long TOKEN_ALIGN = 0x12f9733L;
    public static final long TOKEN_COLOR = 0x16b2be3L;

    public static final long TOKEN_ODDS = 0x13d439L;
    public static final long TOKEN_EVENS = 0x1a9a13dL;

    // public static final long TOKEN_FORCEON = 0x95c3165f2L;
    // public static final long TOKEN_FORCEOFF = 0x15a52ffbb05L;

    // public static final long TOKEN_FORCECAPS = 0x320dfec8a852L;
    // public static final long TOKEN_FORCESHIFT = 0x73c05d4ab24e6L;
    // public static final long TOKEN_FORCECTRL = 0x320dfec90e30L;
    // public static final long TOKEN_FORCEALT = 0x15a52ff7113L;

    public static final long TOKEN_ASBOARD = 0x66e540fa6L;
    // public static final long TOKEN_LOCK = 0x11bd48L;
    // public static final long TOKEN_START = 0x3385de2L;

    public static final long TOKEN_BLOCK = 0x14c4fa3L;
    // public static final long TOKEN_LAYOUT = 0x5805f907L;
    public static final long TOKEN_COLUMN = 0x34597767L;
    public static final long TOKEN_ROW = 0x193faL;
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

    public static final long TOKEN_PACKET = 0x687d4ba9L;
    public static final long TOKEN_FIRST = 0x1bc7434L;
    public static final long TOKEN_SECOND = 0x7556168dL;

    public static final long TOKEN_TEXT = 0x17b9c8L;
    public static final long TOKEN_KEY = 0x16d1bL;
    public static final long TOKEN_COMBINE = 0x790d8163bL;
    public static final long TOKEN_TIME = 0x17cd86L;
    public static final long TOKEN_FORMAT = 0x40c43e96L;

    public static final long TOKEN_DELETE = 0x375d443cL;
    public static final long TOKEN_BACKSPACE = 0x240879d29871L;
    public static final long TOKEN_DRAFT = 0x189da4dL;
    public static final long TOKEN_SETTINGS = 0x273bad5bcccL;
    public static final long TOKEN_SELECTALL = 0x5ab4faaa9d61L;
    public static final long TOKEN_CAPITALIZE = 0x5ac8ad6372377L;

    public static final long TOKEN_TOGGLE = 0x7a99172fL;
    public static final long TOKEN_TURNON = 0x7b4d533bL;
    public static final long TOKEN_TURNOFF = 0x11d2090692L;

    public static final long TOKEN_AUTOFUNC = 0xef6e47d688L;
    // public static final long TOKEN_CURSOR = 0x3509a5d7L;

    public static final long TOKEN_TOP = 0x19ea5L;
    public static final long TOKEN_LEFT = 0x118846L;
    public static final long TOKEN_RIGHT = 0x3136316L;
    public static final long TOKEN_BOTTOM = 0x303d866aL;
    public static final long TOKEN_WORD = 0x1a3febL;
    public static final long TOKEN_PARA = 0x148bffL;
    public static final long TOKEN_CURSOR = 0x3509a5d7L;
    public static final long TOKEN_SELECT = 0x755cd451L;
    public static final long TOKEN_LAST = 0x1174c3L;
    public static final long TOKEN_BEGIN = 0x146bcb1L;
    public static final long TOKEN_END = 0x14e3dL;
    public static final long TOKEN_ALWAYS = 0x2bc78e36L;
    public static final long TOKEN_NEVER = 0x29e37a4L;
    public static final long TOKEN_IFSHIFT = 0xb01e8a12fL;

    public static final long TOKEN_GETFIRST = 0x16a7cb39a23L;
    public static final long TOKEN_GETSECOND = 0x34643b0e9218L;

    // public static final long TOKEN_AUTOFUNC = 0xef6e47d688L;
    // public static final long TOKEN_ENTER = 0x1a3c13eL;

    public static final long TOKEN_AUTOCAPS = 0xef6e451a57L;
    public static final long TOKEN_STRINGCAPS = 0xd4c9a99e4004bL;
    public static final long TOKEN_AUTOSPACE = 0x229af1ada341L;
    public static final long TOKEN_ERASESPACE = 0x6cdfa00056559L;

    public static final long TOKEN_ON = 0x1038eL;
    public static final long TOKEN_OFF = 0x18291L;
    public static final long TOKEN_WAIT = 0x19f3d0L;
    public static final long TOKEN_HOLD = 0xea71aL;

    public static final long TOKEN_AFTER = 0x12b2e92L;
    public static final long TOKEN_BEFORE = 0x2f14a094L;
    public static final long TOKEN_AROUND = 0x2c6d5e42L;

    // public static final long TOKEN_FORCECAPS = 0x320dfec8a852L;
    // public static final long TOKEN_FORCESHIFT = 0x73c05d4ab24e6L;
    // public static final long TOKEN_FORCECTRL = 0x320dfec90e30L;
    // public static final long TOKEN_FORCEALT = 0x15a52ff7113L;

    public static final long TOKEN_BUTTON = 0x30e91c11L;

    public static final long TOKEN_ONSTAY = 0x65db8273L;
    public static final long TOKEN_ONCIRCLE = 0x2206a350fc6L;
    public static final long TOKEN_OVERWRITE = 0x4f61843c6a0fL;

    public static final long TOKEN_SINGLE = 0x75d0cfbfL;
    public static final long TOKEN_REPEAT = 0x713dd0a6L;
    public static final long TOKEN_TWIN = 0x1817d9L;
    public static final long TOKEN_CAPITAL = 0x757560b6cL;

    public static final long TOKEN_DOUBLE = 0x38822138L;

    public static final long TOKEN_ALTERNATE = 0x21d3dacd0f44L;

    public static final long TOKEN_MULTI = 0x28dc92bL;
    public static final long TOKEN_ADD = 0x13767L;
    public static final long TOKEN_LIST = 0x119f8bL;
    public static final long TOKEN_ADDTEXT = 0x63091a2b0L;

    public static final long TOKEN_SWITCH = 0x775d93d7L;
    public static final long TOKEN_BOARD = 0x14e5880L;
    // public static final long TOKEN_LOCK = 0x11bd48L;
    public static final long TOKEN_CAPSSTATE = 0x27422f120618L;
    // !! BACK is used as textual token !!
    public static final long TOKEN_BACK = 0x9b7c8L;

    public static final long TOKEN_META = 0x125016L;
    public static final long TOKEN_CAPS = 0xa7f8eL;
    public static final long TOKEN_CTRL = 0xae56cL;
    public static final long TOKEN_ALT = 0x1389fL;
    public static final long TOKEN_SHIFT = 0x32f4092L;
    // public static final long TOKEN_LOCK = 0x11bd48L;

    public static final long TOKEN_MEMORY = 0x5c9130daL;

    public static final long TOKEN_SPACETRAVEL = 0x1ea02b357b37bacL;

    public static final long TOKEN_ENTER = 0x1a3c13eL;
    // public static final long TOKEN_REPEAT = 0x713dd0a6L;

    public static final long TOKEN_MODIFY = 0x5da813adL;
    public static final long TOKEN_REVERSE = 0x105e77189aL;
    public static final long TOKEN_ROLL = 0x1661c4L;

    public static final long TOKEN_ADDTITLE = 0xe504eb848aL;
    // public static final long TOKEN_TEXT = 0x17b9c8L;
    public static final long TOKEN_SHOW = 0x1702acL;
    public static final long TOKEN_XOFFSET = 0x141b96a3d1L;
    public static final long TOKEN_YOFFSET = 0x14b484849aL;
    public static final long TOKEN_SIZE = 0x17098aL;
    public static final long TOKEN_BOLD = 0xa03ecL;
    public static final long TOKEN_ITALICS = 0xb39c66ee7L;
    public static final long TOKEN_NONBOLD = 0xe232d779aL;
    public static final long TOKEN_NONITALICS = 0xaed49d766321dL;
    // public static final long TOKEN_COLOR = 0x16b2be3L;

    public static final long TOKEN_EXTEND = 0x3da4e6fdL;
    public static final long TOKEN_TOMULTI = 0x11b8b2c3e8L;
    public static final long TOKEN_TOLIST = 0x7a9d0044L;
    public static final long TOKEN_TODOUBLE = 0x28f8bda5e89L;
    public static final long TOKEN_TOALTERNATE = 0x1faa2b1b5295331L;

    public static final long TOKEN_ADDMODIFY = 0x211999969455L;
    // public static final long TOKEN_ID = 0x102a6L;
    public static final long TOKEN_ROLLS = 0x3182194L;
    public static final long TOKEN_ADDROLL = 0x630904aacL;
    public static final long TOKEN_IGNORESPACE = 0x13b2fae0bc2c2ceL;
    // public static final long TOKEN_REVERSE = 0x105e77189aL;

    public static final long TOKEN_MONITOR = 0xd8a451b16L;
    // public static final long TOKEN_LAYOUT = 0x5805f907L;
    // public static final long TOKEN_SIZE = 0x17098aL;
    // public static final long TOKEN_BOLD = 0xa03ecL;
    // public static final long TOKEN_ITALICS = 0xb39c66ee7L;
    // public static final long TOKEN_NONBOLD = 0xe232d779aL;
    // public static final long TOKEN_NONITALICS = 0xaed49d766321dL;
    // public static final long TOKEN_COLOR = 0x16b2be3L;

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

    /**
     * Parameter-commands are stored in an unmodifiable hash-HashMap (LIST)
     * as long token-code key (the command itself) and as Data value (command's data) pairs.
     */
    private static Map<Long, Data> LIST = createDataMap();


    private static Data add( long tokenCode, long[] params )
        {
        Data data = new Data(tokenCode, params );
        if ( LIST.put( tokenCode, data) != null )
            {
            Scribe.error("Please, check COMMANDS! " + tokenCode +
                    " [" + Tokenizer.regenerateKeyword(tokenCode) + "] has multiple definitions!");
            }
        return data;
        }

    private static Data add( long tokenCode, long param )
        {
        long[] params = new long[1];
        params[0] = param;
        return add(tokenCode, params);
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
                TOKEN_BLOCK,
                TOKEN_ADDLAYOUT,
                TOKEN_ADDBOARD,

                TOKEN_LET,
                TOKEN_DEFINE,
                TOKEN_DEFAULT,

                TOKEN_ADDMODIFY,
                TOKEN_MONITOR,

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
                TOKEN_FONT,

                TOKEN_SHOWTITLES,

                TOKEN_STOP
        });

        add(TOKEN_DEFAULT, new long[]{PARAMETER_DEFAULT});
        add(TOKEN_LET, new long[]{PARAMETER_CHANGE_LABEL});
        add(TOKEN_DEFINE, new long[]{PARAMETER_LABEL});

        add(TOKEN_NAME, PARAMETER_STRING).method("setName");
        add(TOKEN_VERSION, PARAMETER_INT).method("setVersion");
        add(TOKEN_AUTHOR, PARAMETER_STRING).method("setAuthor");
        add(TOKEN_ADDTAGS, (PARAMETER_STRING | PARAMETER_MOD_LIST)).method("addTags");
        add(TOKEN_DESCRIPTION, PARAMETER_STRING).method("setDescription");
        add(TOKEN_DOCFILE, PARAMETER_FILE).method("setDocFile");
        add(TOKEN_DOCURI, PARAMETER_STRING).method("setDocUri");

        add(TOKEN_LOCALE, new long[]{
                TOKEN_LANGUAGE, TOKEN_COUNTRY, TOKEN_VARIANT}).method("setLocale");
        add(TOKEN_LANGUAGE, PARAMETER_STRING);
        add(TOKEN_COUNTRY, PARAMETER_STRING);
        add(TOKEN_VARIANT, PARAMETER_STRING);

        add(TOKEN_METACOLOR, PARAMETER_COLOR).method("setMetaColor");
        add(TOKEN_LOCKCOLOR, PARAMETER_COLOR).method("setLockColor");
        add(TOKEN_AUTOCOLOR, PARAMETER_COLOR).method("setAutoColor");
        add(TOKEN_TOUCHCOLOR, PARAMETER_COLOR).method("setTouchColor");
        add(TOKEN_STROKECOLOR, PARAMETER_COLOR).method("setStrokeColor");
        add(TOKEN_FONT, PARAMETER_FILE).method("setTypeface");

        add(TOKEN_SHOWTITLES, new long[]{
                TOKEN_ENTERTEXT, TOKEN_GOTEXT, TOKEN_SEARCHTEXT, TOKEN_SENDTEXT, TOKEN_NEXTTEXT,
                TOKEN_DONETEXT, TOKEN_PREVTEXT, TOKEN_NONETEXT, TOKEN_UNKNOWNTEXT,
                TOKEN_AUTOFUNCON, TOKEN_AUTOFUNCOFF })
                .method("setShowTitles").allowAsLabel().allowAsDefault();

        add(TOKEN_ENTERTEXT, PARAMETER_TEXT);
        add(TOKEN_GOTEXT, PARAMETER_TEXT);
        add(TOKEN_SEARCHTEXT, PARAMETER_TEXT);
        add(TOKEN_SENDTEXT, PARAMETER_TEXT);
        add(TOKEN_NEXTTEXT, PARAMETER_TEXT);
        add(TOKEN_DONETEXT, PARAMETER_TEXT);
        add(TOKEN_PREVTEXT, PARAMETER_TEXT);
        add(TOKEN_NONETEXT, PARAMETER_TEXT);
        add(TOKEN_UNKNOWNTEXT, PARAMETER_TEXT);
        add(TOKEN_AUTOFUNCON, PARAMETER_TEXT);
        add(TOKEN_AUTOFUNCOFF, PARAMETER_TEXT);

        add(TOKEN_ADDBOARD, new long[]{
                TOKEN_ID,
                TOKEN_LAYOUT,
                TOKEN_PORTRAIT, TOKEN_LANDSCAPE,
                TOKEN_LOCK, TOKEN_START})
                .method("addBoard").allowAsLabel().allowAsDefault();

        add(TOKEN_ID, PARAMETER_KEYWORD);
        add(TOKEN_LAYOUT, PARAMETER_KEYWORD);
        add(TOKEN_PORTRAIT, PARAMETER_KEYWORD);
        add(TOKEN_LANDSCAPE, PARAMETER_KEYWORD);
        add(TOKEN_LOCK, PARAMETER_FLAG);
        add(TOKEN_START, PARAMETER_FLAG);

        add(TOKEN_ADDLAYOUT, new long[]{
                TOKEN_ID, TOKEN_HEXAGONAL, TOKEN_WIDE,
                TOKEN_COLUMNS, TOKEN_HALFCOLUMNS, TOKEN_ROWS,
                TOKEN_ALIGN, TOKEN_COLOR,
                TOKEN_TURNON, TOKEN_TURNOFF,
                TOKEN_ASBOARD,
                TOKEN_LOCK, TOKEN_START})
                .method("addLayout").allowAsLabel().allowAsDefault();

        // add(TOKEN_ID, PARAMETER_KEYWORD);
        add(TOKEN_HEXAGONAL, NO_PARAMETERS); // Useless parametercommand - just for clearer readability
        add(TOKEN_WIDE, PARAMETER_FLAG);
        add(TOKEN_COLUMNS, PARAMETER_INT);
        add(TOKEN_HALFCOLUMNS, PARAMETER_INT);
        add(TOKEN_ROWS, PARAMETER_INT);
        add(TOKEN_ALIGN, PARAMETER_KEYWORD );
        add(TOKEN_COLOR, PARAMETER_COLOR);

        add(TOKEN_TURNON, PARAMETER_KEYWORD | PARAMETER_MOD_LIST );
        add(TOKEN_TURNOFF, PARAMETER_KEYWORD | PARAMETER_MOD_LIST);

        /* add(TOKEN_FORCECAPS, PARAMETER_BOOLEAN);
           add(TOKEN_FORCESHIFT, PARAMETER_BOOLEAN);
           add(TOKEN_FORCECTRL, PARAMETER_BOOLEAN);
           add(TOKEN_FORCEALT, PARAMETER_BOOLEAN); */

        add(TOKEN_ASBOARD, PARAMETER_FLAG);
        // If ASBOARD is given, then addBoard is called, so LOCK and ROOT can be used, too
        // add(TOKEN_LOCK, PARAMETER_FLAG);
        // add(TOKEN_START, PARAMETER_FLAG);

        add(TOKEN_BLOCK, new long[]{
                TOKEN_BUTTON | PARAMETER_MOD_MULTIPLE,
                TOKEN_SINGLE | PARAMETER_MOD_MULTIPLE,
                TOKEN_DOUBLE | PARAMETER_MOD_MULTIPLE,
                TOKEN_ALTERNATE | PARAMETER_MOD_MULTIPLE,
                TOKEN_LIST | PARAMETER_MOD_MULTIPLE,
                TOKEN_MULTI | PARAMETER_MOD_MULTIPLE,
                TOKEN_MODIFY | PARAMETER_MOD_MULTIPLE,
                TOKEN_SPACETRAVEL | PARAMETER_MOD_MULTIPLE,
                TOKEN_MEMORY | PARAMETER_MOD_MULTIPLE,
                TOKEN_ENTER | PARAMETER_MOD_MULTIPLE,
                TOKEN_META | PARAMETER_MOD_MULTIPLE,
                TOKEN_SWITCH | PARAMETER_MOD_MULTIPLE,
                TOKEN_SKIP | PARAMETER_MOD_MULTIPLE,
                TOKEN_DL | PARAMETER_MOD_MULTIPLE,
                TOKEN_DR | PARAMETER_MOD_MULTIPLE,
                TOKEN_CRL | PARAMETER_MOD_MULTIPLE,
                TOKEN_CRR | PARAMETER_MOD_MULTIPLE,
                TOKEN_L | PARAMETER_MOD_MULTIPLE,
                TOKEN_R | PARAMETER_MOD_MULTIPLE,
                TOKEN_UL | PARAMETER_MOD_MULTIPLE,
                TOKEN_UR | PARAMETER_MOD_MULTIPLE,
                TOKEN_FINDFREE | PARAMETER_MOD_MULTIPLE,
                TOKEN_HOME | PARAMETER_MOD_MULTIPLE,
                TOKEN_EXTEND | PARAMETER_MOD_MULTIPLE,
                TOKEN_LAYOUT,
                TOKEN_COLUMN,
                TOKEN_ROW })
                .method("setBlock").allowAsLabel().allowAsDefault();

        // add(TOKEN_LAYOUT, PARAMETER_KEYWORD);
        add(TOKEN_COLUMN, PARAMETER_INT);
        add(TOKEN_ROW, PARAMETER_INT);

        add(TOKEN_L, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_R, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_DL, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_DR, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_UL, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_UR, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_CRL, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_CRR, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_FINDFREE, PARAMETER_FLAG).group(TOKEN_BUTTON);
        add(TOKEN_SKIP, PARAMETER_INT).group(TOKEN_BUTTON);
        add(TOKEN_HOME, PARAMETER_FLAG).group(TOKEN_BUTTON);

        // Packet definitions
        // ***************************************

        long[] packetArray = new long[]{
                TOKEN_TEXT,
                TOKEN_AUTOCAPS,
                TOKEN_STRINGCAPS,
                TOKEN_ERASESPACE,
                TOKEN_AUTOSPACE,
                TOKEN_KEY,
                TOKEN_TURNON, TOKEN_TURNOFF,
                TOKEN_DELETE, TOKEN_BACKSPACE, TOKEN_TOGGLE, TOKEN_SELECTALL, TOKEN_CAPITALIZE, TOKEN_DRAFT, TOKEN_SETTINGS,
                TOKEN_TOP, TOKEN_LEFT, TOKEN_RIGHT, TOKEN_BOTTOM, TOKEN_WORD, TOKEN_PARA, TOKEN_CURSOR, TOKEN_SELECT,

                TOKEN_COMBINE,
                TOKEN_TIME, TOKEN_FORMAT};


        add(TOKEN_PACKET, packetArray)
                .allowAsLabel().allowAsDefault()
                .method("packet");

        add(TOKEN_FIRST, packetArray)
                .allowAsLabel().allowAsDefault().labels(new long[]{TOKEN_PACKET})
                .method("packet");

        add(TOKEN_SECOND, packetArray)
                .allowAsLabel().allowAsDefault().labels(new long[]{TOKEN_PACKET})
                .method("packet");

        add(TOKEN_TEXT, PARAMETER_TEXT);
        add(TOKEN_KEY, PARAMETER_INT);
        add(TOKEN_COMBINE, PARAMETER_FLAG);

        add(TOKEN_DELETE, PARAMETER_FLAG);
        add(TOKEN_BACKSPACE, PARAMETER_FLAG);
        add(TOKEN_DRAFT, PARAMETER_FLAG);
        add(TOKEN_SETTINGS, PARAMETER_FLAG);
        add(TOKEN_TOGGLE, PARAMETER_KEYWORD);
        add(TOKEN_SELECTALL, PARAMETER_FLAG);
        add(TOKEN_CAPITALIZE, PARAMETER_FLAG);

        add(TOKEN_TOP, PARAMETER_FLAG);
        add(TOKEN_LEFT, PARAMETER_FLAG);
        add(TOKEN_RIGHT, PARAMETER_FLAG);
        add(TOKEN_BOTTOM, PARAMETER_FLAG);
        add(TOKEN_WORD, PARAMETER_FLAG);
        add(TOKEN_PARA, PARAMETER_FLAG);
        add(TOKEN_CURSOR, PARAMETER_KEYWORD);
        add(TOKEN_SELECT, PARAMETER_KEYWORD);

        add(TOKEN_AUTOCAPS, PARAMETER_KEYWORD);
        add(TOKEN_STRINGCAPS, PARAMETER_FLAG);
        add(TOKEN_AUTOSPACE, PARAMETER_KEYWORD);
        add(TOKEN_ERASESPACE, PARAMETER_KEYWORD);

        // add(TOKEN_FORCECAPS, PARAMETER_BOOLEAN);
        // add(TOKEN_FORCESHIFT, PARAMETER_BOOLEAN);
        // add(TOKEN_FORCECTRL, PARAMETER_BOOLEAN);
        // add(TOKEN_FORCEALT, PARAMETER_BOOLEAN);

        add(TOKEN_TIME, PARAMETER_FLAG);
        add(TOKEN_FORMAT, PARAMETER_TEXT);

        // Button definitions
        // ***************************************

        long[] buttonArray = new long[]{
                TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,
                TOKEN_COLOR,
                TOKEN_ONSTAY,
                TOKEN_ONCIRCLE,
                TOKEN_OVERWRITE};

        add(TOKEN_BUTTON, ArrayUtils.concat( packetArray, buttonArray, new long[]{
                TOKEN_FIRST,
                TOKEN_SECOND,
                TOKEN_REPEAT, TOKEN_TWIN, TOKEN_CAPITAL }))
                .method("setButton").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_PACKET, TOKEN_FIRST, TOKEN_SECOND});

        add(TOKEN_SINGLE, ArrayUtils.concat(packetArray, buttonArray, new long[]{
                TOKEN_FIRST,
                TOKEN_REPEAT, TOKEN_TWIN, TOKEN_CAPITAL }))
                .method("setSingle").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET, TOKEN_FIRST, TOKEN_SECOND});

        add(TOKEN_REPEAT, PARAMETER_FLAG);
        add(TOKEN_TWIN, PARAMETER_FLAG);
        add(TOKEN_CAPITAL, PARAMETER_FLAG);

        add(TOKEN_DOUBLE, ArrayUtils.concat(packetArray, buttonArray, new long[]{
                TOKEN_FIRST,
                TOKEN_SECOND }))
                .method("setDouble").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET, TOKEN_FIRST, TOKEN_SECOND});

        add(TOKEN_ALTERNATE, ArrayUtils.concat(packetArray, buttonArray, new long[]{
                TOKEN_FIRST,
                TOKEN_SECOND }))
                .method("setAlternate").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET, TOKEN_FIRST, TOKEN_SECOND});

        add(TOKEN_MULTI, ArrayUtils.concat(buttonArray, new long[]{
                TOKEN_ADD | PARAMETER_MOD_MULTIPLE }))
                .method("setMulti").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON});

        add(TOKEN_ADD, new long[]{
                TOKEN_TEXT,
                TOKEN_AUTOCAPS,
                TOKEN_STRINGCAPS,
                TOKEN_ERASESPACE,
                TOKEN_AUTOSPACE,
                TOKEN_TIME, TOKEN_FORMAT,

                // If only Text-packet is allowed here, then rem next rows!!
                /* TOKEN_KEY,
                TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT,
                TOKEN_DO */}).method("packet").allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_PACKET});;

        add(TOKEN_LIST, ArrayUtils.concat(buttonArray, new long[]{
                TOKEN_TEXT,
                TOKEN_AUTOCAPS,
                TOKEN_STRINGCAPS,
                TOKEN_ERASESPACE,
                TOKEN_AUTOSPACE,
                TOKEN_ADDTEXT | PARAMETER_MOD_MULTIPLE,
                TOKEN_SECOND}))
                .method("setList").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET, TOKEN_SECOND });

        add(TOKEN_ADDTEXT, PARAMETER_TEXT);

        add(TOKEN_MODIFY, new long[]{
                TOKEN_ROLL,
                TOKEN_REVERSE,

                TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,
                TOKEN_COLOR,
                // TOKEN_ONSTAY,
                // TOKEN_ONCIRCLE,
                TOKEN_OVERWRITE})
                .method("setModify").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault().labels(new long[]{TOKEN_BUTTON});

        add(TOKEN_ROLL, PARAMETER_KEYWORD);
        add(TOKEN_REVERSE, PARAMETER_FLAG);

        add(TOKEN_ENTER, ArrayUtils.concat( buttonArray, new long[]{
                TOKEN_TEXT,
                TOKEN_AUTOCAPS,
                TOKEN_STRINGCAPS,
                TOKEN_ERASESPACE,
                TOKEN_AUTOSPACE,
                TOKEN_KEY,
                TOKEN_TURNON, TOKEN_TURNOFF,
                TOKEN_REPEAT }))
                .method("setEnter").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET});

        // add(TOKEN_REPEAT, PARAMETER_FLAG);

        add(TOKEN_SPACETRAVEL, ArrayUtils.concat(packetArray, buttonArray, new long[]{
                TOKEN_SECOND }))
                .method("setSpaceTravel").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET});

        add(TOKEN_MEMORY, ArrayUtils.concat(buttonArray, new long[]{
                TOKEN_TEXT,
                TOKEN_AUTOCAPS,
                TOKEN_STRINGCAPS,
                TOKEN_ERASESPACE,
                TOKEN_AUTOSPACE}))
                .method("setMemory").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault()
                .labels(new long[]{TOKEN_BUTTON, TOKEN_PACKET});

        add(TOKEN_META, new long[]{
                TOKEN_CAPS,
                TOKEN_SHIFT,
                TOKEN_CTRL,
                TOKEN_ALT,
                TOKEN_LOCK,

                TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,
                        TOKEN_COLOR,
                TOKEN_OVERWRITE})
                .method("setMeta").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault().labels(new long[]{TOKEN_BUTTON});

        add(TOKEN_CAPS, PARAMETER_FLAG);
        add(TOKEN_SHIFT, PARAMETER_FLAG);
        add(TOKEN_CTRL, PARAMETER_FLAG);
        add(TOKEN_ALT, PARAMETER_FLAG);
        // add(TOKEN_LOCK, PARAMETER_FLAG);

        add(TOKEN_SWITCH, new long[]{
                TOKEN_BOARD,
                TOKEN_BACK,
                TOKEN_LOCK,
                TOKEN_CAPSSTATE,
                TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,
                        TOKEN_COLOR,
                TOKEN_OVERWRITE})
                .method("setSwitch").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault().labels(new long[]{TOKEN_BUTTON});

        add(TOKEN_BOARD, PARAMETER_KEYWORD);
        add(TOKEN_BACK, PARAMETER_FLAG);
        // add(TOKEN_LOCK, PARAMETER_FLAG);
        add(TOKEN_CAPSSTATE, PARAMETER_FLAG);

        add(TOKEN_ADDTITLE, new long[]{
                TOKEN_TEXT, TOKEN_GETFIRST, TOKEN_GETSECOND,
                TOKEN_SHOW,
                TOKEN_XOFFSET, TOKEN_YOFFSET, TOKEN_SIZE,
                TOKEN_BOLD, TOKEN_NONBOLD, TOKEN_ITALICS, TOKEN_NONITALICS,
                TOKEN_COLOR })
                .method("addTitle").allowAsLabel().allowAsDefault();
        // add(TOKEN_TEXT, PARAMETER_TEXT);
        add(TOKEN_SHOW, PARAMETER_KEYWORD);
        add(TOKEN_GETFIRST, PARAMETER_FLAG);
        add(TOKEN_GETSECOND, PARAMETER_FLAG);
        add(TOKEN_XOFFSET, PARAMETER_INT );
        add(TOKEN_YOFFSET, PARAMETER_INT );
        add(TOKEN_SIZE, PARAMETER_INT );
        add(TOKEN_BOLD, PARAMETER_FLAG ).group(TOKEN_BOLD);
        add(TOKEN_NONBOLD, PARAMETER_FLAG_FALSE ).group(TOKEN_BOLD);
        add(TOKEN_ITALICS, PARAMETER_FLAG ).group(TOKEN_ITALICS);
        add(TOKEN_NONITALICS, PARAMETER_FLAG_FALSE ).group(TOKEN_ITALICS);
        // add(TOKEN_COLOR, PARAMETER_COLOR);

        // add(TOKEN_COLOR, PARAMETER_COLOR);
        add(TOKEN_ONSTAY, PARAMETER_FLAG);
        add(TOKEN_ONCIRCLE, PARAMETER_FLAG);
        add(TOKEN_OVERWRITE, PARAMETER_FLAG);

        add(TOKEN_EXTEND, new long[]{
                TOKEN_ADDTITLE | PARAMETER_MOD_MULTIPLE,

                TOKEN_COLOR,

                TOKEN_SECOND,
                TOKEN_TODOUBLE,
                TOKEN_TOALTERNATE,
                TOKEN_TOMULTI,
                TOKEN_TOLIST,
                TOKEN_ADD | PARAMETER_MOD_MULTIPLE,
                TOKEN_ADDTEXT | PARAMETER_MOD_MULTIPLE,
                TOKEN_ONSTAY,
                TOKEN_ONCIRCLE })
                .method("extendButton").group(TOKEN_BUTTON).allowAsLabel().allowAsDefault();

        add(TOKEN_TODOUBLE, PARAMETER_FLAG );
        add(TOKEN_TOALTERNATE, PARAMETER_FLAG );
        add(TOKEN_TOMULTI, PARAMETER_FLAG );
        add(TOKEN_TOLIST, PARAMETER_FLAG );

        add(TOKEN_ADDMODIFY, new long[]{
                TOKEN_ID, TOKEN_ADDROLL, TOKEN_ROLLS,
                TOKEN_IGNORESPACE }).method("addModify" );
        // TOKEN_ID is already defined
        // !! addRollHelper functionality should be avoided !!
        // "Multiple" type parameters are needed
        add(TOKEN_ADDROLL, (PARAMETER_STRING | PARAMETER_MOD_LIST)).method("addRollHelper");
        add(TOKEN_ROLLS, (PARAMETER_STRING | PARAMETER_MOD_LIST));
        add(TOKEN_IGNORESPACE, PARAMETER_FLAG);

        add(TOKEN_MONITOR, new long[]{
                TOKEN_LAYOUT,
                TOKEN_SIZE,
                TOKEN_BOLD, TOKEN_NONBOLD, TOKEN_ITALICS, TOKEN_NONITALICS,
                TOKEN_COLOR })
                .method("setMonitor").allowAsLabel().allowAsDefault();
        // add(TOKEN_LAYOUT, PARAMETER_KEYWORD);
        // add(TOKEN_SIZE, PARAMETER_INT );
        // add(TOKEN_BOLD, TOKEN_BOLD, PARAMETER_FLAG );
        // add(TOKEN_NONBOLD, TOKEN_BOLD, PARAMETER_FLAG_FALSE );
        // add(TOKEN_ITALICS, TOKEN_ITALICS, PARAMETER_FLAG );
        // add(TOKEN_NONITALICS, TOKEN_ITALICS, PARAMETER_FLAG_FALSE );
        // add(TOKEN_COLOR, PARAMETER_COLOR);

        add(TOKEN_STOP, MESSAGE_STOP );

//        add(TOKEN_, new long[]{ }.method("" );

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
        private long tokenCode;
        private long allowedLabelTypes[] = null;

        private long groupCode;
        private long params[];
        private Method method = null;
        private boolean allowedAsLabel = false;
        private boolean allowedAsDefault = false;

        private Data( long tokenCode, long[] params )
            {
            this.tokenCode = tokenCode;
            this.groupCode = tokenCode;
            this.params = params;
            }

        private Data group( long groupCode )
            {
            this.groupCode = groupCode;
            return this;
            }

        private Data method( String methodName )
            {
            // Different types of methods should be called - according to parameter type
            // Check SoftBoardParser.parseComplexParameter, third part!
            // ?? Some kind of is...Type() or getMainType() methods could be helpful ??

            try
                {
                // Parameter-command has COMPLEX parameters - forwardParameters
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

            return this;
            }

        private Data allowAsLabel()
            {
            allowedAsLabel = true;
            return this;
            }

        private Data allowAsDefault()
            {
            allowedAsDefault = true;
            return this;
            }

        private Data labels( long[] allowedLabelTypes ) // extendAllowedLabelTypes
            {
            this.allowedLabelTypes = allowedLabelTypes;
            return this;
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

        public boolean isAllowedAsLabel()
            {
            return allowedAsLabel;
            }

        public boolean isAllowedAsDefault()
            {
            return allowedAsDefault;
            }

        public boolean isLabelTypeAllowed( long labelType )
            {
            if ( labelType == tokenCode )
                return true;
            if ( allowedLabelTypes == null )
                return false;
            return ArrayUtils.contains( allowedLabelTypes, labelType );
            }
        }

    }

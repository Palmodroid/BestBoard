package dancingmoon.bestboard.parser;

import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.utils.ExtendedMap;


/**
 * COAT language contains so called parameter-commands.
 * This class stores the data needed for parsing these parameter-commands.
 * Only SoftBoardParser uses this class.
 */
public class Commands
    {
    // Coat version is checked independently at SoftBoardParser.parseSoftBoard()
    public final static long COAT_VERSION = 2L;
    public static final long TOKEN_COAT = 0x9c843L;

    // Special code for first level commands
    public final static long ADDSOFTBOARD = 0L;

    // Token codes for complex parameter-commands - POSITIVE VALUES !!
    public static final long TOKEN_NAME = 0x11ff91L;
    public static final long TOKEN_VERSION = 0x12c1c5d965L;
    public static final long TOKEN_AUTHOR = 0x2cc5c115L;
    public static final long TOKEN_ADDTAGS = 0x630908ad7L;
    public static final long TOKEN_DESCRIPTION = 0xe4e74ed039e9f3L;
    public static final long TOKEN_DOCFILE = 0x828aa14c5L;
    public static final long TOKEN_DOCURI = 0x38739e2cL;

    public static final long TOKEN_LET = 0x7270L;

    public static final long TOKEN_LOCALE = 0x598408feL;
    public static final long TOKEN_LANGUAGE = 0x1d6843b69dcL;
    public static final long TOKEN_COUNTRY = 0x791c55f9bL;
    public static final long TOKEN_VARIANT = 0x12b1358888L;

    public static final long TOKEN_DEFAULT = 0x7ffa73630L;
    public static final long TOKEN_BOARDCOLOR = 0x561cab6e3e939L;
    public static final long TOKEN_BUTTONCOLOR = 0xca246447c75adeL;
    public static final long TOKEN_SLOT = 0x16180eL;

    public static final long TOKEN_METACOLOR = 0x478ed1022507L;
    public static final long TOKEN_LOCKCOLOR = 0x45300a4313a1L;
    public static final long TOKEN_AUTOCOLOR = 0x229aefe28af1L;
    public static final long TOKEN_TOUCHCOLOR = 0xdb3133d210b24L;
    public static final long TOKEN_STROKECOLOR = 0x1ec12db0914dd7eL;
    public static final long TOKEN_TITLEFONT = 0x5e4418d3118eL;

    public static final long TOKEN_ENTERTITLE = 0x6c2ce8db50a6eL;
    public static final long TOKEN_GOTITLE = 0x9f55c6b8bL;
    public static final long TOKEN_SEARCHTITLE = 0x1e4efcbab841f3fL;
    public static final long TOKEN_SENDTITLE = 0x5ab62a415135L;
    public static final long TOKEN_NEXTTITLE = 0x4ac3553a67faL;
    public static final long TOKEN_DONETITLE = 0x2ba804d8335dL;
    public static final long TOKEN_PREVTITLE = 0x523afaf86e24L;
    public static final long TOKEN_NONETITLE = 0x4b9a25bef027L;
    public static final long TOKEN_UNKNOWNTITLE = 0x4ba5296e5765a85eL;

    public static final long TOKEN_ADDSLOT = 0x6309000f6L;
    public static final long TOKEN_ID = 0x2a7L;
    public static final long TOKEN_XOFFSET = 0x141b95a3d2L;
    public static final long TOKEN_YOFFSET = 0x14b483849bL;
    public static final long TOKEN_SIZE = 0x16098bL;

    public static final long TOKEN_ADDBOARD = 0xe502ec0209L;
    public static final long TOKEN_HEXAGONAL = 0x379824b8b630L;
    public static final long TOKEN_WIDE = 0x191dd1L;
    public static final long TOKEN_HALFCOLUMNS = 0x1274de67160b457L;
    public static final long TOKEN_COLUMNS = 0x790c94224L;
    public static final long TOKEN_ROWS = 0x156363L;
    public static final long TOKEN_ALIGN = 0x12e9734L;
    public static final long TOKEN_COLOR = 0x16a2be4L;

    public static final long TOKEN_ODDS = 0x12d43aL;
    public static final long TOKEN_EVENS = 0x1a8a13eL;

    public static final long TOKEN_FORCECAPS = 0x320dfec7a853L;
    public static final long TOKEN_FORCESHIFT = 0x73c05d4aa24e7L;
    public static final long TOKEN_FORCECTRL = 0x320dfec80e31L;
    public static final long TOKEN_FORCEALT = 0x15a52fe7114L;

    public static final long TOKEN_CURSOR = 0x3508a5d8L;
    public static final long TOKEN_NONE = 0x124a94L;
    public static final long TOKEN_ALSO = 0x82f13L;
    public static final long TOKEN_ONLY = 0x130ae2L;
    public static final long TOKEN_BOARD = 0x14d5881L;
    public static final long TOKEN_COLUMN = 0x34587768L;
    public static final long TOKEN_ROW = 0x93fbL;

    public static final long TOKEN_NEXT = 0x12169bL;
    public static final long TOKEN_NEXTROW = 0xdfb035ecaL;

    public static final long TOKEN_SKIP = 0x1611d3L;
    public static final long TOKEN_SKIPROW = 0x110ec55622L;

    public static final long TOKEN_OVERWRITE = 0x4f61843b6a10L;
    public static final long TOKEN_TRANSFORM = 0x5effb66d5b7dL;

    public static final long TOKEN_BUTTON = 0x30e81c12L;

    public static final long TOKEN_SEND = 0x15f26aL;
    public static final long TOKEN_SECOND = 0x7555168eL;

    public static final long TOKEN_TEXT = 0x16b9c9L;
    public static final long TOKEN_KEY = 0x6d1cL;
    public static final long TOKEN_DO = 0x1f9L;

    public static final long TOKEN_DELETE = 0x375c443dL;
    public static final long TOKEN_BACKSPACE = 0x240879d19872L;

    public static final long TOKEN_DRAFT = 0x188da4eL;

    public static final long TOKEN_SETTINGS = 0x273bad4bccdL;

    public static final long TOKEN_META = 0x115017L;

    public static final long TOKEN_CAPS = 0x97f8fL;
    public static final long TOKEN_CTRL = 0x9e56dL;
    public static final long TOKEN_ALT = 0x38a0L;
    public static final long TOKEN_SHIFT = 0x32e4093L;

    public static final long TOKEN_LINK = 0x109ecaL;

    public static final long TOKEN_LOCK = 0x10bd49L;

    public static final long TOKEN_AUTOCAPS = 0xef6e441a58L;
    public static final long TOKEN_ON = 0x38fL;
    public static final long TOKEN_OFF = 0x8292L;
    public static final long TOKEN_WAIT = 0x18f3d1L;
    public static final long TOKEN_HOLD = 0xda71bL;

    public static final long TOKEN_STRINGCAPS = 0xd4c9a99e3004cL;

    public static final long TOKEN_SPACETRAVEL = 0x1ea02b357b27badL;

    public static final long TOKEN_AUTOSPACE = 0x229af1aca342L;
    public static final long TOKEN_ERASESPACES = 0xfbc52200a2a61eL;

    public static final long TOKEN_AFTER = 0x12a2e93L;
    public static final long TOKEN_BEFORE = 0x2f13a095L;
    public static final long TOKEN_AROUND = 0x2c6c5e43L;

    public static final long TOKEN_REPEAT = 0x713cd0a7L;

    public static final long TOKEN_MODIFY = 0x5da713aeL;

    public static final long TOKEN_ENTER = 0x1a2c13fL;

    public static final long TOKEN_ADDTITLE = 0xe504ea848bL;
    // public static final long TOKEN_TEXT = 0x16b9c9L;
    public static final long TOKEN_BOLD = 0x903edL;
    public static final long TOKEN_ITALICS = 0xb39c56ee8L;
    // public static final long TOKEN_COLOR = 0x16a2be4L;
    // public static final long TOKEN_SLOT = 0x16180eL ;

    public static final long TOKEN_INDEX = 0x214cf79L;

    public static final long TOKEN_ADDLINK = 0x6308a87b2L;
    // public static final long TOKEN_INDEX = 0x214cf79L;
    // public static final long TOKEN_BOARD = 0x14d5881L;
    public static final long TOKEN_LANDSCAPE = 0x44010ff837b4L;
    public static final long TOKEN_PORTRAIT = 0x2375cbd8761L;

    public static final long TOKEN_ADDMODIFY = 0x211999959456L;
    //     public static final long TOKEN_ID = 0x2a7L;
    public static final long TOKEN_ROLLS = 0x3172195L;
    public static final long TOKEN_ADDROLL = 0x6308f4aadL;
    public static final long TOKEN_IGNORESPACE = 0x13b2fae0bc1c2cfL;
    public static final long TOKEN_REVERSE = 0x105e76189bL;

    public static final long TOKEN_STOP = 0x1642d2L;


    // One parameter types - NEGATIVE VALUES, ORDER IS IMPORTANT !!
    public final static long PARAMETER_BOOLEAN = -1L;   // Returned as Boolean (false==0, true==anything else)
    public final static long PARAMETER_CHAR = -2L;      // Returned as Character (unsigned 16 bit)
    public final static long PARAMETER_COLOR = -3L;     // Returned as Integer (unsigned 32 bit)
    public final static long PARAMETER_INT = -4L;       // Returned as Integer (signed 32 bit)
    public final static long PARAMETER_LONG = -5L;      // Returned as Long (signed 64 bit)

    public final static long PARAMETER_FILE = -6L;      // Returned as String
    public final static long PARAMETER_STRING = -7L;    // Returned as String

    public final static long PARAMETER_TEXT = -8L;      // Returned as String OR Character
                                                        // Further type-checking is needed after return!!

    public final static long PARAMETER_KEYWORD = -9L;   // Returned as Long (signed 64 bit)

    // List parameter types - NEGATIVE VALUES, ORDER SAME AS ONE PARAMETER, but bit 5 is 0!!
    // Parameters are returned as ArrayList<Object>
    public final static long PARAMETER_BOOLEAN_LIST = -1L & -17L;   // Returned as Boolean (false==0, true==anything else)
    public final static long PARAMETER_CHAR_LIST = -2L & -17L;      // Returned as Character (unsigned 16 bit)
    public final static long PARAMETER_COLOR_LIST = -3L & -17L;     // Returned as Integer (unsigned 32 bit)
    public final static long PARAMETER_INT_LIST = -4L & -17L;       // Returned as Integer (signed 32 bit)
    public final static long PARAMETER_LONG_LIST = -5L & -17L;      // Returned as Long (signed 64 bit)

    public final static long PARAMETER_FILE_LIST = -6L & -17L;      // Returned as String
    public final static long PARAMETER_STRING_LIST = -7L & -17L;    // Returned as String

    public final static long PARAMETER_TEXT_LIST = -8L & -17L;      // Returned as String OR Character
    // Further type-checking is needed after return!!

    public final static long PARAMETER_KEYWORD_LIST = -9L & -17L;   // Returned as Long (signed 64 bit)

    // Label parameter - NEGATIVE VALUES, BELOW LIST AND ABOVE NO-PARAMETER TYPES !!
    public final static long PARAMETER_LABEL = -40L;

    // Flag parameter - NEGATIVE VALUES, BELOW LIST AND ABOVE NO-PARAMETER TYPES !!
    public final static long PARAMETER_FLAG = -42L;

    // Special "messages" are not real parameters, but messages to the parser
    // Messages - NEGATIVE VALUES, BELOW ONE AND ABOVE NO-PARAMETER TYPES !!
    public final static long MESSAGE_STOP = -0xF000L;

    // No parameters - MOST NEGATIVE!!
    public final static long NO_PARAMETERS = -0xFFFFL;

    // These tokens (parameter-commands) can be defined as labels
    public final static long[] ALLOWED_LABELS = new long[]{
            TOKEN_ADDBOARD,
            TOKEN_CURSOR,
            TOKEN_SEND,
            TOKEN_BUTTON,
            TOKEN_ADDTITLE };


    /**
     * Parameter-commands are stored in an unmodifiable hash-HashMap (LIST)
     * as long token-code key (the command itself) and as Data value (command's data) pairs.
     */
    private static final Map<Long, Data> LIST = createDataMap();

    /**
     * Static map initialization is done as suggested by http://stackoverflow.com/a/509016
     * ((it avoids anonymous class,
     *  it makes creation of map more explicit,
     *  it makes map unmodifiable,
     *  as MY_MAP is constant, I would name it like constant))
     * @return the initalized map
     */
    private static Map<Long, Data> createDataMap()
        {
        Scribe.locus( Debug.COMMANDS );

        Map<Long, Data> result = new HashMap<>();

        // KEY: Code (long) of Parameter-command
        // VALUE (new DATA object)
        // - array (long) of allowed parameters for this command
        //      AT LEAST FIRST ITEM IS NEEDED (NO_PARAMETERS if there are no parameters allowed)
        // - method to call in SoftBoardClass (method should have a map parameter)
        result.put(ADDSOFTBOARD, new Data(new long[] {
                TOKEN_NAME,
                TOKEN_VERSION,
                TOKEN_AUTHOR,
                TOKEN_ADDTAGS,
                TOKEN_DESCRIPTION,
                TOKEN_DOCFILE,
                TOKEN_DOCURI,
                TOKEN_LET,
                TOKEN_LOCALE,
                TOKEN_DEFAULT,
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

                TOKEN_ADDSLOT,
                TOKEN_ADDBOARD,
                TOKEN_CURSOR,
                TOKEN_NEXT,
                TOKEN_NEXTROW,
                TOKEN_SKIP,
                TOKEN_SKIPROW,
                TOKEN_BUTTON,
                TOKEN_ADDLINK,
                TOKEN_ADDMODIFY,
                TOKEN_STOP
        }, null ));

        result.put(TOKEN_NAME, new Data(new long[]{PARAMETER_STRING}, "setName" ));
        result.put(TOKEN_VERSION, new Data(new long[]{PARAMETER_INT}, "setVersion" ));
        result.put(TOKEN_AUTHOR, new Data(new long[]{PARAMETER_STRING}, "setAuthor" ));
        result.put(TOKEN_ADDTAGS, new Data(new long[]{PARAMETER_STRING_LIST}, "addTags" ));
        result.put(TOKEN_DESCRIPTION, new Data(new long[]{PARAMETER_STRING}, "setDescription" ));
        result.put(TOKEN_DOCFILE, new Data(new long[]{PARAMETER_FILE}, "setDocFile" ));
        result.put(TOKEN_DOCURI, new Data(new long[]{PARAMETER_STRING}, "setDocUri" ));

        result.put(TOKEN_LET, new Data(new long[]{PARAMETER_LABEL}, null ));

        result.put(TOKEN_LOCALE, new Data(new long[]{
                TOKEN_LANGUAGE, TOKEN_COUNTRY, TOKEN_VARIANT }, "setLocale" ));
        result.put(TOKEN_LANGUAGE, new Data(new long[]{PARAMETER_STRING}, null ));
        result.put(TOKEN_COUNTRY, new Data(new long[]{PARAMETER_STRING}, null ));
        result.put(TOKEN_VARIANT, new Data(new long[]{PARAMETER_STRING}, null ));

        result.put(TOKEN_DEFAULT, new Data(new long[]{
                TOKEN_BOARDCOLOR, TOKEN_BUTTONCOLOR, TOKEN_SLOT}, "setDefault" ));
        result.put(TOKEN_BOARDCOLOR, new Data(new long[]{PARAMETER_COLOR}, null ));
        result.put(TOKEN_BUTTONCOLOR, new Data(new long[]{PARAMETER_COLOR}, null ));
        result.put( TOKEN_SLOT, new Data(new long[]{PARAMETER_KEYWORD}, null ));

        result.put(TOKEN_METACOLOR, new Data(new long[]{PARAMETER_COLOR}, "setMetaColor" ));
        result.put(TOKEN_LOCKCOLOR, new Data(new long[]{PARAMETER_COLOR}, "setLockColor" ));
        result.put(TOKEN_AUTOCOLOR, new Data(new long[]{PARAMETER_COLOR}, "setAutoColor" ));
        result.put(TOKEN_TOUCHCOLOR, new Data(new long[]{PARAMETER_COLOR}, "setTouchColor" ));
        result.put(TOKEN_STROKECOLOR, new Data(new long[]{PARAMETER_COLOR}, "setStrokeColor" ));
        result.put(TOKEN_TITLEFONT, new Data(new long[]{PARAMETER_FILE}, "setTypeface" ));

        result.put(TOKEN_ENTERTITLE, new Data(new long[]{PARAMETER_TEXT}, "setEnterTitle" ));
        result.put(TOKEN_GOTITLE, new Data(new long[]{PARAMETER_TEXT}, "setGoTitle" ));
        result.put(TOKEN_SEARCHTITLE, new Data(new long[]{PARAMETER_TEXT}, "setSearchTitle" ));
        result.put(TOKEN_SENDTITLE, new Data(new long[]{PARAMETER_TEXT}, "setSendTitle" ));
        result.put(TOKEN_NEXTTITLE, new Data(new long[]{PARAMETER_TEXT}, "setNextTitle" ));
        result.put(TOKEN_DONETITLE, new Data(new long[]{PARAMETER_TEXT}, "setDoneTitle" ));
        result.put(TOKEN_PREVTITLE, new Data(new long[]{PARAMETER_TEXT}, "setPrevTitle" ));
        result.put(TOKEN_NONETITLE, new Data(new long[]{PARAMETER_TEXT}, "setNoneTitle" ));
        result.put(TOKEN_UNKNOWNTITLE, new Data(new long[]{PARAMETER_TEXT}, "setUnknownTitle" ));

        result.put( TOKEN_ADDSLOT, new Data(new long[]{
                TOKEN_ID, TOKEN_XOFFSET, TOKEN_YOFFSET, TOKEN_SIZE,
                TOKEN_BOLD, TOKEN_ITALICS, TOKEN_COLOR }, "addSlot" ));
        result.put(TOKEN_ID, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_XOFFSET, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_YOFFSET, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_SIZE, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_BOLD, new Data(new long[]{PARAMETER_FLAG}, null ));
        result.put(TOKEN_ITALICS, new Data(new long[]{PARAMETER_FLAG}, null ));
        result.put(TOKEN_COLOR, new Data(new long[]{PARAMETER_COLOR}, null ));

        result.put(TOKEN_ADDBOARD, new Data(new long[]{
                TOKEN_ID, TOKEN_HEXAGONAL, TOKEN_WIDE,
                TOKEN_COLUMNS, TOKEN_HALFCOLUMNS, TOKEN_ROWS,
                TOKEN_ALIGN, TOKEN_COLOR,
                TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT}, "addBoard" ));
        // TOKEN_ID is already defined
        // Useless parametercommand - just for clearer readability
        result.put(TOKEN_HEXAGONAL, new Data(new long[]{NO_PARAMETERS}, null ));
        result.put(TOKEN_WIDE, new Data(new long[]{PARAMETER_FLAG}, null ));
        result.put(TOKEN_COLUMNS, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_HALFCOLUMNS, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_ROWS, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_ALIGN, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_COLOR, new Data(new long[]{PARAMETER_COLOR}, null ));

        result.put( TOKEN_FORCECAPS, new Data(new long[]{PARAMETER_BOOLEAN}, null ));
        result.put( TOKEN_FORCESHIFT, new Data(new long[]{PARAMETER_BOOLEAN}, null ));
        result.put( TOKEN_FORCECTRL, new Data(new long[]{PARAMETER_BOOLEAN}, null ));
        result.put( TOKEN_FORCEALT, new Data(new long[]{PARAMETER_BOOLEAN}, null ));

        result.put( TOKEN_CURSOR, new Data(new long[]{
                TOKEN_NONE, TOKEN_ALSO, TOKEN_ONLY,
                TOKEN_BOARD, TOKEN_COLUMN, TOKEN_ROW,
                TOKEN_TRANSFORM }, "setCursor" ));
        result.put(TOKEN_NONE, new Data(new long[]{PARAMETER_FLAG}, null ));
        result.put(TOKEN_ALSO, new Data(new long[]{PARAMETER_FLAG}, null ));
        // TOKEN_ONLY is currently NOT checked, If TOKEN_ALSO can be found it is used
        result.put(TOKEN_ONLY, new Data(new long[]{NO_PARAMETERS}, null ));
        result.put(TOKEN_BOARD, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_COLUMN, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_ROW, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_TRANSFORM, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_NEXT, new Data(new long[]{NO_PARAMETERS}, "next" ));
        result.put(TOKEN_NEXTROW, new Data(new long[]{NO_PARAMETERS}, "nextRow" ));

        result.put( TOKEN_SKIP, new Data(new long[]{PARAMETER_INT}, "skip" ));
        result.put( TOKEN_SKIPROW, new Data(new long[]{PARAMETER_INT}, "skipRow" ));

        result.put(TOKEN_BUTTON, new Data(new long[]{
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

                TOKEN_ENTER,

                TOKEN_ADDTITLE,
                TOKEN_COLOR,

                TOKEN_OVERWRITE,

                TOKEN_SECOND,
                TOKEN_SEND },
                // SEND remains only because label's purposes,
                // parameters could be given directly to BUTTON
                "setButton"));

        result.put(TOKEN_OVERWRITE, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_SEND, new Data(new long[]{
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

                TOKEN_ENTER },
                "createButtonFunction"));

        result.put(TOKEN_SECOND, new Data(new long[]{
                TOKEN_TEXT,
                TOKEN_AUTOCAPS,
                TOKEN_STRINGCAPS,
                TOKEN_ERASESPACES,
                TOKEN_AUTOSPACE,

                TOKEN_KEY,
                TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT,

                TOKEN_DO },
                "packet"));

        result.put(TOKEN_TEXT, new Data(new long[]{PARAMETER_TEXT}, null ));
        result.put(TOKEN_KEY, new Data(new long[]{PARAMETER_INT}, null ));
        result.put(TOKEN_DO, new Data(new long[]{PARAMETER_KEYWORD}, null ));

        result.put(TOKEN_META, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_LINK, new Data(new long[]{PARAMETER_INT}, null ));

        result.put(TOKEN_LOCK, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_AUTOCAPS, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_STRINGCAPS, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_REPEAT, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_SPACETRAVEL, new Data(new long[]{PARAMETER_FLAG}, null ));

        // TOKEN_FORCECAPS, TOKEN_FORCESHIFT, TOKEN_FORCECTRL, TOKEN_FORCEALT are already defined

        result.put(TOKEN_AUTOSPACE, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_ERASESPACES, new Data(new long[]{PARAMETER_KEYWORD}, null ));

        result.put(TOKEN_MODIFY, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_REVERSE, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_ENTER, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_ADDTITLE, new Data(new long[]{
                TOKEN_TEXT, TOKEN_SLOT,
                TOKEN_XOFFSET, TOKEN_YOFFSET, TOKEN_SIZE,
                TOKEN_BOLD, TOKEN_ITALICS, TOKEN_COLOR }, "addTitle"));
        // TOKEN_TEXT is already defined
        result.put(TOKEN_SLOT, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        // TOKEN_XOFFSET is already defined
        // TOKEN_YOFFSET is already defined
        // TOKEN_SIZE is already defined
        // TOKEN_BOLD is already defined
        // TOKEN_ITALICS is already defined
        // TOKEN_COLOR is already defined

        result.put( TOKEN_ADDLINK, new Data(new long[]{
                TOKEN_INDEX, TOKEN_BOARD,
                TOKEN_PORTRAIT, TOKEN_LANDSCAPE }, "addLink"));
        result.put(TOKEN_INDEX, new Data(new long[]{PARAMETER_INT}, null ));
        // TOKEN_BOARD is already defined
        result.put(TOKEN_PORTRAIT, new Data(new long[]{PARAMETER_KEYWORD}, null ));
        result.put(TOKEN_LANDSCAPE, new Data(new long[]{PARAMETER_KEYWORD}, null ));

        result.put(TOKEN_ADDMODIFY, new Data(new long[]{
                TOKEN_ID, TOKEN_ADDROLL, TOKEN_ROLLS,
                TOKEN_IGNORESPACE }, "addModify" ));
        // TOKEN_ID is already defined
        // !! addRollHelper functionality should be avoided !!
        // "Multiple" type parameters are needed
        result.put(TOKEN_ADDROLL, new Data(new long[]{PARAMETER_STRING_LIST}, "addRollHelper" ));
        result.put(TOKEN_ROLLS, new Data(new long[]{PARAMETER_STRING_LIST}, null ));
        result.put(TOKEN_IGNORESPACE, new Data(new long[]{PARAMETER_FLAG}, null ));

        result.put(TOKEN_STOP, new Data(new long[]{MESSAGE_STOP}, null ));

//        result.put(TOKEN_, new Data(new long[]{ }, "" ));
        return Collections.unmodifiableMap(result);
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
    public static final long TOKEN_TRUE = 0x16fed0L;
    public static final long TOKEN_FALSE = 0x1b52528L;

    // Hard-key mnemonics
    public static final long TOKEN_KEYUNKNOWN = 0x96bb8b22735dbL;
    public static final long TOKEN_KEYSOFTLEFT = 0x15c91860fd40befL;
    public static final long TOKEN_KEYSOFTRIGHT = 0x326108604a546a5fL;
    public static final long TOKEN_KEYHOME = 0xc304cfe9dL;
    public static final long TOKEN_KEYBACK = 0xc30480f25L;
    public static final long TOKEN_KEYCALL = 0xc3048d650L;
    public static final long TOKEN_KEYENDCALL = 0x96baf22782dbeL;
    public static final long TOKEN_KEY0 = 0xfc50cL;
    public static final long TOKEN_KEY1 = 0xfc50dL;
    public static final long TOKEN_KEY2 = 0xfc50eL;
    public static final long TOKEN_KEY3 = 0xfc50fL;
    public static final long TOKEN_KEY4 = 0xfc510L;
    public static final long TOKEN_KEY5 = 0xfc511L;
    public static final long TOKEN_KEY6 = 0xfc512L;
    public static final long TOKEN_KEY7 = 0xfc513L;
    public static final long TOKEN_KEY8 = 0xfc514L;
    public static final long TOKEN_KEY9 = 0xfc515L;
    public static final long TOKEN_KEYSTAR = 0xc3055982aL;
    public static final long TOKEN_KEYNUMBER = 0x412e6535be4fL; // !!!! SHOULD BE KEYPOUND !!!!
    public static final long TOKEN_KEYDPADUP = 0x412e3b487becL;
    public static final long TOKEN_KEYDPADDOWN = 0x15c903b06918cbdL;
    public static final long TOKEN_KEYDPADLEFT = 0x15c903b069783bcL;
    public static final long TOKEN_KEYDPADRIGHT = 0x3260d887f494bb00L;
    public static final long TOKEN_KEYDPADCENTR = 0x3260d887f2e4d496L;
    public static final long TOKEN_KEYVOLUP = 0x1c2fcb126efL;
    public static final long TOKEN_KEYVOLDOWN = 0x96bb94f4c0fc8L;
    public static final long TOKEN_KEYPOWER = 0x1c2fc05c9ceL;
    public static final long TOKEN_KEYCAMERA = 0x412e3582b62fL;
    public static final long TOKEN_KEYCLEAR = 0x1c2fa8f5324L;
    public static final long TOKEN_KEYA = 0xfc516L;
    public static final long TOKEN_KEYB = 0xfc517L;
    public static final long TOKEN_KEYC = 0xfc518L;
    public static final long TOKEN_KEYD = 0xfc519L;
    public static final long TOKEN_KEYE = 0xfc51aL;
    public static final long TOKEN_KEYF = 0xfc51bL;
    public static final long TOKEN_KEYG = 0xfc51cL;
    public static final long TOKEN_KEYH = 0xfc51dL;
    public static final long TOKEN_KEYI = 0xfc51eL;
    public static final long TOKEN_KEYJ = 0xfc51fL;
    public static final long TOKEN_KEYK = 0xfc520L;
    public static final long TOKEN_KEYL = 0xfc521L;
    public static final long TOKEN_KEYM = 0xfc522L;
    public static final long TOKEN_KEYN = 0xfc523L;
    public static final long TOKEN_KEYO = 0xfc524L;
    public static final long TOKEN_KEYP = 0xfc525L;
    public static final long TOKEN_KEYQ = 0xfc526L;
    public static final long TOKEN_KEYR = 0xfc527L;
    public static final long TOKEN_KEYS = 0xfc528L;
    public static final long TOKEN_KEYT = 0xfc529L;
    public static final long TOKEN_KEYU = 0xfc52aL;
    public static final long TOKEN_KEYV = 0xfc52bL;
    public static final long TOKEN_KEYW = 0xfc52cL;
    public static final long TOKEN_KEYX = 0xfc52dL;
    public static final long TOKEN_KEYY = 0xfc52eL;
    public static final long TOKEN_KEYZ = 0xfc52fL;
    public static final long TOKEN_KEYCOMMA = 0x1c2fa91d12eL;
    public static final long TOKEN_KEYPERIOD = 0x412e6bb4690dL;
    public static final long TOKEN_KEYALTLEFT = 0x96bacb84d0d53L;
    public static final long TOKEN_KEYALTRIGHT = 0x15c8ff6a3d19dd3L;
    public static final long TOKEN_KEYSHLEFT = 0x412e786bca90L;
    public static final long TOKEN_KEYSHRIGHT = 0x96bb76842f7a4L;
    public static final long TOKEN_KEYTAB = 0x545559beL;
    public static final long TOKEN_KEYSPACE = 0x1c2fc5be481L;
    public static final long TOKEN_KEYSYM = 0x545557e8L;
    public static final long TOKEN_KEYEXPLORER = 0x15c9056274ef809L;
    public static final long TOKEN_KEYENVELOPE = 0x15c90504606427eL;
    public static final long TOKEN_KEYENTER = 0x1c2faca618bL;
    public static final long TOKEN_KEYDEL = 0x545504ccL;
    public static final long TOKEN_KEYGRAVE = 0x1c2fb0647aeL;
    public static final long TOKEN_KEYMINUS = 0x1c2fbab2dfdL;
    public static final long TOKEN_KEYEQUAL = 0x1c2faccb7e1L;
    public static final long TOKEN_KEYLBRACKET = 0x15c90e3c28849a4L;
    public static final long TOKEN_KEYRBRACKET = 0x15c916860d137f2L;
    public static final long TOKEN_KEYBACKSLASH = 0x3260d0da17998addL;
    public static final long TOKEN_KEYSEMICOLON = 0x3261078741e74efeL;
    public static final long TOKEN_KEYAPOSTROPH = 0x3260cefb1f91b261L;
    public static final long TOKEN_KEYSLASH = 0x1c2fc58cf60L;
    public static final long TOKEN_KEYAT = 0x2477c4bL;
    public static final long TOKEN_KEYNUM = 0x54553c97L;
    public static final long TOKEN_KEYHOOK = 0xc304cfeedL;
    public static final long TOKEN_KEYFOCUS = 0x1c2fae767c1L;
    public static final long TOKEN_KEYPLUS = 0xc30531eb0L;
    public static final long TOKEN_KEYMENU = 0xc3050a6a9L;
    public static final long TOKEN_KEYNOTIFY = 0x412e648fb74fL;
    public static final long TOKEN_KEYSEARCH = 0x412e780dc448L;
    public static final long TOKEN_KEYPLAYPAUSE = 0x3260fe85a2b0c86dL;
    public static final long TOKEN_KEYSTOP = 0xc30559a2eL;
    public static final long TOKEN_KEYNEXT = 0xc30516df7L;
    public static final long TOKEN_KEYPREV = 0xc30533c79L;
    public static final long TOKEN_KEYREWIND = 0x412e73fc7be3L;
    public static final long TOKEN_KEYFFORWARD = 0x15c90617d85f797L;
    public static final long TOKEN_KEYMUTE = 0xc3050fd07L;
    public static final long TOKEN_KEYPAGEUP = 0x412e6b397050L;
    public static final long TOKEN_KEYPAGEDOWN = 0x15c913b661c7781L;
    public static final long TOKEN_KEYPICTSYM = 0x96bb59ff73d18L;
    public static final long TOKEN_KEYCHARSET = 0x96badd785f795L;
    public static final long TOKEN_KEYBUTTONA = 0x96bad76744510L;
    public static final long TOKEN_KEYBUTTONB = 0x96bad76744511L;
    public static final long TOKEN_KEYBUTTONC = 0x96bad76744512L;
    public static final long TOKEN_KEYBUTTONX = 0x96bad76744527L;
    public static final long TOKEN_KEYBUTTONY = 0x96bad76744528L;
    public static final long TOKEN_KEYBUTTONZ = 0x96bad76744529L;
    public static final long TOKEN_KEYBUTTONL1 = 0x15c90121ecdfce8L;
    public static final long TOKEN_KEYBUTTONR1 = 0x15c90121ecdfdc6L;
    public static final long TOKEN_KEYBUTTONL2 = 0x15c90121ecdfce9L;
    public static final long TOKEN_KEYBUTTONR2 = 0x15c90121ecdfdc7L;
    public static final long TOKEN_KEYBUTTHUMBL = 0x3260d29e7302cedcL;
    public static final long TOKEN_KEYBUTTHUMBR = 0x3260d29e7302cee2L;
    public static final long TOKEN_KEYBUTSTART = 0x15c90121eb4fcbdL;
    public static final long TOKEN_KEYBUTSELECT = 0x3260d29e6e83c9d4L;
    public static final long TOKEN_KEYBUTMODE = 0x96bad766eda97L;
    public static final long TOKEN_KEYESC = 0x54550c22L;
    public static final long TOKEN_KEYFWDEL = 0x1c2faed99abL;
    public static final long TOKEN_KEYCTRLLEFT = 0x15c902797161980L;
    public static final long TOKEN_KEYCTRLRIGHT = 0x3260d5b8d6e06054L;
    public static final long TOKEN_KEYCAPSLOCK = 0x15c901c35f29884L;
    public static final long TOKEN_KEYSCROLLOCK = 0x3261075e239576fdL;
    public static final long TOKEN_KEYMETALEFT = 0x15c90fbaffe738aL;
    public static final long TOKEN_KEYMETARIGHT = 0x3260f460707563c6L;
    public static final long TOKEN_KEYFUNCTION = 0x15c906a6da4eb36L;
    public static final long TOKEN_KEYSYSRQ = 0x1c2fc633bbfL;
    public static final long TOKEN_KEYBREAK = 0x1c2fa775d5aL;
    public static final long TOKEN_KEYMOVEHOME = 0x15c9101b1fcf3ccL;
    public static final long TOKEN_KEYMOVEEND = 0x96bb3f00db12dL;
    public static final long TOKEN_KEYINS = 0x545520ddL;
    public static final long TOKEN_KEYFORWARD = 0x96bafc127fca4L;
    public static final long TOKEN_KEYPLAY = 0xc30531bd2L;
    public static final long TOKEN_KEYPAUSE = 0x1c2fbfaeeffL;
    public static final long TOKEN_KEYCLOSE = 0x1c2fa8f8b2bL;
    public static final long TOKEN_KEYEJECT = 0x1c2fac6f998L;
    public static final long TOKEN_KEYREC = 0x54554fa1L;
    public static final long TOKEN_KEYF1 = 0x2477ce8L;
    public static final long TOKEN_KEYF2 = 0x2477ce9L;
    public static final long TOKEN_KEYF3 = 0x2477ceaL;
    public static final long TOKEN_KEYF4 = 0x2477cebL;
    public static final long TOKEN_KEYF5 = 0x2477cecL;
    public static final long TOKEN_KEYF6 = 0x2477cedL;
    public static final long TOKEN_KEYF7 = 0x2477ceeL;
    public static final long TOKEN_KEYF8 = 0x2477cefL;
    public static final long TOKEN_KEYF9 = 0x2477cf0L;
    public static final long TOKEN_KEYF10 = 0x54550d88L;
    public static final long TOKEN_KEYF11 = 0x54550d89L;
    public static final long TOKEN_KEYF12 = 0x54550d8aL;
    public static final long TOKEN_KEYNUMLOCK = 0x96bb4a0cc6f70L;
    public static final long TOKEN_KEYNUM0 = 0xc3051c1d3L;
    public static final long TOKEN_KEYNUM1 = 0xc3051c1d4L;
    public static final long TOKEN_KEYNUM2 = 0xc3051c1d5L;
    public static final long TOKEN_KEYNUM3 = 0xc3051c1d6L;
    public static final long TOKEN_KEYNUM4 = 0xc3051c1d7L;
    public static final long TOKEN_KEYNUM5 = 0xc3051c1d8L;
    public static final long TOKEN_KEYNUM6 = 0xc3051c1d9L;
    public static final long TOKEN_KEYNUM7 = 0xc3051c1daL;
    public static final long TOKEN_KEYNUM8 = 0xc3051c1dbL;
    public static final long TOKEN_KEYNUM9 = 0xc3051c1dcL;
    public static final long TOKEN_KEYNUMDIV = 0x412e6535c999L;
    public static final long TOKEN_KEYNUMSTAR = 0x96bb4a0d1f2f5L;
    public static final long TOKEN_KEYNUMMINUS = 0x15c911b3da44d54L;
    public static final long TOKEN_KEYNUMPLUS = 0x96bb4a0cf797bL;
    public static final long TOKEN_KEYNUMPERIOD = 0x3260f8eff4b5f0a0L;
    public static final long TOKEN_KEYNUMCOMMA = 0x15c911b3c8af085L;
    public static final long TOKEN_KEYNUMENTER = 0x15c911b3cc380e2L;
    public static final long TOKEN_KEYNUMEQUAL = 0x15c911b3cc5d738L;
    public static final long TOKEN_KEYNUMLPAR = 0x96bb4a0cc7486L;
    public static final long TOKEN_KEYNUMRPAR = 0x96bb4a0d117b4L;
    public static final long TOKEN_KEYVOLMUTE = 0x96bb94f53242bL;
    public static final long TOKEN_KEYINFO = 0xc304dbe28L;
    public static final long TOKEN_KEYCHUP = 0xc3048fd10L;
    public static final long TOKEN_KEYCHDOWN = 0x412e36442641L;
    public static final long TOKEN_KEYZOOMIN = 0x412e962531c2L;
    public static final long TOKEN_KEYZOOMOUT = 0x96bbbb3605240L;
    public static final long TOKEN_KEYTV = 0x2477f0cL;
    public static final long TOKEN_KEYWIN = 0x54556afdL;
    public static final long TOKEN_KEYGUIDE = 0x1c2fb08c173L;
    public static final long TOKEN_KEYDVR = 0x54550747L;
    public static final long TOKEN_KEYBOOKMARK = 0x15c900e738d0142L;
    public static final long TOKEN_KEYCAPTIONS = 0x15c901c360ce17dL;
    public static final long TOKEN_KEYSETTINGS = 0x15c918050649a69L;
    public static final long TOKEN_KEYTVPOWER = 0x96bb83ada167eL;
    public static final long TOKEN_KEYTVINPUT = 0x96bb83a10feedL;
    public static final long TOKEN_KEYSTBPOWER = 0x15c9188fb815276L;
    public static final long TOKEN_KEYSTBINPUT = 0x15c9188fab83ae5L;
    public static final long TOKEN_KEYAVRPOWER = 0x15c8ffc94a3f66eL;
    public static final long TOKEN_KEYAVRINPUT = 0x15c8ffc93dadeddL;
    public static final long TOKEN_KEYPRGRED = 0x412e6d1fdb7aL;
    public static final long TOKEN_KEYPRGGREEN = 0x15c91458e2c30beL;
    public static final long TOKEN_KEYPRGYELLOW = 0x3260ff0dd55ab8f2L;
    public static final long TOKEN_KEYPRGBLUE = 0x96bb5c58e82c4L;
    public static final long TOKEN_KEYAPPSWITCH = 0x3260cefbb8ce8214L;
    public static final long TOKEN_KEYBUTTON1 = 0x96bad76744507L;
    public static final long TOKEN_KEYBUTTON2 = 0x96bad76744508L;
    public static final long TOKEN_KEYBUTTON3 = 0x96bad76744509L;
    public static final long TOKEN_KEYBUTTON4 = 0x96bad7674450aL;
    public static final long TOKEN_KEYBUTTON5 = 0x96bad7674450bL;
    public static final long TOKEN_KEYBUTTON6 = 0x96bad7674450cL;
    public static final long TOKEN_KEYBUTTON7 = 0x96bad7674450dL;
    public static final long TOKEN_KEYBUTTON8 = 0x96bad7674450eL;
    public static final long TOKEN_KEYBUTTON9 = 0x96bad7674450fL;
    public static final long TOKEN_KEYBUTTON10 = 0x15c90121ecdfa03L;
    public static final long TOKEN_KEYBUTTON11 = 0x15c90121ecdfa04L;
    public static final long TOKEN_KEYBUTTON12 = 0x15c90121ecdfa05L;
    public static final long TOKEN_KEYBUTTON13 = 0x15c90121ecdfa06L;
    public static final long TOKEN_KEYBUTTON14 = 0x15c90121ecdfa07L;
    public static final long TOKEN_KEYBUTTON15 = 0x15c90121ecdfa08L;
    public static final long TOKEN_KEYBUTTON16 = 0x15c90121ecdfa09L;
    public static final long TOKEN_KEYLANGUAGE = 0x15c90e319cb4778L;
    public static final long TOKEN_KEYMANNER = 0x412e5ed8b6afL;
    public static final long TOKEN_KEY3D = 0x2477b38L;
    public static final long TOKEN_KEYCONTACTS = 0x15c90248ac4889bL;
    public static final long TOKEN_KEYCALENDAR = 0x15c901c23db20a6L;
    public static final long TOKEN_KEYMUSIC = 0x1c2fbb48d4aL;
    public static final long TOKEN_KEYCALC = 0xc3048d647L;
    public static final long TOKEN_KEYKAKU = 0xc304f051cL;
    public static final long TOKEN_KEYEISU = 0xc304a8ddeL;
    public static final long TOKEN_KEYMUHENKAN = 0x15c91050db74cf3L;
    public static final long TOKEN_KEYHENKAN = 0x412e4aa083a7L;
    public static final long TOKEN_KEYSWKANA = 0x412e7a17e683L;
    public static final long TOKEN_KEYYEN = 0x5455751bL;
    public static final long TOKEN_KEYRO = 0x2477ebbL;
    public static final long TOKEN_KEYKANA = 0xc304f0577L;
    public static final long TOKEN_KEYASSIST = 0x412e2f45fa41L;
    public static final long TOKEN_KEYBRGDOWN = 0x96bad688dd3deL;
    public static final long TOKEN_KEYBRGUP = 0x1c2fa776af5L;
    public static final long TOKEN_KEYMATRACE = 0x96bb3b600fce4L;
    public static final long TOKEN_KEYSLEEP = 0x1c2fc58e2c6L;
    public static final long TOKEN_KEYWAKE = 0xc30584b68L;
    public static final long TOKEN_KEYPAIR = 0xc3052e220L;
    public static final long TOKEN_KEYMEDIATOP = 0x15c90fb6eb95ec3L;
    public static final long TOKEN_KEY11 = 0x2477ae2L;
    public static final long TOKEN_KEY12 = 0x2477ae3L;
    public static final long TOKEN_KEYCHLAST = 0x412e364a09bdL;
    public static final long TOKEN_KEYTVDATA = 0x412e7e18003aL;
    public static final long TOKEN_KEYVOICEASST = 0x326111f73da5cbd6L;
    public static final long TOKEN_KEYTVRADIO = 0x96bb83b081140L;
    public static final long TOKEN_KEYTVTEXT = 0x412e7e247415L;
    public static final long TOKEN_KEYTVNUMENT = 0x15c91a079e4e519L;
    public static final long TOKEN_KEYTVTERRANA = 0x32610c34f21f7698L;
    public static final long TOKEN_KEYTVTERRDIA = 0x32610c34f21f85eaL;
    public static final long TOKEN_KEYTVSAT = 0x1c2fc7d80a7L;
    public static final long TOKEN_KEYTVSATBS = 0x96bb83b24fec2L;
    public static final long TOKEN_KEYTVSATCS = 0x96bb83b24fee7L;
    public static final long TOKEN_KEYTVSATSERV = 0x32610c3448e33847L;
    public static final long TOKEN_KEYTVNETWORK = 0x32610c315cc98c91L;
    public static final long TOKEN_KEYTVANTCABL = 0x32610c29bdd85168L;
    public static final long TOKEN_KEYTVHDMI1 = 0x96bb839ec9977L;
    public static final long TOKEN_KEYTVHDMI2 = 0x96bb839ec9978L;
    public static final long TOKEN_KEYTVHDMI3 = 0x96bb839ec9979L;
    public static final long TOKEN_KEYTVHDMI4 = 0x96bb839ec997aL;
    public static final long TOKEN_KEYTVCMPSIT1 = 0x32610c2aeb2c1f7fL;
    public static final long TOKEN_KEYTVCMPSIT2 = 0x32610c2aeb2c1f80L;
    public static final long TOKEN_KEYTVCOMP1 = 0x96bb839661e44L;
    public static final long TOKEN_KEYTVCOMP2 = 0x96bb839661e45L;
    public static final long TOKEN_KEYTVVGA1 = 0x412e7e260712L;
    public static final long TOKEN_KEYTVAUMIX = 0x96bb839318eadL;
    public static final long TOKEN_KEYTVAUMIXUP = 0x32610c29da03ff94L;
    public static final long TOKEN_KEYTVAUMIXDN = 0x32610c29da03fd1dL;
    public static final long TOKEN_KEYTVZOOMMOD = 0x32610c38b0ae5fd0L;
    public static final long TOKEN_KEYTVCONTMNU = 0x32610c2af337fdc7L;
    public static final long TOKEN_KEYTVMEDCNTX = 0x32610c30c202a272L;
    public static final long TOKEN_KEYTVTIMER = 0x96bb83b47a19aL;
    public static final long TOKEN_KEYHELP = 0xc304cc909L;


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
     * long parameters[] - allowed parameters (token-codes of allowed parameter-commands) or
     * special parameters as negative values.
     * Array cannot be null or empty; at least one item is needed!
     * NO_PARAMETERS value means: no parameter needed
     * Method method - method of SoftBoardData, which should be called
     * (method can be null: no method is called)
     */
    public static class Data
        {
        private Data(long[] params, String methodName )
            {
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
                if (getParameterType() >= 0L)
                    method = MethodsForCommands.class.getDeclaredMethod(methodName, ExtendedMap.class);
                // Parameter-command has ONE parameter - result
                else if (getParameterType() >= Commands.PARAMETER_KEYWORD)
                    method = MethodsForCommands.class.getDeclaredMethod(methodName, Object.class);
                // Parameter-command has LIST parameter - result
                else if (getParameterType() >= Commands.PARAMETER_KEYWORD_LIST)
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

        private long params[];
        private Method method;

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
        }

    }

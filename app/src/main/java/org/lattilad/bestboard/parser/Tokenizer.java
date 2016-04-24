package org.lattilad.bestboard.parser;

import android.content.Context;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.scribe.Scribe;

import java.io.IOException;
import java.io.Reader;


/**
 * ReaderTokenizer identifies keywords and data from a reader stream.
 * Keywords can contain only ASCII (<128) characters (letters, digits and underscore), 
 * while full BMP Unicode set is allowed in string and character constants.
 * Tokens should be surrounded by white-spaces
 * (Currently ascii chars below space, '(' and ')' as section delimiters).
 * Recognition will skip to the next white-space from the first not-known character.
 * <p> 
 * Tokens can be: 
 * <ul>
 * <li>keywords - letters, digits and '_' allowed.
 * Keywords are converted to lowercase, and cannot begin with number.</li>
 * <li>"string" - All BMP characters, some escape sequences are allowed.
 * Strings can be divided by '_' (underline sign right after the " sign: "_)</li>
 * <li>'c' - One character. All BMP characters, same escape sequences are allowed.</li>
 * <li>-+integers - negative or positive sign (on the first position) and 0-9 are allowed.</li>
 * <li>-+0xhexadecimals - same as integers, but A-F letters are also allowed.</li>
 * <li>0ccolors - same as integers, but in aarrggbb/rrggbb/argb/rgb format, 
 * with hexadecimal digits. Default 'aa/a' is 0xFF.
 * One or two digits will be interpreted as grayscale.</li>
 * <li>-+decimal.fraction - same as integers (with one decimal point).</li>
 * <li>EOL - end of line can be treated as white-space (default) or separate token (after #)</li>
 * <li>EOF - end of file is treated as a separate token.</li>
 * <li>( and ) - start and end of section are treated as tokens.</li>
 * <li># - toggles EOL recognition.</li>
 * <li>;notes - all characters (till textual EOL) are ignored after the note sign.</li>
 * </ul>
 * <p>
 * EOL - can be treated as normal white-space or as separate token.
 * ignoreEOL sets default behavior (from code), # toggles it (from stream).
 * <p>
 * Escape sequence can be used instead of any character. 
 * Not all of the escape sequences are implemented yet.
 * <ul>
 * <li> '\n' - New Line/Line Feed (0x0A) </li>
 * <li> '\r' - Carriage Return (0x0D) </li>
 * <li> '\t' - (Horizontal) Tab (0x09) </li>
 * <li> '\\' - Backslash (0x5C) </li>
 * <li> '\'' - Single quotation mark (0x27) </li>
 * <li> '\"' - Double quotation mark (0x22)</li>
 * <li> '\HHHH' - Four digit hexadecimal unicode</li>
 * </ul>
 * <p>
 * nextToken() will recognize the next token and return the token type.
 * getStringToken(), getIntegerToken, getDoubleToken can be used to retrieve the parameters:
 * <ul>
 * <li> TYPE_KEYWORD - string representation and generated token code </li>
 * <li> TYPE_STRING - string constant without double quotation marks </li>
 * <li> TYPE_CHARACTER - character without single  quotation marks as string and unicode value </li>
 * <li> TYPE_INTEGER - long precision integer and original number as string </li>
 * <li> TYPE_FRACTION - double precision fraction and original number as string </li>
 * <li> TYPE_EOL and TYPE_EOF, </li>
 * <li> TYPE_START and TYPE_END - are standalone tokens, but they will return only type </li>
 * </ul>
 * <p>
 * Read errors will throw exception, but recognition mistakes will not stop the process.
 * A message log will be generated in case of any mistakes. 
 */
public class Tokenizer
	{
	/**
	 ** TOKEN TYPES
	 **/

	/** INTERNAL: Type not determined yet - this type will never returned. */
	private static final int TYPE_UNKNOWN = 0;

	/** INTERNAL: Hexadecimal number - TYPE_INTEGER will be returned. */
	private static final int TYPE_HEXADECIMAL = 6;

	/** INTERNAL: Color value - TYPE_INTEGER will be returned. */
	private static final int TYPE_COLOR = 7;

	/** End of reader stream token */
	public static final int TYPE_EOF = -1;

	/** End of line token */
	public static final int TYPE_EOL = -2;
	
	/** Start of section */
	public static final int TYPE_START = -3;

	/** End of section */
	public static final int TYPE_END = -4;
	
	/** Keyword token, string representation and generated code are returned */
	public static final int TYPE_KEYWORD = 1;

	/** String constant token between double quotation marks, only string representation is returned */
	public static final int TYPE_STRING = 2;

	/** Character constant token, character as string, and unicode value as integer will be returned */
	public static final int TYPE_CHARACTER = 3;

	/** Integer number. Long precision, and the original number as string will be returned */
	public static final int TYPE_INTEGER = 4;

	/** Decimal fraction. Double precision, and the original number as string will be returned */
	public static final int TYPE_FRACTION = 5;

	
	/**
	 ** CONSTRUCTOR - SETTING UP READER SOURCE
	 **/
	
	/** Reader stream source. Only read() method is used. */
	private Reader reader;

	/** Context needed to reach string resources. */
	private Context context;
	
	/**
	 * Constructs a new {@code ReaderTokenizer} with {@code reader} as source reader. 
	 * @param context context to get resources
	 * @param reader reader source
	 * @throws java.io.IOException
     * @throws NullPointerException if {@code reader} is {@code null}.
	 */
	public Tokenizer(Context context, Reader reader) throws IOException
		{
		if ( reader == null)
			throw new NullPointerException("Reader parameter is null!");
		this.reader = reader;

		if ( context == null)
			throw new NullPointerException("Context parameter is null!");
		this.context = context;

		// Check for BOM
		if ( read() == 0xFEFF )
			note( R.string.note_bom_character );
		else
			pushBackLastRead();
		}


	/**
	 ** PARAMETERS OF CURRENT TOKEN
	 **/

	/** Type of the token */
	private int tokenType;

	/**
	 * String value of the token
	 * TYPE_EOF, TYPE_EOL, TYPE_START, TYPE_END - Empty string;
	 * TYPE_KEYWORD - lowercase token;
	 * TYPE_STRING - string without quotation marks;
	 * TYPE_CHARACTER - one character long string;
	 * TYPE_INTEGER, TYPE_FRACTION - original number as string;
	 */
	private StringBuilder tokenStringBuilder = new StringBuilder();

	/**
	 * Integer value of the token
	 * TYPE_INTEGER - absolute (always positive) value, tokenMinusSign marks negative values
	 * TYPE_CHARACTER - unicode value (always between 0x0000-0xFFFF)
	 * TYPE_KEYWORD - token code created from the first dozen characters
	 * TYPE_DOUBLE - absolute integer created without the decimal sign
	 * all other types - null value;
	 */
	private long tokenInteger;

	/**
	 * Divider part of the double token (fraction)
	 * TYPE_FRACTION - Divider part for tokenFraction
	 * all other types - null value;
	 */
	private long tokenDivider;

	/** True if the numeric value (tokenInteger or tokenDouble) is negative. */
	private boolean tokenMinusSign;

	/**
	 * No more digits can be processed for numbers (also parts of fractions).
	 * Result is unpredictable above it, log message will be sent.
	 */
	public final static int MAX_DECIMAL_DIGITS = 18;

	/**
	 * No more digits can be processed for hexadecimal numbers.
	 * Result is unpredictable above it, log message will be sent.
	 */
	public final static int MAX_HEXADECIMAL_DIGITS = 15;

	/**
	 * No more characters can be processed for keywords.
	 * Result is unpredictable above it, log message will be sent.
	 */
	public final static int MAX_KEYWORD_CHARACTERS = 12;


	/**
	 ** GETTERS FOR TOKEN PARAMETERS
	 **/

	/**
	 * Get type of the token, as returned by nextToken()
	 */
	public int getTokenType()
		{
		return tokenType;
		}

	/**
	 * Get string representation of current token
	 * @return string value as:
	 * TYPE_OEF, TYPE_EOL, TYPE_START, TYPE END - Empty string;
	 * TYPE_KEYWORD - lowercase token;
	 * TYPE_STRING - string without quotation marks;
	 * TYPE_CHARACTER - one character long string without quotation marks;
	 * TYPE_INTEGER, TYPE_FRACTION - original number as string;
	 */
	public String getStringToken()
		{
		return tokenStringBuilder.toString();
		}

	/**
	 * Get integer representation of current token
	 * @return integer value as:
	 * TYPE_INTEGER - integer value (can be negative)
	 * TYPE_FRACTION - integer value created without the decimal point (can be negative)
	 * TYPE_CHARACTER - unicode value (always between 0x0000-0xFFFF)
	 * TYPE_KEYWORD - token code created from the first dozen characters
	 * all other types - null value;
	 */
	public long getIntegerToken()
		{
		return tokenMinusSign ? -tokenInteger : tokenInteger;
		}

	/**
	 * Get divider integer for calculating double values.
	 * Double value = getInteger()/getIntegerDivider().
	 * This is experimental, later can be used for returning not only decimal fractions.
	 * @return integer value as:
	 * TYPE_FRACTION - integer divider
	 * all other types - null value;
	 */
	public long getIntegerDividerToken()
		{
		return tokenDivider;
		}

	/**
	 * Get double precision floating point representation of current token (fractions)
	 * !! This is just an experimental algorithm to get fractions.
	 * As a side effect all integer returning types will give the same integer here, as double.
	 * Java's parseDouble may work better (and quicker)
	 * @return double value as:
	 * TYPE_INTEGER, TYPE_FRACTION - double value (can be negative)
	 * TYPE_CHARACTER - unicode value (always between 0x0000-0xFFFF) as double
	 * TYPE_KEYWORD - token code created from the first dozen characters as double
	 * all other types - null value;
	 */
	public double getDoubleToken()
		{
		double result = (double)tokenInteger / tokenDivider;
		if ( tokenMinusSign && tokenInteger != 0 )
			return -result;
		return result;
		}

	/**
	 * Returns true if this is an EOL or EOF token.
	 */
	public boolean isEOL()
		{
		return tokenType == TYPE_EOL || tokenType == TYPE_EOF;
		}


	/**
	 ** LETTER AND DIGIT VALIDATIONS
	 **/

	/** End-of-file character */
	public static final int EOF = -1;

	/** End-of-line character */
	public static final int EOL = '\n';

	/** Start-of-section character */
	public static final int MARK_START = '(';

	/** End-of-section character */
	public static final int MARK_END = ')';

	/** End-of-line recognition toggle */
	public static final int EOL_TOGGLE = '#';

	/** Note character - line will be ignored after note */
	public static final int MARK_NOTE = ';';

	/** Decimal fraction character */
	public static final int MARK_FRACTION = '.';

    /** String (double) quote mark character */
	public static final int MARK_STRING = '\"';

    /** String should continue - (double) quote mark character and a plus sign together */
    public static final int MARK_CONTINUE = '_';

    /** Character (single) quote mark character */
	public static final int MARK_CHARACTER = '\'';

    /** Unicode mark (dollar) character */
    public static final int MARK_UNICODE = '$';

	/** Minus sign character */
	public static final int MARK_MINUS = '-';

	/** Plus sign character */
	public static final int MARK_PLUS = '+';

	/** Escape sequence comming */
	public static final int MARK_ESCAPE = '\\';

    /** Keyword token-codes start above this level */
    public static final long TOKEN_CODE_SHIFT = 0xFFFFL;


	/**
	 * True if character is a decimal digit
	 */
	private boolean isValidDecimalDigit( int ch )
		{
        return ch >= '0' && ch <= '9';
        }

	/**
	 * True if character is a hexadecimal digit
	 */
	private boolean isValidHexadecimalDigit( int ch )
		{
		if ( ch >= '0' && ch <= '9' )
			return true;
		if ( ch >= 'a' && ch <= 'f' )
			return true;
		if ( ch >= 'A' && ch <= 'F' )
			return true;
		return false;
		}

	/**
	 * True if character is an ASCII non-accented letter
	 */
	private boolean isValidAsciiLetter( int ch )
		{
		if ( ch >= 'a' && ch <= 'z' )
			return true;
		if ( ch >= 'A' && ch <= 'Z' )
			return true;
		return false;
		}

	/**
	 * Converts non-accented ascii letters to lowercase
	 */
	private int toLowerCaseAsciiLetter( int ch )
		{
		if ( ch >= 'A' && ch <= 'Z' )
			return ch + 'a' - 'A';
		return ch;
		}

	/**
	 * True if character is a valid white-space
     * Eventually white-space terminates each token
	 * EOL and EOF, START and END are special white-spaces
	 * !! Special mark characters cannot be used as white spaces !!
	 */
	private boolean isValidWhiteSpace( int ch )
		{
		if ( ch <= ' ' || ch == MARK_START || ch == MARK_END )
			return true;

		// NOTE is not a real white space, but identifieing it as a white space stops
		// any skip of non-white-spaces
		// That means: no white-spaces are needed before the MARK_NOTE,
		// Therefore MARK_NOTE should be evaluated BEFORE white-spaces
        return ch == MARK_NOTE;
        }

	/**
	 * True if character is accepted in keywords
	 */
	private boolean isValidKeyword( int ch )
		{
        return isValidAsciiLetter(ch) || isValidDecimalDigit(ch) || ch == '_';
        }

	/** Number of characters used for keywords */
	private final static int TOKEN_CODE_RADIX = 'Z' - 'A' + 12;

	/**
	 * Returns the numeric value of the (hexa)decimal character.
	 * Result is inpredictable if character is not valid!
	 * This method is used for creating special token codes for keywords.
	 * In this case -'z' and '_' are allowed.
	 * @param ch (Hexa)decimal character '0'-'9' or 'a'-'f' or 'A'-'F'.
	 * Also 'G'-'Z' and '_' for keywords.
	 * @return decimal value (0-15)
	 */
	private int valueOf( int ch )
		{
		if ( ch <= '9' ) // && >='0'
			return ch-'0';

		if ( ch <= 'Z' ) // && ch >= 'A'
			return ch-'A'+10;

		if ( ch == '_' )
			return TOKEN_CODE_RADIX -1;

		// 'a' <= ch <= 'z'
		return ch-'a'+10;
		}

	/**
	 * Character values as string. Used in error messages.
	 * [char] (unicode) format. [char] is not displayed for characters below ASCII space.
	 * @param ch character
	 * @return character printable form and unicode value
	 */
	private String getCharacterDescription( int ch )
		{
		return (( ch >= ' ' ) ? "[" + (char)ch + "] " : "")  + "(" + ch + ")";
		}


	/**
	 ** READER STREAM READING FUNCTIONS
	 **/

	/** Temporary storage for last read */
	private int lastRead = 0;

	/** read() will not read a new character, but will give back the last one */
	private boolean rewindLastRead = false;

	/**
	 * Reads a new character from reader stream.
	 * If rewindLastRead == true, then the read will return last character instead of a new one
	 * @return Character from the reader stream
	 * @throws java.io.IOException if reading fails
	 */
	private int read() throws IOException
		{
		if ( rewindLastRead )
			rewindLastRead = false;
		else
			lastRead = reader.read();
		return lastRead;
		}

	/**
	 * Push back last character. Next read() will give the last character once more
	 */
	private void pushBackLastRead()
		{
		rewindLastRead = true;
		}

	/**
	 * Skips non-white-space characters in reader stream
	 * @throws java.io.IOException if reading fails
	 */
	private void findNextWhiteSpace() throws IOException
		{
		while ( !isValidWhiteSpace( read() ) );
		pushBackLastRead();
		}

	/**
	 * Skips to the end of the textual line (or EOF) in reader stream.
	 * It will forward to next EOL/EOF character, so ignoreEOL is NOT respected.
	 * The method will stop at the end of the textual line.
	 * nextToken() (which respects ignoreEOL) will give back EOL or
	 * first token of the next line if depending from the state of ignoreEOL.
	 * This method is only used for notes.
	 * Notes will not terminate the line of tokens, only textual lines
	 * therefore multiple lines (signed by #) can contain notes at the end of any line.
	 * @throws java.io.IOException if reading fails
	 */
	private void findNextEOL() throws IOException
		{
		int ch;
		while ( (ch = read()) != EOL && ch != EOF );
		pushBackLastRead();
		}

	/**
	 * Skips the whole remaining line. It respects ignoreEOL, so next valid EOL also will be skipped.
	 * Reads tokens, till the last read token will be an EOL/EOF.
	 * Next token will be the first token of the next line (or EOF once more).
	 * @throws java.io.IOException if reading fails
	 */
	public void skipThisLine() throws IOException
		{
		do 	{
			nextToken();
			} while ( !isEOL() );
		}


    /**
     * Skips next block. Can be a token, or a complex block between parentheses.
     * @throws IOException descriptor (coat) file reading fails
     */
    public void skipBlock() throws IOException
        {
        _block( 0 );
        }

    /**
     * Stops evaluation of block. This block is a complex block, evaluation is between parentheses.
     * !! Method will be added to Tokenizer later
     * @throws IOException descriptor (coat) file reading fails
     */
    public void stopBlock() throws IOException
        {
        _block( 1 );
        }

    /**
     * Helper method for skipBlock() and stopBlock().
     * Scans for the ending of embedded blocks. Starting deepness can be set.
     * 0 means: block is not entered jet, 1 means: tokenizer is after the first opening bracket.
     * @param deep to come out from this deepness
     * @throws IOException descriptor (coat) file reading fails
     */
    private void _block( int deep ) throws IOException
        {
        int tokenType;

        while (true)
            {
            tokenType = nextToken();

            if ( tokenType == Tokenizer.TYPE_START )
                {
                deep++;
                }
            else if ( tokenType == Tokenizer.TYPE_END )
                {
                deep--;
                if ( deep == 0 )
                    return;
                else if ( deep < 0 )
                    {
                    pushBackLastToken();
                    return;
                    }
                }
            else if ( tokenType == Tokenizer.TYPE_EOF )
                return;
            }
        }


    /**
     * Helper method for debugging - recreate keyword from keycode
     * @param code keycode
     * @return readable keyword
     */
    public static String regenerateKeyword(long code)
        {
        char val;
        StringBuilder keyword = new StringBuilder();

        // signed keyword-code cannot exist
        code &= 0x7FFFFFFFFFFFFFFFL;
        code -= TOKEN_CODE_SHIFT;

        while ( code > 0 )
            {
            val = (char) (code % TOKEN_CODE_RADIX);
            code /= TOKEN_CODE_RADIX;

            // Scribe.debug(" Code: " + code + " val: " + val);

            if ( val == TOKEN_CODE_RADIX -1 )
                keyword.insert(0, '_');
            else if ( val >= 10 )
                keyword.insert(0, (char)(val+'a'-10) );
            else
                keyword.insert(0, (char)(val+'0') );
            }

        // Scribe.debug(" Keyword: " + keyword.toString());

        return keyword.toString();
        }


    /**
     ** ERROR HANDLING
     **
     ** Log messages come through these methods.
     ** Line number is attached to all messages.
     ** Error messages are counted.
     **
     ** External classes can use these log handling,
     ** so their messages are completed with line number,
     ** and error messages are also counted.
     **/

	/** Line number. Needed for log messages */
	private int lineNumber = 1;

	/** Returns the number of the actual line. Needed for log messages, helps to locate errors. */
	public int getLineNumber()
		{
		return lineNumber;
		}

	/** Counter of errors */
	private int errorCount = 0;

	/** Returns the number of errors. 0 == no errors, correctly formatted input. */
	public int getErrorCount()
		{
		return errorCount;
		}


    /**
     * Convenience method: error message for secondary log.
     * @param messageResource message resource id
     */
    public void error( int messageResource )
        {
        error( null, messageResource, null );
        }

    /**
     * Convenience method: error message for secondary log.
     * @param source source of bad data - if needed. Can be null.
     * @param messageResource message resource id
     */
    public void error( String source, int messageResource )
        {
        error( source, messageResource, null );
        }

    /**
     * Convenience method: error message for secondary log.
     * @param messageResource message resource id
     * @param value value of the bad data - if needed. Can be null.
     */
    public void error( int messageResource, String value )
        {
        error( null, messageResource, value );
        }

    /**
     * Error message for secondary log.
     * @param source source of the bad data - if needed. Can be null.
     * @param messageResource message resource id
     * @param value value of the bad data - if needed. Can be null.
     */
    public void error( String source, int messageResource, String value )
        {
        errorCount++;

        StringBuilder sb = new StringBuilder();
        if ( source != null )
            {
            sb.append("[").append(source).append("] ");
            }
        sb.append( context.getString( messageResource ));
        if ( value != null )
            {
            sb.append(" ").append(value);
            }
        sb.append( " (line: " ).append( getLineNumber()).append(") ");

        Scribe.error_secondary( sb.toString() );
        }

    /**
     * Convenience method: note message for secondary log.
     * @param messageResource message resource id
     */
    public void note( int messageResource )
        {
        note( null, messageResource, null );
        }

    /**
     * Convenience method: note message for secondary log.
     * @param source source of data - if needed. Can be null.
     * @param messageResource message resource id
     */
    public void note( String source, int messageResource )
        {
        note( source, messageResource, null );
        }

    /**
     * Convenience method: note message for secondary log.
     * @param messageResource message resource id
     * @param value value of data - if needed. Can be null.
     */
    public void note( int messageResource, String value )
        {
        note( null, messageResource, value );
        }

    /**
     * Note message for secondary log.
     * @param source source of data - if needed. Can be null.
     * @param messageResource message resource id
     * @param value value of data - if needed. Can be null.
     */
    public void note( String source, int messageResource, String value )
        {
        StringBuilder sb = new StringBuilder();
        if ( source != null )
            {
            sb.append("[").append(source).append("] ");
            }
        sb.append( context.getString( messageResource ));
        if ( value != null )
            {
            sb.append(" ").append(value);
            }
        sb.append( " (line: " ).append( getLineNumber()).append(") ");

        Scribe.note_secondary(sb.toString());
        }


    /**
     ** HEART OF THE CLASS: TOKENIZER PART
     **/

	/**
	 * Converts escape sequences into characters.
	 * Sequences are read from reader stream, after the escape sign ('\').
	 * \n, \r, \t and \\ (MARK_ESCAPE), \" (MARK_STRING), \'(MARK_CHARACTER) are identified.
	 * Unknown sequnces's second caharcter will be treated as normal.
	 * Or: FOUR hexadecimal digits will be converted to unicode. (\HHHH)
	 * Not four digit long sequnces will be skipped.
	 * @return converted character
	 * @throws java.io.IOException if reading fails.
	 */
	private int convertEscapeSequence() throws IOException
		{
		int ch;

		ch = read();
		// some of the conventional escape sequences for formatting
		if ( ch == 'n' )	return '\n';
		if ( ch == 'r' )	return '\r';
		if ( ch == 't' )	return '\t';
		// These marks could not be used without escaped sequences
		if ( ch == MARK_ESCAPE )	return MARK_ESCAPE;
		if ( ch == MARK_STRING )	return MARK_STRING;
		if ( ch == MARK_CHARACTER )	return MARK_CHARACTER;
		if ( ch == MARK_UNICODE )	return MARK_UNICODE;

		// Four digit hexadecimal code identifies unicode character
		int value = 0;
		int len = 1;
		while (true)
			{
			if ( isValidHexadecimalDigit(ch) )
				{
				value*=0x10;
				value+= valueOf(ch);
				}
			else // Non-valid character was found
				{
				if ( len==1 )
					{
					// This is not a malformed hexadecimal value, rather an unknown escape sequence. Returned as normal (non-escaped) character.
					error( getCharacterDescription(ch), R.string.error_escape_unknown );
					return ch;
					}
				else
					{
					// This is not a four-digit hexadecimal code.
					// Try to return shorter code.
					error( getCharacterDescription( value ), R.string.error_escape_malformed );
					pushBackLastRead();
					break;
					}
				}

			if ( len >= 4)
				break;

			ch = read();
			len++;
			}

		return value;
		}


	/**
	 * Converts unicode (numeric-sequences) into two-characters strings.
	 * Sequences are read from reader stream, after the unicode-mark sign ('$').
	 * First non-hexadecimal character terminates the sequence.
     * Sequence will be terminated after 6 characters
	 * @return converted two-characters long string
	 * @throws java.io.IOException if reading fails.
	 */
	private String convertUnicodeSequence() throws IOException
		{
		int ch;

		long value = 0;
        int len = 0;

        while ( len < 6 )
            {
            ch = read();

            if ( !isValidHexadecimalDigit( ch ) )
                {
                pushBackLastRead();
                break;
                }

			value*=0x10;
			value+= valueOf(ch);
			len++;
			}

        if ( value < 0x10000 )
            {
            // Scribe.debug( "Unicode: " + Long.toHexString( value ));
            return String.valueOf( (char) value );
            }

        if ( value > 0x10FFFF )
            {
            // Scribe.debug( "Unicode: value do not exist! " + Long.toHexString( value ) );
            return "";
            }

        value -= 0x10000;
        char[] units = new char[2];
        units[0] = (char)(0xD800 | ( value >> 10 ));
        units[1] = (char)(0xDC00 | ( value & 0x3FF ));

        // Scribe.debug( "Unicode: " + Integer.toHexString(units[0]) + " - " + Integer.toHexString(units[1]) );

        return String.valueOf( units );

        /*
        // http://unicodebook.readthedocs.org/en/latest/unicode_encodings.html
        void
        encode_utf16_pair(uint32_t character, uint16_t *units)
            {
            unsigned int code;
            assert(0x10000 <= character && character <= 0x10FFF);
            code = (character - 0x10000);
            units[0] = 0xD800 | (code >> 10);
            units[1] = 0xDC00 | (code & 0x3FF);
            }
        */
		}


	/** nextToken() will not read a new token, but will give back the last one if true */
	private boolean rewindLastToken = false;

	/**
	 * Push back last token. Next call on nextToken() will give the last token once more
	 */
	public void pushBackLastToken()
		{
		rewindLastToken = true;
		}

	/**
     * EOL will be recognized as a whitespace instead of a standalone token (true default).
     * EOL_TOGGLE will toggle this
     * *** IMPORTANT!! NOW WE USE BLOCK COMMANDS!! ***
     */
	private boolean ignoreEOL = true;

	/**
	 * This is the central part of the class: tokenize the next token from reader.
	 * Token type is returned, token parameters can be read by getStringToken(), getIntegerToken, getDoubleToken().
	 * Reading errors generate exception, but format mistakes will be only logged.
	 * @return token type
	 * @throws java.io.IOException
	 */
	public int nextToken() throws IOException
		{
		// Repeat the last token
		if (rewindLastToken)
			{
			rewindLastToken = false;
			return tokenType;
			}
		
		// Clear token parameters
		tokenType = TYPE_UNKNOWN;
		tokenStringBuilder.setLength( 0 );
		tokenInteger = 0;
		tokenDivider = 1;
		tokenMinusSign = false;
		
		// number of characters in character constans (if not 1 an error will be generated)
		// number of integer digits in numerical types (if exceeds MAX_DIGITS an error will be generated)
		// number of characters in keywords (longer than MAX_CHARACTERS cannot be converted to long token codes)
		// not used in string type (no length limit)
		int tokenLength = 0;
		
		// The digits of argb, rgb and single digit grayscale formats are counting two digits.
		// This is a helper variable to immediateli calculate doubled digits.
		// Whether Integer or IntegerDoubledDigits should be returned, will be decided after counting all digits. 
		long tokenIntegerDoubledDigits = 0;
		
		int ch;
		while (true)
			{
			ch = read();
			
			// TYPE_UNKNOWN: 
			// First character determines tokenType
			// ************************************
			// ?? what is the best order ??
			// Digits should be before keywords (keywords cannot start with digits)
			// NOTE_MARKS should be before white-spaces (it is special "white-space")
			if ( tokenType == TYPE_UNKNOWN )
				{

				// Unsigned integer (or fraction) number is starting, this character will be evaluated as well
				// This is the entry point for hexadecimal and color values, too
				if ( isValidDecimalDigit(ch) )
					{
					tokenType = TYPE_INTEGER;
					}
				
				// Signed number is starting. Sign can be only the first charter! 
				else if ( ch == MARK_MINUS || ch == MARK_PLUS )
					{
					tokenType = TYPE_INTEGER;
					tokenStringBuilder.append( (char)ch );
					tokenMinusSign = ch == MARK_MINUS;
					// next characters are processed as numbers
					continue;
					}

				// Fraction is started without any leading integer digits 
				else if ( ch == MARK_FRACTION )
					{
					tokenType = TYPE_FRACTION;
					tokenStringBuilder.append( (char)ch );
					// next characters are processed as numbers
					continue;
					}

				// string token starts
				else if ( ch == MARK_STRING )
					{
					tokenType = TYPE_STRING;
					// String delimiter itself is not needed
					continue; 
					}

				// ' character constant token starts
				else if ( ch == MARK_CHARACTER )  
					{
					tokenType = TYPE_CHARACTER;
					// Character delimiter is not needed
					continue; 
					}

				// Note - skip this line
				else if ( ch == MARK_NOTE )
					{
					// Type remains unknown, but in next round an EOL or EOF should come 
					findNextEOL();
					continue;
					}
				
				// Keyword is starting. Because numbers proceed keywords, keywords cannot start with numbers
				else if ( isValidKeyword(ch) )
					{
					tokenType = TYPE_KEYWORD;
					}

				// START - this is a special white-space
				else if ( ch == MARK_START )
					{
					tokenType = TYPE_START;
					// This token is always one character long, we can finish now
					return tokenType;
					}
				
				// END - this is a special white-space
				else if ( ch == MARK_END )
					{
					tokenType = TYPE_END;
					// This token is always one character long, we can finish now
					return tokenType;
					}
				
				// EOF - this is a special white-space
				else if ( ch == EOF )
					{
					tokenType = TYPE_EOF;
					// This token is always one character long, we can finish now
					return tokenType;
					}
				
				// EOL - this is a special white-space
				else if ( ch == EOL )
					{
					// Next read comes from the next line
					lineNumber++;
					
					if ( ignoreEOL )
						{
						// treated as valid white-space
						continue;
						}
					
					// This token is always one character long, we can finish now
					tokenType = TYPE_EOL;
					return tokenType;
					}

				// EOL_TOGGLE - toggles EOL recognition
				else if ( ch == EOL_TOGGLE )
					{
					ignoreEOL = !ignoreEOL;
					continue;
					}

				// Other, non special white spaces are skipped
				else if ( isValidWhiteSpace(ch) )
					{
					continue;
					}
				
				// All other characters are not allowed! The whole token will be skipped
				else
					{
					error( getCharacterDescription(ch), R.string.error_character_invalid );
					findNextWhiteSpace();
					continue;
					}
				}
			
			// All consecutive characters are processed here
			// Token types are the branches of this process
			// *********************************************
			switch ( tokenType )
				{
				case TYPE_INTEGER:
					// All numeric tokens are start as integers.
					// Minus sign was already set.
					// 0x converts integer to hexadecimal integer
					// . converts integer to fractal (floating point) !!
					// Non-numeric ending is truncated till the next white-space
					if ( isValidDecimalDigit(ch) )
						{
						tokenStringBuilder.append( (char)ch );
						if ( tokenLength == MAX_DECIMAL_DIGITS )
							error( getStringToken(), R.string.error_integer_exceeds );
						tokenInteger *= 10L;
						tokenInteger += valueOf( ch );
						tokenLength++;
						}
					else if ( (ch=='x' || ch=='X') && tokenInteger == 0 )
						{
						tokenStringBuilder.append( (char)ch );
						tokenLength = 0;
						tokenType = TYPE_HEXADECIMAL;
						}
					else if ( (ch=='c' || ch=='C') && tokenInteger == 0 )
						{
						tokenStringBuilder.append( (char)ch );
						tokenLength = 0;
						tokenMinusSign = false; // minus sign will be omitted
						tokenType = TYPE_COLOR;
						}
					else if ( (ch == MARK_FRACTION) )
						{
						tokenStringBuilder.append( (char)ch );
						tokenType = TYPE_FRACTION;
						}
					else 
						{
						// Can be MARK_PLUS, _MINUS, _FRACTION - but without any valid digits
						if ( tokenLength < 1)
							error( getStringToken(), R.string.error_integer_malformed_no_numeric );
						if ( !isValidWhiteSpace(ch) )
							{
							error( getStringToken(), R.string.error_integer_malformed_ending );
							findNextWhiteSpace();
							}
						else
							{
							// Correctly terminated integer returned - no messages.
							// Last white-space should be evaluated.
							pushBackLastRead();
							}
						// valid external token should be given because of rewindLastToken
						tokenType = TYPE_INTEGER;
						return tokenType;
						}
					break;
					
				case TYPE_HEXADECIMAL:
					// Format: 0xH...H or 0XH...H, can be signed
					// Otherwise similar to integer type
					if ( isValidHexadecimalDigit(ch) )
						{
						tokenStringBuilder.append( (char)ch );
						if ( tokenLength == MAX_HEXADECIMAL_DIGITS )
							error( getStringToken(), R.string.error_hexadecimal_exceeds );
						tokenInteger *= 0x10L;
						tokenInteger += valueOf(ch);
						tokenLength++;
						}
					else 
						{
						// Can be 0x without any valid digits
						if ( tokenLength < 1)
							error( getStringToken(), R.string.error_hexadecimal_malformed_no_numeric );
						if ( !isValidWhiteSpace(ch) )
							{
							error( getStringToken(), R.string.error_hexadecimal_malformed_ending );
							findNextWhiteSpace();
							}
						else
							{
							// Correctly terminated hexadecimal integer returned - no messages.
							// Last white-space should be evaluated.
							pushBackLastRead();
							}
                        // valid external token should be given because of rewindLastToken
                        tokenType = TYPE_INTEGER;
                        return tokenType;
						}
					break;
					
				case TYPE_COLOR:
					// Format: 
					// 0xaarrggbb - 8 digits hexadecimal,
					// 0xrrggbb - 6 digits hexadecimal (+ 0xFF as aa)
					// 0xargb - 4 digits hexadecimal - all digits will be doubled
					// 0xrgb - 3 digits hexadecimal - all digits will be doubled (+ 0xFF as aa)
					// 0xhh - 2 digits hexadecimal - will be used for all colors (+ 0xFF as aa) - grayscale
					// 0xh - 1 digit hexadecimal - will be used for all color digits (+ 0xFF as aa) - grayscale
					// minus sign is already omitted!
					// Otherwise similar to integer type
					if ( isValidHexadecimalDigit(ch) )
						{
						int digit = valueOf(ch);
						
						tokenStringBuilder.append( (char)ch );
						tokenInteger *= 0x10L;
						tokenInteger += digit;
						// doubled digits will be added twice
						tokenIntegerDoubledDigits *= 0x10L;
						tokenIntegerDoubledDigits += digit;
						tokenIntegerDoubledDigits *= 0x10L;
						tokenIntegerDoubledDigits += digit;
						tokenLength++;
						}
					else 
						{
						// Can be 0x without any valid digits
						if ( tokenLength < 1)
							error( getStringToken(), R.string.error_color_malformed_no_numeric );
						// One digit grayscale
						else if ( tokenLength == 1 ) // 0xh
							{
							tokenInteger = 0xFF000000L +
									tokenIntegerDoubledDigits * 0x10000L +
									tokenIntegerDoubledDigits * 0x100L +
									tokenIntegerDoubledDigits;
							}
						// Two digits grayscale
						else if ( tokenLength == 2 ) // 0xhh
							{
							tokenInteger = 0xFF000000L +
									tokenInteger * 0x10000L +
									tokenInteger * 0x100L +
									tokenInteger;
							}
						// Red-Green-Blue each has one digit
						else if ( tokenLength == 3 ) // 0xrgb
							{
							tokenInteger = 0xFF000000L + tokenIntegerDoubledDigits;
							}
						// Alpha-Red-Green-Blue each has one digit
						else if ( tokenLength == 4 ) // 0xargb
							{
							tokenInteger = tokenIntegerDoubledDigits;
							}
						// Red-Green-Blue each has two digits
						else if ( tokenLength == 6 ) // 0xrrggbb
							{
							tokenInteger = 0xFF000000L + tokenInteger;
							}
						// Alpha-Red-Green-Blue each has two digits - full color value
						else if ( tokenLength == 8 ) // 0xaarrggbb
							{
							// nothing to do, tokenInteger already contains information
							}
						else
							{
							tokenInteger = 0xFFFFFFFFL;
							error( getStringToken(), R.string.error_color_malformed_returning_opaque );
							}

						if ( !isValidWhiteSpace(ch) )
							{
							error( getStringToken(), R.string.error_color_malformed_ending );
							findNextWhiteSpace();
							}
						else
							{
							// Correctly terminated hexadecimal integer returned - no messages.
							// Last white-space should be evaluated.
							pushBackLastRead();
							}
                        // valid external token should be given because of rewindLastToken
                        tokenType = TYPE_INTEGER;
                        return tokenType;
						}
					break;
					
				case TYPE_FRACTION:
					// Format: d...d.d...d, can be signed
					// Will be returned only as double
					// Otherwise similar to integer type
					if ( isValidDecimalDigit(ch) )
						{
						tokenStringBuilder.append( (char)ch );
						if ( tokenLength == MAX_DECIMAL_DIGITS )
							error( getStringToken(), R.string.error_fraction_exceeds );
						tokenInteger *= 10;
						tokenInteger += valueOf( ch );
						tokenDivider*= 10;
						tokenLength++;
						}
					else 
						{
						// Can be MARK_PLUS, _MINUS, _FRACTION - but without any valid digits
						if ( tokenLength < 1)
							error( getStringToken(), R.string.error_fraction_malformed_no_numeric );
						if ( !isValidWhiteSpace(ch) )
							{
							error( getStringToken(), R.string.error_fraction_malformed_ending );
							findNextWhiteSpace();
							}
						else
							{
							// Correctly terminated fraction returned - no messages.
							// Last white-space should be evaluated.
							pushBackLastRead();
							}
						return tokenType;
						}
					break;
					
				case TYPE_KEYWORD:
					// Cannot start with number, otherwise all keyword chars are accepted
					// Ending from unknown character will be truncated
					if ( isValidKeyword(ch) )
						{
						tokenInteger*=TOKEN_CODE_RADIX;
						tokenInteger+= valueOf(ch);
						ch = toLowerCaseAsciiLetter(ch);
						tokenStringBuilder.append( (char)ch );
						if ( tokenLength == MAX_KEYWORD_CHARACTERS )
							error( getStringToken(), R.string.error_keyword_exceeds );
						tokenLength++;
						}
					else
						{
                        if ( isValidWhiteSpace(ch) )
                            {
                            // Correctly terminated keyword returned - no messages.
                            // Last white-space should be evaluated.
                            pushBackLastRead();
                            }
                        else
                            {
                            error( getStringToken(), R.string.error_keyword_malformed_ending );
                            findNextWhiteSpace();
                            }
                        // keyword's token-integer is shifted
                        tokenInteger += TOKEN_CODE_SHIFT;
                        // it cannot be negative or smaller then TOKEN_CODE_SHIFT
                        if ( tokenInteger <= TOKEN_CODE_SHIFT )
                            {
                            // signed bit is cleared
                            tokenInteger &= 0x7FFFFFFFFFFFFFFFL;
                            // if the value is till too small, then it is shifted once more
                            if ( tokenInteger <= TOKEN_CODE_SHIFT )
                                tokenInteger += TOKEN_CODE_SHIFT;
                            }
						return tokenType;
						}
					break;

				case TYPE_STRING:
					// String tokens are ended with ", or terminated by EOL, EOF.
					// All escape sequences are translated to characters, which are added to the token.
					if ( ch == MARK_STRING )
						{
                        if ( read() == MARK_CONTINUE )
                            {
                            while ( (ch = read()) != MARK_STRING )
                                {
                                if ( ch == MARK_NOTE )
                                    findNextEOL();
                                else if ( ch > ' ' || ch == EOF )
                                    {
                                    error( getStringToken(), R.string.error_string_not_terminated );
                                    pushBackLastRead();
                                    return tokenType;
                                    }
                                }
                            continue;
                            }
                        else
                            {
                            pushBackLastRead();
                            return tokenType;
                            }
						}
					else if ( ch == EOF )
						{
						error(getStringToken(), R.string.error_string_not_terminated);
						return tokenType;
						}
					else if ( ch == EOL )
						{
						continue;
						}
                    else if ( ch == MARK_UNICODE )
                        {
                        String unicode = convertUnicodeSequence();
                        tokenStringBuilder.append( unicode );
                        tokenLength += unicode.length();
                        break;
                        }
					else if ( ch == '\\' )
						{
						ch = convertEscapeSequence();
						}
					tokenStringBuilder.append( (char)ch );
					tokenLength++;
					break;
	
				case TYPE_CHARACTER:
					// Characters are always ONE character long, ended with '
					// All escape sequences are translated to character
					// Longer character sequences are skipped till the first white-space
					if ( tokenLength == 0 ) // No character yet!
						{
						if ( ch == MARK_CHARACTER )
							{
							error( R.string.error_character_empty );
							return tokenType;
							}
						else if ( ch == EOL || ch == EOF )
							{
							error( R.string.error_character_missing );
							pushBackLastRead();
							return tokenType;
							}
						else if ( ch == '\\' )
							{
							ch = convertEscapeSequence();
							}
						tokenStringBuilder.append( (char)ch );
						tokenInteger = (char)ch;
						tokenLength++;
						}
					else if ( tokenLength >= 1 ) // Character is ready
						{
						if ( ch == MARK_CHARACTER )
							{
							// Correctly terminated character returned - no messages
							return tokenType;
							}
						else
							{
							error( getCharacterDescription(ch), R.string.error_character_not_terminated );
							pushBackLastRead(); // It can be even a new line!
							findNextWhiteSpace();
							return tokenType;
							}
						}
					break;

				default:
					// Impossible branch entry
					error( Integer.toString(tokenType), R.string.error_token_unknown );
					return tokenType;
				}
			}
		}
	}

package org.lattilad.bestboard.utils;

import java.util.Locale;

/**
 * Useful utilities for strings
 */
public class StringUtils
    {
    /**
     * Change the first character of the string to uppercase.
     * String.toUpperCase( locale ) method is used.
     * @param string string to change CANNOT BE NULL
     * @param locale locale
     * @return new string with uppercase character on the first position
     */
    public static String toUpperFirst( String string, Locale locale )
        {
        if ( string.length() <= 1 )
            {
            return string.toUpperCase( locale );
            }

        char c[] = string.toCharArray();
        c[0] = String.valueOf( c[0] ).toUpperCase( locale ).charAt( 0 );
        return new String(c);
        }

    /**
     * String is abbreviated to maxLength letters.
     * Trailing whitespaces are deleted,
     * and all other whitespaces are changed to one space.
     * Ellipsis is not added, because the space is very short.
     * @param string string to abbreviate ("" if string is null)
     * @param maxLength length of abbreviation in letters
     * @return the abbreviated string
     */
    public static String abbreviateString(String string, int maxLength)
        {
        if (string == null)
            return "";

        StringBuilder abbreviation = new StringBuilder(maxLength);
        boolean spaceAllowed = false;
        for (int counter = 0, abbreviationLength = 0;
             counter < string.length() && abbreviationLength < maxLength;
             counter++)
            {
            int ch = string.charAt(counter);

            if (isWhiteSpace(ch))
                {
                if (spaceAllowed)
                    {
                    spaceAllowed = false;
                    abbreviation.append(' ');
                    abbreviationLength++;
                    }
                // skip longer whitespaces
                }
            else
                {
                spaceAllowed = true;
                abbreviation.append(ch);
                if (!isUTF16FirstHalf(ch))
                    abbreviationLength++;
                // UTF16 lower part will not increase counter
                }
            }
        return abbreviation.toString();
        }

    /**
     * True is ch (2 bytes!) is the lower part of an utf16 code-point
     */
    public static boolean isUTF16FirstHalf( int ch )
        {
        return (ch & 0xFC00) == 0xD800;
        }

    /**
     * True is ch (2 bytes!) is the upper part of an utf16 code-point
     */
    public static boolean isUTF16SecondHalf( int ch )
        {
        return (ch & 0xFC00) == 0xDC00;
        }

    /**
     * True if ch is a whitespace.
     * Currently chars between 0 and 32 are treated as whitespace.
     * -1 (as error) is NOT a whitespace!
     */
    public static boolean isWhiteSpace( int ch )
        {
        // -1 is NOT whitespace
        return ch <= ' ' && ch >= 0;
        // OR: return ch == ' ' || ch == '\n' || ch == '\t';
        }

    /**
     * True if ch is a space.
     * Currently only ' ' (ASCII 32) is treated as space.
     */
    public static boolean isSpace( int ch )
        {
        return ch == ' ';
        }
    }

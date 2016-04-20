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


    public static final int UNKNOWN_CASE = -1;
    public static final int LOWER_CASE = 1;
    public static final int MIXED_CASE = 2;
    public static final int FIRST_UPPER_CASE = 3;
    public static final int UPPER_CASE = 4;

    public static int checkStringCase( String string )
        {
        return checkStringCase( string, Integer.MAX_VALUE );
        }

    /**
     * Checks the case of the string.
     * UNKNOWN_CASE - no letters in the string
     * LOWER_CASE - all letters are lower case
     * UPPER_CASE - all letters are upper case
     * FIRST_UPPER_CASE - first letter is uppercase, but all others are lowercase
     * MICED_CASE - mixed lower case and upper case letters
     * @param string string to check
     * @param maxLength max. characters to check
     * @return case-type
     */
    public static int checkStringCase( String string, int maxLength )
        {
        int counter;
        int firstLetter = UNKNOWN_CASE;
        int lowerCaseLetters = 0;
        int upperCaseLetters = 0;

        if (string.length() < maxLength)
            maxLength = string.length();

        // Find the first letter
        for (counter = 0; counter < maxLength; counter++)
            {
            if (Character.isLetter(string.charAt(counter)))
                {
                firstLetter = Character.isUpperCase(string.charAt(counter)) ? UPPER_CASE : LOWER_CASE;
                break;
                }
            }
        for (counter++; counter < maxLength; counter++)
            {
            if (Character.isLetter(string.charAt(counter)))
                {
                if (Character.isUpperCase(string.charAt(counter)))
                    upperCaseLetters++;
                else
                    lowerCaseLetters++;
                }
            }
        if (firstLetter == UNKNOWN_CASE) return UNKNOWN_CASE; // There are no letters
        else if (firstLetter == UPPER_CASE)
            {
            if (upperCaseLetters == 0) return FIRST_UPPER_CASE;
            if (lowerCaseLetters == 0) return UPPER_CASE;
            }
        else // firstLetter == LOWER_CASE
            {
            if (upperCaseLetters == 0) return LOWER_CASE;
            }
        return MIXED_CASE;
        }
    }

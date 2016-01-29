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
    }

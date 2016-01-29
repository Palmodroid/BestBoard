package org.lattilad.bestboard.server;

import java.util.ArrayList;
import java.util.List;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;

/**
 * Class to store text before cursor.
 */
public class TextBeforeCursor
    {
    /**
     * PartialString stores a String, but its length can be decreased.
     * (Chars could be deleted from the end of the string)
     */
    private class PartialString
        {
        int length;
        String string;

        PartialString( String string )
            {
            this.string = string;
            this.length = string.length();
            }
        }

    /** Last element is cleared above this limit */
    public final static int LENGTH_LIMIT = 20;

    /** Connection to synchronize text directly from editor */
    private Connection connection;


    /** ArrayList stores parts of the text before the cursor */
    private List<PartialString> text = new ArrayList<>();

    /** Summarised length of stored text before the cursor (sum of text.length-s */
    private int textLength = 0;

    /** Ready if stored text is synchronized (even if shorter then LENGTH_LIMIT) */
    private boolean textReady = false;

    /** Item counter for PreTextReader */
    private int textItemCounter = 0;

    /** Character counter for PreTextReader */
    private int textCharCounter = -1;


    /**
     * Constructor stores connection
     * @param connection connection to synchronize text with editor
     */
    public TextBeforeCursor(Connection connection)
        {
        this.connection = connection;
        }


    public void sendString( String string )
        {
        Scribe.debug( Debug.TEXT,
                "TEXT: String to add: " + string );

        text.add( new PartialString( string ) );
        textLength += string.length();

        // while length without the last element is bigger than limit,
        // then last element could be deleted
        while ( textLength - text.get( 0 ).length > LENGTH_LIMIT )
            {
            textLength -= text.get( 0 ).length;
            text.remove( 0 );
            }

        Scribe.debug( Debug.TEXT, "TEXT: Stored text after string added: " + toString() );
        }


    public void sendDelete( int length )
        {
        Scribe.debug( Debug.TEXT, "TEXT: Length to delete before cursor: " + length );

        // only delete can shrink stored text
        // if stored text is already shorter than limit, then it remains valid after delete
        // if cache is full but become shorter than limit after delete,
        // then there could be more characters in the valid text than in the cache.
        if ( textLength >= LENGTH_LIMIT && textLength - length < LENGTH_LIMIT )
            textReady = false;

        int counter = text.size();

        while (counter > 0) // actually this cannot be false
            {
            counter--;

            if ( text.get(counter).length > length)
                {
                text.get(counter).length -= length;
                textLength -= length;
                break;
                }

            length -= text.get(counter).length;
            textLength -= text.get(counter).length;
            text.remove( counter );
            }

        Scribe.debug( Debug.TEXT, "TEXT: Stored text after delete: " + toString() );
        }


    /**
     * If text is no longer identical with stored text (eg. cursor position changed),
     * then stored text should be cleared.
     * Reader is no longer valid, it will be reset, too.
     */
    public void invalidate()
        {
        text.clear();
        textLength = 0;
        textReady = false;

        rewind();

        Scribe.debug( Debug.TEXT, "TEXT: Stored text before cursor is invalidated!" );
        }


    /**
     * Synchronize text before the cursor.
     * text structure changes:
     * there will be only one string with LENGTH_LIMIT length.
     * (Only text right before the cursor could be read.)
     * Its length will be shorter, if there are not enough characters.
     */
    private void synchronize()
        {
        text.clear();

        CharSequence temp = connection.getTextBeforeCursor( LENGTH_LIMIT );
        if ( temp == null )
            {
            textLength = 0;
            }
        else
            {
            textLength = temp.length();
            text.add( new PartialString( temp.toString() ) );
            }
        textReady = true;

        Scribe.debug( Debug.TEXT, "TEXT: Stored text before cursor synchronized: " + toString());
        }


    /**
     * Same as rewind, but if heavy checking is enabled, text will be invalidated
     */
    public void reset()
        {
        if ( connection.isStoreTextEnabled() )
            {
            rewind();
            }
        else
            {
            invalidate();
            }
        }


    /**
     * Stored text could be read like a reader.
     * Before using this reader rewind() should be called.
     * After that each character will be read by read(), starting with the last character.
     * If there are no more characters available, -1 is returned.
     * bookPreTextString() and synchronize() will not change the position,
     * but puffer could become empty.
     * preTextReader gives indeterminate results after preTextDelete().
     */
    public void rewind()
        {
        // Counter is set AFTER the last character
        textItemCounter = text.size();
        textCharCounter = -1;
        }


    /**
     * Reads the previous character from stored text.
     * Counter should be reset before the cycle.
     * If no character is available, then -1 is returned.
     * Text will be synchronized automatically
     * @return Previous character, or -1 if no character is available
     */
    public int read()
        {
        // First step: decrease counter to the previous character
        while (true) // !! --textCharCounter >= 0
            {
            textCharCounter--;
            if ( textCharCounter >= 0 )
                {
                // Counter is on a valid character
                break;
                }

            textItemCounter--;
            if ( textItemCounter >= 0 )
                {
                textCharCounter = text.get( textItemCounter ).length;
                }
            // no more stored partial strings could be found...
            else
                {
                if ( textLength < LENGTH_LIMIT && !textReady )
                    {
                    // ...synchronization is needed for more characters

                    // Now the structure of text will change.
                    // textLength is equal with the already checked length,
                    // and this will be the character we are looking for.

                    int checkedLength = textLength; // This char is needed from backwards!

                    synchronize();

                    // Because of the structural change there is one (or zero) items,
                    // Character counter is set after the needed character in this string.
                    // Counter decrease will happen in the next cycle.
                    // Synchronized string could be shorter than needed,
                    // but it will be checked, and -1 returned during the next cycle
                    textItemCounter = 0;
                    textCharCounter = textLength - checkedLength;
                    }
                else
                    {
                    // ...but whole text was read, or LENGTH_LIMIT was exceeded
                    return -1;
                    }
                }
            }

        return text.get( textItemCounter ).string.charAt( textCharCounter );
        }

    /**
     * Compares the end of the stored text with string.
     * @param string to compare
     * @return true, if stored string ends with string
     */
    public boolean compare( String string )
        {
        reset();

        int n = string.length();

        while ( n > 0 )
            {
            if ( string.charAt(--n) != read() )
                return false;
            }

        return true;
        }

    /**
     * Inner data in text for debugging.
     * @return data formatted as string
     */
    @Override
    public String toString()
        {
        StringBuilder builder = new StringBuilder();
        builder.append( '|' );
        for ( PartialString partialString : text )
            {
            builder.append( partialString.string.substring( 0, partialString.length ) );
            builder.append( '|' );
            }
        builder.append( " (" ).append( textLength ).append( ") " );

        return builder.toString();
        }
    }

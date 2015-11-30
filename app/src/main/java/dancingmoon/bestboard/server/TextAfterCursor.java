package dancingmoon.bestboard.server;

import dancingmoon.bestboard.Debug;
import dancingmoon.bestboard.scribe.Scribe;

/**
 * Class to store text after cursor.
 */
public class TextAfterCursor
    {
    /** Last element is cleared above this limit */
    public final static int LENGTH_LIMIT = 20;

    /** Connection to synchronize text directly from editor */
    private Connection connection;


    /** Stored string - text after the cursor. It can be empty, but cannot be null! */
    private String text = "";

    /** First non-deleted character of the string. Can be invalid, set after the string. */
    private int textStart = 0;

    /** Ready if stored text is synchronized (even if shorter then LENGTH_LIMIT) */
    private boolean textReady = false;

    /** Reader's character counter after start (character already read). */
    private int textCounter = -1;


    /**
     * Constructor stores connection
     * @param connection connection to synchronize text with editor
     */
    public TextAfterCursor(Connection connection)
        {
        this.connection = connection;
        }


    /**
     * If text is no longer identical with stored text (eg. cursor position changed),
     * then stored text should be cleared.
     * Reader is no longer valid, it will be reset, too.
     */
    public void invalidate()
        {
        text = "";
        textStart = 0;
        textReady = false;
        reset();

        Scribe.debug( Debug.TEXT, "PostText and PostTextReader are invalidated!");
        }


    /**
     * Synchronize text after the cursor.
     * Text after the cursor cannot be typed, it can be only synchronized.
     */
    private void synchronize()
        {
        CharSequence temp = connection.getTextAfterCursor( LENGTH_LIMIT );
        if ( temp == null )
            {
            text = "";
            }
        else
            {
            text = temp.toString();
            }
        textStart = 0;
        textReady = true;

        Scribe.debug( Debug.TEXT, "PostText synchronized: " + toString());
        }


    /**
     * Stored text could be read like a reader.
     * Before using this reader reset() should be called.
     * After that each character will be read by read(), starting with the first character.
     * If there are no more characters available, -1 is returned.
     * synchronize() will not change the position,
     * but puffer could become empty.
     */
    public void reset()
        {
        textCounter = -1;
        }


    /**
     * Reads the next character from stored text.
     * Counter should be reset before the cycle.
     * If no character is available, then -1 is returned.
     * Text will be synchronized automatically
     * @return Next character, or -1 if no character is available
     */
    public int read()
        {
        // First step: increase counter to the next character
        textCounter++;

        // If character is AFTER the string end...
        if ( textStart + textCounter >= text.length() )
            {
            // If string is not completely synchronized
            if ( !textReady )
                {
                textCounter -= textStart;
                synchronize();
                // Character is still non available
                if ( textCounter >= text.length() ) // start == 0
                    {
                    return -1;
                    }
                }
            // There are no more characters to synchronize
            else
                {
                return -1;
                }
            }

        return text.charAt( textStart + textCounter );
        }


    /**
     * Delete length characters from the beginning of the stored text.
     * Whole string could be deleted and invalidated.
     * @param length number of characters to delete from the beginning
     */
    public void delete( int length )
        {
        textStart += length;
        if ( text.length() >= LENGTH_LIMIT )
            textReady = false;

        Scribe.debug( Debug.TEXT, "text deleted: " + toString());
        }


    /**
     * Inner data in text for debugging.
     * @return data formatted as string
     */
    @Override
    public String toString()
        {
        if ( textStart < text.length() )
            return text.substring( textStart, text.length() );
        else
            return "";
        }

    }
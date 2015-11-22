package dancingmoon.bestboard;

import java.util.ArrayList;
import java.util.List;

import dancingmoon.bestboard.scribe.Scribe;

/**
 * Class to store text around cursor.
 * !! Stored text should be validated by cursor movement !!
 */
public class StoredText
    {
    /**
     * Connection synchronizes text with stored text.
     * Normally these methods need an InputConnection to load text.
     */
    public interface Connection
        {
        // CharSequence getSelectedText();
        CharSequence getTextAfterCursor(int n);
        CharSequence getTextBeforeCursor(int n);
        }


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
    private List<PartialString> preText = new ArrayList<>();

    /** Summarised length of stored text before the cursor (sum of preText.length-s */
    private int preTextLength = 0;

    /** Ready if stored text is synchronized (even if shorter then LENGTH_LIMIT) */
    private boolean preTextReady = false;

    /** Item counter for PreTextReader */
    private int preItemCounter = 0;

    /** Character counter for PreTextReader */
    private int preCharCounter = -1;


    /** Stored string - text after the cursor. It can be empty, but cannot be null! */
    private String postText = "";

    /** First non-deleted character of the string. Can be invalid, set after the string. */
    private int postTextStart = 0;

    /** Ready if stored text is synchronized (even if shorter then LENGTH_LIMIT) */
    private boolean postTextReady = false;

    /** Reader's character counter after start (character already read). */
    private int postCharCounter = -1;


    /**
     * Constructor stores connection
     * @param connection connection to synchronize text with editor
     */
    public StoredText(Connection connection)
        {
        this.connection = connection;
        }


    /**
     * Invalidate whole stored text
     */
    public void invalidate()
        {
        preTextInvalidate();
        postTextInvalidate();
        }


    /**
     * If text is no longer identical with stored text (eg. cursor position changed),
     * then stored text should be cleared.
     * Reader is no longer valid, it will be reset, too.
     */
    public void preTextInvalidate()
        {
        preText.clear();
        preTextLength = 0;
        preTextReady = false;

        preTextReaderReset();

        Scribe.debug( Debug.TEXT, "PreText and preTextReader are invalidated!");
        }


    /**
     * Add typed string at the end of preText.
     * If preText (without last element) exceeds LIMIT_LENGTH, last element will be deleted.
     * @param string Typed string CANNOT BE NULL!
     */
    public void preTextType(String string)
        {
        preText.add( new PartialString( string ) );
        preTextLength += string.length();

        // while length without the last element is bigger than limit,
        // then last element could be deleted
        while ( preTextLength - preText.get(0).length > LENGTH_LIMIT )
            {
            preTextLength -= preText.get(0).length;
            preText.remove( 0 );
            }

        Scribe.debug( Debug.TEXT, "PreText typed: " + toString());
        }


    /**
     * Synchronize text before the cursor.
     * preText stucture changes:
     * there will be only one string with LENGTH_LIMIT length.
     * (Only text right before the cursor could be read.)
     * Its length will be shorter, if there are not enough characters.
     */
    private void preTextSynchronize()
        {
        preText.clear();

        CharSequence temp = connection.getTextBeforeCursor( LENGTH_LIMIT );
        if ( temp == null )
            {
            preTextLength = 0;
            }
        else
            {
            preTextLength = temp.length();
            preText.add(new PartialString(temp.toString()));
            }
        preTextReady = true;

        Scribe.debug( Debug.TEXT, "PreText synchronized: " + toString());
        }


    /**
     * Stored text could be read like a reader.
     * Before using this reader preTextReaderReset() should be called.
     * After that each character will be read by preTextRead(), starting with the last character.
     * If there are no more characters available, -1 is returned.
     * preTextType() and preTextSynchronize() will not change the position,
     * but puffer could become empty.
     * preTextReader gives indeterminate results after preTextDelete().
     */
    public void preTextReaderReset()
        {
        // Counter is set AFTER the last character
        preItemCounter = preText.size();
        preCharCounter = -1;
        }


    /**
     * Reads the previous character from stored text.
     * Counter should be reset before the cycle.
     * If no character is available, then -1 is returned.
     * Text will be synchronized automatically
     * @return Previous character, or -1 if no character is available
     */
    public int preTextRead()
        {
        // First step: decrease counter to the previous character
        while (true) // !! --preCharCounter >= 0
            {
            preCharCounter --;
            if ( preCharCounter >= 0 )
                {
                // Counter is on a valid character
                break;
                }

            preItemCounter --;
            if ( preItemCounter >= 0 )
                {
                preCharCounter = preText.get(preItemCounter).length;
                }
            // no more stored partial strings could be found...
            else
                {
                if ( preTextLength < LENGTH_LIMIT && !preTextReady )
                    {
                    // ...synchronization is needed for more characters

                    // Now the structure of preText will change.
                    // preTextLength is equal with the already checked length,
                    // and this will be the character we are looking for.

                    int checkedLength = preTextLength; // This char is needed from backwards!

                    preTextSynchronize();

                    // Because of the structural change there is one (or zero) items,
                    // Character counter is set after the needed character in this string.
                    // Counter decrease will happen in the next cycle.
                    // Synchronized string could be shorter than needed,
                    // but it will be checked, and -1 returned during the next cycle
                    preItemCounter = 0;
                    preCharCounter = preTextLength - checkedLength;
                    }
                else
                    {
                    // ...but whole text was read, or LENGTH_LIMIT was exceeded
                    return -1;
                    }
                }
            }

        return preText.get( preItemCounter ).string.charAt( preCharCounter );
        }


    /**
     * Delete length characters from the end of the stored text.
     * (If length is longer then the stored text's length,
     * then the wzole text will be deleted.)
     * @param length number of characters to delete from the end
     */
    public void preTextDelete(int length)
        {
        // only delete can shrink stored text
        // if stored text is already shorter than limit, then it remains valid after delete
        // if cache is full but become shorter than limit after delete,
        // then there could be more characters in the valid text than in the cache.
        if ( preTextLength >= LENGTH_LIMIT && preTextLength - length < LENGTH_LIMIT )
            preTextReady = false;

        int counter = preText.size();

        while (counter > 0) // actually this cannot be false
            {
            counter--;

            if (preText.get(counter).length > length)
                {
                preText.get(counter).length -= length;
                preTextLength -= length;
                break;
                }

            length -= preText.get(counter).length;
            preTextLength -= preText.get(counter).length;
            preText.remove(counter);
            }

        Scribe.debug( Debug.TEXT, "preText deleted: " + toString());
        }



    /**
     * If text is no longer identical with stored text (eg. cursor position changed),
     * then stored text should be cleared.
     * Reader is no longer valid, it will be reset, too.
     */
    public void postTextInvalidate()
        {
        postText = "";
        postTextStart = 0;
        postTextReady = false;
        postTextReaderReset();

        Scribe.debug( Debug.TEXT, "PostText and PostTextReader are invalidated!");
        }


    /**
     * Synchronize text after the cursor.
     * Text after the cursor cannot be typed, it can be only synchronized.
     */
    private void postTextSynchronize()
        {
        CharSequence temp = connection.getTextAfterCursor( LENGTH_LIMIT );
        if ( temp == null )
            {
            postText = "";
            }
        else
            {
            postText = temp.toString();
            }
        postTextStart = 0;
        postTextReady = true;

        Scribe.debug( Debug.TEXT, "PostText synchronized: " + toString());
        }


    /**
     * Stored text could be read like a reader.
     * Before using this reader postTextReaderReset() should be called.
     * After that each character will be read by postTextRead(), starting with the first character.
     * If there are no more characters available, -1 is returned.
     * postTextSynchronize() will not change the position,
     * but puffer could become empty.
     */
    public void postTextReaderReset()
        {
        postCharCounter = -1;
        }


    /**
     * Reads the next character from stored text.
     * Counter should be reset before the cycle.
     * If no character is available, then -1 is returned.
     * Text will be synchronized automatically
     * @return Next character, or -1 if no character is available
     */
    public int postTextRead()
        {
        // First step: increase counter to the next character
        postCharCounter++;

        // If character is AFTER the string end...
        if ( postTextStart + postCharCounter >= postText.length() )
            {
            // If string is not completely synchronized
            if ( !postTextReady )
                {
                postCharCounter -= postTextStart;
                postTextSynchronize();
                // Character is still non available
                if ( postCharCounter >= postText.length() ) // start == 0
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

        return postText.charAt( postTextStart + postCharCounter );
        }


    /**
     * Delete length characters from the beginning of the stored text.
     * Whole string could be deleted and invalidated.
     * @param length number of characters to delete from the beginning
     */
    public void postTextDelete(int length)
        {
        postTextStart += length;
        if ( postText.length() >= LENGTH_LIMIT )
            postTextReady = false;

        Scribe.debug( Debug.TEXT, "postText deleted: " + toString());
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
        for ( PartialString partialString : preText)
            {
            builder.append( partialString.string.substring( 0, partialString.length ) );
            builder.append( '|' );
            }
        builder.append( " (" ).append(preTextLength).append( ") " );

        builder.append("|");
        if ( postTextStart < postText.length() )
            builder.append(postText.substring( postTextStart, postText.length() ));
        builder.append("|");

        return builder.toString();
        }
    }

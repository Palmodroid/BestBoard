package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.SoftBoardListener;
import org.lattilad.bestboard.utils.SimpleReader;
import org.lattilad.bestboard.utils.StringReverseReader;

public abstract class Entry implements Comparable<Entry>
    {
    private String code;


    public Entry( String code )
        {
        this.code = code;
        }

    public String getCode()
        {
        return code;
        }

    public abstract void activate(SoftBoardListener processor);

    // Should be similar to compare, but it doesn't stop after ending
    @Override
    public int compareTo(Entry another)
        {
        StringReverseReader thisString = new StringReverseReader( this.code );
        StringReverseReader anotherString = new StringReverseReader( another.getCode() );
        int thisChar;
        int anotherChar;

        while ( (thisChar = thisString.read()) == (anotherChar = anotherString.read()) )
            {
            if ( thisChar == -1 ) // && == anotherChar; complete eq
                return 0;
            }

        // if ( thisChar == -1 ) // && != anotherChar; ending eq
        //     return 0;

        return anotherChar-thisChar; // non eq
        }

    /**
     * This method should use similar algorithm with ShortCutEntry.compareTo, (that is why it can be found here)
     * but this one is used for entry lookup
     */
    static public int compare(SimpleReader text, SimpleReader ending )
        {
        int textChar;
        int endingChar;

        while ( (textChar = text.read()) == (endingChar = ending.read()) )
            {
            if ( endingChar == -1 ) // && == textChar; complete eq
                return 0;
            }

        if ( endingChar == -1 ) // && != textChar; ending eq
            return 0;

        return endingChar-textChar; // non eq
        }

    }

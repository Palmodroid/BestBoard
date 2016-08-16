package org.lattilad.bestboard.abbreviation;

import org.lattilad.bestboard.utils.SimpleReader;
import org.lattilad.bestboard.utils.StringReverseReader;

public class Entry implements Comparable<Entry>
    {
    public Entry( String ending, String expanded )
        {
        this.ending = ending;
        this.expanded = expanded;
        }

    public String ending;
    public String expanded;

    // Should be similar to compare, but it doesn't stop after ending
    @Override
    public int compareTo(Entry another)
        {
        StringReverseReader thisString = new StringReverseReader( this.ending );
        StringReverseReader anotherString = new StringReverseReader( another.ending );
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
     * This method should be similar to Entry.compareTo, (that is why it can be found here)
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

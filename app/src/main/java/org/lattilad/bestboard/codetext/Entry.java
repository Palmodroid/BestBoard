package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.SoftBoardListener;
import org.lattilad.bestboard.utils.SimpleReader;
import org.lattilad.bestboard.utils.StringReverseReader;

public abstract class Entry implements Comparable<Entry>
    {
    private String code;


    public Entry(String code)
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
        StringReverseReader thisString = new StringReverseReader(this.code);
        StringReverseReader anotherString = new StringReverseReader(another.getCode());
        int thisChar;
        int anotherChar;

        while ((thisChar = thisString.read()) == (anotherChar = anotherString.read()))
            {
            if (thisChar == -1) // && == anotherChar; complete eq
                return 0;
            }

        // if ( thisChar == -1 ) // && != anotherChar; ending eq
        //     return 0;

        return thisChar - anotherChar; // non eq
        }

    /**
     * This method should use similar algorithm with ShortCutEntry.compareTo, (that is why it can be found here)
     * but this one is used for entry lookup
     */
    static public int compare(SimpleReader text, SimpleReader ending)
        {
        return compare(text, ending, -1);
        }

    /*
     Kérdés: text szöveg vége megegyezik-e ending-gel? Text szöveg max hossza: maxLength
     - - text kisebb, mint ending
     0 - text (legfeljebb maxLength hossz) egyezik ending-gel
     + - text nagyobb, mint ending
     abszolút érték: egyező karakterek száma + 1
     */
    static public int compare(SimpleReader text, SimpleReader ending, int maxLength)
        {
        int identicalLength = 1;

        int textChar;
        int endingChar;

        while (true)
            {
            if ( maxLength == 0 )
                {
                textChar = -1;
                }
            else
                {
                textChar = text.read();
                maxLength--;
                }

            endingChar = ending.read();

            // text ends with ending
            if (endingChar == -1)
                return 0;

            // + text is higher
            // - text is lower
            if (textChar != endingChar)
                return textChar > endingChar ? identicalLength : -identicalLength;

            identicalLength++;
            }
        }

    }

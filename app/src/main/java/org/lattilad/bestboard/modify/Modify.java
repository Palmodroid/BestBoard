package org.lattilad.bestboard.modify;

import org.lattilad.bestboard.SoftBoardData;

public abstract class Modify
    {
    /**
     * Ignores spaces between cursor and character if true
     */
    protected boolean ignoreSpace;

    /**
     * SoftBoardData.SoftBoardListener is needed to communicate with Service's StoredText class.
     */
    protected SoftBoardData softBoardData;

    /**
     * Constructor gets the communication channel with Service and StoredText, as SoftBoardListener
     * @param softBoardData contains listener to communicate with the service
     * @param ignoreSpace spaces are ignored between the text and the cursor
     */
    protected Modify( SoftBoardData softBoardData, boolean ignoreSpace )
        {
        this.softBoardData = softBoardData;
        this.ignoreSpace = ignoreSpace;
        }

    /**
     * This method just helps to change the text before the spaces standing before the cursor
     * @param reverse Direction of the change
     * @return true if change was made, false otherwise
     */
    public boolean change( boolean reverse )
        {
        int spaces=0;
        boolean changed;

        if ( ignoreSpace )
            {
            spaces = softBoardData.softBoardListener.deleteSpacesBeforeCursor();
            }

        if ( reverse )
            changed = changeBack();
        else
            changed = change();

        if ( spaces >0 )
            {
            // http://stackoverflow.com/a/2807731
            softBoardData.softBoardListener.sendString(new String(new char[spaces]).replace('\0', ' '), 0 );
            }

        return changed;
        }


    protected abstract boolean change();
    protected abstract boolean changeBack();
    }

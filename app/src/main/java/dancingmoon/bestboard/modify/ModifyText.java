package dancingmoon.bestboard.modify;


import java.util.ArrayList;
import java.util.List;

import dancingmoon.bestboard.SoftBoardData;

/**
 * Changes the string before the cursor as prescribed by the strings.
 * String-list cannot be empty, but SoftBoardData.addModify will check this.
 */
public class ModifyText extends Modify
    {
    /**
     * Texts are stored in a two-dimensional, nested array-list.
     * Modifications are cycle through the strings of one list element of the outer array-list.
     * Modification always happens inside one inner list element!
     */
    private List<List<String>> rolls = new ArrayList<>();

    /** counter to iterate through the string-rolls (outer list) */
    private int rollCounter = 0;

    /** counter to iterate through the strings inside one roll (inner list) */
    private int stringCounter = 0;


    /**
     * Constructor gets the communication channel with Service and StoredText, as SoftBoardListener
     * @param softBoardListener listener to communicate with the service
     * @param ignoreSpace spaces are ignored between the text and the cursor
     */
    public ModifyText( SoftBoardData.SoftBoardListener softBoardListener, boolean ignoreSpace )
        {
        super( softBoardListener, ignoreSpace);
        }


    /**
     * Adds a new string-roll
     * Strings of the roll are checked, and roll is only added
     * when it contains more then one non-null and non-empty string.
     * @param rollObjects String objects for one roll element CANNOT BE NULL!
     * @return true if new roll element was valid and was added
     */
    public boolean addStringRoll( List<Object> rollObjects )
        {
        List<String> tempList = new ArrayList<>();

        for ( Object rollObject : rollObjects )
            {
            if ( rollObject != null && ( (String) rollObject ).length() > 0 )
                {
                tempList.add( (String) rollObject );
                }
            }

        if ( tempList.size() > 1 )
            {
            rolls.add( tempList );
            return true;
            }

        return false;
        }


    /**
     * Compares string with the preText puffer - starting from the end
     * @param string to compare with CANNOT BE NULL
     * @return true if puffer-end is equal with the string
     */
    private boolean comparePreText( String string )
        {
        // preTextReader should start at the cursor
        softBoardListener.getTextBeforeCursor().reset();

        // String should start at the end
        // String cannot be null and cannot be shorter than 1
        // see addStringRoll()
        int counter = string.length();

        while ( counter > 0 )
            {
            counter--;

            if ( string.charAt( counter ) != softBoardListener.getTextBeforeCursor().read() )
                return false;
            }

        // all characters are equal
        return true;
        }


    /**
     * Change will look for the text before the cursor.
     * If this text can be found in the rolls
     * then it will change to the next string inside the same roll.
     * The method will eventually perform the change through the SoftBoardListener.
     * If ignoreSpace is true, then spaces before the cursor are deleted,
     * and are restored after the operation.
     */
    public void change( )
        {
        int spaces=0;

        if ( ignoreSpace )
            {
            spaces = softBoardListener.deleteSpacesBeforeCursor();
            }

        int rollCounter = this.rollCounter;
            do	{
            int stringCounter = this.stringCounter;

                do 	{
                    String inspected = rolls.get( rollCounter ).get( stringCounter++ );

                    if ( stringCounter == rolls.get( rollCounter ).size() )
                        stringCounter = 0;

                    if ( comparePreText( inspected ) )
                        {
                        // Text was found, next search will start from this string
                        this.rollCounter = rollCounter;
                        this.stringCounter = stringCounter;

                        softBoardListener.changeStringBeforeCursor(inspected.length(), rolls.get(rollCounter).get(stringCounter));

                        if ( spaces >0 )
                            {
                            // http://stackoverflow.com/a/2807731
                            softBoardListener.sendString(new String(new char[spaces]).replace('\0', ' '), 0 );
                            }

                        return;
                        }
                    } while ( stringCounter != this.stringCounter );

                this.stringCounter = 0;

                rollCounter++;

                if ( rollCounter == rolls.size() )
                    rollCounter = 0;

            } while ( rollCounter != this.rollCounter );

        this.rollCounter = 0;
        // Text was not found, no change in text
        }


    /**
     * Change will look for the text before the cursor.
     * If this text can be found in the rolls
     * then it will change to the previous string inside the same roll.
     * The method will eventually perform the change through the SoftBoardListener.
     * If ignoreSpace is true, then spaces before the cursor are deleted,
     * and are restored after the operation.
     */
    public void changeBack( )
        {
        int spaces=0;

        if ( ignoreSpace )
            {
            spaces = softBoardListener.deleteSpacesBeforeCursor();
            }

        int rollCounter = this.rollCounter;
        do	{
            int stringCounter = this.stringCounter;

            do 	{
                String inspected = rolls.get( rollCounter ).get( stringCounter );

                if ( stringCounter == 0 )
                    stringCounter = rolls.get( rollCounter ).size();
                stringCounter--;

                if ( comparePreText( inspected ) )
                    {
                    // Text was found, next search will start from this string
                    this.rollCounter = rollCounter;
                    this.stringCounter = stringCounter;

                    softBoardListener.changeStringBeforeCursor( inspected.length(),
                            rolls.get( rollCounter ).get( stringCounter ) );

                    if ( spaces >0 )
                        {
                        // http://stackoverflow.com/a/2807731
                        softBoardListener.sendString(new String(new char[spaces]).replace('\0', ' '), 0 );
                        }

                    return;
                    }
                } while ( stringCounter != this.stringCounter );

            this.stringCounter = 0;

            if ( rollCounter == 0 )
                rollCounter = rolls.size();
            rollCounter--;

            } while ( rollCounter != this.rollCounter );

        this.rollCounter = 0;
        // Text was not found, no change in text
        }

    }

package dancingmoon.bestboard.modify;

import java.util.ArrayList;
import java.util.List;

import dancingmoon.bestboard.SoftBoardData;


/**
 * Changes the character before the cursor as prescribed by the rolls.
 * String-list cannot be empty, but SoftBoardData.addModify will check this.
 */
public class ModifyChar extends Modify
    {
    /**
     * Character modifications are cycle through one string of characters.
     * These character-rolls are stored in an Array-List.
     * Modification always happens inside one string!
     */
    private List<String> rolls = new ArrayList<String>();

    /** counter to iterate through the rolls */
    private int rollCounter = 0;

    /** counter to iterate through the characters of the current string */
    private int charCounter = 0;


    /**
     * Constructor gets the communication channel with Service and StoredText, as SoftBoardListener
     * @param softBoardListener listener to communicate with the service
     * @param ignoreSpace spaces are ignored between the text and the cursor
     */
    public ModifyChar( SoftBoardData.SoftBoardListener softBoardListener, boolean ignoreSpace )
        {
        super( softBoardListener, ignoreSpace);
        }


    /**
     * Adds a new character-string
     * @param string the string itself
     * @return true if string was valid and was added (length > 1)
     */
    public boolean addCharacterRoll( String string )
        {
        if ( string != null && string.length() > 1 )
            {
            rolls.add( string );
            return true;
            }
        return false;
        }


    /**
     * Change will look for the last character before the cursor.
     * If this character can be found in the rolls
     * then it will change to the next character inside the same roll.
     * The method will eventually perform the change through the SoftBoardListener.
     * If ignoreSpace is true, then spaces before the cursor are deleted,
     * and are restored after the operation.
     */
    public void change( )
        {
        softBoardListener.getTextBeforeCursor().reset();
        int last = softBoardListener.getTextBeforeCursor().read();

        int rollCounter = this.rollCounter;
        do	{
            int charCounter = this.charCounter;

            do 	{
                char inspected = rolls.get( rollCounter ).charAt( charCounter++ );

                if ( charCounter == rolls.get(rollCounter).length() )
                    charCounter = 0;

                if ( inspected == last )
                    {
                    // Character was found, next search will start from this character
                    this.charCounter = charCounter;
                    this.rollCounter = rollCounter;

                    softBoardListener.changeStringBeforeCursor(
                            String.valueOf( rolls.get( rollCounter ).charAt( charCounter ) ));

                    return;
                    }
                } while ( charCounter != this.charCounter );

            this.charCounter = 0;

            rollCounter++;

            if ( rollCounter == rolls.size() )
                rollCounter = 0;

            } while ( rollCounter != this.rollCounter );

        this.rollCounter = 0;
        // Character was not found, no change in text
        }
        
        
    /**
     * Change will look for the last character before the cursor.
     * If this character can be found in the rolls
     * then it will change to the previous character inside the same roll.
     * The method will eventually perform the change through the SoftBoardListener.
     * If ignoreSpace is true, then spaces before the cursor are deleted,
     * and are restored after the operation.
     */
    public void changeBack( )
        {
        softBoardListener.getTextBeforeCursor().reset();
        int last = softBoardListener.getTextBeforeCursor().read();

        int stringCounter = this.rollCounter;
        do  {
            int charCounter = this.charCounter;

            do  {
                char inspected = rolls.get( stringCounter ).charAt( charCounter );

                if ( charCounter == 0 )
                    charCounter = rolls.get(stringCounter).length();
                charCounter--;

                if ( inspected == last )
                    {
                    // Character was found, next search will start from this character
                    this.charCounter = charCounter;
                    this.rollCounter = stringCounter;

                    softBoardListener.changeStringBeforeCursor(
                        String.valueOf( rolls.get( stringCounter ).charAt( charCounter ) ));

                    return;
                    }
                } while ( charCounter != this.charCounter );

            this.charCounter = 0;

            if ( stringCounter == 0 )
                stringCounter = rolls.size();
            stringCounter--;
                
            } while ( stringCounter != this.rollCounter );

        this.rollCounter = 0;
        // Character was not found, no change in text
        }
    
    }

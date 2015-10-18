package dancingmoon.bestboard.buttons;

import dancingmoon.bestboard.Commands;
import dancingmoon.bestboard.SoftBoardData;
import dancingmoon.bestboard.utils.ExternalDataException;

/**
 * Editor functions performed by the keyboard
 * These functions have no parameters, functions can communicate to the editor
 */
public class PacketFunction extends Packet
    {
    /**
     * Code of the function - keycode of the function's token
     */
    private long functionCode;

    /**
     * Constructor of button inner function
     * @param softBoardData general keyboard data class
     * @param functionCode token code of the function
     * @throws ExternalDataException if function code is not valid
     */
    public PacketFunction( SoftBoardData softBoardData, long functionCode ) throws ExternalDataException
        {
        super( softBoardData );
        this.functionCode = functionCode;

        if ( functionCode == Commands.TOKEN_DELETE )
            return;

        if ( functionCode == Commands.TOKEN_BACKSPACE )
            return;

        // !!!!!!! JUST DRAFT DO NOT USE IT !!!!!!!
        if ( functionCode == Commands.TOKEN_DRAFT )
            return;

        // functionCode is not known
        throw new ExternalDataException();
        }


    /**
     * String representation of the data (title)
     * @return String representation of this packet
     */
    @Override
    public String getString()
        {
        if ( functionCode == Commands.TOKEN_DELETE )
            return "DEL";

        if ( functionCode == Commands.TOKEN_BACKSPACE )
            return "BS";

        // !!!!!!! JUST DRAFT DO NOT USE IT !!!!!!!
        if ( functionCode == Commands.TOKEN_DRAFT )
            return "DRAFT";

        // this cannot be reached
        return "";
        }


    /**
     * Send data to the editor field
     */
    @Override
    public void send()
        {
        if ( functionCode == Commands.TOKEN_DELETE )
            {
            softBoardData.softBoardListener.deleteTextAfterCursor( 1 );
            }

        else if ( functionCode == Commands.TOKEN_BACKSPACE )
            {
            softBoardData.softBoardListener.deleteTextBeforeCursor( 1 );
            }

        // !!!!!!! JUST DRAFT DO NOT USE IT !!!!!!!
        else if ( functionCode == Commands.TOKEN_DRAFT )
            {
            softBoardData.softBoardListener.__restart__();
            }
        }
    }

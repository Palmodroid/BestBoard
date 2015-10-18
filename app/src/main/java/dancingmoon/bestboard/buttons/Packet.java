package dancingmoon.bestboard.buttons;

import dancingmoon.bestboard.SoftBoardData;

/**
 * Packet represents the data sent by the keyboard.
 * It can be textual (String) - PacketText or Hard-key (int code) - PacketKey
 */
public abstract class Packet
    {
    /** SoftBoardData needed for general keyboard data and for communication */
    protected SoftBoardData softBoardData;

    protected Packet( SoftBoardData softBoardData )
        {
        this.softBoardData = softBoardData;
        }

    /**
     * String representation of the data (title)
     * @return String representation of this packet
     */
    public abstract String getString();

    /**
     * Send data to the editor field
     */
    public abstract void send();
    }

package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

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

    public static final int CAPITAL = 0;
    public static final int TWIN = 1;

    /**
     * Send secondary data - if undo is possible
     * !! This method is not obligatory
     */
    public void sendSecondary( int second )
    	{ 
        }

    /**
     * Finish duties, when button is left
     * Packets are clearing AUTOCAPS state
     * If need, this method can be overridden (PacketText)
     */
    public void release()
    	{
        }
    
    }

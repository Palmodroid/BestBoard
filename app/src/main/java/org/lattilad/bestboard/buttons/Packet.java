package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Packet represents the data sent by the keyboard.
 * It can be textual (String) - PacketText or Hard-key (int code) - PacketKey - or PacketFunction
 *
 * Packet should have a title-string, which can be provided by the constructor,
 * or can be set later by TITLE parameter (setTitleString).
 * send() and sendSecondary(), release() methods should be created or overridden
 */
public abstract class Packet
    {
    /** SoftBoardData needed for general keyboard data and for communication */
    protected SoftBoardData softBoardData;

    /** Title (string) representation of the packet */
    protected String titleString;

    /**
     * Constructor: be careful! Initial title-string should be set by constructor
     * @param softBoardData
     */
    protected Packet( SoftBoardData softBoardData )
        {
        this.softBoardData = softBoardData;
        }

    /**
     * Constructor sets an initial title-string, which can be changed later (by TITLE)
     * @param softBoardData
     * @param titleString
     */
    protected Packet( SoftBoardData softBoardData, String titleString )
        {
        this(softBoardData);
        setTitleString( titleString );
        }

    /**
     * TITLE parameter can change initial title-string
     * @param titleString
     */
    public void setTitleString( String titleString )
        {
        this.titleString = titleString;
        }

    /**
     * Get title-string. This can be overriden for more specialised functions
     * @return String representation of this packet
     */
    public String getTitleString()
        {
        return titleString;
        }


    /**
     * Packets normally do not need changing button,
     * but if they do, then this method should be overridden
     * @return
     */
    public boolean needsChangingButton()
        {
        return false;
        }


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

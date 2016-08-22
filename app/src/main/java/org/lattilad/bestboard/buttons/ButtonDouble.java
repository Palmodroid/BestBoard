package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Double button with double packet
 * First packet should be undo-able (Text),
 * second can be any type: Text(String) or Hard-key or function
 */
public class ButtonDouble extends ButtonMainTouch implements Cloneable
    {
    private Packet packetFirst;
    private Packet packetSecond;

    @Override
    public ButtonDouble clone()
        {
        return (ButtonDouble)super.clone();
        }

    public ButtonDouble( Packet packetFirst, Packet packetSecond )
        {
        this.packetFirst = packetFirst;
        this.packetSecond = packetSecond;
        }

    public String getFirstString()
        {
        return packetFirst.getTitleString();
        }

    public String getSecondString()
        {
        return packetSecond.getTitleString();
        }

    private static int counter = 0;


    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        packetFirst.send();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        counter = 1;
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( counter == 1 )
            packetFirst.release();
        else // counter == 2;
            packetSecond.release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( layout.softBoardData.softBoardListener.undoLastString() )
            {
            if ( counter == 1 )
                {
                packetSecond.send();
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
                counter = 2;
                }
            else // counter == 2
                {
                packetFirst.send();
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
                counter = 1;
                }
            }
        return false;
        }

    }

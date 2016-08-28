package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Double button with double packet
 * First packet should be undo-able (Text),
 * second can be any type: Text(String) or Hard-key or function
 */
public class ButtonAlternate extends ButtonMainTouch implements Cloneable
    {
    private int counter = 0;
    private Packet[] packets = new Packet[2];

    @Override
    public ButtonAlternate clone()
        {
        return (ButtonAlternate)super.clone();
        }

    public ButtonAlternate( Packet packetFirst, Packet packetSecond )
        {
        packets[0] = packetFirst;
        packets[1] = packetSecond;
        }

    @Override
    public boolean isFirstStringChanging()
        {
        return true;
        }

    @Override
    public String getFirstString()
        {
        return packets[0].getTitleString();
        }

    @Override
    public boolean isSecondStringChanging()
        {
        return true;
        }

    @Override
    public String getSecondString()
        {
        return packets[1].getTitleString();
        }

    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        packets[counter].send();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        packets[counter].release();
        counter++;
        counter&=1;
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( layout.softBoardData.softBoardListener.undoLastString() )
            {
            counter++;
            counter&=1;
            packets[counter].send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            }
        return false;
        }
    }

package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Simple button with traveller-space
 * This class doesn't use the Packet sending mechanism, it will send space directly
 */
public class ButtonSpaceTravel extends ButtonMainTouch implements Cloneable
    {
    private Packet packet;
    private Packet packetSecond;

    // 0 - nothing happened
    // 1 - SPACE packet was sent
    // 2 - secondary packet was sent - without previous SPACE
    // 3 - secondary packet was sent - after deleting already delivered space
    private int done = 0;

    @Override
    public ButtonSpaceTravel clone()
        {
        return (ButtonSpaceTravel)super.clone();
        }

    public ButtonSpaceTravel( Packet packet, Packet packetSecond )
        {
        this.packet = packet;
        this.packetSecond = packetSecond; // can be null
        }


    @Override
    public boolean isFirstStringChanging()
        {
        return packet.isTitleStringChanging();
        }

    @Override
    public String getFirstString()
        {
        return packet.getTitleString();
        }

    @Override
    public boolean isSecondStringChanging()
        {
        return packetSecond != null ? packetSecond.isTitleStringChanging() : super.isSecondStringChanging();
        }

    @Override
    public String getSecondString()
        {
        return packetSecond != null ? packetSecond.getTitleString() : super.getSecondString();
        }


    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        if ( isTouchDown )
            {
            packet.send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            done = 1;
            }
        else
            {
            done = 0;
            }
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( isTouchUp && done == 0 )
            {
            packet.send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            done = 1;
            }

        if (done == 1)
            packet.release();   // autocaps should be set
        else if (done >= 2)
            packetSecond.release();

        // done = false; // this is not needed, because bow will always start first
        }
        
    @Override
    public boolean fireSecondary(int type)
    	{
        if ( packetSecond != null )
            {
            // nothing happened OR first packet is undoable
            if (done == 0 || ( done == 1 && layout.softBoardData.softBoardListener.undoLastString() ))
                {
                packetSecond.send();
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
                done = 2;
                }

            // secondary already is sent and is undoable
            else if ( done >= 2 && layout.softBoardData.softBoardListener.undoLastString() )
                {
                if ( done == 3 ) // previous space should be resent
                    {
                    packet.send();
                    done = 1;
                    }
                else // done == 2 // no space was sent previously
                    done = 0;
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
                }
            }
        return false;
        }
    }

package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

import java.util.ArrayList;

/**
 * Double list with list of packets
 * Packets should be undo-able (Text)
 * !! List cannot remain empty !!
 */
public class ButtonMulti extends ButtonMainTouch implements Cloneable
    {
    private ArrayList<Packet> packets = new ArrayList<>();

    @Override
    public ButtonMulti clone()
        {
        return (ButtonMulti)super.clone();
        }

    public void addPacket( Packet packet )
        {
        packets.add( packet );
        }

    public String getFirstString()
        {
        if ( packets.isEmpty() )
            return "MULTI";
        else
            return packets.get(0).getString();
        }

    private static int counter = 0;


    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        counter = 0;
        packets.get(0).send();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        packets.get(counter).release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( layout.softBoardData.softBoardListener.undoLastString() )
            {
            counter++;
            if ( counter == packets.size() )
                counter = 0;

            packets.get(counter).send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            }
        return false;
        }
    }

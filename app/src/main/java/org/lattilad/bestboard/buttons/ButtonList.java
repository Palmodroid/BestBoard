package org.lattilad.bestboard.buttons;

import java.util.ArrayList;

/**
 * Double list with list of packets
 * Packets should be undo-able (Text)
 * !! List cannot remain empty !!
 */
public class ButtonList extends ButtonMainTouch implements Cloneable
    {
    private ArrayList<Packet> packets = new ArrayList<>();

    @Override
    public ButtonList clone()
        {
        return (ButtonList)super.clone();
        }

    public void AddPacket( Packet packet )
        {
        packets.add( packet );
        }

    public String getString()
        {
        if ( packets.isEmpty() )
            return "LIST";
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
            }
        return false;
        }
    }

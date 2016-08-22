package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Simple button with a single packet (Text(String) or Hard-key
 */
public class ButtonSingle extends ButtonMainTouch implements Cloneable
    {
    public static final int CAPITAL = 0;
    public static final int TWIN = 1;
    public static final int REPEAT = 2; // sets onStay

    private Packet packet;
    private int second;

    @Override
    public ButtonSingle clone()
        {
        return (ButtonSingle)super.clone();
        }

    public ButtonSingle(Packet packet, int second)
        {
        this.packet = packet;
        this.second = second;
        if ( second == REPEAT )
            setOnStay();
        }


    public ButtonDouble extendToDouble( Packet packetSecond )
        {
        ButtonDouble buttonDouble;

        buttonDouble = new ButtonDouble( packet, packetSecond );
        buttonDouble.setTitles( getTitles() );
        buttonDouble.setColor( color );
        // onCircle cannot be checked because of repeat

        return buttonDouble;
        }


    public ButtonAlternate extendToAlternate( Packet packetSecond )
        {
        ButtonAlternate buttonAlternate;

        buttonAlternate = new ButtonAlternate( packet, packetSecond );
        buttonAlternate.setTitles(getTitles());
        buttonAlternate.setColor(color);
        // onCircle cannot be checked because of repeat

        return buttonAlternate;
        }


    public ButtonMulti extendToMulti()
        {
        ButtonMulti buttonMulti;

        buttonMulti = new ButtonMulti();
        buttonMulti.addPacket( packet ); // Packet of the Single Button becomes the first element
        buttonMulti.setTitles(getTitles());
        buttonMulti.setColor(color);
        // onCircle cannot be checked because of repeat

        return buttonMulti;
        }

    public ButtonList extendToList( )
        {
        if ( packet instanceof PacketText && packet.getTitleString().length() > 0 )
            {
            ButtonList buttonList = new ButtonList((PacketText)packet, null);
            buttonList.setTitles(getTitles());
            buttonList.setColor(color);
            // onCircle cannot be checked because of repeat
            return buttonList;
            }
        return null;
        }

    public String getFirstString()
        {
        return packet.getTitleString();
        }

    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        packet.send();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        packet.release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( second == REPEAT )
            {
            packet.send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_REPETED);
            return true;
            }
        else
            {
            packet.sendSecondary( second );
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            }
        return false;
        }

    }

package org.lattilad.bestboard.buttons;

/**
 * Simple button with a single packet (Text(String) or Hard-key
 */
public class ButtonSingle extends ButtonMainTouch implements Cloneable
    {
    private Packet packet;
    private boolean repeat;

    @Override
    public ButtonSingle clone()
        {
        return (ButtonSingle)super.clone();
        }

    public ButtonSingle(Packet packet, boolean repeat)
        {
        this.packet = packet;
        this.repeat = repeat;
        if ( repeat )
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


    public ButtonList extendToList()
        {
        ButtonList buttonList;

        buttonList = new ButtonList();
        buttonList.addPacket( packet ); // Packet of the Single Button becomes the first element
        buttonList.setTitles(getTitles());
        buttonList.setColor(color);
        // onCircle cannot be checked because of repeat

        return buttonList;
        }


    public String getString()
        {
        return packet.getString();
        }

    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        packet.send();
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        packet.release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( repeat )
            {
            packet.send();
            return true;
            }
        else
            {
            packet.sendSecondary();
            }
        return false;
        }

    }

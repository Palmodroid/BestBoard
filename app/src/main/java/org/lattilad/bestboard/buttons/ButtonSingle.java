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

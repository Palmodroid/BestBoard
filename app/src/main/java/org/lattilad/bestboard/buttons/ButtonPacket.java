package org.lattilad.bestboard.buttons;

/**
 * Simple button with a single packet (Text(String) or Hard-key
 */
public class ButtonPacket extends ButtonMainTouch implements Cloneable
    {
    private Packet packet;
    private boolean repeat;

    @Override
    public ButtonPacket clone()
        {
        return (ButtonPacket)super.clone();
        }

    public ButtonPacket( Packet packet, boolean repeat )
        {
        this.packet = packet;
        this.repeat = repeat;
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
    public void mainTouchOnCircle( boolean isHardPress )
        {
        if ( !repeat )
        	{
        	packet.sendSecondary();
        	}
        }

    @Override
    public boolean mainTouchOnStay()
        {
        if ( repeat )
            {
            packet.send();
            return true;
            }
        return false;
        }

    }

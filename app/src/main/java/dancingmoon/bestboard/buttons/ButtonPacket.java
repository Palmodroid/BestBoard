package dancingmoon.bestboard.buttons;

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

    @Override
    public void mainTouchEvent(int phase)
        {
        if ( phase == MAIN_START || phase == MAIN_DOWN ||
                ( phase == MAIN_REPEAT && repeat ))
            packet.send();
        }

    }

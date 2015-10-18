package dancingmoon.bestboard.buttons;

/**
 * Simple button with traveller-space
 * This class doesn't use the Packet sending mechanism, it will send space directly
 */
public class ButtonSpaceTravel extends ButtonMainTouch implements Cloneable
    {
    private Packet packet;
    private boolean done = false;

    @Override
    public ButtonSpaceTravel clone()
        {
        return (ButtonSpaceTravel)super.clone();
        }

    public ButtonSpaceTravel( Packet packet )
        {
        this.packet = packet;
        }

    public String getString()
        {
        return packet.getString();
        }

    @Override
    public void mainTouchEvent(int phase)
        {
        if ( phase == MAIN_DOWN )
            {
            packet.send();
            done = true;
            }

        else if ( phase == MAIN_UP )
            {
            if ( done )
                done = false;
            else
                packet.send();
            }

        else if ( phase == MAIN_END )
            {
            done = false;
            }
        }

    }

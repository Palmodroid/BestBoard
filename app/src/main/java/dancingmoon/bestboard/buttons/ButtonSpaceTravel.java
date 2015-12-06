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
    public void mainTouchStart( boolean isTouchDown )
        {
        if ( isTouchDown )
            {
            packet.send();
            done = true;
            }
        else
            {
            done = false;
            }
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( isTouchUp && !done )
            {
            packet.send();
            packet.release();   // autocaps should be set
            }
        // done = false; // this is not needed, because bow will always start first
        }

    @Override
    public void mainTouchOnCircle( boolean isHardPress )
        { }

    @Override
    public boolean mainTouchOnStay()
        {
        return false;
        }

    }

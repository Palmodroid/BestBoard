package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

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
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
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
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            done = true;
            }

        if (done)
            packet.release();   // autocaps should be set

        // done = false; // this is not needed, because bow will always start first
        }

    @Override
    public boolean fireSecondary(int type)
        {
        return false;
        }
    }

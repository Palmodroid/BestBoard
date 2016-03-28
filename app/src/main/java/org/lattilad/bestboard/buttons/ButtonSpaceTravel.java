package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Simple button with traveller-space
 * This class doesn't use the Packet sending mechanism, it will send space directly
 */
public class ButtonSpaceTravel extends ButtonMainTouchTitles implements Cloneable
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
    public String getChangingString()
    	{
        return layout.softBoardData.autoEnabled ? "ON" : "OFF";
    	}

    
    @Override
    public boolean fireSecondary(int type)
    	{
    	if ( !done || layout.softBoardData.softBoardListener.undoLastString() )
        	{
            layout.softBoardData.autoEnabled = !layout.softBoardData.autoEnabled;
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
			done = false;
        	}
    	return false;
    	}
    }

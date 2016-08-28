package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * ButtonProgram is a complex button:
 * 1. Empty memory:     PROG
 * 2. Program stored:   package-name, start
 * Secondary: erase
 */
public class ButtonProgram extends ButtonMainTouch implements Cloneable
    {
    PacketRun packetRun = null;
    boolean primary = false;

    @Override
    public ButtonProgram clone()
        {
        return (ButtonProgram)super.clone();
        }

    // packet is obligatory, but can be empty
    public ButtonProgram( PacketRun packetRun )
        {
        this.packetRun = packetRun;
        }

    /**
     * This all comes from memory button, these buttons are very similar
     * Common class is needed
     */

    @Override
    public boolean isFirstStringChanging()
        {
        return true;
        }


    @Override
    public String getFirstString()
        {
        return packetRun == null ? "PROG" : packetRun.getTitleString();
        }


    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        primary = true;
        // Because run is un-undoable, run should be started only, if button was not erased
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( primary ) // This is needed only as primary function
            {
            if (packetRun != null)
                packetRun.send();
            else
                packetRun = new PacketRun(layout.softBoardData,
                        layout.softBoardData.softBoardListener.getEditorPackageName());
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        }

    @Override
    public boolean fireSecondary(int type)
        {
        primary = false;

        if ( packetRun != null )
            {
            packetRun = null;
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            }

        return false;
        }
    }

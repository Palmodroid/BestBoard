package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Monitor Row
 */
public class ButtonMonitorRow extends ButtonMainTouchInvisible implements Cloneable
    {
    private Packet packet;

    @Override
    public ButtonMonitorRow clone()
        {
        return (ButtonMonitorRow) super.clone();
        }

    public ButtonMonitorRow(Packet packet)
        {
        this.packet = packet;
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
        return false;
        }

    }

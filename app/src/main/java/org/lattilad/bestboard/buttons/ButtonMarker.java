package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Created by Beothe on 2016.04.01..
 */
public class ButtonMarker extends ButtonMainTouchTitles
    {
    public final static int AUTOMATICS = 0;

    private Packet packetFirst;
    private Packet packetSecond;
    private int marker;

    @Override
    public ButtonMarker clone()
        {
        return (ButtonMarker)super.clone();
        }

    public ButtonMarker( Packet packetFirst, Packet packetSecond, int marker )
        {
        this.packetFirst = packetFirst;
        this.packetSecond = packetSecond;
        this.marker = marker;
        }

    public String getString()
        {
        return packetFirst.getString();
        }

    private static int counter = 0;


    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        if
        packetFirst.send();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        counter = 1;
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( counter == 1 )
            packetFirst.release();
        else // counter == 2;
            packetSecond.release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( layout.softBoardData.softBoardListener.undoLastString() )
            {
            if ( counter == 1 )
                {
                packetSecond.send();
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
                counter = 2;
                }
            else // counter == 2
                {
                packetFirst.send();
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
                counter = 1;
                }
            }
        return false;
        }

    @Override
    public String getChangingString()
        {
        return layout.softBoardData.autoEnabled ? "ON" : "OFF";
        }

    }

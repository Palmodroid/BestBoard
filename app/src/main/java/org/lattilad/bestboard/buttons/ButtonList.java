package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.modify.ModifyText;

import java.util.List;


/**
 * List button with modify behavior
 * If no modification is possible, then writes the first string
 */
public class ButtonList extends ButtonMainTouch implements Cloneable
    {
    // packet's string and modifyText's first string should be the same
    private PacketTextSimple packetTextSimple;
    private Packet packetSecond;
    private ModifyText modifyText;
    private List<Object> strings;
    private boolean secondSent = false;

    private long processCounter = -1L;


    @Override
    public ButtonList clone()
        {
        return (ButtonList)super.clone();
        }

    public ButtonList(PacketTextSimple packetTextSimple, Packet packetSecond, List<Object> strings)
        {
        this.packetTextSimple = packetTextSimple;
        this.packetSecond = packetSecond;
        this.strings = strings;
        }

    public ButtonList(PacketTextSimple packetTextSimple, Packet packetSecond )
        {
        this.packetTextSimple = packetTextSimple;
        this.packetSecond = packetSecond;
        modifyText = new ModifyText(layout.softBoardData, true );
        modifyText.setFirstString( packetTextSimple.getTitleString() );
        }

    protected void connected()
        {
        modifyText = new ModifyText(layout.softBoardData, true );
        modifyText.addStringRoll( strings );
        packetTextSimple.setString( modifyText.getFirstString() );
        }

    public void extendList( String string )
        {
        modifyText.extendFirstRoll( string );
        }

    // Title of PacketTextSimple DO NOT change
    @Override
    public String getFirstString()
        {
        return packetTextSimple.getTitleString();
        }

    @Override
    public boolean isSecondStringChanging()
        {
        return packetSecond != null ? packetSecond.isTitleStringChanging() : super.isSecondStringChanging();
        }

    @Override
    public String getSecondString()
        {
        return packetSecond != null ? packetSecond.getTitleString() : super.getSecondString();
        }


    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        if ( layout.softBoardData.softBoardListener.checkProcessCounter(processCounter) )
            {
            modifyText.change( false );
            }
        else
            {
            packetTextSimple.send();
            }
        processCounter = layout.softBoardData.softBoardListener.getProcessCounter();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        // Change behaves as send - so release is always needed
        if ( secondSent )   packetSecond.release();
        else                packetTextSimple.release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( packetSecond != null && layout.softBoardData.softBoardListener.undoLastString() )
            {
            if ( secondSent )
                {
                packetTextSimple.send();
                secondSent = false;
                }
            else
                {
                packetSecond.send();
                secondSent = true;
                }
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            }
        return false;
        }

    }

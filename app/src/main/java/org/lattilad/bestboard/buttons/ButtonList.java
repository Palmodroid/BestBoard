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
    PacketText packetText;
    Packet packetSecond;
    ModifyText modifyText;
    List<Object> strings;
    boolean secondSent = false;


    @Override
    public ButtonList clone()
        {
        return (ButtonList)super.clone();
        }

    public ButtonList(PacketText packetText, Packet packetSecond, List<Object> strings)
        {
        this.packetText = packetText;
        this.packetSecond = packetSecond;
        this.strings = strings;
        }

    public ButtonList(PacketText packetText, Packet packetSecond )
        {
        this.packetText = packetText;
        this.packetSecond = packetSecond;
        modifyText = new ModifyText(layout.softBoardData, true );
        modifyText.setFirstString( packetText.getString() );
        }

    protected void connected()
        {
        modifyText = new ModifyText(layout.softBoardData, true );
        modifyText.addStringRoll( strings );
        packetText.setString( modifyText.getFirstString() );
        }

    public void extendList( String string )
        {
        modifyText.extendFirstRoll( string );
        }

    public String getFirstString()
        {
        return packetText.getString();
        }

    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        if ( !modifyText.change( false ) )
            {
            packetText.send();
            }
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        // Change behaves as send - so release is always needed
        if ( secondSent )   packetSecond.release();
        else                packetText.release();
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( packetSecond != null && layout.softBoardData.softBoardListener.undoLastString() )
            {
            if ( secondSent )
                {
                packetText.send();
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

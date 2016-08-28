package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.MetaState;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * ButtonMemory is a complex button:
 * 1. Empty memory:     MEM
 * 2. SHIFT-LOCK:       SEL
 * 3. Text stored:      text
 */
public class ButtonMemory extends ButtonMainTouch implements Cloneable
    {
    private PacketText packet;
    private boolean done = false;
    String abbreviation;
    int state;

    @Override
    public ButtonMemory clone()
        {
        return (ButtonMemory)super.clone();
        }

    // packet is obligatory, but can be empty
    public ButtonMemory( PacketText packet )
        {
        this.packet = packet;
        if ( packet.getTitleString().length() == 0 )
            {
            state = 1;
            }
        else
            {
            state = 3;
            abbreviation = StringUtils.abbreviateString( packet.getTitleString(), 5 );
            }
        }

    @Override
    public boolean isColorChanging()
        {
        return true;
        }

    @Override
    public int getColor()
        {
        return (state == 2 &&
                layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() == MetaState.META_LOCK ) ?
                layout.softBoardData.lockColor : super.getColor();
        }

    @Override
    public boolean isFirstStringChanging()
        {
        return true;
        }

    @Override
    public String getFirstString()
        {
        if ( state == 3 )
            return abbreviation;
        else if (state == 2 )
            return "SEL";
        return  "MEM";
        }


    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        if ( state == 3 )
            {
            packet.send();
            done = true;
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        else if ( state == 2 )
            {
            layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].setState(MetaState.META_OFF);
            String string = layout.softBoardData.softBoardListener.getWordOrSelected();
            if ( string.length() > 0 )
                {
                packet.setString(string);
                abbreviation = StringUtils.abbreviateString(string, 5);
                state = 3;
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
                }
            }
        else
            {
            layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].setState(MetaState.META_LOCK);
            state = 2;
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( done )
            {
            packet.release();
            done = false;
            }
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( done ) // state 3
            {
            layout.softBoardData.softBoardListener.undoLastString();
            done = false;
            }
        if ( state == 2 )
            {
            layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].setState(MetaState.META_OFF);
            }
        packet.setString( "" );
        state = 1;
        return false;
        }

    }

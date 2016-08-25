package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.MetaState;
import org.lattilad.bestboard.utils.StringUtils;

import java.util.Iterator;

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

    public boolean isChangingButton()
        {
        return true;
        }

    // This is needed only by debug methods, because last title is not drawn
    public String getFirstString()
        {
        return "MEM";
        }

    // All but last title is drawn
    protected void drawButtonTextTitles(Canvas canvas, int xOffsetInPixel, int yOffsetInPixel)
        {
        Iterator<TitleDescriptor> titlesIterator = titles.iterator();
        if (titlesIterator.hasNext())
            titlesIterator.next(); // just step over the last item
        while (titlesIterator.hasNext())
            {
            titlesIterator.next().drawTextTitle(canvas, this, xOffsetInPixel, yOffsetInPixel);
            }
        }

    // Last title is drawn as changing part
    private void drawButtonLastTitle(Canvas canvas)
        {
        // draw last title - always with changing string
        String string;

        if ( state == 3 )
            string = abbreviation;
        else if (state == 2 )
            {
            string = "SEL";
            }
        else
            string = "MEM";

        titles.getLast().drawTitle(canvas, string,
                this, layout.layoutXOffset, layout.layoutYOffset);
        }

    // public void drawButtonConstantPart(Canvas canvas) - remains original

    // Last title is drawn as changing part
    public void drawButtonChangingPart(Canvas canvas)
        {
        if (state == 2 &&
                layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() == MetaState.META_LOCK )
            {
            drawButtonBackground(canvas, layout.softBoardData.lockColor, layout.layoutXOffset, layout.layoutYOffset);
            drawButtonTextTitles(canvas, layout.layoutXOffset, layout.layoutYOffset);
            }

        drawButtonLastTitle( canvas );
        }

    public void drawButtonTouched(Canvas canvas)
        {
        // draw the background
        drawButtonBackground(canvas, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);
        // draw the titles - ONLY TEXT titles
        drawButtonTextTitles(canvas, layout.layoutXOffset, layout.layoutYOffset);

        drawButtonLastTitle(canvas);
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

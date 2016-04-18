package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.SoftBoardData;

import java.util.Iterator;

/**
 * Double button with double packet
 * First packet should be undo-able (Text),
 * second can be any type: Text(String) or Hard-key or function
 */
public class ButtonAlternate extends ButtonMainTouch implements Cloneable
    {
    private int counter = 0;
    private Packet[] packets = new Packet[2];

    @Override
    public ButtonAlternate clone()
        {
        return (ButtonAlternate)super.clone();
        }

    public ButtonAlternate( Packet packetFirst, Packet packetSecond )
        {
        packets[0] = packetFirst;
        packets[1] = packetSecond;
        }

    public String getFirstString()
        {
        return packets[0].getString();
        }

    public String getSecondString()
        {
        return packets[1].getString();
        }

    public boolean isChangingButton()
        {
        return true;
        }

    // All but last title is drawn
    protected void drawButtonTextTitles(Canvas canvas, int xOffsetInPixel, int yOffsetInPixel)
        {
        int centerX = getPixelX(columnInGrids, xOffsetInPixel);
        int centerY = getPixelY(rowInGrids, yOffsetInPixel);

        Iterator<TitleDescriptor> titlesIterator = titles.iterator();
        if (titlesIterator.hasNext())
            titlesIterator.next(); // just step over the last item
        while (titlesIterator.hasNext())
            {
            titlesIterator.next().drawTextTitle(canvas, layout, centerX, centerY);
            }
        }

    // Last title is drawn as changing part
    public void drawButtonChangingPart(Canvas canvas)
        {
        // draw last title - always with changing string
        int centerX = getPixelX( columnInGrids, layout.layoutXOffset);
        int centerY = getPixelY( rowInGrids, layout.layoutYOffset);
        titles.getLast().drawTitle(canvas, layout, packets[counter].getString(), centerX, centerY);
        }

    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        packets[counter].send();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        packets[counter].release();
        counter++;
        counter&=1;
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( layout.softBoardData.softBoardListener.undoLastString() )
            {
            counter++;
            counter&=1;
            packets[counter].send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            }
        return false;
        }
    }

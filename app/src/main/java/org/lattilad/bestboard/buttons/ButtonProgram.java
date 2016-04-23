package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.SoftBoardData;

import java.util.Iterator;

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

    public boolean isChangingButton()
        {
        return true;
        }

    /**
     * This all comes from memory button, these buttons are very similar
     * Common class is needed
     */

    // This is needed only by debug methods, because last title is not drawn
    public String getFirstString()
        {
        return "PROG";
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
    private void drawButtonLastTitle(Canvas canvas)
        {
        // draw last title - always with changing string
        int centerX = getPixelX( columnInGrids, layout.layoutXOffset);
        int centerY = getPixelY(rowInGrids, layout.layoutYOffset);
        String string;

        if ( packetRun == null )
            string = getFirstString();
        else
            string = packetRun.getString();

        titles.getLast().drawTitle(canvas, layout, string, centerX, centerY);
        }

    // public void drawButtonConstantPart(Canvas canvas) - remains original

    // Last title is drawn as changing part
    public void drawButtonChangingPart(Canvas canvas)
        {
        drawButtonLastTitle( canvas );
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

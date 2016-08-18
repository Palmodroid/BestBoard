package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.SoftBoardData;

import java.util.List;

public class ButtonAbbrev extends ButtonMainTouch implements Cloneable
    {
    private List<Long> idList;


    @Override
    public ButtonAbbrev clone()
        {
        return (ButtonAbbrev)super.clone();
        }

    public ButtonAbbrev( List<Long> idList )
        {
        idList = idList;
        }

    public boolean isChangingButton()
        {
        return true;
        }

    public String getFirstString()
        {
        return "ABR";
        }

    @Override
    public void drawButtonChangingPart(Canvas canvas)
        {
        // Call super if show-title is needed
        if ( layout.softBoardData.abbreviations.activeButton == this )
            {
            drawButtonBackground(canvas, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);
            drawButtonTextTitles(canvas, layout.layoutXOffset, layout.layoutYOffset);
            }
        }

    @Override
    public void mainTouchStart(boolean isTouchDown)
        {
        if ( layout.softBoardData.abbreviations.activeButton == this )
            {
            layout.softBoardData.abbreviations.activeButton = null;
            layout.softBoardData.abbreviations.start( idList );
            }
        else
            {
            layout.softBoardData.abbreviations.activeButton = this;
            layout.softBoardData.abbreviations.stop();
            }
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd(boolean isTouchUp)
        {
        // nothing to do
        }

    @Override
    public boolean fireSecondary(int type)
        {
        // no secondary function is implemented
        return false;
        }

    }

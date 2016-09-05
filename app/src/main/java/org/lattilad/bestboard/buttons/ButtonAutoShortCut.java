package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

public class ButtonAutoShortCut extends ButtonMainTouch implements Cloneable
    {
    private Long shortCutId;

    @Override
    public ButtonAutoShortCut clone()
        {
        return (ButtonAutoShortCut)super.clone();
        }

    public ButtonAutoShortCut( Long shortCutId )
        {
        this.shortCutId = shortCutId;
        }

    @Override
    public String getFirstString()
        {
        return "ABR";
        }

    @Override
    public boolean isColorChanging()
        {
        return true;
        }

    @Override
    public int getColor()
        {
        return (layout.softBoardData.codeTextProcessor.activeShortCutId == shortCutId) ?
                layout.softBoardData.lockColor : super.getColor();
        }

    @Override
    public void mainTouchStart(boolean isTouchDown)
        {
        if ( layout.softBoardData.codeTextProcessor.activeShortCutId == shortCutId )
            {
            layout.softBoardData.codeTextProcessor.stopAbbreviation();
            }
        else
            {
            layout.softBoardData.codeTextProcessor.startAbbreviation( shortCutId );
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

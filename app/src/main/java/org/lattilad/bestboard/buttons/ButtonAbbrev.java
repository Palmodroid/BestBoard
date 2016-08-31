package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

import java.util.List;

public class ButtonAbbrev extends ButtonMainTouch implements Cloneable
    {
    private List<Long> idList;

    public List<Long> getIdList()
        {
        return idList;
        }

    @Override
    public ButtonAbbrev clone()
        {
        return (ButtonAbbrev)super.clone();
        }

    public ButtonAbbrev( List<Long> idList )
        {
        this.idList = idList;
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
        return (layout.softBoardData.codeTextProcessor.activeButton == this) ?
                layout.softBoardData.lockColor : super.getColor();
        }

    @Override
    public void mainTouchStart(boolean isTouchDown)
        {
        if ( layout.softBoardData.codeTextProcessor.activeButton == this )
            {
            layout.softBoardData.codeTextProcessor.activeButton = null;
            layout.softBoardData.codeTextProcessor.stopAbbreviation();
            }
        else
            {
            layout.softBoardData.codeTextProcessor.activeButton = this;
            layout.softBoardData.codeTextProcessor.startAbbreviation( idList );
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

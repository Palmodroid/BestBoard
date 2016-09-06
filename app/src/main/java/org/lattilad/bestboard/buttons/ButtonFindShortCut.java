package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.codetext.Entry;
import org.lattilad.bestboard.codetext.EntryList;

public class ButtonFindShortCut extends ButtonMainTouch implements Cloneable
    {
    private Long shortCutId;

    @Override
    public ButtonFindShortCut clone()
        {
        return (ButtonFindShortCut)super.clone();
        }

    public ButtonFindShortCut(Long shortCutId )
        {
        this.shortCutId = shortCutId;
        }

    @Override
    public String getFirstString()
        {
        return "FINDA";
        }

    @Override
    public void mainTouchStart(boolean isTouchDown)
        {
        // another lookup is needed !!
        Entry entry = layout.softBoardData.codeTextProcessor.getShortCut( shortCutId )
                .lookUp( layout.softBoardData.softBoardListener.getTextBeforeCursor(), EntryList.LONGEST );

        if ( entry != null )        // no entry - stop
            entry.activate( layout.softBoardData.softBoardListener );

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

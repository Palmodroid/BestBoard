package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.codetext.Entry;

import java.util.List;

public class ButtonFindShortcut extends ButtonMainTouch implements Cloneable
    {
    private List<Long> idList;

    /* public List<Long> getIdList()
        {
        return idList;
        } */

    @Override
    public ButtonFindShortcut clone()
        {
        return (ButtonFindShortcut)super.clone();
        }

    public ButtonFindShortcut( List<Long> idList )
        {
        this.idList = idList;

        for ( Long id : idList )
            {
            codeEntries.addAll( abbreviations.get( id ) );
            }

        codeEntries.sort();
        }

    @Override
    public String getFirstString()
        {
        return "FINDA";
        }

    @Override
    public void mainTouchStart(boolean isTouchDown)
        {
        if ( layout.softBoardData.codeTextProcessor.activeAbbrevIdList == idList )
            {
            layout.softBoardData.codeTextProcessor.activeAbbrevIdList = null;
            layout.softBoardData.codeTextProcessor.stopAbbreviation();
            }
        else
            {
            layout.softBoardData.codeTextProcessor.activeAbbrevIdList = idList;
            layout.softBoardData.codeTextProcessor.startAbbreviation( idList );
            }

        Entry firstEntry = null;
        // ABBREV - recursive
        while ( abbrevCounter == undoCounter )
            {
            Entry entry = layout.softBoardData.codeTextProcessor.getCodeEntries().lookUp( textBeforeCursor );

            if ( entry == null )        // no entry - stop
                break;

            entry.activate( this );
            /* if ( entry instanceof ShortCutEntry )
                {
                changeStringBeforeCursor(entry.getCode().length(), ((ShortCutEntry) entry).getExpanded());
                } */

            if ( entry == firstEntry)  // abbreviationEntry was already used - prevent infinite loops
                break;

            if ( firstEntry == null )   // set first abbreviationEntry to detect infinite loops
                firstEntry = entry;
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

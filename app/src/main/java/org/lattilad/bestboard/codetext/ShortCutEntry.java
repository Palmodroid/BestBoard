package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.SoftBoardProcessor;

public class ShortCutEntry extends Entry
    {
    private String expanded;

    public ShortCutEntry(String code, String expanded )
        {
        super(code);
        this.expanded = expanded;
        }

    public String getExpanded()
        {
        return expanded;
        }

    @Override
    public void activate(SoftBoardProcessor processor)
        {
        processor.changeStringBeforeCursor( getCode().length(), expanded);
        }
    }

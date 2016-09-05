package org.lattilad.bestboard.codetext;

import org.lattilad.bestboard.SoftBoardListener;

/**
 * Created by Beothe on 2016.08.20..
 */
public class VariaEntry extends Entry
    {
    private VariaGroup variaGroup;

    public VariaEntry(String code, VariaGroup variaGroup )
        {
        super(code);
        this.variaGroup = variaGroup;
        }

    @Override
    public void activate(SoftBoardListener processor)
        {
        variaGroup.activate();
        }

    }

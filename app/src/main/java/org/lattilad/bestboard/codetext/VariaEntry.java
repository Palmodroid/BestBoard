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
    public void activate( SoftBoardListener processor )
        {
        if ( variaGroup.getVaria().isKeepCode() )
            {
            variaGroup.getVaria().setCodeEntry( processor.getProcessCounter(), getCode().length());
            }
        else
            {
            processor.sendDelete( -getCode().length() );
            } 
        variaGroup.activate();
        }

    }

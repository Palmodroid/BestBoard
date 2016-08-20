package org.lattilad.bestboard.codetext;

/**
 * Created by Beothe on 2016.08.20..
 */
public class VariaEntry extends Entry
    {
    private Varia varia;
    private int index;

    public VariaEntry(String code, Varia varia, int index )
        {
        super(code);
        this.varia = varia;
        this.index = index;
        }

    }

package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.codetext.Varia;

/**
 * Created by tamas on 2016.08.22..
 */
public class PacketTextVaria extends PacketTextBase
    {
    private Varia varia;
    private int index;

    public PacketTextVaria( SoftBoardData softBoardData, Varia varia, int index )
        {
        super(softBoardData);
        this. varia = varia;
        this.index = index;
        }

    @Override
    protected String getString()
        {
        return varia.getText( index );
        }

    @Override
    public String getTitleString()
        {
        return varia.getTitle( index );
        }

    // These packets needs constant redraw
    public boolean needsChangingButton()
        {
        return true;
        }
    }

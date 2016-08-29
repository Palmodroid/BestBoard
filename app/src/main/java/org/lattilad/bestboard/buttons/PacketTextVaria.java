package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.codetext.Varia;

/**
 * Created by tamas on 2016.08.22..
 */
public class PacketTextVaria extends PacketText
    {
    private long variaId;
    private int index;

    public PacketTextVaria( SoftBoardData softBoardData, long variaId, int index )
        {
        super(softBoardData);


        this.variaId = variaId;
        this.index = index;
        }

    // !! Packets should be called after parsing, to initialize eg. varia connections

    @Override
    protected String getString()
        {
        Varia varia = softBoardData.codeTextProcessor.getVaria( variaId );
        return (varia != null) ? varia.getText( index ) : "";
        }

    @Override
    public String getTitleString()
        {
        Varia varia = softBoardData.codeTextProcessor.getVaria( variaId );
        return (varia != null) ? varia.getTitle( index ) : "";
        }

    // These packets needs constant redraw
    public boolean isTitleStringChanging()
        {
        return true;
        }
    }

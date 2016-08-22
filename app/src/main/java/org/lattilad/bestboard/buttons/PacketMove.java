package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.SoftBoardProcessor;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.MetaState;

/**
 * PacketMove ( RIGHT/LEFT *CHAR/WORD/PARA CURSOR *LAST/BEGIN/END SELECT ALWAYS/NEVER/*IFSHIFT )
 * PacketMove ( TOP/BOTTOM SELECT ALWAYS/NEVER/*IFSHIFT )
 */
public class PacketMove extends Packet // eventually it is in the group of PacketFunction
    {
    public final static int TOP = 0 ;
    public final static int LEFT = 1 ;
    public final static int RIGHT = 2 ;
    public final static int BOTTOM = 3 ;

    public final static int CHAR = 0;
    public final static int WORD = 0x100;
    public final static int PARA = 2 * 0x100;

    protected int moveType;

    protected int cursorType;

    public final static int SELECT_ALWAYS = 1 ;
    public final static int SELECT_NEVER = 2 ;
    public final static int SELECT_IFSHIFT = 3 ;

    protected int selectionType;

    protected PacketKey packetKey = null;


    public PacketMove(SoftBoardData softBoardData, int moveType, int cursorType, int selectionType, PacketKey packetKey )
        {
        super(softBoardData);

        this.moveType = moveType;
        this.cursorType = cursorType;
        this.selectionType = selectionType;
        this.packetKey = packetKey;

        if ( moveType == TOP )
            setTitleString( "T-" );
        else if ( moveType == BOTTOM )
            setTitleString( "-B" );
        else
            {
            StringBuilder builder = new StringBuilder();
            if ( (moveType & 0xFF00) != CHAR )
                builder.append( (moveType & 0xFF00) == WORD ? 'W' : 'P' );
            builder.append( (moveType & 0xFF) == LEFT ? 'L' : 'R');
            if ( cursorType != SoftBoardProcessor.CURSOR_LAST )
                builder.append( cursorType == SoftBoardProcessor.CURSOR_BEGIN ? '1' : '2' );
            setTitleString( builder.toString() );
            }
        }

    @Override
    public void send()
        {
        if ( !softBoardData.softBoardListener.isRetrieveTextEnabled() )
            {
            packetKey.send();
            return;
            }

        boolean select = selectionType == SELECT_IFSHIFT ?
                softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF :
                selectionType == SELECT_ALWAYS;

        switch ( moveType )
            {
            case RIGHT:
                softBoardData.softBoardListener.jumpRight( cursorType, select );
                break;
            case LEFT:
                softBoardData.softBoardListener.jumpLeft(cursorType, select);
                break;
            case RIGHT | WORD:
                softBoardData.softBoardListener.jumpWordRight(cursorType, select);
                break;
            case LEFT | WORD:
                softBoardData.softBoardListener.jumpWordLeft(cursorType, select);
                break;
            case RIGHT | PARA:
                softBoardData.softBoardListener.jumpParaRight(cursorType, select);
                break;
            case LEFT | PARA:
                softBoardData.softBoardListener.jumpParaLeft(cursorType, select);
                break;
            case TOP:
                softBoardData.softBoardListener.jumpTop( select );
                break;
            case BOTTOM:
                softBoardData.softBoardListener.jumpBottom( select );
                break;
            }
        }
    }

package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * MOVE ( RIGHT/LEFT *CHAR/WORD/PARA CURSOR *LAST/BEGIN/END SELECT ALWAYS/NEVER/*IFSHIFT KEYINSTEAD )
 * MOVE ( TOP/BOTTOM SELECT ALWAYS/NEVER/*IFSHIFT KEYINSTEAD )
 */
public class PacketMove extends Packet // eventually it is in the group of PacketFunction
    {
    public final static int RIGHT = 1 ;
    public final static int LEFT = 1 ;
    public final static int TOP = 1 ;
    public final static int BOTTOM = 1 ;

    public final static int CHAR = 1 ;
    public final static int WORD = 1 ;
    public final static int PARA = 1 ;

    protected int moveType;

    public final static int CURSOR_BEGIN = 1 ;
    public final static int CURSOR_END = 1 ;
    public final static int CURSOR_LAST = 1 ;

    protected int cursorType;

    public final static int SELECT_ALWAYS = 1 ;
    public final static int SELECT_NEVER = 1 ;
    public final static int SELECT_IF = 1 ;

    protected int selectionType;

    protected boolean useKeyInstead;


    protected PacketMove(SoftBoardData softBoardData, int moveType, int cursorType, int selectionType, boolean useKeyInstead )
        {
        super(softBoardData);

        this.moveType = moveType;
        this.cursorType = cursorType;
        this.selectionType = selectionType;
        this.useKeyInstead = useKeyInstead;
        }

    @Override
    public String getString()
        {
        return null;
        }

    @Override
    public void send()
        {

        }
    }

package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;

import dancingmoon.bestboard.Debug;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.CapsState;
import dancingmoon.bestboard.states.MetaState;
import dancingmoon.bestboard.utils.ExternalDataException;

public class ButtonMeta extends ButtonMultiTouch implements
        Button.ChangingButton, Cloneable
    {
    private int type;
    private boolean lockKey;

    public ButtonMeta( int type, boolean lockKey ) throws ExternalDataException
        {
        if ( type < 0 || type >= BoardStates.META_STATES_SIZE )
            throw new ExternalDataException();

        this.type = type;
        this.lockKey = lockKey;
        }

    @Override
    public ButtonMeta clone()
        {
        return (ButtonMeta)super.clone();
        }

    @Override
    public String getString()
        {
        if (lockKey)
            {
            switch ( type )
                {
                case BoardStates.META_CAPS:
                    return "CAPSl";
                case BoardStates.META_SHIFT:
                    return "SHFTl";
                case BoardStates.META_CTRL:
                    return "CTRLl";
                case BoardStates.META_ALT:
                    return "ALTl";
                default:
                    return "N/A";
                }
            }
        else
            {
            switch ( type )
                {
                case BoardStates.META_CAPS:
                    return "CAPS";
                case BoardStates.META_SHIFT:
                    return "SHIFT";
                case BoardStates.META_CTRL:
                    return "CTRL";
                case BoardStates.META_ALT:
                    return "ALT";
                default:
                    return "N/A";
                }
            }
        }

    @Override
    public void drawChangingButton(Canvas canvas)
        {
        if ( board.softBoardData.boardStates.metaStates[type].getState() == MetaState.IN_TOUCH &&
                board.softBoardData.displayTouch)
            drawButton( canvas, board.softBoardData.touchColor, board.xOffset );

        else if ( board.softBoardData.boardStates.metaStates[type].getState() == MetaState.META_ON )
            drawButton( canvas, board.softBoardData.metaColor, board.xOffset );

        else if ( board.softBoardData.boardStates.metaStates[type].getState() == MetaState.META_LOCK )
            drawButton( canvas, board.softBoardData.lockColor, board.xOffset );

        // It is only needed by CAPS, but all meta-buttons will know it.
        else if ( board.softBoardData.boardStates.metaStates[type].getState() == CapsState.AUTOCAPS_ON )
            drawButton( canvas, board.softBoardData.autoColor, board.xOffset );

        // If state == META_OFF, then no redraw is needed
        }

    @Override
    public void multiTouchEvent( int phase )
        {
        // lockKey is not implemented yet !!

        if ( phase == META_TOUCH )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button TOUCH.");
            board.softBoardData.boardStates.metaStates[type].touch();
            }
        else if ( phase == META_RELEASE )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button RELEASE.");
            board.softBoardData.boardStates.metaStates[type].release( lockKey );
            }
        else if ( phase == META_CANCEL )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button CANCEL.");
            board.softBoardData.boardStates.metaStates[type].cancel();
            }
        }
    }

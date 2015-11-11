package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;

import dancingmoon.bestboard.Debug;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.LinkState;

public class ButtonLink extends ButtonMultiTouch implements
        Button.ChangingButton, Cloneable
    {
    private int index;
    private boolean lockKey;

    // USE table is filled up only after the definition of the boards
    // At the time of the definition of the USE keys, no data is
    // available about the table, so index verification is not possible
    // invalid index means: GO BACK
    public ButtonLink( int index, boolean lockKey )
        {
        this.index = index;
        this.lockKey = lockKey;
        }

    @Override
    public ButtonLink clone()
        {
        return (ButtonLink)super.clone();
        }

    // MAX_LINKS is only one hexadecimal digit !!
    @Override
    public String getString()
        {
        if ( index < 0 || index >= LinkState.MAX_LINKS )
            return "PREV";
        else
            return (lockKey ? "L" : "") + Integer.toHexString( index );
        }

    @Override
    public void drawChangingButton(Canvas canvas)
        {
        if ( board.softBoardData.linkState.getState( index ) == LinkState.ACTIVE )
            drawButton( canvas, board.softBoardData.metaColor,
                    board.xOffset - board.areaXOffset, - board.areaYOffset );
        else if ( board.softBoardData.linkState.getState( index ) == LinkState.LOCKED )
            drawButton( canvas, board.softBoardData.lockColor,
                    board.xOffset - board.areaXOffset, - board.areaYOffset );
        else if ( board.softBoardData.linkState.getState( index ) == LinkState.TOUCHED &&
                board.softBoardData.displayTouch)
            drawButton( canvas, board.softBoardData.touchColor,
                    board.xOffset - board.areaXOffset, - board.areaYOffset );
        // If state == HIDDEN, then no redraw is needed
        }

    @Override
    public void multiTouchEvent( int phase )
        {
        // lock is not implemented yet !!

        if ( phase == ButtonMultiTouch.META_TOUCH )
            {
            Scribe.debug( Debug.BUTTON, "Index " + index + " USE Button TOUCH.");
            board.softBoardData.linkState.touch( index );
            }
        else if ( phase == ButtonMultiTouch.META_RELEASE )
            {
            Scribe.debug( Debug.BUTTON, "Index " + index + " USE Button RELEASE.");
            board.softBoardData.linkState.release( index, lockKey );
            }
        else if ( phase == ButtonMultiTouch.META_CANCEL )
            {
            Scribe.debug( Debug.BUTTON, "Index " + index + " USE Button CANCEL.");
            board.softBoardData.linkState.cancel( index );
            }
        }
    }

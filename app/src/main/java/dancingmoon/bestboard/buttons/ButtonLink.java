package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;

import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.BoardLinks;

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
        if ( index < 0 || index >= BoardLinks.MAX_LINKS )
            return "PREV";
        else
            return (lockKey ? "L" : "") + Integer.toHexString( index );
        }

    @Override
    public void drawChangingButton(Canvas canvas)
        {
        if ( layout.softBoardData.boardLinks.getState( index ) == BoardLinks.ACTIVE )
            drawButton( canvas, layout.softBoardData.metaColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( layout.softBoardData.boardLinks.getState( index ) == BoardLinks.LOCKED )
            drawButton( canvas, layout.softBoardData.lockColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( layout.softBoardData.boardLinks.getState( index ) == BoardLinks.TOUCHED &&
                layout.softBoardData.displayTouch)
            drawButton( canvas, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);
        // If state == HIDDEN, then no redraw is needed
        }

    @Override
    public void multiTouchEvent( int phase )
        {
        // lock is not implemented yet !!

        if ( phase == ButtonMultiTouch.META_TOUCH )
            {
            Scribe.debug( Debug.BUTTON, "Index " + index + " USE Button TOUCH.");
            layout.softBoardData.boardLinks.touch( index );
            }
        else if ( phase == ButtonMultiTouch.META_RELEASE )
            {
            Scribe.debug( Debug.BUTTON, "Index " + index + " USE Button RELEASE.");
            layout.softBoardData.boardLinks.release( index, lockKey );
            }
        else if ( phase == ButtonMultiTouch.META_CANCEL )
            {
            Scribe.debug( Debug.BUTTON, "Index " + index + " USE Button CANCEL.");
            layout.softBoardData.boardLinks.cancel( index );
            }
        }
    }

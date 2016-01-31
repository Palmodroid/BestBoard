package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.BoardTable;

public class ButtonLink extends ButtonMultiTouch implements
        Button.ChangingButton, Cloneable
    {
    private Long layoutId;
    private boolean lockKey;

    // USE table is filled up only after the definition of the boards
    // At the time of the definition of the USE keys, no data is
    // available about the table, so index verification is not possible
    // null layoutId means: GO BACK
    public ButtonLink( Long layoutId, boolean lockKey )
        {
        this.layoutId = layoutId;
        this.lockKey = lockKey;
        }

    @Override
    public ButtonLink clone()
        {
        return (ButtonLink)super.clone();
        }

    @Override
    public String getString()
        {
        if ( layoutId == null )
            return "PREV";
        else
            return (lockKey ? "L" : "") + Tokenizer.regenerateKeyword(layoutId);
        }

    @Override
    public void drawChangingButton(Canvas canvas)
        {
        int state = layout.softBoardData.boardTable.getState(layoutId);

        if ( state == BoardTable.ACTIVE )
            drawButton( canvas, layout.softBoardData.metaColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( state == BoardTable.LOCKED )
            drawButton( canvas, layout.softBoardData.lockColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( state == BoardTable.TOUCHED &&
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
            Scribe.debug( Debug.BUTTON, "Board " + Tokenizer.regenerateKeyword(layoutId) + " LINK Button TOUCH.");
            layout.softBoardData.boardTable.touch(layoutId);
            }
        else if ( phase == ButtonMultiTouch.META_RELEASE )
            {
            Scribe.debug( Debug.BUTTON, "Board " + Tokenizer.regenerateKeyword(layoutId) + " LINK Button RELEASE.");
            layout.softBoardData.boardTable.release(layoutId, lockKey );
            }
        else if ( phase == ButtonMultiTouch.META_CANCEL )
            {
            Scribe.debug( Debug.BUTTON, "Board " + Tokenizer.regenerateKeyword(layoutId) + " LINK Button CANCEL.");
            layout.softBoardData.boardTable.cancel(layoutId);
            }
        }
    }

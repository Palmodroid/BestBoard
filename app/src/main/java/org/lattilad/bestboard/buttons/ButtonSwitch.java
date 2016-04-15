package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.BoardTable;

public class ButtonSwitch extends ButtonMultiTouch implements
        Button.ChangingButton, Cloneable
    {
    private long layoutId;
    private boolean lockKey;

    // BOARD table is filled up only after the definition of the boards
    // At the time of the definition of the SWITCH keys, no data is
    // available about the table, so index verification is not possible
    // Special 'BACK' layoutId means: GO BACK
    public ButtonSwitch(long layoutId, boolean lockKey)
        {
        this.layoutId = layoutId;
        this.lockKey = lockKey;
        }

    @Override
    public ButtonSwitch clone()
        {
        return (ButtonSwitch)super.clone();
        }

    @Override
    public String getString()
        {
        return (lockKey ? "L" : "") + Tokenizer.regenerateKeyword(layoutId);
        }

    @Override
    public void drawButtonChangingPart(Canvas canvas)
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
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
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

package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.BoardTable;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;

public class ButtonSwitch extends ButtonMultiTouch implements Cloneable
    {
    private long layoutId;
    private boolean lockKey;
    private boolean showAutoCaps;

    // BOARD table is filled up only after the definition of the boards
    // At the time of the definition of the SWITCH keys, no data is
    // available about the table, so index verification is not possible
    // Special 'BACK' layoutId means: GO BACK
    public ButtonSwitch(long layoutId, boolean lockKey, boolean showAutoCaps)
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
    public String getFirstString()
        {
        return (lockKey ? "L" : "") + Tokenizer.regenerateKeyword(layoutId);
        }

    @Override
    public void drawButtonChangingPart(Canvas canvas)
        {
        int state = layout.softBoardData.boardTable.getState(layoutId);

        if ( state == BoardTable.ACTIVE )
            drawButtonBackground(canvas, layout.softBoardData.metaColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( state == BoardTable.LOCKED )
            drawButtonBackground(canvas, layout.softBoardData.lockColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( state == BoardTable.TOUCHED && layout.softBoardData.displayTouch )
            drawButtonBackground(canvas, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);

        // It is only needed by CAPS, but all meta-buttons will know it.
        else if ( layout.softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS].getState() == CapsState.AUTOCAPS_ON)
            drawButtonBackground(canvas, layout.softBoardData.autoColor, layout.layoutXOffset, layout.layoutYOffset);

        else
            return; // If state == HIDDEN, then no redraw is needed
        drawButtonTextTitles(canvas, layout.layoutXOffset, layout.layoutYOffset);
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

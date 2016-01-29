package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;

import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.parser.Tokenizer;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.BoardLinks;

public class ButtonLink extends ButtonMultiTouch implements
        Button.ChangingButton, Cloneable
    {
    private Long id;
    private boolean lockKey;

    // USE table is filled up only after the definition of the boards
    // At the time of the definition of the USE keys, no data is
    // available about the table, so index verification is not possible
    // null id means: GO BACK
    public ButtonLink( Long id, boolean lockKey )
        {
        this.id = id;
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
        if ( id == null )
            return "PREV";
        else
            return (lockKey ? "L" : "") + Tokenizer.regenerateKeyword(id);
        }

    @Override
    public void drawChangingButton(Canvas canvas)
        {
        int state = layout.softBoardData.boardLinks.getState( id );

        if ( state == BoardLinks.ACTIVE )
            drawButton( canvas, layout.softBoardData.metaColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( state == BoardLinks.LOCKED )
            drawButton( canvas, layout.softBoardData.lockColor, layout.layoutXOffset, layout.layoutYOffset);
        else if ( state == BoardLinks.TOUCHED &&
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
            Scribe.debug( Debug.BUTTON, "Board " + Tokenizer.regenerateKeyword(id) + " LINK Button TOUCH.");
            layout.softBoardData.boardLinks.touch( id );
            }
        else if ( phase == ButtonMultiTouch.META_RELEASE )
            {
            Scribe.debug( Debug.BUTTON, "Board " + Tokenizer.regenerateKeyword(id) + " LINK Button RELEASE.");
            layout.softBoardData.boardLinks.release( id, lockKey );
            }
        else if ( phase == ButtonMultiTouch.META_CANCEL )
            {
            Scribe.debug( Debug.BUTTON, "Board " + Tokenizer.regenerateKeyword(id) + " LINK Button CANCEL.");
            layout.softBoardData.boardLinks.cancel( id );
            }
        }
    }

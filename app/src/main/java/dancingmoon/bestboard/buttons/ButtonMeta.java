package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;

import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.LayoutStates;
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
        if ( type < 0 || type >= LayoutStates.META_STATES_SIZE )
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
                case LayoutStates.META_CAPS:
                    return "CAPSl";
                case LayoutStates.META_SHIFT:
                    return "SHFTl";
                case LayoutStates.META_CTRL:
                    return "CTRLl";
                case LayoutStates.META_ALT:
                    return "ALTl";
                default:
                    return "N/A";
                }
            }
        else
            {
            switch ( type )
                {
                case LayoutStates.META_CAPS:
                    return "CAPS";
                case LayoutStates.META_SHIFT:
                    return "SHIFT";
                case LayoutStates.META_CTRL:
                    return "CTRL";
                case LayoutStates.META_ALT:
                    return "ALT";
                default:
                    return "N/A";
                }
            }
        }

    @Override
    public void drawChangingButton(Canvas canvas)
        {
        if ( layout.softBoardData.layoutStates.metaStates[type].getState() == MetaState.IN_TOUCH &&
                layout.softBoardData.displayTouch)
            drawButton( canvas, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);

        else if ( layout.softBoardData.layoutStates.metaStates[type].getState() == MetaState.META_ON )
            drawButton( canvas, layout.softBoardData.metaColor, layout.layoutXOffset, layout.layoutYOffset);

        else if ( layout.softBoardData.layoutStates.metaStates[type].getState() == MetaState.META_LOCK )
            drawButton( canvas, layout.softBoardData.lockColor, layout.layoutXOffset, layout.layoutYOffset);

        // It is only needed by CAPS, but all meta-buttons will know it.
        else if ( layout.softBoardData.layoutStates.metaStates[type].getState() == CapsState.AUTOCAPS_ON )
            drawButton( canvas, layout.softBoardData.autoColor, layout.layoutXOffset, layout.layoutYOffset);

        // If state == META_OFF, then no redraw is needed
        }

    @Override
    public void multiTouchEvent( int phase )
        {
        // lockKey is not implemented yet !!

        if ( phase == META_TOUCH )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button TOUCH.");
            layout.softBoardData.layoutStates.metaStates[type].touch();
            }
        else if ( phase == META_RELEASE )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button RELEASE.");
            layout.softBoardData.layoutStates.metaStates[type].release( lockKey );
            }
        else if ( phase == META_CANCEL )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button CANCEL.");
            layout.softBoardData.layoutStates.metaStates[type].cancel();
            }
        }
    }

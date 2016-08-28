package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.MetaState;
import org.lattilad.bestboard.utils.ExternalDataException;

public class ButtonMeta extends ButtonMultiTouch implements Cloneable
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
    public String getFirstString()
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
    public int getColor()
        {
        if ( layout.softBoardData.layoutStates.metaStates[type].getState() == MetaState.IN_TOUCH &&
                layout.softBoardData.displayTouch)
            return layout.softBoardData.touchColor;

        else if ( layout.softBoardData.layoutStates.metaStates[type].getState() == MetaState.META_ON )
            return layout.softBoardData.metaColor;

        else if ( layout.softBoardData.layoutStates.metaStates[type].getState() == MetaState.META_LOCK )
            return layout.softBoardData.lockColor;

        // It is only needed by CAPS, but all meta-buttons will know it.
        else if ( layout.softBoardData.autoFuncEnabled &&
                layout.softBoardData.layoutStates.metaStates[type].getState() == CapsState.AUTOCAPS_ON )
            return layout.softBoardData.autoColor;

        return super.getColor(); // If state == META_OFF, then default color is needed
        }


    @Override
    public void multiTouchEvent( int phase )
        {
        // lockKey is not implemented yet !!

        if ( phase == META_TOUCH )
            {
            Scribe.debug( Debug.BUTTON, "Type " + type + " META Button TOUCH.");
            layout.softBoardData.layoutStates.metaStates[type].touch();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
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

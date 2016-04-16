package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

/**
 * Base class for buttons on the MULTI stroke
 */
public abstract class ButtonMultiTouch extends Button
    {
    @Override
    public ButtonMultiTouch clone()
        {
        return (ButtonMultiTouch)super.clone();
        }

    public boolean isChangingButton()
        {
        return true;
        }

    // public void drawButtonConstantPart(Canvas canvas) - remains original

    // public void drawButtonChangingPart(Canvas canvas) - is overridden (call super, if marker is needed)

    // public void drawButtonTouched(Canvas canvas) - is not used

    /** MAIN stroke arrived to a META key */
    public static int META_TOUCH = 10;
    /** META key was released */
    public static int META_RELEASE = 11;
    /** META touch was cancelled (ex. because of SPen) */
    public static int META_CANCEL = 12;

    /**
     * Button performs its action by fire method.
     * It is called at several phases by Layout.evaluateTouch()
     */
    public abstract void multiTouchEvent( int phase );
    }

package org.lattilad.bestboard.buttons;

/**
 * Base class for buttons on the MAIN stroke
 */
public abstract class ButtonMainTouch extends Button
    {
    @Override
    public ButtonMainTouch clone()
        {
        return (ButtonMainTouch)super.clone();
        }

   /**
     * New bow is started, button is touched.
     * @param isTouchDown true if button is touched by touch down and not by touch move
     */
    public abstract void mainTouchStart( boolean isTouchDown );

    /**
     * Bow is ended, button is released.
     * @param isTouchUp true if button is released by touch up and not by touch move
     */
    public abstract void mainTouchEnd( boolean isTouchUp );

    /**
     * Touch is circling on button (or button is hard pressed)
     * @param isHardPress true if method is triggered by hard press
     */
    public abstract void mainTouchOnCircle( boolean isHardPress );

    /**
     * Touch is stayed on button
     * @return true if button could be repeated quickly (repeat)
     * or false if button should wait for next "on stay" trigger
     */
    public abstract boolean mainTouchOnStay();

    }

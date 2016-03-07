package org.lattilad.bestboard.buttons;

/**
 * Base class for buttons on the MAIN stroke
 */
public abstract class ButtonMainTouch extends Button
    {
    /**
     * Types of secondary fire.
     * ON_STAY should be negative
     * ON_CIRCLE and ON_PRESS should be positive
     */
    public static final int ON_STAY = -1;
    public static final int ON_CIRCLE = 1;
    public static final int ON_HARD_PRESS = 2;


    /**
     * onCircle == true, then secondary functions are activated by circling on the button,
     * onCircle == false, then secondary functions are activated bay staying on the button.
     * Default: onCircle
     * Repeat: sets to onStay
     */
    protected boolean onCircle = true;

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
     * Sets onCircle behavior
     */
    public void setOnCircle()
        {
        onCircle = true;
        }

    /**
     * Sets onStay behavior
     */
    public void setOnStay()
        {
        onCircle = false;
        }

    /**
     * This method is called if secondary main touch is activated.
     * Child methods should implement fireSecondary().
     * @param type of the activation: ON_STAY (-1) ON_CIRCLE (1) or ON_HARD_PRESS (2)
     * @return true if button could be repeated quickly (repeat)
     * or false if button should wait for next "on stay" trigger
     * (needed only if type is ON_STAY)
     */
    public boolean mainTouchSecondary( int type )
        {
        // type 1 or 2 (>0, true) AND onCircle == true
        // type -1 (>0, false) AND onCircle == false (onStay)
        return (type > 0) == onCircle && fireSecondary(type);
        }


    /**
     * Methods should implement secondary functionality here.
     * @param type of the activation: ON_STAY (-1) ON_CIRCLE (1) or ON_HARD_PRESS (2)
     * @return true if button could be repeated quickly (repeat)
     * or false if button should wait for next "on stay" trigger
     * (needed only if type is ON_STAY)
     */
    public abstract boolean fireSecondary( int type );
    }

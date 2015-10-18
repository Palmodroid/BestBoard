package dancingmoon.bestboard.buttons;

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


    /** Stroke and bow started by touch down. */
    public static int MAIN_DOWN = 0;
    /** Bow started by move (and not by touch down!) */
    public static int MAIN_START = 1;
    /** Long bow */
    public static int MAIN_LONG = 2;
    /** Hard pressed bow */
    public static int MAIN_PRESS = 3;
    /** Repeated bow */
    public static int MAIN_REPEAT = 4;
    /** Bow finished by move (and not by release!) */
    public static int MAIN_END = 5;
    /** Stroke finished by touch release. */
    public static int MAIN_UP = 6;


    /**
     * Button performs its action by fire method.
     * It is called at several phases by Board.evaluateTouch()
     */
    public abstract void mainTouchEvent(int phase);
    }

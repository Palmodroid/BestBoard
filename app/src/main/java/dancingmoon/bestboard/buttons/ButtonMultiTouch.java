package dancingmoon.bestboard.buttons;

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

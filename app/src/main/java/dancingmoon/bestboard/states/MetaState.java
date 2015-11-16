package dancingmoon.bestboard.states;

import dancingmoon.bestboard.Debug;
import dancingmoon.bestboard.scribe.Scribe;

/**
 * General abstract class for meta-states, like SHIFT, ALT, CTRL, CAPS.
 * This class is fully functional,
 * but CAPS needs AUTOCAPS functionality,
 * and hard-keys needs forced and simulated meta-key presses, too.
 */
public abstract class MetaState
    {
    /** ACTIVE state, button is in TOUCH */
    public final static int IN_TOUCH = -1;

    /** INACTIVE state */
    public final static int META_OFF = 0;
    /** ACTIVE state - only for the next non-meta button */
    public final static int META_ON = 1;
    /** ACTIVE state - locked */
    public final static int META_LOCK = 2;


    /**
     * internal state: META_OFF / META_ON / META_LOCK
     */
    private int state = META_OFF;

    /**
     * touch: while active, state results IN_TOUCH (independent from the state itself)
     * touch-release pair: cycle state
     * touch-type-release: state is not changed
     * touches are counted, release can occur only after finishing the last touch
     */
    private int touchCounter = 0;


    /**
     * type: character was typed on the main stream during the touch
     */
    protected boolean typeFlag = false;


    /**
     * Get the internal value of the (private) state variable.
     * State variable can be reached through this protected method by the descendants.
     * @return internal state
     */
    protected int getInternalState()
        {
        return state;
        }


    /**
     * Set the internal value of (private) state variable.
     * State variable can be reached through this protected method by the descendants.
     * @param state internal state, IT SHOULD BE VALIDATED PREVIOUSLY!
     */
    protected void setInternalState( int state )
        {
        this.state = state;
        checkStateChanges(); // Internal state could be changed
        }


    /**
     * Return state without touch.
     * Possible states: META_OFF 0 / META_ON 1 / META_LOCK 2 / (In CapsState: AUTOCAPS_ON 4)
     */
    protected int getStateWithoutTouch()
        {
        return getInternalState();
        }


    /**
     * Get public state.
     * IN_TOUCH / META_OFF / META_ON / META_LOCK / (In CapsState: AUTOCAPS_ON 4)
     */
    public int getState()
        {
        return touchCounter == 0 ? getStateWithoutTouch() : IN_TOUCH;
        }


    /**
     * True if internal state is OFF (and not ON or LOCK)
     */
    protected boolean isInternalStateOff()
        {
        return getInternalState() == META_OFF;
        }


    /**
     * True if internal state is ON (and not OFF or LOCK)
     */
    protected boolean isInternalStateOn()
        {
        return getInternalState() == META_ON;
        }


    /**
     * Set public state.
     * @param state CAPS_OFF or CAPS_ON or CAPS_LOCK
     * If state is not valid, then internal state will be set to CAPS OFF
     * (Binary expressions cannot shorten it, because there are 3 main states)
     */
    public void setState( int state )
        {
        if ( state == META_LOCK )
            setInternalState( META_LOCK );
        else if ( state == META_ON )
            setInternalState( META_ON );
        else
            setInternalState( META_OFF );
        }


    /**
     * Meta-key or meta-lock-key is touched
     * state is IN_TOUCH during the touch
     * state will not change if user types during the touch
     */
    public void touch()
        {
        touchCounter++;
        checkStateChanges(); // Touch state could change
        Scribe.debug( Debug.METASTATE, "MetaState TOUCH, counter: " + touchCounter );
        }


    /**
     * Non-meta key is touched
     * If meta-key is in touch,
     * it remains in IN_TOUCH state
     * but after the meta-key release, state will not change
     * If meta-key is META_ON,
     * state will be META_OFF
     */
    public void type()
        {
        // During TOUCH flag signs type
        if ( touchCounter > 0 )
            {
            typeFlag = true;
            }
        // In META_ON state will return to META_OFF
        else if ( isInternalStateOn() )
            {
            setInternalState( META_OFF );
            Scribe.debug( Debug.METASTATE, "MetaState set to META_OFF after typing the first non-meta key.");
            }
        // META_OFF and META_LOCK states are not affected by type

        // setAutoCapsState() can influence state later
        }


    /**
     * Meta-key is released (this can happen only after touch :)
     * If non-meta was used during this touch, than nothing happens
     * else state cycles up
     */
    public void release( boolean lockKey )
        {
        touchCounter--;
        Scribe.debug( Debug.METASTATE, "MetaState RELEASE, touch-counter: " + touchCounter);

        // Just for error checking
        if ( touchCounter < 0 )
            {
            Scribe.error("MetaState RELEASE without TOUCH!");
            touchCounter = 0;
            }

        // last touch released
        if ( touchCounter == 0 )
            {
            Scribe.debug( Debug.METASTATE, "MetaState: all button RELEASED.");
            // There was no typing during the released touches
            if ( !typeFlag )
                {
                if ( isInternalStateOff() )
                    {
                    if ( lockKey )
                        {
                        setInternalState( META_LOCK );
                        Scribe.debug( Debug.METASTATE,  "MetaState cycled to META_LOCK by LOCK key." );
                        }
                    else
                        {
                        setInternalState( META_ON );
                        Scribe.debug( Debug.METASTATE,  "MetaState cycled to META_ON." );
                        }
                    }
                else if ( isInternalStateOn() ) // no difference between state and internal state
                    {
                    setInternalState( META_LOCK );
                    Scribe.debug( Debug.METASTATE, "MetaState cycled to META_LOCK.");
                    }
                else // state == META_LOCK or AUTOCAPS_ON
                    {
                    setInternalState( META_OFF );
                    Scribe.debug( Debug.METASTATE, "MetaState cycled to META_OFF.");
                    }
                }
            else // type will not change internal state
                {
                typeFlag = false;
                checkStateChanges(); // but state could change
                }

            // setAutoCapsState() can influence state if typeFlag == TRUE
            }
        }


    /**
     * Meta-key is cancelled (because SPen is activated or orientation changed)
     * Similar to release, but state will be always META_LOCK
     * (this can happen only during touch :)
     */
    public void cancel()
        {
        touchCounter--;
        Scribe.debug( Debug.METASTATE, "MetaState CANCEL, counter: " + touchCounter);

        // Just for error checking
        if ( touchCounter < 0 )
            {
            Scribe.error("MetaState CANCEL without TOUCH!");
            touchCounter = 0;
            }

        if ( touchCounter == 0 )
            {
            Scribe.debug( Debug.METASTATE, "MetaState: all button CANCELLED.");
            setInternalState( META_LOCK );
            typeFlag = false;
            Scribe.debug( Debug.METASTATE, "MetaState cancelled to META_LOCK.");
            }
        }


    /**
     * Touch counter could be checked, when there is no touch.
     */
    public void checkNoTouch()
        {
        if ( touchCounter != 0)
            {
            Scribe.error("MetaState TOUCH remained! Touch-counter: " + touchCounter);
            touchCounter = 0;    // No change in internal state ??
            checkStateChanges(); // State could change
            }
        else
            {
            Scribe.debug( Debug.TOUCH_VERBOSE, "MetaState TOUCH is empty." );
            }
        }

    /**
     * This method should be called if state change is possible.
     * MetaState doesn't use this information, but HardState does.
     */
    public void checkStateChanges()
        {
        // State is changed
        // This method is not needed for MetaState, only for HardState
        }

    }

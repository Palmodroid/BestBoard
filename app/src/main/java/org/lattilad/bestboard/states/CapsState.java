package org.lattilad.bestboard.states;

/**
 * Extended meta-state class to work with autocaps states.
 */
public class CapsState extends MetaState
    {
    /**
     **     AUTOCAPS STATE - added to META states
     **/

    /** INACTIVE state - similar to META_OFF, used as command */
    public final static int AUTOCAPS_OFF = META_OFF;

    /** ACTIVE state - activated by AUTOCAPS */
    public final static int AUTOCAPS_ON = 4;

    /** INACTIVE state, but AUTO_CAPS_HOLD can activate it */
    public final static int AUTOCAPS_WAIT = 8;

    /** NOT state, but command! AUTO_CAPS_HOLD can activate AUTOCAPS_WAIT to AUTOCAPS_ON */
    public final static int AUTOCAPS_HOLD = 12; // AUTOCAPS_ON | AUTOCAPS_WAIT


    /**
     **     MASKS TO DIFFERENTIATE BETWEEN MAIN AND AUTOCAPS STATES
     **/

    /** MASK to cut off AUTOCAPS: META_OFF (even in AUTOCAPS...), META_ON, META_LOCK */
    private final static int MASK_MAIN = 3; // CAPS_ON | CAPS_OFF

    /** MASK to cut off the WAIT bit: META_OFF (even in AUTOCAPS_WAIT), META_ON, META_LOCK, AUTOCAPS_ON */
    private final static int MASK_STATE = 7; // NOT AUTOCAPS_WAIT

    /** MASK to enable only AUTOCAPS: used to validate autocaps-state parameter */
    private final static int MASK_AUTOCAPS = 12; // AUTOCAPS_HOLD == AUTOCAPS_ON | AUTOCAPS_WAIT


    /**
     * Return TRUE, when internal state is OFF (META_OFF and AUTOCAPS_WAIT)
     */
    protected boolean isInternalStateOff()
        {
        return getStateWithoutTouch() == META_OFF;
        }


    /**
     * Return state without touch: META_OFF (0) / META_ON (1) / META_LOCK (2) / AUTOCAPS_ON (4)
     */
    protected int getStateWithoutTouch()
        {
        return getInternalState() & MASK_STATE;
        }

    /**
     * Set auto-caps state
     * State can be set only in META_OFF, AUTOCAPS_ON, AUTOCAPS_WAIT (AUTOCAPS_HOLD is not a valid state)
     * autoCapsState can be: AUTOCAPS_OFF (== CAPS_OFF), AUTOCAPS_WAIT, AUTOCAPS_ON and AUTOCAPS_HOLD
     * enabled parameter == autoFuncENabled - no AUTOCAPS_ON can be set
     */
    public void setAutoCapsState( int autoCapsState, boolean enabled )
        {
        if ( ( getInternalState() & MASK_MAIN ) == META_OFF )   // META_OFF 0, AUTOCAPS_WAIT 8, AUTOCAPS_ON 4
            {
            autoCapsState &= MASK_AUTOCAPS;

            if ( autoCapsState == AUTOCAPS_HOLD )   // change only if AUTOCAPS_WAIT
                {
                if ( getInternalState() == AUTOCAPS_WAIT )
                    autoCapsState = AUTOCAPS_ON;
                }

            setInternalState( autoCapsState );

            if ( !enabled )
                clearAutoCapsOn();
            }
        }

    public void clearAutoCapsOn()
        {
        if ( getInternalState() == AUTOCAPS_ON )
            setInternalState( AUTOCAPS_OFF );
        }
    }

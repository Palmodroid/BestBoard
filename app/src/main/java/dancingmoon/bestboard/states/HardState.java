package dancingmoon.bestboard.states;

import dancingmoon.bestboard.scribe.Scribe;

/**
 * Meta-state for hard-keys. (SHIFT, CTRL, ALT)
 */
public class HardState extends MetaState
    {

    public final int selfMetaState;

    public final BoardStates boardStates;

    public final static int FORCE_MASK = 3;

    public final static int FORCE_BITS = 2;

    public final static int FORCE_IGNORED = 0;

    public final static int FORCE_ON = 2;

    public final static int FORCE_OFF = 1;

    public HardState( int selfMetaState, BoardStates boardStates )
        {
        this.selfMetaState = selfMetaState;
        this.boardStates = boardStates;
        }

    /**
     * Flag of forced state.
     * Forced state does not appear on indicator keys, so getState() is not influenced.
     *
     *
     * overrides every setting, but it is active for only one hard-key.
     */
    private int forceFlag = FORCE_IGNORED;

    public void forceState( int forceFlag )
        {
        this.forceFlag = forceFlag & FORCE_MASK;
        checkStateChanges();
        }

    public void clearForceState( )
        {
        forceState( FORCE_IGNORED );
        }

    public boolean isStateActive()
        {
        return (forceFlag == FORCE_IGNORED) ? getState() != META_OFF :  forceFlag == FORCE_ON;
        }


    public void checkStateChanges()
        {
        if ( isStateActive() )
            {
            if ( !boardStates.isSimulatedMetaButtonPressed( selfMetaState ) )
                {
                Scribe.debug( selfMetaState + " hard state's button is pressed! " );
                boardStates.pressSimulatedMetaButton( selfMetaState );
                }
            }
        else
            {
            if ( boardStates.isSimulatedMetaButtonPressed( selfMetaState) )
                {
                Scribe.debug( selfMetaState + " hard state's button is released! ");
                boardStates.releaseSimulatedMetaButton( selfMetaState );
                }
            }
        }

    }

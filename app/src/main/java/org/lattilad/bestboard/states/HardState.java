package org.lattilad.bestboard.states;

import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;

/**
 * Meta-state for hard-keys. (SHIFT, CTRL, ALT)
 */
public class HardState extends MetaState
    {

    public final int selfMetaState;

    public final LayoutStates layoutStates;

    public final static int FORCE_MASK = 3;

    public final static int FORCE_BITS = 2;

    public final static int FORCE_IGNORED = 0;

    public final static int FORCE_ON = 2;

    public final static int FORCE_OFF = 1;

    public HardState( int selfMetaState, LayoutStates layoutStates)
        {
        this.selfMetaState = selfMetaState;
        this.layoutStates = layoutStates;
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
            if ( !layoutStates.isSimulatedMetaButtonPressed( selfMetaState ) )
                {
                Scribe.debug( Debug.HARDSTATE, selfMetaState + " hard state's button is pressed! " );
                layoutStates.pressSimulatedMetaButton( selfMetaState );
                }
            }
        else
            {
            if ( layoutStates.isSimulatedMetaButtonPressed( selfMetaState) )
                {
                Scribe.debug( Debug.HARDSTATE, selfMetaState + " hard state's button is released! ");
                layoutStates.releaseSimulatedMetaButton( selfMetaState );
                }
            }
        }

    }

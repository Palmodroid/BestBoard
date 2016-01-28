package dancingmoon.bestboard.states;

import android.os.SystemClock;
import android.view.KeyEvent;

import java.util.Arrays;

import dancingmoon.bestboard.parser.Commands;
import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.SoftBoardData;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.utils.ExtendedMap;

/**
 * Summary of the states
 * All meta-states including hard-states and caps-state are stored in this class.
 * This class stores the android meta-state information needed by sendKey... methods.
 *
 * Helper methods: hard-states should simulate meta-key events before and after hard-keys.
 * Actual meta-key status (sent out to the android system) is stored here.
 * !!These helper methods could be moved to the HardState class!!
 */
public class LayoutStates
    {

    /** Size of the whole meta-state array - including hard-states and caps-state */
    public static final int META_STATES_SIZE = 4;

    /** Size of the hard-states at the beginning of the meta-states array */
    public static final int HARD_STATES_SIZE = 3;

    /** SHIFT state - should below HARD_STATES_SIZE! */
    public static final int META_SHIFT = 0;
    /** CTRL state - should below HARD_STATES_SIZE! */
    public static final int META_CTRL = 1;
    /** ALT state - should below HARD_STATES_SIZE! */
    public static final int META_ALT = 2;

    /** CAPS state - comes AFTER hard-states */
    public static final int META_CAPS = HARD_STATES_SIZE;

    /** Mnemonic for released button's hardStateTime */
    public static final long RELEASED = -1L;

    /** Service for simulated hard-key presses */
    private SoftBoardData.SoftBoardListener softBoardListener;

    /**
     * All meta-states in a common array
     * First part: HARD STATES (HARD_STATES_SIZE)
     * Second part: CAPS STATE
     */
    public MetaState[] metaStates = new MetaState[META_STATES_SIZE];


    /**
     * Down-time of simulated hard-state buttons.
     * RELEASED (-1L) for non-active state.
     */
    private long hardStateTimes[] = new long[ LayoutStates.HARD_STATES_SIZE];

    /** Android meta-state masks for simulated hard-state buttons */
    private static int hardStateAndroidMasks[] = new int[HARD_STATES_SIZE];

    /** Android key-codes for simulated hard-state buttons */
    private static int hardStateAndroidCodes[] = new int[HARD_STATES_SIZE];

    public final static int ANDROID_META_STATE_OFF = 0;

    /**
     * Meta-state in android format
     * State is calculated by calculateAndroidMetaState()
     */
    private int androidMetaState;


    /**
     * Constructor populates arrays
     * @param softBoardListener ??????????
     */
    public LayoutStates(SoftBoardData.SoftBoardListener softBoardListener)
        {
        this.softBoardListener = softBoardListener;

        // First, HARD_STATE_SIZE part: HardStates
        for (int m = 0; m < HARD_STATES_SIZE; m++)
            {
            metaStates[m] = new HardState( m, this );
            }

        // Second part: CapsState
        metaStates[ META_CAPS ] = new CapsState();

        // Constants for the HardState section
        hardStateAndroidCodes[ META_SHIFT ] = KeyEvent.KEYCODE_SHIFT_LEFT;
        hardStateAndroidMasks[ META_SHIFT ] = KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;

        hardStateAndroidCodes[ META_CTRL ] = KeyEvent.KEYCODE_CTRL_LEFT;
        hardStateAndroidMasks[ META_CTRL ] = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;

        hardStateAndroidCodes[ META_ALT ] = KeyEvent.KEYCODE_ALT_LEFT;
        hardStateAndroidMasks[ META_ALT ] = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;

        // No meta-button press is simulated
        Arrays.fill( hardStateTimes, -1L );

        // Android meta-state is inactive
        androidMetaState = ANDROID_META_STATE_OFF;
        }

    /**
     * Returns meta-state in android format
     */
    public int getAndroidMetaState()
        {
        return androidMetaState;
        }

    /**
     * Calculates meta-state in android format
     * Meta-states of the pressed meta-keys are added
     */
    private void calculateAndroidMetaState()
        {
        androidMetaState = ANDROID_META_STATE_OFF;
        for ( int m = 0; m < LayoutStates.HARD_STATES_SIZE; m++ )
            {
            if ( isSimulatedMetaButtonPressed( m ) )
                androidMetaState |= hardStateAndroidMasks[m];
            }
        }


    /**
     * If meta-ctrl or meta-alt is active,
     * then PacketText is forced to send keyCodes instead of Strings.
     * @return true if keyCodes should be sent
     */
    public boolean isHardKeyForced()
        {
        return isSimulatedMetaButtonPressed( META_CTRL ) || isSimulatedMetaButtonPressed( META_ALT );
        }


    /**
     * Returns true if simulated meta-button press is active for this hard-state
     */
    public boolean isSimulatedMetaButtonPressed( int hardState )
        {
        return hardStateTimes[hardState] != RELEASED;
        }


    /**
     * Simulates meta-button press.
     * Sets and calculates android meta-state and the sent meta-buttons store.
     */
    public void pressSimulatedMetaButton( int hardState )
        {
        hardStateTimes[hardState] = SystemClock.uptimeMillis();

        // android meta-state is needed for the simulated press ??
        calculateAndroidMetaState();

        softBoardListener.sendKeyDown(
                hardStateTimes[hardState],
                hardStateAndroidCodes[hardState] );
        }


    /**
     * Simulates meta-button release.
     * Sets and calculates android meta-state and the sent meta-buttons store.
     */
    public void releaseSimulatedMetaButton( int hardState )
        {
        softBoardListener.sendKeyUp(
                hardStateTimes[hardState], SystemClock.uptimeMillis(), hardStateAndroidCodes[hardState] );
        hardStateTimes[hardState] = RELEASED;

        calculateAndroidMetaState();
        }

    public void resetSimulatedMetaButtons()
        {
        for ( int m = 0; m < HARD_STATES_SIZE; m++ )
            {
            if ( hardStateTimes[m] == RELEASED )
                softBoardListener.sendKeyUp(
                        SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), hardStateAndroidCodes[m] );
            else
                softBoardListener.sendKeyDown(
                        hardStateTimes[m], hardStateAndroidCodes[m] );
            }
        }


    /**
     * Helper method for creating PacketKey. (SoftBoardData.Packet)
     * Forced hard-states are stored in binary format (instead of three objects).
     * IGNORE: 0, ON: 2, OFF: 1 - as defined in HardState
     * Shift-Ctrl-Alt states are stored on six bits as: AACCSS
     * @param parameters PacketKey tokens
     * @return binaryHardState
     */
    public static int generateBinaryHardState( ExtendedMap<Long, Object> parameters )
        {
        // AACCSS
        int binaryHardState = 0;
        Boolean temp;

        temp = (Boolean) parameters.get( Commands.TOKEN_FORCEALT );
        if ( Boolean.FALSE.equals( temp ) ) binaryHardState |= HardState.FORCE_OFF;
        if ( Boolean.TRUE.equals( temp ) ) binaryHardState |= HardState.FORCE_ON;

        Scribe.debug( Debug.BOARDSTATE,  "Binary Hard state ALT added: " + Integer.toBinaryString( binaryHardState ) );

        binaryHardState <<= HardState.FORCE_BITS;

        temp = (Boolean) parameters.get( Commands.TOKEN_FORCECTRL );
        if ( Boolean.FALSE.equals( temp ) ) binaryHardState |= HardState.FORCE_OFF;
        if ( Boolean.TRUE.equals( temp ) ) binaryHardState |= HardState.FORCE_ON;

        Scribe.debug( Debug.BOARDSTATE,  "Binary Hard state CTRL added: " + Integer.toBinaryString( binaryHardState ) );

        binaryHardState <<= HardState.FORCE_BITS;

        temp = (Boolean) parameters.get( Commands.TOKEN_FORCESHIFT );
        if ( Boolean.FALSE.equals( temp ) ) binaryHardState |= HardState.FORCE_OFF;
        if ( Boolean.TRUE.equals( temp ) ) binaryHardState |= HardState.FORCE_ON;

        Scribe.debug( Debug.BOARDSTATE,  "Binary Hard state SHIFT added, ready: " + Integer.toBinaryString( binaryHardState ) );

        return binaryHardState;
        }


    /**
     * Helper method for PacketKey to force hard-states.
     * (Called by SoftBoardService.sendKeyUpDown() before sending keycode.)
     * Forced hard-states are stored in binary format (instead of three objects).
     * IGNORE: 0, ON: 2, OFF: 1 - as defined in HardState
     * Shift-Ctrl-Alt states are stored on six bits as: AACCSS
     */
    public void forceBinaryHardState( int binaryHardState )
        {
        for ( int m = 0; m < HARD_STATES_SIZE; m++ )
            {
            ( (HardState) metaStates[m] ).forceState( binaryHardState );
            binaryHardState >>= HardState.FORCE_BITS;
            }
        }


    /**
     * Helper method for PacketKey to clear forced hard-states.
     * (Called by SoftBoardService.sendKeyUpDown() after sending keycode.)
     */
    public void clearBinaryHardState()
        {
        for ( int m = 0; m < HARD_STATES_SIZE; m++ )
            {
            ( (HardState) metaStates[m] ).clearForceState();
            }
        }

    }

package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.utils.HardKey;


/**
 * Hard-key (char hardKeyCode) data to be sent to the editor
 */
public class PacketKey extends Packet
    {
    /** Hard-key hardKeyCode of the key */
    private char hardKeyCode;

    private int binaryHardState;


    /**
     *Creator of hard-key button data
     * @param softBoardData general keyboard data class
     * @param combinedKeyCode hard-key data as (ascii char) or (0x10000 & hard-key code)
     */
    public PacketKey( SoftBoardData softBoardData, int combinedKeyCode, int binaryHardState )
        {
        super( softBoardData, null );
        this.hardKeyCode = (combinedKeyCode > 0xFFFF) ? (char)(combinedKeyCode & 0xFFFF)
                : HardKey.convertFromAscii( (char) combinedKeyCode);
        this.binaryHardState = binaryHardState;
        setTitleString( HardKey.getString(hardKeyCode) );
        }


    @Override
    public void send()
        {
        send( true );
        }


    public boolean sendIfNoMeta()
        {
        return send( false );
        }


     /**
      * Force hardStates as prescribed in binaryHardState, then
      * Simulates a key-down/key-up sequence depending on meta-state, then
      * Turns off forced hardStates.
      * If anyMeta is true, then key will be sent in all hard-states,
      * if anymeta is false, then key will be sent only when hard-states are NOT completely off.
      * Input-connection availability is not returned!
      * Batch edit could be implemented ??
      * @param anyMeta if false, then key will be sent only when hard-states are NOT completely off.
      * @return true if key was sent, false when not
      */
    private boolean send( boolean anyMeta )
        {
        boolean sent = false;

        // In most cases forced state is IGNORED
        if ( binaryHardState != 0 )
            softBoardData.layoutStates.forceBinaryHardState( binaryHardState );

        // If no meta is set or forced, then android meta state remains 0
        if ( anyMeta || softBoardData.layoutStates.isAnyHardMetaActive() )
            {
            softBoardData.softBoardListener.sendKeyDownUp(hardKeyCode);
            sent = true;
            }

        // In most cases forced state is IGNORED
        if ( binaryHardState != 0 )
            softBoardData.layoutStates.clearBinaryHardState();

        return sent;
        }
    }

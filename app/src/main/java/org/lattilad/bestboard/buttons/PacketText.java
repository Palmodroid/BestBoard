package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.utils.HardKey;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Base of text-packets, which send textual data (String) to the editor
 */
public abstract class PacketText extends Packet
    {
    protected PacketText(SoftBoardData softBoardData)
        {
        super(softBoardData);
        }

    protected PacketText(SoftBoardData softBoardData, String titleText)
        {
        super(softBoardData, titleText);
        }

    /** String of the packet. Characters are defined as Strings,
     because only String can be sent */
    protected abstract String getString();

    /** Movement of cursor can be set after sending text
     PacketField uses it */
    // protected int movement = 0; // FIELD is vorbidden, use of movement is erased

    /** Autocaps command, delivered after data was sent */
    private int autoCaps = CapsState.AUTOCAPS_OFF;

    /**
     * All characters are uppercase if true
     * always true for originally character data
     */
    private boolean stringCaps = false;


    public static final int AUTO_SPACE_BEFORE = 1;
    public static final int AUTO_SPACE_AFTER = 2;
    public static final int ERASE_SPACES_BEFORE = 4;
    public static final int ERASE_SPACES_AFTER = 8;

    /**
     * AutoSpace function, flags stored on the first 4 bits
     */
    private int autoSpace = 0;

    /**
     * Temporary variable to store uppercase status during one cycle
     * It can be static, because only one needed at a time
     * 0 - no caps
     * 1 - first caps (only if stringcaps == false)
     * 2 - all caps
     */
    private static int capsState = 0;
    private static boolean twinState = false;

    // String cannot be null from coat.descriptor (only from direct definition)
    // If string is not valid in coat.descriptor, then no OneParameter is set
    // Without Text Parameter empty Button is set and not ButtonPacket/PacketTextSimple

    /**
     * Creator of textual button data - String type
     * Creator of textual button data - Character type
     * Currently PARAMETER_TEXT accepts String, Character and Integer data
     * Character and Integer are treated and are sent as char.
     * This behavior can be changed in SoftBoardParser.ParseOneParameter()
     * param softBoardData general keyboard data class
     * param string textual data as String cannot be null, but will be not null from caot.descriptor
     * param autoCaps CapsState.AUTOCAPS_OFF, _ON, _WAIT, _HOLD
     * param stringCaps true if all characters should be uppercase
     * param autoSpace autospace functionality
     */

    public void setAutoCaps( int autoCaps )
        {
        this.autoCaps = autoCaps;
        }

    public void setStringCaps( boolean stringCaps )
        {
        this.stringCaps = stringCaps;
        }

    public void setAutoSpace( int autoSpace )
        {
        this.autoSpace = autoSpace;
        }


    /**
     * If text-packet children do not provide title-string, then getString() will be used.
     * @return
     */
    @Override
    public String getTitleString()
        {
        return super.getTitleString() == null ? getString() : super.getTitleString();
        }


    private void sendString( )
        {
        String stringToSend;

        if ( twinState && getString().length() == 1 )
            {
            stringToSend = new StringBuilder().append(getString()).append(getString()).toString();
            }
        else
            {
            stringToSend = getString();
            }

        switch ( capsState )
            {
            case 0:
                softBoardData.softBoardListener.sendString(
                        stringToSend, autoSpace );
                break;

            case 1:
                softBoardData.softBoardListener.sendString(
                        StringUtils.toUpperOnlyFirst( stringToSend, softBoardData.locale ), autoSpace );
                break;

            default: // case 2:
                // http://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
                // http://stackoverflow.com/questions/26515060/why-java-character-touppercase-tolowercase-has-no-locale-parameter-like-string-t
                softBoardData.softBoardListener.sendString(
                        stringToSend.toUpperCase( softBoardData.locale ), autoSpace );
            }
        }

    @Override
    public void send()
        {
        if ( softBoardData.layoutStates.isHardKeyForced() )
            {
            if ( getString().length() == 1 )
                {
                softBoardData.softBoardListener.sendKeyDownUp(HardKey.convertFromAscii(getString().charAt(0)));
                }
            }

        else
            {
            twinState = false;

            // state can be: META_OFF (no upper) IN_TOUCH META_ON AUTOCAPS_ON (first upper) META_LOCK (all upper)
            int state = softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS].getState();
            if ( state == CapsState.META_OFF )
                {
                capsState = 0;
                }
            else if ( state == CapsState.META_LOCK )
                {
                capsState = 2;
                }
            else if ( state == CapsState.AUTOCAPS_ON && !softBoardData.autoFuncEnabled )
                {
                capsState = 0;
                }
            else // state == IN_TOUCH || META_ON || AUTOCAPS_ON with enabled autoFunc
                {
                capsState = stringCaps ? 2 : 1;
                }

            sendString( );
            }
        }

    @Override
    public void sendSecondary( int second )
        {
        if ( softBoardData.softBoardListener.undoLastString() )
            {
            if ( second == TWIN )
                twinState = !twinState;
            else
                {
                capsState++;
                if (capsState == 1 && stringCaps)   capsState = 2;
                else if (capsState > 2)             capsState = 0;
                }
            sendString( );
            }
        }

    @Override
    public void release()
        {
        // If needed, this could be a standalone method, called when touch releases the button
        ( (CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS] )
                .setAutoCapsState( autoCaps, softBoardData.autoFuncEnabled );
        Scribe.debug(Debug.TEXT, "PacketTextSimple released, autocaps state is set to " +
                softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS].getState());
        }
    }


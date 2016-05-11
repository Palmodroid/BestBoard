package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.utils.HardKey;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Textual (String) data to be sent to the editor
 */
public class PacketText extends Packet
    {
    /** String of the key. Characters are defined as Strings,
     because only String can be sent */
    protected String string;

    /** Movement of cursor can be set after sending text
     PacketField uses it */
    protected int movement = 0; // FIELD is vorbidden, use of movement is erased

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
    // Without Text Parameter empty Button is set and not ButtonPacket/PacketText

    /**
     * Creator of textual button data - String type
     * @param softBoardData general keyboard data class
     * @param string textual data as String cannot be null, but will be not null from caot.descriptor
     * @param autoCaps CapsState.AUTOCAPS_OFF, _ON, _WAIT, _HOLD
     * @param stringCaps true if all characters should be uppercase
     * @param autoSpace autospace functionality
     */
    public PacketText(SoftBoardData softBoardData, String string, int autoCaps, boolean stringCaps, int autoSpace)
        {
        super(softBoardData);
        this.string = string;
        this.autoCaps = autoCaps;
        this.stringCaps = stringCaps;
        this.autoSpace = autoSpace;
        }

    /**
     * Creator of textual button data - Character type
     * Currently PARAMETER_TEXT accepts String, Character and Integer data
     * Character and Integer are treated and are sent as char.
     * This behavior can be changed in SoftBoardParser.ParseOneParameter()
     * @param softBoardData general keyboard data class
     * @param character textual data as Character cannot be null, but will be not null from caot.descriptor
     * @param autoCaps CapsState.AUTOCAPS_OFF, _ON, _WAIT, _HOLD
     * @param autoSpace autospace functionality
     */
    public PacketText(SoftBoardData softBoardData, Character character, int autoCaps, int autoSpace )
        {
        this(softBoardData, character.toString(), autoCaps, true, autoSpace);
        }


    // ?? String.valueOf(character) ?? constructor - does it need this ??


    @Override
    public String getString()
        {
        return string;
        }

    public void setString( String string )
        {
        this.string = string;
        }

    private void sendString( )
        {
        String stringToSend;

        if ( twinState && string.length() == 1 )
            {
            stringToSend = new StringBuilder().append(string).append(string).toString();
            }
        else
            {
            stringToSend = string;
            }

        switch ( capsState )
            {
            case 0:
                softBoardData.softBoardListener.sendString(
                        stringToSend, autoSpace );
                break;

            case 1:
                softBoardData.softBoardListener.sendString(
                        StringUtils.toSentenceCase( stringToSend, softBoardData.locale ), autoSpace );
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
            if ( string.length() == 1 )
                {
                softBoardData.softBoardListener.sendKeyDownUp(HardKey.convertFromAscii(string.charAt(0)));
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
            else if ( state == CapsState.META_LOCK || stringCaps )
                {
                capsState = 2;

                }
            else // state == IN_TOUCH || META_ON || AUTOCAPS_ON
                {
                capsState = 1;

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
        if (softBoardData.autoFuncEnabled)
        	( (CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS] ).setAutoCapsState( autoCaps );
        Scribe.debug(Debug.TEXT, "PacketText released, autocaps state is set.");
        }
    }


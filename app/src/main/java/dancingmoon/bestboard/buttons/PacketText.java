package dancingmoon.bestboard.buttons;

import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.CapsState;
import dancingmoon.bestboard.SoftBoardData;
import dancingmoon.bestboard.utils.HardKey;
import dancingmoon.bestboard.utils.StringUtils;

/**
 * Textual (String) data to be sent to the editor
 */
public class PacketText extends Packet
    {
    /** String of the key. Characters are defined as Strings,
     because only String can be sent */
    private String string;

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
     * Currently PARAMTER_TEXT accepts String, Character and Integer data
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

    @Override
    public void send()
        {
        if ( softBoardData.boardStates.isHardKeyForced() )
            {
            if ( string.length() == 1 )
                {
                softBoardData.softBoardListener.sendKeyDownUp( HardKey.convertFromAscii( string.charAt( 0 ) ));
                }
            }

        else
            {
            // state can be: META_OFF (no upper) IN_TOUCH META_ON AUTOCAPS_ON (first upper) META_LOCK (all upper)
            int state = softBoardData.boardStates.metaStates[BoardStates.META_CAPS].getState();
            if ( state == CapsState.META_OFF )
                {
                softBoardData.softBoardListener.sendString(
                        string, autoSpace );
                }
            else if ( state == CapsState.META_LOCK || stringCaps )
                {
                // http://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case
                // http://stackoverflow.com/questions/26515060/why-java-character-touppercase-tolowercase-has-no-locale-parameter-like-string-t
                softBoardData.softBoardListener.sendString(
                        string.toUpperCase( softBoardData.locale ), autoSpace );
                }
            else // state == IN_TOUCH || META_ON || AUTOCAPS_ON
                {
                softBoardData.softBoardListener.sendString(
                        StringUtils.toUpperFirst( string, softBoardData.locale ), autoSpace );
                }

            // If needed, this could be a standalone method, called when touch releases the button
            ( (CapsState) softBoardData.boardStates.metaStates[BoardStates.META_CAPS] ).setAutoCapsState( autoCaps );
            }
        }
    }

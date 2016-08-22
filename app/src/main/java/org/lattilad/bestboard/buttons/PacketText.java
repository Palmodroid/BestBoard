package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Textual (String) data to be sent to the editor
 */
public class PacketText extends PacketTextBase
    {
    /** String of the packet. Characters are defined as Strings,
     because only String can be sent */
    protected String string;


    /** String is needed by the super-class */
    protected String getString()
        {
        return string;
        }


    /**
     * Some button classes change the string
     * @param string
     */
    public void setString( String string )
        {
        this.string = string;
        }


    /**
     * Creator of textual button data - String type
     * @param softBoardData general keyboard data class
     * @param string textual data as String cannot be null, but will be not null from caot.descriptor
     */
    public PacketText( SoftBoardData softBoardData, String string )
        {
        super(softBoardData);
        this.string = string;
        // StringCaps for characters should be true, but this can be changed by the coat file
        // !! This should come into PacketTextBase,send()
        if ( string.length() == 1 )
            setStringCaps( true );
        }

    /**
     * Creator of textual button data - Character type
     * Currently PARAMETER_TEXT accepts String, Character and Integer data
     * Character and Integer are treated and are sent as char.
     * This behavior can be changed in SoftBoardParser.ParseOneParameter()
     * @param softBoardData general keyboard data class
     * @param character textual data as Character cannot be null, but will be not null from caot.descriptor
     */
    public PacketText( SoftBoardData softBoardData, Character character )
        {
        this(softBoardData, character.toString());
        }

    }


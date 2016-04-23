package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Special Text, where cursor is moved back to the position of the last '\c'
 */
public class PacketField extends PacketText
    {
    // movement should be inside PacketText
    int movement = 0;

    public PacketField(SoftBoardData softBoardData, String string, int autoCaps, boolean stringCaps, int autoSpace)
        {
        super(softBoardData, string, autoCaps, stringCaps, autoSpace);

        int position = string.lastIndexOf('*');
        if ( position >= 0 )
            {
            StringBuilder builder = new StringBuilder( string );
            builder.deleteCharAt( position );
            setString(builder.toString());
            movement = position - getString().length();
            }
        }

    public PacketField(SoftBoardData softBoardData, Character character, int autoCaps, int autoSpace )
        {
        super(softBoardData, character, autoCaps, autoSpace);
        }
    }

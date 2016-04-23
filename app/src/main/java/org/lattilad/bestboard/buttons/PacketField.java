package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Special Text, where cursor is moved back to the position of the last '\c'
 */
public class PacketField extends PacketText
    {
    public PacketField(SoftBoardData softBoardData, String string, int autoCaps, boolean stringCaps, int autoSpace)
        {
        super(softBoardData, string, autoCaps, stringCaps, autoSpace);

        int position = string.lastIndexOf('*');
        if ( position >= 0 )
            {
            StringBuilder builder = new StringBuilder( string );
            builder.deleteCharAt( position );
            this.string = builder.toString();
            this.movement = position - this.string.length();
            }
        }
    }

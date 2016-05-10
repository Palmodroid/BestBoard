package org.lattilad.bestboard.buttons;

import android.content.Intent;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Loads and parses a new coat file temporarily
 */
public class PacketLoad extends Packet
    {
    String coatFileName;
    String name;

    public PacketLoad( SoftBoardData softBoardData, String coatFileName )
        {
        super( softBoardData );
        this.coatFileName = coatFileName;
        name = StringUtils.abbreviateString(coatFileName, 5);
        }

    /**
     * String representation of the data (title)
     * @return String representation of this packet
     */
    @Override
    public String getString()
        {
        return name;
        }

    /**
     * Send data to the editor field
     */
    @Override
    public void send()
        {
        softBoardData.softBoardListener.startSoftBoardParser( coatFileName );
        }

    @Override
    public void release()
        {
        }

    }

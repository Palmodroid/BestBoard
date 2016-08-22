package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Loads and parses a new coat file temporarily
 */
public class PacketLoad extends Packet
    {
    String coatFileName;

    public PacketLoad( SoftBoardData softBoardData, String coatFileName )
        {
        super( softBoardData );
        this.coatFileName = coatFileName;
        setTitleString( StringUtils.abbreviateString(coatFileName, 5) );
        }

    /**
     * Send data to the editor field
     */
    @Override
    public void send()
        {
        softBoardData.softBoardListener.startSoftBoardParser( coatFileName );
        }

    }

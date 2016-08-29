package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

/**
 * Combined text/function and key packet
 * If any of the hard states (shift/ctr/alt) is active, then sends the packet key,
 * while there are no hard state set, then sends the first packet (which is function or text)
 */
public class PacketCombine extends Packet
    {
    Packet packetFirst;
    PacketKey packetKey;

    boolean packetFirstSent = false;


    public PacketCombine( SoftBoardData softBoardData, Packet packetFirst, PacketKey packetKey )
        {
        super( softBoardData );
        // title-string is NOT set, because it will use the title-string of the first packet
        this.packetFirst = packetFirst;
        this.packetKey = packetKey;
        }

    /**
     * original getTitleString should be overridden, because title-string of a packet can change
     * @return
     */
    @Override
    public String getTitleString()
        {
        return packetFirst.getTitleString();
        }

    @Override
    public boolean isTitleStringChanging()
        {
        return packetFirst.isTitleStringChanging();
        }

    @Override
    public void send()
        {
        if (!softBoardData.layoutStates.isAnyHardMetaActive())
            {
            packetFirst.send();
            packetFirstSent = true;
            }
        else
            {
            packetKey.send();
            }
        }

    @Override
    public void sendSecondary( int second )
        {
        if (!softBoardData.layoutStates.isAnyHardMetaActive())
            {
            packetFirst.sendSecondary( second );
            packetFirstSent = true;
            }
        // NO sendSecondary for packetKey
        }

    @Override
    public void release()
        {
        // Both text and function packets should be released
        if ( packetFirstSent )
            {
            packetFirst.release();
            packetFirstSent = false;
            }
        }
    }

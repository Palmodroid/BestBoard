package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.utils.SoundMaker;

/**
 * PacketTextCaps and plays a tone
 */
public class PacketTextCapsTone extends PacketTextCaps
    {
    private int tone;

    public PacketTextCapsTone( SoftBoardData softBoardData, String string, int tone )
        {
        super(softBoardData, string);
        this.tone = tone;
        }

    public PacketTextCapsTone(SoftBoardData softBoardData, Character character, int tone )
        {
        super(softBoardData, character);
        this.tone = tone;
        }

    @Override
    public void send()
        {
        SoundMaker.start( tone );
        super.send();
        }

    /* What to do with tone here??
    @Override
    public void sendSecondary( int second )
        {
        } */

    @Override
    public void release()
        {
        SoundMaker.stop();
        super.release();
        }

    }

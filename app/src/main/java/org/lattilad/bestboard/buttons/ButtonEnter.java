package org.lattilad.bestboard.buttons;


import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.SoftBoardMarker;
import org.lattilad.bestboard.scribe.Scribe;

public class ButtonEnter extends ButtonMainTouch implements Cloneable
    {
    private PacketKey packetKey;
    private PacketText packetText;
    private boolean repeat;

    @Override
    public ButtonEnter clone()
        {
        return (ButtonEnter)super.clone();
        }

    public ButtonEnter(PacketKey packetKey, PacketText packetText, boolean repeat)
        {
        this.packetKey = packetKey;
        this.packetText = packetText;
        this.repeat = repeat;
        if ( repeat )
            setOnStay();
        }

    public int defaultTitleType()
        {
        return SoftBoardMarker.ENTER_ACTION_MARKER;
        }

    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        fire();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        { }

    /**
     * Methods should implement secondary functionality here.
     *
     * @param type of the activation: ON_STAY (-1) ON_CIRCLE (1) or ON_HARD_PRESS (2)
     * @return true if button could be repeated quickly (repeat)
     * or false if button should wait for next "on stay" trigger
     * (needed only if type is ON_STAY)
     */
    @Override
    public boolean fireSecondary(int type)
        {
        if ( repeat )
            {
            fire();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_REPETED);
            return true;
            }
        return false;
        }

    private void fire( )
        {
        if ( layout.softBoardData.isActionSupplied() )
            {
            // fromEnterKey parameter is useless, because multiline actions are performed separately
            // ?? What to do with repeat here ??
            if ( !layout.softBoardData.softBoardListener.sendDefaultEditorAction( true ) )
                {
                Scribe.error( "ENTER: default enterAction was not accepted by editor!" );
                }
            }

        // No enterAction is defined
        else
            {
            // editor
            if ( layout.softBoardData.enterAction == SoftBoardData.ACTION_MULTILINE )
                {
                if ( !packetKey.sendIfNoMeta() )    // if any meta is turned on - send HARD-KEY
                    {
                    packetText.send();              // if all meta is turned off - send TEXT
                    packetText.release();           // autocaps should be set
                    }
                }
            // simulated hard-key
            else // ACTION_UNSPECIFIED or ACTION_NONE
                {
                packetKey.send();                   // No enterAction - send HARD-KEY anyway
                }
            }
        }
    }

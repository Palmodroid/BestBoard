package org.lattilad.bestboard.buttons;


import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.scribe.Scribe;

public class ButtonEnter extends ButtonMainTouchTitles implements
        Button.ChangingButton, Cloneable
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
        }

    @Override
    public String getChangingString()
        {
        return layout.softBoardData.getActionTitle();
        }

    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        fire();
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        { }

    @Override
    public void mainTouchOnCircle( boolean isHardPress )
        { }

    @Override
    public boolean mainTouchOnStay()
        {
        if ( repeat )
            {
            fire();
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
                Scribe.error( "ENTER: default action was not accepted by editor!" );
                }
            }

        // No action is defined
        else
            {
            // editor
            if ( layout.softBoardData.action == SoftBoardData.ACTION_MULTILINE )
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
                packetKey.send();                   // No action - send HARD-KEY anyway
                }
            }
        }
    }

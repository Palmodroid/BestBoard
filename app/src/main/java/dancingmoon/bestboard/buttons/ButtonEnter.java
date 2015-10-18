package dancingmoon.bestboard.buttons;


import dancingmoon.bestboard.SoftBoardData;
import dancingmoon.bestboard.scribe.Scribe;

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
        return board.softBoardData.getActionTitle();
        }

    /**
     * Button performs its action by fire method.
     * It is called at several phases by Board.evaluateTouch()
     * @param phase
     */
    @Override
    public void mainTouchEvent( int phase )
        {
        if ( board.softBoardData.isActionSupplied() )
            {
            if ( phase == MAIN_START || phase == MAIN_DOWN )
                {
                // fromEnterKey parameter is useless, because multiline actions are performed separately
                if ( !board.softBoardData.softBoardListener.sendDefaultEditorAction( true ) )
                    {
                    Scribe.error( "ENTER: default action was not accepted by editor!" );
                    }
                }
            }
        // No action is defined
        else if ( phase == MAIN_START || phase == MAIN_DOWN ||
                    ( phase == MAIN_REPEAT && repeat ) )
            {
            // editor
            if ( board.softBoardData.action == SoftBoardData.ACTION_MULTILINE )
                {
                if ( !packetKey.sendIfNoMeta() )    // if any meta is turned on - send HARD-KEY
                    {
                    packetText.send();              // if all meta is turned off - send TEXT
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

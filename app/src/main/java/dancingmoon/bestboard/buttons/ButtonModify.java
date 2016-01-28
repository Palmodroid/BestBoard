package dancingmoon.bestboard.buttons;

import dancingmoon.bestboard.R;
import dancingmoon.bestboard.parser.Tokenizer;
import dancingmoon.bestboard.modify.Modify;
import dancingmoon.bestboard.scribe.Scribe;

public class ButtonModify extends ButtonMainTouch implements Cloneable
    {
    private long modifyId;
    private boolean reverse;

    @Override
    public ButtonModify clone()
        {
        return (ButtonModify)super.clone();
        }

    public ButtonModify( long modifyId, boolean reverse )
        {
        this.modifyId = modifyId;
        this.reverse = reverse;
        }

    public String getString()
        {
        return reverse ? "REV" : "MOD";
        }

    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        Modify modify = layout.softBoardData.modify.get( modifyId );
        if ( modify != null )
            {
            modify.change( reverse );
            }
        else
            {
            // Error message should mimic tokenizer error
            Scribe.error_secondary(
                    "[RUNTIME ERROR] " +
                            layout.softBoardData.softBoardListener.getApplicationContext().getString( R.string.modify_missing ) +
                            Tokenizer.regenerateKeyword( modifyId ) );
            }
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
        return false;
        }

    }

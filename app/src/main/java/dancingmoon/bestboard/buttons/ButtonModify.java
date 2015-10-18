package dancingmoon.bestboard.buttons;

import dancingmoon.bestboard.R;
import dancingmoon.bestboard.SoftBoardParser;
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
    public void mainTouchEvent(int phase)
        {
        if ( phase == MAIN_START || phase == MAIN_DOWN )
            {
            Modify modify = board.softBoardData.modify.get( modifyId );
            if ( modify != null )
                {
                if ( reverse )
                    modify.changeBack();
                else
                    modify.change();
                }
            else
                {
                // Error message should mimic tokenizer error
                Scribe.error_secondary(
                        "[RUNTIME ERROR] " +
                        board.softBoardData.softBoardListener.getApplicationContext().getString( R.string.modify_missing ) +
                        SoftBoardParser.regenerateKeyword( modifyId ) );
                }
            }
        }
    }

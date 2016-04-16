package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.modify.Modify;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;

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

    public String getFirstString()
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
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
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
    public boolean fireSecondary(int type)
        {
        return false;
        }
    }

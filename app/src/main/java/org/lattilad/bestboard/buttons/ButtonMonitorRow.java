package org.lattilad.bestboard.buttons;

import android.content.Context;
import android.content.Intent;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.monitorrow.TestModeActivity;

/**
 * Monitor Row
 */
public class ButtonMonitorRow extends ButtonMainTouchInvisible implements Cloneable
    {
    /*
    Normally secondary functions undo primary functions.
    Undo is not possible here, so primary will fire only if secondary hasn't.
     */
    private boolean secondaryFired = false;

    @Override
    public ButtonMonitorRow clone()
        {
        return (ButtonMonitorRow) super.clone();
        }

    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( secondaryFired )
            secondaryFired = false;
        else
            {
            Intent intent = new Intent(layout.softBoardData.softBoardListener.getApplicationContext(),
                    TestModeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            layout.softBoardData.softBoardListener.getApplicationContext().startActivity(intent);
            // packet.send();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        }

    @Override
    public boolean fireSecondary(int type)
        {
        secondaryFired = true;

        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);

        // ** !! These methods should come to a common place
        Context context = layout.softBoardData.softBoardListener.getApplicationContext();
        TestModeActivity.toggleTestMode( context );

        return false;
        }

    }

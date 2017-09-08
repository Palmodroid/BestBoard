package org.lattilad.bestboard.buttons;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.monitorrow.MonitorRowActivity;

import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_TEST_LOAD;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_TEST_RETURN;
import static org.lattilad.bestboard.prefs.PrefsFragment.performAction;

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
                    MonitorRowActivity.class);
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

        // ** !! These methods should come to a common place
        Context context = layout.softBoardData.softBoardListener.getApplicationContext();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );
        SharedPreferences.Editor editor = sharedPrefs.edit();

        boolean testMode = sharedPrefs.getBoolean( context.getString(R.string.test_mode_key),
                context.getResources().getBoolean(R.bool.test_mode_default));

        if ( testMode )
            {
            editor.putBoolean(context.getString(R.string.test_mode_key), false);
            editor.apply();
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);

            performAction( context, PREFS_ACTION_TEST_RETURN);
            }
        else
            {
            String testFileName =
                    sharedPrefs.getString(context.getString(R.string.test_file_key),
                            context.getString(R.string.test_file_default));

            if (!testFileName.isEmpty())
                {
                editor.putBoolean(context.getString(R.string.test_mode_key), true);
                editor.apply();
                layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);

                performAction( context, PREFS_ACTION_TEST_LOAD);
                }

            }

        return false;
        }

    }

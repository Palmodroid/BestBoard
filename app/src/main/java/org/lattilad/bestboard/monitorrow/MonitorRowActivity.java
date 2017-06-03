package org.lattilad.bestboard.monitorrow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.prefs.PrefsActivity;

import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_RELOAD;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_COUNTER;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_TYPE;

/**
 * startActivity(new Intent(getApplicationContext(), PermissionRequestActivity.class));
 * Process: org.lattilad.bestboard, PID: 31817
 android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
 */

public class MonitorRowActivity extends AppCompatActivity
    {

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.monitor_row_activity);
        //getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFinishOnTouchOutside(false);

        ((EditTextWithBackButton) findViewById(R.id.editText)).setOnBackButtonListener(new EditTextWithBackButton.OnBackButtonListener()
            {
            @Override
            public boolean onBackButton()
                {
                finish();
                return true;
                }
            });

        ((Button) findViewById( R.id.draft_button )).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                // Same communication as in Prefs
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putInt( PREFS_COUNTER, sharedPrefs.getInt( PREFS_COUNTER, 0 ) + 1 );
                editor.putInt( PREFS_TYPE, PREFS_ACTION_RELOAD );

                editor.apply();
                finish();
                }
            });

        ((Button) findViewById( R.id.set_button )).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Intent intent = new Intent( getApplicationContext(), PrefsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // http://stackoverflow.com/a/36841529
                //intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity(intent);
                finish();
                }
            });
        }

    }

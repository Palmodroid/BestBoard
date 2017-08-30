package org.lattilad.bestboard.monitorrow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.fileselector.FileSelectorActivity;
import org.lattilad.bestboard.prefs.PrefsActivity;

import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_TEST_LOAD;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_TEST_RETURN;
import static org.lattilad.bestboard.prefs.PrefsFragment.performAction;

/**
 * startActivity(new Intent(getApplicationContext(), PermissionRequestActivity.class));
 * Process: org.lattilad.bestboard, PID: 31817
 * android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
 *
 * TEST:
 * Preferences:
 * test_mode, test_file, test_selecet
 *
 * TEST_MODE is always started from outside (by prefs)
 * SoftBoardService.startSoftBoardParser() decides on the value of the preference
 * storedSoftBoardData is located at service
 * softBoardData is given immediately to SoftBoardProcessor
 *
 * THIS IS IMPORTANT
 * Entering MAIN mode: always clears stored data
 * Entering TEST mode: (only in TEST_LOAD action) stores MAIN mode, if it was not stored
 * RELOAD - reloads without knowing stored state
 *
 * Long click:
 *      Selects file for testing
 *      Stores main data (if it was not stored already)
 *      Sets testing bit
 *      Reloads test file
 * Short click:
 *      If no file is selected yet - selects file for testing
 *      Otherwise, same as Long Click
 *
 * MAIN - visible only in testing mode:
 *      Clears testing bit
 *      Restores (if data was stored) otherwise reloads main file
 *
 * Actions: CANNOT SET BIT, BECAUSE OF ENDLESS PREFS LOOP!!
 * ACTION_TEST_LOAD:
 * if not yet stored stores data
 * reloads
 *
 * ACTION_TEST_RETURN:
 * if data is stored - restores it, clears store-variable
 * if not - reloads
 */

public class MonitorRowActivity extends AppCompatActivity
    {
    private static final int TEST_SELECTOR_REQUEST = 2;

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

        Button testButton = ((Button) findViewById(R.id.test_button));
        testButton.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String testFileName =
                        sharedPrefs.getString(getString(R.string.test_file_key),
                                getString(R.string.test_file_default));
                if ( testFileName.isEmpty() )
                    selectTestFile();
                else
                    loadTestFile();
                }
            });

        testButton.setOnLongClickListener(new View.OnLongClickListener()
            {
            @Override
            public boolean onLongClick(View v)
                {
                selectTestFile();
                return true;
                }
            });


        Button mainButton = ((Button) findViewById(R.id.main_button));
        mainButton.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putBoolean(getString(R.string.test_mode_key), false);

                editor.apply();

                performAction(getApplicationContext(), PREFS_ACTION_TEST_RETURN);
                finish();
                }
            });

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( this );
        boolean testMode = sharedPrefs.getBoolean(getString(R.string.test_mode_key),
                getResources().getBoolean(R.bool.test_mode_default));

        mainButton.setVisibility( testMode ? View.VISIBLE : View.GONE );


        ((Button) findViewById(R.id.set_button)).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Intent intent = new Intent(getApplicationContext(), PrefsActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // http://stackoverflow.com/a/36841529
                //intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity(intent);
                finish();
                }
            });
        }

    void selectTestFile()
        {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String directoryName =
                sharedPrefs.getString(getString(R.string.descriptor_directory_key),
                        getString(R.string.descriptor_directory_default));
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), FileSelectorActivity.class);
        intent.putExtra(FileSelectorActivity.DIRECTORY_SUB_PATH, directoryName);
        intent.putExtra(FileSelectorActivity.ONE_DIRECTORY, true);
        // intent.putExtra( FileSelectorActivity.FILE_ENDING, ".txt");
        startActivityForResult(intent, TEST_SELECTOR_REQUEST);
        }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        if (requestCode == TEST_SELECTOR_REQUEST)
            {
            if (resultCode == Activity.RESULT_OK)
                {
                String fileName = data.getStringExtra(FileSelectorActivity.FILE_NAME);
                // Toast.makeText(this, "File clicked: " + fileName, Toast.LENGTH_LONG).show();

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putString(getString(R.string.test_file_key), fileName);

                editor.apply();

                loadTestFile();
                }
            else if (resultCode == Activity.RESULT_CANCELED)
                {
                Toast.makeText(this, "- C A N C E L -", Toast.LENGTH_SHORT).show();
                }

            finish();
            }
        }

    void loadTestFile()
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putBoolean(getString(R.string.test_mode_key), true);

        editor.apply();

        performAction(this, PREFS_ACTION_TEST_LOAD);
        finish();
        }
    }

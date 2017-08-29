package org.lattilad.bestboard.monitorrow;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.fileselector.FileSelectorActivity;
import org.lattilad.bestboard.prefs.PrefsActivity;

import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_RECALL_DATA;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_RELOAD;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_ACTION_STORE_DATA;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_COUNTER;
import static org.lattilad.bestboard.prefs.PrefsFragment.PREFS_TYPE;
import static org.lattilad.bestboard.prefs.PrefsFragment.performAction;

/**
 * startActivity(new Intent(getApplicationContext(), PermissionRequestActivity.class));
 * Process: org.lattilad.bestboard, PID: 31817
 * android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
 *
 * TEST:
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
 * Actions:
 * ACTION_LOAD_TEST:
 * if testing bit is off - stores data, sets testing bit
 * reloads
 *
 * ACTION_RETURN_MAIN:
 * only if testing bit is on
 * clears testing bit
 * if data is stored - restores it, clears store-variable
 * if not - reloads
 */

public class MonitorRowActivity extends AppCompatActivity
    {
    private static final int FILE_SELECTOR_REQUEST = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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

        ((Button) findViewById(R.id.test_button)).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
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
                startActivityForResult(intent, FILE_SELECTOR_REQUEST);
                }
            });


        ((Button) findViewById(R.id.test_button)).setOnLongClickListener(new View.OnLongClickListener()
            {
            @Override
            public boolean onLongClick(View v)
                {



                return true;
                }
            });


        ((Button) findViewById(R.id.main_button)).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putBoolean(getString(R.string.use_testing_key), false);

                editor.apply();

                performAction(getApplicationContext(), PREFS_ACTION_RELOAD);
                }
            });

        ((Button) findViewById(R.id.store_button)).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                // Same communication as in Prefs
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putInt(PREFS_COUNTER, sharedPrefs.getInt(PREFS_COUNTER, 0) + 1);
                editor.putInt(PREFS_TYPE, PREFS_ACTION_STORE_DATA);

                editor.apply();
                finish();
                }
            });

        ((Button) findViewById(R.id.recall_button)).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                // Same communication as in Prefs
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putInt(PREFS_COUNTER, sharedPrefs.getInt(PREFS_COUNTER, 0) + 1);
                editor.putInt(PREFS_TYPE, PREFS_ACTION_RECALL_DATA);

                editor.apply();
                finish();
                }
            });

        ((Button) findViewById(R.id.draft_button)).setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                // Same communication as in Prefs
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putInt(PREFS_COUNTER, sharedPrefs.getInt(PREFS_COUNTER, 0) + 1);
                editor.putInt(PREFS_TYPE, PREFS_ACTION_RELOAD);

                editor.apply();
                finish();
                }
            });

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        if (requestCode == FILE_SELECTOR_REQUEST)
            {
            if (resultCode == Activity.RESULT_OK)
                {
                String fileName = data.getStringExtra(FileSelectorActivity.FILE_NAME);
                Toast.makeText(this, "File clicked: " + fileName, Toast.LENGTH_LONG).show();

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putBoolean(getString(R.string.use_testing_key), true);
                editor.putString(getString(R.string.descriptor_testing_key), fileName);

                editor.apply();

                performAction(this, PREFS_ACTION_RELOAD);
                } else if (resultCode == Activity.RESULT_CANCELED)
                {
                Toast.makeText(this, "- C A N C E L -", Toast.LENGTH_SHORT).show();
                }

            finish();
            }
        }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction()
        {
        Thing object = new Thing.Builder()
                .setName("MonitorRow Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
        }

    @Override
    public void onStart()
        {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
        }

    @Override
    public void onStop()
        {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
        }
    }

package dancingmoon.bestboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.io.File;

import dancingmoon.bestboard.scribe.Scribe;

/**
 * PreferenceFragment is managed by PrefsActivity (which is a standard Activity).
 * xml/prefs.xml, values/prefs_strings.xml, values/prefs_defaults.xml files are also needed.
 * AndroidManifest should contain PrefsActivity:
 * 	<activity
 *		android:name="PrefsActivity"
 *		android:label="@string/app_name">
 *	</activity>
 * It can be started from other activities:
 * 		startActivity( new Intent( this, PrefsActivity.class ));
 * It can be started even from the main screen if we add two lines to AndroidManifest:
 *	<intent-filter>
 * 		<action android:name="android.intent.action.MAIN" />
 * 		<category android:name="android.intent.category.LAUNCHER" />
 *	</intent-filter>
 * Or can be started from general settings. method.xml should contain:
 * 	<input-method xmlns:android="http://schemas.android.com/apk/res/android"
 * 		android:settingsActivity="dancingmoon.bestboard.PrefsActivity"/>
 * PreferenceFragment without any error-checking is very simple:
 *		addPreferencesFromResource(R.xml.prefs); (onCreate method)
 *
 * Preferences can be read:
 * SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getContext() );
 * boolean pref = sharedPrefs.getBoolean( getContext().getString( R.string.pref_key ), false);
 */

/**
 * PrefsActivity only starts PrefsFragment
 * All preferences are managed by PrefsFragment
 */
public class PrefsActivity extends Activity
    {
    private static String PREFS_FRAGMENT_TAG = "dancingmoon.bestboard.prefs";

    /**
     * This is the only method needed by PrefsActivity.
     * It only finds (or creates if missing) fragment and set it as root view
     * @param savedInstanceState savedInstanceState - not used
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
        {
        super.onCreate(savedInstanceState);

        // This should be called at every starting point
        Ignition.start(this);

        Scribe.locus( Debug.PREFS );

        // Preference manager should save/recreate the fragment instance
        PrefsFragment prefsFragment = (PrefsFragment)getFragmentManager()
                .findFragmentByTag(PREFS_FRAGMENT_TAG);
        if ( prefsFragment == null )
            {
            Scribe.note(Debug.PREFS, "New preference fragment is created.");
            prefsFragment = new PrefsFragment();
            }
        else
            {
            Scribe.note(Debug.PREFS, "Preference fragment is restored.");
            }

        // android.R.id.content is the root view
        // but it can hidden behind action bar - http://stackoverflow.com/a/4488149
        // - !! not modified yet !!
        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, prefsFragment, PREFS_FRAGMENT_TAG )
                .commit();
        }
    }

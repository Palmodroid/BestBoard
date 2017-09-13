package org.lattilad.bestboard.prefs;

import android.app.Activity;
import android.os.Bundle;

import org.lattilad.bestboard.Ignition;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.permission.RequestPermissionDialog;
import org.lattilad.bestboard.scribe.Scribe;

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
 * 		android:settingsActivity="org.lattilad.bestboard.prefs.PrefsActivity"/>
 * PreferenceFragment without any error-checking is very simple:
 *		addPreferencesFromResource(R.xml.prefs); (onCreate method)
 *
 * Preferences can be read:
 * SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getContext() );
 * boolean pref = sharedPrefs.getBoolean( getContext().getString( R.string.pref_key ), false);
 *
 * https://stackoverflow.com/questions/32070186/how-to-use-the-v7-v14-preference-support-library
 */

/**
 * PrefsActivity only starts PrefsFragment
 * All preferences are managed by PrefsFragment
 */
public class PrefsActivity extends Activity // https://stackoverflow.com/a/31297546
        implements RequestPermissionDialog.OnDialogFinishListener
    {
    private static String PREFS_FRAGMENT_TAG = "org.lattilad.bestboard.prefs";

    /**
     * This is the only method needed by PrefsActivity.
     * It only finds (or creates if missing) fragment and set it as root view
     *
     * @param savedInstanceState savedInstanceState - not used
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        // No file log before getting
        Scribe.disableFileLog();

        Scribe.locus(Debug.PREF);

        // Preference manager should save/recreate the fragment instance
        PrefsFragment prefsFragment = (PrefsFragment) getFragmentManager()
                .findFragmentByTag(PREFS_FRAGMENT_TAG);
        if (prefsFragment == null)
            {
            Scribe.debug(Debug.PREF, "New preference fragment is created.");
            prefsFragment = new PrefsFragment();
            }
        else
            {
            Scribe.debug(Debug.PREF, "Preference fragment is restored.");
            }

        // android.R.id.content is the root view
        // but it can hidden behind action bar - http://stackoverflow.com/a/4488149
        // - !! not modified yet !!
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, prefsFragment, PREFS_FRAGMENT_TAG)
                .commit();

        Scribe.locus(Debug.PERMISSION);

        RequestPermissionDialog requestPermissionDialog =
                (RequestPermissionDialog)getFragmentManager().findFragmentByTag("dialog");
        if ( requestPermissionDialog == null )
            {
            Scribe.debug(Debug.PERMISSION, "Permission dialog is not found, it should be recreated!");
            requestPermissionDialog = RequestPermissionDialog.newInstance();
            // requestPermissionDialog.setRetainInstance(true); - do not need to retain
            // testDialog.setCancelable(false);
            requestPermissionDialog.show( getFragmentManager(), "dialog");
            }
        else
            Scribe.debug(Debug.PERMISSION, "Permission dialog is found");
        }

    @Override
    public void onFinish(boolean ready)
        {
        if ( ready )
            {
            // Permissions are ready. We could start here
            // This should be called at every starting point
            Ignition.start(this);
            }
        else
            {
            // User want to escape without giving permissions
            finish();
            }
        }
    }

package dancingmoon.bestboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * SupportLibrary doesn't contain PreferenceFragment, so we should use the older method.
 * PrefsActivity, xml/prefs.xml, values/prefs_strings.xml files are needed.
 * AndroidManifest should contain PreferenceActivity:
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
 * PreferenceActivity without error-checking is very simple:
 *		addPreferencesFromResource(R.xml.prefs); (onCreate method)
 *
 * Preferences can be read:
 * SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getContext() );
 * boolean pref = sharedPrefs.getBoolean( getContext().getString( R.string.pref_key ), false);
 *
 */

public class PrefsActivity extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener
    {
    public static class PrefsFragment extends PreferenceFragment
        {
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);
            }
        }

    @Override
    public void onCreate( Bundle savedInstanceState )
        {
        super.onCreate( savedInstanceState );

        // this should come to every entry points
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
        }

    @Override
    protected void onResume()
    	{
    	super.onResume();

    	PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    	}

    @Override
    protected void onPause()
    	{
    	super.onPause();

    	PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
		{
//		if ( key.equals( getString( R.string.package_limitation_key )))
//			updatePackageLimitation(sharedPrefs);
		}

	/*
	private void updatePackageLimitation( SharedPreferences sharedPrefs )
		{
      	String packageLimitationString = sharedPrefs.getString( getString(R.string.package_limitation_key), getString(R.string.package_limitation_default));
		Preference packageLimitationField = findPreference( getString(R.string.package_limitation_key));
		if ( packageLimitationString.length() == 0 )
			{
			packageLimitationField.setSummary( R.string.package_limitation_summary_empty );
			}
		else
			{
			packageLimitationField.setSummary( getString(R.string.package_limitation_summary_intro) + packageLimitationString );
			}
		}
	*/
    }

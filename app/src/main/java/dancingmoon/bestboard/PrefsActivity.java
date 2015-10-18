package dancingmoon.bestboard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import dancingmoon.bestboard.scribe.Scribe;

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
    {
    public static class PrefsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener
        {
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
            super.onCreate( savedInstanceState );

            addPreferencesFromResource( R.xml.prefs );



            }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
            {
            if ( key.equals( getString( R.string.drawing_hide_upper_key )))
                {
                Scribe.note("PREFERENCES: hide-upper has changed!");

                boolean hideUpper = sharedPrefs.getBoolean( getString( R.string.drawing_hide_upper_key ), false );
                Preference hideUpperField =  findPreference( getString(R.string.drawing_hide_upper_key));
                if ( hideUpper )
                    {
                    Scribe.note("PREFERENCES: hide-upper reverted!");

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean( getString( R.string.drawing_hide_upper_key ), false );

                    int cnt = sharedPrefs.getInt( "CNT", 0 );
                    cnt++;
                    editor.putInt( "CNT", cnt );
                    editor.commit();
                    }
              /*  else
                    {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean( getString( R.string.drawing_hide_upper_key ), true );
                    editor.commit();
                    } */

                }
//		if ( key.equals( getString( R.string.package_limitation_key )))
//			updatePackageLimitation(sharedPrefs);
            }

        }

    PrefsFragment prefsFragment;

    @Override
    public void onCreate( Bundle savedInstanceState )
        {
        super.onCreate( savedInstanceState );

        // this should come to every entry points
        PreferenceManager.setDefaultValues( this, R.xml.prefs, false );

        prefsFragment = new PrefsFragment();

        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, prefsFragment )
                .commit();
        }

    @Override
    protected void onResume()
    	{
    	super.onResume();

    	PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener( prefsFragment );
    	}

    @Override
    protected void onPause()
    	{
    	super.onPause();

    	PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener( prefsFragment );
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

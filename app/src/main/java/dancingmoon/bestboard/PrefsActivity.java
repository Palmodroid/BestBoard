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
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);

            Preference reloadButton = findPreference(getString(R.string.descriptor_reload_key));
            reloadButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
            @Override
            public boolean onPreferenceClick(Preference preference)
                {

                Scribe.error("Preference Button was touched!");

                // this = your fragment
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPrefs.edit();

                int cnt = sharedPrefs.getInt("REL", 0);
                cnt++;
                editor.putInt("REL", cnt);
                editor.apply();

                return true;
                }
            });

            }
        @Override
        public void onResume()
            {
            super.onResume();

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .registerOnSharedPreferenceChangeListener(this);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Scribe.note("Preferences are cleared completely");
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.clear();
            editor.apply();


            Scribe.note("BEFORE DEFAULT: Default preferences: descriptor: " + sharedPrefs.getString(getString(R.string.descriptor_key), "nincs"));

            // this should come to every entry points
            PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs, true);

            Scribe.note("AFTER DEFAULT: Default preferences: descriptor: " + sharedPrefs.getString(getString(R.string.descriptor_key), "nincs"));
            }

        @Override
        public void onPause()
            {
            super.onPause();

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .unregisterOnSharedPreferenceChangeListener(this);
            }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
            {
            if ( key.equals( getString( R.string.descriptor_key )))
                {
                Scribe.note("PREFERENCES: descriptor has changed!");
                }

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
                    editor.apply();
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


    @Override
    public void onCreate( Bundle savedInstanceState )
        {
        super.onCreate(savedInstanceState);

        // Preference manager should save/recreate the fragment instance
        PrefsFragment prefsFragment = (PrefsFragment)getFragmentManager()
                .findFragmentById(android.R.id.content);
        if ( prefsFragment == null )
            {
            prefsFragment = new PrefsFragment();
            }

        // android.R.id.content is the root view
        // but it can hidden behind action bar - http://stackoverflow.com/a/4488149 - !! not modified yet !!
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, prefsFragment)
                .commit();
        }

    }

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

/**
 * PrefsActivity only starts PrefsActivity.PrefsFragment
 * All preferences are managed by PrefsFragment
 */
public class PrefsActivity extends Activity
    {
    /**
     * This is the only method needed by PrefsActivity.
     * It only finds (or creates if missing) fragment and set it as root view
     * @param savedInstanceState savedInstanceState - not used
     */
    @Override
    public void onCreate( Bundle savedInstanceState )
        {
        super.onCreate(savedInstanceState);

        /** !!!! **/
        // initScribe should be started before any use of Scribe
        // PreferenceManager.setDefaultValues() should be called BEFORE initScribe() to set up dir.
        // PreferenceManager...registerOnSharedPreferenceChangeListener() should be called BEFORE
        // setDefaultValues()
        /** !!!! **/
        // This could only work, if PrefsActivity is started first.
        // AND working directory should be stored also as "default"
        /** !!!! **/
        Debug.initScribe( this );

        Scribe.locus();

        // Preference manager should save/recreate the fragment instance
        PrefsFragment prefsFragment = (PrefsFragment)getFragmentManager()
                .findFragmentById(android.R.id.content);
        if ( prefsFragment == null )
            {
            Scribe.note("New preference fragment is created.");
            prefsFragment = new PrefsFragment();
            }
        else
            {
            Scribe.note("Preference fragment is restored.");
            }

        // android.R.id.content is the root view
        // but it can hidden behind action bar - http://stackoverflow.com/a/4488149 - !! not modified yet !!
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, prefsFragment)
                .commit();
        }


    // Key of counter preference - service should react, if counter increases
    public static final String PREFS_COUNTER = "actioncounter";

    // Key of type preference - service should perform this action, if counter increases
    public static final String PREFS_TYPE = "actiontype";

    /** Coat.descriptor should be reloaded */
    public static final int PREFS_ACTION_RELOAD = 1;

    /** All layouts should be redrawn */
    public static final int PREFS_ACTION_REDRAW = 2;

    /** Refresh preference variables */
    public static final int PREFS_ACTION_REFRESH = 3;


    /**
     * PrefsFragment manages all preferences.
     *
     * Communication with SoftBoardService is very difficult.
     * Bind cannot be used, because methods are final.
     * Broadcasting could be a possibility.
     *
     * PrefsFragment is checking all preference changes, and corrects invalid values.
     * If result is ready and service should be do something,
     * then PREFS_COUNTER preference increases.
     * Service should check only this change, and react as prescribed in PREFS_TYPE.
     */
    public static class PrefsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener
        {
        /**
         * Notifies server to react preference changes
         * @param type action type
         */
        private void performAction( int type )
            {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );
            SharedPreferences.Editor editor = sharedPrefs.edit();

            editor.putInt( PREFS_COUNTER, sharedPrefs.getInt( PREFS_COUNTER, 0 ) + 1 );
            editor.putInt( PREFS_TYPE, type );

            editor.apply();

            switch ( type )
                {
                case PREFS_ACTION_RELOAD:
                    Scribe.note( "PREFERENCE: server is notified to reload descriptor." );
                    break;

                case PREFS_ACTION_REDRAW:
                    Scribe.note( "PREFERENCE: server is notified to redraw layouts." );
                    break;

                default:
                    Scribe.error( "PREFERENCE: preference action type is invalid!");
                }
            }


        /**
         * Preferences are initialized
         * @param savedInstanceState - not used
         */
        @Override
        public void onCreate(Bundle savedInstanceState)
            {
            super.onCreate( savedInstanceState );
            Scribe.locus();

            // Load preferences from resources
            addPreferencesFromResource( R.xml.prefs );

            // Preference as button - only click behavior is used
            findPreference(getString(R.string.descriptor_reload_key)).
                    setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
                    {
                    @Override
                    public boolean onPreferenceClick( Preference preference )
                        {
                        // getActivity() cannot be null, when button is displayed
                        performAction( PREFS_ACTION_RELOAD );
                        return true;
                        }
                    } );
                }


        /**
         * Entry point of preference fragment
         * Registers on SharedPreferenceChangeListener and
         * AFTER registering set default preference values
         * (so checking happens even when setting default values)
         * (getActivity() cannot be null in onResume())
         */
        @Override
        public void onResume()
            {
            super.onResume();
            Scribe.locus();

            PreferenceManager.getDefaultSharedPreferences( getActivity() )
                    .registerOnSharedPreferenceChangeListener( this );

            /* Testing default preferences
            Scribe.note( "Preferences are cleared completely" );

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.clear();
            editor.apply();

            Scribe.note( "BEFORE SETTING DEFAULT VALUE - Sample preference: "
                    + sharedPrefs.getString( getString( R.string.descriptor_file_key ), " is missing" ) );
            PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs, true);
            Scribe.note( "BEFORE SETTING DEFAULT VALUE - Sample preference: "
                    + sharedPrefs.getString( getString( R.string.descriptor_file_key ), " is missing" ) );
            */

            // OR

            // Default preference values are set only at start
            PreferenceManager.setDefaultValues( getActivity(), R.xml.prefs, false );

            // Descriptor file preferences should be checked immediately, because file system could change
            checkDescriptorFilePreferences();
            }


        /**
         * Exit point of preference fragment - pair of onResume
         * unregisters on SharedPreferenceChangeListener
         * (getActivity() cannot be null in onResume())
         */
        @Override
        public void onPause()
            {
            super.onPause();
            Scribe.locus();

            PreferenceManager.getDefaultSharedPreferences( getActivity() )
                    .unregisterOnSharedPreferenceChangeListener( this );
            }


        /**
         * Check of every change in preferences
         * If service should react, then PREFS_COUNTER is increased, and PREFS_TYPE is set
         * (getActivity() cannot be null between onResume() and onPause())
         * @param sharedPrefs shared preferences
         * @param key key changed preferences
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
            {
            // Descriptor / Working directory
            if ( key.equals( getString( R.string.descriptor_directory_key )))
                {
                Scribe.note( "PREFERENCES: working directory has changed!" );
                // If directory (and descriptor file) is valid,
                // then working directory for debug should be also changed
                // Debug is needed for preferences, too - so directory will be changed directly
                if ( checkDescriptorFilePreferences() )
                    {
                    String directoryName =
                            sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                                    getString( R.string.descriptor_directory_default ) );

                    Scribe.setDirectoryName( directoryName ); // Primary directory name
                    Scribe.setDirectoryNameSecondary( directoryName ); // Secondary directory name
                    }
                }

            // Descriptor / Coat descriptor file
            else if ( key.equals( getString( R.string.descriptor_file_key )))
                {
                Scribe.note( "PREFERENCES: descriptor file has changed!" );
                // If new descriptor file is valid, then it should be reloaded
                if ( checkDescriptorFilePreferences() )
                    {
                    performAction( PREFS_ACTION_RELOAD );
                    }
                }

            // Descriptor / Reload descriptor file
                // Defined in onCreate as Button

            // Drawing / Hide upper quoter
            else if ( key.equals( getString( R.string.drawing_hide_upper_key )))
                {
                Scribe.note( "PREFERENCES: Hide upper behavior has changed!" );

                }

            // Drawing / Hide lower quoter
            else if ( key.equals( getString( R.string.drawing_hide_lower_key )))
                {
                Scribe.note( "PREFERENCES: Hide lower behavior has changed!" );

                }

            // Drawing / Screen height ratio
            else if ( key.equals( getString( R.string.drawing_height_ratio_key )))
                {
                Scribe.note( "PREFERENCES: Screen height ratio has changed!" );

                /** THIS SHOULD BE CHANGED !! Integer preference with min and max values is needed.**/
                String heightRatioString = sharedPrefs.getString( getString(R.string.drawing_height_ratio_key), "" );
                int heightRatio;
                try
                    {
                    heightRatio = Integer.valueOf( heightRatioString );
                    }
                catch ( NumberFormatException nfe )
                    {
                    heightRatio = 0;
                    }

                if ( heightRatio < 200 )
                    {
                    heightRatio = 200;

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString( getString( R.string.drawing_height_ratio_key ), Integer.toString( heightRatio ) );
                    editor.apply();
                    }
                else if (heightRatio > 700 )
                    {
                    heightRatio = 700;

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString( getString( R.string.drawing_height_ratio_key ), Integer.toString( heightRatio ) );
                    editor.apply();
                    }

                // Cannot be null, if prefs.xml is valid
                Preference heightRatioPreference = findPreference( getString( R.string.drawing_height_ratio_key ) );
                heightRatioPreference.setSummary( getString( R.string.drawing_height_ratio_summary ) + " " + Integer.toString( heightRatio ) );

                performAction( PREFS_ACTION_REDRAW );
                }

            // Drawing / Landscape offset for non-wide boards
            else if ( key.equals( getString( R.string.drawing_landscape_offset_key )))
                {
                /** THIS SHOULD BE CHANGED !! Integer preference with min and max values is needed.**/
                String landscapeOffsetString = sharedPrefs.getString( getString(R.string.drawing_landscape_offset_key), "" );
                int landscapeOffset;
                try
                    {
                    landscapeOffset = Integer.valueOf( landscapeOffsetString );
                    }
                catch ( NumberFormatException nfe )
                    {
                    landscapeOffset = 0;
                    }

                if ( landscapeOffset < 0 )
                    {
                    landscapeOffset = 0;

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString( getString( R.string.drawing_landscape_offset_key ), Integer.toString( landscapeOffset ) );
                    editor.apply();
                    }
                else if (landscapeOffset > 1000 )
                    {
                    landscapeOffset = 1000;

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString( getString( R.string.drawing_landscape_offset_key ), Integer.toString( landscapeOffset ) );
                    editor.apply();
                    }

                // Cannot be null, if prefs.xml is valid
                Preference landscapeOffsetPreference = findPreference( getString( R.string.drawing_landscape_offset_key ) );
                landscapeOffsetPreference.setSummary( getString( R.string.drawing_landscape_offset_summary ) + " " + Integer.toString( landscapeOffset ) );

                Scribe.note( "PREFERENCES: Landscape offset for non-wide boards has changed: " + landscapeOffset);

                performAction( PREFS_ACTION_REDRAW );
                }

            // Drawing / Outer rim ratio
            else if ( key.equals( getString( R.string.drawing_outer_rim_key )))
                {
                Scribe.note( "PREFERENCES: Outer rim ratio has changed!" );

                /** THIS SHOULD BE CHANGED !! Integer preference with min and max values is needed.**/
                String outerRimString = sharedPrefs.getString( getString(R.string.drawing_outer_rim_key), "" );
                int outerRim;
                try
                    {
                    outerRim = Integer.valueOf( outerRimString );
                    }
                catch ( NumberFormatException nfe )
                    {
                    outerRim = 0;
                    }

                if ( outerRim < 0 )
                    {
                    outerRim =  0;

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString( getString( R.string.drawing_outer_rim_key ), Integer.toString( outerRim ) );
                    editor.apply();
                    }
                else if (outerRim > 900 )
                    {
                    outerRim = 900;

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString( getString( R.string.drawing_outer_rim_key ), Integer.toString( outerRim ) );
                    editor.apply();
                    }

                // Cannot be null, if prefs.xml is valid
                Preference outerRimPreference = findPreference( getString( R.string.drawing_outer_rim_key ) );
                outerRimPreference.setSummary( getString( R.string.drawing_outer_rim_summary ) + " " + Integer.toString( outerRim ) );

                performAction( PREFS_ACTION_REDRAW );
                }

            // Cursor / Touch allow
            else if ( key.equals( getString( R.string.cursor_touch_allow_key )))
                {
                Scribe.note( "PREFERENCES: Cursor touch indicator has changed!" );

                performAction( PREFS_ACTION_REFRESH );
                }

            // Cursor / Stroke allow
            else if ( key.equals( getString( R.string.cursor_stroke_allow_key )))
                {
                Scribe.note( "PREFERENCES: Cursor stroke indicator has changed!" );

                performAction( PREFS_ACTION_REFRESH );
                }

            // Debug
            else if ( key.equals( getString( R.string.debug_key )))
                {
                Scribe.note( "PREFERENCES: debug has changed!" );

                // !! Parametrized Scribe.enable() is needed
                if ( sharedPrefs.getBoolean( getString(R.string.debug_key), true ) )
                    {
                    Scribe.enable();
                    }
                else
                    {
                    Scribe.disable();
                    }
                }

            }


        /**
         * Checking and validating descriptor file preferences.
         * Both working directory and descriptor file are checked, and reload is also set.
         * This method now is only used in PrefsFragment, parsing will check the files once more.
         * If it will be used more generally, then getAction() should be checked against null!
         */
        private boolean checkDescriptorFilePreferences( )
            {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );

            // These cannot be null, if prefs.xml is valid
            Preference directoryPreference = findPreference( getString( R.string.descriptor_directory_key ) );
            Preference filePreference = findPreference( getString( R.string.descriptor_file_key ) );
            Preference reloadPreference = findPreference( getString( R.string.descriptor_reload_key ) );

            // Originally all preferences handled as invalid
            directoryPreference.setSummary( getString( R.string.descriptor_directory_summary_invalid ) );
            filePreference.setSummary( getString( R.string.descriptor_file_summary_invalid ) );
            reloadPreference.setEnabled( false );

            // Working directory is checked
            // It can change later, so parsing will check it again

            String directoryName =
                    sharedPrefs.getString( getString( R.string.descriptor_directory_key ), "" );
            File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

            if ( !directoryFile.exists() || !directoryFile.isDirectory() )
                {
                Scribe.error( "PREFERENCES: working directory is missing!" );
                return false;
                }

            directoryPreference.setSummary( getString( R.string.descriptor_directory_summary ) + " " +
                    directoryName );

            // Directory is valid, descriptor file is checked
            // It can change later, so parsing will check it again

            String fileName =
                    sharedPrefs.getString( getString( R.string.descriptor_file_key ), "" );
            File fileFile = new File( directoryFile, fileName );

            if ( !fileFile.exists() || !fileFile.isFile() )
                {
                Scribe.error( "PREFERENCES: descriptor file is missing!" );
                return false;
                }

            filePreference.setSummary( getString( R.string.descriptor_file_summary ) + " " +
                    fileName );

            reloadPreference.setEnabled( true );

            return true;
            }

        }
    }

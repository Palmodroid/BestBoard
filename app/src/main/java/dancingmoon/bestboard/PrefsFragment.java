package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.io.File;

import dancingmoon.bestboard.scribe.Scribe;


/**
 * PrefsFragment manages all preferences.
 *
 * Communication with SoftBoardService is very difficult.
 * Bind cannot be used, because methods are final.
 * Broadcasting could be a possibility.
 * I tried out an other approach, secondary preference changes are listened.
 *
 * PrefsFragment is checking all preference changes, and corrects invalid values.
 * If result is ready and service should be do something,
 * then PREFS_COUNTER preference increases.
 * Service should check only this change, and react as prescribed in PREFS_TYPE.
 */
public class PrefsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
    {
    /**
     ** ACTIONS OF THE SERVICE, FORCED BY PREFERENCE CHANGES
     **/

    // Key of counter preference - service should react, if counter increases
    // PREF_COUNTER is initialized during init(), so it signs, if this is the first run
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
     ** INTEGER PREFERENCE KEYS
     **/

    /** Maximal screen height ratio */
    public static String DRAWING_HEIGHT_RATIO_INT_KEY = "intheightratio";

    /** Horizontal offset for landscape */
    public static String DRAWING_LANDSCAPE_OFFSET_INT_KEY = "intlandscapeoffset";

    /** Ratio of the outer rim */
    public static String DRAWING_OUTER_RIM_INT_KEY = "intouterrim";

    // -- NEW INTEGER PREFERENCE KEYS SHOULD COME HERE -- //


    /**
     ** STATIC PART TO MANAGE HYBRID INTEGER PREFERENCES
     ** This part is needed by every entry point of the whole program
     **/

    /**
     * This should be called at every entry point
     * Sets default values.
     * Checks and sets integer preferences.
     * Ignition.start() calls this.
     * @param context context
     * @return true if this is the very first start
     */
    public static boolean init(Context context)
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );
        SharedPreferences.Editor editor = sharedPrefs.edit();

        ///* Testing default preferences
        Scribe.note( "Testing. Preferences are cleared completely" );
        editor.clear();
        editor.apply();
        Scribe.note("BEFORE SETTING DEFAULT VALUE - Contains sample preference: "
                + sharedPrefs.contains( context.getString( R.string.debug_key ) ));
        PreferenceManager.setDefaultValues(context, R.xml.prefs, true);
        Scribe.note("BEFORE SETTING DEFAULT VALUE - Contains sample preference: "
                + sharedPrefs.contains( context.getString( R.string.debug_key ) ));
        //*/

        // OR

        if ( !sharedPrefs.contains( PREFS_COUNTER ) )
            {
            Scribe.note( "COUNTER Preference cannot be found. This is the very first start." );

            // Default preference values are set only at start
            // Default preference values are set only at start
            PreferenceManager.setDefaultValues(context, R.xml.prefs, false);

            // PREFS_COUNTER pref signs, that program was already started
            editor.putInt( PREFS_COUNTER, 0 );

            // Integer preferences should be set, too
            // !! This will happen at every start, checking an int pref could avoid repeat !!
            checkAndStoreHeightRatioPref(context);
            checkAndStoreLandscapeOffsetPref( context );
            checkAndStoreOuterRimPref( context );

            // -- NEW INTEGER PREFERENCE CALLS SHOULD COME HERE -- //

            Scribe.note( "Preferences are initialized." );
            return true;
            }
        else
            {
            Scribe.note( "COUNTER Preference can be found. No further initialization is needed." );
            return false;
            }
        }


    /**
     * Checks and sets height-ratio integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreHeightRatioPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.drawing_height_ratio_key,
                DRAWING_HEIGHT_RATIO_INT_KEY,
                R.string.drawing_height_ratio_default,
                R.integer.drawing_height_ratio_min,
                R.integer.drawing_height_ratio_max );
        }

    /**
     * Checks and sets landscape-offset integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreLandscapeOffsetPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.drawing_landscape_offset_key,
                DRAWING_LANDSCAPE_OFFSET_INT_KEY,
                R.string.drawing_landscape_offset_default,
                R.integer.drawing_landscape_offset_min,
                R.integer.drawing_landscape_offset_max );
        }

    /**
     * Checks and sets outer-rim integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreOuterRimPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.drawing_outer_rim_key,
                DRAWING_OUTER_RIM_INT_KEY,
                R.string.drawing_outer_rim_default,
                R.integer.drawing_outer_rim_min,
                R.integer.drawing_outer_rim_max );
        }

    // -- NEW INTEGER PREFERENCE METHODS SHOULD COME HERE -- //

    /**
     * Helper method to check, correct and store an integer preference.
     * Integer preferences are stored by string, and also by numeric value.
     * String preference is only changed if given value is out of range.
     * Int preference changes every time.
     * @param context context
     * @param stringKeyRes key of string preference (as string by resId)
     * @param integerKey key of integer preference (by string!)
     * @param defaultRes default value (as string by resId)
     * @param minRes min value (as integer by resId)
     * @param maxRes max value (as integer by resId)
     * @return integer value of the preference
     */
    private static int _checkAndStoreIntPref( Context context,
                                              int stringKeyRes,
                                              String integerKey,
                                              int defaultRes,
                                              int minRes,
                                              int maxRes )
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );
        SharedPreferences.Editor editor = sharedPrefs.edit();

        String key = context.getString(stringKeyRes);
        String prefString = sharedPrefs.getString( key, context.getString( defaultRes ));

        int prefInteger;
        try
            {
            prefInteger = Integer.valueOf( prefString );
            }
        catch ( NumberFormatException nfe )
            {
            // If number is not valid, then 0 is set
            prefInteger = 0;
            }

        int min = context.getResources().getInteger( minRes );
        int max = context.getResources().getInteger( maxRes );
        if ( prefInteger < min )
            {
            prefInteger = min;
            // !! It triggers a new change
            // if we still want to correct the textual data, listener should be turned off !!
            // editor.putString( key, Integer.toString( prefInteger ) );
            }
        else if ( prefInteger > max )
            {
            prefInteger = max;
            // !! It triggers a new change
            // if we still want to correct the textual data, listener should be turned off !!
            // editor.putString(key, Integer.toString(prefInteger));
            }

        editor.putInt( integerKey, prefInteger );
        editor.apply();

        return prefInteger;
        }


    /**
     ** PREPARE INTEGER DIALOGS AND SUMMARIES
     ** This part is needed by preference-fragment prepare
     **/

    /**
     * Prepare integer prefs' dialogs with range.
     * This should be called at preference-fragment prepare
     */
    private void prepareIntPrefsDialogMessage()
        {
        _prepareDialogMessage( R.string.drawing_height_ratio_key, 
                R.string.drawing_height_ratio_dialog_message,
                R.integer.drawing_height_ratio_min, 
                R.integer.drawing_height_ratio_max );

        _prepareDialogMessage( R.string.drawing_landscape_offset_key,
                R.string.drawing_landscape_offset_dialog_message,
                R.integer.drawing_landscape_offset_min,
                R.integer.drawing_landscape_offset_max );

        _prepareDialogMessage( R.string.drawing_outer_rim_key,
                R.string.drawing_outer_rim_dialog_message,
                R.integer.drawing_outer_rim_min,
                R.integer.drawing_outer_rim_max );

        // -- NEW INTEGER PREFERENCE DIALOG MESSAGE PREPS SHOULD COME HERE -- //
        }

    /**
     * Helper method to expand dialog message with min-max range.
     * R.string.prefs_range is used as message.
     * @param stringKeyRes key of string preference (as string by resId)
     * @param dialogMessageRes dialog message (as string by resId)
     * @param minRes min value (as integer by resId)
     * @param maxRes max value (as integer by resId)
     */
    private void _prepareDialogMessage(int stringKeyRes,
                                   int dialogMessageRes,
                                   int minRes,
                                   int maxRes )
        {
        EditTextPreference editTextPreference =
                (EditTextPreference)findPreference( getString( stringKeyRes ) );
        editTextPreference.setDialogMessage(
                getString( dialogMessageRes ) + " " +
                getString( R.string.prefs_range ) + " " +
                Integer.toString(getResources().getInteger(minRes)) + " - " +
                Integer.toString(getResources().getInteger(maxRes)) + "." );
        }

    /**
     * Checks every preference, and sets summaries.
     * This should be called at preference-fragment prepare with allKeys true,
     * and at every preference change with allKeys false.
     * Integer preferences are checked by the checkAndStore... methods.
     * Directory and descriptor file validity is checked by checkDescriptorFilePrefs.
     * @param sharedPrefs shared preferences
     * @param key key changed preferences
     * @param allKeys check all keys if true
     */
    private void checkPrefs( SharedPreferences sharedPrefs, String key, boolean allKeys )
        {
        // Descriptor / Working directory
        if ( key.equals( getString( R.string.descriptor_directory_key )) || allKeys )
            {
            Scribe.note( "PREFERENCES: working directory has changed!" );
            // If directory (and descriptor file) is valid,
            // then working directory for debug should be also changed
            // Debug is needed for preferences, too - so directory will be changed directly
            if ( checkDescriptorFilePreferences( sharedPrefs ))
                {
                String directoryName =
                        sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                                getString( R.string.descriptor_directory_default ) );

                Scribe.setDirectoryName( directoryName ); // Primary directory name
                Scribe.setDirectoryNameSecondary( directoryName ); // Secondary directory name
                }
            }

        // Descriptor / Coat descriptor file
        if ( key.equals( getString( R.string.descriptor_file_key )) || allKeys )
            {
            Scribe.note( "PREFERENCES: descriptor file has changed!" );
            // If new descriptor file is valid, then it should be reloaded
            if ( checkDescriptorFilePreferences( sharedPrefs ) && !allKeys )
                {
                performAction( PREFS_ACTION_RELOAD );
                }
            }

        // Descriptor / Reload descriptor file
        // Defined in onCreate as Button

        // Drawing / Hide upper quoter
        if ( key.equals( getString( R.string.drawing_hide_upper_key )) || allKeys )
            {
            Scribe.note( "PREFERENCES: Hide upper behavior has changed!" );

            }

        // Drawing / Hide lower quoter
        if ( key.equals( getString( R.string.drawing_hide_lower_key )) || allKeys )
            {
            Scribe.note( "PREFERENCES: Hide lower behavior has changed!" );

            }

        // Drawing / Screen height ratio
        if ( key.equals( getString( R.string.drawing_height_ratio_key )) || allKeys )
            {
            int heightRatio = checkAndStoreHeightRatioPref( getActivity() );

            // Cannot be null, if prefs.xml is valid
            Preference heightRatioPreference = findPreference( getString( R.string.drawing_height_ratio_key ) );
            heightRatioPreference.setSummary(getString(R.string.drawing_height_ratio_summary) + " " +
                    Integer.toString(heightRatio));
            Scribe.note("PREFERENCES: Screen height ratio has changed: " + heightRatio);
            if ( !allKeys )     performAction(PREFS_ACTION_REDRAW);
            }

        // Drawing / Landscape offset for non-wide boards
        if ( key.equals( getString( R.string.drawing_landscape_offset_key )) || allKeys )
            {
            int landscapeOffset = checkAndStoreLandscapeOffsetPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference landscapeOffsetPreference = findPreference( getString( R.string.drawing_landscape_offset_key ) );
            landscapeOffsetPreference.setSummary(getString(R.string.drawing_landscape_offset_summary) + " " +
                    Integer.toString(landscapeOffset));
            Scribe.note("PREFERENCES: Landscape offset for non-wide boards has changed: " + landscapeOffset);
            if ( !allKeys )     performAction(PREFS_ACTION_REDRAW);
            }

        // Drawing / Outer rim ratio
        if ( key.equals( getString( R.string.drawing_outer_rim_key )) || allKeys )
            {
            int outerRim = checkAndStoreOuterRimPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference outerRimPreference = findPreference( getString( R.string.drawing_outer_rim_key ) );
            outerRimPreference.setSummary(getString(R.string.drawing_outer_rim_summary) + " " +
                    Integer.toString(outerRim));
            Scribe.note("PREFERENCES: Outer rim ratio has changed!" + outerRim);
            if ( !allKeys )     performAction(PREFS_ACTION_REDRAW);
            }

        // Cursor / Touch allow
        if ( key.equals( getString( R.string.cursor_touch_allow_key )) || allKeys )
            {
            Scribe.note("PREFERENCES: Cursor touch indicator has changed!");

            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Stroke allow
        if ( key.equals( getString( R.string.cursor_stroke_allow_key )) || allKeys )
            {
            Scribe.note( "PREFERENCES: Cursor stroke indicator has changed!" );

            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Debug
        if ( key.equals( getString( R.string.debug_key )) || allKeys )
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
    private boolean checkDescriptorFilePreferences( SharedPreferences sharedPrefs )
        {
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
            Scribe.error( "PREFERENCES: working directory is missing: " +
                    directoryFile.getAbsolutePath() );
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
            Scribe.error( "PREFERENCES: descriptor file is missing: " +
                    fileFile.getAbsolutePath() );
            return false;
            }

        filePreference.setSummary( getString( R.string.descriptor_file_summary ) + " " +
                fileFile.getAbsolutePath() );

        reloadPreference.setEnabled( true );

        return true;
        }


    /**
     ** ACTIONS OF THE SERVICE, FORCED BY PREFERENCE CHANGES
     **/

    /**
     * Notifies server to react preference changes
     * @param type action type
     */
    private void performAction( int type )
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putInt( PREFS_COUNTER, sharedPrefs.getInt( PREFS_COUNTER, 0 ) + 1 );
        editor.putInt( PREFS_TYPE, type );

        editor.apply();

        switch ( type )
            {
            case PREFS_ACTION_RELOAD:
                Scribe.note("PREFERENCE: server is notified to reload descriptor.");
                break;

            case PREFS_ACTION_REDRAW:
                Scribe.note( "PREFERENCE: server is notified to redraw layouts." );
                break;

            default:
                Scribe.error( "PREFERENCE: preference action type is invalid!");
            }
        }


    /**
     ** STANDARD METHODS CONNECT FRAGMENT TO ANDROID SYSTEM
     **/

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
        addPreferencesFromResource(R.xml.prefs);

        // Preference as button - only click behavior is used
        findPreference(getString(R.string.descriptor_reload_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                @Override
                public boolean onPreferenceClick(Preference preference)
                    {
                    // getActivity() cannot be null, when button is displayed
                    performAction(PREFS_ACTION_RELOAD);
                    return true;
                    }
                });

        // Prepare dialog messages
        prepareIntPrefsDialogMessage();

        // Preferences (descriptor file) is checked and summaries are updated
        checkPrefs(PreferenceManager.getDefaultSharedPreferences(getActivity()),
                "", true);

        }


    /**
     * Entry point of preference fragment
     * Registers on SharedPreferenceChangeListener
     * (getActivity() cannot be null in onResume())
     */
    @Override
    public void onResume()
        {
        super.onResume();
        Scribe.locus();

        // Change listener is registered
        PreferenceManager.getDefaultSharedPreferences( getActivity() )
                .registerOnSharedPreferenceChangeListener(this);

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

        // Change listener is unregistered
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
        checkPrefs( sharedPrefs, key, false );
        }

    }

package org.lattilad.bestboard.prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.lattilad.bestboard.Ignition;
import org.lattilad.bestboard.R;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.fileselector.FileSelectorActivity;
import org.lattilad.bestboard.permission.RequestPermissionActivity;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.webview.WebViewActivity;

import java.io.File;


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

    /** All layouts should be recalculated */
    public static final int PREFS_ACTION_RECALCULATE = 2;

    /** All layouts should be recalculated and pictures redrawn */
    public static final int PREFS_ACTION_REDRAW = 3;

    /** Refresh preference variables, layouts are not affected */
    public static final int PREFS_ACTION_REFRESH = 4;

    /** Clears spedometer data */
    public static final int PREFS_ACTION_CLEAR_SPEDOMETER = 5;

    /** Just for testing */
    // public static final int PREFS_ACTION_STORE_DATA = 6;

    /** Just for testing */
    // public static final int PREFS_ACTION_RECALL_DATA = 7;

    /** Saves main softboarddata, and loads test */
    public static final int PREFS_ACTION_TEST_LOAD = 8;

    /** Restores main softboarddata */
    public static final int PREFS_ACTION_TEST_RETURN = 9;


    /**
     ** INTEGER PREFERENCE KEYS
     **/

    /** Maximal screen height ratio */
    public static String DRAWING_HEIGHT_RATIO_INT_KEY = "intheightratio";

    /** Horizontal offset for landscape */
    public static String DRAWING_LANDSCAPE_OFFSET_INT_KEY = "intlandscapeoffset";

    /** Ratio of the outer rim */
    public static String DRAWING_OUTER_RIM_INT_KEY = "intouterrim";

    /** Limit of the speed measurement */
    public static String DRAWING_SPEDOMETER_LIMIT_INT_KEY = "intspedolimit";

    /** Length of circular movements */
    public static String TOUCH_LONG_COUNT_INT_KEY = "intlongcount";

    /** Count of hard presses */
    public static String TOUCH_PRESS_COUNT_INT_KEY = "intpresscount";

    /** Threshold of hard presses */
    public static String TOUCH_PRESS_THRESHOLD_INT_KEY = "intpressthreshold";

    /** Time of stay */
    public static String TOUCH_STAY_TIME_INT_KEY = "intstaytime";

    /** Time of repeat */
    public static String TOUCH_REPEAT_TIME_INT_KEY = "intrepeattime";

    /** Length of primary vibration */
    public static String CURSOR_VIBRATION_FIRST_INT_KEY = "intvibrationfirst";

    /** Length of secondary vibration */
    public static String CURSOR_VIBRATION_SECOND_INT_KEY = "intvibrationsecond";

    /** Length of repeated vibration */
    public static String CURSOR_VIBRATION_REPEAT_INT_KEY = "intvibrationrepeat";

    /** Elongation period */
    public static String EDITING_ELONGATION_PERIOD_INT_KEY = "intelongationperiod";


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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        /* Testing default preferences
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Scribe.note( Debug.PREF,  "Testing. Preferences are cleared completely" );
        editor.clear();
        editor.apply();
        Scribe.note( Debug.PREF, "BEFORE SETTING DEFAULT VALUE - Prefs contains sample preference: "
                + sharedPrefs.contains( context.getString( R.string.debug_key ) ));
        PreferenceManager.setDefaultValues(context, R.xml.prefs, true);
        Scribe.note( Debug.PREF, "AFTER SETTING DEFAULT VALUE - Prefs contains sample preference: "
                + sharedPrefs.contains( context.getString( R.string.debug_key ) ));
        */

        // OR

        if ( !sharedPrefs.contains( PREFS_COUNTER ) )
            {
            Scribe.note( Debug.PREF,  "COUNTER Preference cannot be found. This is the very first start." );

            // Default preference values are set only at start
            // Default preference values are set only at start
            PreferenceManager.setDefaultValues(context, R.xml.prefs, false);

            // PREFS_COUNTER pref signs, that program was already started
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt( PREFS_COUNTER, 0 );
            editor.apply();

            // Integer preferences should be set, too
            // !! This will happen at every start, checking an int pref could avoid repeat !!
            checkAndStoreHeightRatioPref(context);
            checkAndStoreLandscapeOffsetPref(context);
            checkAndStoreOuterRimPref(context);
            checkAndStoreSpedometerLimitPref(context);

            checkAndStoreLongCountPref(context);
            checkAndStorePressCountPref(context);
            checkAndStorePressThresholdPref(context);
            checkAndStoreStayTimePref(context);
            checkAndStoreRepeatTimePref(context);

            checkAndStoreVibrationFirstPref(context);
            checkAndStoreVibrationSecondPref(context);
            checkAndStoreVibrationRepeatPref(context);

            checkAndStoreElongationPeriodPref(context);

            // -- NEW INTEGER PREFERENCE CALLS SHOULD COME HERE -- //

            Scribe.note( Debug.PREF,  "Preferences are initialized." );
            return true;
            }
        else
            {
            Scribe.note( Debug.PREF,  "COUNTER Preference can be found. No further initialization is needed." );
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

    /**
     * Checks and sets spedometer limit integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreSpedometerLimitPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.drawing_spedometer_limit_key,
                DRAWING_SPEDOMETER_LIMIT_INT_KEY,
                R.string.drawing_spedometer_limit_default,
                R.integer.drawing_spedometer_limit_min,
                R.integer.drawing_spedometer_limit_max );
        }

    /**
     * Checks and sets long count integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreLongCountPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.touch_long_count_key,
                TOUCH_LONG_COUNT_INT_KEY,
                R.string.touch_long_count_default,
                R.integer.touch_long_count_min,
                R.integer.touch_long_count_max );
        }

    /**
     * Checks and sets press count integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStorePressCountPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.touch_press_count_key,
                TOUCH_PRESS_COUNT_INT_KEY,
                R.string.touch_press_count_default,
                R.integer.touch_press_count_min,
                R.integer.touch_press_count_max );
        }

    /**
     * Checks and sets press threshold integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStorePressThresholdPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.touch_press_threshold_key,
                TOUCH_PRESS_THRESHOLD_INT_KEY,
                R.string.touch_press_threshold_default,
                R.integer.touch_press_threshold_min,
                R.integer.touch_press_threshold_max );
        }

    /**
     * Checks and sets stay time integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreStayTimePref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.touch_stay_time_key,
                TOUCH_STAY_TIME_INT_KEY,
                R.string.touch_stay_time_default,
                R.integer.touch_stay_time_min,
                R.integer.touch_stay_time_max );
        }

    /**
     * Checks and sets repeat time integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreRepeatTimePref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.touch_repeat_time_key,
                TOUCH_REPEAT_TIME_INT_KEY,
                R.string.touch_repeat_time_default,
                R.integer.touch_repeat_time_min,
                R.integer.touch_repeat_time_max );
        }

    /**
     * Checks and sets primary vibration integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreVibrationFirstPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.cursor_vibration_first_key,
                CURSOR_VIBRATION_FIRST_INT_KEY,
                R.string.cursor_vibration_first_default,
                R.integer.cursor_vibration_first_min,
                R.integer.cursor_vibration_first_max );
        }

    /**
     * Checks and sets secondary vibration integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreVibrationSecondPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.cursor_vibration_second_key,
                CURSOR_VIBRATION_SECOND_INT_KEY,
                R.string.cursor_vibration_second_default,
                R.integer.cursor_vibration_second_min,
                R.integer.cursor_vibration_second_max );
        }

    /**
     * Checks and sets repeated vibration integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreVibrationRepeatPref( Context context )
        {
        return _checkAndStoreIntPref( context,
                R.string.cursor_vibration_repeat_key,
                CURSOR_VIBRATION_REPEAT_INT_KEY,
                R.string.cursor_vibration_repeat_default,
                R.integer.cursor_vibration_repeat_min,
                R.integer.cursor_vibration_repeat_max );
        }

    /**
     * Checks and sets elongation period integer preference
     * @param context context
     * @return integer value of the preference
     */
    private static int checkAndStoreElongationPeriodPref(Context context)
        {
        return _checkAndStoreIntPref( context,
                R.string.editing_elongation_period_key,
                EDITING_ELONGATION_PERIOD_INT_KEY,
                R.string.editing_elongation_period_default,
                R.integer.editing_elongation_period_min,
                R.integer.editing_elongation_period_max );
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

        _prepareDialogMessage( R.string.drawing_spedometer_limit_key,
                R.string.drawing_spedometer_limit_dialog_message,
                R.integer.drawing_spedometer_limit_min,
                R.integer.drawing_spedometer_limit_max );

        _prepareDialogMessage( R.string.touch_long_count_key,
                R.string.touch_long_count_dialog_message,
                R.integer.touch_long_count_min,
                R.integer.touch_long_count_max );

        _prepareDialogMessage( R.string.touch_press_count_key,
                R.string.touch_press_count_dialog_message,
                R.integer.touch_press_count_min,
                R.integer.touch_press_count_max );

        _prepareDialogMessage( R.string.touch_press_threshold_key,
                R.string.touch_press_threshold_dialog_message,
                R.integer.touch_press_threshold_min,
                R.integer.touch_press_threshold_max );

        _prepareDialogMessage( R.string.touch_stay_time_key,
                R.string.touch_stay_time_dialog_message,
                R.integer.touch_stay_time_min,
                R.integer.touch_stay_time_max );

        _prepareDialogMessage( R.string.touch_repeat_time_key,
                R.string.touch_repeat_time_dialog_message,
                R.integer.touch_repeat_time_min,
                R.integer.touch_repeat_time_max );

        _prepareDialogMessage( R.string.cursor_vibration_first_key,
                R.string.cursor_vibration_first_dialog_message,
                R.integer.cursor_vibration_first_min,
                R.integer.cursor_vibration_first_max );

        _prepareDialogMessage( R.string.cursor_vibration_second_key,
                R.string.cursor_vibration_second_dialog_message,
                R.integer.cursor_vibration_second_min,
                R.integer.cursor_vibration_second_max );

        _prepareDialogMessage( R.string.cursor_vibration_repeat_key,
                R.string.cursor_vibration_repeat_dialog_message,
                R.integer.cursor_vibration_repeat_min,
                R.integer.cursor_vibration_repeat_max );

        _prepareDialogMessage( R.string.editing_elongation_period_key,
                R.string.editing_elongation_period_dialog_message,
                R.integer.editing_elongation_period_min,
                R.integer.editing_elongation_period_max );

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
        // Test / Mode
        if ( key.equals( getString( R.string.test_mode_key )) || allKeys )
            {
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.test_mode_key ) );
            boolean value = sharedPrefs.getBoolean(getString(R.string.test_mode_key),
                    getResources().getBoolean(R.bool.test_mode_default));
            preference.setSummary( getString( value ?
                    R.string.test_mode_on : R.string.test_mode_off ));

            Scribe.note( Debug.PREF, "PREFERENCES: Test mode is set to: " + value );
            if ( !allKeys )     performAction( value ?
                    PREFS_ACTION_TEST_LOAD : PREFS_ACTION_TEST_RETURN );
            }

        // Descriptor / Working directory
        if ( key.equals( getString( R.string.descriptor_directory_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: working directory has changed!" );
            // If directory (and descriptor file) is valid,
            // then working directory for debug should be also changed
            // Debug is needed for preferences, too - so directory will be changed directly
            if ( checkDescriptorFilePreferences( sharedPrefs ) )
                {
                String directoryName =
                        sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                                getString( R.string.descriptor_directory_default ) );

                Scribe.setDirectoryName( directoryName ); // Primary directory name
                Scribe.setDirectoryNameSecondary( directoryName ); // Secondary directory name
                }

            // Test file should also be checked.
            checkTestFilePreferences( sharedPrefs );
            }

        // Descriptor / Coat descriptor file
        if ( key.equals( getString( R.string.descriptor_file_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: descriptor file has changed!" );
            // If new descriptor file is valid, then it should be reloaded
            if ( checkDescriptorFilePreferences( sharedPrefs ) && !allKeys )
                {
                // RELOAD only, if NOT in TEST mode
                if ( !((CheckBoxPreference)findPreference( getString(R.string.test_mode_key) )).isChecked() )
                    performAction( PREFS_ACTION_RELOAD );
                }
            }

        // Test / Coat descriptor file
        if ( key.equals( getString( R.string.test_file_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: test file has changed!" );
            // If new descriptor file is valid, then it should be reloaded
            if ( checkTestFilePreferences( sharedPrefs ) && !allKeys )
                {
                // RELOAD only, if in TEST mode
                if ( !((CheckBoxPreference)findPreference( getString(R.string.test_mode_key) )).isChecked() )
                    performAction( PREFS_ACTION_RELOAD );
                }
            }

        // Descriptor / Reload descriptor file
        // Defined in onCreate as Button

        // Descriptor / Copy assets
        // Defined in onCreate as Button

        // Drawing / Hide upper quoter
        if ( key.equals( getString( R.string.drawing_hide_upper_key )) || allKeys )
            {
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.drawing_hide_upper_key ) );
            boolean value = sharedPrefs.getBoolean(getString(R.string.drawing_hide_upper_key),
                    getResources().getBoolean(R.bool.drawing_hide_upper_default)); 
            preference.setSummary( getString( value ?
                    R.string.drawing_hide_upper_on : R.string.drawing_hide_upper_off ));

            Scribe.note( Debug.PREF, "PREFERENCES: Hide upper is set to: " + value );
            if ( !allKeys )     performAction(PREFS_ACTION_RECALCULATE);
            }

        // Drawing / Hide lower quoter
        if ( key.equals( getString( R.string.drawing_hide_lower_key )) || allKeys )
            {
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.drawing_hide_lower_key ) );
            boolean value = sharedPrefs.getBoolean(getString(R.string.drawing_hide_lower_key),
                    getResources().getBoolean(R.bool.drawing_hide_lower_default));
            preference.setSummary( getString( value ?
                    R.string.drawing_hide_lower_on : R.string.drawing_hide_lower_off ));

            Scribe.note( Debug.PREF, "PREFERENCES: Hide lower is set to: " + value );
            if ( !allKeys )     performAction(PREFS_ACTION_RECALCULATE);
            }

        // Drawing / Screen height ratio
        if ( key.equals( getString( R.string.drawing_height_ratio_key )) || allKeys )
            {
            int heightRatio = checkAndStoreHeightRatioPref(getActivity());

            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference(getString(R.string.drawing_height_ratio_key));
            preference.setSummary(getString(R.string.drawing_height_ratio_summary) + " " +
                    Integer.toString(heightRatio));
            Scribe.note( Debug.PREF, "PREFERENCES: Screen height ratio has changed: " + heightRatio);
            if ( !allKeys )     performAction(PREFS_ACTION_REDRAW);
            }

        // Drawing / Landscape offset for non-wide boards
        if ( key.equals( getString( R.string.drawing_landscape_offset_key )) || allKeys )
            {
            int landscapeOffset = checkAndStoreLandscapeOffsetPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.drawing_landscape_offset_key ) );
            preference.setSummary(getString(R.string.drawing_landscape_offset_summary) + " " +
                    Integer.toString(landscapeOffset));
            Scribe.note( Debug.PREF, "PREFERENCES: Landscape offset for non-wide boards has changed: " + landscapeOffset);
            if ( !allKeys )     performAction(PREFS_ACTION_RECALCULATE);
            }

        // Drawing / Outer rim ratio
        if ( key.equals( getString( R.string.drawing_outer_rim_key )) || allKeys )
            {
            int outerRim = checkAndStoreOuterRimPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.drawing_outer_rim_key ) );
            preference.setSummary(getString(R.string.drawing_outer_rim_summary) + " " +
                    Integer.toString(outerRim));
            Scribe.note( Debug.PREF, "PREFERENCES: Outer rim ratio has changed!" + outerRim);
            if ( !allKeys )     performAction(PREFS_ACTION_REDRAW);
            }

        // Drawing / Spedometer limit
        if ( key.equals( getString( R.string.drawing_spedometer_limit_key )) || allKeys )
            {
            int spedoLimit = checkAndStoreSpedometerLimitPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.drawing_spedometer_limit_key ) );
            preference.setSummary(getString(R.string.drawing_spedometer_limit_summary) + " " +
                    Integer.toString(spedoLimit));
            Scribe.note( Debug.PREF, "PREFERENCES: Spedometer limit has changed!" + spedoLimit);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Drawing / Monitor row
        if ( key.equals( getString( R.string.drawing_monitor_row_key )) || allKeys )
            {
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.drawing_monitor_row_key ) );
            boolean value = sharedPrefs.getBoolean(getString(R.string.drawing_monitor_row_key),
                    getResources().getBoolean(R.bool.drawing_monitor_row_default));
            preference.setSummary( getString( value ?
                    R.string.drawing_monitor_row_on : R.string.drawing_monitor_row_off ));

            Scribe.note( Debug.PREF, "PREFERENCES: Monitor row is set to: " + value );
            if ( !allKeys )     performAction(PREFS_ACTION_RECALCULATE);
            }

        // Touch / Long count
        if ( key.equals( getString( R.string.touch_long_count_key )) || allKeys )
            {
            int longCount = checkAndStoreLongCountPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference(getString(R.string.touch_long_count_key));
            preference.setSummary(getString(R.string.touch_long_count_summary) + " " +
                    Integer.toString(longCount));
            Scribe.note( Debug.PREF, "PREFERENCES: Long counter has changed!" + longCount);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Touch / Press counter
        if ( key.equals( getString( R.string.touch_press_count_key )) || allKeys )
            {
            int pressCount = checkAndStorePressCountPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.touch_press_count_key ) );
            preference.setSummary(getString(R.string.touch_press_count_summary) + " " +
                    Integer.toString(pressCount));
            Scribe.note( Debug.PREF, "PREFERENCES: Press counter has changed!" + pressCount);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Touch / Press threshold
        if ( key.equals( getString( R.string.touch_press_threshold_key )) || allKeys )
            {
            int pressThreshold = checkAndStorePressThresholdPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.touch_press_threshold_key ) );
            preference.setSummary(getString(R.string.touch_press_threshold_summary) + " " +
                    Integer.toString(pressThreshold));
            Scribe.note( Debug.PREF, "PREFERENCES: Press threshold has changed!" + pressThreshold);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Touch / Stay time
        if ( key.equals( getString( R.string.touch_stay_time_key )) || allKeys )
            {
            int stayTime = checkAndStoreStayTimePref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.touch_stay_time_key ) );
            preference.setSummary(getString(R.string.touch_stay_time_summary) + " " +
                    Integer.toString(stayTime));
            Scribe.note( Debug.PREF, "PREFERENCES: Stay time has changed!" + stayTime);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Touch / Repeat time
        if ( key.equals( getString( R.string.touch_repeat_time_key )) || allKeys )
            {
            int repeatTime = checkAndStoreRepeatTimePref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.touch_repeat_time_key ) );
            preference.setSummary(getString(R.string.touch_repeat_time_summary) + " " +
                    Integer.toString(repeatTime));
            Scribe.note( Debug.PREF, "PREFERENCES: Repeat time has changed!" + repeatTime);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Touch allow
        if ( key.equals( getString( R.string.cursor_touch_allow_key )) || allKeys )
            {
            Scribe.note( Debug.PREF, "PREFERENCES: Cursor touch indicator has changed!");

            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Stroke allow
        if ( key.equals( getString( R.string.cursor_stroke_allow_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: Cursor stroke indicator has changed!" );

            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Paths allow
        if ( key.equals( getString( R.string.cursor_paths_allow_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: Cursor paths indicator has changed!" );

            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Vibration allow
        if ( key.equals( getString( R.string.cursor_vibration_allow_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: Cursor vibration has changed!" );

            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Primary vibration
        if ( key.equals( getString( R.string.cursor_vibration_first_key )) || allKeys )
            {
            int vibrationFirst = checkAndStoreVibrationFirstPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.cursor_vibration_first_key ) );
            preference.setSummary(getString(R.string.cursor_vibration_first_summary) + " " +
                    Integer.toString(vibrationFirst));
            Scribe.note( Debug.PREF, "PREFERENCES: Primary vibration has changed!" + vibrationFirst);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Secondary vibration
        if ( key.equals( getString( R.string.cursor_vibration_second_key )) || allKeys )
            {
            int vibrationSecond = checkAndStoreVibrationSecondPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.cursor_vibration_second_key ) );
            preference.setSummary(getString(R.string.cursor_vibration_second_summary) + " " +
                    Integer.toString(vibrationSecond));
            Scribe.note( Debug.PREF, "PREFERENCES: Secondary vibration has changed!" + vibrationSecond);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Cursor / Repeated vibration
        if ( key.equals( getString( R.string.cursor_vibration_repeat_key )) || allKeys )
            {
            int vibrationRepeat = checkAndStoreVibrationRepeatPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.cursor_vibration_repeat_key ) );
            preference.setSummary(getString(R.string.cursor_vibration_repeat_summary) + " " +
                    Integer.toString(vibrationRepeat));
            Scribe.note( Debug.PREF, "PREFERENCES: Repeated vibration has changed!" + vibrationRepeat);
            if ( !allKeys )     performAction(PREFS_ACTION_REFRESH);
            }

        // Most editing preferences do not need any refresh, because initInput reads them
        // Editing / Text sesion is read out by softboarddata

        // Editing / Retrieve text
        if ( key.equals( getString( R.string.editing_retrieve_text_key)) || allKeys )
            {
            Preference preference = findPreference( getString( R.string.editing_retrieve_text_key) );
            String value = sharedPrefs.getString(getString(R.string.editing_retrieve_text_key),
                    getString(R.string.editing_retrieve_text_default));
            if ( value.startsWith("A") )
                preference.setSummary( getString(R.string.editing_retrieve_text_automatic));
            else if ( value.startsWith("E") )
                preference.setSummary(getString(R.string.editing_retrieve_text_enabled));
            else if ( value.startsWith("D") )
                preference.setSummary(getString(R.string.editing_retrieve_text_disabled));
            else
                preference.setSummary(getString(R.string.editing_retrieve_text_summary));

            Scribe.note( Debug.PREF, "PREFERENCES: Editing (retrieve text) has changed!" + value);
            }

        // Editing / Store text
        if ( key.equals( getString( R.string.editing_store_text_key)) || allKeys )
            {
            Preference preference = findPreference( getString( R.string.editing_store_text_key) );
            String value = sharedPrefs.getString(getString(R.string.editing_store_text_key),
                    getString(R.string.editing_store_text_default));
            if ( value.startsWith("A") )
                preference.setSummary( getString(R.string.editing_store_text_automatic));
            else if ( value.startsWith("E") )
                preference.setSummary(getString(R.string.editing_store_text_enabled));
            else if ( value.startsWith("D") )
                preference.setSummary(getString(R.string.editing_store_text_disabled));
            else
                preference.setSummary(getString(R.string.editing_store_text_summary));

            Scribe.note( Debug.PREF, "PREFERENCES: Editing (store text) has changed!" + value);
            }

        // Editing / elongation period
        if ( key.equals( getString( R.string.editing_elongation_period_key )) || allKeys )
            {
            int elongationPeriod = checkAndStoreElongationPeriodPref(getActivity());
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference(getString(R.string.editing_elongation_period_key));
            preference.setSummary(getString(R.string.editing_elongation_period_summary) + " " +
                    Integer.toString(elongationPeriod));
            Scribe.note( Debug.PREF, "PREFERENCES: Elongation period has changed!" + elongationPeriod);
            }

        // Editing / New text session
        if ( key.equals( getString( R.string.editing_text_session_key )) || allKeys )
            {
            // Cannot be null, if prefs.xml is valid
            Preference preference = findPreference( getString( R.string.editing_text_session_key ) );
            boolean value = sharedPrefs.getBoolean(getString(R.string.editing_text_session_key),
                    getResources().getBoolean(R.bool.editing_text_session_default));
            preference.setSummary( getString( value ?
                    R.string.editing_text_session_on : R.string.editing_text_session_off ));

            Scribe.note( Debug.PREF, "PREFERENCES: New text session is set to: " + value );
            if ( !allKeys )     performAction( PREFS_ACTION_REFRESH );
            }

        // Debug
        if ( key.equals( getString( R.string.debug_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: debug has changed!" );

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

        // Debug / Grid title
        if ( key.equals( getString( R.string.debug_grid_title_key )) || allKeys )
            {
            Scribe.note( Debug.PREF,  "PREFERENCES: Grid title has changed!" );

            if ( !allKeys )     performAction(PREFS_ACTION_REDRAW);
            }

        }

    /**
     * Checking and validating directory preference.
     * This method now is only used in PrefsFragment, parsing will check the files once more.
     * If it will be used more generally, then getAction() should be checked against null!
     */
    private File checkDirectoryPreference( SharedPreferences sharedPrefs )
        {
        // These cannot be null, if prefs.xml is valid
        Preference directoryPreference = findPreference( getString( R.string.descriptor_directory_key ) );

        // Originally all preferences handled as invalid
        directoryPreference.setSummary( getString( R.string.descriptor_directory_summary_invalid ) );

        // Working directory is checked
        // It can change later, so parsing will check it again

        String directoryName =
                sharedPrefs.getString( getString( R.string.descriptor_directory_key ), "" );
        File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

        if ( !directoryFile.exists() || !directoryFile.isDirectory() )
            {
            Scribe.error( "PREFERENCES: working directory is missing: " +
                    directoryFile.getAbsolutePath() );
            return null;
            }

        directoryPreference.setSummary( getString( R.string.descriptor_directory_summary ) + " " +
                directoryName );

        return directoryFile;
        }

    /**
     * Checking and validating descriptor file preferences. Directory is also checked.
     * This method now is only used in PrefsFragment, parsing will check the files once more.
     * If it will be used more generally, then getAction() should be checked against null!
     */
    private boolean checkDescriptorFilePreferences( SharedPreferences sharedPrefs )
        {
        // These cannot be null, if prefs.xml is valid
        Preference filePreference = findPreference( getString( R.string.descriptor_file_key ) );
        Preference reloadPreference = findPreference( getString( R.string.descriptor_reload_key ) );

        // Originally all preferences handled as invalid
        filePreference.setSummary( getString( R.string.descriptor_file_summary_invalid ) );

        Scribe.debug(Debug.PREF, "Reload disabled!");
        reloadPreference.setEnabled( false );

        // Working directory is checked
        // It can change later, so parsing will check it again
        File directoryFile = checkDirectoryPreference( sharedPrefs );
        if (directoryFile == null)
            return false;

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

        Scribe.debug( Debug.PREF, "Reload enabled!");
        reloadPreference.setEnabled( true );

        return true;
        }

    /**
     * Checking and validating test file preferences. Directory is also checked.
     * If test file is missing, then test mode is disabled.
     * This method now is only used in PrefsFragment, parsing will check the files once more.
     * If it will be used more generally, then getAction() should be checked against null!
     */
    private boolean checkTestFilePreferences( SharedPreferences sharedPrefs )
        {
        // These cannot be null, if prefs.xml is valid
        Preference testFilePreference = findPreference( getString( R.string.test_file_key ) );
        CheckBoxPreference testModePreference =
                (CheckBoxPreference)findPreference( getString( R.string.test_mode_key ) );

        String testName =
                sharedPrefs.getString(getString(R.string.test_file_key), "");

        if (testName.isEmpty())
            {
            // TEST FILE IS EMPTY
            testFilePreference.setSummary(getString(R.string.test_file_summary_empty));
            Scribe.debug(Debug.PREF, "PREFERENCES: test file is empty");
            }
        else
            {
            // SUPPOSE, THAT TEST FILE IS INVALID
            testFilePreference.setSummary(getString(R.string.test_file_summary_invalid));

            // Working directory is checked
            // It can change later, so parsing will check it again
            File directoryFile = checkDirectoryPreference(sharedPrefs);
            if (directoryFile != null)
                {
                // Directory is valid, test file is checked
                // It can change later, so parsing will check it again

                // test file can be empty, but in test mode it cannot be deleted
                File testFile = new File(directoryFile, testName);

                if (testFile.exists() && testFile.isFile())
                    {
                    // TEST FILE IS OK - TEST MODE IS ENABLED
                    testFilePreference.setSummary(getString(R.string.test_file_summary) + " " +
                            testFile.getAbsolutePath());
                    testModePreference.setEnabled(true);
                    return true;
                    }
                else
                    {
                    Scribe.error("PREFERENCES: test file is missing: " +
                            testFile.getAbsolutePath());
                    }
                }
            }

        // TEST FILE IS EMPTY OR INVALID

        testModePreference.setChecked( false );
        testModePreference.setEnabled( false );

        return false;
        }

    /**
     ** ACTIONS OF THE SERVICE, FORCED BY PREFERENCE CHANGES
     **/

    /**
     * Helper method to call similar method inside of PrefsFragment
     * @param type action type
     */
    private void performAction( int type )
        {
        performAction( getActivity(), type);
        }

    /**
     * Notifies server to react preference changes - this can be called outside of PrefsFragment
     * @param type action type
     */
    public static void performAction( Context context, int type )
        {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putInt( PREFS_COUNTER, sharedPrefs.getInt( PREFS_COUNTER, 0 ) + 1 );
        editor.putInt( PREFS_TYPE, type );

        editor.apply();

        switch ( type )
            {
            case PREFS_ACTION_RELOAD:
                Scribe.note( Debug.PREF, "PREFERENCE: server is notified to reload descriptor.");
                break;

            case PREFS_ACTION_RECALCULATE:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to recalculate layouts." );
                break;

            case PREFS_ACTION_REDRAW:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to redraw layouts." );
                break;

            case PREFS_ACTION_REFRESH:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to refresh preferences." );
                break;

            case PREFS_ACTION_CLEAR_SPEDOMETER:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to clear spedometer data." );
                break;

/*            case PREFS_ACTION_STORE_DATA:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to store softboarddata." );
                break;

            case PREFS_ACTION_RECALL_DATA:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to recall softboarddata." );
                break;
*/
            case PREFS_ACTION_TEST_LOAD:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to load test file in test mode." );
                break;

            case PREFS_ACTION_TEST_RETURN:
                Scribe.note( Debug.PREF,  "PREFERENCE: server is notified to return from test mode." );
                break;

            default:
                Scribe.error( "PREFERENCE: preference action type is invalid!");
            }
        }


    /**
     ** STANDARD METHODS CONNECT FRAGMENT TO ANDROID SYSTEM
     **/

    private static final int FILE_SELECTOR_REQUEST = 1;
    private static final int TEST_SELECTOR_REQUEST = 2;


    public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
        Scribe.locus( Debug.PREF );

        if (requestCode == FILE_SELECTOR_REQUEST && resultCode == Activity.RESULT_OK)
            {
            //String result=data.getStringExtra("RESULT");

            //visszateres data reszben
            String result = data.getData().getPath();
            //Toast.makeText(getActivity(), "File Clicked: " + result, Toast.LENGTH_LONG).show();

/*
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPrefs.edit();

            String fileName = data.getStringExtra(FileSelectorActivity.FILE_NAME);
            if (fileName != null)
                editor.putString(getString(R.string.descriptor_file_key), fileName);

            String subDirectory = data.getStringExtra(FileSelectorActivity.DIRECTORY_SUB_PATH);
            if (subDirectory != null)
                editor.putString(getString(R.string.descriptor_directory_key), subDirectory);

            editor.apply();
*/

            String subDirectory = data.getStringExtra(FileSelectorActivity.DIRECTORY_SUB_PATH);
            EditTextPreference descriptorDirectoryPreference =
                    (EditTextPreference)findPreference( getString(R.string.descriptor_directory_key) );
            descriptorDirectoryPreference.setText( subDirectory );

            String fileName = data.getStringExtra(FileSelectorActivity.FILE_NAME);
            EditTextPreference descriptorFilePreference =
                    (EditTextPreference)findPreference( getString(R.string.descriptor_file_key) );
            descriptorFilePreference.setText( fileName );

//                Scribe.debug(Debug.PREF, sharedPrefs.getString(getString(R.string.descriptor_file_key), ""));
//                Scribe.debug(Debug.PREF, sharedPrefs.getString(getString(R.string.descriptor_directory_key), ""));

            performAction(PREFS_ACTION_RELOAD);
            }

        else if (requestCode == TEST_SELECTOR_REQUEST && resultCode == Activity.RESULT_OK)
            {
            String fileName = data.getStringExtra(FileSelectorActivity.FILE_NAME);
            EditTextPreference testFilePreference =
                    (EditTextPreference)findPreference( getString(R.string.test_file_key) );
            testFilePreference.setText( fileName );

            // Theoretically this is a valid test file
            // Set TEST MODE on!

            CheckBoxPreference testModePreference = (CheckBoxPreference)findPreference( getString(R.string.test_mode_key) );
            testModePreference.setEnabled( true ); // Setting testFilePreference could set it on already
            testModePreference.setChecked( true );

            performAction(PREFS_ACTION_TEST_LOAD);
            }

        else if (resultCode == Activity.RESULT_CANCELED) // Any request code
            {
            Toast.makeText(getActivity(), "- C A N C E L -", Toast.LENGTH_SHORT).show();
            }
        }


    /**
     * Preferences are initialized
     * @param savedInstanceState - not used
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        Scribe.locus(Debug.PREF);

        // Load preferences from resources
        addPreferencesFromResource(R.xml.prefs);

        // Preference as button - only click behavior is used
        findPreference(getString(R.string.help_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                @Override
                public boolean onPreferenceClick(Preference preference)
                    {
                    // getActivity() cannot be null, when button is displayed
                    Intent intent = new Intent( getActivity(), WebViewActivity.class);
                    intent.putExtra( WebViewActivity.WORK, "help.html" );
                    startActivity(intent);
                    return true;
                    }
                });

        // Preference as button - only click behavior is used
        findPreference(getString(R.string.settings_input_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                    {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                        {
                    // getActivity() cannot be null, when button is displayed
//                    Intent intent = new Intent();
//                    intent.setAction(Settings.ACTION_INPUT_METHOD_SETTINGS);
                    // per doc activity may not exist
                    // intent.resolveActivity(packageManager) can be helpful
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);

Intent intent = new Intent( getActivity(), RequestPermissionActivity.class);
startActivity(intent);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                    return true;
                    }
                });

        findPreference(getString(R.string.descriptor_select_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                @Override
                public boolean onPreferenceClick(Preference preference)
                    {
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String directoryName =
                            sharedPrefs.getString( getString( R.string.descriptor_directory_key ),
                                    getString( R.string.descriptor_directory_default ));
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), FileSelectorActivity.class);
                    intent.putExtra( FileSelectorActivity.DIRECTORY_SUB_PATH, directoryName);
                    // intent.putExtra( FileSelectorActivity.FILE_ENDING, ".txt");
                    startActivityForResult(intent, FILE_SELECTOR_REQUEST);
                    return true;
                    }
                });

        findPreference(getString(R.string.test_select_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                    {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                        {
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        if ( checkDirectoryPreference( sharedPrefs ) != null )
                            {
                            String directoryName =
                                    sharedPrefs.getString(getString(R.string.descriptor_directory_key),
                                            getString(R.string.descriptor_directory_default));
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), FileSelectorActivity.class);
                            intent.putExtra(FileSelectorActivity.DIRECTORY_SUB_PATH, directoryName);
                            intent.putExtra(FileSelectorActivity.ONE_DIRECTORY, true);
                            // intent.putExtra( FileSelectorActivity.FILE_ENDING, ".txt");
                            startActivityForResult(intent, TEST_SELECTOR_REQUEST);
                            }
                        else
                            {
                            Toast.makeText(getActivity(), "Directory is not valid!", Toast.LENGTH_SHORT).show();
                            }
                        return true;
                        }
                    });

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

        findPreference(getString(R.string.descriptor_assets_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                @Override
                public boolean onPreferenceClick(Preference preference)
                    {
                    // getActivity() cannot be null, when button is displayed
                    Ignition.copyAssets(getActivity());

                    EditTextPreference descriptorDirectoryPreference =
                            (EditTextPreference)findPreference( getString(R.string.descriptor_directory_key) );
                    descriptorDirectoryPreference.setText( getString( R.string.descriptor_directory_default ) );

                    EditTextPreference descriptorFilePreference =
                            (EditTextPreference)findPreference( getString(R.string.descriptor_file_key) );
                    descriptorFilePreference.setText( getString( R.string.descriptor_file_default ) );

                    performAction(PREFS_ACTION_RELOAD);
                    Scribe.debug(Debug.PREF, "Directory and descriptor file are reset to their default values. ");
                    return true;
                    }
                });

        findPreference(getString(R.string.drawing_spedometer_clear_key)).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                @Override
                public boolean onPreferenceClick(Preference preference)
                    {
                    // getActivity() cannot be null, when button is displayed
                    performAction(PREFS_ACTION_CLEAR_SPEDOMETER);
                    return true;
                    }
                });

        // Prepare dialog messages
        prepareIntPrefsDialogMessage();
        }


    /**
     * ShortCutEntry point of preference fragment
     * Registers on SharedPreferenceChangeListener
     * (getActivity() cannot be null in onResume())
     */
    @Override
    public void onResume()
        {
        super.onResume();
        Scribe.locus(Debug.PREF);

        // Preferences (descriptor file) is checked and summaries are updated
        // Needed here, because preferences could change during pause or in onActivityResult
        checkPrefs(PreferenceManager.getDefaultSharedPreferences(getActivity()),
                "", true);

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
        Scribe.locus( Debug.PREF );

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
        Scribe.locus( Debug.PREF );
        checkPrefs(sharedPrefs, key, false);
        }

    }

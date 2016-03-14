package org.lattilad.bestboard.debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.scribe.Scribe;

/**
 * Collection of message-limit constants for Scribe and
 * Scribe initialisation.
 */
public class Debug
	{
	// Constants for PRIMARY configuration
    private static final String LOG_TAG = "SCRIBE_BEST";

	// Constants for SECONDARY configuration
    private static final String coatLogFileName = "coat.log";
    private static final String LOG_TAG_COAT = "SCRIBE_COAT";

    public static final String tokenLogFileName = "token.log";
    public static final String LOG_TAG_TOKEN = "TOKEN";

    // Settings of debug levels - only EVEN numbers!
    private static final int LIMIT = 500;

    public static final int PREF = 610;
    public static final int LAYOUT = 620;
    public static final int TOUCH = 24;
    public static final int TOUCH_VERBOSE = 24;
    public static final int DRAW =26;
    public static final int DRAW_VERBOSE = 26;
    public static final int VIEW = 630;
    public static final int COMMANDS = 40;
    public static final int IGNITION = 650;
    public static final int DATA = 60;
    public static final int PARSER = 670;
    public static final int BLOCK = 670;
    public static final int SERVICE = 680;
    public static final int TEXT = 690;
    public static final int CURSOR = 690;


    public static final int LAYOUTSTATE = 660;
    public static final int CAPSSTATE = 660;
    public static final int HARDSTATE = 660;
    public static final int METASTATE = 660;
    public static final int BOARDTABLE = 660;

    public static final int BUTTON = 60;

    // Other settings


	/**
	 * Scribe primary and secondary config initialisation.
	 * !! If there are more entry points, during initial settings, 
	 * messages could be delivered to default log-file. This should be fixed later !! 
	 * @param context context containing package information
	 */
	public static void initScribe( Context context )
		{
        // Scribe initialization: PRIMARY - debug
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );

        String directoryName =
                sharedPrefs.getString( context.getString( R.string.descriptor_directory_key ),
                        context.getString( R.string.descriptor_directory_default ) );

        boolean enabled =
                sharedPrefs.getBoolean( context.getString( R.string.debug_key ),
                        context.getResources().getBoolean( R.bool.debug_default ));

        Scribe.setConfig()
                .enable( enabled )
                .setDirectoryName( directoryName )      // Primary directory name
                .enableSysLog( LOG_TAG )                // Primary log-tag : BEST
                .setLimit( LIMIT )
                .init( context );                       // Primary file name : package name

		// !! Service will be started only once, so this should go into a more frequent position
        // InputMethodService.onWindowHidden() or .onFinishInput() could be a good place.
		Scribe.checkLogFileLength(); // Primary log will log several runs
		Scribe.logUncaughtExceptions(); // Primary log will store uncaught exceptions


        // Scribe initialization: SECONDARY - log for user
        Scribe.setConfig()
                .setDirectoryName( directoryName )      // Secondary directory name should be given
                .enableFileLog( coatLogFileName )       // Secondary file name : "coat.log"
                .enableSysLog( LOG_TAG_COAT )           // Secondary log-tag : "COAT"
                .initSecondary();

        Scribe.clear_secondary(); // Secondary config will store data from ONE run
		}
	
	}

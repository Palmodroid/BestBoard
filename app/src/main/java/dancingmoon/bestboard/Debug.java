package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dancingmoon.bestboard.scribe.Scribe;

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

    // Settings of debug levels
    public static final int PREF = 10;
    public static final int BOARD = 520;
    public static final int VIEW = 530;
    public static final int COMMANDS = 40;
    public static final int IGNITION = 50;
    public static final int DATA = 60;
    public static final int PARSER = 70;
    public static final int SERVICE = 80;
    public static final int TEXT = 90;

    public static final int BOARDSTATE = 100;
    public static final int CAPSSTATE = 100;
    public static final int HARDSTATE = 100;
    public static final int LINKSTATE = 100;
    public static final int METASTATE = 100;

    public static final int BUTTON = 110;

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
                .setLimit( 500 )
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

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
    private static final String LOG_TAG = "BEST";

	// Constants for SECONDARY configuration
    private static final String coatLogFileName = "coat.log";
    private static final String LOG_TAG_COAT = "COAT";

    public static final String tokenLogFileName = "token.log";
    public static final String LOG_TAG_TOKEN = "TOKEN";

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
		Scribe.init(context); // Primary file name : package name

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );

		String directoryName =
				sharedPrefs.getString( context.getString( R.string.descriptor_directory_key ), context.getString( R.string.descriptor_directory_default ) );
		Scribe.setDirectoryName( directoryName ); // Primary directory name

		Scribe.clearSysLog();
		Scribe.enableSysLog( LOG_TAG ); // Primary log-tag : BESTBOARD

		// !! Service will be started only once, so this should go into a more frequent position
        // InputMethodService.onWindowHidden() or .onFinishInput() could be a good place.
		Scribe.checkLogFileLength(); // Primary log will log several runs

		Scribe.logUncaughtExceptions(); // Primary log will store uncaught exceptions

        if ( sharedPrefs.getBoolean( context.getString( R.string.debug_key ), true ) )
            {
            Scribe.enable();
            }
        else
            {
            Scribe.disable();
            }

		Scribe.title("Best's Board started!");

        // Scribe initialization: SECONDARY - log for user
        Scribe.enableFileLogSecondary(coatLogFileName); // Secondary log activated, all other data come from primary log
        // Scribe.setDirectoryNameSecondary( directoryName ); // This is not necessary, directory name comes from primary log
        Scribe.clear_secondary(); // Secondary config will store data from ONE run
        Scribe.enableSysLogSecondary( LOG_TAG_COAT ); // Scribe.disableSysLogSecondary(); // Scribe.enableSysLogSecondary( LOG_TAG_COAT );
		}
	
	}

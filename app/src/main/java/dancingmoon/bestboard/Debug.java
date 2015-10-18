package dancingmoon.bestboard;

import android.content.Context;

import dancingmoon.bestboard.scribe.Scribe;

/**
 * Collection of message-limit constants for Scribe and
 * Scribe initialisation.
 */
public class Debug
	{
    // Logs are stored in working directory
    // Get it from main file!!
    // public static final String WORKING_DIRECTORY = "_bestboard";

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
		Scribe.setDirectoryName( SoftBoardService.WORKING_DIRECTORY ); // Primary directory name

		Scribe.clearSysLog();
		Scribe.enableSysLog( LOG_TAG ); // Primary log-tag : BESTBOARD
		
		Scribe.checkLogFileLength(); // Primary log will log several runs
		Scribe.logUncaughtExceptions(); // Primary log will store uncaught exceptions
		
		Scribe.title("Best's Board started!");

        // Scribe initialization: SECONDARY - log for user
        Scribe.enableFileLogSecondary(coatLogFileName); // Secondary log activated, all other data come from primary log
        // Scribe.setDirectoryNameSecondary( directoryName ); // This is not necessary, directory name comes from primary log
        Scribe.clear_secondary(); // Secondary config will store data from ONE run
        Scribe.enableSysLogSecondary( LOG_TAG_COAT ); // Scribe.disableSysLogSecondary(); // Scribe.enableSysLogSecondary( LOG_TAG_COAT );
		}
	
	}

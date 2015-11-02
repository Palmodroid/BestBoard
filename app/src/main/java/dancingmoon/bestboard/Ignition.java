package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;

import dancingmoon.bestboard.scribe.Scribe;

/**
 * Ignition.start() should be called at every entry points of the program.
 */
public class Ignition
    {
    /**
     * Initialization of the whole system.
     * This should be called at every staring point!
     */
    public static void start( Context context )
        {
        // initScribe should be started before any use of Scribe
        // This could come BEFORE PrefsFragment.initDefaultPrefs(context),
        // because initScribe uses default values from xml
        Debug.initScribe( context );

        // Default and integer preferences should be initialized first
        // Check whether this is the very first start
        if ( PrefsFragment.init(context) )
            {
            copyAssetFiles( context );
            }
        }

    /**
     * http://stackoverflow.com/a/11212942 - copy asset folder
     * http://stackoverflow.com/a/6187097 - compressed files in assets
     * @param context context
     */
    public static void copyAssetFiles( Context context )
        {
        // Check working directory
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );

        String directoryName =
                sharedPrefs.getString( context.getString( R.string.descriptor_directory_key ),
                        context.getString( R.string.descriptor_directory_default ));
        File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

        if ( !directoryFile.exists() )
            {
            Scribe.error("Creating working directory: " + directoryName);
            // Create even whole directory structure
            directoryFile.mkdirs();
            }

        // mkdirs() can also fail
        if ( !directoryFile.isDirectory() )
            {
            // Serious error!
            Scribe.enableToastLog( context );
            Scribe.error("Working directory cannot be used with these settings. Please, check directory: " +
                    directoryFile.getAbsolutePath());
            Scribe.disableToastLog();
            return;
            }

        // Working directory is ready

        Scribe.note("Copying files from asset. Target directory: " + directoryFile.getAbsolutePath() );

        AssetManager assetManager = context.getAssets();

        try
            {
            String[] assetNames = assetManager.list( "" );

            for ( String assetName : assetNames )
                {
                Scribe.debug( "Asset file: " + assetName );
                }
            }
        catch ( IOException e )
            {
            e.printStackTrace();
            }

        /*
        AssetFileDescriptor afd = null;
        try {
        afd = am.openFd( "MyFile.dat");

        // Create new file to copy into.
        File file = new File(Environment.getExternalStorageDirectory() + java.io.File.separator + "NewFile.dat");
        file.createNewFile();

        copyFdToFile(afd.getFileDescriptor(), file);

        } catch (IOException e) {
        e.printStackTrace();
        }
        */
        }

    }

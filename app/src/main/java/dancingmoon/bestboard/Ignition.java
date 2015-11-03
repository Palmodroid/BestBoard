package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            copyAssets( context );
            }
        }

    /**
     * http://stackoverflow.com/a/11212942 - copy asset folder
     * http://stackoverflow.com/a/6187097 - compressed files in assets
     * http://stackoverflow.com/a/27673773 - assets should be created below main
     * @param context context
     */
    public static void copyAssets( Context context )
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
                copyAssetFile( assetManager, assetName, directoryFile );
                }
            }
        catch ( IOException e )
            {
            e.printStackTrace();
            }
        }

    private static void copyAssetFile( AssetManager assetManager, String assetName, File targetDirectory )
        {
        File targetFile = new File( targetDirectory, assetName );

        if ( targetFile.exists() )
            backupFile( targetFile );

        StringBuilder stringBuilder = new StringBuilder( name );

        int n = 0;
        File backup;
        do
            {
            backup = new File( dir, stringBuilder.append( n++ ).toString() );
            } while ( backup.exists() );








        InputStream inputStream = null;
        OutputStream outPutStream = null;

        try
            {
            InputStream input = assetManager.open( assetName );

            if (input.read() == -1)
                {
                Scribe.debug("Asset file: " + assetName + " is not a valid file!");
                }
            else
                {
                Scribe.debug("Asset file: " + assetName + " is OK!");
                }
            input.close();
            }
        catch ( FileNotFoundException fnfe )
            {
            Scribe.debug("Asset file: " + assetName + " cannot be found!");
            }
        catch ( IOException ioe )
            {

            }
        }

    private static void backupFile( File file )
        {







        }



    }

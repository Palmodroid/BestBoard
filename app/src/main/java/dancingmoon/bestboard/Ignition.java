package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        Debug.initScribe(context);

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
        Scribe.note("Copying files from asset.");

        // Check working directory
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );

        String directoryName =
                sharedPrefs.getString( context.getString( R.string.descriptor_directory_key ),
                        context.getString( R.string.descriptor_directory_default ));
        File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

        if ( !directoryFile.exists() )
            {
            Scribe.note("Creating working directory:" + directoryName);
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
        Scribe.note("Working directory is ready: " + directoryFile.getAbsolutePath() );

        // Copying each file from assets
        try
            {
            AssetManager assetManager = context.getAssets();
            String[] assetNames = assetManager.list("");

            for ( String assetName : assetNames )
                {
                copyAssetFile( assetManager, assetName, directoryFile );
                }
            }
        catch ( IOException e )
            {
            // Serious error!
            Scribe.enableToastLog( context );
            Scribe.error("Could not copy files to sdcard! Keyboard cannot be used without sdcard.");
            Scribe.disableToastLog();
            }
        }


    private static void copyAssetFile( AssetManager assetManager, String assetName, File targetDirectory )
            throws IOException
        {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
            {
            inputStream = assetManager.open( assetName );

            Scribe.note("Copying asset: " + assetName);

            File targetFile = new File( targetDirectory, assetName );

            File backupFile = null;
            String backupString;
            if ( targetFile.exists() )
                {

                // compare these files













                StringBuilder backupNameBuilder = new StringBuilder();
                int n = 0;
                do
                    {
                    backupNameBuilder.setLength(0);
                    backupString = backupNameBuilder.append( assetName ).append( n++ ).toString();
                    backupFile = new File( targetDirectory, backupString );
                    } while ( backupFile.exists() );

                targetFile.renameTo( backupFile );
                Scribe.note("Target file backup: " + backupString);
                }

            outputStream = new FileOutputStream( targetFile );

            byte[] buffer = new byte[1024];
            int read;
            while((read = inputStream.read(buffer)) != -1)
                {
                outputStream.write(buffer, 0, read);
                }
            }
        catch ( FileNotFoundException fnfe)
            {
            Scribe.note("Asset skipped: " + assetName);
            }
        finally
            {
            if ( outputStream != null )
                {
                try
                    {
                    outputStream.close();
                    }
                catch ( IOException ioe )
                    {
                    ; // do nothing, this error cannot be noted
                    }
                }
            if ( inputStream != null )
                {
                try
                    {
                    inputStream.close();
                    }
                catch ( IOException ioe )
                    {
                    ; // do nothing, this error cannot be noted
                    }
                }
            }
        }
    }

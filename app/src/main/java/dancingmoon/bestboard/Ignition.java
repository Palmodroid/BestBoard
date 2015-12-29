package dancingmoon.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.prefs.PrefsFragment;
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
        Scribe.locus( Debug.IGNITION );
        Scribe.debug( Debug.IGNITION, "Copying files from asset.");

        // Check working directory
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( context );

        String directoryName =
                sharedPrefs.getString( context.getString( R.string.descriptor_directory_key ),
                        context.getString( R.string.descriptor_directory_default ));
        File directoryFile = new File( Environment.getExternalStorageDirectory(), directoryName );

        if ( !directoryFile.exists() )
            {
            Scribe.debug( Debug.IGNITION, "Could not find directory. Working directory is created:" + directoryName);
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
        Scribe.debug( Debug.IGNITION, "Working directory is ready: " + directoryFile.getAbsolutePath() );

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


    /**
     * Copy one asset file to target directory.
     * If a file with the same name could be found in target directory,
     * it will be checked first.
     * If the two files are identical, then no copy is needed.
     * If the two files are not the same, then first a backup will be created from target file.
     * @param assetManager asset manager to reach assets
     * @param assetName name of the file (it will be skipped, if this is not a valid file)
     * @param targetDirectory target directory
     * @throws IOException if reading error occurs
     */
    private static void copyAssetFile( AssetManager assetManager, String assetName, File targetDirectory )
            throws IOException
        {
        InputStream assetStream = null;
        BufferedInputStream assetBufferedStream = null;
        BufferedInputStream targetBufferedStream = null;
        OutputStream outputStream = null;

        try
            {
            // Open will throw FileNotFoundException, if this is not a valid file
            assetStream = assetManager.open( assetName );

            Scribe.debug( Debug.IGNITION, "Copying asset: " + assetName);

            File targetFile = new File( targetDirectory, assetName );

            File backupFile = null;
            String backupString;

            // if target file already exists...
            if ( targetFile.exists() )
                {
                // compare these files
                targetBufferedStream = new BufferedInputStream( new FileInputStream( targetFile ) );
                assetBufferedStream = new BufferedInputStream( assetStream );

                // ... and it is identical with asset - copy should stop
                if ( compareStreams( assetBufferedStream, targetBufferedStream ) )
                    {
                    Scribe.debug( Debug.IGNITION, "Asset and target files are identical, no copy is needed:" + assetName);
                    return;
                    }
                // ... and it is not identical with asset - it should be backed up
                else
                    {
                    StringBuilder backupNameBuilder = new StringBuilder();
                    int n = 0;
                    do  {
                        backupNameBuilder.setLength( 0 );
                        backupString = backupNameBuilder
                                .append( n++ )
                                .append( '_' )
                                .append( assetName ).toString();
                        backupFile = new File( targetDirectory, backupString );
                        } while ( backupFile.exists() );
                    targetFile.renameTo( backupFile );
                    Scribe.debug( Debug.IGNITION,  "Target file with same name is backed up: " + backupString );
                    }
                }

            assetStream.reset();
            outputStream = new FileOutputStream( targetFile );

            copyStreams( assetStream, outputStream );
            Scribe.debug( Debug.IGNITION,  "Asset file is copied: " + assetName );
            }
        catch ( FileNotFoundException fnfe)
            {
            Scribe.debug( Debug.IGNITION, "Asset is skipped: " + assetName);
            }
        finally
            {
            // Scribe.debug( Debug.IGNITION,  "Closing streams silently" );
            closeSilently( outputStream );
            closeSilently( targetBufferedStream );
            closeSilently( assetBufferedStream );
            // if assetBufferedStream is not null, then assetStream will be already closed
            closeSilently( assetStream );
            }
        }


    /**
     * Helper method to close a stream without throwing IOException.
     * Android supports try-with only above API 19!
     * @param closeable stream to close
     */
    private static void closeSilently( Closeable closeable )
        {
        if ( closeable != null )
            {
            try
                {
                closeable.close();
                }
            catch ( IOException ioe )
                {
                ; // do nothing, this error cannot be noted
                }
            }
        }


    /**
     * Compares two streams. The two streams should be buffered.
     * @param streamA One stream
     * @param streamB Other stream
     * @return true, if streams are identical, false otherwise
     * @throws IOException if reading error occurs
     */
    private static boolean compareStreams( InputStream streamA, InputStream streamB )
            throws IOException
        {
        int data;

        do
            {
            if ( ( data = streamA.read() ) != streamB.read() )
                return false;
            } while ( data != -1 );

        return true;
        }


    /** Copies inputStream to outputStream. Streams should not be buffered.
     * @param inputStream input stream
     * @param outputStream output stream
     * @throws IOException if reading error occurs
     */
    private static void copyStreams( InputStream inputStream, OutputStream outputStream )
            throws IOException
        {
        byte[] buffer = new byte[1024];
        int read;
        while((read = inputStream.read(buffer)) != -1)
            {
            outputStream.write(buffer, 0, read);
            }
        }

    }

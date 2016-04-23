package org.lattilad.bestboard.buttons;

import android.content.Intent;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Run external programs
 */
public class PacketRun extends Packet
    {
    String packageName;
    String name;

    public PacketRun( SoftBoardData softBoardData, String packageName )
        {
        super( softBoardData );
        this.packageName = packageName;

        int index = packageName.lastIndexOf('.');
        name = StringUtils.abbreviateString( index > 0 ?
            packageName.substring(index +1) : packageName, 5 );
        }

    /**
     * String representation of the data (title)
     * @return String representation of this packet
     */
    @Override
    public String getString()
        {
        return name;
        }

    /**
     * Send data to the editor field
     */
    @Override
    public void send()
        {
        Intent intent =
                softBoardData.softBoardListener.getApplicationContext()
                        .getPackageManager().getLaunchIntentForPackage( packageName );

        if ( intent != null )
            softBoardData.softBoardListener.getApplicationContext().startActivity(intent);
        else
            {
            Scribe.enableToastLog(softBoardData.softBoardListener.getApplicationContext());
            Scribe.error("Could not run: " + packageName + "!");
            Scribe.disableToastLog();
            }
        }

    @Override
    public void release()
        {
        }

    }

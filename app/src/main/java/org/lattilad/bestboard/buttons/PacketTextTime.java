package org.lattilad.bestboard.buttons;
// ( DateFormat.getDateInstance().format(new Date()) );
// SimpleDateFormat sdf=new SimpleDateFormat(df);
// ed2.setText( sdf.format(new Date()));

/**
 * Time as formatted string to be sent to the editor
 */
import org.lattilad.bestboard.SoftBoardData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PacketTextTime extends PacketTextBase
    {
    /* time format, or null for auto-format */
    String format = null;
    String time;

    public PacketTextTime( SoftBoardData softBoardData, String format )
        {
        super( softBoardData, "TIME" );
        this.format = format;
        }

    protected String getString()
        {
        return time;
        }

    private void setTimeString()
        {
        try
            {
            if ( format == null )
                {
                time = DateFormat.getDateInstance().format(new Date());
                }
            else
                {
                SimpleDateFormat sdf=new SimpleDateFormat( format );
                time = sdf.format(new Date());
                }
            }
        catch (Exception ex)
            {
            time = "[ERROR]";
            }
        }

    @Override
    public void send()
        {
        setTimeString();
        super.send();
        }

    @Override
    public void sendSecondary( int second )
        {
        // ?? Other format ??
        }

    }

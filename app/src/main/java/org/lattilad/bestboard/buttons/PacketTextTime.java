package org.lattilad.bestboard.buttons;
// ( DateFormat.getDateInstance().format(new Date()) );
// SimpleDateFormat sdf=new SimpleDateFormat(df);
// ed2.setText( sdf.format(new Date()));

/**
 * Time as formatted string to be sent to the editor
 */
import org.lattilad.bestboard.*;
import java.text.*;
import java.util.*;

public class PacketTextTime extends PacketText
    {
    /* time format, or null for auto-format */
    String format = null;

    
    public PacketTextTime(SoftBoardData softBoardData, String format, int autoCaps, int autoSpace)
        {
        super( softBoardData, null, autoCaps, false, autoSpace );
        this.format = format;
        }
        
    @Override
    public String getString()
        {
        return "TIME";
        }

    @Override
    public void send()
        {
        try
            {
            if ( format == null )
                {
                string = DateFormat.getDateInstance().format(new Date());
                }
            else
                {
                SimpleDateFormat sdf=new SimpleDateFormat( format );
                string = sdf.format(new Date());
                }
            }
        catch (Exception ex)
            {
            string = "[ERROR]";
            }
        super.send();
        }

    @Override
    public void sendSecondary( int second )
        {
        // ?? Other format ??
        }

    }

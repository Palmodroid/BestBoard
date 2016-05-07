package org.lattilad.bestboard.buttons;

import android.content.Intent;
import android.net.Uri;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.parser.Commands;
import org.lattilad.bestboard.prefs.PrefsActivity;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.utils.StringUtils;
import org.lattilad.bestboard.webview.WebViewActivity;

/**
 * Editor functions performed by the keyboard
 * These functions have no parameters, functions can communicate to the editor
 */
public class PacketFunction extends Packet
    {
    /**
     * Code of the function - keycode of the function's token
     */
    private long functionCode;


    /**
     * Constructor of button inner function
     * @param softBoardData general keyboard data class
     * @param functionCode token code of the function
     */
    public PacketFunction( SoftBoardData softBoardData, long functionCode )
        {
        super( softBoardData );
        this.functionCode = functionCode;
        }


    /**
     * String representation of the data (title)
     * @return String representation of this packet
     */
    @Override
    public String getString()
        {
        if ( functionCode == Commands.TOKEN_DELETE )
            return "DEL";

        if ( functionCode == Commands.TOKEN_BACKSPACE )
            return "BS";

        if ( functionCode == Commands.TOKEN_DRAFT )
            return "DRAFT";

        if ( functionCode == Commands.TOKEN_SETTINGS )
            return "SET";

        if ( functionCode == Commands.TOKEN_CURSOR )
            return "CURS";

        if ( functionCode == Commands.TOKEN_SELECTALL )
            return "ALL";

        if ( functionCode == Commands.TOKEN_CHANGECASE)
            return "CAP";

        if ( functionCode == Commands.TOKEN_AUTOFUNC )
            return "AF";

        if ( functionCode == Commands.TOKEN_HELP )
            return "HELP";

        // any other code
        return "ERR";
        }


    /**
     * Send data to the editor field
     */
    @Override
    public void send()
        {
        if ( functionCode == Commands.TOKEN_DELETE )
            {
            softBoardData.softBoardListener.deleteCharAfterCursor(1);
            }

        else if ( functionCode == Commands.TOKEN_BACKSPACE )
            {
            softBoardData.softBoardListener.deleteCharBeforeCursor(1);
            }

        else if ( functionCode == Commands.TOKEN_DRAFT )
            {
            softBoardData.softBoardListener.startSoftBoardParser();
            }

        else if (functionCode == Commands.TOKEN_SETTINGS )
            {
            // The whole part is copied from Best's Board ver 1.

            // Original idea: http://stackoverflow.com/a/3607934
            // but it doesn't work without Single_top - BACK went back to an other instance

            Intent intent = new Intent( softBoardData.softBoardListener.getApplicationContext(), PrefsActivity.class);
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            // http://stackoverflow.com/a/7910905 - cannot use single_top, etc. if startactivityforresult is used
            //intent.addFlags( Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
            //intent.addFlags( Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY );
            //intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
            //intent.addFlags( Intent.FLAG_FROM_BACKGROUND );

            softBoardData.softBoardListener.getApplicationContext().startActivity(intent);
            }

        else if ( functionCode == Commands.TOKEN_CURSOR )
            {
            softBoardData.softBoardListener.toggleCursor();
            }

        else if ( functionCode == Commands.TOKEN_SELECTALL )
            {
            softBoardData.softBoardListener.selectAll();
            }

        else if ( functionCode == Commands.TOKEN_CHANGECASE)
            {
            String string = softBoardData.softBoardListener.getWordOrSelected();
            switch (StringUtils.checkStringCase(string, 2048))
                {
                case StringUtils.LOWER_CASE:
                    softBoardData.softBoardListener.changeLastWordOrSelected
                            (StringUtils.toUpperFirst(string, softBoardData.locale), true);
                    break;
                case StringUtils.FIRST_UPPER_CASE:
                case StringUtils.MIXED_CASE:
                    softBoardData.softBoardListener.changeLastWordOrSelected
                            ( string.toUpperCase( softBoardData.locale ), true);
                    break;
                case StringUtils.UPPER_CASE:
                    softBoardData.softBoardListener.changeLastWordOrSelected
                            ( string.toLowerCase(softBoardData.locale), true);
                    break;
                }
            }

        else if ( functionCode == Commands.TOKEN_AUTOFUNC )
            {
            softBoardData.autoFuncEnabled = !softBoardData.autoFuncEnabled;
            }

        else if (functionCode == Commands.TOKEN_HELP )
            {
            Intent intent = new Intent( softBoardData.softBoardListener.getApplicationContext(), WebViewActivity.class);
            intent.putExtra( WebViewActivity.WORK, "help.html");
            //intent.setData(Uri.parse("http://lattilad.org/"));
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            //intent.addFlags( Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
            //intent.addFlags( Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY );
            //  intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
            //intent.addFlags( Intent.FLAG_FROM_BACKGROUND );
            softBoardData.softBoardListener.getApplicationContext().startActivity(intent);
            }

        }


    @Override
    public void release()
        {
        if ( functionCode == Commands.TOKEN_BACKSPACE )
            {
            ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                    .setAutoCapsState(CapsState.AUTOCAPS_OFF);
            }
        }

    }

package org.lattilad.bestboard;

/**
 * Markers
 */
public class SoftBoardMarker
    {
    /**
     * Available markers:
     * - Enteraction
     * - Autofunc
     */
    private SoftBoardData softBoardData;

    public SoftBoardMarker( SoftBoardData softBoardData )
        {
        this.softBoardData = softBoardData;
        }

    // Markers should start from 1 ( TEXT is 0 )

    public final static int ENTER_ACTION_MARKER = 1;

    public final static int AUTO_FUNC_MARKER = 2;

    public String[] enterActionTexts = {
            "???",
            "---",
            "GO",
            "SRCH",
            "SEND",
            "NEXT",
            "DONE",
            "PREV",
            "CR"};

    public String[] autoFuncTexts = {
            "OFF",
            "AUTO" };

    public String getMarkerText( int marker )
        {
        if ( marker == ENTER_ACTION_MARKER )
            return enterActionTexts[ softBoardData.enterAction ];

        if ( marker == AUTO_FUNC_MARKER )
            return autoFuncTexts[ softBoardData.autoFuncEnabled ? 1 : 0 ];

        return "";
        }

    public void setMarkerText( int marker, int num, String text )
        {
        String[] array;

        if ( marker == ENTER_ACTION_MARKER )
            array = enterActionTexts;
        else if ( marker == AUTO_FUNC_MARKER )
            array = autoFuncTexts;
        else
            return;

        if ( num >=0 && num <= array.length )
            array[num] = text;
        }

    }

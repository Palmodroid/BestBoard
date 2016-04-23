package org.lattilad.bestboard;

import org.lattilad.bestboard.parser.SoftBoardParser;

/**
 * Show-titles
 */
public class SoftBoardShow
    {
    /**
     * Available shows:
     * - Enteraction
     * - Autofunc
     */
    private SoftBoardData softBoardData;

    public SoftBoardShow(SoftBoardData softBoardData)
        {
        this.softBoardData = softBoardData;
        }

    // Markers should start from 1 ( TEXT is 0 )

    public final static int ENTER_ACTION = 1;

    public final static int AUTO_FUNC = 2;

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

    public String getShowText(int show)
        {
        if ( show == ENTER_ACTION)
            return enterActionTexts[ softBoardData.enterAction ];

        if ( show == AUTO_FUNC)
            return autoFuncTexts[ softBoardData.autoFuncEnabled ? 1 : 0 ];

        return "";
        }

    public void setShowText(int show, int num, Object text)
        {
        String[] array;

        if ( show == ENTER_ACTION)
            array = enterActionTexts;
        else if ( show == AUTO_FUNC)
            array = autoFuncTexts;
        else
            return;

        if ( num >=0 && num <= array.length )
            array[num] = SoftBoardParser.stringFromText( text );
        }

    }

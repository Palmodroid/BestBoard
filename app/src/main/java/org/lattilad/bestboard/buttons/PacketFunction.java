package org.lattilad.bestboard.buttons;

import android.content.Intent;

import org.lattilad.bestboard.SoftBoardProcessor;
import org.lattilad.bestboard.parser.Commands;
import org.lattilad.bestboard.prefs.PrefsActivity;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.MetaState;
import org.lattilad.bestboard.utils.ExternalDataException;

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
     * @throws ExternalDataException if function code is not valid
     */
    public PacketFunction( SoftBoardData softBoardData, long functionCode ) throws ExternalDataException
        {
        super( softBoardData );
        this.functionCode = functionCode;

        if ( functionCode == Commands.TOKEN_DELETE )
            return;

        if ( functionCode == Commands.TOKEN_BACKSPACE )
            return;

        if ( functionCode == Commands.TOKEN_DRAFT )
            return;

        if ( functionCode == Commands.TOKEN_SETTINGS )
            return;

        if ( functionCode == Commands.TOKEN_TOGGLECURSOR )
            return;

        if ( functionCode == Commands.TOKEN_SELECTALL )
            return;

        if ( functionCode == Commands.TOKEN_BEGIN )
            return;

        if ( functionCode == Commands.TOKEN_END )
            return;

        if ( functionCode == Commands.TOKEN_LEFT )
            return;

        if ( functionCode == Commands.TOKEN_RIGHT )
            return;

        if ( functionCode == Commands.TOKEN_LEFT1ST )
            return;

        if ( functionCode == Commands.TOKEN_RIGHT1ST )
            return;

        if ( functionCode == Commands.TOKEN_LEFT2ND )
            return;

        if ( functionCode == Commands.TOKEN_RIGHT2ND )
            return;

        if ( functionCode == Commands.TOKEN_WORDLEFT )
            return;

        if ( functionCode == Commands.TOKEN_WORDRIGHT )
            return;

        if ( functionCode == Commands.TOKEN_WORDLEFT1ST )
            return;

        if ( functionCode == Commands.TOKEN_WORDRIGHT1ST )
            return;

        if ( functionCode == Commands.TOKEN_WORDLEFT2ND )
            return;

        if ( functionCode == Commands.TOKEN_WORDRIGHT2ND )
            return;

        if ( functionCode == Commands.TOKEN_PARALEFT )
            return;

        if ( functionCode == Commands.TOKEN_PARARIGHT )
            return;

        if ( functionCode == Commands.TOKEN_PARALEFT1ST )
            return;

        if ( functionCode == Commands.TOKEN_PARARIGHT1ST )
            return;

        if ( functionCode == Commands.TOKEN_PARALEFT2ND )
            return;

        if ( functionCode == Commands.TOKEN_PARARIGHT2ND )
            return;

        // functionCode is not known
        throw new ExternalDataException();
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

        if ( functionCode == Commands.TOKEN_TOGGLECURSOR )
            return "CURS";

        if ( functionCode == Commands.TOKEN_SELECTALL )
            return "ALL";

        if ( functionCode == Commands.TOKEN_BEGIN )
            return "BEGIN";

        if ( functionCode == Commands.TOKEN_END )
            return "END";

        if ( functionCode == Commands.TOKEN_LEFT )
            return "L";

        if ( functionCode == Commands.TOKEN_RIGHT )
            return "R";

        if ( functionCode == Commands.TOKEN_LEFT1ST )
            return "L1";

        if ( functionCode == Commands.TOKEN_RIGHT1ST )
            return "R1";

        if ( functionCode == Commands.TOKEN_LEFT2ND )
            return "L2";

        if ( functionCode == Commands.TOKEN_RIGHT2ND )
            return "R2";

        if ( functionCode == Commands.TOKEN_WORDLEFT )
            return "WL";

        if ( functionCode == Commands.TOKEN_WORDRIGHT )
            return "WR";

        if ( functionCode == Commands.TOKEN_WORDLEFT1ST )
            return "WL1";

        if ( functionCode == Commands.TOKEN_WORDRIGHT1ST )
            return "WR1";

        if ( functionCode == Commands.TOKEN_WORDLEFT2ND )
            return "WL2";

        if ( functionCode == Commands.TOKEN_WORDRIGHT2ND )
            return "WR2";

        if ( functionCode == Commands.TOKEN_PARALEFT )
            return "PL";

        if ( functionCode == Commands.TOKEN_PARARIGHT )
            return "PR";

        if ( functionCode == Commands.TOKEN_PARALEFT1ST )
            return "PL1";

        if ( functionCode == Commands.TOKEN_PARARIGHT1ST )
            return "PR1";

        if ( functionCode == Commands.TOKEN_PARALEFT2ND )
            return "PL2";

        if ( functionCode == Commands.TOKEN_PARARIGHT2ND )
            return "PR2";

        // this cannot be reached
        return "";
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
            //intent.addFlags( Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT );
            //intent.addFlags( Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY );
            intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
            //intent.addFlags( Intent.FLAG_FROM_BACKGROUND );

            softBoardData.softBoardListener.getApplicationContext().startActivity(intent);
            }

        if ( functionCode == Commands.TOKEN_TOGGLECURSOR )
            {
            softBoardData.softBoardListener.toggleCursor();
            }

        if ( functionCode == Commands.TOKEN_SELECTALL )
            {
            softBoardData.softBoardListener.selectAll();
            }

        else if ( functionCode == Commands.TOKEN_BEGIN )
            {
            softBoardData.softBoardListener.jumpBegin(
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_END )
            {
            softBoardData.softBoardListener.jumpEnd(
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_LEFT )
            {
            softBoardData.softBoardListener.jumpLeft(
                    SoftBoardProcessor.SELECTION_LAST,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_RIGHT )
            {
            softBoardData.softBoardListener.jumpRight(
                    SoftBoardProcessor.SELECTION_LAST,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_LEFT1ST )
            {
            softBoardData.softBoardListener.jumpLeft(
                    SoftBoardProcessor.SELECTION_START,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_RIGHT1ST )
            {
            softBoardData.softBoardListener.jumpRight(
                    SoftBoardProcessor.SELECTION_START,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_LEFT2ND )
            {
            softBoardData.softBoardListener.jumpLeft(
                    SoftBoardProcessor.SELECTION_END,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_RIGHT2ND )
            {
            softBoardData.softBoardListener.jumpRight(
                    SoftBoardProcessor.SELECTION_END,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_WORDLEFT )
            {
            softBoardData.softBoardListener.jumpWordLeft(
                    SoftBoardProcessor.SELECTION_LAST,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_WORDRIGHT )
            {
            softBoardData.softBoardListener.jumpWordRight(
                    SoftBoardProcessor.SELECTION_LAST,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_WORDLEFT1ST )
            {
            softBoardData.softBoardListener.jumpWordLeft(
                    SoftBoardProcessor.SELECTION_START,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_WORDRIGHT1ST )
            {
            softBoardData.softBoardListener.jumpWordRight(
                    SoftBoardProcessor.SELECTION_START,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_WORDLEFT2ND )
            {
            softBoardData.softBoardListener.jumpWordLeft(
                    SoftBoardProcessor.SELECTION_END,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_WORDRIGHT2ND )
            {
            softBoardData.softBoardListener.jumpWordRight(
                    SoftBoardProcessor.SELECTION_END,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_PARALEFT )
            {
            softBoardData.softBoardListener.jumpParagraphLeft(
                    SoftBoardProcessor.SELECTION_LAST,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_PARARIGHT )
            {
            softBoardData.softBoardListener.jumpParagraphRight(
                    SoftBoardProcessor.SELECTION_LAST,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_PARALEFT1ST )
            {
            softBoardData.softBoardListener.jumpParagraphLeft(
                    SoftBoardProcessor.SELECTION_START,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_PARARIGHT1ST )
            {
            softBoardData.softBoardListener.jumpParagraphRight(
                    SoftBoardProcessor.SELECTION_START,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_PARALEFT2ND )
            {
            softBoardData.softBoardListener.jumpParagraphLeft(
                    SoftBoardProcessor.SELECTION_END,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
            }

        else if ( functionCode == Commands.TOKEN_PARARIGHT2ND )
            {
            softBoardData.softBoardListener.jumpParagraphRight(
                    SoftBoardProcessor.SELECTION_END,
                    softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() != MetaState.META_OFF);
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

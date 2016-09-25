package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * Same as PacketTextSimple, but Title (getfirst) will change with caps state
 */
public class PacketTextCaps extends PacketTextSimple
    {
    public PacketTextCaps(SoftBoardData softBoardData, String string )
        {
        super(softBoardData, string);
        }

    public PacketTextCaps(SoftBoardData softBoardData, Character character )
        {
        super(softBoardData, character);
        }

    @Override
    public boolean isTitleStringChanging()
        {
        return true;
        }

    // Same functionality as per PacketText
    @Override
    public String getTitleString()
        {
        int capsState;

        int state = softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS].getState();
        if ( state == CapsState.META_OFF )
            {
            capsState = 0;
            }
        else if ( state == CapsState.META_LOCK )
            {
            capsState = 2;
            }
        else if ( state == CapsState.AUTOCAPS_ON && !softBoardData.autoFuncEnabled )
            {
            capsState = 0;
            }
        else // state == IN_TOUCH || META_ON || AUTOCAPS_ON with enabled autoFunc
            {
            capsState = stringCaps ? 2 : 1;
            }

        switch ( capsState )
            {
            case 0:
                return getString();

            case 1:
                return StringUtils.toUpperOnlyFirst( getString(), softBoardData.locale );

            default: // case 2:
                return getString().toUpperCase( softBoardData.locale );
            }

        }
    }

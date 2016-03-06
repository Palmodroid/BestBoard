package org.lattilad.bestboard.buttons;

/**
 * Double button with double packet
 * First packet should be undo-able (Text),
 * second can be any type: Text(String) or Hard-key or function
 */
public class ButtonAlternate extends ButtonMainTouchTitles implements Cloneable
    {
    private static int counter = 0;
    private Packet[] packets = new Packet[2];

    @Override
    public ButtonAlternate clone()
        {
        return (ButtonAlternate)super.clone();
        }

    public ButtonAlternate( Packet packetFirst, Packet packetSecond )
        {
        packets[0] = packetFirst;
        packets[1] = packetSecond;
        }

    public String getString()
        {
        return packets[0].getString();
        }


    @Override
    public String getChangingString()
        {
        return packets[counter].getString();
        }

    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        packets[counter].send();
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        packets[counter].release();
        counter++;
        counter&=1;
        }

    @Override
    public void mainTouchOnCircle( boolean isHardPress )
        {
        if ( layout.softBoardData.softBoardListener.undoLastString() )
            {
            counter++;
            counter&=1;
            packets[counter].send();
            }
        }

    @Override
    public boolean mainTouchOnStay()
        {
        return false;
        }

    }

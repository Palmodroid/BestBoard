package dancingmoon.bestboard.buttons;

/**
 * Double button with double packet
 * First packet should be undo-able (Text),
 * second can be any type: Text(String) or Hard-key or function
 */
public class ButtonDouble extends ButtonMainTouch implements Cloneable
    {
    private Packet packetFirst;
    private Packet packetSecond;

    @Override
    public ButtonDouble clone()
        {
        return (ButtonDouble)super.clone();
        }

    public ButtonDouble( Packet packetFirst, Packet packetSecond )
        {
        this.packetFirst = packetFirst;
        this.packetSecond = packetSecond;
        }

    public String getString()
        {
        return packetFirst.getString();
        }

    private static int counter = 0;


    /**
     * Packet is sent independently from touch down/move
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        board.softBoardData.softBoardListener.beginCheckSuspension();

        packetFirst.send();
        counter = 1;
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( counter == 1 )
            packetFirst.release();
        else // counter == 2;
            packetSecond.release();

        board.softBoardData.softBoardListener.finishCheckSuspension();
        }

    @Override
    public void mainTouchOnCircle( boolean isHardPress )
        {
        if ( board.softBoardData.softBoardListener.undoLastString() )
            {
            if ( counter == 1 )
                {
                packetSecond.send();
                counter = 2;
                }
            else // counter == 2
                {
                packetFirst.send();
                counter = 1;
                }

            }
        }

    @Override
    public boolean mainTouchOnStay()
        {
        return false;
        }

    }

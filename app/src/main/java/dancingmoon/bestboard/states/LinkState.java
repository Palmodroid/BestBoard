package dancingmoon.bestboard.states;

import android.content.res.Configuration;

import dancingmoon.bestboard.Board;
import dancingmoon.bestboard.Debug;
import dancingmoon.bestboard.SoftBoardData;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.utils.ExternalDataException;

public class LinkState
    {
    // useState is defined in the constructor of SoftBoardData
    // There are 3 entry points:
    // - SoftBoardService.softBoardParserFinished()
    // - SoftBoardService.onCreateInputView()
    // - SoftBoardService.setBoardUse()


    public static final int MAX_LINKS = 1000; // !! Just for trying!! Original: 16;
    public static final int LINK_PORTRAIT = 0;
    public static final int LINK_LANDSCAPE = 1;

    // structure of boards for indexes and orientation
    private Board linkBoard[][] = new Board[MAX_LINKS][2];

    // orientation: can be LINK_PORTRAIT or LINK_LANDSCAPE
    private int orientation = LINK_PORTRAIT;

    // Connection to service
    private SoftBoardData.SoftBoardListener softBoardListener;


    // Constructor - UseState should be able to reach Service (SoftBoardDataListener)
    public LinkState( SoftBoardData.SoftBoardListener softBoardListener )
        {
        this.softBoardListener = softBoardListener;
        }


    // Use the same not/wide board for activeIndex
    // SoftBoardParser calls it
    public boolean setLinkBoardTable( int index, Board board ) throws ExternalDataException
        {
        return setLinkBoardTable( index, board, board );
        }

    // Use portrait/landscape board pair for activeIndex
    // SoftBoardParser calls it
    public boolean setLinkBoardTable( int index, Board portrait, Board landscape ) throws ExternalDataException
        {
        boolean err = false;

        if ( index < 0 || index >= MAX_LINKS )
            {
            throw new ExternalDataException("UseBoard activeIndex is out of range!");
            }

        if ( linkBoard[index][0] == null )
            err = true;

        linkBoard[index][LINK_PORTRAIT] = portrait;
        linkBoard[index][LINK_LANDSCAPE] = landscape;

        return err;
        }

    // true if first (activeIndex 0) board is missing
    // UseBoard activeIndex 0 is obligatory
    public boolean isFirstBoardMissing()
        {
        return linkBoard[0][0] == null;
        }

    // sets orientation
    // SoftBoardService.softBoardParserFinished() (!!this call could be in constructor!!)
    // and .SoftBoardService.onCreateInputView()
    public void setOrientation()
        {
        Configuration config = softBoardListener.getApplicationContext().getResources().getConfiguration();
        orientation = (config.orientation == Configuration.ORIENTATION_PORTRAIT ? LINK_PORTRAIT : LINK_LANDSCAPE );
        // Theoretically it could be undefinied, but then it will be treated as landscape

        Scribe.debug( Debug.LINKSTATE, "Orientation is " + ( orientation == LINK_PORTRAIT ? "PORTRAIT" : "LANDSCAPE" ) );
        }

    // BoardView.onMeasure() and Board.setScreenData checks orientation
    // This can be used at least for error checking
    public boolean isLandscape()
        {
        return orientation == LINK_LANDSCAPE;
        }

    // returns selected board
    // !! activeIndex cannot be invalid !!
    // All three SoftBoardService methods calls this
    public Board getActiveBoard()
        {
        return linkBoard[activeIndex][orientation];
        }


    /** Board is active because of continuous touch of its button */
    public final static int TOUCHED = -1;
    /** Board is inactive */
    public final static int HIDDEN = 0;
    /** Board is active for one main stream button, then it will return to the previous board */
    public final static int ACTIVE = 1;
    /** Board is active */
    public final static int LOCKED = 2;

    /**
     * Index of the active board.
     * Only one board can be active.
     */
    private int activeIndex = 0;

    /**
     * Index of the previous board, where active board could return.
     * Board cannot be nested. Return works only ONE LEVEL deep.
     * After that board will return to board 0.
     */
    private int previousIndex = 0;

    /**
     * state: HIDDEN / ACTIVE / LOCKED
     * Board 0 is always LOCKED.
     */
    private int state = LOCKED;

    /**
     * Touch counter of the use key of the active board.
     * Key is released, when counter is 0
     */
    private int touchCounter = 0;

    /**
     * Type flag of the active board.
     * True, if main stream button was used during the TOUCH.
     */
    private boolean typeFlag = false;


    /**
     * Check whether this activeIndex signs the current board.
     * Invalid indices also signs the current board!
     * @param index to check
     * @return true, if activeIndex signs the current board
     */
    public boolean isActive(int index)
        {
        return ( index < 0 || index == this.activeIndex || index >= MAX_LINKS );
        }

    /**
     * State of the board.
     * @param index Index of board to check
     * @return TOUCHED / ACTIVE / LOCK / HIDDEN
     */
    public int getState( int index )
        {
        if ( isActive(index) )
            {
            if ( touchCounter > 0 )
                return TOUCHED;

            if ( state == LOCKED )
                return LOCKED;

            return ACTIVE;
            }
        else
            return HIDDEN;
        }


    private void activatePreviousBoard()
        {
        if ( activeIndex != previousIndex )
            {
            Scribe.debug( Debug.LINKSTATE, "Returning to board: " + previousIndex );
            activeIndex = previousIndex;
            previousIndex = 0;
            state = LOCKED;
            typeFlag = false;
            softBoardListener.getBoardView().setBoard( getActiveBoard() );
            }
        else
            {
            Scribe.error( "No previous board is available!" );
            }
        }


    /**
     * Use-key is touched
     * OTHER BOARD'S USE-KEY:
     * Immediately changes to the other board (if new board exists)
     * ACTIVE BOARD'S USE-KEY:
     * Touch is stored, but nothing happens until release.
     */
    public void touch( int index )
        {
        // BACK key
        if ( isActive( index ) )
            {
            touchCounter++;
            }

        // NEW board - if exist
        else if ( linkBoard[index][0] != null )
            {
            Scribe.debug( Debug.LINKSTATE, "New board was selected: " + index );
            previousIndex = this.activeIndex;
            this.activeIndex = index;
            touchCounter = 1; // previous touches are cleared
            state = HIDDEN; // it is only active because of TOUCHED
            typeFlag = false;

            // requestLayout is called by setBoard
            softBoardListener.getBoardView().setBoard( getActiveBoard() );
            }

        // NEW board is missing - nothing happens
        else
            {
            Scribe.error( "Board missing, it cannot be selected: " + index );
            }
        }


    /**
     * Touch counter could be checked, when there is no touch
     */
    public void checkNoTouch()
        {
        if ( touchCounter != 0)
            {
            Scribe.error( "UseState TOUCH remained! Counter: " + touchCounter );
            touchCounter = 0; // No change in use-state
            }
        }

    /**
     * Type could be happen only on the current board!!
     * Button is touched on the main stream.
     * If use-key is in touch, it remains in TOUCH state
     * After its release, state will not change
     */
    public boolean type()
        {
        if ( touchCounter > 0 )
            {
            typeFlag = true;
            }
        else if ( state == ACTIVE )
            {
            activatePreviousBoard();

            return true;
            }

        return false;
        }


    /**
     * This could be only the current board !!
     *
     * Meta-key is released
     * If non-meta was used during this touch, than nothing happens
     * else state cycles up
     */
    public void release( int index, boolean lockKey )
        {
        // touchCounter can be 0 if use-key was continuously pressed,
        // while selection/return happens
        if (isActive(index) && touchCounter > 0)
            {
            touchCounter--;
            Scribe.debug( Debug.LINKSTATE, "UseState RELEASE, counter: " + touchCounter );

            if (touchCounter == 0)
                {
                Scribe.debug( Debug.LINKSTATE, "UseState: all button RELEASED." );
                if ( !typeFlag )
                    {
                    if (state == HIDDEN)
                        {
                        if (lockKey)
                            {
                            state = LOCKED;
                            Scribe.debug( Debug.LINKSTATE, "UseState cycled to LOCKED by LOCK key." );
                            }
                        else
                            {
                            state = ACTIVE;
                            Scribe.debug( Debug.LINKSTATE, "UseState cycled to ACTIVE." );
                            }
                        }
                    else if (state == ACTIVE)
                        {
                        state = LOCKED;
                        Scribe.debug( Debug.LINKSTATE, "UseState cycled to LOCKED." );
                        }
                    else
                        {
                        activatePreviousBoard();
                        }
                    }
                else
                    {
                    activatePreviousBoard();
                    }
                }
            }
        }

    /**
     * Meta-key is cancelled (because SPen is activated)
     * Similar to release, but state will be always META_LOCK
     */
    public void cancel( int index )
        {
        // This should be always true
        if (isActive(index))
            {
            typeFlag = false;
            touchCounter = 0;
            state = LOCKED;
            Scribe.debug( Debug.LINKSTATE, "UseState cancelled to META_LOCK." );
            }
        }
    }

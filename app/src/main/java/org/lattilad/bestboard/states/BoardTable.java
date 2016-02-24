package org.lattilad.bestboard.states;

import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lattilad.bestboard.Layout;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.ExternalDataException;

/**
 * Boards consist of two layouts (however the two can be tha same):
 * one for portrait and one for landscape mode.
 * Boards identified by their keyword token id.
 * BoardLinks stores all these Boards, and switches between them, if needed by link-buttons.
 */
public class BoardTable
    {
    // boardTable is defined in the constructor of SoftBoardData
    // There are 3 entry points:
    // - SoftBoardService.softBoardParserFinished()
    // - SoftBoardService.onCreateInputView()
    // - LayoutView.setLayout()??

    /** Orientation: can be Board.ORIENTATION_PORTRAIT or Board.ORIENTATION_LANDSCAPE */
    private int orientation = Board.ORIENTATION_PORTRAIT;

    /** board-id/board pairs are stored here */
    private Map<Long, Board> boards = new HashMap<>();

    /**
     * BoardStack handles the already opened boards.
     * It can be initialized, when MAIN board is definied
     */
    private BoardStack boardStack = null;


    /** Layout is active because of continuous touch of its button */
    public final static int TOUCHED = -1;
    /** Layout is inactive */
    public final static int HIDDEN = 0;
    /** Layout is active for one main stream button, then it will return to the previous layout */
    public final static int ACTIVE = 1;
    /** Layout is active */
    public final static int LOCKED = 2;

    /**
     * state: HIDDEN / ACTIVE / LOCKED
     * Non-active boards are always HIDDEN
     * "Main" boards are always LOCKED.
     */
    private int state = LOCKED;

    /**
     * Touch counter of the use key of the active layout.
     * Key is released, when counter is 0
     */
    private int touchCounter = 0;

    /**
     * Type flag of the active layout.
     * True, if main stream button was used during the TOUCH.
     */
    private boolean typeFlag = false;

    /** Connection to service */
    private SoftBoardData.SoftBoardListener softBoardListener;


    /** Constructor - BoardLinks should be able to reach Service (SoftBoardDataListener) */
    public BoardTable(SoftBoardData.SoftBoardListener softBoardListener)
        {
        this.softBoardListener = softBoardListener;
        }


    /**
     * Use the same not/wide layout
     * SoftBoardParser calls it
     * returns TRUE, if Board was already defined
     */
    public boolean addBoard(Long id, Layout layout, boolean locked)
        {
        return addBoard(id, layout, layout, locked);
        }

    /**
     * Use portrait/landscape layout pair
     * SoftBoardParser calls it
     * returns TRUE, if Board was already defined
     */
    public boolean addBoard(Long id, Layout portrait, Layout landscape, boolean locked)
        {
        Board board = new Board( portrait, landscape, locked );
        return ( boards.put( id, board ) != null );
        }

    /**
     * Explicitly sets baseBoard
     * @param baseBoardId id of the base board
     */
    public void defineBaseBoard(Long baseBoardId)
        {
        Board board = boards.get( baseBoardId );
        if ( board != null )
            {
            boardStack = new BoardStack( baseBoardId, board );
            }
        // !! else: there is a serious error - baseBoard is not defined yet !!
        }

    /**
     * If base-board is missing, then there are no boards at all.
     * This is not possible!
     * @return true if there are no boards
     */
    public boolean isBaseBoardMissing()
        {
        return boardStack == null;
        }

    // sets orientation
    // SoftBoardService.softBoardParserFinished() (!!this call could be in constructor!!)
    // and .SoftBoardService.onCreateInputView()
    public void setOrientation()
        {
        Configuration config = softBoardListener.getApplicationContext()
                .getResources().getConfiguration();
        orientation = (config.orientation == Configuration.ORIENTATION_PORTRAIT ?
                Board.ORIENTATION_PORTRAIT : Board.ORIENTATION_LANDSCAPE);
        // Theoretically it could be undefined, but then it will be treated as landscape

        Scribe.debug( Debug.LINKSTATE, "Orientation is " +
                ( orientation == Board.ORIENTATION_PORTRAIT ? "PORTRAIT" : "LANDSCAPE" ) );
        }

    // BoardView.onMeasure() and Layout.calculateScreenData checks orientation
    // This can be used at least for error checking
    public boolean isLandscape()
        {
        return orientation == Board.ORIENTATION_LANDSCAPE;
        }


    /**
     * Returns the currently selected (active) layout
     * (depending on the active board and orientation)
     * All three SoftBoardService methods calls this
     * !! visibleBoardId cannot be invalid !!
     * @return the active layout
     */
    public Layout getActiveLayout()
        {
        Board board = boardStack.getActiveBoard();
        return board.getLayout(orientation);
        }


    /**
     * All calculations should be cleared in all boards, if preference changes.
     * After this clear a new requestLayout() call will refresh the screen.
     * @param erasePictures pictures will be deleted, if true
     */
    public void invalidateCalculations( boolean erasePictures )
        {
        for ( Board board : boards.values() )
            {
            for ( int o = 0; o < 2; o++ )
                {
                board.getLayout(o).invalidateCalculations(erasePictures);
                }
            }
        }


    /**
     * Check whether this id signs the current board.
     * @param id to check
     * @return true, if id signs the current board
     */
    private boolean isActive( Long id )
        {
        return (long)id==boardStack.getActiveBoardId();
        }

    /**
     * State of the board.
     * @param id board id to check
     * @return TOUCHED / ACTIVE / LOCK / HIDDEN
     */
    public int getState( Long id )
        {
        if ( isActive(id) )
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
        if ( !visibleBoardId.equals(previousBoardId) )
            {
            Scribe.debug( Debug.LINKSTATE, "Returning to board: " +
                    Tokenizer.regenerateKeyword( previousBoardId ));
            visibleBoardId = previousBoardId;
            previousBoardId = rootBoardId;
            state = LOCKED;
            typeFlag = false;
            softBoardListener.getLayoutView().setLayout(getActiveLayout());
            }
        else
            {
            Scribe.error( "No previous layout is available!" );
            }
        }


    /**
     * Link-key is touched
     * OTHER LAYOUT'S USE-KEY:
     * Immediately changes to the other layout (if new layout exists)
     * ACTIVE LAYOUT'S USE-KEY:
     * Touch is stored, but nothing happens until release.
     */
    public void touch( Long id )
        {
        // BACK key
        if ( isActive( id ) )
            {
            touchCounter++;
            }
        else
            {
            // NEW layout - if exist
            Board board = boards.get(id);
            if (board != null)
                {
                Scribe.debug(Debug.LINKSTATE, "New board was selected: " +
                        Tokenizer.regenerateKeyword(id));
                previousBoardId = visibleBoardId;
                visibleBoardId = id;
                touchCounter = 1; // previous touches are cleared
                state = HIDDEN; // it is only active because of TOUCHED
                typeFlag = false;

                // requestLayout is called by setLayout
                softBoardListener.getLayoutView().setLayout(board.layout[orientation]);
                }
            // NEW layout is missing - nothing happens
            else
                {
                Scribe.error("Layout missing, it cannot be selected: " +
                        Tokenizer.regenerateKeyword(id));
                }
            }
        }


    /**
     * Touch counter could be checked, when there is no touch
     */
    public void checkNoTouch()
        {
        if ( touchCounter != 0)
            {
            Scribe.error( "UseState TOUCH remained! Touch-counter: " + touchCounter );
            touchCounter = 0; // No change in use-state
            }
        else
            {
            Scribe.debug( Debug.TOUCH_VERBOSE, "UseState TOUCH is empty." );
            }
        }

    /**
     * Type could be happen only on the current layout!!
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
     * This could be only the current layout !!
     *
     * Meta-key is released
     * If non-meta was used during this touch, than nothing happens
     * else state cycles up
     */
    public void release( Long id, boolean lockKey )
        {
        // touchCounter can be 0 if use-key was continuously pressed,
        // while selection/return happens
        if (isActive( id ) && touchCounter > 0)
            {
            touchCounter--;
            Scribe.debug( Debug.LINKSTATE, "BoardLinks RELEASE, touch-counter: " + touchCounter );

            if (touchCounter == 0)
                {
                Scribe.debug( Debug.LINKSTATE, "BoardLinks: all button RELEASED." );
                if ( !typeFlag )
                    {
                    if (state == HIDDEN)
                        {
                        if (lockKey)
                            {
                            state = LOCKED;
                            Scribe.debug( Debug.LINKSTATE, "BoardLinks cycled to LOCKED by LOCK key." );
                            }
                        else
                            {
                            state = ACTIVE;
                            Scribe.debug( Debug.LINKSTATE, "BoardLinks cycled to ACTIVE." );
                            }
                        }
                    else if (state == ACTIVE)
                        {
                        state = LOCKED;
                        Scribe.debug( Debug.LINKSTATE, "BoardLinks cycled to LOCKED." );
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
    public void cancel( Long id )
        {
        // This should be always true
        if (isActive( id ))
            {
            typeFlag = false;
            touchCounter = 0;
            state = LOCKED;
            Scribe.debug( Debug.LINKSTATE, "BoardLinks cancelled to META_LOCK." );
            }
        }
    }

package org.lattilad.bestboard.states;

import android.content.res.Configuration;

import org.lattilad.bestboard.Layout;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.Commands;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Boards consist of two layouts (however the two can be tha same):
 * one for portrait and one for landscape mode.
 * Boards identified by their keyword token id.
 * BoardTable stores all these Boards, and switches between them by switch buttons.
 * <p/>
 * PARSING PHASE:
 * BoardTable should be filled up with boards: addBoard() methods.
 * First (root) board should be selected: defineRootBoard().
 * Root board could be changed during the process,
 * but it should be checked at the end: isRootBoardDefined().
 */
public class BoardTable
    {
    // boardTable is defined in the constructor of SoftBoardData
    // There are 3 entry points:
    // - SoftBoardService.softBoardParserFinished()
    // - SoftBoardService.onCreateInputView()
    // - LayoutView.setLayout()??

    /**
     * Connection to service
     */
    private SoftBoardData.SoftBoardListener softBoardListener;


    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    /**
     * Orientation: can be Board.ORIENTATION_PORTRAIT or Board.ORIENTATION_LANDSCAPE
     */
    private int orientation = ORIENTATION_PORTRAIT;


    /******** DATA OF ALL BOARDS ********/

    /**
     * Boards consists of two layouts:
     * layout[ORIENTATION_PORTRAIT] and layout[ORIENTATION_LANDSCAPE]
     */
    private class BoardEntry
        {
        private Layout[] layout = new Layout[2];
        private boolean locked = false;

        BoardEntry(Layout portrait, Layout landscape, boolean locked)
            {
            this.layout[ORIENTATION_PORTRAIT] = portrait;
            this.layout[ORIENTATION_LANDSCAPE] = landscape;
            this.locked = locked;
            }

        boolean isLocked()
            {
            return locked;
            }

        Layout getLayout(int orientation)
            {
            return layout[orientation];
            }
        }

    /**
     * All boards are listed here as board-id/board-entry pairs
     */
    private Map<Long, BoardEntry> boards = new HashMap<>();

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
        BoardEntry boardEntry = new BoardEntry(portrait, landscape, locked);
        return (boards.put(id, boardEntry) != null);
        }


    /******** DATA OF PREVIOUS BOARDS ********/

    /**
     * BoardStackEntry describes previous boards (but not the active one!)
     * BoardId, BoardEntry, locked (if state was locked at exit)
     */
    private class BoardStackEntry
        {
        long boardId;
        BoardEntry boardEntry;
        boolean locked;

        BoardStackEntry(long boardId, BoardEntry boardEntry, boolean locked)
            {
            this.boardId = boardId;
            this.boardEntry = boardEntry;
            this.locked = locked;
            }
        }

    /**
     * Previous boards without the active one
     */
    private ArrayList<BoardStackEntry> boardStackEntries = new ArrayList<>();


    private void debugBoardStack()
        {
        for ( BoardStackEntry entry : boardStackEntries )
            {
            Scribe.debug( Debug.BOARDTABLE, "  - " +
                    Tokenizer.regenerateKeyword(entry.boardId) +
                    (entry.locked ? " - lock" : "") );
            }
        Scribe.debug( Debug.BOARDTABLE, "  * " +
                Tokenizer.regenerateKeyword(activeBoardId) +
                (state == LOCKED ? " - lock" : "") );
        }

    /**
     * If active board can be found in the stack,
     * it and all boards after it will be removed.
     * This should be called every time, when a new board is selected.
     */
    private void checkBoardStack( )
        {
        Iterator<BoardStackEntry> boardIterator = boardStackEntries.iterator();

        while ( boardIterator.hasNext() )
            {
            if ( activeBoardId == boardIterator.next().boardId )
                {
                while (true)
                    {
                    boardIterator.remove();
                    if ( !boardIterator.hasNext() )
                        return;
                    boardIterator.next();
                    }
                }
            }
        }


    /**
     * Pushes currently active board onto the stack
     */
    private void pushBoard( )
        {
        Scribe.locus(Debug.BOARDTABLE);
        boardStackEntries.add(new BoardStackEntry(activeBoardId, activeBoard, state == LOCKED));
        }

    /**
     * Pops the previous board from the stack, and load it as the currently active board
     * (Prev. active board is released)
     * @param currentlyLocked
     * If true, then the last board is selected (and became locked).
     * If false, then the last LOCKED board is selected.
     * @return true on success, false if stack is empty
     */
    private boolean popBoard( boolean currentlyLocked )
        {
        Scribe.locus(Debug.BOARDTABLE);

        if ( boardStackEntries.isEmpty() )
            return false;

        // if currently locked, then last board is needed
        // if not, not-locked boards should be removed from the end of the stack
        // (first board is ALWAYS locked!)
        if ( !currentlyLocked )
            {
            while ( !boardStackEntries.get( boardStackEntries.size()-1 ).locked )
                {
                boardStackEntries.remove( boardStackEntries.size()-1 );
                }
            }

        // Now the last entry should became active, and it should be removed
        BoardStackEntry lastEntry = boardStackEntries.get( boardStackEntries.size()-1 );

        activeBoard = lastEntry.boardEntry;
        activeBoardId = lastEntry.boardId;
        state = LOCKED;
        typeFlag = false;
        // touchCounter is 0 in most cases. In case of SWITCH BACK, it should be 0
        touchCounter = 0;

        boardStackEntries.remove(boardStackEntries.size() - 1);

        return true;
        }


    /******** DATA OF THE ACTIVE BOARD ********/

    /**
     * touch: TOUCHED ( touchCounter > 0 )
     * |                    |
     * type: typeFlag       release
     * |                    |
     * release: BACK        - TOUCHED/locked?       LOCKED
     *                      - TOUCHED/non-locked?   ACTIVE
     *                      - ACTIVE                LOCKED
     *                      - LOCKED                BACK
     *
     * Active board can be:
     * - TOUCHED if touchCounter > 0
     * - ACTIVE
     * - LOCKED
     * Non-active boards are always:
     * - HIDDEN
     */

    /** Layout is active because of continuous touch of its button */
    public final static int TOUCHED = -1;
    /** Layout is inactive - all previous boards */
    public final static int HIDDEN = 0;
    /** Layout is active for one main stream button, then it will return to the previous layout */
    public final static int ACTIVE = 1;
    /** Layout is active */
    public final static int LOCKED = 2;


    /** Id of the currently visible (active) board */
    private long activeBoardId;

    /** Currently visible (active) board */
    private BoardEntry activeBoard;

    /** State of the currently visible (active) board */
    private int state = LOCKED;

    /**
     * Touch counter of the use key of currently visible (active) board
     * Key is released, when counter is 0
     */
    private int touchCounter = 0;

    /**
     * Type flag of the currently visible (active) board
     * True, if main stream button was used during the TOUCH.
     */
    private boolean typeFlag = false;


    /******** MAIN PART OF THE CODE ********/

    /** Constructor - BoardLinks should be able to reach Service (SoftBoardDataListener) */
    public BoardTable(SoftBoardData.SoftBoardListener softBoardListener)
        {
        this.softBoardListener = softBoardListener;
        }


    /**
     * Explicitly sets active board (or the root board, if stack is empty)
     * IT SHOULD BE USED ONLY DURING THA PARSING PHASE!!
     * @param boardId id of the root board
     */
    public void defineRootBoard(long boardId)
        {
        // Emptiness of BoardStack is not checked - it should be empty at the parsing phase
        BoardEntry boardEntry = boards.get( boardId );
        if ( boardEntry != null )
            {
            activeBoardId = boardId;
            activeBoard = boardEntry;
            // state = LOCKED; not needed during parsing phase
            }
        // !! else: there is a serious error - baseBoard is not defined yet !!
        }


    /**
     * If active-board is missing, then there are no boards at all.
     * This is not possible!
     * @return true if boards are ready
     */
    public boolean isRootBoardDefined()
        {
        return activeBoard != null;
        }


    // sets orientation
    // SoftBoardService.softBoardParserFinished() (!!this call could be in constructor!!)
    // and .SoftBoardService.onCreateInputView()
    public void setOrientation()
        {
        Configuration config = softBoardListener.getApplicationContext()
                .getResources().getConfiguration();
        orientation = (config.orientation == Configuration.ORIENTATION_PORTRAIT ?
                ORIENTATION_PORTRAIT : ORIENTATION_LANDSCAPE);
        // Theoretically it could be undefined, but then it will be treated as landscape

        Scribe.debug( Debug.BOARDTABLE, "Orientation is " +
                ( orientation == ORIENTATION_PORTRAIT ? "PORTRAIT" : "LANDSCAPE" ) );
        }


    // BoardView.onMeasure() and Layout.calculateScreenData checks orientation
    // This can be used at least for error checking
    public boolean isLandscape()
        {
        return orientation == ORIENTATION_LANDSCAPE;
        }


    /**
     * Returns the currently selected (active) layout
     * (depending on the active board and orientation)
     * All three SoftBoardService methods calls this
     * activeBoardId cannot be invalid, but it should be checked during parsing process
     * @return the active layout
     */
    public Layout getActiveLayout()
        {
        return activeBoard.getLayout(orientation);
        }


    /**
     * All calculations should be cleared in all boards, if preference changes.
     * After this clearing a new requestLayout() call will refresh the screen.
     * ?? List of all Layouts - does it exist ??
     * @param erasePictures pictures will be deleted, if true
     */
    public void invalidateCalculations( boolean erasePictures )
        {
        for ( BoardEntry boardEntry : boards.values() )
            {
            boardEntry.getLayout(ORIENTATION_PORTRAIT).invalidateCalculations( erasePictures );
            boardEntry.getLayout(ORIENTATION_LANDSCAPE).invalidateCalculations( erasePictures );
            }
        }


    /**
     * Check whether this id signs the currently active board.
     * @param id to check
     * @return true, if id signs the current board
     */
    private boolean isActive( long id )
        {
        return id==activeBoardId;
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


    private void selectPreviousBoard()
        {
        if ( popBoard(state == LOCKED) )
            {
            Scribe.debug( Debug.BOARDTABLE, "Returning to board: " +
                    Tokenizer.regenerateKeyword( activeBoardId ));

            debugBoardStack();
            softBoardListener.getLayoutView().setLayout(getActiveLayout());
            }
        else
            {
            Scribe.error( "No previous board is available!" );
            }
        }


    public void selectLockedBoard()
        {
        if ( state != LOCKED )
            {
            selectPreviousBoard();
            }
        }


    /**
     * Switch-key is touched
     * OTHER LAYOUT'S USE-KEY:
     * Immediately changes to the other layout (if new layout exists)
     * ACTIVE LAYOUT'S USE-KEY:
     * Touch is stored, but nothing happens until release.
     */
    public void touch( long id )
        {
        // BACK key
        if ( isActive( id ) )
            {
            touchCounter++;
            return;
            }

        if ( id == Commands.TOKEN_BACK )
            {
            selectPreviousBoard();
            return;
            }

        // NEW layout - if exist
        BoardEntry boardEntry = boards.get(id);
        if (boardEntry != null)
            {
            Scribe.debug(Debug.BOARDTABLE, "New board was selected: " +
                    Tokenizer.regenerateKeyword(id));

            pushBoard();

            activeBoardId = id;
            activeBoard = boardEntry;

            touchCounter = 1; // previous touches are cleared
            state = TOUCHED;  // it is only active because of TOUCHED
            typeFlag = false;

            checkBoardStack();

            debugBoardStack();
            // requestLayout is called by setLayout
            softBoardListener.getLayoutView().setLayout( boardEntry.getLayout(orientation));
            }
        // NEW layout is missing - nothing happens
        else
            {
            Scribe.error("Layout missing, it cannot be selected: " +
                    Tokenizer.regenerateKeyword(id));
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
            selectPreviousBoard();

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
        // touchCounter can be 0 if switch-key was continuously pressed,
        // while selection/return happens
        if (isActive( id ) && touchCounter > 0)
            {
            touchCounter--;
            Scribe.debug( Debug.BOARDTABLE, "BoardLinks RELEASE, touch-counter: " + touchCounter );

            if (touchCounter == 0)
                {
                Scribe.debug( Debug.BOARDTABLE, "BoardLinks: all button RELEASED." );
                if ( !typeFlag )
                    {
                    if (state == TOUCHED)
                        {
                        if (lockKey || activeBoard.isLocked())
                            {
                            state = LOCKED;
                            Scribe.debug( Debug.BOARDTABLE, "BoardLinks cycled to LOCKED by LOCK key." );
                            }
                        else
                            {
                            state = ACTIVE;
                            Scribe.debug( Debug.BOARDTABLE, "BoardLinks cycled to ACTIVE." );
                            }
                        }
                    else if (state == ACTIVE)
                        {
                        state = LOCKED;
                        Scribe.debug( Debug.BOARDTABLE, "BoardLinks cycled to LOCKED." );
                        }
                    else // if (state == LOCKED)
                        {
                        selectPreviousBoard();
                        }
                    }
                else
                    {
                    selectPreviousBoard();
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
            Scribe.debug( Debug.BOARDTABLE, "BoardLinks cancelled to META_LOCK." );
            }
        }
    }

package dancingmoon.bestboard.states;

import android.content.res.Configuration;

import java.util.HashMap;
import java.util.Map;

import dancingmoon.bestboard.Layout;
import dancingmoon.bestboard.SoftBoardData;
import dancingmoon.bestboard.debug.Debug;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.utils.ExternalDataException;

public class BoardLinks
    {
    // boardLinks is defined in the constructor of SoftBoardData
    // There are 3 entry points:
    // - SoftBoardService.softBoardParserFinished()
    // - SoftBoardService.onCreateInputView()
    // - SoftBoardService.setBoardUse()

    private class Board
        {
        Layout[] layouts = new Layout[2];

        Board( Layout portrait, Layout landscape )
            {
            this.layouts[LINK_PORTRAIT] = portrait;
            this.layouts[LINK_LANDSCAPE] = landscape;
            }
        }

    private Map<Long, Board> boards = new HashMap<>();

    public static final int LINK_PORTRAIT = 0;
    public static final int LINK_LANDSCAPE = 1;

    // orientation: can be LINK_PORTRAIT or LINK_LANDSCAPE
    private int orientation = LINK_PORTRAIT;

    // Connection to service
    private SoftBoardData.SoftBoardListener softBoardListener;


    // Constructor - BoardLinks should be able to reach Service (SoftBoardDataListener)
    public BoardLinks(SoftBoardData.SoftBoardListener softBoardListener)
        {
        this.softBoardListener = softBoardListener;
        }


    // Use the same not/wide layout
    // SoftBoardParser calls it
    public boolean addBoardLink(Long id, Layout layout) throws ExternalDataException
        {
        return addBoardLink(id, layout, layout);
        }

    // Use portrait/landscape layout pair
    // SoftBoardParser calls it
    // returns TRUE, if Board was already definied
    public boolean addBoardLink(Long id, Layout portrait, Layout landscape) throws ExternalDataException
        {
        Board board = new Board( portrait, landscape );
        return ( boards.put( id, board ) != null );
        }

    // true if first (activeIndex 0) layout is missing
    // UseBoard activeIndex 0 is obligatory
    public boolean isFirstBoardMissing()
        {
        return linkLayout[0][0] == null;
        }

    // sets orientation
    // SoftBoardService.softBoardParserFinished() (!!this call could be in constructor!!)
    // and .SoftBoardService.onCreateInputView()
    public void setOrientation()
        {
        Configuration config = softBoardListener.getApplicationContext().getResources().getConfiguration();
        orientation = (config.orientation == Configuration.ORIENTATION_PORTRAIT ? LINK_PORTRAIT : LINK_LANDSCAPE );
        // Theoretically it could be undefined, but then it will be treated as landscape

        Scribe.debug( Debug.LINKSTATE, "Orientation is " + ( orientation == LINK_PORTRAIT ? "PORTRAIT" : "LANDSCAPE" ) );
        }

    // BoardView.onMeasure() and Layout.calculateScreenData checks orientation
    // This can be used at least for error checking
    public boolean isLandscape()
        {
        return orientation == LINK_LANDSCAPE;
        }

    // returns selected layout
    // !! activeIndex cannot be invalid !!
    // All three SoftBoardService methods calls this
    public Layout getActiveBoard()
        {
        return linkLayout[activeIndex][orientation];
        }


    /**
     * All calculations should be cleared in all boards, if preference changes.
     * After this clear a new requestLayout() call will refresh the screen.
     * @param erasePictures pictures will be deleted, if true
     */
    public void invalidateCalculations( boolean erasePictures )
        {
        for ( int b = 0; b < MAX_LINKS; b++ )
            {
            for ( int o = 0; o < 2; o++ )
                {
                if ( linkLayout[b][o] != null )
                    {
                    linkLayout[b][o].invalidateCalculations( erasePictures );
                    }
                }
            }
        }


    /** Layout is active because of continuous touch of its button */
    public final static int TOUCHED = -1;
    /** Layout is inactive */
    public final static int HIDDEN = 0;
    /** Layout is active for one main stream button, then it will return to the previous layout */
    public final static int ACTIVE = 1;
    /** Layout is active */
    public final static int LOCKED = 2;

    /**
     * Index of the active layout.
     * Only one layout can be active.
     */
    private int activeIndex = 0;

    /**
     * Index of the previous layout, where active layout could return.
     * Layout cannot be nested. Return works only ONE LEVEL deep.
     * After that layout will return to layout 0.
     */
    private int previousIndex = 0;

    /**
     * state: HIDDEN / ACTIVE / LOCKED
     * Layout 0 is always LOCKED.
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


    /**
     * Check whether this activeIndex signs the current layout.
     * Invalid indices also signs the current layout!
     * @param index to check
     * @return true, if activeIndex signs the current layout
     */
    public boolean isActive(int index)
        {
        return ( index < 0 || index == this.activeIndex || index >= MAX_LINKS );
        }

    /**
     * State of the layout.
     * @param index Index of layout to check
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
            Scribe.debug( Debug.LINKSTATE, "Returning to layout: " + previousIndex );
            activeIndex = previousIndex;
            previousIndex = 0;
            state = LOCKED;
            typeFlag = false;
            softBoardListener.getLayoutView().setLayout(getActiveBoard());
            }
        else
            {
            Scribe.error( "No previous layout is available!" );
            }
        }


    /**
     * Use-key is touched
     * OTHER LAYOUT'S USE-KEY:
     * Immediately changes to the other layout (if new layout exists)
     * ACTIVE LAYOUT'S USE-KEY:
     * Touch is stored, but nothing happens until release.
     */
    public void touch( int index )
        {
        // BACK key
        if ( isActive( index ) )
            {
            touchCounter++;
            }

        // NEW layout - if exist
        else if ( linkLayout[index][0] != null )
            {
            Scribe.debug( Debug.LINKSTATE, "New layout was selected: " + index );
            previousIndex = this.activeIndex;
            this.activeIndex = index;
            touchCounter = 1; // previous touches are cleared
            state = HIDDEN; // it is only active because of TOUCHED
            typeFlag = false;

            // requestLayout is called by setLayout
            softBoardListener.getLayoutView().setLayout(getActiveBoard());
            }

        // NEW layout is missing - nothing happens
        else
            {
            Scribe.error( "Layout missing, it cannot be selected: " + index );
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
    public void release( int index, boolean lockKey )
        {
        // touchCounter can be 0 if use-key was continuously pressed,
        // while selection/return happens
        if (isActive(index) && touchCounter > 0)
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
    public void cancel( int index )
        {
        // This should be always true
        if (isActive(index))
            {
            typeFlag = false;
            touchCounter = 0;
            state = LOCKED;
            Scribe.debug( Debug.LINKSTATE, "BoardLinks cancelled to META_LOCK." );
            }
        }
    }

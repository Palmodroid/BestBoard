package org.lattilad.bestboard.states;

import org.lattilad.bestboard.Layout;

/**
 * Boards consists of two layouts:
 * layout[ORIENTATION_PORTRAIT] and layout[ORIENTATION_LANDSCAPE]
 * "main" boards cannot switch back to previous boards
 */
public class Board
    {
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    private Layout[] layout = new Layout[2];
    private boolean locked = false;

    public Board( Layout portrait, Layout landscape, boolean locked )
        {
        this.layout[ORIENTATION_PORTRAIT] = portrait;
        this.layout[ORIENTATION_LANDSCAPE] = landscape;
        this.locked = locked;
        }

    public boolean isLocked()
        {
        return locked;
        }

    public Layout getLayout( int orientation )
        {
        return layout[ orientation ];
        }
    }


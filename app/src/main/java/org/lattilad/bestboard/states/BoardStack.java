package org.lattilad.bestboard.states;

import java.util.ArrayList;

/**
 * BoardStack stores all previous boards
 * Each board can be only once in the list.
 * After adding the same board twice, the list will switch back to the previous entry.
 */
public class BoardStack
    {
    private class BoardEntry
        {
        Long boardId;
        boolean locked;

        BoardEntry( Long boardId, boolean locked )
            {
            this.boardId = boardId;
            this.locked = locked;
            }
        }

    private ArrayList<BoardEntry> boards;

    public BoardStack( Long boardId )
        {
        boards = new ArrayList<>();
        boards.add( new BoardEntry( boardId, true ));
        }

    public void addBoard( Long boardId, boolean locked )
        {

        }

    public void back()
        {

        }
    }
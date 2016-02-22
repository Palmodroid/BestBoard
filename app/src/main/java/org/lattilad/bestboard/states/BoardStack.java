package org.lattilad.bestboard.states;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * BoardStack stores all previously chosen boards
 * Each board can be only once in the list.
 * After adding the same board twice, the list will switch back to the previous entry.
 */
public class BoardStack
    {
    private class BoardEntry
        {
        Board board;
        boolean locked;

        BoardEntry( Board board, boolean locked )
            {
            this.board = board;
            this.locked = locked;
            }
        }

    private ArrayList<BoardEntry> boardEntries;

    public BoardStack( Board board )
        {
        boardEntries = new ArrayList<>();
        boardEntries.add(new BoardEntry(board, true));
        }

    public void addBoard( Board board, boolean locked )
        {
        Iterator<BoardEntry> boardIterator = boardEntries.iterator();

        while ( boardIterator.hasNext() )
            {
            // board already stored, all proceeding boards are cleared
            if (board.equals(boardIterator.next().board))
                {
                while (boardIterator.hasNext())
                    {
                    boardIterator.next();
                    boardIterator.remove();
                    }
                return;
                }
            }
        boardEntries.add(new BoardEntry( board, locked ));
        }

    public Board backBoard( boolean currentlyLocked )
        {
        if ( boardEntries.size() > 1 )
            {
            // remove last (currently selected) board
            boardEntries.remove( boardEntries.size()-1 );

            if ( currentlyLocked )
                {
                // if currently locked, then previous board should lock as well
                // (or can be locked originally)
                boardEntries.get( boardEntries.size()-1 ).locked = true;
                }
            else
                {
                // if currently not locked, then all previous non-locked boards should be skipped
                // (first board is ALWAYS locked!)
                while ( !boardEntries.get( boardEntries.size()-1 ).locked )
                    {
                    boardEntries.remove( boardEntries.size()-1 );
                    }
                }
            }
        // return the remaining top element
        return boardEntries.get( boardEntries.size()-1 ).board;
        }
    }

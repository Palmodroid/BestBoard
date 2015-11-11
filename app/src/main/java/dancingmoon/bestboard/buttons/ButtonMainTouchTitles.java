package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;

import java.util.Iterator;


public abstract class ButtonMainTouchTitles extends ButtonMainTouch
        implements Button.ChangingButton
    {
    @Override
    public ButtonMainTouchTitles clone()
        {
        return (ButtonMainTouchTitles)super.clone();
        }

    public abstract String getChangingString();

    /**
     * Draws the titles for drawButton
     * drawButton will calculate pixel coordinates previously
     * This method could be changed, if not all titles are needed
     * @param canvas canvas to draw on
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or board.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -board.areaYOffset (direct draw on screen)
     */
    @Override
    protected void drawButtonTitles( Canvas canvas, int xOffsetInPixel, int yOffsetInPixel )
        {
        int centerX = getPixelX( columnInGrids, xOffsetInPixel);
        int centerY = getPixelY( rowInGrids, yOffsetInPixel );

        Iterator<TitleDescriptor> titlesIterator = titles.iterator();

        if ( titlesIterator.hasNext() )
            titlesIterator.next(); // just step over the last item

        while (titlesIterator.hasNext() )
            {
            titlesIterator.next().drawTitle(canvas, board, centerX, centerY);
            }
        }


    /**
     * Draws the changeable part of the button - the last title.
     * Title should be supplied by getString()
     * @param canvas canvas to draw on
     */
    @Override
    public void drawChangingButton(Canvas canvas)
        {
        int centerX = getPixelX( columnInGrids, board.xOffset - board.areaXOffset );
        int centerY = getPixelY( rowInGrids, - board.areaYOffset );

        titles.getLast().drawTitle(canvas, board, getChangingString(), centerX, centerY);
        }


    /**
     * Draw button directly on the screen (above layout-bitmap) (Board.onDraw)
     * Background color is the color of the touched keys (board.softBoardData.touchColor)
     * Board.xOffset is applied (as for the layout-bitmap)
     * This method uses super-method, and then draws the changing last title.
     * @param canvas canvas of the bitmap
     */
    @Override
    public void drawTouchedButton( Canvas canvas )
        {
        super.drawTouchedButton( canvas ); // draws touchedButton without the changing part
        drawChangingButton( canvas ); // draws the changing part
        }

    }

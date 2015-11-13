package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import dancingmoon.bestboard.Board;
import dancingmoon.bestboard.utils.SinglyLinkedList;

/**
 * Base class for buttons.
 * The class itself defines an "empty" Button without any function
 */
public class Button implements Cloneable
    {
    /**
     * If a Button subclass implements ChangingButton interface,
     * then Board.onDraw() calls its drawChangingButton() method,
     * which can redraw the changed button over the layout-bitmap.
     * Buttons with ChangingButton interface will be collected in the Board.addButton method().
     */
    public interface ChangingButton
        {
        // offset is always board.xOffset (direct draw on screen)
        public void drawChangingButton(Canvas canvas);
        }


    /*
    http://stackoverflow.com/a/7580966 how to clone
    http://www.artima.com/intv/bloch13.html

    Cloned instance should be created inside the highest (most details) class,
    and this instance should travel towards the lowest (less detailed class) to load their data.
    Interface cannot achieve this, because the returned type will be different at each level.
    */
    @Override
    public Button clone()
        {
        try
            {
            return (Button)super.clone();
            }
        catch (CloneNotSupportedException e)
            {
            return null;
            }
        }


    /**
     * Button's default title text.
     * It will be only used if no title/title-text is given.
     * @return Default title text - empty string for empty button
     */
    public String getString()
        {
        return "";
        }


    /** Button's board */
    protected Board board;

    /** Button's position in grid */
    protected int columnInGrids;
    protected int rowInGrids;

    /** Button's background color */
    protected int color;

    /** Button's title(s) */
    protected SinglyLinkedList<TitleDescriptor> titles;


    /**
     * Connects the Button instance to its board and position.
     * (Each Button instance refers to only one specific button.)
     * @param board button's board
     * @param columnInHexagons column (hexagonal)
     * @param rowInHexagons row (hexagonal)
     */
    public void setPosition( Board board, int columnInHexagons, int rowInHexagons )
        {
        this.board = board;
        this.columnInGrids = getGridX( columnInHexagons, rowInHexagons );
        this.rowInGrids = getGridY( rowInHexagons );
        }


    /**
     * Sets button's background color
     * @param color background color
     */
    public void setColor( int color )
        {
        this.color = color;
        }


    /**
     * Sets button's titles.
     * Title cannot be null, but it is checked previously.
     * @param titles title(s) of the button. CANNOT BE NULL OR INVALID!
     */
    public void setTitles( SinglyLinkedList<TitleDescriptor> titles )
        {
        this.titles = titles;
        }


    /** Hexagons fill paint will be set in static initialization, color is variable */
    protected static Paint hexagonFillPaint = new Paint();

    /** Hexagons stroke paint will be set in static initialization, color is BLACK */
    protected static Paint hexagonStrokePaint = new Paint();

    static
        {
        hexagonFillPaint.setStyle( Paint.Style.FILL );
        hexagonStrokePaint.setStyle( Paint.Style.STROKE );
        hexagonStrokePaint.setStrokeWidth( 0f );
        hexagonStrokePaint.setColor( Color.BLACK );
        }


    // !! Always use X-Y or Column-Row pairs !!
    // GridX = HX * 2 + ( (HY + HK) % 2 )
    // + 1 because board is wider then area

    /**
     * Converts columns (hexagon) into grids
     * This is only needed by the constructor and ButtonForMaps!
     * @param columnInHexagons hexagonal column
     * @param rowInHexagons hexagonal row
     * @return Y-grid
     */
    protected int getGridX( int columnInHexagons, int rowInHexagons )
        {
        return columnInHexagons * 2 + ( (rowInHexagons + board.rowsAlignOffset) % 2 ) + 1;
        }


    /**
     * Converts rows (hexagon) into grids
     * This is only needed by the constructor and ButtonForMaps!
     * @param rowInHexagons hexagonal row
     * @return Y-grid
     */
    protected int getGridY( int rowInHexagons )
        {
        return rowInHexagons * 3 + 2;
        }


    // "Grid" is a rectangular coordinate-system, which measures in
    // HALF-WIDTH and QUOTER-HEIGHT hexagons

    /**
     * Converts X-grid to X-pixel
     * (Center and corners of the hexagon)
     * @param gridX grid X coordinate
     * @param xOffsetInPixel X offset in pixels
     * @return pixel X coordinate
     */
    protected int getPixelX( int gridX, int xOffsetInPixel )
        {
        return gridX * board.areaWidthInPixels / board.areaWidthInGrids + xOffsetInPixel;
        }


    /**
     * Converts Y-grid to Y-pixel
     * (Center and corners of the hexagon)
     * @param gridY grid Y coordinate
     * @param yOffsetInPixel Y offset in pixels
     * @return pixel Y coordinate
     */
    protected int getPixelY( int gridY, int yOffsetInPixel )
        {
        return gridY * board.boardHeightInPixels / board.boardHeightInGrids + yOffsetInPixel;
        }


    /**
     * Creates button's hexagon with the use of the grids
     * The created path can be used both for outline and fill
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or board.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -board.boardYOffset (direct draw on screen)
     * @return created path
     */
    protected Path hexagonPath( int xOffsetInPixel, int yOffsetInPixel)
        {
        Path path = new Path();

        int xminus = getPixelX( columnInGrids - 1, xOffsetInPixel );
        int xcenter = getPixelX( columnInGrids, xOffsetInPixel );
        int xplus = getPixelX( columnInGrids + 1, xOffsetInPixel );
        int yminus = getPixelY( rowInGrids - 1, yOffsetInPixel );
        int yplus = getPixelY( rowInGrids + 1, yOffsetInPixel );

        path.moveTo(xcenter, getPixelY( rowInGrids - 2, yOffsetInPixel ));
        path.lineTo(xplus, yminus);
        path.lineTo(xplus, yplus);
        path.lineTo(xcenter, getPixelY( rowInGrids + 2, yOffsetInPixel ));
        path.lineTo(xminus, yplus);
        path.lineTo(xminus, yminus);
        path.close();

        return path;
        }


    /**
     * Draw button directly on the screen (above layout-bitmap) (Board.onDraw)
     * Background color is the color of the touched keys (board.softBoardData.touchColor)
     * Board.xOffset is applied (as for the layout-bitmap)
     * @param canvas canvas of the bitmap
     */
    public void drawTouchedButton( Canvas canvas )
        {
        drawButton( canvas, board.softBoardData.touchColor, board.boardXOffset, board.boardYOffset);
        }


    /**
     * Draw button on layout-bitmap (Board.createLayoutScreen())
     * Background color is the button's original color
     * No x offset is applied
     * @param canvas canvas of the bitmap
     */
    public void drawButton( Canvas canvas )
        {
        drawButton( canvas, color, 0, 0 );
        }


    /**
     * Draw button with the specified background color and with the specified x-offset in pixel
     * @param canvas canvas to draw on
     * @param color background color
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or board.xOffset-board.boardXOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -board.boardYOffset (direct draw on screen)
     */
    protected void drawButton( Canvas canvas, int color, int xOffsetInPixel, int yOffsetInPixel )
        {
        // draw the background
        drawButtonBackground( canvas, color, xOffsetInPixel, yOffsetInPixel );

        // draw the titles
        drawButtonTitles(canvas, xOffsetInPixel, yOffsetInPixel );
        }


    /**
     * Draws button background with the specified background color and
     * with the specified x-offset in pixel
     * @param canvas canvas to draw on
     * @param color background color
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or board.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -board.boardYOffset (direct draw on screen)
     */
    protected void drawButtonBackground( Canvas canvas, int color, int xOffsetInPixel, int yOffsetInPixel )
        {
        hexagonFillPaint.setColor( color );

        Path hexagonPath = hexagonPath( xOffsetInPixel, yOffsetInPixel );
        canvas.drawPath(hexagonPath, hexagonFillPaint);
        canvas.drawPath(hexagonPath, hexagonStrokePaint);
        }


    /**
     * Draws the titles for drawButton
     * drawButton will calculate pixel coordinates previously
     * This method could be changed, if not all titles are needed
     * @param canvas canvas to draw on
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or board.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -board.boardYOffset (direct draw on screen)
     */
    protected void drawButtonTitles( Canvas canvas, int xOffsetInPixel, int yOffsetInPixel )
        {
        // index (in buttons[][index]) == touchCode (this is always true)
        // Theoretically from index/touchCode the buttons position can be calculated.
        // BUT this is NOT obligatory!! So the buttons will store their position.

        int centerX = getPixelX( columnInGrids, xOffsetInPixel);
        int centerY = getPixelY( rowInGrids, yOffsetInPixel );

        for ( TitleDescriptor title : titles )
            {
            title.drawTitle(canvas, board, centerX, centerY);
            }
        }
    }

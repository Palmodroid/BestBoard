package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import org.lattilad.bestboard.Layout;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.SinglyLinkedList;


/*
 *  Button
 *
 *      ButtonMainTouch
 *          ButtonSingle        - repeat (sets onStay)  - (SIMPLE) (FIRST)
 *          ButtonDouble        - second                - (DOUBLE) (FIRST) SECOND
 *          ButtonList          - second                - LIST
 *          ButtonSpaceTravel                           - SPACETRAVEL
 *          ButtonModify                                - MODIFY
 *
 *          ButtonMainTouchTitles
 *              ButtonEnter     - repeat (sets onStay)  - ENTER
 *              ButtonAlternate - second (return to previous)? repeat?
 *                                                      - ALTERNATE
 *      ButtonMultiTouch
 *          ButtonSwitch                                - SWITCH
 *          ButtonMeta                                  - META
 *
 *      ButtonForMaps
 *
 */


/**
 * Base class for buttons.
 * The class itself defines an "empty" Button without any function
 */
public class Button implements Cloneable
    {
    /**
     * If a Button subclass implements ChangingButton interface,
     * then Layout.onDraw() calls its drawButtonChangingPart() method,
     * which can redraw the changed button over the layout-bitmap.
     * Buttons with ChangingButton interface will be collected in the Layout.addButton method().
     *
    public interface ChangingButton
        {
        // offset is always layout.xOffset (direct draw on screen)
        public void drawButtonChangingPart(Canvas canvas);
        }
    */

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
    public String getFirstString()
        {
        return "";
        }

    public String getSecondString()
        {
        return getFirstString();
        }



    /** Button's layout */
    protected Layout layout;

    /** Button's position in grid */
    protected int columnInGrids;
    protected int rowInGrids;

    /** Button's background color */
    protected int color;

    /** Button's title(s) */
    protected SinglyLinkedList<TitleDescriptor> titles;

    /** Button's name - only for parsing */
    public String name;

    /**
     * Connects the Button instance to its layout and position.
     * (Each Button instance refers to only one specific button.)
     * @param layout button's layout
     * @param arrayColumn column (hexagonal)
     * @param arrayRow row (hexagonal)
     */
    public void setPosition( Layout layout, int arrayColumn, int arrayRow )
        {
        this.layout = layout;
        this.columnInGrids = getGridX(arrayColumn, arrayRow);
        this.rowInGrids = getGridY(arrayRow);
        connected();
        }

    protected void connected()
        {
        // methods which requires softboarddata should come here
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

    public SinglyLinkedList<TitleDescriptor> getTitles()
        {
        return titles;
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
    // + 1 because layout is wider then area

    /**
     * Converts columns (hexagon) into grids
     * This is only needed by the constructor and ButtonForMaps!
     * @param arrayColumn hexagonal column
     * @param arrayRow hexagonal row
     * @return Y-grid
     */
    protected int getGridX( int arrayColumn, int arrayRow )
        {
        int gridX = arrayColumn * 2 + 1 + (( arrayRow + layout.rowsAlignOffset ) % 2 );
        Scribe.debug(Debug.BUTTON, "ArrayX: " + arrayColumn + ", GridX: " + gridX + ", Align: " + layout.rowsAlignOffset);
        return gridX;
        }


    /**
     * Converts rows (hexagon) into grids
     * This is only needed by the constructor and ButtonForMaps!
     * @param arrayRow hexagonal row
     * @return Y-grid
     */
    protected int getGridY( int arrayRow )
        {
        int gridY = arrayRow * 3 + 2;
        Scribe.debug( Debug.BUTTON, "ArrayY: " + arrayRow + ", GridY: " + gridY );
        return arrayRow * 3 + 2;
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
        return gridX * layout.areaWidthInPixels / layout.areaWidthInGrids + xOffsetInPixel;
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
        return gridY * layout.layoutHeightInPixels / layout.layoutHeightInGrids + yOffsetInPixel;
        }


    /**
     * Creates button's hexagon with the use of the grids
     * The created path can be used both for outline and fill
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or layout.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -layout.layoutYOffset (direct draw on screen)
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
     * Changing buttons should be redrawn after each touch
     * This method should return true, if this is a changing button.
     * The original method checks only showtitles.
     * To change other parts (background), this method should be overridden
     * @return true, if this button has got changing parts
     */
    public boolean isChangingButton()
        {
        // This method checks only show-titles
        return titles.getLast().isShowTitle();
        }


    /**
     * Button subclasses can decide about the default title type by overriding this method.
     * @return default title type
     */
    public int defaultTitleType()
        {
        return TitleDescriptor.GET_FIRST_STRING;
        }


    /**
     * Draw button on layout-bitmap (Layout.createLayoutScreen())
     * Background color is the button's original color
     * No x offset is applied
     * @param canvas canvas of the bitmap
     */
    public void drawButtonConstantPart(Canvas canvas)
        {
        // draw the background
        drawButtonBackground(canvas, color, 0, 0);

        // draw the titles - ONLY TEXT titles
        drawButtonTextTitles(canvas, 0, 0);
        }


    /**
     * If isChangingButton == true than changing part will be redrawn at every touch
     * Draw button directly on the screen (above layout-bitmap) (Layout.onDraw)
     * Layout.xOffset is applied (as for the layout-bitmap)
     * @param canvas canvas of the bitmap
     */
    public void drawButtonChangingPart(Canvas canvas)
        {
        // draw last title - if it is a showtitle
        drawButtonShowTitle(canvas, layout.layoutXOffset, layout.layoutYOffset);
        }


    /**
     * Draw button directly on the screen (above layout-bitmap) (Layout.onDraw)
     * Background color is the color of the touched keys (layout.softBoardData.touchColor)
     * Layout.xOffset is applied (as for the layout-bitmap)
     * @param canvas canvas of the bitmap
     */
    public void drawButtonTouched(Canvas canvas)
        {
        // draw the background
        drawButtonBackground(canvas, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);

        // draw the titles - ONLY TEXT titles
        drawButtonTextTitles(canvas, layout.layoutXOffset, layout.layoutYOffset);

        drawButtonChangingPart(canvas);
        }


    /**
     * Draws button background with the specified background color and
     * with the specified x-offset in pixel
     * @param canvas canvas to draw on
     * @param color background color
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or layout.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -layout.layoutYOffset (direct draw on screen)
     */
    protected void drawButtonBackground( Canvas canvas, int color, int xOffsetInPixel, int yOffsetInPixel )
        {
        hexagonFillPaint.setColor( color );

        Path hexagonPath = hexagonPath(xOffsetInPixel, yOffsetInPixel);
        canvas.drawPath(hexagonPath, hexagonFillPaint);
        canvas.drawPath(hexagonPath, hexagonStrokePaint);
        }


    /**
     * Draws the titles for drawButton. ONLY TEXT Titles are drawn, show-titles are skipped
     * drawButton will calculate pixel coordinates previously
     * This method could be changed, if not all titles are needed
     * @param canvas canvas to draw on
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or layout.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -layout.layoutYOffset (direct draw on screen)
     */
    protected void drawButtonTextTitles(Canvas canvas, int xOffsetInPixel, int yOffsetInPixel)
        {
        // index (in buttons[][index]) == touchCode (this is always true)
        // Theoretically from index/touchCode the buttons position can be calculated.
        // BUT this is NOT obligatory!! So the buttons will store their position.

        int centerX = getPixelX(columnInGrids, xOffsetInPixel);
        int centerY = getPixelY(rowInGrids, yOffsetInPixel);

        for ( TitleDescriptor title : titles )
            {
            // ONLY TEXT titles are drawn (text != null)
            title.drawTextTitle(canvas, layout, centerX, centerY);
            }
        }


    /**
     * Draws the showtitle (if any) for drawButton. Show-title can be ONLY at the last position
     * drawButton will calculate pixel coordinates previously
     * This method could be changed, if not all titles are needed
     * @param canvas canvas to draw on
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or layout.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -layout.layoutYOffset (direct draw on screen)
     */
    protected void drawButtonShowTitle(Canvas canvas, int xOffsetInPixel, int yOffsetInPixel)
        {
        int centerX = getPixelX(columnInGrids, xOffsetInPixel);
        int centerY = getPixelY(rowInGrids, yOffsetInPixel);

        // ONLY VALID MARKERS are drawn
        titles.getLast().drawShowTitle(canvas, layout, centerX, centerY);
        }


    /**
     * Grid numbers are written for all buttons (even for non-existing ones) by layout creator
     * Calculations of the button class, and the static textPaint of TitleDescriptor are used
     * Grid text is the USERCOLUMN, or USERROW : USERCOLUMN for the 2nd column
     * @param canvas canvas
     * @param layout layout
     * @param columnInHexagons arrayColumn
     * @param rowInHexagons arrayRow
     */
    public void drawGridTitle( Canvas canvas, Layout layout, int columnInHexagons, int rowInHexagons )
        {
        setPosition( layout, columnInHexagons, rowInHexagons );

        TitleDescriptor.textPaint.setTextSize(layout.fontData.textSize);
        TitleDescriptor.textPaint.setColor(Color.BLACK);
        TitleDescriptor.textPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
                Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG);
        TitleDescriptor.textPaint.setTextSkewX(0);

        columnInHexagons++; // back from arrayColumn
        rowInHexagons++;    // back from arrayRow

        canvas.drawText(
                columnInHexagons == 2 ? rowInHexagons + ":" + columnInHexagons : Integer.toString(columnInHexagons),
                getPixelX( columnInGrids, 0), // + layout.halfHexagonWidthInPixels / 1000,
                getPixelY( rowInGrids, - layout.halfHexagonHeightInPixels / 2),
                TitleDescriptor.textPaint);
        }
    }

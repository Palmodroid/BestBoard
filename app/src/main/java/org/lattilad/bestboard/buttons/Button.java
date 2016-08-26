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

    public boolean isFirstStringChanging()
        {
        return false;
        }

    public String getSecondString()
        {
        return "";
        }

    public boolean isSecondStringChanging()
        {
        return false;
        }


    /** Button's layout */
    protected Layout layout;

    public Layout getLayout()
        {
        return layout;
        }


    /** Button's and hexagon's position in pixel */
    private int xMinus;
    private int xCenter;
    private int xPlus;

    private int yMinus2;
    private int yMinus1;
    private int yCenter;
    private int yPlus1;
    private int yPlus2;



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
        setPosition(arrayColumn,arrayRow);
        }

    /**
     * If layout is ready (eg. ButtonForMaps) connects the Button instance to its position.
     * @param arrayColumn column (hexagonal)
     * @param arrayRow row (hexagonal)
     */
    public void setPosition( int arrayColumn, int arrayRow )
        {
        int columnInGrids = getGridX(arrayColumn, arrayRow);
        int rowInGrids = getGridY(arrayRow);

        xMinus = getPixelX( columnInGrids - 1 );
        xCenter = getPixelX( columnInGrids );
        xPlus = getPixelX( columnInGrids + 1 );

        yMinus2 = getPixelY( rowInGrids - 2 );
        yMinus1 = getPixelY( rowInGrids - 1 );
        yCenter = getPixelY( rowInGrids );
        yPlus1 = getPixelY( rowInGrids + 1 );
        yPlus2 = getPixelY( rowInGrids + 2 );

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
     * Converts X-grid to X-pixel without offset
     * (Center and corners of the hexagon)
     * @param gridX grid X coordinate
     * @return pixel X coordinate
     */
    protected int getPixelX( int gridX )
        {
        return gridX * layout.areaWidthInPixels / layout.areaWidthInGrids;
        }


    /**
     * Converts Y-grid to Y-pixel without offset
     * (Center and corners of the hexagon)
     * @param gridY grid Y coordinate
     * @return pixel Y coordinate
     */
    protected int getPixelY( int gridY )
        {
        return gridY * layout.layoutHeightInPixels / layout.layoutHeightInGrids;
        }

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
     * Returns button centre in pixels without offset
     * @return x center
     */
    public int getXCenter()
        {
        return xCenter;
        }

    /**
     * Returns button centre in pixels without offset
     * @return y center
     */
    public int getYCenter()
        {
        return yCenter;
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

        path.moveTo(xCenter + xOffsetInPixel, yMinus2 + yOffsetInPixel);
        path.lineTo(xPlus + xOffsetInPixel, yMinus1 + yOffsetInPixel);
        path.lineTo(xPlus + xOffsetInPixel, yPlus1 + yOffsetInPixel);
        path.lineTo(xCenter + xOffsetInPixel, yPlus2 + yOffsetInPixel);
        path.lineTo(xMinus + xOffsetInPixel, yPlus1 + yOffsetInPixel);
        path.lineTo(xMinus + xOffsetInPixel, yMinus1 + yOffsetInPixel);
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
        return isChangingTitle() != TitleDescriptor.NO_CHANGE;
        }

    public int isChangingTitle()
        {
        int isChanging = 0;
        for ( TitleDescriptor title : titles )
            {
            isChanging |= title.isChangingTitle( this );
            }
        return isChanging;
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
     * Change           ConstantPart    ChangingPart    Touched
     * Background             -           B T(all)       tB T(all)
     * Title:
     *    NO              B T(NO FIRST SECOND)           tB T(all)
     *    FIRST           B T(NO)         T(FIRST)       tB T(all)
     *    SECOND          B T(NO)         T(SECOND)      tB T(all)
     *    SHOW            B T(NO)         T(SHOW)        tB T(all)
     *
     * Button.changingInfo contains binary information about changing parts:
     *
     *  00011110
     *         * - Constant text (only together with background)
     *        *  - Primary text (depends on the result of isPrimaryTextChanging())
     *       *   - Secondary text (depends on the result of isSecondaryTextChanging())
     *      *    - Showtext - always change
     *     *     - Background (and all texts should change)
     *
     *  TitleDescriptor.getChangingInfo() returns whether this title is changing (same format as above)
     *
     *  Button.setChaningInfo() should be called always (?? where)
     *  If Button subtype needs changing background, then it should be overridden to return -1
     *  Otherwise it should return the sum (logical-or) of titles (getTitlesChangingInfo())
     *
     *  Constants:
     *  ALL_CHANGE: -1 (because with background all titles should be redrawn)
     *  BACKGROUND_CHANGE = 16;
     public static final int NO_CHANGE = 0;
     public static final int TEXT_TITLE_CHANGE = 1;
     public static final int SHOW_TITLE_CHANGE = 2;
     public static final int FIRST_TITLE_CHANGE = 4;
     public static final int SECOND_TITLE_CHANGE = 8;
     *
     *  Button.drawConstantParts() - is only called when layout is created
     *      Draw NON-changing parts, !changingInfo (logical-no) should be used
     *
     *  BUtton.drawChangingParts() - is called at every touch
     *      Draw changing parts, changingInfo shows these parts with binary 1
     *
     *  Button.drawTouchedParts() - is called when button is in touch
     *      Draw touched background
     *      Draw all titles (call drawTitles() with -1 (ALL_CHANGE)
     *
     *   Button subclasses should override:
     *   isPrimaryTextChanging()/isSecondaryTextChanging() (together with getPrimary/secondary text)
     *   - if they use changing packets
     *   OR - at least
     *   getPrimaryText - to get one static title
     *
     *   setChangingInfo to return -1, AND drawBackground, if button needs changing background
     */


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

        for ( TitleDescriptor title : titles )
            {
            // ONLY TEXT titles are drawn (text != null)
            title.drawTextTitle(canvas, this, xOffsetInPixel, yOffsetInPixel);
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
        // ONLY VALID MARKERS are drawn
        titles.getLast().drawShowTitle(canvas, this, xOffsetInPixel, yOffsetInPixel);
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
                getXCenter(), // + layout.halfHexagonWidthInPixels / 1000,
                getYCenter() - layout.halfHexagonHeightInPixels / 2,
                TitleDescriptor.textPaint);
        }
    }

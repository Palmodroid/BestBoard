package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import org.lattilad.bestboard.Layout;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.utils.SinglyLinkedList;

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
 *  TitleDescriptor.letChangingInfo() returns whether this title is changing (same format as above)
 *
 *  Button.letChangingInfo() should be called always (after titles are added)
 *  If Button subtype needs changing background, then it should return -1
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
 *   isColorChanging() if background can change together with
 *   getColor() to return background color depending on the situation. Supermethod returns default color.
 */

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
    /** Button's layout to reach layout's and softboard's data */
    protected Layout layout;

    /** Position data of the button in grids */
    int columnInGrids;
    int rowInGrids;

    /** Position data of the button's hexagon in pixel */
    private int xMinus;
    private int xCenter;
    private int xPlus;

    private int yMinus2;
    private int yMinus1;
    private int yCenter;
    private int yPlus1;
    private int yPlus2;

    /**
     * Button.changingInfo contains binary information about changing parts:
     *  00011110
     *         * - Constant text- never change
     *        *  - Showtext - always change
     *       *   - Primary text (depends on the result of isPrimaryTextChanging())
     *      *    - Secondary text (depends on the result of isSecondaryTextChanging())
     *     *     - Background (and all texts should change)
     */
    public static final int DRAW_NOTHING = 0;
    public static final int DRAW_TEXT_TITLE = 1;
    public static final int DRAW_SHOW_TITLE = 2;
    public static final int DRAW_FIRST_TITLE = 4;
    public static final int DRAW_SECOND_TITLE = 8;
    public static final int DRAW_BACKGROUND = 16;
    public static final int DRAW_ALL = -1;

    /** Changing parts of the button */
    protected int changingInfo;

    /** Button's default background color */
    protected int color;

    /** Button's title(s) */
    protected SinglyLinkedList<TitleDescriptor> titles;

    /** Button's name - only for parsing */
    public String name;

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
     * Get button's layout set by setPosition() (or ButtonForMaps constructor)
     * @return layout
     */
    public Layout getLayout()
        {
        return layout;
        }


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
     * Connects the Button instance to its position
     * if layout is ready (eg. ButtonForMaps sets layout in constructor)
     * @param arrayColumn column (hexagonal)
     * @param arrayRow row (hexagonal)
     */
    public void setPosition( int arrayColumn, int arrayRow )
        {
        columnInGrids = getGridX(arrayColumn, arrayRow);
        rowInGrids = getGridY(arrayRow);

        connected();
        }


    /**
     * This method cen be overridden if button needs initialization after getting
     * layout/softboard data
     */
    protected void connected()
        {
        }


    /**
     * This method is called, when layout is ready, so it can be measured
     */
    public void onLayoutReady()
        {
        xMinus = getPixelX( columnInGrids - 1 );
        xCenter = getPixelX( columnInGrids );
        xPlus = getPixelX( columnInGrids + 1 );

        yMinus2 = getPixelY( rowInGrids - 2 );
        yMinus1 = getPixelY( rowInGrids - 1 );
        yCenter = getPixelY( rowInGrids );
        yPlus1 = getPixelY( rowInGrids + 1 );
        yPlus2 = getPixelY( rowInGrids + 2 );
        }


    // "Grid" is a rectangular coordinate-system, which measures in
    // HALF-WIDTH and QUOTER-HEIGHT hexagons

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
     *
    protected int getPixelX( int gridX, int xOffsetInPixel )
        {
        return gridX * layout.areaWidthInPixels / layout.areaWidthInGrids + xOffsetInPixel;
        }


    **
     * Converts Y-grid to Y-pixel
     * (Center and corners of the hexagon)
     * @param gridY grid Y coordinate
     * @param yOffsetInPixel Y offset in pixels
     * @return pixel Y coordinate
     *
    protected int getPixelY( int gridY, int yOffsetInPixel )
        {
        return gridY * layout.layoutHeightInPixels / layout.layoutHeightInGrids + yOffsetInPixel;
        }
     */


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
     * Changing buttons are redrawn at every cycle. Layout needs this information
     * @return true if button has got changing parts
     */
    public boolean isChangingButton()
        {
        return changingInfo != DRAW_NOTHING;
        }

    /**
     * Changing buttons should be redrawn during each invalidation
     * This method calculates and returns changingInfo,
     * which contains the changing parts in binary format.
     * To change other parts (background), this method should be overridden
     * @return calculated changingInfo
     */
    protected int letChangingInfo()
        {
        // if background is changing, then everything should be redrawn at each session
        if (isColorChanging())
            return DRAW_ALL;

        // otherwise title-list decides, which title types should be redrawn
        int changingInfo = 0;
        for ( TitleDescriptor title : titles )
            {
            changingInfo |= title.getChangingInfo( this );
            }

        return changingInfo;
        }


    /**
     * Button subclasses can decide about the default title type by overriding this method.
     * @return default title type
     */
    public int getDefaultTitleType()
        {
        return TitleDescriptor.GET_FIRST_STRING;
        }


    /**
     * Subclasses should override this method, to gather information for the first string
     * from the firstPacket (if any)
     * @return true, if first string can change during sessions
     */
    public boolean isFirstStringChanging()
        {
        return false;
        }


    /**
     * Button's default, first title text.
     * @return Default title text - empty string for empty button
     */
    public String getFirstString()
        {
        return "";
        }


    /**
     * Subclasses should override this method, to gather information for the second string
     * from the secondPacket (if any)
     * @return true, if second string can change during sessions
     */
    public boolean isSecondStringChanging()
        {
        return false;
        }


    /**
     * Button's second title text.
     * @return Default title text - empty string for empty button
     */
    public String getSecondString()
        {
        return "";
        }


    /**
     * Sets button's titles.
     * Title cannot be null, but it is checked previously.
     * ChangingInfo is recalculated after each set, because new titles can change it.
     * This method is called during button creation and extension.
     * @param titles title(s) of the button. CANNOT BE NULL OR INVALID!
     */
    public void setTitles( SinglyLinkedList<TitleDescriptor> titles )
        {
        this.titles = titles;
        changingInfo = letChangingInfo();
        }


    /**
     * Gets button's title-list
     * @return title(s) of the button
     */
    public SinglyLinkedList<TitleDescriptor> getTitles()
        {
        return titles;
        }


    /**
     * Subclasses should override this method, if background could change between sessions
     * @return true, if background can change
     */
    public boolean isColorChanging()
        {
        return false;
        }


    /**
     * Sets button's default background color
     * @param color background color
     */
    public void setColor( int color )
        {
        this.color = color;
        }


    /**
     * Button subclasses can set background color by overriding this method.
     * Supermethod will return default background color (set by setColor)
     * @return default title type
     */
    public int getColor()
        {
        return color;
        }


    /**
     * Helper method to draw the whole button
     * @param canvas canvas of the layout
     * @param drawInfo button parts to be drawn as defined by changing parts binary format
     * @param color color of the background (getColor() should be used for default color)
     * @param xOffsetInPixel x offset in pixels
     * (can be 0 (layout bitmap) or layout.xOffset (direct draw on screen)
     * @param yOffsetInPixel y offset in pixels
     * (can be 0 (layout bitmap) or -layout.layoutYOffset (direct draw on screen)
     */
    private void drawButton( Canvas canvas, int drawInfo, int color, int xOffsetInPixel, int yOffsetInPixel )
        {

        // draw the background

        if ((drawInfo & DRAW_BACKGROUND) != 0)
            {
            hexagonFillPaint.setColor( color );

            Path hexagonPath = hexagonPath(xOffsetInPixel, yOffsetInPixel);
            canvas.drawPath(hexagonPath, hexagonFillPaint);
            canvas.drawPath(hexagonPath, hexagonStrokePaint);
            }

        // draw the titles

        // index (in buttons[][index]) == touchCode (this is always true)
        // Theoretically from index/touchCode the buttons position can be calculated.
        // BUT this is NOT obligatory!! So the buttons will store their position.
        for ( TitleDescriptor title : titles )
            {
            // ONLY TEXT titles are drawn (text != null)
            title.drawTitle(canvas, drawInfo, this, xOffsetInPixel, yOffsetInPixel);
            }
        }


    /**
     * Draw button on layout-bitmap (Layout.createLayoutScreen())
     * Background color is the button's original color
     * No x offset is applied
     * NON-changing parts are drawn, so the opposite of changingInfo is needed
     * @param canvas canvas of the bitmap
     */
    public void drawButtonConstantPart( Canvas canvas )
        {
        drawButton( canvas, ~changingInfo, getColor(), 0, 0 );
        }


    /**
     * Draw button on layout-bitmap (Layout.createLayoutScreen())
     * Background color is the button's original color
     * No x offset is applied
     * Changing parts are drawn
     * @param canvas canvas of the bitmap
     */
    public void drawButtonChangingPart( Canvas canvas )
        {
        drawButton( canvas, changingInfo, getColor(), 0, 0 );
        }


    /**
     * Draw button directly on the screen (above layout-bitmap) (Layout.onDraw)
     * Background color is the color of the touched keys (layout.softBoardData.touchColor)
     * Layout.xOffset is applied (as for the layout-bitmap)
     * ALL parts should be drawn
     * @param canvas canvas of the bitmap
     */
    public void drawButtonTouched( Canvas canvas )
        {
        drawButton( canvas, DRAW_ALL, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);
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

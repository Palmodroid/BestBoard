package org.lattilad.bestboard;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import org.lattilad.bestboard.buttons.Button;
import org.lattilad.bestboard.buttons.ButtonForMaps;
import org.lattilad.bestboard.buttons.ButtonMonitorRow;
import org.lattilad.bestboard.buttons.TitleDescriptor;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.BoardTable;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.MetaState;
import org.lattilad.bestboard.utils.ExternalDataException;
import org.lattilad.bestboard.utils.Trilean;

import java.io.File;
import java.util.ArrayList;

public class Layout
    {
    /**
     ** CLASS VARIABLES - LOADED FROM COAT DESCRIPTOR FILE
     **/

    /**
     * Areas without button will give EMPTY_TOUCH_CODE.
     * More buttons than EMPTY_TOUCH_CODE cannot be defined.
     */
    public final static int EMPTY_TOUCH_CODE = 0x3FF;

    /**
     * The number of buttons on one layout is maximized
     * The 'last' code (which can be represented on the map) determines the maximum number of buttons
     */
    public static final int MAX_BUTTONS = EMPTY_TOUCH_CODE;

    /**
     * Maximal layout width in hexagons
     */
    public static final int MAX_LAYOUT_WIDTH_IN_HEXAGONS = 48;

    /**
     * Maximal layout height in hexagons
     */
    public static final int MAX_LAYOUT_HEIGHT_IN_HEXAGONS = 24;

    /**
     * Data stored in softBoardData is needed for each layout.
     */
    public SoftBoardData softBoardData;

    /**
     * number of keys (full hexagons) in one row of the layout
     * This is the number of the STORED keys, and NOT the displayed hexagons
     */
    public int layoutWidthInHexagons;

    /**
     * number of hexagon rows
     * This is the number of the STORED rows, and NOT the displayed hexagons
     */
    public int layoutHeightInHexagons;

    /**
     * displayed half hexagons in one row (this value is used for calculations)
     * layout is one hexagon wider
     */
    public int areaWidthInGrids;

    /**
     * layout height in quarter hexagon rows (this value is used for calculations)
     * displayed area can be smaller if hideTop or hideBottom are active
     */
    public int layoutHeightInGrids;

    /**
     * 1 if first (0) row starts with WHOLE button on the left
     * 0 if first (0) row starts with HALF button on the left
     */
    public int rowsAlignOffset;

    /**
     * true if layout is optimised for landscape. Portrait (false) layouts can be used in both modes
     */
    public boolean wide = false;

    /**
     * background color of the layout
     */
    public int layoutColor;
    
    public int lineColor;
    
    public int lineSize;
    
    private File picture = null;

    /**
     * meta-states to force
     */
    private Trilean[] metaStates;

    /**
     * Needed by TitleDescriptor to change titles to uppercase if CAPS is forced
     * @return true if caps lock is forced by the layout
     */
    public boolean isCapsForced()
        {
        return metaStates[LayoutStates.META_CAPS].isTrue();
        }

    /**
     * Buttons of the layout - will be initialized in constructor, and filled up by addButton
     */
    public Button[] buttons;

    /**
     * Changeable buttons of the layout - filled up by addButton
     */
    public ArrayList<Button> changingButtons = new ArrayList<>();


    /**
     ** CLASS VARIABLES - CALCULATED FROM SCREEN SPECIFIC DATA
     **/


    /**
     * screen width and height are stored to check whether new measurement is needed
     * in calculateScreenData
     * if new calculation is needed, then width value should be invalidated (-1)
     */
    public int screenWidthInPixels = -1;
    public int screenHeightInPixels;

    /**
     ** areaWidth and layoutHeight are measured and calculated first
     ** layoutWidth is two halfhexagons wider then areaWidth,
     ** Xoffset is one (-)halfhexagon and the offset of the whole layout
     ** (non-wide on wide screen oe 0)
     ** areaHeight depends on hidden edges and on monitor row,
     ** Yoffset depends on hidden upper part
     **/

    /**
     * width of the visible area (equals to screen's lower diameter for non-wide layouts)
     */
    public int areaWidthInPixels;

    /**
     * visible height: height of the layout - hidden edges and monitor
     */
    public int areaHeightInPixels;

    /**
     * layout width in pixels - it contains the non-visible two half-hexagons, too
     */
    public int layoutWidthInPixels;

    /**
     * layout height in pixels (calculated from width)
     */
    public int layoutHeightInPixels;

    /**
     * offset in landscape if layout is not wide - a half hexagon width
     */
    public int layoutXOffset;

    /**
     * equals with a quarter of hexagon, if upper edge is hidden
     */
    public int layoutYOffset;

    /**
     * Text with this size:
     * 5 chars can fit in one hexagon width AND text can fit in one grid height
     */
    public TitleDescriptor.FontData fontData;
    // TITLE DESCRIPTOR needs this data !!

    /**
     * halfHexagons are used in title positioning
     */
    public int halfHexagonHeightInPixels;
    // TITLE DESCRIPTOR needs this data !!

    /**
     * halfHexagons are used in title positioning
     */
    public int halfHexagonWidthInPixels;
    // TITLE DESCRIPTOR needs this data !!

    /**
     * Map contains the touchCodes for all levels
     */
    private Bitmap layoutMap;

    /**
     * Layout skin for the current layout.
     */
    private Bitmap layoutPicture = null;


    /**
     * Paint for monitor row background
     */
    private Paint monitorBackground = new Paint();


    /**
     * Paint for monitor row text
     */
    private Paint monitorText = new Paint();


    /**
     * Ratio of monitor text (permil)
     * Actual size should be calculated in calculateScreenData
     */
    private int monitorSizePermil = 1000;


    /**
     * Size of monitor row - calculated in calculateScreenData
     */
    private int monitorSize = 0;


    /**
     ** CONSTRUCTION OF THE LAYOUT
     ** Parsing phase:
     **   1. Constructor - adds non screen-specific data
     **      setForcedMeta() - if meta-states should be forced
     **   2. addButton - populates the buttons array and
     **      setShift - sets the shift levels by descriptor file
     ** Displaying phase:
     **   3. onMeasure - receives screen diameters
     **   4. calculateScreenData - screen specific information set by onMeasure
     **/

    /**
     * Constructor needs data to generate a keyboard with button-holes.
     * It needs information about the measures of the layout.
     * All specific data (buttons etc.) will be added later.
     * Screen information is needed to draw the layout.
     * It will added in the calculateScreenData() method.
     *
     * @param data           softBoardData for all boards
     * @param halfcolumns    width in half hexagons (grids)
     * @param rows           height in full hexagons
     * @param oddRowsAligned first row (0) starts with whole button on the left
     * @param wide           layout is optimised for wide (landscape) screen (cannot be used in portrait)
     * @param color          background color
     * @param metaStates     forced metastates
     * @throws IllegalArgumentException if layout cannot be created with this dimension
     *                                  Dimension can be checked previously with isValidDimension()
     */
    public Layout(SoftBoardData data, int halfcolumns, int rows,
                  boolean oddRowsAligned, boolean wide, 
                  int color, int lineColor, int lineSize,
                  Trilean[] metaStates) throws ExternalDataException
        {
        Scribe.locus( Debug.LAYOUT);
        // NON SCREEN-SPECIFIC DATA

        // Common data is needed
        this.softBoardData = data;

        // first two hidden, "side" halfcolumns are added, then full columns are calculated
        // even halfcolumns: last button can be in hidden position,
        // but starting with a half button it will appear
        this.layoutWidthInHexagons = (halfcolumns + 2) / 2;
        this.layoutHeightInHexagons = rows;

        // Each row has one more half hexagon column
        this.areaWidthInGrids = halfcolumns;

        // Each row has three quarters of hexagonal height (3 grids) + 1 for the last row
        this.layoutHeightInGrids = layoutHeightInHexagons * 3 + 1;

        if (!isValidDimension(layoutWidthInHexagons, layoutHeightInHexagons))
            {
            throw new ExternalDataException("Layout cannot be created with these arguments!");
            }

        this.rowsAlignOffset = oddRowsAligned ? 1 : 0;

        this.wide = wide;

        this.layoutColor = color;
        this.lineColor = lineColor;
        this.lineSize = lineSize;

        // INITIALIZE MONITOR ROW
        // These data can be overridden later,
        // Size will be set later
        setMonitorText( 1000, false, false, Color.BLACK);

        monitorBackground.setStyle(Paint.Style.FILL_AND_STROKE);
        monitorBackground.setColor( layoutColor );


        // INITIALIZE BUTTONS' ARRAY
        // this two-dimensional array will be populated later
        // null: non-defined (empty) button

        buttons = new Button[layoutWidthInHexagons * layoutHeightInHexagons + 1];
        // +1 because 0 is the monitor-row-button
        // ADDBUTTON will fill up this array

        //**************************************
        Button button = new ButtonMonitorRow( );
        button.setPosition(this, 0, 0);
        buttons[0] = button;
        //**************************************

        // SETSCREENDATA is needed for screen-specific information

        this.metaStates = metaStates;
        }

    public void setPicture( File picture )
        {
        this.picture = picture;
        }

    boolean[] storedMetaStates = new boolean[ LayoutStates.META_STATES_SIZE ];

    /**
     * Force meta-states, as defined by setForcedMeta()
     * This method is called when layout is chosen.
     * revertForcedMetaStates should be called when exiting layout.
     */
    public void forceMetaStates()
        {
        for (int m = 0; m < metaStates.length; m++)
            {
            // LOCKed and non-LOCKed state will be restored (but TOUCH and ON not!)
            storedMetaStates[m] =
                    (softBoardData.layoutStates.metaStates[m].getState() == MetaState.META_LOCK);

            if ( metaStates[m].isTrue() )
                softBoardData.layoutStates.metaStates[m].setState( MetaState.META_LOCK );
            else if ( metaStates[m].isFalse() )
                softBoardData.layoutStates.metaStates[m].setState( MetaState.META_OFF );
            // IGNORE -> no change
            }
        }

    public void revertForcedMetaStates()
        {
        for (int m = 0; m < metaStates.length; m++)
            {
            // LOCKED and OFF state are reverted for forced metas.
            if ( !metaStates[m].isIgnored() )
                {
                softBoardData.layoutStates.metaStates[m].setState(
                        storedMetaStates[m] ? MetaState.META_LOCK : MetaState.META_OFF );
                }
            }
        }


    /**
     * Predefined button is added at the defined position.
     * Layout and position infos are added previously.
     *
     * @param arrayColumn arrayColumn (in hexagons) of the button
     * @param arrayRow    arrayRow (in hexagons) of the button
     * @param button predefined button instance (layout, positions are not needed)
     * @return true if button overwrites an other button
     * @throws ExternalDataException If button position is not valid
     */
    public boolean addButton(int arrayColumn, int arrayRow, Button button) throws ExternalDataException
        {
        // BUTTONS ARE COMING FROM COAT DESCRIPTOR FILE
        // Scribe.locus();

        if ( checkPosition(arrayColumn, arrayRow) <= POSITION_LINE_ENDED )
            {
            throw new ExternalDataException("This button position is not valid! Button cannot be added!");
            }

        boolean ret = false;

        button.setPosition(this, arrayColumn, arrayRow);

        // put in its position
        int index = touchCodeFromPosition(arrayColumn, arrayRow);

        // check whether this is empty position !! changedButtons list is not checked if button is overwritten !!
        if (buttons[index] != null)
            ret = true;

        // put button to its place
        buttons[index] = button;

        // if button is a changedButton, then store it in changedButtons list as well
        if (button.isChangingButton())
            changingButtons.add(button);

        return ret;
        }


    /**
     * Gets an existing button.
     * @param arrayColumn arrayColumn (in hexagons) of the button
     * @param arrayRow    arrayRow (in hexagons) of the button
     * @return button instance (which is already on the layout!)
     * @throws ExternalDataException If button position is not valid
     */
    public Button getButton( int arrayColumn, int arrayRow ) throws ExternalDataException
        {
        if ( checkPosition(arrayColumn, arrayRow) == POSITION_INVALID )
            {
            throw new ExternalDataException("This button position is not valid! Button cannot be get!");
            }

        int index = touchCodeFromPosition(arrayColumn, arrayRow);

        return buttons[index];
        }


    /**
     * Calculates dimensions of this layout from screen specific data:
     * - xOffset, layoutWidthInPixels, layoutHeightInPixels
     * - halfHexagonWidthInPixels, halfHexagonWidthInPixels
     * - textSize
     * It is called by LayoutView.onMeasure() when screen (width) is changed or
     * layout is changed. Recalculation is needed only, when ScreenWidthInPixels changed.
     * This method also calculates data from preferences.
     * If those data are changed, invalidateCalculations should be called, to invalidate data.
     * @param screenWidthInPixels  screen width
     * @param screenHeightInPixels screen height - full screen height should be given!
     */
    public void calculateScreenData( int screenWidthInPixels, int screenHeightInPixels )
        {
        Scribe.locus( Debug.LAYOUT);
        
        // calculateScreenData is needed only, if orientation was changed
        // invalidateCalculations invalidates it to force calculations
        // With Navigation Bar, this question become more difficult,
        // measure cycle somtimes fails, and there are more evaluations.
        // This check is maintained for older systems
        if ( screenWidthInPixels == this.screenWidthInPixels &&
                screenHeightInPixels == this.screenHeightInPixels )
            return;

        this.screenWidthInPixels = screenWidthInPixels;
        this.screenHeightInPixels = screenHeightInPixels;

        // GENERATE SCREEN SPECIFIC VALUES
        boolean landscape = (screenWidthInPixels > screenHeightInPixels);
        boolean landscapeControl = softBoardData.boardTable.getOrientation() == BoardTable.ORIENTATION_LANDSCAPE;

        // orientation can be found in UseState also
        if ( landscape != landscapeControl )
            Scribe.error("Orientation in onMeasure and in link-state is not the same!");

        // temporary variables are needed to check whether layout dimension is changed

        // IMPORTANT!
        // New area WIDTH is calculated, real layout width is wider with one hexagon, and
        // New layout HEIGHT is calculated, real height is calculated from hideupper/lower and monitor

        int newAreaWidthInPixels;
        int newLayoutHeightInPixels;

        // Calculate AreaWidth and Offset

        // WIDE layout for LANDSCAPE mode OR NORMAL layout for PORTRAIT mode
        if (wide == landscape)
            {
            newAreaWidthInPixels = screenWidthInPixels;
            layoutXOffset = 0;
            Scribe.debug( Debug.LAYOUT, "Full width keyboard");
            }
        // NORMAL layout for LANDSCAPE mode - change values
        else if (!wide) // && landscape)
            {
            // noinspection SuspiciousNameCombination
            newAreaWidthInPixels = screenHeightInPixels; // This is the shorter diameter
            layoutXOffset = (screenWidthInPixels - newAreaWidthInPixels) *
                    softBoardData.landscapeOffsetPermil / 1000;
            Scribe.debug( Debug.LAYOUT, "Normal keyboard for landscape. Offset:" + layoutXOffset);
            }
        // LANDSCAPE layout PORTRAIT mode - incompatible layout! - !! NOW WE LET IT WORK (TESTING!!) !!
        else // if (wide && !landscape)
            {
            newAreaWidthInPixels = screenWidthInPixels; // Layout will be distorted!!
            layoutXOffset = 0;
            Scribe.debug( Debug.LAYOUT, "Wide keyboard for portrait! NOT POSSIBLE! Keyboard is distorted.");
            }

        // Calculate BoardHeight - from AreaWidth

        // Layout pixel height is calculated with the ratio of a regular hexagon
        // after this point all the measurements are calculated from pixelHeight backwards
        newLayoutHeightInPixels = ((layoutHeightInGrids * newAreaWidthInPixels * 1000) /
                ( areaWidthInGrids * 1732));

        // Only part of the real height can be occupied, this can be set by prefs
        // this true even for NORMAL layout: the actual height (== width of the layout) should be calculated
        screenHeightInPixels *= softBoardData.heightRatioPermil;
        screenHeightInPixels /= 1000;
        
        // If layout height exceeds maximal value, layout will be distorted
        if (newLayoutHeightInPixels > screenHeightInPixels)
            newLayoutHeightInPixels = screenHeightInPixels;

        // If layout dimensions are changed then layout should be redrawn
        if (newAreaWidthInPixels != areaWidthInPixels ||
                newLayoutHeightInPixels != layoutHeightInPixels)
            {
            // release layout picture
            layoutMap = null;
            layoutPicture = null;
            }

        // if dimensions are changed, all variables should be recalculated

        areaWidthInPixels = newAreaWidthInPixels;
        layoutHeightInPixels = newLayoutHeightInPixels;

        halfHexagonWidthInPixels = areaWidthInPixels / areaWidthInGrids;
        int quarterHexagonHeightInPixels = layoutHeightInPixels / layoutHeightInGrids;
        halfHexagonHeightInPixels = 2 * quarterHexagonHeightInPixels;

        // MONITOR ROW size set here - a quoter of a hexagon is needed (and a little bit more, this is the extra 20%)
        monitorSize = (softBoardData.monitorRow ? halfHexagonHeightInPixels * monitorSizePermil / 1000 : 0);

        // Area is one hexagon wider, then layout width
        layoutXOffset -= halfHexagonWidthInPixels;
        layoutWidthInPixels = areaWidthInPixels + 2* halfHexagonWidthInPixels;

        layoutYOffset = - softBoardData.hideTop * quarterHexagonHeightInPixels;
        areaHeightInPixels = layoutHeightInPixels
                - softBoardData.hideTop * quarterHexagonHeightInPixels
                - softBoardData.hideBottom * quarterHexagonHeightInPixels
                + monitorSize;

        // CALCULATE FONT PARAMETERS
        fontData = TitleDescriptor.calculateTextSize(this);

        // Monitor text size can be calculated only after general text size
        monitorText.setTextSize(fontData.textSize * monitorSizePermil / 1000);
        }


    /**
     * Data calculated from screen size and preferences is invalidated.
     * Stored pictures could be deleted, too.
     * This method should be called if preferences are changed.
     * Screen changes do not need this method, those changes are followed by calculateScreenData.
     * @param erasePictures
     */
    public void invalidateCalculations( boolean erasePictures )
        {
        screenWidthInPixels = -1;

        if ( erasePictures )
            {
            layoutMap = null;
            layoutPicture = null;
            }
        }


    /**
     ** VALIDATIONS
     **/

    /**
     * True if a layout can be created with these parameters.
     * This is checked before creating layout
     */
    public static boolean isValidDimension(
            int layoutWidthInHexagons, int layoutHeightInHexagons)
        {
        if (layoutWidthInHexagons < 1 || layoutWidthInHexagons > MAX_LAYOUT_WIDTH_IN_HEXAGONS)
            return false;
        if (layoutHeightInHexagons < 1 || layoutHeightInHexagons > MAX_LAYOUT_HEIGHT_IN_HEXAGONS)
            return false;
        if (layoutWidthInHexagons * layoutHeightInHexagons > MAX_BUTTONS)
            return false;

        return true;
        }

    public static final int POSITION_INVALID = -2;
    public static final int POSITION_LINE_ENDED = -1;
    public static final int POSITION_HALF_HEXAGON = POSITION_LINE_ENDED + 1;
    public static final int POSITION_WHOLE_HEXAGON = POSITION_HALF_HEXAGON + 1;
    public static final int POSITION_OCCUPIED = 0x10;
    public static final int POSITION_HALF_OCCUPIED = POSITION_HALF_HEXAGON | POSITION_OCCUPIED;
    public static final int POSITION_WHOLE_OCCUPIED = POSITION_WHOLE_HEXAGON | POSITION_OCCUPIED;

    public int checkPosition( int arrayColumn, int arrayRow )
        {
        if ( arrayRow >= 0 && arrayRow < layoutHeightInHexagons)
            {
            // row is valid
            if ( arrayColumn > 0 && arrayColumn < layoutWidthInHexagons -1 )
                {
                // rows, (cental) columns are valid, cell is whole
                return POSITION_WHOLE_HEXAGON;
                }
            if ( arrayColumn == 0 )
                {
                // first (0) column check
                return (arrayRow % 2 == rowsAlignOffset) ? POSITION_HALF_HEXAGON : POSITION_WHOLE_HEXAGON;
                // array-odd (1) == array-evens start with whole (1)
                // array-evens (0) != array-evens start with whole (1)
                }
            if ( arrayColumn == layoutWidthInHexagons -1 )
                {
                // last column check
                return  (areaWidthInGrids % 2) + ((arrayRow % 2 == rowsAlignOffset ) ? POSITION_HALF_HEXAGON : POSITION_LINE_ENDED);
                //  if ( areaWidthInGrids % 2 == 1 )
                //    return (arrayColumn % 2 == rowsAlignOffset ) ? POSITION_WHOLE_HEXAGON : POSITION_HALF_HEXAGON;...
                }
            if ( arrayColumn >= layoutWidthInHexagons)
                return POSITION_LINE_ENDED;
            }
        // row or column is invalid
        return POSITION_INVALID;
        }

    public int checkButton( int arrayColumn, int arrayRow )
        {
        int result = checkPosition( arrayColumn, arrayRow );

        if ( result > POSITION_LINE_ENDED &&
                buttons[touchCodeFromPosition(arrayColumn, arrayRow)] != null )
            {
            result |= POSITION_OCCUPIED;
            }

        return result;
        }


    /**
     ** CALCULATE TOUCH CODES
     **/

    /**
     * Calculates touchCode from the hexagonal position.
     * Important, that the touchCodes should be identical in all levels and in the map.
     * TouchCodes are the indexes of button[] in LayoutDescription.
     *
     * @param hexagonCol column of the button (x coord in hexagons)
     * @param hexagonRow row of the button (y coord in hexagons)
     * @return touchCode of the button
     */
    public int touchCodeFromPosition(int hexagonCol, int hexagonRow)
        {
        return hexagonRow * layoutWidthInHexagons + hexagonCol + 1;
        }

    // !! isValid(touchCode, layout) nem kéne???

    public static int colorFromTouchCode(int touchCode, boolean outerRim)
        {
        // TouchCode 5 + 5 bit > R 5 bit (G) B 5 bit
        // R byte : 5bit << 3 + 5
        // B byte : 5bit << 3 + 5

        int red = ((touchCode & 0x3E0) << 14) + 0x50000; // >> 5 << 3 << 16 + 5 << 16;

        int green = outerRim ? 0 : 0xFF00;

        int blue = ((touchCode & 0x1F) << 3) + 5;

        return 0xFF000000 | red | green | blue;
        }

    public static int touchCodeFromColor(int color)
        {
        // 5 bit : byte >> 3
        // R >> 16 >> 3 << 5
        // B >> 3

        return ((color & 0xF80000) >> 14) | ((color & 0xF8) >> 3);
        }

    public static boolean outerRimFromColor(int color)
        {
        return (color & 0xFF00) != 0;
        }

/*	public int touchCodeFromMap( int canvasX, int canvasY )
		{
		int mapX = canvasX - xOffset;

		if ( mapX < 0 || mapX >= getLayoutMap().getWidth() )
			return Layout.EMPTY_TOUCH_CODE;

		if ( canvasY < 0 || canvasY >= getLayoutMap().getHeight() )
			return Layout.EMPTY_TOUCH_CODE;

		int color = getLayoutMap().getPixel( mapX, canvasY );

		return Layout.touchCodeFromColor(color);
		}
*/

    public int colorFromMap(int canvasX, int canvasY)
        {
        int mapX = canvasX - layoutXOffset;
        int mapY = canvasY - layoutYOffset;

        if (mapX < 0 || mapX >= getLayoutMap().getWidth())
            return 0xFFFD00FD; // Empty color code generates Layout.EMPTY_TOUCH_CODE;

        if (mapY < 0)
            return 0xFFFD00FD; // Empty color code generates Layout.EMPTY_TOUCH_CODE;

        if (mapY >= getLayoutMap().getHeight())
            return colorFromTouchCode(0, false);

        return getLayoutMap().getPixel( mapX, mapY );
        }


    /**
     * * CREATE LAYOUT MAP
     */

    private void createLayoutMap()
        {
        Scribe.debug( Debug.LAYOUT, "Layout Map is created - W: " + layoutWidthInPixels + " H: " + layoutHeightInPixels);

        layoutMap = Bitmap.createBitmap(layoutWidthInPixels, layoutHeightInPixels, Bitmap.Config.RGB_565);
        layoutMap.eraseColor(colorFromTouchCode(EMPTY_TOUCH_CODE, false));

        Canvas canvas = new Canvas(layoutMap);

        // Cannot be created before setting screen specific data
        ButtonForMaps buttonForMaps = new ButtonForMaps(this);

        // hexagon rows
        for (int row = 0; row < layoutHeightInHexagons; row++)
            {
            // hexagon columns
            for (int col = 0; col < layoutWidthInHexagons; col++)
                {
                buttonForMaps.drawButtonForMap(canvas, col, row);
                }
            }
        }

    public Bitmap getLayoutMap()
        {
        if ( layoutMap == null)
            createLayoutMap();
        return layoutMap;
        }

    // Just for debugging purposes
    public void drawLayoutMap(Canvas canvas)
        {
        canvas.drawBitmap( getLayoutMap(), (float) layoutXOffset, (float) layoutYOffset, null);
        }


    /**
     * * CREATE LAYOUT LAYOUT
     */

    public void drawLayoutPicture(Canvas canvas)
        {
        canvas.drawBitmap( getLayoutPicture(), (float) layoutXOffset, (float) layoutYOffset, null);
        }

    public void drawChangedButtons(Canvas canvas)
        {
        // ChangedButtons - draw over the bitmap, too
        for (Button changingButton : changingButtons)
            {
            changingButton.drawButtonChangingPart(canvas);
            }
        }
        
    /*
     * Not all layout can be stored as bitmap because of memory problems.
     * Now only one layout is cached in layoutPicture.
     * The process could be quicker, if bitmaps (same size!) could be reused.
     * Now a new bitmap will be generated for every bitmap.
     */
    public Bitmap getLayoutPicture()
        {
        if ( layoutPicture != null)
            {
            return layoutPicture;
            }

        Scribe.debug( Debug.LAYOUT, "Layout skin is created for " + toString());

        layoutPicture = createLayoutPicture();

        return layoutPicture;
        }

    private Bitmap createLayoutPicture()
        {
        Scribe.debug( Debug.LAYOUT, "Layout Layout is created - W: " + layoutWidthInPixels + " H: " + layoutHeightInPixels);

        Bitmap skin = null;
        if ( picture != null )
            {
            /*
            // This will only crop the centre of the image
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(picture.getAbsolutePath(), bitmapOptions);
            skin = ThumbnailUtils.extractThumbnail(bitmap, layoutWidthInPixels, layoutHeightInPixels, ThumbnailUtils.OPTIONS_RECYCLE_INPUT );
            */
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeFile( picture.getAbsolutePath(), bitmapOptions );
            if ( bitmap != null )
                {
                skin = Bitmap.createScaledBitmap(bitmap, layoutWidthInPixels, layoutHeightInPixels, true);
                //bitmap.recycle(); skin and bitmap can be the same. recycle is not needed per documentation
                }
            else
                {
                Scribe.error( Debug.LAYOUT, "Cannot decode picture from: " + picture.getAbsolutePath());
                }
            }

        if ( skin == null )
            {
            skin = Bitmap.createBitmap(layoutWidthInPixels, layoutHeightInPixels, Bitmap.Config.RGB_565);
            skin.eraseColor(layoutColor);
            }

        Canvas canvas = new Canvas(skin);

        for (Button button : buttons)
            {
            if (button != null)
                {
                button.onLayoutReady();
                button.drawButtonConstantPart(canvas);
                }
            }

        if ( softBoardData.gridTitle)
            {
            // Cannot be created before setting screen specific data
            Button buttonForGridTitle = new Button();

            // hexagon rows
            for (int row = 0; row < layoutHeightInHexagons; row++)
                {
                // hexagon columns
                for (int col = 0; col < layoutWidthInHexagons; col++)
                    {
                    buttonForGridTitle.drawGridTitle(canvas, this, col, row);
                    }
                }
            }

        return skin;
        }


    public void setMonitorTypeface( Typeface typeface )
        {
        monitorText.setTypeface(typeface);
        }


    public void setMonitorText( int size, boolean bold, boolean italics, int color )
        {
        // What about typeface? It should be set for all layouts OR
        // general (static) paint should be used for all layouts (as in title-descriptor
        monitorSizePermil = size;
        // actual size should be set in calculateScreenData

        monitorText.setColor(color);
        monitorText.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
                Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG |
                (bold ? Paint.FAKE_BOLD_TEXT_FLAG : 0));
        monitorText.setTextSkewX(italics ? -0.25f : 0);
        }


    public void drawMonitorRow( Canvas canvas )
        {
        if ( softBoardData.monitorRow )
            {
            canvas.drawRect(
                    layoutXOffset,
                    areaHeightInPixels - monitorSize,
                    layoutXOffset + layoutWidthInPixels,
                    areaHeightInPixels,
                    monitorBackground);

            // takes text from softBoardData.monitorText
            canvas.drawText( softBoardData.getMonitorString(),
                    layoutXOffset + halfHexagonWidthInPixels + halfHexagonWidthInPixels / 5, // +20% as well
                    //(float)(layout.layoutHeightInPixels + layout.halfHexagonHeightInPixels),
                    // layout.layoutHeightInPixels - probe.descent(),
                    // layout.layoutHeightInPixels - probe.ascent(),
                    areaHeightInPixels - monitorText.descent(),
                    monitorText);
            }

        }


    // id is needed only for debug
    private long layoutId;
    
    // set id could be part of the constructor
    public void setLayoutId(long layoutId)
        {
        this.layoutId = layoutId;
        Scribe.debug( Debug.LAYOUT, "Layout is created: " + toString());
        }
    
    // toString is needed only for debugging
    @Override
    public String toString()
        {
        StringBuilder result = new StringBuilder();
        result.append("Layout ").append( Tokenizer.regenerateKeyword(layoutId));
        result.append(" - C:").append(layoutWidthInHexagons);
        result.append("/R:").append(layoutHeightInHexagons);
        result.append("/A:").append(rowsAlignOffset);
        return result.toString();
        }

    }

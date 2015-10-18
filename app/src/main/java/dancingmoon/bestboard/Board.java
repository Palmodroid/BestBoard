package dancingmoon.bestboard;


import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;

import dancingmoon.bestboard.buttons.Button;
import dancingmoon.bestboard.buttons.ButtonForMaps;
import dancingmoon.bestboard.buttons.TitleDescriptor;
import dancingmoon.bestboard.scribe.Scribe;
import dancingmoon.bestboard.states.BoardStates;
import dancingmoon.bestboard.states.MetaState;
import dancingmoon.bestboard.utils.ExternalDataException;
import dancingmoon.bestboard.utils.Trilean;

public class Board
    {
    /**
     ** PREFERENCES - settings per device
     **/

    /**
     * Size of the outer rim on buttons.
     * Touch movement (stroke) will not fire from the outer rim, but touch down will do.
     */
    public int outerRimPercent = 30;

    /**
     * Hide grids from the top of the board - VALUE IS NOT VERIFIED!
     * 0 - no hide
     * 1 - hide one quater (one grid) from the top row
     * 2 - hide one half (two grids) from the top row
     */
    public int hideTop = 0;

    /**
     * Hide grids from the bottom of the board - VALUE IS NOT VERIFIED!
     * 0 - no hide
     * 1 - hide one quater (one grid) from the bottom row
     * 2 - hide one half (two grids) from the bottom row
     */
    public int hideBottom = 0;

    /**
     * Offset for non-wide boards in landscape mode
     * (percent of the free area) - VALUE IS NOT VERIFIED!
     */
    public int landscapeOffsetPercent = 30;
        
    /**
     * Background of the touched key is changed or not
     */
    public boolean displayTouch = true;

    /**
      * Stroke is displayed or not
      */
    public boolean displayStroke = true;


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
    public static final int MAX_BOARD_WIDTH_IN_HEXAGONS = 48;

    /**
     * Maximal layout height in hexagons
     */
    public static final int MAX_BOARD_HEIGHT_IN_HEXAGONS = 24;

    /**
     * Data stored in softBoardData is needed for each board.
     */
    public SoftBoardData softBoardData;

    /**
     * number of keys (full hexagons) in one row of the board
     * This is the number of the STORED keys, and NOT the displayed hexagons
     */
    public int boardWidthInHexagons;

    /**
     * number of hexagon rows
     * This is the number of the STORED rows, and NOT the displayed hexagons
     */
    public int boardHeightInHexagons;

    /**
     * displayed half hexagons in one row (this value is used for calculations)
     */
    public int boardWidthInGrids;

    /**
     * displayed quarter hexagon rows (this value is used for calculations)
     */
    public int boardHeightInGrids;

    /**
     * 1 if first (0) row starts with WHOLE button on the left
     * 0 if first (0) row starts with HALF button on the left
     */
    public int rowsAlignOffset;

    /**
     * true if board is optimised for landscape. Portrait (false) boards can be used in both modes
     */
    public boolean wide = false;

    /**
     * background color of the board
     */
    public int boardColor;

    /**
     * meta-states to force
     */
    private Trilean[] metaStates;

    /**
     * Needed by TitleDescriptor to change titles to uppercase if CAPS is forced
     * @return true if caps lock is forced by the board
     */
    public boolean isCapsForced()
        {
        return metaStates[BoardStates.META_CAPS].isTrue();
        }

    /**
     * Buttons of the board - will be initialized in constructor, and filled up by addButton
     */
    public Button[] buttons;

    /**
     * Changeable buttons of the board - filled up by addButton
     */
    public ArrayList<Button.ChangingButton> changingButtons = new ArrayList<>();


    /**
     ** CLASS VARIABLES - CALCULATED FROM SCREEN SPECIFIC DATA
     **/

    /**
     * screen width is stored to check whether new measurement is needed
     * in setScreenData
     */
    public int screenWidthInPixels = -1;

    /**
     * board width in pixels (equals to screen's lower diameter for non-wide boards)
     */
    public int boardWidthInPixels;

    /**
     * board height in pixels (calculated from width)
     */
    public int boardHeightInPixels;

    /**
     * offset in landscape if board is not wide
     */
    public int xOffset = 0;

    /**
     * Text with this size:
     * 5 chars can fit in one hexagon width AND text can fit in one grid height
     */
    public int textSize;
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
    private Bitmap layoutSkin = null;


    /**
     ** CONSTRUCTION OF THE BOARD
     ** Parsing phase:
     **   1. Constructor - adds non screen-specific data
     **      setForcedMeta() - if meta-states should be forced
     **   2. addButton - populates the buttons array and
     **      setShift - sets the shift levels by descriptor file
     ** Displaying phase:
     **   3. onMeasure - receives screen diameters
     **   4. setScreenData - screen specific information set by onMeasure
     **/

    /**
     * Constructor needs data to generate a keyboard with button-holes.
     * It needs information about the measures of the board.
     * All specific data (buttons etc.) will be added later.
     * Screen information is needed to draw the board.
     * It will added in the setScreenData() method.
     *
     * @param data           softBoardData for all boards
     * @param halfcolumns    width in half hexagons (grids)
     * @param rows           height in full hexagons
     * @param oddRowsAligned first row (0) starts with whole button on the left
     * @param wide           board is optimised for wide (landscape) screen (cannot be used in portrait)
     * @param color          background color
     * @param metaStates     forced metastates
     * @throws IllegalArgumentException if board cannot be created with this dimension
     *                                  Dimension can be checked previously with isValidDimension()
     */
    public Board( SoftBoardData data, int halfcolumns, int rows,
                 boolean oddRowsAligned, boolean wide, int color,
                 Trilean[] metaStates ) throws ExternalDataException
        {
        // NON SCREEN-SPECIFIC DATA

        // first two hidden, "side" halfcolumns are added, then full columns are calculated
        // even halfcolumns: last button can be in hidden position,
        // but starting with a half button it will appear
        this.boardWidthInHexagons = (halfcolumns + 2) / 2;
        this.boardHeightInHexagons = rows;

        if (!isValidDimension(boardWidthInHexagons, boardHeightInHexagons))
            {
            throw new ExternalDataException("Board cannot be created with these arguments!");
            }

        // Each row has one more half hexagon column
        this.boardWidthInGrids = halfcolumns;
        // Each row has three quarters of hexagonal height (3 grids) + 1 for the last row
        // hideTop and hideBottom can "hide" 1 or 2 grids
        this.boardHeightInGrids = rows * 3 + 1 - hideTop - hideBottom;

        // Common data is needed
        this.softBoardData = data;

        this.rowsAlignOffset = oddRowsAligned ? 1 : 0;

        this.wide = wide;

        this.boardColor = color;

        // INITIALIZE BUTTONS' ARRAY
        // this two-dimensional array will be populated later
        // null: non-defined (empty) button
        buttons = new Button[boardWidthInHexagons * boardHeightInHexagons];
        // ADDBUTTON will fill up this array

        // SETSCREENDATA is needed for screen-specific information

        this.metaStates = metaStates;
        }


    /**
     * Force meta-states, as defined by setForcedMeta()
     * This method is called when board is chosen.
     */
    public void forceMetaStates()
        {
        for (int m = 0; m < metaStates.length; m++)
            {
            if ( metaStates[m].isTrue() )
                softBoardData.boardStates.metaStates[m].setState( MetaState.META_LOCK );
            else if ( metaStates[m].isFalse() )
                softBoardData.boardStates.metaStates[m].setState( MetaState.META_OFF );
            // IGNORE -> no change
            }
        }


    /**
     * Predefined button is added at the defined position.
     * Board and position infos are added previously.
     *
     * @param column column (in hexagons) of the button
     * @param row    row (in hexagons) of the button
     * @param button predefined button instance (board, positions are not needed)
     * @return true if button overwrites an other button
     * @throws ExternalDataException If button position is not valid
     */
    public boolean addButton(int column, int row, Button button) throws ExternalDataException
        {
        // BUTTONS ARE COMING FROM COAT DESCRIPTOR FILE
        // Scribe.locus();

        if (!isValidPosition(column, row))
            {
            throw new ExternalDataException("This button position is not valid! Button cannot be added!");
            }

        boolean ret = false;

        button.setPosition(this, column, row);

        // put in its position
        int index = touchCodeFromPosition(column, row);

        // check whether this is empty position !! changedButtons list is not checked if button is overwritten !!
        if (buttons[index] != null)
            ret = true;

        // put button to its place
        buttons[index] = button;

        // if button is a changedButton, then store it in changedButtons list as well
        if (button instanceof Button.ChangingButton)
            changingButtons.add((Button.ChangingButton) button);

        return ret;
        }


    /**
     * Calculates board dimensions from screen specific data:
     * - xOffset, boardWidthInPixels, boardHeightInPixels
     * - halfHexagonWidthInPixels, halfHexagonWidthInPixels
     * - textSize
     * It is called by BoardView.onMeasure() when screen (width) is changed or
     * board is changed. Recalculation is needed only, when ScreenWidthInPixels changed.
     *
     * @param screenWidthInPixels  screen width
     * @param screenHeightInPixels screen height
     */
    public void setScreenData(int screenWidthInPixels, int screenHeightInPixels)
        {
        Scribe.locus();
        
        // setScreenData is needed only, if orientation was changed
        if ( screenWidthInPixels == this.screenWidthInPixels )
            return;           
        this.screenWidthInPixels = screenWidthInPixels;

        // GENERATE SCREEN SPECIFIC VALUES
        boolean landscape = (screenWidthInPixels > screenHeightInPixels);
        
        // orientation can be found in UseState also
        if ( landscape != softBoardData.linkState.isLandscape() )
            Scribe.error("Orientation in onMeasure and in usetate is not the same!");

        // temporary variables are needed to check whether board dimension is changed
        int newBoardWidthInPixels;
        int newBoardHeightInPixels;

        // Calculate BoardWidth and Offset

        // WIDE board for LANDSCAPE mode OR NORMAL board for PORTRAIT mode
        if (wide == landscape)
            {
            newBoardWidthInPixels = screenWidthInPixels;
            this.xOffset = 0;
            Scribe.debug("Full width keyboard");
            }
        // NORMAL board for LANDSCAPE mode - change values
        else if (!wide) // && landscape)
            {
            // noinspection SuspiciousNameCombination
            newBoardWidthInPixels = screenHeightInPixels; // This is the shorter diameter
            this.xOffset = (screenWidthInPixels - newBoardWidthInPixels) *
                    landscapeOffsetPercent / 100;
            Scribe.debug("Normal keyboard for landcape. Offset:" + xOffset);
            }
        // LANDSCAPE board PORTRAIT mode - incompatible board! - !! NOW WE LET IT WORK (TESTING!!) !!
        else // if (wide && !landscape)
            {
            newBoardWidthInPixels = screenWidthInPixels; // Board will be distorted!!
            this.xOffset = 0;
            Scribe.debug("Wide keyboard for portrait! NOT POSSIBLE! Keyboard is distorted.");
            }

        // Calculate BoardHeight

        // Board pixel height is calculated with the ratio of a regular hexagon
        // after this point all the measurements are calculated from pixelHeight backwards
        newBoardHeightInPixels = ((boardHeightInGrids * newBoardWidthInPixels * 1000) /
                (boardWidthInGrids * 1732));

        // Only 3/4 of the real height can be occupied
        // this true even for NORMAL board: the actual height (== width of the board) should be calculated
        screenHeightInPixels *= 3;
        screenHeightInPixels /= 4;

        // If board height exceeds maximal value, board will be distorted
        if (newBoardHeightInPixels > screenHeightInPixels)
            newBoardHeightInPixels = screenHeightInPixels;

        // Check if board dimensions are changed
        if (newBoardWidthInPixels != boardWidthInPixels ||
                newBoardHeightInPixels != boardHeightInPixels)
            {
            boardWidthInPixels = newBoardWidthInPixels;
            boardHeightInPixels = newBoardHeightInPixels;

            // release layout picture
            layoutMap = null;
            layoutSkin = null;
            }

        // CALCULATE FONT PARAMETERS
        textSize = TitleDescriptor.calculateTextSize(this);

        halfHexagonHeightInPixels = 2 * boardHeightInPixels / boardHeightInGrids;
        halfHexagonWidthInPixels = screenWidthInPixels / boardWidthInGrids;
        }


    /**
     ** VALIDATIONS
     **/

    /**
     * True if a board can be created with these parameters.
     * This is checked before creating board
     */
    public static boolean isValidDimension(
            int boardWidthInHexagons, int boardHeightInHexagons)
        {
        if (boardWidthInHexagons < 1 || boardWidthInHexagons > MAX_BOARD_WIDTH_IN_HEXAGONS)
            return false;
        if (boardHeightInHexagons < 1 || boardHeightInHexagons > MAX_BOARD_HEIGHT_IN_HEXAGONS)
            return false;
        if (boardWidthInHexagons * boardHeightInHexagons > MAX_BUTTONS)
            return false;

        return true;
        }

    /**
     * True if this hexagonal position is a valid on this board
     * This is checked before adding a button
     */
    public boolean isValidPosition(int column, int row)
        {
        if (row < 0 || row >= boardHeightInHexagons)
            return false;
        if (column < 0 || column >= boardWidthInHexagons)
            return false;
        return true;
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
        return hexagonRow * boardWidthInHexagons + hexagonCol;
        }

    // !! isValid(touchCode, layout) nem kéne???

    public int colorFromTouchCode(int touchCode, boolean outerRim)
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

		if ( mapX < 0 || mapX >= getMap().getWidth() )
			return Board.EMPTY_TOUCH_CODE;

		if ( canvasY < 0 || canvasY >= getMap().getHeight() )
			return Board.EMPTY_TOUCH_CODE;

		int color = getMap().getPixel( mapX, canvasY );

		return Board.touchCodeFromColor(color);
		}
*/

    public int colorFromMap(int canvasX, int canvasY)
        {
        int mapX = canvasX - xOffset;

        if (mapX < 0 || mapX >= getMap().getWidth())
            return Board.EMPTY_TOUCH_CODE;

        if (canvasY < 0 || canvasY >= getMap().getHeight())
            return Board.EMPTY_TOUCH_CODE;

        int color = getMap().getPixel(mapX, canvasY);

        return color;
        }


    /**
     * * CREATE BOARD MAP
     */

    private void createMap()
        {
        layoutMap = Bitmap.createBitmap(boardWidthInPixels, boardHeightInPixels, Bitmap.Config.RGB_565);
        layoutMap.eraseColor(colorFromTouchCode(EMPTY_TOUCH_CODE, false));

        Canvas canvas = new Canvas(layoutMap);

        // Cannot be created before setting screen specific data
        ButtonForMaps buttonForMaps = new ButtonForMaps(this);

        // hatszög "sorok"
        for (int row = 0; row < boardHeightInHexagons; row++)
            {
            // hatszög oszlopok
            for (int col = 0; col < boardWidthInHexagons; col++)
                {
                buttonForMaps.drawButtonForMap(canvas, col, row);
                }
            }
        }

    public Bitmap getMap()
        {
        if (layoutMap == null)
            createMap();
        return layoutMap;
        }

    // Just for debugging purposes
    public void drawMap(Canvas canvas)
        {
        canvas.drawBitmap(getMap(), (float) xOffset, 0f, null);
        }


    /**
     * * CREATE LAYOUT SKIN
     */

    public void drawLayoutSkin(Canvas canvas)
        {
        canvas.drawBitmap(getLayoutSkin(), (float) xOffset, 0f, null);
        }

    public void drawChangedButtons(Canvas canvas)
        {
        // ChangedButtons - draw over the bitmap, too
        for (Button.ChangingButton changingButton : changingButtons)
            {
            changingButton.drawChangingButton(canvas);
            }
        }
        
    /*
     * Not all layout can be stored as bitmap because of memory problems.
     * Now only one layout is cached in layoutSkin.
     * The process could be quicker, if bitmaps (same size!) could be reused.
     * Now a new bitmap will be generated for every bitmap.
     */
    public Bitmap getLayoutSkin()
        {
        if (layoutSkin != null)
            {
            return layoutSkin;
            }

        layoutSkin = createLayoutSkin();

        return layoutSkin;
        }

    private Bitmap createLayoutSkin()
        {
        Bitmap skin = Bitmap.createBitmap(boardWidthInPixels, boardHeightInPixels, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(skin);
        skin.eraseColor(boardColor);

        for (Button button : buttons)
            {
            if (button != null)
                {
                button.drawButton(canvas);
                }
            }

        return skin;
        }

    public static String StringPrelude(String string, int prelude)
        {
        if (string == null)
            return null;
        if (prelude >= string.length())
            return string;
        if (prelude <= 0)
            return "";
        return string.substring(0, prelude);
        }

    // id is needed only for debug
    private long boardId;    
    
    // set id could be part of the constructor
    public void setBoardId( long boardId )
        {
        this.boardId = boardId;
        }
    
    // toString is needed only for debuging
    @Override
    public String toString()
        {
        StringBuilder result = new StringBuilder();
        result.append("Board ").append( SoftBoardParser.regenerateKeyword( boardId ));
        result.append(" - C:").append(boardWidthInHexagons);
        result.append("/R:").append(boardHeightInHexagons);
        return result.toString();
        }

    }
package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import dancingmoon.bestboard.Layout;

/**
 * Describes titles (formatted strings) on buttons.
 * Titles do not change (after creation), and can connect to different buttons
 */
public class TitleDescriptor
    {
    /**
     ** COMMON SETTINGS FOR ALL TITLES
     **/

    /** Paint for text, set before drawing each title */
    private static Paint textPaint = new Paint();

    /** Common paint settings for all titles */
    static
        {
        textPaint.setTextAlign( Paint.Align.CENTER );
        }

    /**
     * Typeface is set by coat descriptor file
     * @param typeface Common font for all titles
     */
    public static void setTypeface( Typeface typeface )
        {
        textPaint.setTypeface( typeface );
        }

    /**
     * Calculates text size for each layout.
     * All TitleDescriptor instances use the same paint and the same typeface.
     * Text size is different for each layout (and is stored in layout's data)
     * At least 4 "ly"-s (vertically) and 5 "M"-s (horizontally) drawn with this size
     * can be placed inside one hexagon.
     * @param layout calculate text size for this layout
     * @return text size for this layout
     */
    public static int calculateTextSize( Layout layout)
        {
        Rect bounds = new Rect();

        // Typeface was set previously by SoftBoardData

        textPaint.setTextSize( 1000f );

        textPaint.getTextBounds("MMMMM", 0, 5, bounds);
        int textWidth = bounds.width();

        textPaint.getTextBounds("ly", 0, 2, bounds);
        int textHeight = bounds.height();

        // Calculate font size from the height of "ly" characters
        // intendedHeightInPixels is one grid (1/4 hexagon) == layoutHeightInPixels / layoutHeightInGrids
        // Ratio => SIZE : intendedHeightInPixels = 1000f : bounds.height()
        int textSizeFromHeight = 1000 * layout.layoutHeightInPixels / (layout.layoutHeightInGrids * textHeight) ;

        // Calculate font size from the width of "MMMMM" characters
        // intendedWidthInPixels is one hexagon (2 grids) == 2 * areaWidthInPixels / areaWidthInGrids
        // Ratio => SIZE : intendedWidthInPixels = 1000f : bounds.width()
        int textSizeFromWidth = 2 * 1000 * layout.areaWidthInPixels / (layout.areaWidthInGrids * textWidth);

        // In most cases textSizeFromWidth is smaller
        // Now text with max. 5 characters can fit in the width of a hexagon
        // AND can fit in one grid (1/4 hexagon) height
        return  Math.min(textSizeFromWidth, textSizeFromHeight);
        }


    /**
     ** TITLE SPECIFIC SETTINGS
     **/

    private String text; // !! getters and setters needed !!

    public String getText()
        {
        return text;
        }

    public void checkText( String defaultText )
        {
        if (text == null)
            text = defaultText;
        }

    private int xOffset;
    private int yOffset;
    private int size;
    private boolean bold;
    private boolean italics;
    private int color;

    public TitleDescriptor(String text, int xOffset, int yOffset, int size,
                           boolean bold, boolean italics, int color)
        {
        this.text = text;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.size = size;
        this.bold = bold;
        this.italics = italics;
        this.color = color;
        }

    // !! Cloneable could be used here, but it has some problems !!
    public TitleDescriptor copy()
        {
        return new TitleDescriptor( text, xOffset, yOffset, size, bold, italics, color);
        }

    /**
     * Draws the stored title on the button.
     * Title can be attached to several buttons, so position data is given as parameter.
     * Because button's background is drawn first, the buttons calculated position can be used.
     * (Repeated calculation is not needed.)
     * Title text will be uppercase if capslock is forced by the layout
     * @param canvas to draw on
     * @param layout provides screen specific information (text and hexagon size)
     * @param centerX coord. in pixels (offset included)
     * @param centerY coord. in pixels
     */
    public void drawTitle( Canvas canvas, Layout layout, int centerX, int centerY )
        {
        drawTitle( canvas, layout, text, centerX, centerY );
        }

    /**
     * Draws an external title on the button.
     * @param canvas to draw on
     * @param layout provides screen specific information (text and hexagon size)
     * @param text external text to show as title
     * @param centerX coord. in pixels (offset included)
     * @param centerY coord. in pixels
     */
    public void drawTitle( Canvas canvas, Layout layout, String text, int centerX, int centerY )
        {
        textPaint.setTextSize(layout.textSize * size / 1000);
        textPaint.setColor(color);

        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
                Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG |
                (bold ? Paint.FAKE_BOLD_TEXT_FLAG : 0));

        textPaint.setTextSkewX(italics ? -0.25f : 0);

        canvas.drawText(
                layout.isCapsForced() ? text.toUpperCase( layout.softBoardData.locale ) : text,
                centerX + xOffset * layout.halfHexagonWidthInPixels / 1000,
                centerY + yOffset * layout.halfHexagonHeightInPixels / 1000,
                textPaint);
        }

    }
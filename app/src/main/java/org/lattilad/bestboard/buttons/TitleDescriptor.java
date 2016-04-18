package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import org.lattilad.bestboard.Layout;
import org.lattilad.bestboard.scribe.*;

/**
 * Describes titles (formatted strings) on buttons.
 * Titles do not change (after creation), and can connect to different buttons
 */
public class TitleDescriptor
    {
    /**
     ** COMMON SETTINGS FOR ALL TITLES
     **/

    /**
     * Paint for text, set before drawing each title
     * Button can use it as well, because data is written before each draw
     */
    static public Paint textPaint = new Paint();

    /** Common paint settings for all titles */
    static
        {
        textPaint.setTextAlign( Paint.Align.CENTER );
        }

    /**
     * Typeface is set by coat descriptor file
     * @param typeface Common font for all titles
     */
    static public void setTypeface( Typeface typeface )
        {
        textPaint.setTypeface( typeface );
        }

    static public class FontData
        {
        public int textSize;
        public int yAdjust;
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
    public static FontData calculateTextSize( Layout layout)
        {
        Rect bounds = new Rect();
        FontData fontData = new FontData();

        // Typeface was set previously by SoftBoardData

        textPaint.setTextSize(1000f);

        textPaint.getTextBounds("y", 0, 1, bounds);
        int textDescents = 2 * bounds.bottom;

        textPaint.getTextBounds("MMM", 0, 3, bounds);

        Scribe.debug( "1000 size MMM width: " + bounds.width() +
                 " height: " + bounds.height() +
                 " bounds: " + bounds +
                 " y 2*descent: " + textDescents + " pixels");

        // TEXT SIZE for 1000 virtual points

        // Calculate font size from the height of "MMM" and descent of "y" characters
        // (Descent is needed twice, because center is the middle of the "MMM"-s)
        // intendedHeightInPixels is half hexagon == 2 * layoutHeightInPixels / layoutHeightInGrids
        // Ratio => SIZE : intendedHeightInPixels = 1000f : (bounds.height() + textDescents)
        int textSizeFromHeight = 2 * 1000 * layout.layoutHeightInPixels /
                (layout.layoutHeightInGrids * (bounds.height() + textDescents)) ;

        // Calculate font size from the width of "MMM" characters
        // intendedWidthInPixels is one hexagon (2 grids) == 2 * areaWidthInPixels / areaWidthInGrids
        // Ratio => SIZE : intendedWidthInPixels = 1000f : bounds.width()
        int textSizeFromWidth = 2 * 1000 * layout.areaWidthInPixels /
                (layout.areaWidthInGrids * bounds.width());

        // Scribe.debug( "Size for 1000 points from width: " + textSizeFromWidth +
        //         " from height: " + textSizeFromHeight );

        // In most cases textSizeFromWidth is smaller
        // Now text with max. 3 characters can fit in the width of a hexagon
        // AND can fit in one half hexagon height
        fontData.textSize = Math.min(textSizeFromWidth, textSizeFromHeight);

        fontData.yAdjust = bounds.height() * fontData.textSize / 2000;

        // Scribe.debug( "Y adjust for 1000 points from height: " + fontData.yAdjust );

        return fontData;
        // Actually the "normal" text size is 600: 5M/hexagon width and 3Ly/hexagon height (1800)
        }


    /**
     ** TITLE SPECIFIC SETTINGS
     **/

    public static final int TEXT = 0;
    public static final int GET_FIRST_STRING = -1;
    public static final int GET_SECOND_STRING = -2;
    public static final int BUTTON_DECIDES = -3;

    private int type;

    public int getType()
        {
        return type;
        }

    public void setType( int type )
        {
        this.type = type;
        }

    public boolean isShowTitle()
        {
        return type > TEXT;
        }

    private String text; // !! getters and setters needed !!

    public String getText()
        {
        return text;
        }

    public void setText(String newText)
        {
        type = 0;
        text = newText;
        }

    private int xOffset;
    private int yOffset;
    private int size;
    private boolean bold;
    private boolean italics;
    private int color;

    public TitleDescriptor( String text, int xOffset, int yOffset, int size,
                           boolean bold, boolean italics, int color )
        {
        this( 0, text, xOffset, yOffset, size, bold, italics, color);
        }

    public TitleDescriptor( int type, int xOffset, int yOffset, int size,
                           boolean bold, boolean italics, int color )
        {
        this( type, null, xOffset, yOffset, size, bold, italics, color);
        }

    // Only for private use! type OR text should be NULL
    private TitleDescriptor(int type, String text, int xOffset, int yOffset, int size,
                           boolean bold, boolean italics, int color)
        {
        this.type = type;
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
        return new TitleDescriptor( type, text, xOffset, yOffset, size, bold, italics, color);
        }

    /**
     * Draws titles on the button. Only TEXT titles are drawn.
     * Title can be attached to several buttons, so position data is given as parameter.
     * Because button's background is drawn first, the buttons calculated position can be used.
     * (Repeated calculation is not needed.)
     * Title text will be uppercase if capslock is forced by the layout
     * @param canvas to draw on
     * @param layout provides screen specific information (text and hexagon size)
     * @param centerX coord. in pixels (offset included)
     * @param centerY coord. in pixels
     */
    public void drawTextTitle(Canvas canvas, Layout layout, int centerX, int centerY)
        {
        if ( text != null )
            drawTitle( canvas, layout, text, centerX, centerY );
        }

    public void drawShowTitle(Canvas canvas, Layout layout, int centerX, int centerY)
        {
        // if ( type > 0 ) getShowText checks for real showtitles
        drawTitle( canvas, layout,
                layout.softBoardData.softBoardShow.getShowText(type),
                centerX, centerY );
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
        textPaint.setTextSize(layout.fontData.textSize * size / 1000);
        textPaint.setColor(color);

        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
                Paint.SUBPIXEL_TEXT_FLAG | Paint.LINEAR_TEXT_FLAG |
                (bold ? Paint.FAKE_BOLD_TEXT_FLAG : 0));

        textPaint.setTextSkewX(italics ? -0.25f : 0);

        int adjust = layout.fontData.yAdjust * size / 1000;
        // Scribe.debug("Size: " + size + " Adjust: " + adjust);

        canvas.drawText(
                layout.isCapsForced() ? text.toUpperCase(layout.softBoardData.locale) : text,
                centerX + xOffset * layout.halfHexagonWidthInPixels / 1500,
                centerY + yOffset * layout.halfHexagonHeightInPixels / 1000 + adjust,
                textPaint);
        }
    }

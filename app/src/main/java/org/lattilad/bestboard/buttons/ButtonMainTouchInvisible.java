package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;

import org.lattilad.bestboard.Layout;

/**
 * Special MAIN Button without any visible parts - used for Monitor Row
 */
public abstract class ButtonMainTouchInvisible extends ButtonMainTouch
    {
    @Override
    public ButtonMainTouchInvisible clone()
        {
        return (ButtonMainTouchInvisible) super.clone();
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
        // NOT NEEDED!! setPosition(arrayColumn,arrayRow);
        connected(); // ??
        }


    /**
     * This method is called, when layout is ready, so it can be measured
     */
    public void onLayoutReady()
        {
        // NOT NEEDED!!
        /*
        xMinus = getPixelX( columnInGrids - 1 );
        xCenter = getPixelX( columnInGrids );
        xPlus = getPixelX( columnInGrids + 1 );

        yMinus2 = getPixelY( rowInGrids - 2 );
        yMinus1 = getPixelY( rowInGrids - 1 );
        yCenter = getPixelY( rowInGrids );
        yPlus1 = getPixelY( rowInGrids + 1 );
        yPlus2 = getPixelY( rowInGrids + 2 );
         */
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
    private void drawButton(Canvas canvas, int drawInfo, int color, int xOffsetInPixel, int yOffsetInPixel )
        {
        // NOT NEEDED !! - But it will be never called !
        /*

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
        */
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
        // NOT NEEDED!! drawButton( canvas, ~changingInfo, getColor(), 0, 0 );
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
        // NOT NEEDED!! drawButton( canvas, changingInfo, getColor(), layout.layoutXOffset, layout.layoutYOffset);
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
        // NOT NEEDED!! drawButton( canvas, DRAW_ALL, layout.softBoardData.touchColor, layout.layoutXOffset, layout.layoutYOffset);
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
        // NOT NEEDED !! - But it will be never called !
        /*
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
        */
        }

    }

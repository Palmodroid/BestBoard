package org.lattilad.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import org.lattilad.bestboard.Layout;

/**
 * Layout map uses only one ButtonForMaps instance.
 * It is NOT Cloneable.
 */
public class ButtonForMaps extends Button
    {
    private static Paint hexagonMapPaint = new Paint();

    static
        {
        hexagonMapPaint.setStyle(Paint.Style.FILL);
        hexagonMapPaint.setAntiAlias(false);
        hexagonMapPaint.setDither(false);
        }

    private int pixelRimQuarterHeight;
    private int pixelRimHalfWidth;

    public ButtonForMaps(Layout layout)
        {
        // layout is stored in Button superclass
        this.layout = layout;

        pixelRimQuarterHeight = (layout.layoutHeightInPixels * (1000 - layout.softBoardData.outerRimPermil)) /
                (layout.layoutHeightInGrids * 1000);
        pixelRimHalfWidth = (layout.areaWidthInPixels * (1000 - layout.softBoardData.outerRimPermil))
                / (layout.areaWidthInGrids * 1000);
        }


    private Path RimHexagonPath()
        {
        Path path = new Path();

        path.moveTo(getXCenter() , getYCenter() - 2 * pixelRimQuarterHeight );
        path.lineTo(getXCenter() + pixelRimHalfWidth, getYCenter() - pixelRimQuarterHeight );
        path.lineTo(getXCenter() + pixelRimHalfWidth, getYCenter() + pixelRimQuarterHeight );
        path.lineTo(getXCenter() , getYCenter() + 2 * pixelRimQuarterHeight );
        path.lineTo(getXCenter()  - pixelRimHalfWidth, getYCenter() + pixelRimQuarterHeight );
        path.lineTo(getXCenter()  - pixelRimHalfWidth, getYCenter() - pixelRimQuarterHeight );
        path.close();

        return path;
        }


    public void drawButtonForMap(Canvas canvas, int columnInHexagons, int rowInHexagons)
        {
        setPosition( columnInHexagons, rowInHexagons );
        onLayoutReady();
        // Layout is always ready at this time, but after setting position, coordinates should be recalculated

        hexagonMapPaint.setColor(
                layout.colorFromTouchCode(
                        layout.touchCodeFromPosition(columnInHexagons, rowInHexagons), false));
        canvas.drawPath(hexagonPath(0, 0), hexagonMapPaint);

        hexagonMapPaint.setColor(
                layout.colorFromTouchCode(
                        layout.touchCodeFromPosition(columnInHexagons, rowInHexagons), true));
        canvas.drawPath(RimHexagonPath(), hexagonMapPaint);

        // Scribe.debug("touchCode: " + touchCodeFromPosition(row, col) +
        //        " ret: " + touchCodeFromColor(layoutMap.getPixel(getPixelX(gridX, 0), getPixelY(gridY))) +
        //        " color: " + Integer.toHexString(colorFromTouchCode(touchCodeFromPosition(row, col), true)) +
        //        " r: " + row + " c: " + col);
        }

    /**
    @Override
    public String getFirstString()
        {
        throw new UnsupportedOperationException();
        }

    @Override
    public String getSecondString()
        {
        throw new UnsupportedOperationException();
        }

    @Override
    protected void drawButton( Canvas canvas, int color, int xOffsetInPixel, int yOffsetInPixel )
        {
        throw new UnsupportedOperationException();
        }
    */
    }

package dancingmoon.bestboard.buttons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import dancingmoon.bestboard.Board;

/**
 * Board map uses only one ButtonForMaps instance.
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

    public ButtonForMaps(Board board)
        {
        // board is stored in Button superclass
        this.board = board;

        pixelRimQuarterHeight = (board.boardHeightInPixels * (1000 - board.softBoardData.outerRimPermil)) /
                (board.boardHeightInGrids * 1000);
        pixelRimHalfWidth = (board.areaWidthInPixels * (1000 - board.softBoardData.outerRimPermil))
                / (board.areaWidthInGrids * 1000);
        }


    private Path RimHexagonPath()
        {
        int pixelX = getPixelX(columnInGrids, 0);
        int pixelY = getPixelY(rowInGrids, 0);
        Path path = new Path();

        path.moveTo(pixelX, pixelY - 2 * pixelRimQuarterHeight );
        path.lineTo(pixelX + pixelRimHalfWidth, pixelY - pixelRimQuarterHeight );
        path.lineTo(pixelX + pixelRimHalfWidth, pixelY + pixelRimQuarterHeight );
        path.lineTo(pixelX, pixelY + 2 * pixelRimQuarterHeight );
        path.lineTo(pixelX - pixelRimHalfWidth, pixelY + pixelRimQuarterHeight );
        path.lineTo(pixelX - pixelRimHalfWidth, pixelY - pixelRimQuarterHeight );
        path.close();

        return path;
        }


    public void drawButtonForMap(Canvas canvas, int columnInHexagons, int rowInHexagons)
        {
        this.columnInGrids = getGridX(columnInHexagons, rowInHexagons);
        this.rowInGrids = getGridY(rowInHexagons);

        hexagonMapPaint.setColor(
                board.colorFromTouchCode(
                        board.touchCodeFromPosition(columnInHexagons, rowInHexagons), false));
        canvas.drawPath(hexagonPath( 0,0 ), hexagonMapPaint);

        hexagonMapPaint.setColor(
                board.colorFromTouchCode(
                        board.touchCodeFromPosition(columnInHexagons, rowInHexagons), true));
        canvas.drawPath(RimHexagonPath(), hexagonMapPaint);

        // Scribe.debug("touchCode: " + touchCodeFromPosition(row, col) +
        //        " ret: " + touchCodeFromColor(layoutMap.getPixel(getPixelX(gridX, 0), getPixelY(gridY))) +
        //        " color: " + Integer.toHexString(colorFromTouchCode(touchCodeFromPosition(row, col), true)) +
        //        " r: " + row + " c: " + col);
        }


    @Override
    public String getString()
        {
        throw new UnsupportedOperationException();
        }


    @Override
    protected void drawButton( Canvas canvas, int color, int xOffsetInPixel, int yOffsetInPixel )
        {
        throw new UnsupportedOperationException();
        }
    }

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

    private int pixelRimQuaterHeight;
    private int pixelRimHalfWidth;

    public ButtonForMaps(Board board)
        {
        // board is stored in Button superclass
        this.board = board;

        pixelRimQuaterHeight = (board.boardHeightInPixels * (100 - board.softBoardData.outerRimPercent)) / (board.boardHeightInGrids * 100);
        pixelRimHalfWidth = (board.boardWidthInPixels * (100 - board.softBoardData.outerRimPercent)) / (board.boardWidthInGrids * 100);
        }


    private Path RimHexagonPath()
        {
        int pixelX = getPixelX(columnInGrids, 0);
        int pixelY = getPixelY(rowInGrids);
        Path path = new Path();

        path.moveTo(pixelX, pixelY - 2 * pixelRimQuaterHeight);
        path.lineTo(pixelX + pixelRimHalfWidth, pixelY - pixelRimQuaterHeight);
        path.lineTo(pixelX + pixelRimHalfWidth, pixelY + pixelRimQuaterHeight);
        path.lineTo(pixelX, pixelY + 2 * pixelRimQuaterHeight);
        path.lineTo(pixelX - pixelRimHalfWidth, pixelY + pixelRimQuaterHeight);
        path.lineTo(pixelX - pixelRimHalfWidth, pixelY - pixelRimQuaterHeight);
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
        canvas.drawPath(hexagonPath(0), hexagonMapPaint);

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
    protected void drawButton( Canvas canvas, int color, int offsetInPixel )
        {
        throw new UnsupportedOperationException();
        }
    }

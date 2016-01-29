package org.lattilad.bestboard.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class Screen
    {
    private static int rawWidth = -1;
    private static int rawHeight;

    public static int getRawHeight( Context context )
        {
        if ( rawWidth < 0 )
            calculate( context );

        return rawHeight;
        }

    public static int getRawWidth( Context context )
        {
        if ( rawWidth < 0 )
            calculate( context );

        return rawWidth;
        }

    public static int getShorterDiameter( Context context )
        {
        if ( rawWidth < 0 )
            calculate( context );

        return Math.min( rawWidth, rawHeight );
        }

    public static int getLongerDiameter( Context context )
        {
        if ( rawWidth < 0 )
            calculate( context );

        return Math.max( rawWidth, rawHeight );
        }

    private static void calculate( Context context )
        {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // Activity.getWindowManager() cannot be used in service
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        // since SDK_INT = 1;
        rawWidth = displayMetrics.widthPixels;
        rawHeight = displayMetrics.heightPixels;

        // includes window decorations (statusbar bar/menu bar)
        if ( Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17 )
            {
            try
                {
                rawWidth = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                rawHeight = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
                }
            catch (Exception ignored) {}
            }

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17)
            {
            try
                {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                rawWidth = realSize.x;
                rawHeight = realSize.y;
                }
            catch (Exception ignored) {}
            }

        // Scribe.debug("Screen diameter: " + rawWidth + "x" + rawHeight);
        }

    }

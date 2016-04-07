package org.lattilad.bestboard;

import android.content.Context;

import org.lattilad.bestboard.server.TextBeforeCursor;


/**
 * INTERFACE - CONNECTION FOR SENDING KEYS
 */
public interface SoftBoardListener
    {
    /**
     * Get application context.
     * This method is defined by both Service and Activity.
     * @return application context
     */
    Context getApplicationContext();
    // THIS IS NOT NEEDED (MAYBE) IF LAYOUT and BOARDVIEW IS DIVIDED
    // UseState.checkOrientation() needs context
    // readPreferences() need context
    // vibration needs context

    boolean sendKeyDown( long downTime, int keyEventCode );
    boolean sendKeyUp( long downTime, long eventTime, int keyEventCode );
    void sendKeyDownUp(int keyEventCode);

    void sendString( String string, int autoSpace );
    // UseState needs this to change layout
    boolean undoLastString();

    LayoutView getLayoutView();

    TextBeforeCursor getTextBeforeCursor();

    void checkAtBowStart();
    void checkAtStrokeEnd();

    void deleteCharBeforeCursor(int n);
    void deleteCharAfterCursor(int n);

    int deleteSpacesBeforeCursor();
    void changeStringBeforeCursor( String string );
    void changeStringBeforeCursor( int length, String string );

    boolean sendDefaultEditorAction(boolean fromEnterKey);

    void jumpBegin( boolean select );
    void jumpEnd( boolean select );
    void jumpLeftStart( boolean select );
    void jumpRightStart( boolean select );
    void jumpLeftEnd( boolean select );
    void jumpRightEnd( boolean select );

    void jumpLeft( boolean select );
    void jumpRight( boolean select );
    void jumpWordLeft( boolean select );
    void jumpWordRight( boolean select );
    void jumpParagraphLeft( boolean select );
    void jumpParagraphRight( boolean select );

    void startSoftBoardParser();
    }


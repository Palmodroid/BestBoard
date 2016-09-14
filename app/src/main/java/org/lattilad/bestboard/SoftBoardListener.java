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

    String getEditorPackageName( );

    boolean isRetrieveTextEnabled();

    boolean sendKeyDown( long downTime, int keyEventCode );
    boolean sendKeyUp( long downTime, long eventTime, int keyEventCode );
    void sendKeyDownUp(int keyEventCode);

    long getProcessCounter();
    boolean checkProcessCounter(long operationCounter);

    void sendString( String string, int autoSpace );

    // Actually not in use - should be part of FIELD/PLAY
    void sendString( String string, int autoSpace, int movement );

    void sendDelete( int length );

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

    void toggleCursor();
    void selectAll();

    void jumpTop(boolean select);
    void jumpBottom(boolean select);

    void jumpLeft( int cursor, boolean select );
    void jumpRight( int cursor, boolean select );
    void jumpWordLeft( int cursor, boolean select );
    void jumpWordRight( int cursor, boolean select );
    void jumpParaLeft(int cursor, boolean select);
    void jumpParaRight(int cursor, boolean select);

    String getWordOrSelected();
    void changeLastWordOrSelected( String newText, boolean restoreCursor );

    void startSoftBoardParser( String coatFileName );
    }


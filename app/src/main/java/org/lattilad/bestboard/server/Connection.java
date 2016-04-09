package org.lattilad.bestboard.server;


import android.view.inputmethod.InputConnection;

/**
 * Connection synchronizes text with stored text.
 * Normally these methods need an InputConnection to load text.
 */
public interface Connection
    {
    // CharSequence getSelectedText();
    CharSequence getTextAfterCursor( InputConnection ic, int n);
    CharSequence getTextBeforeCursor( InputConnection ic, int n);
    boolean isStoreTextEnabled();
    }

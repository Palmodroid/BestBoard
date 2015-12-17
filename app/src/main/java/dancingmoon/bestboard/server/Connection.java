package dancingmoon.bestboard.server;


/**
 * Connection synchronizes text with stored text.
 * Normally these methods need an InputConnection to load text.
 */
public interface Connection
    {
    // CharSequence getSelectedText();
    CharSequence getTextAfterCursor(int n);
    CharSequence getTextBeforeCursor(int n);
    boolean isStoreTextEnabled();
    }

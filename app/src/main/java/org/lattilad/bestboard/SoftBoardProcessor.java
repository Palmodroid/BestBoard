package org.lattilad.bestboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.lattilad.bestboard.buttons.PacketText;
import org.lattilad.bestboard.debug.Debug;
import org.lattilad.bestboard.prefs.PrefsFragment;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.server.Connection;
import org.lattilad.bestboard.server.TextAfterCursor;
import org.lattilad.bestboard.server.TextBeforeCursor;
import org.lattilad.bestboard.states.CapsState;
import org.lattilad.bestboard.states.LayoutStates;

/**
 * Text processing part of SoftBoardService
 */
public class SoftBoardProcessor implements
        SoftBoardListener,
        Connection
    {
    /** Connection for the main soft-board service */
    private SoftBoardService softBoardService;

    /** Data structure for the whole softBoard */
    private SoftBoardData softBoardData;

    public SoftBoardData getSoftBoardData()
        {
        return softBoardData;
        }

    /** There is only ONE boardView for the whole softBoard, generated in softBoardParserFinished() */
    private LayoutView layoutView;

    public LayoutView getLayoutView()
        {
        return layoutView;
        }


    /**
     * FALSE: getText... methods are disabled - no communication is enabled
     */
    private boolean retrieveTextEnabled = true;

    /**
     * FALSE: stored text and calculated position are used; cursor position is checked after time-limit
     * TRUE: text around cursor is always re-read, stored text/cursor position are not used
     */
    // private boolean heavyCheckEnabled = false;
    private boolean storeTextEnabled = true;

    /** public access is needed by Connection */
    public boolean isStoreTextEnabled()
        {
        return storeTextEnabled;
        }

    /*
     * real cursor positions are provided by the system (onUpdateSelection...)
     * calculated position is calculated by bestboard.
     * It is only used to check, whether stored texts should be invalidated.
     * Real positions should be used always!
     */

    /** Cursor position presented by the editor */
    private int realCursorStart;

    /** Cursor position presented by the editor */
    private int realCursorEnd;

    public boolean isTextSelected()
        {
        return realCursorStart != realCursorEnd;
        }

    /** Cursor position calculated by the softkeyboard */
    private int calculatedCursorStart = 0;

    /** Lastly sent string. Null if undo is not possible */
    private String undoString;

    private static final long NEVER = Long.MAX_VALUE;

    private static final long ALWAYS = 0L;

    // -> CHAT
    private long checkEnabledAfter = ALWAYS;

    private int elongationPeriod;

    /** Text before the cursor is stored in textBeforeCursor. Text is provided by IMS */
    private TextBeforeCursor textBeforeCursor = new TextBeforeCursor( this );

    public TextBeforeCursor getTextBeforeCursor()
        {
        return textBeforeCursor;
        }

    /** Text after the cursor is stored in textBeforeCursor. Text is provided by IMS */
    private TextAfterCursor textAfterCursor = new TextAfterCursor( this );

    public TextAfterCursor getTextAfterCursor()
        {
        return textAfterCursor;
        }

    /** Temporary variable to store string to be send */
    private StringBuilder sendBuilder = new StringBuilder();


    public SoftBoardProcessor( SoftBoardService softBoardService, SoftBoardData softBoardData )
        {
        this.softBoardService = softBoardService;
        this.softBoardData = softBoardData;

        softBoardData.connect(this);

        // Orientation should be checked, but index is 0 by default.
        // No setIndex() is needed
        this.softBoardData.boardTable.setOrientation();

        layoutView = new LayoutView( softBoardService );
        layoutView.setLayout( this.softBoardData.boardTable.getActiveLayout() );

        softBoardService.setInputView(layoutView);
        }


    public View onCreateInputView()
        {
        // setting index is not necessary
        softBoardData.boardTable.setOrientation();

        // boardView should be saved
        ViewGroup parent = (ViewGroup) getLayoutView().getParent();
        if (parent != null)
            {
            parent.removeView(getLayoutView());
            }

        getLayoutView().setLayout(softBoardData.boardTable.getActiveLayout());

        return getLayoutView();
        }


    public Context getApplicationContext()
        {
        return softBoardService.getApplicationContext();
        }

    public void startSoftBoardParser()
        {
        softBoardService.startSoftBoardParser();
        }


    /**
     * Initializes a new text session.
     * This happens at each EditText start, and at each uncontrolled cursor movements
     */
    private void initTextSession( )
        {
        // no undo at start
        undoString = null;

        // surrounding text is changed
        textBeforeCursor.invalidate();
        textAfterCursor.invalidate();
        }


    public void initInput()
        {
        if (softBoardData != null)
            {
            Scribe.title( "Editor session started" );

            EditorInfo editorInfo = softBoardService.getCurrentInputEditorInfo();
            Scribe.debug( Debug.TEXT, "Start: " + editorInfo.initialSelStart + ", end: " + editorInfo.initialSelEnd);

            // position of the cursor
            calculatedCursorStart = editorInfo.initialSelStart;

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(softBoardService);
            String retrieveTextPreference = sharedPrefs.getString(
                    softBoardService.getString(R.string.editing_retrieve_text_key),
                    softBoardService.getString(R.string.editing_retrieve_text_default));

            if ( retrieveTextPreference.startsWith("E") )
                {
                retrieveTextEnabled = true;
                Scribe.debug(Debug.CURSOR, "Editing (retrieve text) is enabled (forced)");
                }
            else if ( retrieveTextPreference.startsWith("D") )
                {
                retrieveTextEnabled = false;
                Scribe.debug(Debug.CURSOR, "Editing (retrieve text) is disabled (forced)");
                }
            else
                {
                retrieveTextEnabled = (calculatedCursorStart >= 0);
                Scribe.debug(Debug.CURSOR, "Editing is set automatically. Retrieve text: " + retrieveTextEnabled);
                }

            String storeTextPreference = sharedPrefs.getString(
                    softBoardService.getString(R.string.editing_store_text_key),
                    softBoardService.getString(R.string.editing_store_text_default));

            if ( storeTextPreference.startsWith("E") )
                {
                storeTextEnabled = true;
                Scribe.debug(Debug.CURSOR, "Editing (store text) is enabled (forced), LIGHT check.");
                }
            else if ( storeTextPreference.startsWith("D") )
                {
                storeTextEnabled = false;
                Scribe.debug(Debug.CURSOR, "Editing (store text) is disabled (forced), HEAVY check.");
                }
            else
                {
                int inputType = editorInfo.inputType & InputType.TYPE_MASK_CLASS;

                if ( inputType == InputType.TYPE_CLASS_TEXT || inputType == 0 )
                    {
                    storeTextEnabled = true;
                    Scribe.debug(Debug.CURSOR, "Editing (store text) is enabled automatically. LIGHT CHECK" );
                    }
                else
                    {
                    storeTextEnabled = false;
                    Scribe.debug(Debug.CURSOR, "Editing (store text) is disabled automatically. HEAVY CHECK" );
                    }
                }

            elongationPeriod = sharedPrefs.getInt( PrefsFragment.EDITING_ELONGATION_PERIOD_INT_KEY, 0 );
            Scribe.debug( Debug.CURSOR, "Elongation period: " + elongationPeriod );

            // The order is not obligatory, but we have to know, where is the start, and where is the end
            if ( editorInfo.initialSelStart < editorInfo.initialSelEnd )
                {
                realCursorStart = editorInfo.initialSelStart;
                realCursorEnd = editorInfo.initialSelEnd;
                }
            else
                {
                realCursorStart = editorInfo.initialSelEnd;
                realCursorEnd = editorInfo.initialSelStart;
                }

            Scribe.debug( Debug.CURSOR, isTextSelected() ?
                    "Text is selected" :
                    "Cursor position: " + realCursorStart );

            // pressed hard-keys are released
            // NOT NEEDED IN INSTANTSIMULATE
            softBoardData.layoutStates.resetMetaButtons();

            // enter's title is set
            softBoardData.setAction(editorInfo.imeOptions);

            initTextSession();
            // Meta reset needed only in new input box
            if ( softBoardData.textSessionSetsMetastates )
                {
                layoutView.type();
                ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                        .setAutoCapsState(CapsState.AUTOCAPS_OFF);
                layoutView.invalidate();
                }

            // set autocaps state depending on field behavior and cursor position
            if ( calculatedCursorStart == 0 && editorInfo.initialCapsMode != 0 )
                {
                ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                        .setAutoCapsState(CapsState.AUTOCAPS_ON);
                }
            }
        }


    /**
     * This is the most important part:
     * Here we can control whether cursor/selection was changed without our knowledge.
     * Implemented send... methods should set calculatedPosition.
     * Stored text is invalidated if new position does not match calculatedPosition
     * If text is selected, then all stored-text functions are disabled
     */
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd)
        {
        Scribe.debug(Debug.TEXT,
                "OldStart: " + oldSelStart +
                        " OldEnd: " + oldSelEnd +
                        " NewStart: " + newSelStart +
                        " NewEnd: " + newSelEnd );

        // Cursor movement checking is independent from selection.
        // Cursor position is the start of the selection.
        if ( calculatedCursorStart != newSelStart )
            {
            if ( System.nanoTime() > checkEnabledAfter )
                {
                Scribe.debug( Debug.CURSOR, "Cursor is moving. Calculated position does not match: " + calculatedCursorStart);
                calculatedCursorStart = newSelStart;
                initTextSession();
                }
            else
                {
                Scribe.debug( Debug.CURSOR, "Text processing is not yet finished. Calculated position does not match: " + calculatedCursorStart);
                }
            }

        if ( newSelStart < newSelEnd )
            {
            realCursorStart = newSelStart;
            realCursorEnd = newSelEnd;
            }
        else
            {
            realCursorStart = newSelEnd;
            realCursorEnd = newSelStart;
            }

        // Text is NOT selected...
        if ( newSelStart == newSelEnd )
            {
            Scribe.debug( Debug.CURSOR, "Real cursor position: " + newSelStart );
            }

        // Text is selected
        else // newSelStart != newSelEnd
            {
            Scribe.debug( Debug.CURSOR, "Cursor position: Text is selected, cursor position is invalidated!");
            calculatedCursorStart = newSelStart;
            initTextSession();
            }

        Scribe.debug(Debug.TEXT,
                "Real Start: " + realCursorStart + " Real End: " + realCursorEnd );
        }


    /**
     * This method is called by BoardView.MainTouchBow constructor
     * Only if light check is active:
     * if real and calculated positions are different, then stored text is invalidated
     */
    public void checkAtBowStart()
        {
        Scribe.locus(Debug.CURSOR);

        checkEnabledAfter = ALWAYS;

        if ( storeTextEnabled )
            {
            if (realCursorStart != calculatedCursorStart)
                {
                Scribe.debug(Debug.CURSOR, "LIGHT CHECK: Cursor positions doesn't match at the start of the bow!" +
                        " Calculated: " + calculatedCursorStart +
                        " Real: " + realCursorStart);

                calculatedCursorStart = realCursorStart;
                initTextSession();
                }
            else
                {
                Scribe.debug(Debug.CURSOR, "LIGHT CHECK: Cursor positions match. Position: " + realCursorStart);
                }
            }
        else
            {
            Scribe.debug(Debug.CURSOR, "HEAVY CHECK: Cursor position check is not needed.");
            }
        }

    public void checkAtStrokeEnd()
        {
        Scribe.locus(Debug.CURSOR);

        if ( checkEnabledAfter == NEVER )
            {
            checkEnabledAfter = System.nanoTime() + (long)elongationPeriod * 1000000L;
            Scribe.debug( Debug.CURSOR, "Check is enabled after: " + checkEnabledAfter );
            }
        }

    /**
     * Text (string) can be sent ONLY through this method
     * CalculatedPosition is calculated
     * String is sent to the editor (commitText)
     * String is sent to stored-text
     * @param inputConnection input connection - CANNOT BE NULL!
     * @param string string to send - CANNOT BE NULL!
     */
    private void sendString(InputConnection inputConnection, String string)
        {
        Scribe.debug(Debug.TEXT, "String to send: [" + string + "], length: " + string.length());

        undoString = string;
        calculatedCursorStart += string.length();
        checkEnabledAfter = NEVER;
        if ( storeTextEnabled )
            {
            Scribe.debug(Debug.TEXT, "LIGHT CHECK: Text before cursor is updated.");
            textBeforeCursor.sendString(string);
            textAfterCursor.invalidate();
            }
        inputConnection.commitText(string, 1);

        // TIMING EVENT
        softBoardData.characterCounter.measure(string.length());
        softBoardData.showTiming();

        Scribe.debug(Debug.CURSOR, "String. Calculated cursor position: " + calculatedCursorStart);
        }


    /**
     * Text can be deleted before cursor ONLY through this method
     * CalculatedPosition is calculated
     * Delete is sent to the editor
     * Delete is sent to stored-text
     * @param inputConnection input connection - CANNOT BE NULL!
     * @param length number of java characters to delete before cursor
     */
    private void sendDeleteBeforeCursor(InputConnection inputConnection, int length)
        {
        Scribe.debug(Debug.TEXT, "Chars to delete before cursor: " + length);

        if ( length > 0 )
            {
            undoString = null;
            calculatedCursorStart -= length;
            checkEnabledAfter = NEVER;
            if ( storeTextEnabled )
                {
                Scribe.debug(Debug.TEXT, "LIGHT CHECK: Text before cursor is updated (deleted).");
                textBeforeCursor.sendDelete(length);
                }
            inputConnection.deleteSurroundingText(length, 0);
            }

        Scribe.debug(Debug.CURSOR, "Deleted. Calculated cursor position: " + calculatedCursorStart);
        }


    private void sendDeleteAfterCursor(InputConnection inputConnection, int length)
        {
        Scribe.debug(Debug.TEXT, "Chars to delete after cursor: " + length);

        if ( length > 0 )
            {
            undoString = null;
            // calculatedCursorStart does not change
            checkEnabledAfter = NEVER;
            if ( storeTextEnabled )
                {
                Scribe.debug(Debug.TEXT, "LIGHT CHECK: Text after cursor is updated (deleted).");
                textAfterCursor.sendDelete(length);
                }
            inputConnection.deleteSurroundingText( 0, length );
            }

        Scribe.debug(Debug.CURSOR, "Deleted. Calculated cursor position: " + calculatedCursorStart);
        }


    private int sendDeleteSpacesBeforeCursor( InputConnection inputConnection )
        {
        int space;

        textBeforeCursor.reset();
        for ( space = 0; textBeforeCursor.read() == ' '; space++ ) ;

        sendDeleteBeforeCursor(inputConnection, space);

        Scribe.debug(Debug.TEXT, "Spaces deleted before cursor: " + space);
        return space;
        }


    private int sendDeleteSpacesAfterCursor( InputConnection inputConnection )
        {
        int space;

        textAfterCursor.reset();
        for ( space = 0; textAfterCursor.read() == ' '; space++ ) ;

        sendDeleteAfterCursor(inputConnection, space);

        Scribe.debug(Debug.SERVICE, "Spaces deleted after cursor: " + space);
        return space;
        }


    public boolean undoLastString()
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if ( ic != null && undoString != null )
            {
            if ( storeTextEnabled )
                {
                sendDeleteBeforeCursor(ic, undoString.length());
                undoString = null;
                return true;
                }
            else
                {
                if (textBeforeCursor.compare(undoString))
                    {
                    Scribe.debug(Debug.TEXT, "HEAVY CHECK: Text undo: string matches!");
                    sendDeleteBeforeCursor(ic, undoString.length());
                    undoString = null;
                    return true;
                    }
                else
                    {
                    Scribe.debug(Debug.TEXT, "HEAVY CHECK: Text undo: string does NOT match!");
                    }
                }
            }
        return false;
        }


    public void sendString( String string, int autoSpace )
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();

            if ( (autoSpace & PacketText.ERASE_SPACES_BEFORE) != 0 && softBoardData.autoEnabled)
                {
                sendDeleteSpacesBeforeCursor( ic );
                }
            if ( (autoSpace & PacketText.ERASE_SPACES_AFTER) != 0 && softBoardData.autoEnabled )
                {
                sendDeleteSpacesAfterCursor( ic );
                }

            sendBuilder.setLength(0);
            if ( (autoSpace & PacketText.AUTO_SPACE_BEFORE) != 0 && softBoardData.autoEnabled )
                {
                textBeforeCursor.reset();
                if ( !isWhiteSpace( textBeforeCursor.read()) )
                    sendBuilder.append(' ');
                }

            sendBuilder.append(string);

            if ( (autoSpace & PacketText.AUTO_SPACE_AFTER) != 0 && softBoardData.autoEnabled )
                {
                textAfterCursor.reset();
                if ( !isSpace( textAfterCursor.read()) )
                    sendBuilder.append(' ');
                }

            sendString(ic, sendBuilder.toString());

            ic.endBatchEdit();
            }
        }


    public void changeStringBeforeCursor( String string )
        {
        changeStringBeforeCursor(string.length(), string);
        }


    public void changeStringBeforeCursor( int length, String string )
        {
        Scribe.locus(Debug.SERVICE);

        if ( !isTextSelected() )
            {
            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                sendDeleteBeforeCursor(ic, length);
                sendString(ic, string);
                }
            }
        }

    @Override
    public boolean sendDefaultEditorAction(boolean fromEnterKey)
        {
        return false;
        }


    // Modify needs it!
    public int deleteSpacesBeforeCursor()
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            return sendDeleteSpacesBeforeCursor( ic );
            }

        return 0;
        }


    // DO BACKSPACE
    public void deleteCharBeforeCursor(int n)
        {
        Scribe.locus(Debug.SERVICE);

        // text is selected
        if ( isTextSelected() )
            {
            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                sendString(ic, "");
                }
            }

        // text is not selected
        else
            {
            textBeforeCursor.reset();
            int data = textBeforeCursor.read();
            int l = 1;

            Scribe.debug( Debug.TEXT, "Character before cursor: " + Integer.toHexString(data));
            if (data < 0)
                {
                Scribe.debug(Debug.TEXT, "No more text to delete, hard DEL is sent!");

                softBoardData.layoutStates.forceBinaryHardState( 0x15 );
                sendKeyDownUp(KeyEvent.KEYCODE_DEL);
                softBoardData.layoutStates.clearBinaryHardState();

                return;
                }
            else if ((data & 0xFC00) == 0xDC00 )
                {
                Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                l++;
                }
            else if ((data & 0xFC00) == 0xD800 )
                {
                Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                }

            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                sendDeleteBeforeCursor(ic, l);
                }
            }
        }


    // DO DELETE
    public void deleteCharAfterCursor(int n)
        {
        Scribe.locus(Debug.SERVICE);

        // text is selected
        if ( isTextSelected() )
            {
            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                sendString(ic, "");
                }
            }

        // text is not selected
        else
            {
            textAfterCursor.reset();
            int data = textAfterCursor.read();
            int l = 1;

            Scribe.debug( Debug.TEXT, "Character after cursor: " + Integer.toHexString(data));
            if (data < 0)
                {
                Scribe.debug(Debug.TEXT, "No more text to delete, hard FORWARD_DEL is sent!");

                softBoardData.layoutStates.forceBinaryHardState( 0x15 );
                sendKeyDownUp(KeyEvent.KEYCODE_FORWARD_DEL);
                softBoardData.layoutStates.clearBinaryHardState();

                return;
                }
            else if ((data & 0xFC00) == 0xD800 )
                {
                Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                l++;
                }
            else if ((data & 0xFC00) == 0xDC00 )
                {
                Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                }

            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                sendDeleteAfterCursor(ic, l);
                }
            }
        }


    public void jumpBegin( boolean select )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null && retrieveTextEnabled) // because of consistency with jumpEnd
            {
            ic.setSelection(select ? realCursorEnd : 0, 0);
            }
        }


    public void jumpEnd( boolean select )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null && retrieveTextEnabled)
            {
            ic.beginBatchEdit();

            CharSequence temp;
            int position = realCursorEnd;

            do
                {
                temp = ic.getTextAfterCursor(2048, 0);
                position += temp.length();
                ic.setSelection( select ? realCursorStart : position, position );
                } while (temp.length() == 2048);

            ic.endBatchEdit();
            }

        }


    public void jumpLeft( boolean select ){}
    public void jumpRight( boolean select ){}

    public void jumpWordLeft( boolean select )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            int offset = 0;
            int c;

            Scribe.error( "Real start: " + realCursorStart + " Real end: " + realCursorEnd );
            getTextBeforeCursor().reset();
            while ( isWhiteSpace( c = getTextBeforeCursor().read()) ) // -1 is NOT whitespace !!
                {
                offset++;
                }

            while ( !isWhiteSpace(c) && c != -1 )
                {
                offset++;
                c = getTextBeforeCursor().read();
                }

            if ( select )
                ic.setSelection( realCursorEnd, realCursorStart - offset );
            else
                ic.setSelection( realCursorStart - offset, realCursorStart - offset);

            Scribe.error("New start: " + (realCursorStart - offset) + " New end: " + realCursorEnd);
            }
        }

    public void jumpWordRight( boolean select )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            int offset = 0;
            int c;

            Scribe.error( "Real start: " + realCursorStart + " Real end: " + realCursorEnd );
            getTextAfterCursor().reset();
            while ( isWhiteSpace( c = getTextAfterCursor().read()) ) // -1 is NOT whitespace !!
                {
                offset++;
                }

            while ( !isWhiteSpace(c) && c != -1 )
                {
                offset++;
                c = getTextAfterCursor().read();
                }

            if ( select )
                ic.setSelection( realCursorStart, realCursorEnd + offset );
            else
                ic.setSelection( realCursorEnd + offset, realCursorEnd + offset);

            Scribe.error("New start: " + realCursorStart + " New end: " + (realCursorEnd + offset));
            }
        }


    public void jumpParagraphLeft( boolean select ){}
    public void jumpParagraphRight( boolean select ){}



    /**
     * Simulates key-event
     * @param downTime time, when key was pressed
     * @param eventTime time, when key was released
     * @param keyEventAction KeyEvent.ACTION_DOWN or KeyEvent.ACTION_UP
     * @param keyEventCode android keyCode
     * @return true in success, false if no InputConnection is available
     */
    private boolean sendKeyEvent( long downTime, long eventTime,
                                  int keyEventAction, int keyEventCode )
        {
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic == null)
            {
            Scribe.error("Cannot get input connection!");
            return false;
            }

        return ic.sendKeyEvent(new KeyEvent(
                downTime,               // this key originally went down
                eventTime,              // this event happened (downTime in ACTION_DOWN)
                keyEventAction,         // ACTION_DOWN or ACTION_UP
                keyEventCode,           // android keyCode
                0,                      // repeat is not implemented
                softBoardData.layoutStates.getAndroidMetaState(),
                // meta-state in android format
                KeyCharacterMap.VIRTUAL_KEYBOARD,
                // device id FIX
                keyEventCode,           // android keyCode as scan-code
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE,
                InputDevice.SOURCE_TOUCHSCREEN));
        }


    /**
     * Simulates key-down event
     * @param downTime time, when key was pressed
     * @param keyEventCode android keyCode
     * @return true in success, false if no InputConnection is available
     */
    public boolean sendKeyDown( long downTime, int keyEventCode )
        {
        Scribe.debug(Debug.SERVICE, keyEventCode + " hard button is down!");

        return sendKeyEvent( downTime, downTime, KeyEvent.ACTION_DOWN, keyEventCode );
        }


    /**
     * Simulates key-up event
     * @param downTime time, when key was pressed
     * @param eventTime time, when key was released
     * @param keyEventCode android keyCode
     * @return true in success, false if no InputConnection is available
     */
    public boolean sendKeyUp( long downTime, long eventTime, int keyEventCode )
        {
        Scribe.debug(Debug.SERVICE, keyEventCode + " hard button is up!");

        return sendKeyEvent( downTime, eventTime, KeyEvent.ACTION_UP, keyEventCode );
        }


    /**
     * Simulates key-down/key-up sequence
     * Input-connection availability is not returned!
     * @param keyEventCode android keyCode
     */
    public void sendKeyDownUp(int keyEventCode)
        {
        Scribe.debug(Debug.SERVICE, keyEventCode + " hard button is down-up!");

        // INSTANTSIMULATE softBoardData.layoutStates.simulateMetaPress();

        long downTime = SystemClock.uptimeMillis();
        if (sendKeyDown( downTime, keyEventCode ))
            sendKeyUp( downTime, SystemClock.uptimeMillis(), keyEventCode );
        // ACTION_UP will be sent only, if ACTION_DOWN was successfully sent

        // INSTANTSIMULATE softBoardData.layoutStates.simulateMetaRelease();
        }


    /**
     * Gets text before cursor - needed only by StoredText
     * Text can be retrieved only, if text is NOT selected
     * @param n number of java chars to get
     * @return text or null, if no text is available (or text is selected)
     */
    public CharSequence getTextBeforeCursor( int n )
        {
        if (retrieveTextEnabled)
            {
            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                return ic.getTextBeforeCursor(n, 0);
                }
            }
        return null;
        }


    /**
     * Gets text after cursor - needed only by StoredText
     * Text can be retrieved only, if text is NOT selected
     * @param n number of java chars to get
     * @return text or null, if no text is available (or text is selected)
     */
    public CharSequence getTextAfterCursor( int n )
        {
        if (retrieveTextEnabled)
            {
            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                return ic.getTextAfterCursor(n, 0);
                }
            }
        return null;
        }

    public boolean isWhiteSpace( int ch )
        {
        return ch == ' ' || ch == '\n' || ch == '\t';
        }

    public boolean isSpace( int ch )
        {
        return ch == ' ';
        }
    }

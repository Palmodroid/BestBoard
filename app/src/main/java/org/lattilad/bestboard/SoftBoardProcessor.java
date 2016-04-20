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
import org.lattilad.bestboard.utils.StringUtils;

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
     * TRUE: getText... methods can take text from editor
     * FALSE: getText... methods are disabled - no communication is enabled
     * (jumpBottom uses the same method, so it is disabled, too)
     */
    private boolean retrieveTextEnabled = true;

    /** public access */
    public boolean isRetrieveTextEnabled()
        {
        return retrieveTextEnabled;
        }

    /**
     * TRUE: calculated cursor positions are used. Calculated positions checked at each bow-start,
     * and after stroke-end+elongation+time (between strokes)
     * FALSE: text around cursor is always re-read, stored text/cursor position are not used
     */
    private boolean storeTextEnabled = true;

    /** public access is needed by Connection */
    public boolean isStoreTextEnabled()
        {
        return storeTextEnabled;
        }

    /**
     * Két "kurzor" van:
     * - NINCS kijelölés (cursor) - ilyenkor a két kurzor pozíciója egybe esik
     * - VAN kijelölés (selection) - a két kurzor pozíciója elkülönül:
     * - CURSOR_BEGIN - a kisebbik pozíción álló kurzor mozog (a másik fix),
     * - CURSOR_END - a magasabbik pozíción álló kurzor mozog (a kisebb fix)
     */

    public static final int CURSOR_BEGIN = 0;
    public static final int CURSOR_END = 1;
    public static final int CURSOR_LAST = -1;

    /** a ket kurzorpozicio kivulrol - onUpdateSelection() */
    private int[] realCursor = { 0, 0 };

    /** a ket kurzorpozicio - bow soran szamitva */
    private int[] calculatedCursor = { 0, 0 };

    /** nincs kijeloles, ha a ketto egybeesik */
    public boolean isSelected()
         {
         return calculatedCursor [0] != calculatedCursor [1];
         }

    /**
     * Az éppen mozgó kurzor a három lehetőség közül (CURSOR/SELECTIOM_START/CURSOR_END).
     * Fontos tudni, hogy a szöveget utoljára erre a kurzorra tároltuk el.
     * Ezt megváltoztatni csak a selectCursor() metódussal szabad.
     */
    private int cursorLastMoved = CURSOR_BEGIN;

    private static final long NEVER = Long.MAX_VALUE;
    private static final long ALWAYS = 0L;
    /**
     * Controls the behavior of onUpdateSelection
     * Bow start: ALWAYS
     * Text modifications: NEVER
     * Stroke end: + elongationPeriod
     */
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

    private String undoString = null;

    /** Temporary variable to store string to be send */
    private StringBuilder sendBuilder = new StringBuilder();


    /*****************************************
     * CONTROL OF TEXT PROCESSING AND CURSOR MOVEMENTS
     *****************************************/


    public void initInput()
        {
        Scribe.title("Editor session started");

        EditorInfo editorInfo = softBoardService.getCurrentInputEditorInfo();
        Scribe.debug( Debug.TEXT, "Start: " + editorInfo.initialSelStart + ", end: " + editorInfo.initialSelEnd);

        checkEnabledAfter = ALWAYS;

        // The order is not obligatory, but we have to know, where is the start, and where is the end
        realCursor[0] = editorInfo.initialSelStart;
        realCursor[1] = editorInfo.initialSelEnd;

        // position of the cursor
        checkCalculatedToReal();

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
            retrieveTextEnabled = (calculatedCursor[0] >= 0);
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

        // pressed hard-keys are released
        // NOT NEEDED IN INSTANTSIMULATE
        softBoardData.layoutStates.resetMetaButtons();

        // enter's title is set
        softBoardData.setEnterAction(editorInfo.imeOptions);

        // Meta reset needed only in new input box
        if ( softBoardData.textSessionSetsMetastates )
            {
            layoutView.type();
            ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                    .setAutoCapsState(CapsState.AUTOCAPS_OFF);
            layoutView.invalidate();
            }

        // set autocaps state depending on field behavior and cursor position
        if ( calculatedCursor[0] == 0 && editorInfo.initialCapsMode != 0 )
            {
            ((CapsState) softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS])
                    .setAutoCapsState(CapsState.AUTOCAPS_ON);
            }
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


    /**
     * This is the most important part:
     * Here we can control whether cursor/selection was changed without our knowledge.
     * Implemented send... methods should set calculatedPosition.
     * Stored text is invalidated if new position does not match calculatedPosition
     * If text is selected, then all stored-text functions are disabled
     */
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd)
        {
        Scribe.locus(Debug.CURSOR);

        // Real cursor position always should be updated
        realCursor[0] = newSelStart;
        realCursor[1] = newSelEnd;

        Scribe.debug(Debug.TEXT,
                "Real Start: " + realCursor[0] +
                        " Real End: " + realCursor[1] +
                        " Calculated Start: " + calculatedCursor[0] +
                        " Calculated End: " + calculatedCursor[1]);

        // Calculated cursor positions should be checked only if checkEnabledAfter allows it
        if ( System.nanoTime() > checkEnabledAfter )
            {
            checkCalculatedToReal();
            }
        else
            {
            Scribe.debug(Debug.CURSOR, "Real position changed during forbidden time-frame.");
            }
        }


    /**
     * This method is called by BoardView.MainTouchBow constructor
     * Only if light check is active:
     * if real and calculated positions are different, then stored text is invalidated
     */
    public void checkAtBowStart()
        {
        Scribe.locus(Debug.CURSOR);

        // Check will be disabled after the first text processing,
        // and enabled after stroke-end
        checkEnabledAfter = ALWAYS;
        checkCalculatedToReal();
        }


    public void checkAtStrokeEnd()
        {
        Scribe.locus(Debug.CURSOR);

        if ( checkEnabledAfter == NEVER )
            {
            checkEnabledAfter = System.nanoTime() + (long)elongationPeriod * 1000000L;
            Scribe.debug(Debug.CURSOR, "Check is enabled after: " + checkEnabledAfter);
            }
        }


    private int modifyCalculatedCursor(int calculatedBoth )
        {
        return modifyCalculatedCursor( calculatedBoth, calculatedBoth);
        }

    /** returns overlapped length, if cursors overlap */
    private int modifyCalculatedCursor(int calculated0, int calculated1)
        {
        checkEnabledAfter = NEVER;
        realCursor[0] = -1;
        if ( calculated0 <= calculated1 )
            {
            calculatedCursor[0] = calculated0;
            calculatedCursor[1] = calculated1;
            return 0;
            }
        else
            {
            calculatedCursor[0] = calculated1;
            calculatedCursor[1] = calculated0;
            return calculated0 - calculated1;
            }
        }


    /**
     * A továbbiakban a kiválasztott kurzor körüli szöveg tárolódik,
     * és a kiolvasáshoz be is állítja a pozíciót (kijelölés nélkül) a kurzorra.
     * Ha befejeztük a pozíciók módosítását, akkor be kell állítani a kijelölést annak megfelelően.
     *
     * Az a baj, hogy csak a kurzorpozíció mellől tudunk szöveget beolvasni.
     * ERRE AKKOR VAN SZÜKSÉG, HA MI MAGUNK AKARJUK A KURZORT VÁLTOZTATNI
     */
    private void selectCursor(InputConnection ic, int cursorToMove)
        {
        Scribe.locus(Debug.CURSOR);

        // valtas csak akkor ertelmezheto, ha van kijeloles
        if ( cursorLastMoved != cursorToMove )
            {
            cursorLastMoved = cursorToMove;
            if  ( isSelected() )
                {
                textBeforeCursor.invalidate();
                textAfterCursor.invalidate();
                }
            Scribe.debug(Debug.CURSOR, "Controlled cursor changed: " + cursorLastMoved );
            }
        else
            {
            Scribe.debug(Debug.CURSOR, "Controlled cursor remained: " + cursorLastMoved);
            }

        Scribe.debug(Debug.CURSOR, "Position set to: " + calculatedCursor[cursorToMove]);
//        if ( ic != null )
//            ic.setSelection( calculatedCursor[cursorToMove], calculatedCursor[cursorToMove] );

        textBeforeCursor.reset();
        textAfterCursor.reset();
        }

    /**
     * Javítja a cursor pozíciókat, ha külső változtatás történt.
     * ERRE AKKOR VAN SZÜKSÉG, HA A KÜLSŐ VÁLTOZÁSOK OKOZTÁK A MÓDOSÍTÁST
     */
    private void checkCalculatedToReal( )
        {
        Scribe.locus(Debug.CURSOR);

        if ( realCursor[0] == -1 )
            {
            Scribe.error(Debug.CURSOR, " Real position is already consumed! ");
            return;
            }

        if ( realCursor[0] > realCursor[1] )
            {
            int tmp = realCursor[0];
            realCursor[0] = realCursor[1];
            realCursor[1] = tmp;
            }

        // This part is not necessary, as modifyCalculatedCursor does not allow it
        /* if ( calculatedCursor[0] > calculatedCursor[1] )
            {
            int tmp = calculatedCursor[0];
            calculatedCursor[0] = calculatedCursor[1];
            calculatedCursor[1] = tmp;
            cursorLastMoved++; cursorLastMoved %= 2;
            } */

        Scribe.debug(Debug.CURSOR, " Real position: " + realCursor[0] + "-" + realCursor[1] +
                " Calculated position: " + calculatedCursor[0] + "-" + calculatedCursor[1]);

        if ( calculatedCursor[0] != realCursor[0] ||
                calculatedCursor[1] != realCursor[1] )
            {
            calculatedCursor[0] = realCursor[0];
            calculatedCursor[1] = realCursor[1];
            Scribe.debug(Debug.CURSOR, "Calculated and real positions do not match, calculated is corrected to " +
                    + calculatedCursor[0] + "-" + calculatedCursor[1] );

            initTextSession(); // invalidate and clear undo
            }
        else
            {
            Scribe.debug(Debug.CURSOR, "Calculated positions are correct.");
            }

        realCursor[0] = -1;
        }


    /*****************************************
     * LOW LEVEL TEXT PROCESSING
     * Itt minden metódusnak szüksége van az InputConnection-re, ÉS
     * Minden méret char-ban van megadva (és nem code-point-ban)
     *****************************************/


    private void sendString( InputConnection ic, String string )
        {
        Scribe.locus(Debug.TEXT);

        selectCursor(ic, CURSOR_BEGIN);

        undoString = string;
        modifyCalculatedCursor(calculatedCursor[0] + string.length());
        textBeforeCursor.sendString(string);
        textAfterCursor.invalidate();
        ic.commitText(string, 1);

        // TIMING EVENT
        softBoardData.characterCounter.measure(string.length());
        softBoardData.showTiming();

        Scribe.debug(Debug.TEXT, "Text was sent: " + string);
        }

    private boolean undoLastString( InputConnection ic )
        {
        if ( undoString != null ) // undoString can exist only, if there is no selection
            {
            if (isStoreTextEnabled() || textBeforeCursor.compare( ic, undoString )) // compare do reset also
                {
                sendDelete(ic, -undoString.length());
                return true;
                }
            // törlés már nem volt sikeres
            undoString = null;
            }
        return false;
        }

    private void sendDelete( InputConnection ic, int length )
        {
        Scribe.locus(Debug.TEXT);

        // kijelölésnél mindenképpen a kijelölést törli először
        if ( isSelected() )
            {
            selectCursor(ic, CURSOR_BEGIN);
            // no undoString in selection
            // no change in calculatedCursor[0]
            modifyCalculatedCursor(calculatedCursor[0]);
            // no change in textBeforeCursor
            textAfterCursor.invalidate();
            ic.commitText("", 1);

            Scribe.debug(Debug.TEXT, "Selected text was deleted.");
            }
        else if ( length != 0 )
            {
            undoString = null;
            if ( length < 0 )
                {
                modifyCalculatedCursor(calculatedCursor[0] + length);
                textBeforeCursor.sendDelete( -length);
                // ?? textAfterCursor.invalidate();
                ic.deleteSurroundingText( -length, 0);
                }
            else // AFTER
                {
                // calculatedCursorStart does not change
                modifyCalculatedCursor(calculatedCursor[0]);
                // textBeforeCursor
                textAfterCursor.sendDelete(length);
                ic.deleteSurroundingText( 0, length );
                }
            Scribe.debug(Debug.TEXT, "Text was deleted - " + length + " chars long.");
            }
        }

    /*
     * CURSOR_BEGIN/CURSOR_END
     */
    private void moveRelative(InputConnection ic, int cursor, int length, boolean select)
        {
        Scribe.locus(Debug.CURSOR);

        undoString = null;
        int otherCursor = (cursor + 1) & 1;
        if ( isSelected() && !select )
            // If length is 0, then it should be at the end
            // There is a selection in the other direction, so that way it cannot be 0
            {
            if ( length < 0 )
                {
                selectCursor(ic, CURSOR_BEGIN);
                modifyCalculatedCursor(calculatedCursor[0]);
                // textBeforeCursor no change;
                textAfterCursor.invalidate();
                Scribe.debug(Debug.CURSOR, "Cursor was moved to the beginning of the selection.");
                }
            else if ( length > 0 )
                {
                selectCursor(ic, CURSOR_END);
                modifyCalculatedCursor(calculatedCursor[1]);
                textBeforeCursor.invalidate();
                // textAfterCursor no change;
                Scribe.debug(Debug.CURSOR, "Cursor was moved to the end of the selection.");
                }
            else // length == 0
                {
                selectCursor(ic, cursor);
                modifyCalculatedCursor(calculatedCursor[cursor]);
                textBeforeCursor.invalidate(); // The remaining stored text should be empty
                textAfterCursor.invalidate();  // The other one should be invalidated
                Scribe.debug(Debug.CURSOR, "Cursor was moved to the beginning/end of the selection.");
                }
            }
        else
            {
            if (length == 0 )   return;

            Scribe.debug(Debug.CURSOR, "Cursor [" + cursor + "] is: " + calculatedCursor[cursor]);
            Scribe.debug(Debug.CURSOR, "Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);
            Scribe.error(Debug.CURSOR, "Length to move: " + length);
            selectCursor(ic, cursor);
            int overlap = 0;

            if (!select)
                {
                modifyCalculatedCursor(calculatedCursor[cursor] + length);
                }
            else
                {
                if ( cursor == CURSOR_BEGIN)
                    overlap = modifyCalculatedCursor(calculatedCursor[cursor] + length, calculatedCursor[otherCursor]);
                else
                    overlap = modifyCalculatedCursor(calculatedCursor[otherCursor], calculatedCursor[cursor] + length);

                if ( overlap > 0 )
                    modifyCalculatedCursor(calculatedCursor[cursor]); // cursor and otherCursor are switched!
                }

            Scribe.error(Debug.CURSOR, "Overlap: " + overlap);
            Scribe.debug(Debug.CURSOR, "Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);

            if (length < 0)
                {
                textBeforeCursor.sendDelete( - length - overlap );
                textAfterCursor.invalidate();
                }
            else
                {
                textBeforeCursor.invalidate();
                textAfterCursor.sendDelete( length - overlap );
                }

            }

        // some editors accept change only as second parameter. Why??
        ic.setSelection(calculatedCursor[otherCursor], calculatedCursor[cursor]);
        // ic.setSelection(calculatedCursor[0], calculatedCursor[1]);

        Scribe.debug(Debug.CURSOR, "Cursor [" + cursor + "] was moved. Length: " + length + " chars. Selection: " + select);
        Scribe.debug(Debug.CURSOR, "Cursor positions: " + calculatedCursor[0] + "-" + calculatedCursor[1]);
        }


    private void moveAbsolute(InputConnection ic, int cursor, int position, boolean select)
        {
        Scribe.locus(Debug.CURSOR);

        if ( position == calculatedCursor[cursor] )   return;

        undoString = null;

        Scribe.debug(Debug.CURSOR, "Cursor [" + cursor + "] is: " + calculatedCursor[cursor]);
        Scribe.debug(Debug.CURSOR, "Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);
        Scribe.error(Debug.CURSOR, "New position: " + position);

        int otherCursor = (cursor + 1) & 1;
        selectCursor(ic, cursor);
        if (!select)
            {
            modifyCalculatedCursor( position );
            }
        else
            {
            modifyCalculatedCursor(position, calculatedCursor[otherCursor]);
            }

        Scribe.debug(Debug.CURSOR, "Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);

        textBeforeCursor.invalidate();
        textAfterCursor.invalidate();

        // some editors accept change only as second parameter. Why??
        ic.setSelection(calculatedCursor[otherCursor], calculatedCursor[cursor]);
        // ic.setSelection(calculatedCursor[0], calculatedCursor[1]);
        }


    /*****************************************
     * CONNECTION TO SERVICE
     *****************************************/


    public SoftBoardProcessor( SoftBoardService softBoardService, SoftBoardData softBoardData )
        {
        this.softBoardService = softBoardService;
        this.softBoardData = softBoardData;

        softBoardData.connect(this);

        // Orientation should be checked, but index is 0 by default.
        // No setIndex() is needed
        this.softBoardData.boardTable.setOrientation();

        layoutView = new LayoutView( softBoardService );
        layoutView.setLayout(this.softBoardData.boardTable.getActiveLayout());

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



    /*****************************************
     * HIGHER LEVEL TEXT PROCESSING
     *****************************************/


    public void toggleCursor()
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if ( ic != null )
            {
            selectCursor(ic, (cursorLastMoved + 1) & 1);
            }
        }


    private int findLastPosition( InputConnection ic )
        {
        CharSequence temp;
        int position = calculatedCursor[1];
        // ic.setSelection( position, position ); not needed, selection is ready
        do
            {
            temp = ic.getTextAfterCursor(2048, 0);
            position += temp.length();
            ic.setSelection( position, position );
            } while (temp.length() == 2048);
        // original positions should be reset
        ic.setSelection(calculatedCursor[0], calculatedCursor[1]);
        return position;
        }


    public void selectAll()
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null && retrieveTextEnabled) // because of consistency with jumpBottom
            {
            ic.beginBatchEdit();
            moveAbsolute(ic, CURSOR_END, findLastPosition(ic), false);
            moveAbsolute(ic, CURSOR_BEGIN, 0, true);
            ic.endBatchEdit();
            }
        }


    // Only CURSOR_BEGIN
    public void jumpTop(boolean select)
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null && retrieveTextEnabled) // because of consistency with jumpBottom
            {
            ic.beginBatchEdit();
            moveAbsolute(ic, CURSOR_BEGIN, 0, select);
            ic.endBatchEdit();
            }
        }


    // Only CURSOR_END
    public void jumpBottom(boolean select)
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null && retrieveTextEnabled)
            {
            ic.beginBatchEdit();
            moveAbsolute(ic, CURSOR_END, findLastPosition(ic), select);
            ic.endBatchEdit();
            }
        }


    public void jumpLeft( int cursor, boolean select )
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if ( ic != null )
            {
            if ( cursor == CURSOR_LAST)
                {
                if ( !select )
                    cursor = CURSOR_BEGIN;
                else if ( isSelected() ) // && select
                    cursor = cursorLastMoved;
                else // text is NOT selected BUT select
                    cursor = CURSOR_BEGIN;
                }

            ic.beginBatchEdit();
            selectCursor(ic, cursor);

            int data = textBeforeCursor.read();
            int l = -1;

            if ( data < 0 )
                {
                l = 0 ;
                }
            else if ((data & 0xFC00) == 0xDC00)
                {
                Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                l--;
                }
            else if ((data & 0xFC00) == 0xD800)
                {
                Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                }

            moveRelative(ic, cursor, l, select);
            ic.endBatchEdit();
            }
        }

    public void jumpRight( int cursor, boolean select )
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if ( ic != null )
            {
            if ( cursor == CURSOR_LAST)
                {
                if ( !select )
                    cursor = CURSOR_BEGIN;
                else if ( isSelected() ) // && select
                    cursor = cursorLastMoved;
                else // text is NOT selected BUT select
                    cursor = CURSOR_END;
                }

            ic.beginBatchEdit();
            selectCursor(ic, cursor);

            int data = textAfterCursor.read();
            int l = 1;

            if ( data < 0 )
                {
                l = 0 ;
                }
            else if ((data & 0xFC00) == 0xDC00)
                {
                Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                }
            else if ((data & 0xFC00) == 0xD800)
                {
                Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                l++;
                }

            moveRelative(ic, cursor, l, select);
            ic.endBatchEdit();
            }
        }

    public void jumpWordLeft( int cursor, boolean select )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            if ( cursor == CURSOR_LAST)
                {
                if ( !select )
                    cursor = CURSOR_BEGIN;
                else if ( isSelected() ) // && select
                    cursor = cursorLastMoved;
                else // text is NOT selected BUT select
                    cursor = CURSOR_BEGIN;
                }

            ic.beginBatchEdit();
            selectCursor(ic, cursor);

            Scribe.debug( "Cursor: " + cursor );
            Scribe.debug("Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);
            Scribe.debug("Stored text before cursor: " + textBeforeCursor.toString());

            int offset = 0;
            int c;

            while (StringUtils.isWhiteSpace(c = getTextBeforeCursor().read()) ) // -1 is NOT whitespace !!
                {
                offset--;
                }
            while ( !StringUtils.isWhiteSpace(c) && c != -1 )
                {
                offset--;
                c = getTextBeforeCursor().read();
                }

            Scribe.debug( "Offset: " + offset );
            moveRelative( ic, cursor, offset, select );
            ic.endBatchEdit();
            }
        }

    public void jumpWordRight( int cursor, boolean select )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            if ( cursor == CURSOR_LAST)
                {
                if ( !select )
                    cursor = CURSOR_BEGIN;
                else if ( isSelected() ) // && select
                    cursor = cursorLastMoved;
                else // text is NOT selected BUT select
                    cursor = CURSOR_END;
                }

            Scribe.debug("Cursor: " + cursor);
            Scribe.debug("Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);
            Scribe.debug( "Stored text before cursor: " + textBeforeCursor.toString() );

            ic.beginBatchEdit();
            selectCursor(ic, cursor);

            int offset = 0;
            int c;

            while ( StringUtils.isWhiteSpace(c = getTextAfterCursor().read()) ) // -1 is NOT whitespace !!
                {
                offset++;
                }
            while ( !StringUtils.isWhiteSpace(c) && c != -1 )
                {
                offset++;
                c = getTextAfterCursor().read();
                }
            Scribe.debug( "Offset: " + offset );
            moveRelative( ic, cursor, offset, select );
            ic.endBatchEdit();
            }
        }


    public void jumpParaLeft(int cursor, boolean select)
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            if ( cursor == CURSOR_LAST)
                {
                if ( !select )
                    cursor = CURSOR_BEGIN;
                else if ( isSelected() ) // && select
                    cursor = cursorLastMoved;
                else // text is NOT selected BUT select
                    cursor = CURSOR_BEGIN;
                }

            ic.beginBatchEdit();
            selectCursor(ic, cursor);

            Scribe.debug( "Cursor: " + cursor );
            Scribe.debug("Calculated cursor: " + calculatedCursor[0] + "-" + calculatedCursor[1]);
            Scribe.debug("Stored text before cursor: " + textBeforeCursor.toString());

            int offset = 0;
            int c;

            while ( StringUtils.isWhiteSpace(c = getTextBeforeCursor().read()) ) // -1 is NOT whitespace !!
                {
                offset--;
                }

            while ( c != '\n' && c != -1 )
                {
                offset--;
                c = getTextBeforeCursor().read();
                }

            Scribe.debug( "Offset: " + offset );

            moveRelative( ic, cursor, offset, select );
            ic.endBatchEdit();
            }
        }


    public void jumpParaRight(int cursor, boolean select)
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            if ( cursor == CURSOR_LAST)
                {
                if ( !select )
                    cursor = CURSOR_BEGIN;
                else if ( isSelected() ) // && select
                    cursor = cursorLastMoved;
                else // text is NOT selected BUT select
                    cursor = CURSOR_END;
                }

            ic.beginBatchEdit();
            selectCursor(ic, cursor);

            int offset = 0;
            int c;

            while ( StringUtils.isWhiteSpace(c = getTextAfterCursor().read()) ) // -1 is NOT whitespace !!
                {
                offset++;
                }

            while ( c!='\n' && c != -1 )
                {
                offset++;
                c = getTextAfterCursor().read();
                }

            moveRelative( ic, cursor, offset, select );
            ic.endBatchEdit();
            }
        }


    private int sendDeleteSpacesBeforeCursor( InputConnection inputConnection )
        {
        int space;

        selectCursor(inputConnection, CURSOR_BEGIN);
        for ( space = 0; textBeforeCursor.read() == ' '; space++ ) ;

        sendDelete(inputConnection, -space);

        Scribe.debug(Debug.TEXT, "Spaces deleted before cursor: " + space);
        return space;
        }


    private int sendDeleteSpacesAfterCursor( InputConnection inputConnection )
        {
        int space;

        selectCursor(inputConnection, CURSOR_BEGIN);
        for ( space = 0; textAfterCursor.read() == ' '; space++) ;

        sendDelete(inputConnection, space);

        Scribe.debug(Debug.SERVICE, "Spaces deleted after cursor: " + space);
        return space;
        }


    public void sendString( String string, int autoSpace )
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();

            if ( (autoSpace & PacketText.ERASE_SPACES_BEFORE) != 0 && softBoardData.autoFuncEnabled)
                {
                sendDeleteSpacesBeforeCursor( ic );
                }
            if ( (autoSpace & PacketText.ERASE_SPACES_AFTER) != 0 && softBoardData.autoFuncEnabled)
                {
                sendDeleteSpacesAfterCursor( ic );
                }

            sendBuilder.setLength(0);
            if ( (autoSpace & PacketText.AUTO_SPACE_BEFORE) != 0 && softBoardData.autoFuncEnabled)
                {
                textBeforeCursor.reset();
                if ( !StringUtils.isWhiteSpace(textBeforeCursor.read()) )
                    sendBuilder.append(' ');
                }

            sendBuilder.append(string);

            if ( (autoSpace & PacketText.AUTO_SPACE_AFTER) != 0 && softBoardData.autoFuncEnabled)
                {
                textAfterCursor.reset();
                if ( !StringUtils.isSpace(textAfterCursor.read()) )
                    sendBuilder.append(' ');
                }

            sendString(ic, sendBuilder.toString());

            ic.endBatchEdit();
            }
        }


    public boolean undoLastString()
        {
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if ( ic != null )
            {
            return undoLastString( ic );
            }
        return false;
        }


    public void changeStringBeforeCursor( String string )
        {
        changeStringBeforeCursor(string.length(), string);
        }


    public void changeStringBeforeCursor( int length, String string )
        {
        Scribe.locus(Debug.SERVICE);

        if ( !isSelected() )
            {
            InputConnection ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                ic.beginBatchEdit();
                sendDelete(ic, -length);
                sendString(ic, string);
                ic.endBatchEdit();
                }
            }
        }


    @Override
    public boolean sendDefaultEditorAction(boolean fromEnterKey)
        {
        return softBoardService.sendDefaultEditorAction( fromEnterKey );
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

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();
            // text is selected
            if (isSelected())
                {
                sendDelete(ic, -1);
                }

            // text is not selected
            else
                {
                selectCursor(ic, CURSOR_BEGIN);

                int data = textBeforeCursor.read();
                int l = 1;

                Scribe.debug(Debug.TEXT, "Character before cursor: " + Integer.toHexString(data));
                if (data < 0)
                    {
                    Scribe.debug(Debug.TEXT, "No more text to delete, hard DEL is sent!");

                    softBoardData.layoutStates.forceBinaryHardState(0x15);
                    sendKeyDownUp(KeyEvent.KEYCODE_DEL);
                    softBoardData.layoutStates.clearBinaryHardState();
                    }
                else
                    {
                    if ((data & 0xFC00) == 0xDC00)
                        {
                        Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                        l++;
                        }
                    else if ((data & 0xFC00) == 0xD800)
                        {
                        Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                        }
                    sendDelete(ic, -l);
                    }
                }
            ic.endBatchEdit();
            }
        }


    // DO DELETE
    public void deleteCharAfterCursor(int n)
        {
        Scribe.locus(Debug.SERVICE);

        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();
            // text is selected
            if ( isSelected() )
                {
                sendDelete(ic, -1);
                }

            // text is not selected
            else
                {
                selectCursor(ic, CURSOR_BEGIN);
                int data = textAfterCursor.read();
                int l = 1;

                Scribe.debug(Debug.TEXT, "Character after cursor: " + Integer.toHexString(data));
                if (data < 0)
                    {
                    Scribe.debug(Debug.TEXT, "No more text to delete, hard FORWARD_DEL is sent!");

                    softBoardData.layoutStates.forceBinaryHardState(0x15);
                    sendKeyDownUp(KeyEvent.KEYCODE_FORWARD_DEL);
                    softBoardData.layoutStates.clearBinaryHardState();
                    }
                else
                    {
                    if ((data & 0xFC00) == 0xD800)
                        {
                        Scribe.debug(Debug.TEXT, "Unicode (2 bytes) delete!");
                        l++;
                        }
                    else if ((data & 0xFC00) == 0xDC00)
                        {
                        Scribe.error(Debug.TEXT, "Deleting unicode lower part!");
                        }
                    sendDelete(ic, l);
                    }
                }
            ic.endBatchEdit();
            }
        }


    /*****************************************
     * HARD-KEY PROCESSING
     *****************************************/


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

        return sendKeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyEventCode);
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
    public CharSequence getTextBeforeCursor( InputConnection ic, int n )
        {
        CharSequence text = null;
        if (retrieveTextEnabled)
            {
            if ( ic == null )   ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                ic.setSelection( calculatedCursor[cursorLastMoved], calculatedCursor[cursorLastMoved] );
                text = ic.getTextBeforeCursor(n, 0);
                ic.setSelection( calculatedCursor[0], calculatedCursor[1] );
                }
            }
        return text;
        }


    /**
     * Gets text after cursor - needed only by StoredText
     * Text can be retrieved only, if text is NOT selected
     * @param n number of java chars to get
     * @return text or null, if no text is available (or text is selected)
     */
    public CharSequence getTextAfterCursor( InputConnection ic, int n )
        {
        CharSequence text = null;
        if (retrieveTextEnabled)
            {
            if ( ic == null )   ic = softBoardService.getCurrentInputConnection();
            if (ic != null)
                {
                ic.setSelection( calculatedCursor[cursorLastMoved], calculatedCursor[cursorLastMoved] );
                text = ic.getTextAfterCursor(n, 0);
                ic.setSelection( calculatedCursor[0], calculatedCursor[1] );
                }
            }
        return text;
        }


    private boolean lastIsSelected = true;
    private int lastWordLengthBeforeCursor = -1;
    private int lastWordLengthAfterCursor = -1;


    public String getWordOrSelected()
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            if ( isSelected() )
                {
                lastIsSelected = true;

                CharSequence text = ic.getSelectedText(0); // can be null
                if ( text != null )
                    return text.toString();
                }

            else
                {
                lastIsSelected = false;

                ic.beginBatchEdit();
                StringBuilder builder = new StringBuilder();
                String string;
                int c;

                textBeforeCursor.reset();
                lastWordLengthBeforeCursor = 0;
                while (!StringUtils.isWhiteSpace(c = getTextBeforeCursor().read()) && c != -1) // -1 is NOT whitespace !!
                    {
                    builder.insert(0, (char) c);
                    lastWordLengthBeforeCursor ++;
                    }

                textAfterCursor.reset();
                lastWordLengthAfterCursor = 0;
                while ( !StringUtils.isWhiteSpace(c = getTextAfterCursor().read()) && c != -1) // -1 is NOT whitespace !!
                    {
                    builder.append( (char)c);
                    lastWordLengthAfterCursor++;
                    }

                string = builder.toString();
                Scribe.debug( "Word around cursor: " + string );
                ic.endBatchEdit();
                return string;
                }
            }
        return "";
        }


    public void changeLastWordOrSelected( String newText, boolean restoreCursor )
        {
        Scribe.locus(Debug.SERVICE);
        InputConnection ic = softBoardService.getCurrentInputConnection();
        if (ic != null)
            {
            ic.beginBatchEdit();

            if (!lastIsSelected) // word should be deleted
                {
                sendDelete(ic, -lastWordLengthBeforeCursor);
                sendDelete(ic, lastWordLengthAfterCursor);
                }

            sendString(ic, newText);

            if (restoreCursor)
                {
                if (lastIsSelected)
                    {
                    modifyCalculatedCursor(calculatedCursor[0] - newText.length(), calculatedCursor[1]);
                    textBeforeCursor.sendDelete(newText.length());
                    }
                else
                    {
                    modifyCalculatedCursor(calculatedCursor[0] - lastWordLengthAfterCursor);
                    textBeforeCursor.sendDelete(lastWordLengthAfterCursor);
                    }
                }
            ic.endBatchEdit();
            }
        }

    }

package org.lattilad.bestboard.monitorrow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * https://stackoverflow.com/questions/4921507/how-to-close-activity-with-software-keyboard-after-back-press
 */


public class EditTextWithBackButton extends EditText
    {
    public interface OnBackButtonListener
        {
        boolean onBackButton();
        }

    public EditTextWithBackButton(Context context)
        {
        super(context);
        }

    public EditTextWithBackButton(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public void setOnBackButtonListener(OnBackButtonListener onBackButtonListener)
        {
        this.onBackButtonListener = onBackButtonListener;
        }

    OnBackButtonListener onBackButtonListener;

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
        {
        if (event.getAction()==KeyEvent.ACTION_UP && keyCode==KeyEvent.KEYCODE_BACK)
            {
            if (onBackButtonListener !=null)
                return onBackButtonListener.onBackButton();
            }
        return super.onKeyPreIme(keyCode, event);
        }
    }


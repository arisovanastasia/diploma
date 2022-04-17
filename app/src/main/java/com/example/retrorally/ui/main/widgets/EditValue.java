package com.example.retrorally.ui.main.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.Serializable;

public abstract class EditValue<V extends Serializable> extends AppCompatEditText {
	Context mContext;
	V value;
	boolean undo = false;
	boolean sendDone = true;

	public EditValue(Context context) {
		super(context);
		
		mContext = context;
	}
	
	public EditValue(Context context, AttributeSet attrs) {
        super(context, attrs);
        
		mContext = context;
    }

    public EditValue(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
		mContext = context;
    }

	@Override
	public Parcelable onSaveInstanceState(){
		Bundle b = new Bundle();
		b.putParcelable("super", super.onSaveInstanceState());
		b.putSerializable("payload", value);
		return b;
	}

	@Override
	public void onRestoreInstanceState(Parcelable b){
		if(b instanceof Bundle){
			try {
				value = (V) ((Bundle) b).getSerializable("payload");
			} catch(Exception e){
				value = null;
			}
			b = ((Bundle) b).getParcelable("super");
		}

		super.onRestoreInstanceState(b);
	}

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			undo = true;
			clearFocus();
			if(mContext != null) {
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
        }
        
        return super.onKeyPreIme(keyCode, event);
    }
    
    @Override
    public void onEditorAction(int actionCode){
    	if (actionCode == EditorInfo.IME_ACTION_DONE) {
			sendDone = false;
			clearFocus();
			if(mContext != null) {
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
    	
    	super.onEditorAction(actionCode);
    }

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect){
		if(!gainFocus){
			if(!undo){
				updateValueFromText();

				if(sendDone) {
					super.onEditorAction(EditorInfo.IME_ACTION_DONE);
				}
			}

			if(value != null){
				setText(toText(value));
			}

			undo = false;
			sendDone = true;
		}

		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	public void setValue(V v){
		if(value == null || !value.equals(v)) {
			value = v;

			if (!isInputMethodTarget()) {
				setText(toText(v));
			}
		}
	}

	public V getValue(){
		return value;
	}

	public void updateValueFromText(){
		// maybe add a check to see if anything was actually changed?
		String t = getText().toString();

		if(!t.equals(toText(value))) {
			V v = fromText(t);
			if (v != null || t.isEmpty()) {
				setValue(v);
			}
		}
	}

	public abstract String toText(V v);
	public abstract V fromText(String s);
}
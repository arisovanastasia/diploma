package com.example.retrorally.ui.main.widgets;

import android.content.Context;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

public class EditInteger extends EditValue<Integer> {
	public EditInteger(Context context) {
		super(context);

		setOptions();
	}

	public EditInteger(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOptions();
    }

    public EditInteger(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setOptions();
    }

	public String toText(Integer v){
		if(v != null) {
			return v.toString();
		}

		return "";
	}

	public Integer fromText(String s){
		if(s.length() != 0){
			try {
				return Integer.parseInt(s);
			} catch(NumberFormatException e) {
				return null;
			}
		}

		return null;
	}

    private void setOptions(){
    	setImeOptions(EditorInfo.IME_ACTION_DONE);
    	setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    	setTransformationMethod(SingleLineTransformationMethod.getInstance());
    	setHorizontallyScrolling(true);
    	setKeyListener(DigitsKeyListener.getInstance("1234567890"));
    }
}
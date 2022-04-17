package com.example.retrorally.ui.main.widgets;

import com.example.retrorally.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

public class EditTime extends EditValue<Integer> {
	private boolean allowPlus = false;
	private boolean showSeconds = false;
	
	public EditTime(Context context) {
		super(context);
		
		setOptions();
	}
	
	public EditTime(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        setOptions();
		setStyle(context, attrs);
    }

    public EditTime(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setOptions();
		setStyle(context, attrs);
    }
    
    public Integer fromText(String t){
		if(t.length() != 0){
			int hours = 0, minutes = 0, seconds = 0;

			if(allowPlus && t.charAt(0) == '+') {
				t = t.substring(1);
			}
			
			try{
				if(t.length() == 1 || t.length() == 2){ // ч, чч
					hours = Integer.parseInt(t);
				} else if(t.length() == 3){ // чмм
					hours = Integer.parseInt(t.substring(0, 1));
					minutes = Integer.parseInt(t.substring(1, 3));
				} else if(t.length() == 4 && t.charAt(1) == ':'){ // ч:мм
					hours = Integer.parseInt(t.substring(0, 1));
					minutes = Integer.parseInt(t.substring(2, 4));
				} else if(t.length() == 4 && t.charAt(1) != ':'){ // ччмм
					hours = Integer.parseInt(t.substring(0, 2));
					minutes = Integer.parseInt(t.substring(2, 4));
				} else if(t.length() == 5 && t.charAt(2) == ':'){ // чч:мм
					hours = Integer.parseInt(t.substring(0, 2));
					minutes = Integer.parseInt(t.substring(3, 5));
				} else if(t.length() == 5 && t.charAt(2) != ':'){ // чммсс
					hours = Integer.parseInt(t.substring(0, 1));
					minutes = Integer.parseInt(t.substring(1, 3));
					seconds = Integer.parseInt(t.substring(3, 5));
				} else if(t.length() == 6){ // ччммсс
					hours = Integer.parseInt(t.substring(0, 2));
					minutes = Integer.parseInt(t.substring(2, 4));
					seconds = Integer.parseInt(t.substring(4, 6));
				} else if(t.length() == 7){ // ч:мм:сс
					hours = Integer.parseInt(t.substring(0, 1));
					minutes = Integer.parseInt(t.substring(2, 4));
					seconds = Integer.parseInt(t.substring(5, 7));
				} else if(t.length() == 8){ // чч:мм:сс
					hours = Integer.parseInt(t.substring(0, 2));
					minutes = Integer.parseInt(t.substring(3, 5));
					seconds = Integer.parseInt(t.substring(6, 8));
				} else {
					return null;
				}
			} catch (NumberFormatException e) {
				return null;
			}
			
			if(hours < 0|| hours > 23 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59){
				return null;
			}
			
			return seconds + minutes * 60 + hours * 3600;
		} 
		
		return null;
	}

    public String toText(Integer time){
		if(time == null || time == -1){
			return "";
		}

    	int hours = time / 3600;
		int ms = time % 3600;
		int minutes = ms / 60;
		int seconds = ms % 60;

		if(!showSeconds) {
			return String.format(allowPlus ? "+%02d:%02d" : "%02d:%02d", hours, minutes);
		} else {
			return String.format(allowPlus ? "+%02d:%02d:%02d" : "%02d:%02d:%02d", hours, minutes, seconds);
		}
    }
    
    private void setOptions(){
    	setImeOptions(EditorInfo.IME_ACTION_DONE);
    	setRawInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
    	setTransformationMethod(SingleLineTransformationMethod.getInstance());
    	setHorizontallyScrolling(true);
    }

	private void setStyle(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.CustomEdits,
				0, 0);

		try {
			allowPlus = a.getBoolean(R.styleable.CustomEdits_allowPlus, false);
			showSeconds = a.getBoolean(R.styleable.CustomEdits_showSeconds, false);
		} finally {
			a.recycle();
		}
	}
}
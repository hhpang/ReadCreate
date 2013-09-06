package com.didithemouse.alfa;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

class RecordButton extends ToggleButton {
    private ShareActivity sa;

    /*OnTouchListener clicker = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			ToggleButton tbutton = (ToggleButton) v;
						
			if (!tbutton.isChecked()) {
				sa.onRecord(true);				
			}
			else{
				sa.onRecord(false);
			}
            return true;
		}
    };*/

    public RecordButton(Context ctx) {
        super(ctx);
        //setOnTouchListener(clicker);
    }
    
    public RecordButton(Context context, AttributeSet attrs) {
    	  super(context, attrs);
    	  // TODO Auto-generated constructor stub
    	  //initStyleButton(attrs);
    	  //setOnTouchListener(clicker);
    	 }

	 public RecordButton(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	  // TODO Auto-generated constructor stub
	  //initStyleButton(attrs);
	  	//setOnTouchListener(clicker);
	 }
    	 

	public ShareActivity getSa() {
		return sa;
	}

	public void setSa(ShareActivity sa) {
		this.sa = sa;
	}
}

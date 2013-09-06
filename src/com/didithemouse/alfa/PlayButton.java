package com.didithemouse.alfa;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ToggleButton;

class PlayButton extends ToggleButton {
    boolean mStartPlaying = true;
    private ShareActivity sa;

    OnClickListener clicker = new OnClickListener() {
        public void onClick(View v) {
            sa.onPlay(mStartPlaying);
            if (mStartPlaying) {
                //setText("Stop playing");
            } else {
                //setText("Start playing");
            }
        }
    };

    public PlayButton(Context ctx) {
        super(ctx);
        setOnClickListener(clicker);
    }
    
    public PlayButton(Context context, AttributeSet attrs) {
  	  super(context, attrs);
  	  // TODO Auto-generated constructor stub
  	  //initStyleButton(attrs);
	    setOnClickListener(clicker);
  	 }

	 public PlayButton(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	  // TODO Auto-generated constructor stub
	  //initStyleButton(attrs);
	      setOnClickListener(clicker);
	 }
    
    public ShareActivity getSa() {
		return sa;
	}

	public void setSa(ShareActivity sa) {
		this.sa = sa;
	}
}

package com.didithemouse.alfa;

import java.io.IOException;
import java.util.ArrayList;


import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class PresentationActivity extends Activity{
	
	//private FrameLayout big = null;
	private FrameLayout content = null;
//	private LinearLayout ll = null;
	private int slideNumber = 1;
	private ArrayList<DropPanelWrapper> panels;
	private ImageView drawing = null;
//	private boolean playing;
	private MediaPlayer   mPlayer = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.presentation);  
//		ll = (LinearLayout) findViewById(R.id.bigparent);
		//big = (FrameLayout) findViewById(R.id.share_big_layout);
		content = (FrameLayout) findViewById(R.id.panel_content);
		drawing = (ImageView) findViewById(R.id.bitmapDraw);
		content.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		panels = MochilaContents.getInstance().getDropPanels();
		
		ViewTreeObserver vto = content.getViewTreeObserver(); 
	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
	    	@Override 
	        public void onGlobalLayout() { 
	    		ShowContent();
	    		content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	    	}
	    });
	}
	
	 public void ShowContent() {
	    	content.removeAllViews();
			DropPanelWrapper p1 = getPanel();
	    	ArrayList<ViewWrapper> wrappers = p1.getWrappers();
			for(ViewWrapper w : wrappers){
				View iv = null;
				if(w.getView(getApplicationContext())!=null) iv = (View)w.getView(getApplicationContext());
				int left=0, top=0;
				if(iv instanceof ImageView){
					ImageView img = new ImageView(this.getApplicationContext());
					img.setImageDrawable(((ImageView)iv).getDrawable());
					left = (int)(w.getX()*content.getWidth());
					top = (int)(w.getY()*content.getHeight());
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)(img.getDrawable().getIntrinsicWidth()*3f), (int)(img.getDrawable().getIntrinsicHeight()*3f));
					lp.leftMargin = left;
					lp.topMargin = top;
					content.addView(img, lp);
				}
				else if(iv instanceof TextView){
					TextView tv = (TextView)iv;
					TextView tv2 = new TextView(this.getApplicationContext());
					tv2.setText(tv.getText());
					int newSize = 60;
					tv2.setTextSize(newSize);
					left = (int)(w.getX()*content.getWidth());
					top = (int)(w.getY()*content.getHeight());
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
					lp.leftMargin = left;
					lp.topMargin = top;
					content.addView(tv2, lp);
				}
			}
			drawing.setImageDrawable(p1.getPanelView(this).getBigBitmap());
			content.addView(drawing);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			//iniciamos el audio correspondiente
			
			startPlaying();
	}
	 
	 private void startPlaying() {
	        mPlayer = new MediaPlayer();
	        try {
//	        	playing = true;
	        	mPlayer.setOnCompletionListener(new OnCompletionListener() {
	        		@Override
					public void onCompletion(MediaPlayer arg0) {
						waitTransition(arg0.getDuration());
						stopPlaying();
						slideNumber++;
						if(slideNumber<=MochilaContents.numPaneles) ShowContent();
						else
							terminate();
					}
	        	});
	            
	            mPlayer.setDataSource(getPanel().getFileName());
	            mPlayer.prepare();
	            mPlayer.start();
	            
	            //
	            
	        }
	        catch (IOException e) {}
	}
	
	 private void waitTransition(int d) {
		 try {
			Thread.sleep((int)(3*Math.exp(-d)+2)*1000);
		} catch (InterruptedException e) {
		}
		 
	}

	private void stopPlaying() {
	        if(mPlayer != null) {
	        	mPlayer.stop();
	        	mPlayer.release();
	        }
//	        playing = false;
	    } 
	 
	public DropPanelWrapper getPanel() {
    	for(int i=0; i< panels.size(); i++) {
    		if(panels.get(i).getIndex() == (slideNumber)) {
    			return panels.get(i);
    		}
    	}
    	return null;
    }
    public DropPanelWrapper getPanel(int number) {
    	for(int i=0; i< panels.size(); i++) {
    		if(panels.get(i).getIndex() == (number)) {
    			return panels.get(i);
    		}
    	}
    	return null;
    }
    
    private void terminate()
    {
    	Intent i = new Intent(this, EndingActivity.class);
    	LogX.i("Share","Ha finalizado la aplicación.");

		startActivity(i);
		finish();
    }
    
    @Override
	public void onBackPressed() {
	}
	
}

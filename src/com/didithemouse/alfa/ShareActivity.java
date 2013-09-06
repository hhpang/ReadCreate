package com.didithemouse.alfa;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;



public class ShareActivity extends Activity
{
    private static final String LOG_TAG = "AudioRecordTest";
    private String mFileName = null;
    private RecordButton mRecordButton = null;    
    private ImageView redHover = null, greenHover = null, cuentaText = null;
    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    private FrameLayout big = null, content = null;
    private ImageView drawing = null;
    private int slideNumber = 0;
    private Button prev, next, terminar;
    private boolean[] recorded;
    private ArrayList<DropPanelWrapper> panels;
    private boolean playing = false;
    private boolean recording = false;
    
    public ShareActivity() {
    	recorded = new boolean[MochilaContents.numPaneles+1];
    	for(int i=0; i<=MochilaContents.numPaneles; i++) {
    		recorded[i] = false;
    	}
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
    public void NextSlide() {
    	if(slideNumber<MochilaContents.numPaneles) {
    		slideNumber++;
    		ShowContent();
    	}
    	if(slideNumber==MochilaContents.numPaneles) {
    		next.setVisibility(View.INVISIBLE);
    	}
    	else {
    		next.setVisibility(View.VISIBLE);
    		prev.setVisibility(View.VISIBLE);
    	}
    }
    public void PrevSlide() {
    	if(slideNumber>1) {
    		slideNumber--;
    		ShowContent();
    	}
    	if(slideNumber==1) {
    		prev.setVisibility(View.INVISIBLE);
    	}
    	else {
    		prev.setVisibility(View.VISIBLE);
    		next.setVisibility(View.VISIBLE);
    	}
    }
    
    public void ShowContent() {
    	ObjectAnimator anim = ObjectAnimator.ofFloat(content, "alpha", 0.6f, 1f);
    	anim.setDuration(100);
    	anim.start();
    	content.removeAllViews();
		DropPanelWrapper p1 = getPanel();
    	ArrayList<ViewWrapper> wrappers = p1.getWrappers();
		for(ViewWrapper w : wrappers){	
			View iv = (View)w.getView(getApplicationContext());
			if(iv instanceof ImageView){
    			ImageView v = (ImageView)iv;	
    			ImageView ex = new ImageView(getApplicationContext());
    			ex.setImageDrawable(v.getDrawable());
    			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)(ex.getDrawable().getIntrinsicWidth()*2.3f), (int)(ex.getDrawable().getIntrinsicHeight()*2.3f), Gravity.TOP | Gravity.LEFT);
    			lp.leftMargin = (int)(w.getX()*768);
    			lp.topMargin = (int)(w.getY()*520);		
    			content.addView(ex, lp);
			}
			else if(iv instanceof TextView){
				TextView tv = (TextView)iv;
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT);	
				lp.leftMargin = (int)(w.getX()*768);
    			lp.topMargin = (int)(w.getY()*520);	
    			TextView tv2 = new TextView(this.getApplicationContext());
    			tv2.setText(tv.getText());
    			tv2.setTextSize(45);
				content.addView(tv2, lp);
			}
		}
    	ShowPanelHover();
    	drawing.setImageDrawable(p1.getPanelView(this).getBigBitmap());
		content.addView(drawing);
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	
    }
    
    public void ShowPanelHover() {
    	if(recorded[slideNumber]) {
    		redHover.setAlpha(0.5f);
    		greenHover.setAlpha(1f);
    		redHover.setVisibility(4);
    		greenHover.setVisibility(0);
    		mPlayButton.setVisibility(0);
    	}
    	else {
    		redHover.setAlpha(0.5f);
    		greenHover.setAlpha(1f);
    		redHover.setVisibility(0);
    		greenHover.setVisibility(4);
    		mPlayButton.setVisibility(4);
    	}
    }
    
    public boolean isReady() {
    	boolean res = true;
    	for(int i=1; i<=MochilaContents.numPaneles && res; i++) {
    		res = recorded[i];
    	}
    	return res;
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.share);
        drawing = (ImageView) findViewById(R.id.drawing_panel);
        panels = MochilaContents.getInstance().getDropPanels();
        big = (FrameLayout) findViewById(R.id.share_big_layout);
        big.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
        content = (FrameLayout) findViewById(R.id.panel_content);
        mRecordButton = (RecordButton) findViewById(R.id.grabar);
        mPlayButton = (PlayButton) findViewById(R.id.reproducir);
        redHover = (ImageView) findViewById(R.id.red_hover);
        cuentaText = (ImageView) findViewById(R.id.red_hover_text);
        greenHover = (ImageView) findViewById(R.id.green_hover);
        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        terminar = (Button) findViewById(R.id.terminar);
        
        mPlayButton.setVisibility(4);
        greenHover.setVisibility(4);
        mRecordButton.setSa(this);
        mPlayButton.setSa(this);
        
        for(int i=1; i<=MochilaContents.numPaneles; i++) {
    		recorded[i] = getPanel(i).isRecorded();
    	}
        
		Button buttonback = (Button) findViewById(R.id.buttonback);
		buttonback.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Intent i = new Intent(v.getContext(), PowerPointActivity.class);
				LogX.i("Share","El instructor ha vuelto atras.");

				startActivity(i);
				finish();
				return true;
			}
		});
        
        prev.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
            	if(!recording && !playing)
            	 {
            		PrevSlide(); LogX.i("Share","Se ha cambiado de diapositiva (retrocede).");
            	 }
            }
        });
        next.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
            	if(!recording && !playing)
            	{
            		NextSlide(); LogX.i("Share","Se ha cambiado de diapositiva (avanza).");
            	}
            }
        });
        
		
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(flag){
					flag = false;
					Intent i = new Intent(v.getContext(), PresentationActivity.class);
					LogX.i("Share","Se verá la presentacion.");

					Saver.savePresentation(Saver.ActivityEnum.SHARE1);
					startActivity(i);
					finish();
				}
			}
		});
		terminar.setVisibility(View.GONE);
        
		prev.setVisibility(View.INVISIBLE);
		
		ViewTreeObserver vto = redHover.getViewTreeObserver(); 
	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
	        @Override 
	        public void onGlobalLayout() { 
	        	slideNumber++;
	    		ShowContent();
	            redHover.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        } 
	    });
      
    }
    
    public void onRecord(boolean start) {
    	if(playing) return;
        if (start) {
        	LogX.i("Share","Se ha comenzado a grabar.");
        	mFileName = MochilaContents.getInstance().getDirectory();
            mFileName += "/audioSlide_"+panels.get(slideNumber-1).getID()+".wav";
        	
            cuentaText.setVisibility(4);
            ObjectAnimator.ofFloat(redHover, "alpha", 0f).setDuration(200).start();
            ObjectAnimator.ofFloat(greenHover, "alpha", 0f).setDuration(200).start();
            ObjectAnimator.ofFloat(mPlayButton, "alpha", 0f).setDuration(200).start();
            startRecording();
            recorded[slideNumber] = true;
            panels.get(slideNumber-1).setRecorded(true,mFileName);
            prev.getBackground().setColorFilter(0xC0808080, Mode.MULTIPLY);
            next.getBackground().setColorFilter(0xC0808080, Mode.MULTIPLY);
            if (isReady()) {
            	terminar.setVisibility(View.VISIBLE);
            }
        } else {
            stopRecording();
            LogX.i("Share","Se ha terminado de grabar.");
            prev.getBackground().setColorFilter(null);
            next.getBackground().setColorFilter(null);
            greenHover.setVisibility(0);
            ObjectAnimator.ofFloat(greenHover, "alpha", 1f).setDuration(200).start();
            mPlayButton.setVisibility(0);
            ObjectAnimator.ofFloat(mPlayButton, "alpha", 1f).setDuration(200).start();
        }
    }

    public void onPlay(boolean start) {
        if (start) {
            startPlaying();
            ObjectAnimator.ofFloat(greenHover, "alpha", 0f).setDuration(200).start();
            prev.getBackground().setColorFilter(0xC0808080, Mode.MULTIPLY);
            next.getBackground().setColorFilter(0xC0808080, Mode.MULTIPLY);
        } else {
            stopPlaying();
            ObjectAnimator.ofFloat(greenHover, "alpha", 1f).setDuration(200).start();
        }
    }

    private void startPlaying() {
    	if(playing)
    		return;
    	LogX.i("Share","Se ha reproducido la diapositiva.");
        mPlayer = new MediaPlayer();
        try {

        	playing = true;
        	mPlayer.setOnCompletionListener(new OnCompletionListener() {
        		@Override
				public void onCompletion(MediaPlayer arg0) {
					stopPlaying();
		            ObjectAnimator.ofFloat(greenHover, "alpha", 1f).setDuration(200).start();
				}
        	});
            mPlayer.setDataSource(getPanel().getFileName());
            mPlayer.prepare();
            mPlayer.start();

            
        } catch (IOException e) {
        	onPlay(false);
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        if(mPlayer != null && mPlayer.isPlaying()) {
        	mPlayer.stop();
        	mPlayer.release();
        }
        playing = false;
        prev.getBackground().setColorFilter(null);
        next.getBackground().setColorFilter(null);
    }

    RawRecorder rawRecorder;
    Runnable runWhenStop;
    private void startRecording() {
    	recording = true;
    	rawRecorder = new RawRecorder(mFileName);
    	rawRecorder.record();
    	
    	runWhenStop = new Runnable() {
			
			@Override
			public void run() {
				mRecordButton.setChecked(false);
				onRecToggleClicked(mRecordButton);				
			}
		};
    	rawRecorder.setOnFinishListener(new Runnable() {
			
			@Override
			public void run() {
				terminar.getHandler().post(runWhenStop);		
			}
		});
    }
    
    private void stopRecording() {
    	recording = false;
    	rawRecorder.stop();
    	rawRecorder = null;
        /*
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        */

    }


    @Override
    public void onPause() {
        super.onPause();
        if (rawRecorder != null)
        {
        	rawRecorder.stop();
        	rawRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    
    public void onRecToggleClicked(View v) {
    	ToggleButton b = (ToggleButton) v;
    	if(playing) 
    	{
    		b.setChecked(false);
    		return;
    	}
    	
        if (b.isChecked()) {            
        	onRecord(true);
        	b.setBackgroundResource(R.drawable.rec2p);
        } else {
        	onRecord(false);
        	b.setBackgroundResource(R.drawable.rec2);
        }
    }
    @Override
	public void onBackPressed() {
	}
    
}

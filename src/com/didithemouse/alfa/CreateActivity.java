package com.didithemouse.alfa;

import java.util.ArrayList;

import com.didithemouse.alfa.MyAbsoluteLayout.LayoutParams;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateActivity extends Activity implements OnTouchListener{

	private DragController dragController;
	private DragLayer dragLayer;
	private Mochila mochila;
	private boolean waiting = false;
	private ImageView[] drawings = new ImageView[MochilaContents.numPaneles];
	Button terminar;
	

	public void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(dragController == null) dragController = new DragController(this);
		dragController.setCreateMode(true);
		
		setContentView(R.layout.create);  
		terminar = (Button) findViewById(R.id.terminar);
		terminar.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		terminar.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				Intent i = new Intent(v.getContext().getApplicationContext(), PowerPointActivity.class);
				LogX.i("Create","Se ha comenzado la edición de diapositivas.");

				Saver.savePresentation(Saver.ActivityEnum.CREATE1);
				startActivity(i);
				finish();
			}
		});
		

		
		setupViews();
		
		AlphaAnimation aa = new AlphaAnimation(1, 0);
	    aa.setDuration(2000);
	    aa.setFillEnabled(true);
	    aa.setFillAfter(true);	 
	    mochila.startAnimation(aa);
		
	}


	private void setupViews() 
	{
		dragLayer = (DragLayer) findViewById(R.id.drag_layer); 
		dragLayer.setDragController(this.dragController);
		dragController.setFixedDropTarget((DropTarget) dragLayer);
		dragController.addDropTarget((DropTarget) dragLayer);
		mochila = (Mochila) findViewById (R.id.backpack);

		ArrayList<DropPanelWrapper> panels = mochila.getContents().getDropPanels();
		
		for(DropPanelWrapper p : panels){
			DropPanel px = p.getPanelView(this);
			updatePanelView(px, p.getIndex());	 
			px.setDragController(this.dragController);
			dragController.addDropTarget((DropTarget)px);
		}
		
		/*
			
			PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", 0f);
			PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("y", 0f);
			PropertyValuesHolder pvhA = PropertyValuesHolder.ofFloat("alpha", 1f);
			//PropertyValuesHolder pvhSX = PropertyValuesHolder.ofFloat("scaleX", 0.7f);
			//PropertyValuesHolder pvhSY = PropertyValuesHolder.ofFloat("scaleY", 0.7f);
			ObjectAnimator.ofPropertyValuesHolder(estatua1, pvhX, pvhY, pvhA).setDuration(1000).start();
			
		}
		 */

		DragLayer.LayoutParams lpmochila = new LayoutParams(200,200,0,550);
		dragLayer.updateViewLayout(mochila,lpmochila);
		
		ViewTreeObserver vto = mochila.getViewTreeObserver(); 
	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
	        @Override 
	        public void onGlobalLayout() { 
	        	for(DropPanelWrapper p : mochila.getContents().getDropPanels()){	
	    			updateViewsInPanel(p);
	    		}
	            mochila.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        } 
	    });
	    
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (resultCode == 2) {
            	finish();
        }
    }

	public void updatePanelView(DropPanel p, int _index){
		int index = _index;
		int verticalOffset = (index>MochilaContents.numPaneles/2)? 300 : 50;		
		int horizontalOffset = (MochilaContents.numPaneles < 5)? 200: 0;
		DragLayer.LayoutParams lp = new LayoutParams(341, 231,
				100 +horizontalOffset + ((index-1)%((int)(MochilaContents.numPaneles/2)))*361 , 100+verticalOffset);
		if(p.getParent() != null)((ViewGroup)p.getParent()).removeView(p);
		dragLayer.addView(p, 0);
		if (drawings[index-1]== null) 
		{
			drawings[index-1] = new ImageView(this);
			dragLayer.addView(drawings[index-1],1,lp);
		}
		dragLayer.updateViewLayout(p, lp);
	}

	public void updateViewsInPanel(DropPanelWrapper p)
	{
//		drawings[p.getIndex()-1].setBackgroundDrawable(p.getPanelView(this).getThumbnail());
		drawings[p.getIndex()-1].setImageDrawable(p.getPanelView(this).getThumbnail());
		
		for(ViewWrapper w : p.getWrappers()){
			View iv = w.getView(this);
			
			if(!p.getItems(getApplicationContext()).contains(iv)){
				p.getItems(getApplicationContext()).add(iv);
			}
			
			int index = p.getIndex();
			int verticalOffset = (index>MochilaContents.numPaneles/2)? 300 : 50;	
			int horizontalOffset = ((index-1)%((int)(MochilaContents.numPaneles/2)))*361+ ((MochilaContents.numPaneles < 5)? 200:0);
//			int width=0, height=0;
			int left=0, top=0;
			if(iv instanceof ImageView){
				ImageView img = (ImageView) iv;
				img.setScaleX(1f);
				img.setScaleY(1f);
//				width = img.getWidth();
//				height = img.getHeight();
				//if(width > 100 || height > 100){
					//img.setScaleX(0.3f);
					//img.setScaleY(0.3f);					
				//}
				
				left = (int)(w.getX()*341);
				top = (int)(w.getY()*231);
			}
			else if(iv instanceof TextView){
				TextView tv = (TextView)iv;				
				TextView tv_temp = new TextView(this.getApplicationContext());
				tv_temp.setTextSize(20);
				tv_temp.setText(tv.getText());
				left = (int)(w.getX()*341);
				top = (int)(w.getY()*231);	
				if(iv.getParent() != null)((ViewGroup)iv.getParent()).removeView(iv);
				p.replaceObject(tv, tv_temp);
				iv = tv_temp;
			}
			iv.setContentDescription("no");
			MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(MyAbsoluteLayout.LayoutParams.WRAP_CONTENT, MyAbsoluteLayout.LayoutParams.WRAP_CONTENT, (int)((100+horizontalOffset)+left) , (int)((100+verticalOffset)+top));
			if(iv.getParent() != null)((ViewGroup)iv.getParent()).removeView(iv);
			iv.setOnTouchListener(this); 
			iv.setVisibility(0);
			dragLayer.addView(iv, lp);
			if(w.wasDisplayed() == false) {
				//animar entrada
				PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", 10f, (int)((100+horizontalOffset)+left));
				PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("y", 600f, (int)((100+verticalOffset)+top));
				PropertyValuesHolder pvhA = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
				ObjectAnimator.ofPropertyValuesHolder(iv, pvhX, pvhY, pvhA).setDuration(1000).start();
				w.setDisplayed(true);
			}
			else {
				PropertyValuesHolder pvhA = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
				ObjectAnimator.ofPropertyValuesHolder(iv, pvhA).setDuration(300).start();
			}
		}
		dragLayer.bringChildToFront(drawings[p.getIndex()-1]);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		this.dragLayer.invalidate();

	}


	public void toast (String msg)
	{
		}

	@Override
	protected void onResume() {
		super.onResume();
		if(waiting) {
			ArrayList<DropPanelWrapper> panels = MochilaContents.getInstance().getDropPanels();	
			for(DropPanelWrapper p : panels){
				updatePanelView(p.getPanelView(this),p.getIndex());		
				updateViewsInPanel(p);
			}
		}
		waiting = false;
	}
	
	@Override
    protected void onStop() {
        super.onStop();
    }
	
	public boolean onLongClick(View v) 
	{
		if (!v.isInTouchMode()) {
			return false;
		}
		return startDrag(v);
	}
	
    @Override
	public boolean onTouch(View v, MotionEvent m) {
    	Log.d("onTouch create", "x: "+m.getX()+" y: "+m.getY());
    	if(m.getAction()==MotionEvent.ACTION_MOVE || m.getAction()==MotionEvent.ACTION_DOWN)
    		return startDrag(v);
    	return false;
	}

	public boolean startDrag (View v)
	{
		bringAllDrawingsToFront();
		Object dragInfo = v;
		dragController.startDrag (v, dragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
		return true;
	}
	public void bringAllDrawingsToFront()
	{
		for(int i=0;i<MochilaContents.numPaneles;i++)
			dragLayer.bringChildToFront(drawings[i]);	
	}
	
	@Override
	public void onBackPressed() {
	}

}

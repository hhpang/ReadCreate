package com.didithemouse.alfa;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.didithemouse.alfa.MyAbsoluteLayout.LayoutParams;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PowerPointActivity extends Activity implements OnTouchListener{

	private DragController dragController;
	private DragLayer dragLayer;
	MyAbsoluteLayout[] miniCanvases;
	DragLayer dl;
	FrameLayout fl;
	private FingerPaint fp;
	Button next;
	
	private MyAbsoluteLayout toolbar;
	private ImageButton[] toolbar_button;
	private View selectedToolbarIcon;
	
	private Runnable updateWhenDropped = null;
	
	//Tamano de los minicanvases
	int miniCanvasesHeight = 100;
	//Cual es la separacion entre minicanvases (top to top)
	int miniCanvasesVertOffset= 10 + ((6-MochilaContents.numPaneles)/2)*20;
	//Desde DONDE parten los minicanvases
	int miniCanvasesFirstOffset= 10 + ((6-MochilaContents.numPaneles)/2)*(miniCanvasesHeight+miniCanvasesVertOffset)/2;

	
	private int selectedPanel = 1;
	private ArrayList<DropPanelWrapper> panels = MochilaContents.getInstance().getDropPanels();
	DropPanelWrapper[] panelsArray =  {getPanel(0),getPanel(1),getPanel(2),getPanel(3),getPanel(4),getPanel(5)};

	public DropPanelWrapper getPanel(int number) {
    	for(int i=0; i< panels.size(); i++) {
    		if(panels.get(i).getIndex() == (number+1)) {
    			return panels.get(i);
    		}
    	}
    	return null;
    }
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dragController = new DragController(this);

		setContentView(R.layout.powerpoint);  
		dl = (DragLayer) findViewById(R.id.canvas_big_draglayer);
		dl.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		dragLayer = dl;
		
		dl.setDragController(dragController);

		dragController.addDropTarget((DropTarget)dl);
		dragController.setFixedDropTarget(panelsArray[0].getPanelView(this));
		dragController.setPowerPointMode(true);

		miniCanvases = new MyAbsoluteLayout[] {
				(MyAbsoluteLayout) findViewById(R.id.canvas1),
				(MyAbsoluteLayout) findViewById(R.id.canvas2),
				(MyAbsoluteLayout) findViewById(R.id.canvas3),
				(MyAbsoluteLayout) findViewById(R.id.canvas4),
				(MyAbsoluteLayout) findViewById(R.id.canvas5),
				(MyAbsoluteLayout) findViewById(R.id.canvas6)
		};
		
		FrameLayout [] miniCanvasesFl = new FrameLayout[] {
				(FrameLayout) findViewById(R.id.canvas1frame),
				(FrameLayout) findViewById(R.id.canvas2frame),
				(FrameLayout) findViewById(R.id.canvas3frame),
				(FrameLayout) findViewById(R.id.canvas4frame),
				(FrameLayout) findViewById(R.id.canvas5frame),
				(FrameLayout) findViewById(R.id.canvas6frame)
		};
				
		for(MyAbsoluteLayout i : miniCanvases) i.setBackgroundColor(0);

		((LinearLayout.LayoutParams) miniCanvasesFl[0].getLayoutParams()).setMargins(25,miniCanvasesFirstOffset,0,0);
		for (int i = 1; i < MochilaContents.numPaneles; i++)
		{
			((LinearLayout.LayoutParams) miniCanvasesFl[i].getLayoutParams()).setMargins(25, miniCanvasesVertOffset, 0, 0);
		}


		for(MyAbsoluteLayout i : miniCanvases)
			i.setOnClickListener(new MyOnClickListener(i));

		
		Button buttonback = (Button) findViewById(R.id.buttonback);
		buttonback.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Intent i = new Intent(v.getContext(), CreateActivity.class);
				LogX.i("Create","El instructor ha vuelto atras.");

				startActivity(i);
				finish();
				return true;
			}
		});
		
		next = (Button) findViewById(R.id.terminar);
		Timer time = new Timer();
		time.schedule(new TimerTask() {
			@Override
			public void run() {
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						next.setClickable(true);
					}
				};
				next.getHandler().post(r);
			}
		}, 5000);
		
		next.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				Intent i = new Intent(v.getContext(), ShareActivity.class);
				LogX.i("Share","Se ha iniciado Share.");

				Saver.savePresentation(Saver.ActivityEnum.CREATE2);
				startActivity(i);
				finish();
			}
		});
		next.setClickable(false);
		
		//Se arregla el linearlayout dependiendo del num de paneles
		LinearLayout linear = (LinearLayout) findViewById(R.id.mini_canvas);

		View[] linearArray = {linear.getChildAt(0),linear.getChildAt(1),linear.getChildAt(2),linear.getChildAt(3),linear.getChildAt(4),linear.getChildAt(5)};

		linear.removeAllViews();
		for(int i=0 ; i<MochilaContents.numPaneles ; i++){
			linear.addView(linearArray[panelsArray[i].getIndex() - 1]);
		}
		
	
		FrameLayout fl = (FrameLayout) findViewById(R.id.canvas_framelayout);
		if(panels.get(0)!=null)
		{
			fp= new FingerPaint(this.getApplicationContext(), dl);
			fp.setBitmap(panels.get(0).getBitmap());
			fl.addView(fp);
			fp.setMinimumWidth(989);
			fp.setMinimumHeight(670);
			}
		
		updateWhenDropped = new Runnable() {
			@Override
			public void run() {
				updateThumbnails();
			}
		};
		fp.setRunWhenChangeRunnable(new Runnable() {
			@Override
			public void run() {
				if (updateWhenDropped != null)
				dl.post(updateWhenDropped);
			}
		});
		
		/*Aqui seteamos los colores */
		toolbar = (MyAbsoluteLayout) findViewById(R.id.canvas_toolbarlayout);
		int top = 60;
		int[] resources = new int[]{
						 R.drawable.button_hand,
				         R.drawable.button_eraser,
				         R.drawable.button_color};
		int[] colors = new int[] { 0xFF000000 };/*, 0xFFFF0000, 0xFF00FF00 , 0xFF0000FF,
								   0xFFFFFF00, 0xFFFF00FF, 0xFF593E1A};*/
		fp.setColor(colors[0]);
		int numbuttons = 3 + colors.length;
		int numcolors = colors.length;
		toolbar_button = new ImageButton[numbuttons];
		selectedToolbarIcon = new View(this.getApplicationContext());
		selectedToolbarIcon.setBackgroundColor(0x50FFFFFF);
		//SOLO NEGRO
		for (int i=0; i<numbuttons-1; i++)
		{
			toolbar_button[i] = new ImageButton(this.getApplicationContext());
			toolbar_button[i].setBackgroundResource((i<2)? resources[i] : R.drawable.button_pencil);
			LayoutParams params = new LayoutParams(40, 40, 0, top);
			toolbar_button[i].setLayoutParams(params);
			top+= 50;
			toolbar.addView(toolbar_button[i]);
	    }
		
		/*VARIOS COLORES (!)
		for (int i=0, j=0; i<numbuttons-1; i++)
		{
			toolbar_button[i] = new ImageButton(this.getApplicationContext());
			toolbar_button[i].setBackgroundResource((i<2)? resources[i] : resources[2]);
			toolbar_button[i].getBackground().mutate();
			LayoutParams params = new LayoutParams(40, 40, 0, top);
			toolbar_button[i].setLayoutParams(params);
			top+= 50;
			if(i>=2 && j < numcolors){
				toolbar_button[i].getBackground().setColorFilter(colors[j++],Mode.MULTIPLY);
			}
			toolbar.addView(toolbar_button[i]);
	    }
		*/
		toolbar_button[numbuttons-1] = new ImageButton(this.getApplicationContext());
		toolbar_button[numbuttons-1].setBackgroundResource(R.drawable.button_trashbin);
		toolbar_button[numbuttons-1].setLayoutParams(new LayoutParams(40, 40, 0, 620));
		//toolbar.addView(toolbar_button[numbuttons-1]);
		
		selectedToolbarIcon.setLayoutParams(toolbar_button[2].getLayoutParams());
		toolbar.addView(selectedToolbarIcon);
		selectedToolbarIcon.bringToFront();
		
		//Seteamos que hara el click de la MANO
		toolbar_button[0].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fp.setDrag(); 
			    selectedToolbarIcon.setLayoutParams(toolbar_button[0].getLayoutParams());
			    LogX.i("Create","Se ha utilizado la mano.");
			    }
		});
		//Seteamos que hara el click de la GOMA
		toolbar_button[1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fp.setErase();
				LogX.i("Create","Se ha seleccionado la goma.");
			    selectedToolbarIcon.setLayoutParams(toolbar_button[1].getLayoutParams());
			}
		});
		
		//Boton de trashbin
		toolbar_button[numbuttons-1].setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fp.eraseAll();
				LogX.i("Create","Se ha usado la herramienta trashbin.");
			}
		});
		
		//Y que hará CADA color
		for(int i = 2, j=0; i < numbuttons; i++)
		{
			if (j < numcolors)
				toolbar_button[i].setOnClickListener(new ColorClickListener(colors[j]));j++;
				
		}
		
		setupViews();
	}
	class ColorClickListener implements View.OnClickListener{
		int color;
		public ColorClickListener (int _color){ color = _color; }
		@Override
		public void onClick(View v) {
			fp.setColor(color);		
			LogX.i("Create","Se ha cambiado de color, a " + Integer.toHexString(color));
			selectedToolbarIcon.setLayoutParams(v.getLayoutParams());
		}
	}

	class MyOnClickListener implements View.OnClickListener {

		private MyAbsoluteLayout minicanvas;

		public MyOnClickListener(MyAbsoluteLayout mc){
			minicanvas = mc;
		}

		public void onClick(View v) {
			deselectAllCanvas();
			((ImageView)(minicanvas.getChildAt(0))).setColorFilter(0xC00080ff);

			int index = getIndexOfChildInLinearLayout((LinearLayout)v.getParent().getParent(),(View)v.getParent());
			dragController.setFixedDropTarget(panelsArray[index].getPanelView(v.getContext()));
			showObjects(panelsArray[index]);
			updateThumbnails();
			selectedPanel = index+1;
		}

		private int getIndexOfChildInLinearLayout(LinearLayout parent, View child) {

			for( int i=0 ; i<parent.getChildCount() ; i++ ){
				if(parent.getChildAt(i) == child){
					return i;
				}
			}

			return -1;
		}
	}

	private void deselectAllCanvas() {
		for(int i=0;i<MochilaContents.numPaneles;i++)
			((ImageView)(miniCanvases[i].getChildAt(0))).setColorFilter(Color.TRANSPARENT);
	}
	
	private void updateThumbnails()
	{
		LinearLayout linear = (LinearLayout) findViewById(R.id.mini_canvas);
		for(int i=0 ; i<MochilaContents.numPaneles ; i++){
			ImageView img = (ImageView)((FrameLayout)(linear.getChildAt(i))).getChildAt(1);
			img.setImageDrawable(getPanel(i).getPanelView(this).getMiniThumbnail());
			
			View fondo = miniCanvases[i].getChildAt(0);
			miniCanvases[i].removeAllViews();
			miniCanvases[i].addView(fondo);
			
			for(ViewWrapper wrapper : getPanel(i).getWrappers())
			{
				View iv = (View)wrapper.getView(this.getApplicationContext());

				if(iv instanceof ExtendedImageView){
					ExtendedImageView big = new ExtendedImageView(this.getApplicationContext(),
							((ExtendedImageView)iv).getDrawableID(),
							((ExtendedImageView)iv).getScaleFactor());
					big.setWrapper(wrapper);
					big.setImageDrawable(((ImageView)iv).getDrawable());
					int height = (int) (big.getDrawable().getIntrinsicHeight()*0.43f);
					int width = (int) (big.getDrawable().getIntrinsicWidth()*0.43f);
					int left = (int)(wrapper.getX()*150);
					int top = (int)(wrapper.getY()*100);
					MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(width, height, left, top);
					miniCanvases[i].addView(big, lp);
				}
				else if(iv instanceof TextView){
					TextView tv = (TextView)iv;
					TextView tv2 = new TextView(this.getApplicationContext());
					tv2.setText(tv.getText());
					tv2.setTextSize(9);
					int left = (int)(wrapper.getX()*150);
					int top = (int)(wrapper.getY()*100); 
					MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(MyAbsoluteLayout.LayoutParams.WRAP_CONTENT, MyAbsoluteLayout.LayoutParams.WRAP_CONTENT, left, top);
					miniCanvases[i].addView(tv2, lp);		
				}
			}
		}
	}

	//grandes
	private void showObjects(DropPanelWrapper panel) {
		dl.removeAllViews();
		for(ViewWrapper wrapper : panel.getWrappers())
		{
			View iv = (View)wrapper.getView(getApplicationContext());
			iv.setContentDescription("no");
			if(iv instanceof ImageView){
				ImageView big = (ImageView) iv;
				int left = (int)(wrapper.getX()*989);
				int top = (int)(wrapper.getY()*670);
				if(big.getParent() != null)((ViewGroup)big.getParent()).removeView(big);
				MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(big.getDrawable().getIntrinsicWidth()*3,big.getDrawable().getIntrinsicHeight()*3, left, top);
				dl.addView(big, lp);
				big.setOnTouchListener(this); 
			}
			else if(iv instanceof TextView){
				TextView tv = (TextView)iv;
				tv.setTextSize(60);	
				int left = (int)(wrapper.getX()*989);
				int top = (int)(wrapper.getY()*670);
				if(tv.getParent() != null)((ViewGroup)tv.getParent()).removeView(tv);
				MyAbsoluteLayout.LayoutParams lp = new MyAbsoluteLayout.LayoutParams(MyAbsoluteLayout.LayoutParams.WRAP_CONTENT, MyAbsoluteLayout.LayoutParams.WRAP_CONTENT, left, top);
				dl.addView(tv, lp);
				tv.setOnTouchListener(this); 
			}
		}
		fp.setBitmap(panel.getBitmap());
	}

	//peque�os
	private void setupViews() 
	{
		for(int i=0; i<MochilaContents.numPaneles ; i++){	
			if(i==0){
				((ImageView)(miniCanvases[i].getChildAt(0))).setColorFilter(0xC00080ff);
			}
			showObjects(getPanel(i));
		}
		
		deselectAllCanvas();
		((ImageView)(miniCanvases[0].getChildAt(0))).setColorFilter(0xC00080ff);
		int index = 0;
		showObjects(panelsArray[index]); 
		selectedPanel = index+1;
		updateThumbnails();
	}

    @Override
	public boolean onTouch(View v, MotionEvent m) {
    	//EVITAR Multitouch
    	if(m.getPointerCount() > 1) return true;
    	
    	if(m.getAction()==MotionEvent.ACTION_MOVE || m.getAction()==MotionEvent.ACTION_DOWN)
    		return startDrag(v);
    	return false;
	}
	
	public boolean startDrag (View v)
	{
		Object dragInfo = v;
		dragController.startDrag (v, dragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
		return true;
	}
	
	@Override
	public void onBackPressed() {
	}
}

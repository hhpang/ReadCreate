package com.didithemouse.alfa.etapas;

import com.didithemouse.alfa.DragController;
import com.didithemouse.alfa.DragLayer;
import com.didithemouse.alfa.DropPanelWrapper;
import com.didithemouse.alfa.DropTarget;
import com.didithemouse.alfa.ExtendedImageView;
import com.didithemouse.alfa.LogX;
import com.didithemouse.alfa.Mochila;
import com.didithemouse.alfa.MochilaContents;
import com.didithemouse.alfa.R;
import com.didithemouse.alfa.MyAbsoluteLayout.LayoutParams;
import com.didithemouse.alfa.Saver;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

abstract class EtapaActivity extends Activity implements View.OnTouchListener {

	protected EtapaSurfaceView mySurfaceView;
	protected DragController dragController;
	protected DragLayer dragLayer;
	protected Handler handler;
	protected Button closeButton;
	protected int panelNumber;
	protected DropPanelWrapper dropPanel;
	protected Mochila mochila;
	protected ImageView mochilaView;
	protected ExtendedImageView[] arrastrables;
	protected LayoutParams[] posiciones;
	protected final static int maxObjetos = 4;
	public static String title;
	protected ImageView badge;
	protected int badgeDrawable;
	protected ImageButton volver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		badge = new ImageView(this.getApplicationContext());
	}

	protected void genericInicialization() {

        LogX.i("Read (" + title + ")","Ha comenzado la etapa. ");
		mySurfaceView.setActivity(this);
		mySurfaceView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		mochila = (Mochila) findViewById(R.id.backpack);
		dragController = new DragController(this);
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);
		
		if(!MochilaContents.SKIP_OBJECTS)
		{
			dragLayer.setDropRunnable(new Runnable(){
				@Override
				public void run() {
					checkObjects(); }});
		}

		dropPanel = MochilaContents.getInstance().searchForPanel(title);
		badge.setImageResource(badgeDrawable);

		if (dropPanel == null) {
			// primera vez que entra aquí
			dropPanel = MochilaContents.getInstance().getNewPanel();
			dropPanel.setTitle(title);

			ExtendedImageView badge_eim = new ExtendedImageView(getApplicationContext(),badgeDrawable,1);
			badge_eim.setImageResource(badgeDrawable);
			badge_eim.setContentDescription("no");

			//dropPanel.addObject(badge_eim, 0.8f, 0.05f);
			dropPanel.addObject(badge_eim);
		}

		mochila.setCurrentPanel(dropPanel);

		inicializarBoton();

		/*** ANIMACION INICAL ***/
		ImageView badge = (ImageView) findViewById(R.id.badge);
		ImageView backpack = (ImageView) findViewById(R.id.backpack_intro);

		PropertyValuesHolder finalX = PropertyValuesHolder.ofFloat("x", -120f);
		PropertyValuesHolder finalY = PropertyValuesHolder.ofFloat("y", 380f);
		PropertyValuesHolder finalA = PropertyValuesHolder.ofFloat("alpha", 0f);
		ObjectAnimator translate = ObjectAnimator.ofPropertyValuesHolder(badge,
				finalX, finalY).setDuration(1000);

		PropertyValuesHolder finalScaleX = PropertyValuesHolder.ofFloat(
				"scaleX", 0.1f);
		PropertyValuesHolder finalScaleY = PropertyValuesHolder.ofFloat(
				"scaleY", 0.1f);
		ObjectAnimator shrink = ObjectAnimator.ofPropertyValuesHolder(badge,
				finalScaleX, finalScaleY).setDuration(1000);
		shrink.setInterpolator(new BounceInterpolator());

		ObjectAnimator disappear_backpack = ObjectAnimator
				.ofPropertyValuesHolder(backpack, finalA).setDuration(2000);
		ObjectAnimator disappear_badge = ObjectAnimator.ofPropertyValuesHolder(
				badge, finalA).setDuration(200);

		AnimatorSet move = new AnimatorSet();
		move.play(shrink).before(translate);
		move.play(disappear_badge).after(translate);
		move.play(disappear_backpack).after(disappear_badge);
		move.setStartDelay(1000);
		move.start();

		/*** SETUP DE LAS VISTAS ***/
		setupViews();

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Pausamos el thread de rendering.
		// TODO: de-allocate objects para liberar memoria
		mySurfaceView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Resumimos el thread suspendido
		// TODO: si se hizo la de-allocation, re-allocar los objetos
		mySurfaceView.onResume();
	}

	public void showObjects() {
		//Animation appear = AnimationUtils.loadAnimation(this,
		//		R.animator.appear_long);
		dragLayer.setVisibility(View.VISIBLE);
		//dragLayer.startAnimation(appear);
		if(MochilaContents.SKIP_OBJECTS)
		{
			volver.setClickable(true);
			volver.setVisibility(View.VISIBLE);
		}
		else
		{
			volver.setVisibility(View.INVISIBLE);
		}
	}

	boolean hole_shown = false;

	public void showMouseHole() {
		if (!hole_shown) {
			hole_shown = true;
			View mousehole = findViewById(R.id.mousehole);
			mousehole.setVisibility(View.VISIBLE);
			mousehole.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					LogX.i("Read (" + title + ")", "Se hizo click en el agujero del ratón.");
					View mousehole = findViewById(R.id.mousehole);
					mousehole.setVisibility(View.GONE);
					mousehole.setClickable(false);
					mousehole.setAlpha(0);
					mySurfaceView.mRenderer.collectItems = true;
					setOverlay();
				}
			});
		}
	}

	public void hideMouseHole() {
		if(hole_shown) {
			hole_shown = false;
			View mousehole = findViewById(R.id.mousehole);
			mousehole.setVisibility(View.INVISIBLE);
			mousehole.setClickable(false);
		}
	}
	
	public Handler getHandler() {
		return this.handler;
	}

	int drawables[];
	protected void setupViews() {
		DragController dragController = this.dragController;
		dragLayer = (DragLayer) findViewById(R.id.drag_layer);
		dragLayer.setVisibility(View.INVISIBLE);
		dragLayer.setDragController(dragController);
		dragController.addDropTarget(dragLayer);

		mochilaView = (ImageView) findViewById(R.id.backpack);
		arrastrables = new ExtendedImageView[maxObjetos];
		posiciones = new LayoutParams[maxObjetos];

		setObjects();

		for (int i = 0; i < drawables.length; i++) {
			if (posiciones[i] != null) {
				arrastrables[i] = new ExtendedImageView(this.getApplicationContext(),drawables[i],1);
				arrastrables[i].setImageResource(drawables[i]);
				arrastrables[i].setVisibility(ImageView.VISIBLE);
				dragLayer.addView(arrastrables[i]);
				arrastrables[i].setContentDescription("no");
				arrastrables[i].setOnTouchListener(this);
				
				dragLayer.updateViewLayout(arrastrables[i], posiciones[i]);
			}
		}

		dragLayer.removeView(mochilaView);
		dragLayer.addView(mochilaView);
		mochilaView.setContentDescription("no");
		dragController.addDropTarget((DropTarget) findViewById(R.id.backpack));

		DragLayer.LayoutParams lp1 = new LayoutParams(179, 205, 0, 540);
		dragLayer.updateViewLayout(mochilaView, lp1);
		volver.setClickable(false);

	}
	
	public boolean startDrag(View v) {
		Object dragInfo = v;
		dragController.startDrag(v, dragLayer, dragInfo,
				DragController.DRAG_ACTION_MOVE);
		return true;
	}

	public void toast(String msg) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent m) {
		//EVITAR Multitouch
    	if(m.getPointerCount() > 1) return true;
    	
		if (m.getAction() == MotionEvent.ACTION_MOVE
				|| m.getAction() == MotionEvent.ACTION_DOWN)
			return startDrag(v);
		return false;
	}
	
	protected void checkObjects()
	{
		int counter = 0;
		for (int i = 0; i < maxObjetos; i ++)
		{if (arrastrables[i] == null) counter++;}
		if (dropPanel.getNumItems() >= maxObjetos - counter + 1)
		{
			volver.setVisibility(View.VISIBLE);
			volver.setColorFilter(0);
			volver.setClickable(true);
			LogX.i("Read (" + title + ")","Se ha guardado un objeto en la mochila.");
			dragLayer.setDropRunnable(null);
		}
	}

	abstract protected void setVisited();

	abstract void setObjects();

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogX.i("Read (" + title + ")","Ha finalizado la etapa.");
		this.mySurfaceView.setVisibility(View.GONE);
		this.mySurfaceView.destroyDrawingCache();
		this.mySurfaceView.mRenderer.finish();
		this.mySurfaceView = null;
		
		dragController = null;
		dragLayer = null;
		handler = null;
		closeButton = null;
		dropPanel = null;
		mochila = null;
		mochilaView = null;
		arrastrables = null;
		posiciones = null;
		badge = null;
		volver = null;
		System.gc();
	}

	protected void inicializarBoton() {
		volver = (ImageButton) findViewById(R.id.volver);
		volver.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			public void onClick(View v) {
				if(!flag) return;
				flag = false;
				if (mySurfaceView.mRenderer.finishDisappear) {
					setVisited();
				}
				MochilaContents.getInstance().cleanPanels();
				Saver.savePresentation(Saver.ActivityEnum.ETAPA);
				finish();
			}
		});
	}
	
	protected void setOverlay()
	{
		
	}
	@Override
	public void onBackPressed() {
	}

}

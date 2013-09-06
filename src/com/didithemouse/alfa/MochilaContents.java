package com.didithemouse.alfa;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;

import com.didithemouse.alfa.etapas.ChinatownActivity;
import com.didithemouse.alfa.etapas.ConeyActivity;
import com.didithemouse.alfa.etapas.EmpireStateActivity;
import com.didithemouse.alfa.etapas.InicioActivity;

public class MochilaContents {

	private ArrayList<DropPanelWrapper> dropPanelsWrappers;
	private boolean created;
	private int counter;	
	
	private int kidNumber = 0;
	private String kidName = "";
	private String dirName = "" ;
	private String RCSdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/My SugarSync Folders/My SugarSync/RCS/";
	//private String RCSdir = Environment.getExternalStorageDirectory().getAbsolutePath() +"/RCS/";
	
	//Deshabilita log, guardado, etc. para debug.
	public static final boolean SAVING  = true;
	public static final boolean LOGGING = true;
	public static final boolean SKIP_OBJECTS = false;
	public static final boolean SKIP_MAP     = false;
	
	
	private int visitedPlaces = 0;
	public int getVisitedPlaces()
	{
		visitedPlaces = 0;
		if(EmpireStateActivity.visitedFlag) visitedPlaces++;
		if(ConeyActivity.visitedFlag ) visitedPlaces++;
		if(InicioActivity.visitedFlag) visitedPlaces++;
		if(ChinatownActivity.visitedFlag ) visitedPlaces++;
		return visitedPlaces;
	}
	
	static final int numPaneles = 4; // SOLO 4 o 6
	
	private static MochilaContents INSTANCE = new MochilaContents();
	private MochilaContents() {

	}
	public static MochilaContents getInstance() {
        return INSTANCE;
    }
	public ArrayList<DropPanelWrapper> getDropPanels() {
		return dropPanelsWrappers;
	}
	public void setDropPanels(ArrayList<DropPanelWrapper> dropPanelsWrappers) {
		this.dropPanelsWrappers = dropPanelsWrappers;
	}
	public DropPanelWrapper getPanel(int i) {
		return dropPanelsWrappers.get(i);
	}
	public DropPanelWrapper getNewPanel() {
		return dropPanelsWrappers.get(counter++);
	}
	public DropPanelWrapper searchForPanel(String place) {
		for(int i=0; i<MochilaContents.numPaneles; i++) {
			if(i < dropPanelsWrappers.size() && dropPanelsWrappers.get(i) != null) {
				String title = dropPanelsWrappers.get(i).getTitle();
				if(title != null && title == place) {
					return dropPanelsWrappers.get(i);
				}
			}
		}
		return null;
	}
	
	public boolean isCreated() { return created;	}
	public void setCreated(boolean created) {	this.created = created; }
	
	
	public int getCounter()  {	return counter; }
	public void increaseCounter() {	this.counter++;	}
	
	public void cleanPanels()
	{
		for (DropPanelWrapper dpw: dropPanelsWrappers)
			dpw.cleanPanel();
	}
	
	public void setKid(int _kidNumber,String _kidName) { 
		kidNumber = _kidNumber;
		kidName = _kidName != null? _kidName: "";
		
		dirName = RCSdir +"/"+kidNumber+"/";
        (new File (dirName)).mkdirs();
	}
	
	public int getKidNumber(){ return kidNumber; }
	public String getKidName(){ return kidName; }
	public String getDirectory() { return dirName; }
	
	private final String logDirname =  RCSdir + "/log/";
	
	public String getLogDirname()
	{
		File f = new File(logDirname);
		if (!f.exists()) f.mkdirs();
		return logDirname;
	}
	
	
	public boolean kidExists(int num)
	{
		String dirnameX = RCSdir +"/"+num+"/" ;
        return (new File(dirnameX)).exists();
        	
	}
	
	public void restart()
	{
		Saver.clear();
		if (dropPanelsWrappers != null)
		for (DropPanelWrapper dpw: dropPanelsWrappers)
			dpw.killPanel();
		dropPanelsWrappers = new ArrayList<DropPanelWrapper>();
		created = false;
		counter = 0;	
		
		visitedPlaces = 1;
				
		InicioActivity.visitedFlag=false;
		EmpireStateActivity.visitedFlag = false;
		ConeyActivity.visitedFlag = false;
		ChinatownActivity.visitedFlag = false;

	}
	
}

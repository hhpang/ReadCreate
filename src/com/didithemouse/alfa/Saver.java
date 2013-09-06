package com.didithemouse.alfa;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;

import com.didithemouse.alfa.etapas.ChinatownActivity;
import com.didithemouse.alfa.etapas.ConeyActivity;
import com.didithemouse.alfa.etapas.EmpireStateActivity;
import com.didithemouse.alfa.etapas.InicioActivity;

//http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/


public class Saver {
	
	public enum ActivityEnum {ETAPA,MAPA,CREATE1,CREATE2,SHARE1, ERROR}
	
	public static void savePresentation(ActivityEnum fromActivity)
	{		
		MochilaContents mc = MochilaContents.getInstance();
		if(!MochilaContents.SAVING) return;
		
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {		
			Log.d("SAVER", "FAILED TRANSFORM (1)");
			e.printStackTrace();
			return;
		}
		
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.d("SAVER", "FAILED TRANSFORM (2)");
			e.printStackTrace();
			return;
		}
		Document doc = db.newDocument();
		
		Element root = doc.createElement("savedFile");
		root.setAttribute("kidNumber", "" + mc.getKidNumber());
		root.setAttribute("kidName", mc.getKidName());
		doc.appendChild(root);
		
		Element flags = doc.createElement("visitedFlags");
		flags.setAttribute("inicio", InicioActivity.isVisited() + "");
		flags.setAttribute("china", ChinatownActivity.isVisited() + "");
		flags.setAttribute("coney", ConeyActivity.isVisited()+ "");
		flags.setAttribute("empire", EmpireStateActivity.isVisited()+"");
		root.appendChild(flags);
		
		Element lastActivity = doc.createElement("lastActivity");
		lastActivity.setAttribute("last", fromActivity.name());
		root.appendChild(lastActivity);
		
		Element panels = doc.createElement("panels");
		root.appendChild(panels);
		
		for (DropPanelWrapper dpw : mc.getDropPanels())
		{
			Element panel = doc.createElement("panel");
			panel.setAttribute("ID", dpw.getID() + "");
			panel.setAttribute("index", dpw.getIndex() + "");
			
			
			Element record = doc.createElement("record");
			record.setAttribute("filename", dpw.getFileName());
			record.setAttribute("isRecorded", dpw.isRecorded() + "");
			panel.appendChild(record);

			
			Element bitmap = doc.createElement("bitmap");
			boolean hasDrawn = dpw.hasDrawn();
			
			bitmap.setAttribute("hasDrawn", "" + hasDrawn);
			if (hasDrawn){
				String bitmapFilename = mc.getDirectory() + "/panelDraw_" + dpw.getID()+ ".png"; 
				bitmap.setAttribute("bitmapFilename", bitmapFilename);
				Bitmap panelBitmap = dpw.getBitmap();
				try {
			       	FileOutputStream out = new FileOutputStream(bitmapFilename);
			       	panelBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				} catch (Exception e) {
				}
			}
			panel.appendChild(bitmap);
			
			Element views = doc.createElement("views");
			panel.appendChild(views);
			
			for(ViewWrapper vw : dpw.getWrappers())
			{
				Element view = doc.createElement("view");
				
				int drawID = vw.getDrawableID();
				boolean isImage = (drawID != -1 && idToString().indexOfKey(drawID)>=0)
						? true: false;
				view.setAttribute("type", isImage? "image":"text");
				
				if (isImage)
				{
					Element image = doc.createElement("image");
					image.setAttribute("imageID", idToString().get(drawID));
					image.setAttribute("scaleFactor", vw.getScaleFactor() + "");
					view.appendChild(image);
				}
				else
				{
					Element text = doc.createElement("text");
					text.setAttribute("drawText", vw.getText());
					view.appendChild(text);
				}
				
				
				Element coords = doc.createElement("coords");
				coords.setAttribute("x1", vw.getX()+"");
				coords.setAttribute("y1", vw.getY()+"");
				coords.setAttribute("x2", vw.getxOffset()+"");
				coords.setAttribute("y2", vw.getyOffset()+"");
				view.appendChild(coords);
							
				views.appendChild(view);
								
			}
			
			panels.appendChild(panel);
		}
		
		File file = new File(mc.getDirectory()+ "/saveFile.xml");
		file.delete();
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(mc.getDirectory()+ "/saveFile.xml");
		}catch (Exception e) {return;}
		
		Result output = new StreamResult(out);
		Source input = new DOMSource(doc);

		try {
			transformer.transform(input, output);
			out.close();
		} catch (TransformerException e) {
			Log.d("SAVER", "FAILED TRANSFORM (3)");
			e.printStackTrace();
			return;
		} catch(Exception e) {return;}
	}
	
	public static ActivityEnum loadPresentation()
	{
		MochilaContents mc = MochilaContents.getInstance();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return ActivityEnum.ERROR;
		}
		
		Document doc;
		File file = new File(mc.getDirectory()+ "/saveFile.xml");
		if(!file.exists()) return ActivityEnum.ERROR;
		try {
			doc = db.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
			return ActivityEnum.ERROR;
		}
		
		doc.getDocumentElement().normalize();
		Element rootNode = doc.getDocumentElement();
		String kidName = rootNode.getAttribute("kidName");
		int kidNumber = parseInt(rootNode.getAttribute("kidNumber"));
		mc.setKid(kidNumber, kidName);
		
		Element visitedFlags = getChildrenFromElement(rootNode,"visitedFlags").get(0);
		InicioActivity.visitedFlag= parseBool(visitedFlags.getAttribute("inicio"));
		ChinatownActivity.visitedFlag= parseBool(visitedFlags.getAttribute("china"));
		ConeyActivity.visitedFlag= parseBool(visitedFlags.getAttribute("coney"));
		EmpireStateActivity.visitedFlag= parseBool(visitedFlags.getAttribute("empire"));
		
		Element lastActivity = getChildrenFromElement(rootNode,"lastActivity").get(0);
		String last = lastActivity.getAttribute("last");
		
		Element panelsElement = getChildrenFromElement(rootNode,"panels").get(0);
		List<Element> panelElement = getChildrenFromElement(panelsElement,"panel");
		ArrayList<DropPanelWrapper> panels = new ArrayList<DropPanelWrapper>();
		
		for (Element panel : panelElement)
		{
			DropPanelWrapper dpw = new DropPanelWrapper();
			
			int id = parseInt(panel.getAttribute("ID"));
			int index = parseInt(panel.getAttribute("index"));
			
			dpw.setID(id);
			dpw.setIndex(index);
			
			Element record = getChildrenFromElement(panel,"record").get(0);
			String recordFilename = record.getAttribute("filename");
			boolean isRecorded = parseBool(record.getAttribute("isRecorded"));
			
			dpw.setRecorded(isRecorded, recordFilename);
			
			Element bitmapElement = getChildrenFromElement(panel,"bitmap").get(0);
			boolean hasDrawn = parseBool(bitmapElement.getAttribute("hasDrawn"));
			
			
			if(hasDrawn)
			{
				String filename = bitmapElement.getAttribute("bitmapFilename");
				filename = new File(filename).getAbsolutePath();
				
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inMutable = true;
				Bitmap b = BitmapFactory.decodeFile(filename,opts);
				dpw.setBitmap(b);
			}
			
			Element viewsElement = getChildrenFromElement(panel,"views").get(0);
			List<Element> viewElement = getChildrenFromElement(viewsElement,"view");
			ArrayList<ViewWrapper> items = new ArrayList<ViewWrapper>();
			
			for(Element view : viewElement)
			{
				Element coords = getChildrenFromElement(view,"coords").get(0);
				
				double x1 = parseDouble(coords.getAttribute("x1"));
				double y1 = parseDouble(coords.getAttribute("y1"));
				int x2 = parseInt(coords.getAttribute("x2"));
				int y2  = parseInt(coords.getAttribute("y2"));
				
				if("image".equals(view.getAttribute("type")))
				{
					Element image = getChildrenFromElement(view,"image").get(0);
					int drawID = stringToID().get(image.getAttribute("imageID"));
					float scaleFactor = parseFloat(image.getAttribute("scaleFactor"));
					items.add(new ViewWrapper(x1, y1, x2, y2, drawID,scaleFactor));
				}
				else if ("text".equals(view.getAttribute("type")))
				{
					Element text = getChildrenFromElement(view,"text").get(0);
					String tex = text.getAttribute("drawText");
					items.add(new ViewWrapper(x1, y1, x2, y2, tex));
				}
				
			}
			dpw.setWrappers(items);
			panels.add(dpw);
		}		
		mc.setDropPanels(panels);
		try{
			return ActivityEnum.valueOf(last);
		}catch(Exception e)
		{
			return ActivityEnum.ERROR;
		}
	}
	
	static List<Element> getChildrenFromElement(Element e, String tag) {
	    List<Element> result = new LinkedList<Element>();
	    NodeList nl = e.getElementsByTagName(tag);
	    for (int i = 0; i < nl.getLength(); ++i) {
	        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
	            result.add((Element) nl.item(i));
	    }
	    return result;
	}
	
	static int parseInt (String s)
	{
		int res = 0;
		try{ res = Integer.parseInt(s); } catch(Exception e) {}		
		return res;
	}
	
	static float parseFloat (String s)
	{
		float res = 0.0f;
		try{ res = Float.parseFloat(s); } catch(Exception e) {}		
		return res;
	}
	
	static double parseDouble (String s)
	{
		double res = 0.0;
		try{ res = Double.parseDouble(s); } catch(Exception e) {}		
		return res;
	}
	
	static boolean parseBool (String s)
	{
		return "true".equals(s);
	}
	
	
	public static void clear()
	{
		idToStringArr = null;
		stringToIDMap = null;
	}
	
	static SparseArray<String> idToStringArr = null;
	static SparseArray<String> idToString()
	{ 
		if(idToStringArr == null)
		 idToStringArr = new SparseArray<String>(){
			{
				put(R.drawable.badge_didi_small, "inicio-badge");		
				put(R.drawable.inicio_mapa        , "inicio-mapa");
				put(R.drawable.inicio_despertador , "inicio-despertador");
				put(R.drawable.inicio_polera      , "inicio-polera");
				
				put(R.drawable.badge_chinatown_small  , "china-badge");
				put(R.drawable.chinatown_arroz     , "china-arroz");
				put(R.drawable.chinatown_lampara   , "china-lampara");
				put(R.drawable.chinatown_palitos   , "china-palitos");
				
				put(R.drawable.badge_coney_small, "coney-badge");
				put(R.drawable.coney_cabritas,"coney-cabritas");
				put(R.drawable.coney_globo   , "coney-globo");
				put(R.drawable.coney_gorro   , "coney-gorro");
				
				put(R.drawable.badge_empirestate_small, "empire-badge");
				put(R.drawable.empirestate_bandera , "empire-bandera");
				put(R.drawable.empirestate_semaforo, "empire-semaforo");
				put(R.drawable.empirestate_taxi    , "empire-taxi");
			}};
			
		return idToStringArr;
	}
	
	static Map<String, Integer> stringToIDMap = null;
	static Map<String, Integer> stringToID()
	{
		if (stringToIDMap == null)
			stringToIDMap = new HashMap<String,Integer>(){
				private static final long serialVersionUID = 1L;

				{
					put("inicio-badge"  , R.drawable.badge_didi_small);	
					put("inicio-mapa"      , R.drawable.inicio_mapa);
					put("inicio-despertador", R.drawable.inicio_despertador );
					put("inicio-polera"     , R.drawable.inicio_polera);
					
					put("china-badge"     , R.drawable.badge_chinatown_small);
					put("china-arroz"     , R.drawable.chinatown_arroz);
					put("china-lampara"   , R.drawable.chinatown_lampara);
					put("china-palitos"   , R.drawable.chinatown_palitos);
					
					put("coney-badge"   , R.drawable.badge_coney_small);
					put("coney-cabritas", R.drawable.coney_cabritas);
					put("coney-globo"   , R.drawable.coney_globo);
					put("coney-gorro"   , R.drawable.coney_gorro);
					
					put( "empire-badge"  , R.drawable.badge_empirestate_small);
					put("empire-bandera" , R.drawable.empirestate_bandera);
					put("empire-semaforo", R.drawable.empirestate_semaforo);
					put("empire-taxi"    , R.drawable.empirestate_taxi);
				}};
		return stringToIDMap;
	}
}

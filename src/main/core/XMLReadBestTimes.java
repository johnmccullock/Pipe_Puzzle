package main.core;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import main.core.XMLAttrib;
import main.core.util.XMLReader;

public class XMLReadBestTimes extends XMLReader
{
	public XMLReadBestTimes(XMLReader reader)
	{
		super(reader);
		return;
	}
	
	public HashMap<Difficulty, HashMap<Integer, Long>> read(HashMap<Difficulty, HashMap<Integer, Long>> bestTimes)
	{
		try{
			NodeList nodes = this.getDocument().getElementsByTagName(XMLTag.BEST_TIMES);
			bestTimes = this.readBestTimes((Element)nodes.item(0), bestTimes);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return bestTimes;
	}
	
	private HashMap<Difficulty, HashMap<Integer, Long>> readBestTimes(Element e, HashMap<Difficulty, HashMap<Integer, Long>> bestTimes)
	{
		try{
			NodeList nodes = e.getElementsByTagName(XMLTag.BEST_TIME);
			if(nodes.getLength() <= 0){
				return bestTimes;
			}
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				Integer devID = Integer.parseInt(line.getAttribute(XMLAttrib.DEV_ID));
				Difficulty dif = Difficulty.getTypeForName(line.getAttribute(XMLAttrib.DIFFICULTY));
				Long time = Long.parseLong(line.getAttribute(XMLAttrib.TIME));
				if(bestTimes.get(dif) == null){
					bestTimes.put(dif, new HashMap<Integer, Long>());
				}
				bestTimes.get(dif).put(devID, time);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return bestTimes;
	}
}

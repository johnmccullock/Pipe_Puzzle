package main.core;

import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import main.core.XMLAttrib;
import main.core.util.XMLReader;

public class XMLReadPuzzles extends XMLReader
{
	public XMLReadPuzzles(XMLReader reader)
	{
		super(reader);
		return;
	}
	
	public ArrayList<PipePuzzleData> read()
	{
		ArrayList<PipePuzzleData> results = new ArrayList<PipePuzzleData>();
		try{
			NodeList nodes = this.getDocument().getElementsByTagName(XMLTag.PUZZLE);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				PipePuzzleData data = this.readPuzzle((Element)nodes.item(i));
				results.add(data);
			}
			Collections.sort(results);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	private PipePuzzleData readPuzzle(Element e)
	{
		PipePuzzleData data = new PipePuzzleData();
		try{
			NodeList nodes = e.getElementsByTagName(XMLTag.GRID);
			Element line = (Element)nodes.item(0);
			data.size = Integer.parseInt(line.getAttribute(XMLAttrib.SIZE));
			nodes = e.getElementsByTagName(XMLTag.PARTS);
			line = (Element)nodes.item(0);
			data.elbows = Integer.parseInt(line.getAttribute(XMLAttrib.ELBOWS));
			data.pipes = Integer.parseInt(line.getAttribute(XMLAttrib.PIPES));
			data.tJunctions = Integer.parseInt(line.getAttribute(XMLAttrib.T_JUNCTIONS));
			data.crosses = Integer.parseInt(line.getAttribute(XMLAttrib.CROSSES));
			
			nodes = e.getElementsByTagName(XMLTag.START);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				line = (Element)nodes.item(i);
				PipePuzzleData.StartGoal s = new PipePuzzleData.StartGoal();
				s.x = Integer.parseInt(line.getAttribute(XMLAttrib.X_VALUE));
				s.y = Integer.parseInt(line.getAttribute(XMLAttrib.Y_VALUE));
				s.orientation = PipePuzzle.Orientation.getTypeForName(line.getAttribute(XMLAttrib.ORIENTATION));
				data.starts.add(s);
			}
			
			nodes = e.getElementsByTagName(XMLTag.GOAL);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				line = (Element)nodes.item(i);
				PipePuzzleData.StartGoal g = new PipePuzzleData.StartGoal();
				g.x = Integer.parseInt(line.getAttribute(XMLAttrib.X_VALUE));
				g.y = Integer.parseInt(line.getAttribute(XMLAttrib.Y_VALUE));
				g.orientation = PipePuzzle.Orientation.getTypeForName(line.getAttribute(XMLAttrib.ORIENTATION));
				data.goals.add(g);
			}
			
			data.solution = this.readSolution((Element)e.getElementsByTagName(XMLTag.SOLUTION).item(0));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return data;
	}
	
	private ArrayList<SolutionData> readSolution(Element e)
	{
		ArrayList<SolutionData> s = new ArrayList<SolutionData>();
		NodeList nodes = e.getElementsByTagName(XMLTag.PART);
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element line = (Element)nodes.item(i);
			SolutionData d = new SolutionData();
			d.type = PipePuzzle.ImageKey.getTypeForName(line.getAttribute(XMLAttrib.TYPE));
			d.x = Integer.parseInt(line.getAttribute(XMLAttrib.X_VALUE));
			d.y = Integer.parseInt(line.getAttribute(XMLAttrib.Y_VALUE));
			d.angle = Double.parseDouble(line.getAttribute(XMLAttrib.ANGLE));
			s.add(d);
		}
		return s;
	}
}

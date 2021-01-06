package main.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Utility class used to trace a spanning tree of PipeNodes starting from a specified root.
 * 
 * @author John McCullock
 * @version 1.0 2019-02-11
 */
public class PipeNodePath
{
	private static LinkedHashMap<Integer, PipeNode> mPath = null;
	
	public static ArrayList<PipeNode> find(PipeNode start)
	{
		mPath = new LinkedHashMap<Integer, PipeNode>();
		mPath.put(start.hashCode(), start);
		addChild(start);
		ArrayList<PipeNode> results = new ArrayList<PipeNode>();
		for(PipeNode node : mPath.values())
		{
			results.add(node);
		}
		return results;
	}
	
	private static void addChild(PipeNode current)
	{
		if(current.north != null && !(current.north instanceof PipeCap)){
			addNorthChild(current); // north
		}
		if(current.east != null && !(current.east instanceof PipeCap)){
			addEastChild(current); // east
		}
		if(current.south != null && !(current.south instanceof PipeCap)){
			addSouthChild(current); // south
		}
		if(current.west != null && !(current.west instanceof PipeCap)){
			addWestChild(current); // west
		}
		return;
	}
	
	private static void addNorthChild(PipeNode current)
	{
		if(!mPath.containsKey(current.north.hashCode())){
			mPath.put(current.north.hashCode(), current.north);
			addChild(current.north);
		}
		return;
	}
	
	private static void addEastChild(PipeNode current)
	{
		if(!mPath.containsKey(current.east.hashCode())){
			mPath.put(current.east.hashCode(), current.east);
			addChild(current.east);
		}
		return;
	}
	
	private static void addSouthChild(PipeNode current)
	{
		if(!mPath.containsKey(current.south.hashCode())){
			mPath.put(current.south.hashCode(), current.south);
			addChild(current.south);
		}
		return;
	}
	
	private static void addWestChild(PipeNode current)
	{
		if(!mPath.containsKey(current.west.hashCode())){
			mPath.put(current.west.hashCode(), current.west);
			addChild(current.west);
		}
		return;
	}
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		for(PipeNode node : mPath.values())
		{
			b.append(node);
			b.append(" >> ");
		}
		
		return b.toString();
	}
}


package main.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a Disjoint Set (also called a Union Find or a Merge Find Set).
 * 
 * Among other things, a disjoint set is good for finding all subsets of elements grouped together by union.
 * 
 * A good example, is a graph of nodes where some nodes are linked together by edges (unions) and others aren't.  By using the 
 * findSet method, a given node can be traced, be each connected node, back to its root.  In this way, the findSet method can tell if an 
 * intended series of connected node is completed or not.
 * 
 * One user on StackOverflow said he "...used a Disjoint Set for my Dungeon generator, to ensure all rooms are reachable by passages."
 * 
 * Based on article and code by Manish Bhojasia at https://www.sanfoundry.com/java-program-implement-disjoint-set-data-structure/
 * 
 * @author John McCullock
 * @version 1.0 2019-02-07
 */
public class DisjointSet<T>
{
	private List<Map<T, Set<T>>> mLists = new ArrayList<Map<T, Set<T>>>();
	
	public DisjointSet()
	{
		return;
	}
	
	/**
	 * One of these needs to be made for every element/node of the graph.
	 * @param element
	 */
	public void createSet(T element)
	{
		Map<T, Set<T>> map = new HashMap<T, Set<T>>();
		Set<T> set = new HashSet<T>();
		set.add(element);
		map.put(element, set);
		this.mLists.add(map);
		return;
	}
	
	/**
	 * Traverses unions from the specified element to their beginning, returning the earliest element in the set. 
	 * @param element
	 * @return the earliest element in a union set, or null if none are found.
	 */
	public T findSet(T element)
	{
		for(int index = 0; index < this.mLists.size(); index++)
		{
			Map<T, Set<T>> map = this.mLists.get(index);
			Set<T> keySet = map.keySet();
			for(T key : keySet)
			{
				Set<T> set = map.get(key);
				if(set.contains(element)){
					return key;
				}
			}
		}
		return null;
	}
	
	/**
	 * Creates a single-direction union from one element to a second element.
	 * @param first
	 * @param second
	 */
	public void union(T first, T second)
	{
		T firstRep = this.findSet(first);
		T secondRep = this.findSet(second);
		Set<T> firstSet = null;
		Set<T> secondSet = null;
		
		for(int index = 0; index < this.mLists.size(); index++)
		{
			Map<T, Set<T>> map = this.mLists.get(index);
			if(map.containsKey(firstRep)){
				firstSet = map.get(firstRep);
			}else if(map.containsKey(secondRep)){
				secondSet = map.get(secondRep);
			}
		}
		
		if(firstSet != null && secondSet != null){
			firstSet.addAll(secondSet);
		}
		
		for(int index = 0; index < this.mLists.size(); index++)
		{
			Map<T, Set<T>> map = this.mLists.get(index);
			if(map.containsKey(firstRep)){
				map.put(firstRep, firstSet);
			}else if(map.containsKey(secondRep)){
				map.remove(secondRep);
				this.mLists.remove(index);
			}
		}
		return;
	}
	
	public int getNumberOfDisjointSets()
	{
		return this.mLists.size();
	}
}

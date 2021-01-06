package main.core;

import java.util.Iterator;

/**
 * Crazy way to iterate through a HashMap of ArrayLists.
 * @author John McCullock
 * @version 1.0 20200107
 */
public abstract class PipeMapIterator implements Iterator<PipeNode>
{
	protected Iterator<Object> mKeyIterator = null;
	protected Iterator<PipeNode> mArrayIterator = null;
	protected Object mCurrentKey = null;
	
	public PipeMapIterator()
	{
		this.initialize();
		return;
	}
	
	abstract void initialize();
}

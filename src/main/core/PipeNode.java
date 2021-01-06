package main.core;

public class PipeNode
{
	private double[] DIR = new double[]{0.0, Math.PI * 0.5, Math.PI, Math.PI * 1.5};
	
	public PipeNode north = null;
	public PipeNode east = null;
	public PipeNode south = null;
	public PipeNode west = null;
	private Object mImageKey = null;
	private int mGridX = 0;
	private int mGridY = 0;
	private int mRotationIndex = 0;
	private SolutionData mSolution = null;
	
	public PipeNode()
	{
		return;
	}
	
	public PipeNode(PipeNode north, PipeNode east, PipeNode south, PipeNode west, Object imageKey)
	{
		this.north = north;
		this.east = east;
		this.south = south;
		this.west = west;
		this.mImageKey = imageKey;
		return;
	}
	
	public void setGridX(int x)
	{
		this.mGridX = x;
		return;
	}
	
	public int getGridX()
	{
		return this.mGridX;
	}
	
	public void setGridY(int y)
	{
		this.mGridY = y;
		return;
	}
	
	public int getGridY()
	{
		return this.mGridY;
	}
	
	public void rotateLeft()
	{
		/*
		 * Plus one, because radians turns counter-clockwise, and the DIR array goes from left to right.
		 */
		this.mRotationIndex = Math.floorMod(this.mRotationIndex + 1, 4);
		PipeNode temp = this.north;
		this.north = this.east;
		this.east = this.south;
		this.south = this.west;
		this.west = temp;
		return;
	}
	
	public void rotateRight()
	{
		/*
		 * Minus one, because radians turns counter-clockwise, and the DIR array goes from left to right.
		 */
		this.mRotationIndex = Math.floorMod(this.mRotationIndex - 1, 4);
		PipeNode temp = this.north;
		this.north = this.west;
		this.west = this.south;
		this.south = this.east;
		this.east = temp;
		return;
	}
	
	public void setRotationIndex(int index)
	{
		this.mRotationIndex = index;
		return;
	}
	
	public void setRotation(double factor)
	{
		this.mRotationIndex = this.getIndexForAngle(factor);
		return;
	}
	
	public double getRotation()
	{
		return DIR[this.mRotationIndex];
	}
	
	private int getIndexForAngle(double factor)
	{
		if(factor > 0.25 && factor <= 0.75){
			return 1;
		}else if(factor > 0.75 && factor <= 1.25){
			return 2;
		}else if(factor > 1.25 && factor <= 1.75){
			return 3;
		}else if(factor > 1.75 || factor <= 0.25){
			return 0;
		}
//		if(angle == 0.0){
//			return 0;
//		}else if(angle == 0.5){
//			return 1;
//		}else if(angle == 1.0){
//			return 2;
//		}else{
//			return 3;
//		}
		return 0;
	}
	
	public void setImageKey(Object key)
	{
		this.mImageKey = key;
		return;
	}
	
	public Object getImageKey()
	{
		return this.mImageKey;
	}
	
	public void setSolutionData(SolutionData solution)
	{
		this.mSolution = solution;
		return;
	}
	
	public SolutionData getSolutionData()
	{
		return this.mSolution;
	}
}

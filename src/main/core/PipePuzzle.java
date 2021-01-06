package main.core;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import main.core.util.DisjointSet;
import main.core.util.MathUtil;

public class PipePuzzle
{
	public enum ImageKey
	{
		CROSS(XMLTag.CROSS), 
		ELBOW(XMLTag.ELBOW), 
		GREEN_GAUGE(XMLTag.GREEN_GAUGE), 
		PIPE(XMLTag.PIPE), 
		RED_GAUGE(XMLTag.RED_GAUGE), 
		T_JUNCTION(XMLTag.T_JUNCTION), 
		TILE(XMLTag.TILE);
		
		private String mTag = null;
		
		private ImageKey(String tag)
		{
			this.mTag = tag;
			return;
		}
		
		public String getTag()
		{
			return this.mTag;
		}
		
		public static ImageKey getTypeForName(String name)
		{
			for(ImageKey key : ImageKey.values())
			{
				if(key.name().equalsIgnoreCase(name)){
					return key;
				}
			}
			return null;
		}
	};
	
	public enum Orientation
	{
		HORIZONTAL, VERTICAL;
		
		public static Orientation getTypeForName(String name)
		{
			for(Orientation type : Orientation.values())
			{
				if(type.name().equalsIgnoreCase(name)){
					return type;
				}
			}
			return null;
		}
	};
	
	private int mGridSize = 0;
	private PipeNode[][] mGrid = null;
	private PipeNode mSelected = null;
	private HashMap<Object, BufferedImage> mTemplates = new HashMap<Object, BufferedImage>();
	private ArrayList<PipeNode> mPipeList = new ArrayList<PipeNode>();
	private ArrayList<PipeNode> mStarts = new ArrayList<PipeNode>();
	private ArrayList<PipeNode> mGoals = new ArrayList<PipeNode>();
	private ArrayList<SolutionData> mSolution = new ArrayList<SolutionData>();
	
	public PipePuzzle(int gridSize)
	{
		this.mGridSize = gridSize;
		this.mGrid = new PipeNode[gridSize][gridSize];
		return;
	}
	
	public void renderPuzzleGrid(Graphics2D g2d, Rectangle bounds)
	{
		for(int i = 0; i < this.mGridSize; i++)
		{
			for(int j = 0; j < this.mGridSize; j++)
			{
				g2d.drawImage(this.mTemplates.get(ImageKey.TILE), bounds.x + (this.mTemplates.get(ImageKey.TILE).getWidth() * j), bounds.y + (this.mTemplates.get(ImageKey.TILE).getWidth() * i), null);
			}
		}
		return;
	}
	
	public void renderParts(Graphics2D g2d, Rectangle bounds, boolean exceptSelected)
	{
		AffineTransform original = g2d.getTransform();
		for(int y = 0; y < this.mGridSize; y++)
		{
			for(int x = 0; x < this.mGridSize; x++)
			{
				PipeNode part = this.mGrid[x][y];
				if(part == null){
					continue;
				}
				if(exceptSelected && part == this.mSelected){
					continue;
				}
				Point screen = this.gridToScreen(bounds, x, y);
				g2d.rotate(-part.getRotation(), screen.x + (this.mTemplates.get(part.getImageKey()).getWidth() / 2.0), screen.y + (this.mTemplates.get(part.getImageKey()).getHeight() / 2.0));
				g2d.drawImage(this.mTemplates.get(part.getImageKey()), screen.x, screen.y, null);
				g2d.setTransform(original);
			}
		}
		return;
	}
	
	public Point gridToScreen(Rectangle bounds, int gridX, int gridY)
	{
		int x = bounds.x + (this.mTemplates.get(ImageKey.TILE).getWidth() * gridX);
		int y = bounds.y + (this.mTemplates.get(ImageKey.TILE).getHeight() * gridY);
		return new Point(x, y);
	}
	
	public Point screenToGrid(Rectangle bounds, int screenX, int screenY, double tileWidth)
	{
		int x = (int)Math.floor((screenX - bounds.x) / tileWidth);
		int y = (int)Math.floor((screenY - bounds.y) / tileWidth);
		return new Point(x, y);
	}
	
	public void renderMovingSelected(Graphics2D g2d, int x, int y)
	{
		AffineTransform original = g2d.getTransform();
		g2d.rotate(-this.mSelected.getRotation(), x + (this.mTemplates.get(this.mSelected.getImageKey()).getWidth() / 2.0), y + (this.mTemplates.get(this.mSelected.getImageKey()).getHeight() / 2.0));
		g2d.drawImage(this.mTemplates.get(this.mSelected.getImageKey()), x, y, null);
		g2d.setTransform(original);
		return;
	}
	
	public void addStart(PipeNode start)
	{
		this.mStarts.add(start);
		return;
	}
	
	public void addGoal(PipeNode goal)
	{
		this.mGoals.add(goal);
		return;
	}
	
	public void setSolution(ArrayList<SolutionData> solution)
	{
		this.mSolution = solution;
		return;
	}
	
	public ArrayList<SolutionData> getSolution()
	{
		return this.mSolution;
	}
	
	public void selectPart(int x, int y)
	{
		if(!this.notStationaryPart(x, y)){
			return;
		}
		this.mSelected = this.mGrid[x][y];
		//System.out.println("part selected at " + x + ", " + y);
		//System.out.println("north=" + this.mSelected.north + ", east=" + this.mSelected.east + ", south=" + this.mSelected.south + ", west=" + this.mSelected.west);
		return;
	}
	
	public void selectPart(PipeNode node)
	{
		this.mSelected = node;
		return;
	}
	
	public void unselectPart()
	{
		this.mSelected = null;
		return;
	}
	
	public PipeNode getSelectedPart()
	{
		return this.mSelected;
	}
	
	public void movePart(int gridX, int gridY)
	{
		if(this.mSelected == null){
			return;
		}
		if(!this.targetIsEmpty(gridX, gridY)){
			return;
		}
		this.disconnectPart(this.mSelected.getGridX(), this.mSelected.getGridY());
		this.mGrid[this.mSelected.getGridX()][this.mSelected.getGridY()] = null;
		this.mSelected.setGridX(gridX);
		this.mSelected.setGridY(gridY);
		this.mGrid[gridX][gridY] = this.mSelected;
		this.connectPart(gridX, gridY);
		//ArrayList<PipeNode> path = this.getPath();
		this.checkFinished();
		return;
	}
	
	public void movePartUnchecked(int gridX, int gridY)
	{
		/*
		 * All parts need to be disconnected for unchecked moves and rotations.
		 */
		if(this.mSelected == null){
			return;
		}
		this.mGrid[this.mSelected.getGridX()][this.mSelected.getGridY()] = null;
		this.mSelected.setGridX(gridX);
		this.mSelected.setGridY(gridY);
		this.mGrid[gridX][gridY] = this.mSelected;
		return;
	}
	
	public void rotateSelectedPart()
	{
		if(this.mSelected == null){
			return;
		}
		this.disconnectPart(this.mSelected.getGridX(), this.mSelected.getGridY());
		this.mSelected.rotateLeft();
		this.connectPart(this.mSelected.getGridX(), this.mSelected.getGridY());
		//ArrayList<PipeNode> path = this.getPath();
		this.checkFinished();
		return;
	}
	
	public void rotateSelectedPart(double factor)
	{
		if(this.mSelected == null){
			return;
		}
		this.disconnectPart(this.mSelected.getGridX(), this.mSelected.getGridY());
		this.mSelected.setRotation(factor);
		this.connectPart(this.mSelected.getGridX(), this.mSelected.getGridY());
		this.checkFinished();
		return;
	}
	
	private void disconnectPart(int x, int y)
	{
		if(this.mGrid[x][y].north != null && !(this.mGrid[x][y].north instanceof PipeCap)){
			this.mGrid[x][y].north = null;
		}
		if(this.mGrid[x][y].east != null && !(this.mGrid[x][y].east instanceof PipeCap)){
			this.mGrid[x][y].east = null;
		}
		if(this.mGrid[x][y].south != null && !(this.mGrid[x][y].south instanceof PipeCap)){
			this.mGrid[x][y].south = null;
		}
		if(this.mGrid[x][y].west != null && !(this.mGrid[x][y].west instanceof PipeCap)){
			this.mGrid[x][y].west = null;
		}
		//System.out.println("[x][y] north=" + this.mGrid[x][y].north + " east=" + this.mGrid[x][y].east + " south=" + this.mGrid[x][y].south + " west=" + this.mGrid[x][y].west);
		if(y > 0 && this.mGrid[x][y - 1] != null){
			if(this.mGrid[x][y - 1].south != null && !(this.mGrid[x][y - 1].south instanceof PipeCap)){
				this.mGrid[x][y - 1].south = null;
			}
		}
		if(x < this.mGridSize - 1 && this.mGrid[x + 1][y] != null){
			if(this.mGrid[x + 1][y].west != null && !(this.mGrid[x + 1][y].west instanceof PipeCap)){
				this.mGrid[x + 1][y].west = null;
			}
		}
		if(y < this.mGridSize - 1 && this.mGrid[x][y + 1] != null){
			if(this.mGrid[x][y + 1].north != null && !(this.mGrid[x][y + 1].north instanceof PipeCap)){
				this.mGrid[x][y + 1].north = null;
			}
		}
		if(x > 0 && this.mGrid[x - 1][y] != null){
			if(this.mGrid[x - 1][y].east != null && !(this.mGrid[x - 1][y].east instanceof PipeCap)){
				this.mGrid[x - 1][y].east = null;
			}
		}
		return;
	}
	
	private void connectPart(int x, int y)
	{
		if(this.mGrid[x][y].north == null || !(this.mGrid[x][y].north instanceof PipeCap)){
			if(y > 0 && this.mGrid[x][y - 1] != null && (this.mGrid[x][y - 1].south == null || !(this.mGrid[x][y - 1].south instanceof PipeCap))){
				this.mGrid[x][y].north = this.mGrid[x][y - 1];
				this.mGrid[x][y - 1].south = this.mGrid[x][y];
			}
		}
		if(this.mGrid[x][y].east == null || !(this.mGrid[x][y].east instanceof PipeCap)){
			if(x < this.mGridSize - 1 && this.mGrid[x + 1][y] != null && (this.mGrid[x + 1][y].west == null || !(this.mGrid[x + 1][y].west instanceof PipeCap))){
				this.mGrid[x][y].east = this.mGrid[x + 1][y];
				this.mGrid[x + 1][y].west = this.mGrid[x][y];
			}
		}
		if(this.mGrid[x][y].south == null || !(this.mGrid[x][y].south instanceof PipeCap)){
			if(y < this.mGridSize - 1 && this.mGrid[x][y + 1] != null && (this.mGrid[x][y + 1].north == null || !(this.mGrid[x][y + 1].north instanceof PipeCap))){
				this.mGrid[x][y].south = this.mGrid[x][y + 1];
				this.mGrid[x][y + 1].north = this.mGrid[x][y];
			}
		}
		if(this.mGrid[x][y].west == null || !(this.mGrid[x][y].west instanceof PipeCap)){
			if(x > 0 && this.mGrid[x - 1][y] != null && (this.mGrid[x - 1][y].east == null || !(this.mGrid[x - 1][y].east instanceof PipeCap))){
				this.mGrid[x][y].west = this.mGrid[x - 1][y];
				this.mGrid[x - 1][y].east = this.mGrid[x][y];
			}
		}
		//System.out.println("[x][y] north=" + this.mGrid[x][y].north + " east=" + this.mGrid[x][y].east + " south=" + this.mGrid[x][y].south + " west=" + this.mGrid[x][y].west);
		return;
	}
	
	public boolean targetIsEmpty(int x, int y)
	{
		return this.mGrid[x][y] == null;
	}
	
	public boolean notStationaryPart(int x, int y)
	{
		for(PipeNode s : this.mStarts)
		{
			if(s.getGridX() == x && s.getGridY() == y){
				return false;
			}
		}
		for(PipeNode g : this.mGoals)
		{
			if(g.getGridX() == x && g.getGridY() == y){
				return false;
			}
		}
		return true;
	}
	
	public void shuffleParts(ArrayList<PipeNode> parts)
	{
		this.mPipeList = parts;
		
		/* Start by placing the starts and goals in position.  These won't be randomized */
		for(int i = 0; i < this.mStarts.size(); i++)
		{
			this.mGrid[this.mStarts.get(i).getGridX()][this.mStarts.get(i).getGridY()] = this.mStarts.get(i);
		}
		for(int i = 0; i < this.mGoals.size(); i++)
		{
			this.mGrid[this.mGoals.get(i).getGridX()][this.mGoals.get(i).getGridY()] = this.mGoals.get(i);
		}
		
		/* Make a list of all unoccupied grid squares */
		ArrayList<Integer> empties = new ArrayList<Integer>();
		for(int y = 0; y < this.mGridSize; y++)
		{
			for(int x = 0; x < this.mGridSize; x++)
			{
				if(this.targetIsEmpty(x, y)){
					empties.add(y + (this.mGridSize * x));
				}
			}
		}
		
		Iterator<PipeNode> iterator = this.getIterator();
		for(int i = empties.size() - 1; i >= 0; i--)
		{
			/* Randomly choose and empty grid position and place a pipe-piece there. */
			int index = (int)Math.floor(Math.random() * (empties.size() - 1));
			int x = (int)Math.floor(empties.get(index) / (double)this.mGridSize);
			int y = Math.floorMod(empties.get(index), this.mGridSize);
			if(!iterator.hasNext()){
				break;
			}
			PipeNode node = iterator.next();
			node.setGridX(x);
			node.setGridY(y);
			//this.mGrid[x][y] = node;
			/* Randomly rotate the pipe-piece */
			int rotateTimes = MathUtil.getRandomInt(0, 3);
			for(int k = 0; k < rotateTimes; k++)
			{
				node.rotateLeft();
			}
			empties.remove(index);
		}
		return;
	}
	
	public void connectAllParts()
	{
		Iterator<PipeNode> iterator = this.mPipeList.iterator();
		while(iterator.hasNext())
		{
			PipeNode node = iterator.next();
			this.connectPart(node.getGridX(), node.getGridY());
		}
		return;
	}
	
	public void disconnectAllParts()
	{
		Iterator<PipeNode> iterator = this.mPipeList.iterator();
		while(iterator.hasNext())
		{
			PipeNode node = iterator.next();
			this.disconnectPart(node.getGridX(), node.getGridY());
		}
		return;
	}
	
	public void clearGrid()
	{
		Iterator<PipeNode> iterator = this.mPipeList.iterator();
		while(iterator.hasNext())
		{
			PipeNode node = iterator.next();
			this.mGrid[node.getGridX()][node.getGridY()] = null;
		}
		return;
	}
	
	public PipeNode createElbowPart(Object key)
	{
		PipeNode n = new PipeNode(new PipeCap(), null, null, new PipeCap(), key);
		return n;
	}
	
	public PipeNode createPipePart(Object key)
	{
		PipeNode n = new PipeNode(new PipeCap(), null, new PipeCap(), null, key);
		return n;
	}
	
	public PipeNode createTJunctionPart(Object key)
	{
		PipeNode n = new PipeNode(null, null, null, new PipeCap(), key);
		return n;
	}
	
	public PipeNode createCrossPart(Object key)
	{
		PipeNode n = new PipeNode(null, null, null, null, key);
		return n;
	}
	
	public PipeNode createStartPart(Object key, Orientation orientation)
	{
		PipeNode node = null;
		if(orientation.equals(Orientation.HORIZONTAL)){
			node = new PipeNode(new PipeCap(), null, new PipeCap(), null, key);
			node.setRotationIndex(0); // Zero or two is a horizontal angle.
		}else{
			node = new PipeNode(null, new PipeCap(), null, new PipeCap(), key);
			node.setRotationIndex(1); // One or three is a vertical angle.
		}
		return node;
	}
	
	public PipeNode createGoalPart(Object key, Orientation orientation)
	{
		return this.createStartPart(key, orientation);
	}
	
	public int getSize()
	{
		return this.mGridSize;
	}
	
	public void clearPuzzleImages()
	{
		this.mTemplates.clear();
		return;
	}
	
	public void addPartImage(Object key, BufferedImage image)
	{
		this.mTemplates.put(key, image);
		return;
	}
	
	public BufferedImage getPartImage(Object key)
	{
		return this.mTemplates.get(key);
	}
	
	public Iterator<PipeNode> getIterator()
	{
		return this.mPipeList.iterator();
	}
	
	public void checkFinished()
	{
		this.clearAllGoalLights();
		for(PipeNode start : this.mStarts)
		{
			ArrayList<PipeNode> path = PipeNodePath.find(start);
			DisjointSet<PipeNode> dj = new DisjointSet<PipeNode>();
			dj.createSet(start);
			Iterator<PipeNode> iterator = this.getIterator();
			while(iterator.hasNext())
			{
				dj.createSet(iterator.next());
			}
			for(PipeNode goal : this.mGoals)
			{
				dj.createSet(goal);
			}
			for(int i = 0; i < path.size() - 1; i++)
			{
				dj.union(path.get(i), path.get(i + 1));
			}
			this.updateGoalLights(start, dj);
		}
		return;
	}
	
	private void updateGoalLights(PipeNode start, DisjointSet<PipeNode> dj)
	{
		for(PipeNode goal : this.mGoals)
		{
			if(dj.findSet(goal) == start){
				goal.setImageKey(ImageKey.GREEN_GAUGE);
			}
		}
		return;
	}
	
	private void clearAllGoalLights()
	{
		for(PipeNode goal : this.mGoals)
		{
			goal.setImageKey(ImageKey.RED_GAUGE);
		}
		return;
	}
	
	public ArrayList<PipeNode> getPath(PipeNode start)
	{
		return PipeNodePath.find(start);
	}
}

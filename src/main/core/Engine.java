package main.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import main.core.PipePuzzle.ImageKey;
import main.core.PipePuzzle.Orientation;
import main.core.util.ImageLoader;
import main.core.util.ImageUtils;
import main.core.util.MathUtil;
import main.core.util.Ticker;
import main.core.util.TimeUtil;
import main.core.util.XMLReader;
import main.core.util.XMLUtil;

public class Engine
{
	private Engine.Mediator mMediator = null;
	private PipePuzzle mPuzzle = null;
	private Rectangle mBounds = new Rectangle();
	private int mTileWidth = 0;
	private Difficulty mDifficulty = Difficulty.EASY;
	private long mStart = 0L;
	private HashMap<Difficulty, Integer> mCurrentPuzzleNumbers = new HashMap<Difficulty, Integer>();
	private HashMap<Difficulty, String> mDataPaths = null;
	private HashMap<Object, BufferedImage> mOriginals = new HashMap<Object, BufferedImage>();
	private ArrayList<AnimatedAction> mAnimations = new ArrayList<AnimatedAction>();
	private ResourceBundle mMainResource = null;
	
	private GameState mShuffleState = new ShuffleAnimationState();
	private GameState mNormalPlay = new NormalPlayState();
	private GameState mSolutionState = new SolutionAnimationState();
	private GameState mCurrentState = this.mShuffleState;
	
	public Engine(Engine.Mediator mediator, HashMap<Difficulty, String> dataPaths)
	{
		this.mMediator = mediator;
		this.mDataPaths = dataPaths;
		this.initialize();
		return;
	}
	
	private void initialize()
	{
		this.mCurrentPuzzleNumbers.put(Difficulty.EASY, 0);
		this.mCurrentPuzzleNumbers.put(Difficulty.MEDIUM, 0);
		this.mCurrentPuzzleNumbers.put(Difficulty.HARD, 0);
		this.mMainResource = this.mMediator.getMainResource();
		return;
	}
	
	public void update()
	{
		this.mCurrentState.update();
		return;
	}
	
	public void render(Graphics2D g2d)
	{
		this.mCurrentState.render(g2d);
		return;
	}
	
	public String getTimeStatus(long millis)
	{
		StringBuilder caption = new StringBuilder();
		caption.append(this.mMainResource.getString("TIME_CAPTION"));
		caption.append(" ");
		caption.append(millis <= 0L ? 0L : TimeUtil.printShortTimeDuration(millis));
		return caption.toString();
	}
	
	public String getDifficultyStatus()
	{
		StringBuilder caption = new StringBuilder();
		caption.append(this.mMainResource.getString("DIFFICULTY_CAPTION"));
		caption.append(" ");
		caption.append(this.mMainResource.getString(this.mDifficulty.getI18NCaption()));
		return caption.toString();
	}
	
	public String getPuzzleNumberStatus()
	{
		StringBuilder caption = new StringBuilder();
		caption.append(this.mMainResource.getString("PUZZLE_NUMBER_CAPTION"));
		caption.append(this.mCurrentPuzzleNumbers.get(this.mDifficulty) + 1);
		return caption.toString();
	}
	
	public void renderCursor(Graphics2D g2d)
	{
		if(!this.mMediator.getShowCursor()){
			return;
		}
		
		/* Draw the selected image under the cursor if there is one selected. */
		if(this.mMediator.getMouseIsDragging() && this.mPuzzle.getSelectedPart() != null){
			int x = this.mMediator.getMousePosition().x;
			int y = this.mMediator.getMousePosition().y;
			this.mPuzzle.renderMovingSelected(g2d, x - this.mMediator.getXDiff(), y - this.mMediator.getYDiff());
		}
		return;
	}
	
	public void setDifficulty(Difficulty dif)
	{
		this.mDifficulty = dif;
		return;
	}
	
	public Difficulty getDifficulty()
	{
		return this.mDifficulty;
	}
	
	public void loadNewPuzzle(Difficulty dif, int puzzleNum)
	{
		XMLReader reader = XMLUtil.getReaderResource(this.mDataPaths.get(dif), XMLTag.PUZZLES);
		ArrayList<PipePuzzleData> data = new XMLReadPuzzles(reader).read();
		puzzleNum = MathUtil.clamp(0, data.size() - 1, puzzleNum);
		this.mPuzzle = new PipePuzzle(data.get(puzzleNum).size);
		/* The Math.min of the display's width or height ensures that the game board will fit inside the display bounds. */
		int sizeRef = Math.min(this.mMediator.getGameBoardSize().width, this.mMediator.getGameBoardSize().height);
		double tileSize = sizeRef / (double)data.get(puzzleNum).size;
		tileSize -= data.get(puzzleNum).size; /* gives it a little space from the edges of the display.  Looks better to me. */
		this.loadBaseImages();
		this.resetPuzzleImages(tileSize);
		this.createStartsGoals(this.mPuzzle, data.get(puzzleNum));
		this.mPuzzle.setSolution(data.get(puzzleNum).solution);
		ArrayList<PipeNode> parts = this.createParts(data.get(puzzleNum));
		this.mPuzzle.shuffleParts(parts);
		this.setTileWidth((int)Math.round(tileSize));
		this.mStart = 0L;
		this.displayDifficultyData(dif);
		this.mMediator.displayDifficultyStatus(this.getDifficultyStatus());
		this.mMediator.displayPuzzleNumberStatus(this.getPuzzleNumberStatus());
		this.mMediator.displayTimeDuration(this.getTimeStatus(0L));
		return;
	}
	
	public void resizeUIPerformed()
	{
		if(this.mMediator.getGameBoardSize() == null){
			return;
		}
		if(this.mPuzzle == null){
			return;
		}
		
		/* The Math.min of the display's width or height ensures that the game board will fit inside the display bounds. */
		int sizeRef = Math.min(this.mMediator.getGameBoardSize().width, this.mMediator.getGameBoardSize().height);
		double tileSize = sizeRef / (double)this.mPuzzle.getSize();
		tileSize -= this.mPuzzle.getSize(); /* gives it a little space from the edges of the display.  Looks better to me. */
		this.resetPuzzleImages(tileSize);
		this.setTileWidth((int)Math.round(tileSize));
		return;
	}
	
	private ArrayList<PipeNode> createParts(PipePuzzleData data)
	{
		ArrayList<PipeNode> parts = new ArrayList<PipeNode>();
		try{
			for(SolutionData s : data.solution)
			{
				if(s.type.equals(PipePuzzle.ImageKey.ELBOW)){
					parts.add(this.createElbowPart(this.mPuzzle, s));
				}else if(s.type.equals(PipePuzzle.ImageKey.PIPE)){
					parts.add(this.createPipePart(this.mPuzzle, s));
				}else if(s.type.equals(PipePuzzle.ImageKey.T_JUNCTION)){
					parts.add(this.createTJunctionPart(this.mPuzzle, s));
				}else if(s.type.equals(PipePuzzle.ImageKey.CROSS)){
					parts.add(this.createCrossPart(this.mPuzzle, s));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return parts;
	}
	
	private void loadBaseImages()
	{
		try{
			this.mOriginals.clear();
			BufferedImage image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.CROSS.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.CROSS, image);
			image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.ELBOW.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.ELBOW, image);
			image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.PIPE.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.PIPE, image);
			image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.T_JUNCTION.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.T_JUNCTION, image);
			image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.GREEN_GAUGE.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.GREEN_GAUGE, image);
			image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.RED_GAUGE.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.RED_GAUGE, image);
			image = ImageLoader.getImageFromResourcePath(Engine.class.getResource(this.mMediator.readImagePath(PipePuzzle.ImageKey.TILE.getTag())));
			this.mOriginals.put(PipePuzzle.ImageKey.TILE, image);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return;
	}
	
	private void resetPuzzleImages(double tileSize)
	{
		try{
			this.mPuzzle.clearPuzzleImages();
			BufferedImage image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.CROSS), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.CROSS, image);
			image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.ELBOW), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.ELBOW, image);
			image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.PIPE), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.PIPE, image);
			image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.T_JUNCTION), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.T_JUNCTION, image);
			image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.GREEN_GAUGE), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.GREEN_GAUGE, image);
			image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.RED_GAUGE), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.RED_GAUGE, image);
			image = ImageUtils.resizeImage(this.mOriginals.get(PipePuzzle.ImageKey.TILE), tileSize, tileSize);
			this.mPuzzle.addPartImage(PipePuzzle.ImageKey.TILE, image);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return;
	}
	
	private PipeNode createElbowPart(PipePuzzle puzzle, SolutionData solution)
	{
		PipeNode elbow = puzzle.createElbowPart(PipePuzzle.ImageKey.ELBOW);
		elbow.setSolutionData(solution);
		return elbow;
	}
	
	private PipeNode createPipePart(PipePuzzle puzzle, SolutionData solution)
	{
		PipeNode pipe = puzzle.createPipePart(PipePuzzle.ImageKey.PIPE);
		pipe.setSolutionData(solution);
		return pipe;
	}
	
	private PipeNode createTJunctionPart(PipePuzzle puzzle, SolutionData solution)
	{
		PipeNode tJunction = puzzle.createTJunctionPart(PipePuzzle.ImageKey.T_JUNCTION);
		tJunction.setSolutionData(solution);
		return tJunction;
	}
	
	private PipeNode createCrossPart(PipePuzzle puzzle, SolutionData solution)
	{
		PipeNode cross = puzzle.createCrossPart(PipePuzzle.ImageKey.CROSS);
		cross.setSolutionData(solution);
		return cross;
	}
	
	private void createStartsGoals(PipePuzzle puzzle, PipePuzzleData data)
	{
		for(int i = 0; i < data.starts.size(); i++)
		{
			PipeNode ps = puzzle.createStartPart(PipePuzzle.ImageKey.GREEN_GAUGE, PipePuzzle.Orientation.HORIZONTAL);
			ps.setGridX(data.starts.get(i).x);
			ps.setGridY(data.starts.get(i).y);
			puzzle.addStart(ps);
		}
		for(int i = 0; i < data.goals.size(); i++)
		{
			PipeNode pg = puzzle.createGoalPart(PipePuzzle.ImageKey.RED_GAUGE, PipePuzzle.Orientation.HORIZONTAL);
			pg.setGridX(data.goals.get(i).x);
			pg.setGridY(data.goals.get(i).y);
			puzzle.addGoal(pg);
		}
		return;
	}
	
	public void partSelectionRequested(Point screenPoint)
	{
		if(screenPoint == null){
			return;
		}
		if(!this.mBounds.contains(screenPoint)){
			return;
		}
		Point gridPoint = this.mPuzzle.screenToGrid(this.mBounds, screenPoint.x, screenPoint.y, this.mTileWidth);
		if(this.mPuzzle.targetIsEmpty(gridPoint.x, gridPoint.y)){
			return;
		}
		this.mPuzzle.selectPart(gridPoint.x, gridPoint.y);
		if(this.mStart <= 0L){
			this.mStart = System.currentTimeMillis();
		}
		return;
	}
	
	public void partRotateRequested(Point p)
	{
		if(p == null){
			return;
		}
		if(!this.mBounds.contains(p)){
			return;
		}
		int x = (int)Math.floor((p.x - this.mBounds.x) / (double)this.mTileWidth);
		int y = (int)Math.floor((p.y - this.mBounds.y) / (double)this.mTileWidth);
		if(this.mPuzzle.targetIsEmpty(x, y)){
			return;
		}
		if(!this.mPuzzle.notStationaryPart(x, y)){
			return;
		}
		this.mPuzzle.rotateSelectedPart();
		return;
	}
	
	public void partMoveRequested(Point p)
	{
		if(p == null){
			return;
		}
		if(!this.mBounds.contains(p)){
			return;
		}
		int x = (int)Math.floor((p.x - this.mBounds.x) / (double)this.mTileWidth);
		int y = (int)Math.floor((p.y - this.mBounds.y) / (double)this.mTileWidth);
		if(!this.mPuzzle.targetIsEmpty(x, y)){
			return;
		}
		this.mPuzzle.movePart(x, y);
		return;
	}
	
	public void partUnselectionRequested()
	{
		this.mPuzzle.unselectPart();
		return;
	}
	
	private void setTileWidth(int width)
	{
		this.mTileWidth = width;
		this.mBounds = new Rectangle();
		this.mBounds.width = this.mTileWidth * this.mPuzzle.getSize();
		this.mBounds.height = this.mTileWidth * this.mPuzzle.getSize();
		this.mBounds.x = (int)Math.round((this.mMediator.getGameBoardSize().getWidth() - this.mBounds.width) / 2.0);
		this.mBounds.y = (int)Math.round((this.mMediator.getGameBoardSize().getHeight() - this.mBounds.height) / 2.0);
		return;
	}
	
	public int getTileWidth()
	{
		return this.mTileWidth;
	}
	
	public Rectangle getBounds()
	{
		return new Rectangle(this.mBounds);
	}
	
	public void displayDifficultyData(Difficulty dif)
	{
		XMLReader reader = XMLUtil.getReaderResource(this.mDataPaths.get(dif), XMLTag.PUZZLES);
		ArrayList<PipePuzzleData> data = new XMLReadPuzzles(reader).read();
		StringBuilder text = new StringBuilder();
		text.append("#");
		text.append(String.valueOf(this.mCurrentPuzzleNumbers.get(dif) + 1)); // zero-based array index to countable number.
		text.append(" of ");
		text.append(String.valueOf(data.size()));
		this.mMediator.displayDifficultyData(text.toString());
		return;
	}
	
	private ArrayList<PipePuzzleData> seekPuzzle()
	{
		XMLReader reader = XMLUtil.getReaderResource(this.mDataPaths.get(this.mDifficulty), XMLTag.PUZZLES);
		return new XMLReadPuzzles(reader).read();
	}
	
	private void seekCurrentPuzzle()
	{
		ArrayList<PipePuzzleData> data = this.seekPuzzle();
		int current = this.mCurrentPuzzleNumbers.get(this.mDifficulty);
		current = current >= data.size() ? 0 : current < 0 ? data.size() - 1 : current;
		this.loadNewPuzzle(this.mDifficulty, this.mCurrentPuzzleNumbers.get(this.mDifficulty));
		return;
	}
	
	private void seekNextPuzzle()
	{
		ArrayList<PipePuzzleData> data = this.seekPuzzle();
		int current = this.mCurrentPuzzleNumbers.get(this.mDifficulty) + 1;
		current = current >= data.size() ? 0 : current < 0 ? data.size() - 1 : current;
		this.mCurrentPuzzleNumbers.put(this.mDifficulty, current);
		this.loadNewPuzzle(this.mDifficulty, this.mCurrentPuzzleNumbers.get(this.mDifficulty));
		return;
	}
	
	private void seekPreviousPuzzle()
	{
		ArrayList<PipePuzzleData> data = this.seekPuzzle();
		int current = this.mCurrentPuzzleNumbers.get(this.mDifficulty) - 1;
		current = current >= data.size() ? 0 : current < 0 ? data.size() - 1 : current;
		this.mCurrentPuzzleNumbers.put(this.mDifficulty, current);
		this.loadNewPuzzle(this.mDifficulty, this.mCurrentPuzzleNumbers.get(this.mDifficulty));
		return;
	}
	
	private void seekFirstPuzzle()
	{
		this.mCurrentPuzzleNumbers.put(this.mDifficulty, 0);
		this.loadNewPuzzle(this.mDifficulty, this.mCurrentPuzzleNumbers.get(this.mDifficulty));
		return;
	}
	
	private void seekLastPuzzle()
	{
		ArrayList<PipePuzzleData> data = this.seekPuzzle();
		this.mCurrentPuzzleNumbers.put(this.mDifficulty, data.size() - 1);
		this.loadNewPuzzle(this.mDifficulty, this.mCurrentPuzzleNumbers.get(this.mDifficulty));
		return;
	}
	
	private Point getRandomEdgeCoordinate()
	{
		int x = 0;
		int y = 0;
		if(Math.random() < 0.5){
			if(Math.random() < 0.5){
				x = MathUtil.getRandomInt(mBounds.x, mBounds.x + mBounds.width);
				y = mBounds.y;
			}else{
				x = mBounds.x;
				y = MathUtil.getRandomInt(mBounds.y, mBounds.y + mBounds.height);
			}
		}else{
			if(Math.random() < 0.5){
				x = MathUtil.getRandomInt(mBounds.x, mBounds.x + mBounds.width);
				y = mBounds.y + mBounds.height;
			}else{
				x = mBounds.x + mBounds.width;
				y = MathUtil.getRandomInt(mBounds.y, mBounds.y + mBounds.height);
			}
		}
		return new Point(x, y); 
	}
	
	private void runSolution()
	{
		this.mCurrentState = this.mSolutionState;
		this.mCurrentState.reset();
		return;
	}
	
	private class ShuffleAnimationState implements GameState
	{
		@Override
		public void reset()
		{
			Iterator<PipeNode> iterator = mPuzzle.getIterator();
			while(iterator.hasNext())
			{
				PipeNode node = iterator.next();
				AnimatedAction action = new AnimatedAction(createAnimatedActionMediator(node), node.getImageKey());
				Point start = getRandomEdgeCoordinate();
				action.setX(start.x);
				action.setY(start.y);
				Point dest = mPuzzle.gridToScreen(mBounds, node.getGridX(), node.getGridY());
				action.setBehavior(new MovementBehavior(createMovementBehavior(action), 1.0, 5.0, dest.x, dest.y, Math.PI * node.getRotation()));
				mAnimations.add(action);
			}
			return;
		}
		
		@Override
		public void update()
		{
			for(int i = mAnimations.size() - 1; i >= 0; i--)
			{
				if(mAnimations.get(i).readyForDisposal()){
					
					mAnimations.remove(i);
					if(mAnimations.isEmpty()){
						mPuzzle.connectAllParts();
					}
					continue;
				}
				mAnimations.get(i).update();
			}
			
			if(mAnimations.isEmpty()){
				/* 
				 * What if this shuffling randomly places a part, or two, in their correctly connected position to begin with?
				 * The connectPart() method must be called on all parts after shuffling, or else the PipeNodePath.find() method 
				 * will later miss the parts that started off connected after shuffling. 
				 */
				mPuzzle.connectAllParts();
			}
			
			return;
		}
		
		@Override
		public void render(Graphics2D g2d)
		{
			if(mPuzzle == null){
				return;
			}
			g2d.setPaint(new Color(0, 0, 0, 255));
			g2d.fillRect(0, 0, mMediator.getGameBoardSize().width, mMediator.getGameBoardSize().height);
			mPuzzle.renderPuzzleGrid(g2d, mBounds);
			for(int i = mAnimations.size() - 1; i >= 0; i--)
			{
				mAnimations.get(i).render(g2d);
			}
			return;
		}
	}
	
	private class NormalPlayState implements GameState
	{
		@Override
		public void reset()
		{
			return;
		}
		
		@Override
		public void update()
		{
			if(mStart > 0L){
				mMediator.displayTimeDuration(getTimeStatus(System.currentTimeMillis() - mStart));
			}
			return;
		}
		
		@Override
		public void render(Graphics2D g2d)
		{
			if(mPuzzle == null){
				return;
			}
			g2d.setPaint(new Color(0, 0, 0, 255));
			g2d.fillRect(0, 0, mMediator.getGameBoardSize().width, mMediator.getGameBoardSize().height);
			mPuzzle.renderPuzzleGrid(g2d, mBounds);
			boolean except = (mPuzzle.getSelectedPart() != null) && mMediator.getMouseIsDragging();
			mPuzzle.renderParts(g2d, mBounds, except);
			renderCursor(g2d);
			return;
		}
	}
	
	private class SolutionAnimationState implements GameState
	{
		@Override
		public void reset()
		{
			mPuzzle.disconnectAllParts();
			Iterator<PipeNode> iterator = mPuzzle.getIterator();
			while(iterator.hasNext())
			{
				PipeNode node = iterator.next();
				AnimatedAction action = new AnimatedAction(createAnimatedActionMediator(node), node.getImageKey());
				Point start = mPuzzle.gridToScreen(mBounds, node.getGridX(), node.getGridY());
				action.setX(start.x);
				action.setY(start.y);
				Point dest = mPuzzle.gridToScreen(mBounds, node.getSolutionData().x, node.getSolutionData().y);
				action.setBehavior(new MovementBehavior(createMovementBehavior(action), 1.0, 5.0, dest.x, dest.y, Math.PI * node.getSolutionData().angle));
				
				mAnimations.add(action);
			}
			return;
		}
		
		@Override
		public void update()
		{
			return;
		}
		
		@Override
		public void render(Graphics2D g2d)
		{
			if(mPuzzle == null){
				return;
			}
			g2d.setPaint(new Color(0, 0, 0, 255));
			g2d.fillRect(0, 0, mMediator.getGameBoardSize().width, mMediator.getGameBoardSize().height);
			mPuzzle.renderPuzzleGrid(g2d, mBounds);
			
			return;
		}
	}
	
	private AnimatedAction.Mediator createAnimatedActionMediator(PipeNode node)
	{
		return new AnimatedAction.Mediator()
		{
			@Override
			public void movePart()
			{
				//mPuzzle.selectPart(node.getGridX(), node.getGridY());
				mPuzzle.selectPart(node);
				mPuzzle.rotateSelectedPart(node.getSolutionData().angle);
				mPuzzle.unselectPart();
				//mPuzzle.selectPart(node.getGridX(), node.getGridY());
				mPuzzle.selectPart(node);
				//mPuzzle.movePart(node.getSolutionData().x, node.getSolutionData().y);
				mPuzzle.movePartUnchecked(node.getSolutionData().x, node.getSolutionData().y);
				mPuzzle.unselectPart();
				return;
			}
			
			@Override
			public BufferedImage getImage(Object key)
			{
				return mPuzzle.getPartImage(key);
			}
		};
	}
	
	private MovementBehavior.Mediator createMovementBehavior(AnimatedAction action)
	{
		return new MovementBehavior.Mediator()
		{
			@Override
			public void setX(double x)
			{
				action.setX(x);
				return;
			}
			
			@Override
			public double getX()
			{
				return action.getX();
			}
			
			@Override
			public void setY(double y)
			{
				action.setY(y);
				return;
			}
			
			@Override
			public double getY()
			{
				return action.getY();
			}
		};
	}
	
	public ExitGameObserver createExitGameObserver()
	{
		return new ExitGameObserver()
		{
			@Override
			public void actionPerformed()
			{
				mMediator.beginAppShutdown();
				return;
			}
		};
	}
	
	public EasyDifficultyObserver createEasyDifficultyObserver()
	{
		return new EasyDifficultyObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.mDifficulty = Difficulty.EASY;
				Engine.this.seekCurrentPuzzle();
				return;
			}
		};
	}
	
	public MediumDifficultyObserver createMediumDifficultyObserver()
	{
		return new MediumDifficultyObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.mDifficulty = Difficulty.MEDIUM;
				Engine.this.seekCurrentPuzzle();
				return;
			}
		};
	}
	
	public HardDifficultyObserver createHardDifficultyObserver()
	{
		return new HardDifficultyObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.mDifficulty = Difficulty.HARD;
				Engine.this.seekCurrentPuzzle();
				return;
			}
		};
	}
	
	public FirstPuzzleObserver createFirstPuzzleObserver()
	{
		return new FirstPuzzleObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.seekFirstPuzzle();
				return;
			}
		};
	}
	
	public LastPuzzleObserver createLastPuzzleObserver()
	{
		return new LastPuzzleObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.seekLastPuzzle();
				return;
			}
		};
	}
	
	public NextPuzzleObserver createNextPuzzleObserver()
	{
		return new NextPuzzleObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.seekNextPuzzle();
				return;
			}
		};
	}
	
	public PreviousPuzzleObserver createPreviousPuzzleObserver()
	{
		return new PreviousPuzzleObserver()
		{
			@Override
			public void actionPerformed()
			{
				Engine.this.seekPreviousPuzzle();
				return;
			}
		};
	}
	
	public ShuffleObserver createShuffleObserver()
	{
		return new ShuffleObserver()
		{
			@Override
			public void actionPerformed()
			{
				
				return;
			}
		};
	}
	
	public SolveObserver createSolveObserver()
	{
		return new SolveObserver()
		{
			@Override
			public void actionPerformed()
			{
				runSolution();
				return;
			}
		};
	}
	
	public interface Mediator
	{
		abstract ResourceBundle getAppResource();
		abstract ResourceBundle getMainResource();
		abstract String readImagePath(String xmlTag);
		abstract Dimension getGameBoardSize();
		abstract boolean getShowCursor();
		abstract Point getMousePosition();
		abstract boolean getMouseIsDragging();
		abstract int getXDiff();
		abstract int getYDiff();
		abstract void displayDifficultyData(String caption);
		abstract void displayDifficultyStatus(String caption);
		abstract void displayTimeDuration(String caption);
		abstract void displayPuzzleNumberStatus(String caption);
		abstract void beginAppShutdown();
	}
}

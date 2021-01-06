package main.core.util;

/**
 * Fixed time-step game timer with separate update and render events.
 * 
 * It's recommended to use the startTimer() and stopTimer() methods instead of the thread's start() and stop().
 * 
 * Based on article/code found at http://www.java-gaming.org/index.php/topic,24220.0.html
 * 
 * @author John McCullock
 * @version 1.0 2015-05-14
 */
public class Ticker extends Thread
{
	private final double BILLION = 1.0E9;
	
	private Ticker.Observer mObserver = null;
	
	private double mUpdateHertz = 60.0;
	private double mUpdateDelta = BILLION / this.mUpdateHertz; // Nanoseconds.
	private double mRenderHertz = 60; // Frames per second.
	private double mRenderDelta = BILLION / this.mRenderHertz;
	
	private int mMaxUpdatesBeforeRender = 5;
	
	private double mLastUpdate = System.nanoTime();
	private double mLastRender = System.nanoTime();
	private int mFPS = 0;

	private boolean mIsRunning = false;
	private boolean mStopRequested = false;
	private boolean mStatisticsEnabled = false;
	private boolean mAllowSleep = true;
	
	public Ticker(Ticker.Observer listener)
	{
		this.mObserver = listener;
		return;
	}
	
	public Ticker(Ticker.Observer listener, double updateHertz, double renderHertz, int maxUpdates)
	{
		this.mObserver = listener;
		this.setUpdateHertz(updateHertz);
		this.setRenderHertz(renderHertz);
		this.setMaxUpdateBeforeRender(maxUpdates);
		return;
	}
	
	public void startTimer()
	{
		this.start();
		return;
	}
	
	public void stopTimer()
	{
		this.mStopRequested = true;
		return;
	}
	
	@Override
	public void run()
	{
		double now = System.nanoTime();
		int updateCount = 0;
		int frameCount = 0;
		int thisSecond = 0;
		int lastSecondTime = (int)(this.mLastUpdate / BILLION);
		 
		while(!this.mStopRequested)
		{
			now = System.nanoTime();
			updateCount = 0;
			while(now - this.mLastUpdate > this.mUpdateDelta && updateCount < this.mMaxUpdatesBeforeRender)
			{
				this.mObserver.tickerUpdateRequested();
				this.mLastUpdate += this.mUpdateDelta;
				updateCount++;
				
				// Keep unprocessed updates from building up.
				if(now - this.mLastUpdate > this.mUpdateDelta){
					this.mLastUpdate = now - this.mUpdateDelta;
				}
				
				this.mObserver.tickerRenderRequested();
				this.mLastRender = now;
				
				if(this.mStatisticsEnabled){
					frameCount++;
					thisSecond = (int)(this.mLastUpdate / BILLION);
					if(thisSecond > lastSecondTime)
					{
						this.mFPS = frameCount;
						frameCount = 0;
						lastSecondTime = thisSecond;
					}
				}
				
				while(now - this.mLastRender < this.mRenderDelta && now - this.mLastUpdate < this.mUpdateDelta)
				{
					Thread.yield();
					
					if(this.mAllowSleep){
						//This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it.
						//You can remove this line and it will still work (better), your CPU just climbs on certain OSes.
						//FYI on some OS's this can cause pretty bad stuttering.
						try{
							Thread.sleep(1);
						}catch(Exception e){
							System.out.println(e.getMessage());
						}
					}
					
					now = System.nanoTime();
				}
			}
		}
		this.mIsRunning = false;
		return;
	}
	
	public void setUpdateHertz(double updateHertz)
	{
		if(this.mIsRunning){
			return;
		}
		this.mUpdateHertz = updateHertz;
		this.mUpdateDelta = BILLION / this.mUpdateHertz;
		return;
	}
	
	public void setRenderHertz(double renderHertz)
	{
		if(this.mIsRunning){
			return;
		}
		this.mRenderHertz = renderHertz;
		this.mRenderDelta = BILLION / this.mRenderHertz;
		return;
	}
	
	public void setMaxUpdateBeforeRender(int max)
	{
		if(this.mIsRunning){
			return;
		}
		this.mMaxUpdatesBeforeRender = max;
		return;
	}
	
	public void enableStatisticsMode(boolean enabled)
	{
		this.mStatisticsEnabled = enabled;
		return;
	}
	
	public boolean inStatiscticsMode()
	{
		return this.mStatisticsEnabled;
	}
	
	public boolean isRunning()
	{
		return this.mIsRunning;
	}
	
	public int getFPS()
	{
		return this.mFPS;
	}
	
	public interface Observer
	{
		abstract void tickerUpdateRequested();
		abstract void tickerRenderRequested();
	}
}

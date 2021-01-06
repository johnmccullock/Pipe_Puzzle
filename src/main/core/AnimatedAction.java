package main.core;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import main.core.util.State;

public class AnimatedAction
{
	private AnimatedAction.Mediator mMediator = null;
	private Object mImageKey = null;
	private double mX = 0.0;
	private double mY = 0.0;
	private MovementBehavior mBehavior = null;
	private State mPrimaryState = new PrimaryState();
	private State mCompletionState = new CompletionState();
	private State mDisposalState = new DisposalState();
	private State mCurrent = this.mPrimaryState;
	
	public AnimatedAction(AnimatedAction.Mediator mediator, Object imageKey)
	{
		this.mMediator = mediator;
		this.mImageKey = imageKey;
		return;
	}
	
	public void update()
	{
		this.mCurrent.update();
		return;
	}
	
	public void render(Graphics2D g2d)
	{
		AffineTransform original = g2d.getTransform();
		g2d.rotate(-mBehavior.getRotation(), mX + (this.mMediator.getImage(this.mImageKey).getWidth() / 2.0), mY + (this.mMediator.getImage(this.mImageKey).getHeight() / 2.0));
		g2d.drawImage(this.mMediator.getImage(this.mImageKey), (int)Math.round(mX), (int)Math.round(mY), null);
		g2d.setTransform(original);
		return;
	}
	
	public void setX(double x)
	{
		this.mX = x;
		return;
	}
	
	public double getX()
	{
		return this.mX;
	}
	
	public void setY(double y)
	{
		this.mY = y;
		return;
	}
	
	public double getY()
	{
		return this.mY;
	}
	
	public boolean readyForDisposal()
	{
		return this.mCurrent == this.mDisposalState;
	}
	
	public void setBehavior(MovementBehavior behavior)
	{
		this.mBehavior = behavior;
		return;
	}
	
	private class PrimaryState implements State
	{
		@Override
		public void reset()
		{
			return;
		}
		
		@Override
		public void update()
		{
			if(mBehavior.destArrived()){
				mCurrent = mCompletionState;
				mCurrent.reset();
				return;
			}
			
			mBehavior.update();
			return;
		}
	}
	
	private class CompletionState implements State
	{
		@Override
		public void reset()
		{
			mMediator.movePart();
			return;
		}
		
		@Override
		public void update()
		{
			mCurrent = mDisposalState;
			mCurrent.reset();
			return;
		}
	}
	
	private class DisposalState implements State
	{
		@Override
		public void reset()
		{
			return;
		}
		
		@Override
		public void update()
		{
			return;
		}
	}
	
	public interface Mediator
	{
		abstract void movePart();
		abstract BufferedImage getImage(Object key);
	}
}

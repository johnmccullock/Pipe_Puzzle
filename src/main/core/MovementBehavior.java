package main.core;

import main.core.util.MathUtil;
import main.core.util.State;

public class MovementBehavior
{
	private MovementBehavior.Mediator mMediator = null;
	private double mDirection = 0.0;
	private double mSpeed = 0.0;
	private double mMinSpeed = 0.0;
	private double mMaxSpeed = 0.0;
	private double mDestX = 0.0;
	private double mDestY = 0.0;
	private double mRotation = 0.0;
	
	private State mIdle = new Idle();
	private State Moving = new Moving();
	private State mCurrent = this.mIdle;
	
	public MovementBehavior(MovementBehavior.Mediator mediator, double minSpeed, double maxSpeed, double destX, double destY, double rotation)
	{
		this.mMediator = mediator;
		this.mMinSpeed = minSpeed;
		this.mMaxSpeed = maxSpeed;
		this.mDestX = destX;
		this.mDestY = destY;
		this.mRotation = rotation;
		this.mDirection = MathUtil.getAngleFromPoints(mMediator.getX(), mMediator.getY(), this.mDestX, this.mDestY);
		this.mCurrent = this.Moving;
		this.mCurrent.reset();
		return;
	}
	
	public void update()
	{
		this.mCurrent.update();
		return;
	}
	
	public boolean destArrived()
	{
		return (this.mMediator.getX() == this.mDestX) && (this.mMediator.getY() == this.mDestY);
	}
	
	public double getRotation()
	{
		return this.mRotation;
	}
	
	private class Idle implements State
	{
		@Override
		public void reset()
		{
			mSpeed = 0.0;
			return;
		}
		
		@Override
		public void update()
		{
			return;
		}
	}
	
	private class Moving implements State
	{
		double mStartX = 0.0;
		double mStartY = 0.0;
		double mHalfDist = 0.0;
		
		@Override
		public void reset()
		{
			this.mStartX = mMediator.getX();
			this.mStartY = mMediator.getY();
			this.mHalfDist = MathUtil.distance(this.mStartX, this.mStartY, mDestX, mDestY) / 2.0;
			return;
		}
		
		@Override
		public void update()
		{
			double dist = MathUtil.distance(this.mStartX, this.mStartY, mMediator.getX(), mMediator.getY());
			if(dist < this.mHalfDist){
				mSpeed = MathUtil.easeInQuad(dist, mMinSpeed, mMaxSpeed, this.mHalfDist);
			}else{
				mSpeed = MathUtil.easeOutQuad(dist, mMinSpeed, mMaxSpeed, this.mHalfDist);
			}
			if(MathUtil.distance(mMediator.getX(), mMediator.getY(), mDestX, mDestY) <= mSpeed){
				mMediator.setX(mDestX);
				mMediator.setY(mDestY);
				mCurrent = mIdle;
				mCurrent.reset();
				return;
			}else{
				double dirX = Math.cos(mDirection);
				double dirY = -Math.sin(mDirection);
				mMediator.setX(mMediator.getX() + (dirX * mSpeed));
				mMediator.setY(mMediator.getY() + (dirY * mSpeed));
			}
			return;
		}
	}
	
	public interface Mediator
	{
		abstract void setX(double x);
		abstract double getX();
		abstract void setY(double y);
		abstract double getY();
	}
}

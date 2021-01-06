package main.core.util;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Vector;

/**
 * General graph and trigonometry functions.
 *
 * Version 2.5 adds Tweening functions: easeInQuad(), easeOutCubic(), easeOutQuad(), easeInOutQuad().
 * Version 2.4 adds getPointDistanceToLine() and getPerpendicularPoint().
 * Version 2.3 adds intercept() function to predict the collision point for two moving objects given their position, direction and speed.  Good for finding firing solutions on moving targets.
 * Version 2.2 adds getRandomLong() and getRandomInt().
 * Version 2.1 adds toDegrees() and roundTo() functions.  DecimalBounds enum also added.
 * Version 2.0 returns the class's floating point data type to double instead of float.  Clamp functions added.
 * Version 1.9 adds centeroid().
 * Version 1.8 adds getLineIntersectI() and getLineIntersectF().
 * Version 1.6 adds angleDifference() and absAngleDifference() functions for better angle comparisons.
 * Version 1.5 replaces buggy directionToFacePoint() with getShortestTurn() which is easier to understand.
 * Version 1.4 reintroduced the "norm" function for normalizing radian values; necessary for radian value comparison
 * Version 1.3 adds getAngleA function.
 * Version 1.2 fixes buggy code by removing "normalizing" and "abs" functions that acted on radian values.
 * Version 1.1 includes function to find intersecting points of two circles.
 *
 * @author John McCullock
 * @version 2.4 2019-03-24
 */
public class MathUtil
{
	public enum DecimalBounds
	{
		TENTHS(10.0),
		HUNDREDTHS(100.0),
		THOUSANDTHS(1000.0);
		
		private double mValue = 0.0;
		
		private DecimalBounds(double value)
		{
			this.mValue = value;
			return;
		}
		
		public double getValue()
		{
			return this.mValue;
		}
	}
	
	public static final double QUARTER_PI = Math.PI * 0.25;
	public static final double HALF_PI = Math.PI * 0.5;
	public static final double PI2 = Math.PI * 2.0;
	
	/**
	 * Finds the distance between two points using Pythagorean Theorem.
	 * @param x1 double x value for first point.
	 * @param y1 double y value for first point.
	 * @param x2 double x value for second point.
	 * @param y2 double y value for second point.
	 * @return double distance between first and second point.
	 */
	public static double distance(final double x1, final double y1, final double x2, final double y2)
	{
		return Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
	}
	
	/**
	 * Finds the difference between angles on the side less that Pi.
	 * @param theta1 double first angle.
	 * @param theta2 double second angle.
	 * @return double
	 */
	public static double angleDifference(double theta1, double theta2)
	{
		return ((theta2 - theta1) + Math.PI) % PI2 - Math.PI;
	}
	
	/**
	 * Similar to angleDifference function, but return the absolute value.
	 * @param theta1 double first angle.
	 * @param theta2 double second angle.
	 * @return double
	 */
	public static double absAngleDifference(double theta1, double theta2)
	{
		return Math.abs(((theta2 - theta1) + Math.PI) % PI2 - Math.PI);
	}
	
	/**
	 * This function side-steps the problem where the angles span across the 0-2Pi divide.
	 * @param targetAngle
	 * @param actualAngle
	 * @param rotation
	 * @param rotationIncrement
	 * @return
	 */
	public static double absAngleDiffWithAntiJitter(double targetAngle, double actualAngle, double rotationIncrement)
	{
		if((targetAngle > 0.0 && targetAngle < rotationIncrement) && (actualAngle < PI2 && actualAngle > (PI2 - rotationIncrement))){
			return norm(absAngleDifference(actualAngle, targetAngle + PI2));
		}else{
			return norm(absAngleDifference(actualAngle, targetAngle));
		}
	}
	
	/**
	 *
	 * @param x1 double x value for start point.
	 * @param y1 double y value for start point.
	 * @param x2 double x value for destination point.
	 * @param y2 double y value for destination point.
	 * @return double angle in radians.
	 */
	public static double getAngleFromPoints(final double x1, final double y1, final double x2, final double y2)
	{
		return Math.atan2(-(y2 - y1), x2 - x1);
	}
	
	/**
	 * Determines if a clockwise or counterclockwise turn is shortest to a target angle.
	 * @param current double current angle in radians.
	 * @param target double target angle in radians.
	 * @return double Positive results means turn counterclockwise, negative means turn clockwise.
	 */
	public static double getShortestTurn(double current, double target)
	{
		double difference = target - current;
		while (difference < -Math.PI) difference += PI2;
		while (difference > Math.PI) difference -= PI2;
		return difference;
	}
	
	public static double distanceToStartTurn(double radius, double radiusFactor, double turnAngle, double turnExponent)
	{
		//double diameter = radius * 2f;
		//double timesRadiusFactor = diameter * radiusFactor;
		//double exponent = (double)Math.pow(turnExponent, turnAngle);
		//double total = timesRadiusFactor * exponent;
		return ((radius * 2f) * radiusFactor) * (double)Math.pow(turnExponent, turnAngle);
	}
	
	/**
	 * Finds the average (or mean) angle, in radians, among an array of angles.  
	 * @param angles angle in radians
	 * @return radians
	 */
	public static double getMeanAngle(double[] angles)
	{
		double x = 0.0;
		double y = 0.0;
		for(int i = 0; i < angles.length; i++)
		{
			x += Math.cos(angles[i]);
			y += Math.sin(angles[i]);
		}
		return Math.atan2(y / (double)angles.length, x / (double)angles.length);
	}
	
	/**
	 * Finds the average (or mean) angle, in radians, among an array of angles.  
	 * @param angles angle in radians
	 * @return radians
	 */
	public static double getMeanAngle(Vector<Double> angles)
	{
		double x = 0.0;
		double y = 0.0;
		for(int i = 0; i < angles.size(); i++)
		{
			x += Math.cos(angles.get(i));
			y += Math.sin(angles.get(i));
		}
		return Math.atan2(y / (double)angles.size(), x / (double)angles.size());
	}
	
	/**
	 * Useful for radian comparisons.
	 *
	 * Takes any radian value and shifts it to a value between 0.0 and 2PI.
	 *
	 * @param angle double angle to be evaluated.
	 * @return double value between 0.0 and 2PI.
	 */
	public static double norm(double angle)
	{
		angle = angle % (Math.PI * 2.0f);
		return angle = angle < 0 ? angle + (Math.PI * 2.0f) : angle;
	}
	
	/**
	 * Finds the angleA opposite to sideA of a triangle where the lengths of all three sides are known.
	 * @param opposite double length of sideA, which is opposite the angle being searched for (angleA).
	 * @param sideB double length of sideB.
	 * @param sideC double length of sideC.
	 * @return double angle in radians of angleA, or zero if any parameters were zero.
	 */
	public static double getAngleA(double opposite, double sideB, double sideC)
	{
		if(opposite == 0.0f || sideB == 0.0f || sideC == 0.0f){
			return 0.0f;
		}
		return Math.acos((opposite * opposite - sideB * sideB - sideC * sideC) / (-2.0f * sideB * sideC));
	}
	
	/**
	 * Generates a random int between high and low values, inclusive.
	 * @param low int radian value.
	 * @param high int radian value.
	 * @return int.
	 */
	public static int getRandomInt(int low, int high)
	{
		return (int)Math.round((Math.random() * (high - low)) + low);
	}
	
	/**
	 * Generates a random float between high and low values.
	 * @param low float radian value.
	 * @param high float radian value.
	 * @return float.
	 */
	public static float getRandomFloat(float low, float high)
	{
		return ((float)Math.random() * (high - low)) + low;
	}
	
	public static int getExclusiveRandomInt(int high, int low, int[] excluding)
	{
		int result = 0;
		boolean done = false;
		while(!done)
		{
			boolean found = false;
			result = (int)Math.round(new Random().nextDouble() * (high - low)) + low;
			for(int i = 0; i < excluding.length; i++)
			{
				if(excluding[i] == result){
					found = true;
					break;
				}
			}
			if(!found){
				done = true;
			}
		}
		return result;
	}
	
	public static int getExclusiveRandomInt(int high, int low, Vector<Integer> excluding)
	{
		int result = 0;
		boolean done = false;
		while(!done)
		{
			boolean found = false;
			result = (int)Math.round(new Random().nextDouble() * (high - low)) + low;
			for(int i = 0; i < excluding.size(); i++)
			{
				if(excluding.get(i) == result){
					found = true;
					break;
				}
			}
			if(!found){
				done = true;
			}
		}
		return result;
	}
	
	/**
	 * Generates a random double between a high and low radian values.
	 * @param low double radian value.
	 * @param high double radian value.
	 * @return double.
	 */
	public static double getRandomDouble(double low, double high)
	{
		return (Math.random() * (high - low)) + low;
	}
	
	public static long getRandomLong(long low, long high)
	{
		return (long)Math.round((Math.random() * (high - low)) + low);
	}
	
	/**
	 * Finds the shortest distance between a line and a target point.
	 * Found at: https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
	 * An error may result if any of the points are the same, and no check is made on the input parameters.
	 * @param x double x-coordinate of the target point.
	 * @param y double y-coordinate of the target point.
	 * @param x1 double x-coordinate of the first line point.
	 * @param y1 double y-coordinate of the first line point.
	 * @param x2 double x-coordinate of the second line point.
	 * @param y2 double y-coordinate of the second line point.
	 * @return double.
	 */
	public static double getPointDistanceToLine(double x, double y, double x1, double y1, double x2, double y2)
	{
		double xDiff = x2 - x1;
		double yDiff = y2 - y1;
		
		double dot = (x - x1) * xDiff + (y - y1) * yDiff;
		double lenSQ = xDiff * xDiff + yDiff * yDiff;
		double param = lenSQ != 0.0 ? dot / lenSQ : -1.0;
		
		double xx = 0.0;
		double yy = 0.0;
		
		if(param < 0.0){
			xx = x1;
			yy = y1;
		}else if(param > 1.0){
			xx = x2;
			yy = y2;
		}else{
			xx = x1 + param * xDiff;
			yy = y1 + param * yDiff;
		}
		
		double dx = x - xx;
		double dy = y - yy;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * Finds a point on a line where a target point is perpendicular to the line.
	 * Found at: https://stackoverflow.com/questions/1811549/perpendicular-on-a-line-from-a-given-point
	 * @param x double x-coordinate of the target point.
	 * @param y double y-coordinate of the target point.
	 * @param x1 double x-coordinate of the first line point.
	 * @param y1 double y-coordinate of the first line point.
	 * @param x2 double x-coordinate of the second line point.
	 * @param y2 double y-coordinate of the second line point.
	 * @return Point2D.Double
	 */
	public static Point2D.Double getPerpendicularPoint(double x, double y, double x1, double y1, double x2, double y2)
	{
		double xDiff = x2 - x1;
		double yDiff = y2 - y1;
		double dist = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		xDiff /= dist;
		yDiff /= dist;
		double dot = (xDiff * (x - x1)) + (yDiff * (y - y1));
		return new Point2D.Double((xDiff * dot) + x1, (yDiff * dot) + y1);
	}
	
	/**
	 * Finds the point where two lines intersect.  If the lines don't intersect, the returned point may contain NaN or Infinity.
	 * Found at: http://www.dreamincode.net/forums/topic/217676-intersection-point-of-two-lines/
	 * @param x1 double x of first point on first line.
	 * @param y1 double y of first point on first line.
	 * @param x2 double x of second point on first line.
	 * @param y2 double y of second point on first line.
	 * @param x3 double x of first point on second line.
	 * @param y3 double y of first point on second line.
	 * @param x4 double x of second point on second line.
	 * @param y4 double y of second point on second line.
	 * @return Point2D.Double
	 */
	public static Point2D.Double getLineIntersectF(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		double x = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
		double y = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Finds the point where two lines intersect.  If the lines don't intersect, the returned point may contain NaN or Infinity.
	 * Found at: http://www.dreamincode.net/forums/topic/217676-intersection-point-of-two-lines/
	 * @param x1 double x of first point on first line.
	 * @param y1 double y of first point on first line.
	 * @param x2 double x of second point on first line.
	 * @param y2 double y of second point on first line.
	 * @param x3 double x of first point on second line.
	 * @param y3 double y of first point on second line.
	 * @param x4 double x of second point on second line.
	 * @param y4 double y of second point on second line.
	 * @return Point
	 */
	public static Point getLineIntersectI(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		int x = (int)Math.round(((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d);
		int y = (int)Math.round(((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d);
		return new Point(x, y);
	}
	
	/**
	 * Do line segments (x1, y1)--(x2, y2) and (x3, y3)--(x4, y4) intersect?
	 * Found at: http://ptspts.blogspot.com/2010/06/how-to-determine-if-two-line-segments.html
	 * This design avoids as many multiplications and divisions as possible.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param x4
	 * @param y4
	 * @return boolean true if lines intersect, false otherwise.
	 */
	public static boolean LineSegmentsIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		int d1 = ComputeDirection(x3, y3, x4, y4, x1, y1);
		int d2 = ComputeDirection(x3, y3, x4, y4, x2, y2);
		int d3 = ComputeDirection(x1, y1, x2, y2, x3, y3);
		int d4 = ComputeDirection(x1, y1, x2, y2, x4, y4);
		return (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
				((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) ||
				(d1 == 0 && IsOnSegment(x3, y3, x4, y4, x1, y1)) ||
				(d2 == 0 && IsOnSegment(x3, y3, x4, y4, x2, y2)) ||
				(d3 == 0 && IsOnSegment(x1, y1, x2, y2, x3, y3)) ||
				(d4 == 0 && IsOnSegment(x1, y1, x2, y2, x4, y4));
	}
	
	private static int ComputeDirection(double xi, double yi, double xj, double yj, double xk, double yk)
	{
		double a = (xk - xi) * (yj - yi);
		double b = (xj - xi) * (yk - yi);
		return a < b ? -1 : a > b ? 1 : 0;
	}
	
	private static boolean IsOnSegment(double xi, double yi, double xj, double yj, double xk, double yk)
	{
		return (xi <= xk || xj <= xk) && (xk <= xi || xk <= xj) && (yi <= yk || yj <= yk) && (yk <= yi || yk <= yj);
	}
	
	/**
	 * Finds possible intersecting points of two circles.  The length of the resulting array describes how many intersecting points were found.
	 *
	 * Will return empty array if:
	 *  - the circles do not intersect.
	 *  - the circles coincide.
	 *  - one circle contains the other.
	 *
	 * Will return only one intersection point if the circle edges merely touch.
	 *
	 * Will return two intersection points if either circle overlaps the other.
	 *
	 * @param radius1 double radius of the first circle.
	 * @param center1x double x-coordinate of first circle's center.
	 * @param center1y double y-coordinate of first circle's center.
	 * @param radius2 double radius of the second circle.
	 * @param center2x double x-coordinate of second circle's center.
	 * @param center2y double y-coordinate of second circle's center.
	 * @return Point2D.Double[] array containing any possible intersecting points.
	 */
	public static Point2D.Double[] circleIntersects(double radius1, final double center1x, final double center1y, double radius2, final double center2x, final double center2y)
	{
		Point2D.Double[] results = null;
		
		double d = MathUtil.distance(center1x, center1y, center2x, center2y);
		
		// Determine possible solutions:
		if(d > radius1 + radius2){
			// Circles do not intersect.
			return new Point2D.Double[0];
		}else if(d == 0.0f && radius1 == radius2){
			// Circles coincide.
			return new Point2D.Double[0];
		}else if(d + Math.min(radius1, radius2) < Math.max(radius1, radius2)){
			// One circle contains the other.
			return new Point2D.Double[0];
		}else{
			double a = ((radius1 * radius1) - (radius2 * radius2) + (d * d)) / (2.0f * d);
			double h = Math.sqrt((radius1 * radius1) - (a * a));
			
			// Find p2
			Point2D.Double p2 = new Point2D.Double(center1x + (a * (center2x - center1x)) / d,
					center1y + (a * (center2y - center1y)) / d);
			
			results = new Point2D.Double[2];
			
			results[0] = new Point2D.Double(p2.x + (h * (center2y - center1y) / d),
					p2.y - (h * (center2x - center1x) / d));
			
			results[1] = new Point2D.Double(p2.x - (h * (center2y - center1y) / d),
					p2.y + (h * (center2x - center1x) / d));
			
		}
		return results;
	}
	
	/**
	 * Compares the distance between the first point and two others, returning the closest.
	 * @param x1 double x-value of the first point.
	 * @param y1 double y-value of the first point.
	 * @param x2 double x-value of a second point to compare to the first.
	 * @param y2 double y-value of a second point to compare to the first.
	 * @param x3 double x-value of a third point to compare to the first.
	 * @param y3 double y-value of a third point to compare to the first.
	 * @return java.awt.Point of the closest: either the second or third point.
	 */
	public static Point2D.Double minPoint(double x1, double y1, double x2, double y2, double x3, double y3)
	{
		if(MathUtil.distance(x1, y1, x2, y2) < MathUtil.distance(x1, y1, x3, y3)){
			return new Point2D.Double(x2, y2);
		}else{
			return new Point2D.Double(x3, y3);
		}
	}
	
	public static Point2D.Double rotatePoint(double x, double y, double centerX, double centerY, double radians)
	{
		double dx = x - centerX;
		double dy = y - centerY;
		double newX = centerX + (dx * Math.cos(radians) - dy * Math.sin(radians));
		double newY = centerY + (dx * Math.sin(radians) + dy * Math.cos(radians));
		return new Point2D.Double(newX, newY);
	}
	
	public static Point centeroid(Vector<Point> points)
	{
		int xTotal = 0;
		int yTotal = 0;
		for(Point p : points)
		{
			xTotal += p.x;
			yTotal += p.y;
		}
		return new Point((int)Math.round(xTotal / (double)points.size()), (int)Math.round(yTotal / (double)points.size()));
	}
	
	/**
	 * Predicts the collision point for two moving objects given their position, direction and speed.  Good for finding firing 
	 * solutions on moving targets.  Can return null if no solution is found.
	 * Found at: https://stackoverflow.com/questions/2248876/2d-game-fire-at-a-moving-target-by-predicting-intersection-of-projectile-and-u
	 * 
	 * Example:
	 * double vX = Math.cos(target.direction) * target.speed;
	 * double vY = -Math.sin(target.direction) * target.speed;
	 * Point2D.Double() solution = intercept(missile.x, missile.y, target.x, target.y, vX, vY, missile.speed);
	 * 
	 * @param sourceX X-coordinate of source (shooter, launcher, cannon, etc.).
	 * @param sourceY Y-coordinate of source (shooter, launcher, cannon, etc.).
	 * @param targetX X-coordinate of target.
	 * @param targetY Y-coordinate of target.
	 * @param targetVelocityX Cosine value of target direction, multiplied by its speed.
	 * @param targetVelocityY Sine value of target direction, multiplied by its speed.
	 * @param sourceVelocity Speed value of source (bullet, missile, torpedo, etc.).
	 * @return intercept coordinates.
	 */
	public static Point2D.Double intercept(double sourceX, double sourceY, double targetX, double targetY, double targetVelocityX, double targetVelocityY, double sourceVelocity)
	{
		double tx = targetX - sourceX;
		double ty = targetY - sourceY;
		double tvx = targetVelocityX;
		double tvy = targetVelocityY;
		
		// Get quadratic equation components.
		double a = (tvx * tvx) + (tvy * tvy) - (sourceVelocity * sourceVelocity);
		double b = 2 * (tvx * tx + tvy * ty);
		double c = (tx * tx) + (ty * ty);
		
		// Solve quadratic.
		Point2D.Double ts = quadratic(a, b, c);
		
		// Find smallest positive solution.
		Point2D.Double solution = null;
		if(ts != null){
			double t0 = ts.x;
			double t1 = ts.y;
			double t = Math.min(t0, t1);
			if(t < 0) t = Math.max(t0, t1);
			if(t > 0){
				solution = new Point2D.Double(targetX + (targetVelocityX * t), targetY + (targetVelocityY * t));
			}
		}
		return solution;
	}
	
	/**
	 * Function to solve a quadratic equation (i.e.; (-b +/- sqrt(sqr(b) - 4ac)) / 2a).
	 * Found at: https://stackoverflow.com/questions/2248876/2d-game-fire-at-a-moving-target-by-predicting-intersection-of-projectile-and-u
	 * @param a double
	 * @param b double
	 * @param c double
	 * @return Point2D.Double solution.
	 */
	private static Point2D.Double quadratic(double a, double b, double c)
	{
		Point2D.Double solution = null;
		if(Math.abs(a) < 1e-6){
			if(Math.abs(b) < 1e-6){
				solution = Math.abs(c) < 1e-6 ? solution = new Point2D.Double(0.0, 0.0) : null;
			}else{
				solution = new Point2D.Double(-c / b, -c / b);
			}
		}else{
			double discriminant = (b * b) - (4 * a * c);
			if(discriminant >= 0.0){
				discriminant = Math.sqrt(discriminant);
				a = 2 * a;
				solution = new Point2D.Double((-b - discriminant) / a, (-b + discriminant) / a);
			}
		}
		return solution;
	}
	
	public static int clamp(int lowerBound, int upperBound, int value)
	{
		return (value < lowerBound) ? lowerBound : (value > upperBound) ? upperBound : value;
	}
	
	public static long clamp(long lowerBound, long upperBound, long value)
	{
		return (value < lowerBound) ? lowerBound : (value > upperBound) ? upperBound : value;
	}
	
	public static float clamp(float lowerBound, float upperBound, float value)
	{
		return (value < lowerBound) ? lowerBound : (value > upperBound) ? upperBound : value;
	}
	
	public static double clamp(double lowerBound, double upperBound, double value)
	{
		return (value < lowerBound) ? lowerBound : (value > upperBound) ? upperBound : value;
	}
	
	public static double toDegrees(double radians)
	{
		return 360.0 * (radians / PI2);
	}
	
	public static double toRadians(double degrees)
	{
		return PI2 * (degrees / 360.0);
	}
	
	public static double roundTo(double value, DecimalBounds bounds)
	{
		return Math.round(value * bounds.getValue()) / bounds.getValue();
	}
	
	/**
	 * Ease in function
	 * http://blog.moagrius.com/actionscript/jsas-understanding-easing/
	 * https://easings.net/en
	 * @param t Amount of time passed so far.
	 * @param b Beginning value of the property being tweened (eg; speed, angle etc.).
	 * @param c Change in position; Total distance minus beginning position.
	 * @param d Total duration of easing function.
	 * @return The speed at this current step.
	 */
	public static double easeInQuad(double t, double b, double c, double d)
	{
		return c * (t /= d) * t + b;
	};
	
	public static double easeOutCubic(double t, double b, double c, double d)
	{
		t /= d;
		t--;
		return c * (t * t * t + 1) + b;
	};
	
	public static double easeOutQuad(double t, double b, double c, double d)
	{
		return -c * (t /= d) * (t - 2) + b;
	}
	
	public static double easeInOutQuad(double t, double b, double c, double d)
	{
		if((t /= d / 2) < 1){
			return c / 2 * t * t + b;
		}
		return -c / 2 * ((--t) * (t - 2) - 1) + b;
	};
}

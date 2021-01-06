package main.core.util;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * General image manipulation utilities.
 * 
 * The purpose of the resize-by-screen-size functions is to size an image so it appears to use the same 
 * amount of screen space depending on the screen size.  This is useful for applications dealing with many 
 * screen sizes.  The screenFactor value is the target size divided by the screen size, using only the screen 
 * width or height as a reference.
 * 
 * Example: An image width of 100 pixels, on a screen resolution 1920 pixels wide, needs to be factored for
 * other screen widths (100 / 1920 = ~0.0521).  From here, the factor of 0.0521 would be used for other 
 * screen widths like 1680 (i.e.: 1680 * 0.0521 = ~87.528).  So, an image 100 pixels wide on a 1920-wide
 * screen, would be adjusted to about 87 pixels wide for a 1680-wide screen.  0.0521 being our screenFactor.
 * 
 * Version 1.1 adds functions for resizing by screen height.
 * 
 * Version 1.2 adds the rotateImageArray() function.
 * 
 * Version 1.3 adds a version of the rotateImageArray() function that returns a Vector<BufferedImage>. 
 * 
 * Version 1.4 adds the obvious methods to resize to a specific width and height that I'd been forgetting.  Also the obvious
 * rotate single image function.
 * 
 * @author John McCullock
 * @version 1.4 2018-10-31
 */
public class ImageUtils
{
	public static BufferedImage resizeImage(BufferedImage source, int width, int height)
	{
		AffineTransform at = new AffineTransform();
		at.scale(width / (double)source.getWidth(), height / (double)source.getHeight());
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(source, null);
	}
	
	public static BufferedImage resizeImage(BufferedImage source, double width, double height)
	{
		AffineTransform at = new AffineTransform();
		at.scale(width / (double)source.getWidth(), height / (double)source.getHeight());
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(source, null);
	}
	
	public static BufferedImage[] resizeImage(Vector<BufferedImage> source, double sizeFactor)
	{
		/* The factor value is a proportion of the reference width by the factor value, 
		   divided the source images's width. */
		AffineTransform at = new AffineTransform();
		at.scale(sizeFactor, sizeFactor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		
		BufferedImage[] results = new BufferedImage[source.size()];
		for(int i = 0; i < source.size(); i++)
		{
			results[i] = op.filter(source.get(i), null);
		}
		return results;
	}
	
	/**
	 * Resizes an image in proportion to the screen width.
	 * This function uses a bilinear transform operation for resizing.
	 * Height/width ratio is preserved during resizing.
	 * @param source java.awt.image.BufferedImage source which the resulting image will be based on.
	 * @param screenWidth int screen width.
	 * @param screenFactor double scale factor to apply to the screen width.
	 * @return java.awt.image.BufferedImage
	 */
	public static BufferedImage resizeImageByScreenWidth(BufferedImage source, int screenWidth, double screenFactor)
	{
		/* The factor value is a proportion of the screen width by the screenFactor value, 
		   divided the source images's width. */
		final double factor = (screenWidth * screenFactor) / source.getWidth();
		AffineTransform at = new AffineTransform();
		at.scale(factor, factor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(source, null);
	}
	
	/**
	 * Resizes an image in proportion to the screen height.
	 * This function uses a bilinear transform operation for resizing.
	 * Height/width ratio is preserved during resizing.
	 * @param source java.awt.image.BufferedImage source which the resulting image will be based on.
	 * @param screenWidth int screen height.
	 * @param screenFactor double scale factor to apply to the screen height.
	 * @return java.awt.image.BufferedImage
	 */
	public static BufferedImage resizeImageByScreenHeight(BufferedImage source, int screenHeight, double screenFactor)
	{
		/* The factor value is a proportion of the screen height by the screenFactor value, 
		   divided the source images's height. */
		final double factor = (screenHeight * screenFactor) / source.getHeight();
		AffineTransform at = new AffineTransform();
		at.scale(factor, factor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(source, null);
	}
	
	/**
	 * It's assumed that all source images are the same dimension.
	 * This function uses a bilinear transform operation for resizing.
	 * Height/width ratio is preserved during resizing.
	 * @param source java.util.Vector<java.awt.image.BufferedImage> original images to be modified.
	 * @param screenWidth int width of the screen.
	 * @param screenFactor double scaling factor.
	 * @return java.awt.image.BufferedImage[] array containing resized BufferedImage objects.
	 */
	public static BufferedImage[] resizeImageByScreenWidth(Vector<BufferedImage> source, BufferedImage[] results, int screenWidth, double screenFactor)
	{
		/* The factor value is a proportion of the screen width by the screenFactor value, 
		   divided the source images's width. */
		final double factor = (screenWidth * screenFactor) / (double)source.get(0).getWidth();
		AffineTransform at = new AffineTransform();
		at.scale(factor, factor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		
		for(int i = 0; i < source.size(); i++)
		{
			results[i] = op.filter(source.get(i), null);
		}
		return results;
	}
	
	public static Vector<BufferedImage> resizeImageByScreenWidth(Vector<BufferedImage> source, Vector<BufferedImage> results, int screenWidth, double screenFactor)
	{
		/* The factor value is a proportion of the screen width by the screenFactor value, 
		   divided the source images's width. */
		final double factor = (screenWidth * screenFactor) / (double)source.get(0).getWidth();
		AffineTransform at = new AffineTransform();
		at.scale(factor, factor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		
		for(int i = 0; i < source.size(); i++)
		{
			results.add(op.filter(source.get(i), null));
		}
		return results;
	}
	
	/**
	 * It's assumed that all source images are the same dimension.
	 * This function uses a bilinear transform operation for resizing.
	 * Height/width ratio is preserved during resizing.
	 * @param source java.util.Vector<java.awt.image.BufferedImage> original images to be modified.
	 * @param screenHeight int height of the screen.
	 * @param screenFactor double scaling factor.
	 * @return java.awt.image.BufferedImage[] array containing resized BufferedImage objects.
	 */
	public static BufferedImage[] resizeImageByScreenHeight(Vector<BufferedImage> source, int screenHeight, double screenFactor)
	{
		/* The factor value is a proportion of the screen height by the screenFactor value, 
		   divided the source images's height. */
		final double factor = (screenHeight * screenFactor) / (double)source.get(0).getHeight();
		AffineTransform at = new AffineTransform();
		at.scale(factor, factor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		
		BufferedImage[] results = new BufferedImage[source.size()];
		for(int i = 0; i < source.size(); i++)
		{
			results[i] = op.filter(source.get(i), null);
		}
		return results;
	}
	
	public static BufferedImage rotateImage(BufferedImage source, double radians, double anchorX, double anchorY)
	{
		AffineTransform at = new AffineTransform();
		at.rotate(radians, anchorX, anchorY);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(source, null);
	}
	
	public static BufferedImage[] rotateImageArray(BufferedImage[] source, double radians, double anchorX, double anchorY)
	{
		AffineTransform at = new AffineTransform();
		at.rotate(radians, anchorX, anchorY);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage[] results = new BufferedImage[source.length];
		
		for(int i = 0; i < source.length; i++)
		{
			results[i] = op.filter(source[i], null);
		}
		return results;
	}
	
	public static BufferedImage[] RandomRotateAndResizeImageArray(BufferedImage[] source, double minRadians, double maxRadians, double anchorXFactor, double anchorYFactor, double minSizeFactor, double maxSizeFactor)
	{
		double rotation = MathUtil.getRandomDouble(minRadians, maxRadians);
		double sizeFactor = MathUtil.getRandomDouble(minSizeFactor, maxSizeFactor);
		double anchorX = (source[0].getWidth() * sizeFactor) * anchorXFactor;
		double anchorY = (source[0].getHeight() * sizeFactor) * anchorYFactor;
		AffineTransform at = new AffineTransform();
		at.rotate(rotation, anchorX, anchorY);
		at.scale(sizeFactor, sizeFactor);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage[] results = new BufferedImage[source.length];
		for(int i = 0; i < source.length; i++)
		{
			results[i] = op.filter(source[i], null);
		}
		return results;
	}
	
	public static Vector<BufferedImage> rotateImageArray(BufferedImage[] source, Vector<BufferedImage> dest, double radians, double anchorX, double anchorY)
	{
		AffineTransform at = new AffineTransform();
		at.rotate(radians, anchorX, anchorY);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		
		for(int i = 0; i < source.length; i++)
		{
			dest.add(op.filter(source[i], null));
		}
		return dest;
	}
}

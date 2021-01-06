package main.core.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Wrapper class for commonly used image file opening functions.
 *
 * Version 1.3 includes "getVectorFromSpriteSheet" function to load in images from a single sprite sheet.
 *
 * @author John McCullock
 * @version 1.3 2018-10-05
 */
public class ImageLoader
{
	public static BufferedImage getImageFromFilePath(String path) throws IOException, Exception
	{
		BufferedImage aBMP = null;
		try{
			File aFile = new File(path);
			aBMP = ImageIO.read(aFile);
		}catch(IOException ioe){
			throw ioe;
		}catch(Exception ex){
			throw ex;
		}
		return aBMP;
	}
	
	public static BufferedImage getImageFromResourcePath(URL imageURL) throws Exception
	{
		BufferedImage aBMP = null;
		try{
			aBMP = ImageIO.read(imageURL);
		}catch(IOException ioe){
			throw ioe;
		}catch(Exception ex){
			throw ex;
		}
		return aBMP;
	}
	
	public static ImageIcon getImageIconFromResourcePath(URL imageURL) throws Exception
	{
		ImageIcon icon = null;
		try{
			icon = new ImageIcon(ImageIO.read(imageURL));
		}catch(Exception ex){
			throw ex;
		}
		return icon;
	}
	
	public static ImageIcon getImageIconFromResourcePath(URL imageURL, String altText) throws Exception
	{
		ImageIcon icon = null;
		try{
			icon = new ImageIcon(ImageIO.read(imageURL), altText);
		}catch(Exception ex){
			throw ex;
		}
		return icon;
	}
	
	/**
	 * Assumes the source image is a full set of sprites, with no empty areas.  In other words, the source image 
	 * contains exactly enough images for all rows and columns.
	 * @param fileURL URL pointing to an image resource.
	 * @param rows int number of rows contained in the source image.
	 * @param columns int the number of columns in the source image.
	 * @param width int the width of each sprite.
	 * @param height int the height of each sprite.
	 * @return Vector<BufferedImage> containing all sprites found in the source image.
	 * @throws IOException
	 * @throws Exception
	 */
	public static Vector<BufferedImage> getVectorFromSpriteSheet(final URL fileURL, final int rows, final int columns, final int width, final int height) throws IOException, Exception
	{
		Vector<BufferedImage> sections = new Vector<BufferedImage>();
		try{
			BufferedImage source = ImageIO.read(fileURL);
			for(int j = 0; j < rows; j++)
			{
				for(int i = 0; i < columns; i++)
				{
					sections.add(source.getSubimage(i * width, j * height, width, height));
				}
			}
		}catch(IOException ioe){
			throw ioe;
		}catch(Exception ex){
			throw ex;
		}
		return sections;
	}
}

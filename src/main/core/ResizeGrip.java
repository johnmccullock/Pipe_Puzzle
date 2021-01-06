package main.core;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Custom grip component for making resizing other components easier.  A lot of dialog windows and other windows have
 * a small, triangular shaped grip in the lower left-hand corner.  Users often find it easier to drag and resize a
 * window by this kind of grip.
 *
 * Version 2.1 adds JInternalFrame to the GripParent options.  Now ResizeGrip can be used with JFrame, JDialog and JInternalFrame.
 *
 * Version 2.0 eliminates the ResizableClient interface, and simply uses a reference to JFrame or JDialog to perform actions.
 *
 * @author John McCullock
 * @version 2.1 2017-10-21
 */

@SuppressWarnings("serial")
public class ResizeGrip extends JPanel
{
	private GripParent mParent = null;
	private BufferedImage mImage = null;
	private Point mMousePos = null;
	
	public ResizeGrip(JFrame parent, BufferedImage image)
	{
		this.mParent = new FrameGripParent(parent);
		this.mImage = image;
		this.initializeLayout();
		return;
	}
	
	public ResizeGrip(JDialog parent, BufferedImage image)
	{
		this.mParent = new DialogGripParent(parent);
		this.mImage = image;
		this.initializeLayout();
		return;
	}
	
	public ResizeGrip(JInternalFrame parent, BufferedImage image)
	{
		this.mParent = new InternalFrameGripParent(parent);
		this.mImage = image;
		this.initializeLayout();
		return;
	}
	
	private void initializeLayout()
	{
		this.setLayout(new BorderLayout());
		this.setMinimumSize(new Dimension(this.mImage.getWidth(), this.mImage.getHeight()));
		this.setMaximumSize(new Dimension(this.mImage.getWidth(), this.mImage.getHeight()));
		this.setPreferredSize(new Dimension(this.mImage.getWidth(), this.mImage.getHeight()));
		this.addMouseListener(this.createMouseListener());
		this.addMouseMotionListener(this.createMouseMotionListener());
		return;
	}
	
	public BufferedImage getImage()
	{
		return this.mImage;
	}
	
	private MouseListener createMouseListener()
	{
		return new MouseAdapter()
		{
			public void mouseEntered(MouseEvent e)
			{
				ResizeGrip.this.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
				return;
			}
			
			public void mouseExited(MouseEvent e)
			{
				ResizeGrip.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				return;
			}
			
			public void mousePressed(MouseEvent e)
			{
				ResizeGrip.this.mMousePos = e.getLocationOnScreen();
				return;
			}
			
			public void mouseReleased(MouseEvent e)
			{
				ResizeGrip.this.mMousePos = null;
				return;
			}
		};
	}
	
	private MouseMotionListener createMouseMotionListener()
	{
		return new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent e)
			{
				if(ResizeGrip.this.mMousePos == null){
					return;
				}
				int xDiff = e.getLocationOnScreen().x - ResizeGrip.this.mMousePos.x;
				int yDiff = e.getLocationOnScreen().y - ResizeGrip.this.mMousePos.y;
				int width = 0;
				int height  = 0;
				
				if(mParent.getWidth() + xDiff < mParent.getMinimumSize().width){
					width = mParent.getMinimumSize().width;
				}else if(mParent.getWidth() + xDiff > mParent.getMaximumSize().width){
					width = mParent.getMaximumSize().width;
				}else{
					width = mParent.getWidth() + xDiff;
					mMousePos.x = e.getLocationOnScreen().x;
				}
				
				if(mParent.getHeight() + yDiff < mParent.getMinimumSize().height){
					height = mParent.getMinimumSize().height;
				}else if(mParent.getHeight() +yDiff > mParent.getMaximumSize().height){
					height = mParent.getMaximumSize().height;
				}else{
					height = mParent.getHeight() + yDiff;
					mMousePos.y = e.getLocationOnScreen().y;
				}
				
				ResizeGrip.this.mParent.setSize(width, height);
				return;
			}
		};
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawImage(this.mImage, 0, 0, null);
		g2d.dispose();
		return;
	}
	
	/**
	 * GripParent is necessary because JDialog's owner field requires something extended from java.awt.Window.  And JInternalFrame
	 * is not related to the Window class.
	 * @author John McCullock
	 * @version 1.0 2017-10-21
	 */
	private abstract class GripParent
	{
		abstract int getHeight();
		abstract int getWidth();
		abstract void setSize(int width, int height);
		abstract Dimension getMinimumSize();
		abstract Dimension getMaximumSize();
	}
	
	private class FrameGripParent extends GripParent
	{
		private JFrame mParent = null;
		
		public FrameGripParent(JFrame parent)
		{
			this.mParent = parent;
			return;
		}
		
		@Override
		public void setSize(int width, int height)
		{
			mParent.setSize(width, height);
			return;
		}
		
		@Override
		public int getWidth()
		{
			return mParent.getWidth();
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			return mParent.getMinimumSize();
		}
		
		@Override
		public Dimension getMaximumSize()
		{
			return mParent.getMaximumSize();
		}
		
		@Override
		public int getHeight()
		{
			return mParent.getHeight();
		}
	}
	
	private class DialogGripParent extends GripParent
	{
		private JDialog mParent = null;
		
		public DialogGripParent(JDialog parent)
		{
			this.mParent = parent;
			return;
		}
		
		@Override
		public void setSize(int width, int height)
		{
			mParent.setSize(width, height);
			return;
		}
		
		@Override
		public int getWidth()
		{
			return mParent.getWidth();
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			return mParent.getMinimumSize();
		}
		
		@Override
		public Dimension getMaximumSize()
		{
			return mParent.getMaximumSize();
		}
		
		@Override
		public int getHeight()
		{
			return mParent.getHeight();
		}
	}
	
	private class InternalFrameGripParent extends GripParent
	{
		private JInternalFrame mParent = null;
		
		public InternalFrameGripParent(JInternalFrame parent)
		{
			this.mParent = parent;
			return;
		}
		
		@Override
		public void setSize(int width, int height)
		{
			mParent.setSize(width, height);
			return;
		}
		
		@Override
		public int getWidth()
		{
			return mParent.getWidth();
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			return mParent.getMinimumSize();
		}
		
		@Override
		public Dimension getMaximumSize()
		{
			return mParent.getMaximumSize();
		}
		
		@Override
		public int getHeight()
		{
			return mParent.getHeight();
		}
	}
}

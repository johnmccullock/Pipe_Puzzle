package main.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class MainGUI extends JFrame
{
	public enum StatusSection{DIFFICULTY, PUZZLE_NUM, TIMER};
	
	private MainMenu mMainMenu = null;
	private MainToolBar mToolBar = null;
	private JLabel mIDDisplay = null;
	private JLabel mBestTimeDisplay = null;
	private Display mDisplay = null;
	private StatusBar mStatusBar = null;
	private boolean mShowCursor = true;
	private Point mMousePosition = new Point();
	private boolean mMouseIsDragging = false;
	private int mXDiff = 0; // Makes the click and drag smoother.
	private int mYDiff = 0; // Makes the click and drag smoother.
	
	public MainGUI()
	{
		
	}
	
	public void repaintDisplay()
	{
		if(this.mDisplay == null){
			return;
		}
		this.mDisplay.repaint();
		return;
	}
	
	public void setMainMenu(MainMenu menu)
	{
		this.mMainMenu = menu;
		this.setJMenuBar(this.mMainMenu);
		return;
	}
	
	public void setMainToolBar(MainToolBar toolbar)
	{
		this.mToolBar = toolbar;
		return;
	}
	
	public void setDisplay(Display display)
	{
		this.mDisplay = display;
		return;
	}
	
	public void setIDLabel(JLabel idLabel)
	{
		this.mIDDisplay = idLabel;
		return;
	}
	
	public void setBestTimeLabel(JLabel bestLabel)
	{
		this.mBestTimeDisplay = bestLabel;
		return;
	}
	
	public void setStatusBar(StatusBar statusBar)
	{
		this.mStatusBar = statusBar;
		return;
	}
	
	public StatusBar getStatusBar()
	{
		return this.mStatusBar;
	}
	
	public void setDifficultyValue(Difficulty dif)
	{
		this.mMainMenu.setDifficultySelected(dif);
		this.mToolBar.setDifficultySelected(dif);
		return;
	}
	
	public void displayDifficultyData(String caption)
	{
		this.mIDDisplay.setText(caption);
		return;
	}
	
	public void setMousePosition(int x, int y)
	{
		this.mMousePosition.x = x;
		this.mMousePosition.y = y;
		return;
	}
	
	public Point getMousePosition()
	{
		return new Point(this.mMousePosition);
	}
	
	public void setMouseIsDragging(boolean state)
	{
		this.mMouseIsDragging = state;
		return;
	}
	
	public boolean getMouseIsDragging()
	{
		return this.mMouseIsDragging;
	}
	
	public void setXDiff(int diff)
	{
		this.mXDiff = diff;
		return;
	}
	
	public int getXDiff()
	{
		return this.mXDiff;
	}
	
	public void setYDiff(int diff)
	{
		this.mYDiff = diff;
		return;
	}
	
	public int getYDiff()
	{
		return this.mYDiff;
	}
	
	public void setShowCursor(boolean state)
	{
		this.mShowCursor = state;
		return;
	}
	
	public boolean getShowCursor()
	{
		return this.mShowCursor;
	}
	
	public Dimension getDisplaySize()
	{
		if(this.mDisplay == null){
			return new Dimension();
		}
		return new Dimension(this.mDisplay.getSize());
	}
	
	public interface Mediator
	{
		abstract void setDifficulty(Difficulty dif);
		abstract Difficulty getDifficulty();
	}
}

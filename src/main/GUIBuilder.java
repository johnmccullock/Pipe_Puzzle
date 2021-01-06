package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import main.core.ActionContext;
import main.core.Difficulty;
import main.core.Display;
import main.core.EasyDifficultyObserver;
import main.core.Engine;
import main.core.ExitGameObserver;
import main.core.FirstPuzzleObserver;
import main.core.HardDifficultyObserver;
import main.core.LastPuzzleObserver;
import main.core.MainGUI;
import main.core.MainMenu;
import main.core.MainToolBar;
import main.core.MediumDifficultyObserver;
import main.core.NewGameObserver;
import main.core.NextPuzzleObserver;
import main.core.PreviousPuzzleObserver;
import main.core.ShuffleObserver;
import main.core.SolveObserver;
import main.core.StatusBar;
import main.core.util.GUIHelper;
import main.core.util.ImageLoader;

public class GUIBuilder
{
	private GUIBuilder.Mediator mMediator = null;
	private Display.Mediator mDisplayMediator = null;
	
	public GUIBuilder(GUIBuilder.Mediator mediator, Display.Mediator displayMediator)
	{
		this.mMediator = mediator;
		this.mDisplayMediator = displayMediator;
		return;
	}
	
	public MainGUI createPrimaryStyle(MainGUI gui)
	{
		ResourceBundle appResource = this.mMediator.getAppResource();
		ResourceBundle guiResource = this.mMediator.getMainResource();
		gui.setTitle(appResource.getString("APPLICATION_TITLE"));
		gui.setMainMenu(this.createMenuBar001(guiResource));
		JPanel basePanel = new JPanel();
		basePanel.setLayout(new GridBagLayout());
		Font font = UIManager.getFont("Menu.font");
		JLabel idLabel = this.createPuzzleIDLabel(font);
		gui.setIDLabel(idLabel);
		JLabel bestLabel = this.createBestTimeLabel(guiResource, font);
		basePanel.add(this.createToolBar001(guiResource, gui, idLabel, bestLabel, font), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		Display display = new Display(this.mDisplayMediator);
		gui.setDisplay(display);
		basePanel.add(display, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 6, 0, 6), 0, 0));
		StatusBar status = this.createStatusBar(gui);
		gui.setStatusBar(status);
		basePanel.add(status, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		gui.addComponentListener(this.createComponentListener());
		gui.addWindowListener(this.createWindowListener());
		display.addMouseListener(this.createMouseListener(gui));
		display.addMouseMotionListener(this.createMouseMotionListener(gui));
		
		gui.setFocusable(true); // required for keyboard events.
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setContentPane(basePanel);
		gui.setMinimumSize(new Dimension(840, 680));
		gui.setPreferredSize(new Dimension(840, 680));
		gui.setResizable(true);
		gui.setVisible(true);
		gui.pack();
		return gui;
	}
	
	private MainMenu createMenuBar001(ResourceBundle resource)
	{
		MainMenu bar = new MainMenu();
		bar.add(this.createGameMenu001(resource));
		bar.add(this.createDifficultyMenu001(resource, bar));
		bar.add(this.createShuttleMenu001(resource));
		return bar;
	}
	
	private JMenu createGameMenu001(ResourceBundle resource)
	{
		JMenu menu = new JMenu(resource.getString("GAME_MENU_CAPTION"));
		menu.add(this.createFileMenuExit001(resource));
		return menu;
	}
	
	private JMenu createDifficultyMenu001(ResourceBundle resource, MainMenu mainMenu)
	{
		JMenu menu = new JMenu(resource.getString("DIFFICULTY_MENU_CAPTION"));
		ButtonGroup group = new ButtonGroup();
		menu.add(mainMenu.addDifficultyItem(Difficulty.EASY, this.createDifficultyMenuEasy001(resource, group)));
		menu.add(mainMenu.addDifficultyItem(Difficulty.MEDIUM, this.createDifficultyMenuMedium001(resource, group)));
		menu.add(mainMenu.addDifficultyItem(Difficulty.HARD, this.createDifficultyMenuHard001(resource, group)));
		return menu;
	}
	
	private JMenu createShuttleMenu001(ResourceBundle resource)
	{
		JMenu menu = new JMenu(resource.getString("SHUTTLE_MENU_CAPTION"));
		menu.add(this.createShuttlePreviousMenuItem001(resource));
		menu.add(this.createShuttleNextMenuItem001(resource));
		return menu;
	}
	
	private JMenuItem createFileMenuExit001(ResourceBundle resource)
	{
		JMenuItem item = new JMenuItem(resource.getString("EXIT_GAME_CAPTION"));
		item.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createExitGameObserver().actionPerformed();
			}
		});
		return item;
	}
	
	private JRadioButtonMenuItem createDifficultyMenuEasy001(ResourceBundle resource, ButtonGroup group)
	{
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(resource.getString("EASY_DIFFICULTY_CAPTION"));
		item.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createEasyDifficultyObserver().actionPerformed();
			}
		});
		group.add(item);
		return item;
	}
	
	private JRadioButtonMenuItem createDifficultyMenuMedium001(ResourceBundle resource, ButtonGroup group)
	{
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(resource.getString("MEDIUM_DIFFICULTY_CAPTION"));
		item.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createMediumDifficultyObserver().actionPerformed();
			}
		});
		group.add(item);
		return item;
	}
	
	private JRadioButtonMenuItem createDifficultyMenuHard001(ResourceBundle resource, ButtonGroup group)
	{
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(resource.getString("HARD_DIFFICULTY_CAPTION"));
		item.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createHardDifficultyObserver().actionPerformed();
				return;
			}
		});
		group.add(item);
		return item;
	}
	
	private JMenuItem createShuttlePreviousMenuItem001(ResourceBundle resource)
	{
		JMenuItem item = new JMenuItem(resource.getString("PREVIOUS_PUZZLE_CAPTION"));
		item.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createPreviousPuzzleObserver().actionPerformed();
				return;
			}
		});
		return item;
	}
	
	private JMenuItem createShuttleNextMenuItem001(ResourceBundle resource)
	{
		JMenuItem item = new JMenuItem(resource.getString("NEXT_PUZZLE_CAPTION"));
		item.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createNextPuzzleObserver().actionPerformed();
				return;
			}
		});
		return item;
	}
	
	private MainToolBar createToolBar001(ResourceBundle resource, MainGUI gui, JLabel idLabel, JLabel bestTimeLabel, Font font)
	{
		MainToolBar bar = new MainToolBar();
		bar.setOrientation(JToolBar.HORIZONTAL);
		//bar.setBorder(BorderFactory.createEtchedBorder());
		bar.setFloatable(false);
		bar.setLayout(new GridBagLayout());
		Dimension size = this.getIdealMinimumButtonSize(new String[]{resource.getString("FIRST_PUZZLE_SYMBOL"), resource.getString("PREVIOUS_PUZZLE_SYMBOL"), resource.getString("NEXT_PUZZLE_SYMBOL"), resource.getString("LAST_PUZZLE_SYMBOL"), resource.getString("SHUFFLE_CAPTION"), resource.getString("SOLVE_CAPTION")}, font);
		ButtonGroup group = new ButtonGroup();
		bar.add(bar.addDifficultyButton(Difficulty.EASY, this.createEasyDifficultyRadioButton001(resource, group)), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
		bar.add(bar.addDifficultyButton(Difficulty.MEDIUM, this.createMediumDifficultyRadioButton001(resource, group)), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(bar.addDifficultyButton(Difficulty.HARD, this.createHardDifficultyRadioButton001(resource, group)), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(new JSeparator(JSeparator.VERTICAL), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
		bar.add(this.configureButton(this.createFirstPuzzleButton001(resource), size.width, size.height, font), new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(this.configureButton(this.createPreviousPuzzleButton001(resource), size.width, size.height, font), new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(idLabel, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(this.configureButton(this.createNextPuzzleButton001(resource), size.width, size.height, font), new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(this.configureButton(this.createLastPuzzleButton001(resource), size.width, size.height, font), new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		bar.add(bestTimeLabel, new GridBagConstraints(9, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 10), 0, 0));
		bar.add(this.configureButton(this.createShuffleButton001(resource), size.width, size.height, font), new GridBagConstraints(10, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		bar.add(this.configureButton(this.createSolveButton001(resource), size.width, size.height, font), new GridBagConstraints(11, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		gui.setMainToolBar(bar);
		return bar;
	}
	
	private JRadioButton createEasyDifficultyRadioButton001(ResourceBundle resource, ButtonGroup group)
	{
		JRadioButton button = new JRadioButton(resource.getString("EASY_DIFFICULTY_CAPTION"));
		button.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(!button.isSelected()){
					return;
				}
				mMediator.createEasyDifficultyObserver().actionPerformed();
				return;
			}
		});
		group.add(button);
		return button;
	}
	
	private JRadioButton createMediumDifficultyRadioButton001(ResourceBundle resource, ButtonGroup group)
	{
		JRadioButton button = new JRadioButton(resource.getString("MEDIUM_DIFFICULTY_CAPTION"));
		button.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(!button.isSelected()){
					return;
				}
				mMediator.createMediumDifficultyObserver().actionPerformed();
				return;
			}
		});
		group.add(button);
		return button;
	}
	
	private JRadioButton createHardDifficultyRadioButton001(ResourceBundle resource, ButtonGroup group)
	{
		JRadioButton button = new JRadioButton(resource.getString("HARD_DIFFICULTY_CAPTION"));
		button.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(!button.isSelected()){
					return;
				}
				mMediator.createHardDifficultyObserver().actionPerformed();
				return;
			}
		});
		group.add(button);
		return button;
	}
	
	private JButton createFirstPuzzleButton001(ResourceBundle resource)
	{
		JButton button = new JButton(resource.getString("FIRST_PUZZLE_SYMBOL"));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createFirstPuzzleObserver().actionPerformed();
				return;
			}
		});
		return button;
	}
	
	private JButton createPreviousPuzzleButton001(ResourceBundle resource)
	{
		JButton button = new JButton(resource.getString("PREVIOUS_PUZZLE_SYMBOL"));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createPreviousPuzzleObserver().actionPerformed();
				return;
			}
		});
		return button;
	}
	
	private JButton createNextPuzzleButton001(ResourceBundle resource)
	{
		JButton button = new JButton(resource.getString("NEXT_PUZZLE_SYMBOL"));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createNextPuzzleObserver().actionPerformed();
				return;
			}
		});
		return button;
	}
	
	private JButton createLastPuzzleButton001(ResourceBundle resource)
	{
		JButton button = new JButton(resource.getString("LAST_PUZZLE_SYMBOL"));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createLastPuzzleObserver().actionPerformed();
				return;
			}
		});
		return button;
	}
	
	private JButton createShuffleButton001(ResourceBundle resource)
	{
		JButton button = new JButton(resource.getString("SHUFFLE_CAPTION"));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createShuffleObserver().actionPerformed();
				return;
			}
		});
		return button;
	}
	
	private JButton createSolveButton001(ResourceBundle resource)
	{
		JButton button = new JButton(resource.getString("SOLVE_CAPTION"));
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mMediator.createSolveObserver().actionPerformed();
				return;
			}
		});
		return button;
	}
	
	private JButton configureButton(JButton button, int width, int height, Font font)
	{
		button.setFont(font);
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setMinimumSize(new Dimension(width, height));
		button.setMaximumSize(new Dimension(width, height));
		button.setPreferredSize(new Dimension(width, height));
		return button;
	}
	
	private StatusBar createStatusBar(MainGUI gui)
	{
		StatusBar bar = null;
		try{
			BufferedImage image = ImageLoader.getImageFromResourcePath(GUIBuilder.class.getResource(this.mMediator.getResizeIconPath()));
			bar = new StatusBar(gui, image);
			bar.setInsets(new Insets(2, 6, 4, 6));
			bar.setLabelInsets(new Insets(0, 10, 0, 0));
			bar.setLabelBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
			bar.addPanel(MainGUI.StatusSection.DIFFICULTY, javax.swing.SwingConstants.LEFT);
			bar.addPanel(MainGUI.StatusSection.PUZZLE_NUM, javax.swing.SwingConstants.LEFT);
			bar.addPanel(MainGUI.StatusSection.TIMER, javax.swing.SwingConstants.CENTER);
			bar.doLayout();
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return bar;
	}
	
	private Dimension getIdealMinimumButtonSize(String[] captions, Font font)
	{
		JButton test = new JButton("test");
		Insets ins = test.getInsets();
		int width = GUIHelper.maxLengthByFont(captions, font) + ins.left;// + ins.right;
		/* Somehow, the button captions look better when the button dimensions are odd numbers. */
		width = width % 2 == 0 ? width + 1 : width;
		int height = GUIHelper.maxHeightByFont("ABCXYZ", font) + ins.top + ins.bottom;
		height = height % 2 == 0 ? height + 1 : height;
		return new Dimension(width, height);
	}
	
	private JLabel createPuzzleIDLabel(Font font)
	{
		JLabel aLabel = new JLabel();
		aLabel.setHorizontalAlignment(SwingConstants.CENTER);
		int width = GUIHelper.maxLengthByFont("00000 of 00000", font) + 20;
		int height = GUIHelper.maxHeightByFont("00000 of 00000", font) + 2;
		aLabel.setMinimumSize(new Dimension(width, height));
		aLabel.setMaximumSize(new Dimension(width, height));
		aLabel.setPreferredSize(new Dimension(width, height));
		return aLabel;
	}
	
	private JLabel createBestTimeLabel(ResourceBundle resource, Font font)
	{
		JLabel aLabel = new JLabel();
		aLabel.setHorizontalAlignment(SwingConstants.LEFT);
		int minWidth = GUIHelper.maxLengthByFont(resource.getString("BEST_TIME_CAPTION") + "000:00:00", font) + 20;
		int height = GUIHelper.maxHeightByFont(resource.getString("BEST_TIME_CAPTION") + "000:00:00", font) + 2;
		aLabel.setMinimumSize(new Dimension(minWidth, height));
		aLabel.setMaximumSize(new Dimension(1000, height));
		aLabel.setPreferredSize(new Dimension(minWidth, height));
		return aLabel;
	}
	
	private ComponentListener createComponentListener()
	{
		return new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				mMediator.resizePerformed();
				return;
			}
		};
	}
	
	private MouseListener createMouseListener(MainGUI gui)
	{
		return new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				mMediator.partSelectionRequested(e.getPoint());
				if(e.getButton() > 1){
					mMediator.partRotateRequested(e.getPoint());
					mMediator.partUnselectionRequested();
				}
				gui.setXDiff(Math.floorMod(e.getX() - mMediator.getPuzzleBounds().x, mMediator.getTileWidth()));
				gui.setYDiff(Math.floorMod(e.getY() - mMediator.getPuzzleBounds().y, mMediator.getTileWidth()));
				return;
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if(e.getButton() < 2){
					mMediator.partMoveRequested(e.getPoint());
				}
				gui.setXDiff(0);
				gui.setYDiff(0);
				mMediator.partUnselectionRequested();
				gui.setMouseIsDragging(false);
				return;
			}
		};
	}
	
	private MouseMotionListener createMouseMotionListener(MainGUI gui)
	{
		return new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				gui.setMousePosition(e.getX(), e.getY());
				if(e.getButton() < 2){
					gui.setMouseIsDragging(true);
				}
				return;
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				gui.setMousePosition(e.getX(), e.getY());
				return;
			}
		};
	}
	
	private WindowListener createWindowListener()
	{
		return new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				mMediator.beginAppShutdown();
				return;
			}
			
			@Override
			public void windowOpened(WindowEvent e)
			{
				return;
			}
		};
	}
	
	public interface Mediator
	{
		abstract ResourceBundle getAppResource();
		abstract ResourceBundle getMainResource();
		abstract ExitGameObserver createExitGameObserver();
		abstract EasyDifficultyObserver createEasyDifficultyObserver();
		abstract FirstPuzzleObserver createFirstPuzzleObserver();
		abstract LastPuzzleObserver createLastPuzzleObserver();
		abstract MediumDifficultyObserver createMediumDifficultyObserver();
		abstract HardDifficultyObserver createHardDifficultyObserver();
		abstract PreviousPuzzleObserver createPreviousPuzzleObserver();
		abstract NextPuzzleObserver createNextPuzzleObserver();
		abstract ShuffleObserver createShuffleObserver();
		abstract SolveObserver createSolveObserver();
		abstract void partSelectionRequested(Point p);
		abstract void partRotateRequested(Point p);
		abstract void partUnselectionRequested();
		abstract void partMoveRequested(Point p);
		abstract Rectangle getPuzzleBounds();
		abstract int getTileWidth();
		abstract String getResizeIconPath();
		abstract void resizePerformed();
		abstract void beginAppShutdown();
	}
}

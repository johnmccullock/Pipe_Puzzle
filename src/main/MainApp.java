package main;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import main.core.Difficulty;
import main.core.Display;
import main.core.EasyDifficultyObserver;
import main.core.Engine;
import main.core.ExitGameObserver;
import main.core.FirstPuzzleObserver;
import main.core.HardDifficultyObserver;
import main.core.LastPuzzleObserver;
import main.core.MainGUI;
import main.core.MediumDifficultyObserver;
import main.core.NewGameObserver;
import main.core.NextPuzzleObserver;
import main.core.PipePuzzle;
import main.core.PreviousPuzzleObserver;
import main.core.ShuffleObserver;
import main.core.SolveObserver;
import main.core.XMLAttrib;
import main.core.XMLReadBestTimes;
import main.core.XMLTag;
import main.core.util.ReadWriteFile;
import main.core.util.Ticker;
import main.core.util.XMLReader;
import main.core.util.XMLUtil;
import main.core.util.XMLWriter;

public class MainApp
{
	private static final String CONFIG_FILE_PATH = "config.xml";
	
	private Engine mEngine = null;
	private MainGUI mMainGUI = null;
	private Ticker mTicker = null;
	private HashMap<Difficulty, HashMap<Integer, Long>> mBestTimes = new HashMap<Difficulty, HashMap<Integer, Long>>();
	private ResourceBundle mAppResource = null;
	private ResourceBundle mMainResource = null;
	
	public MainApp()
	{
		this.initializeLookAndFeel();
		this.fillBestTimesMap();
		this.retrieveI18NResources();
		this.mMainGUI = new MainGUI();
		this.mEngine = new Engine(this.createEngineMediator(this.mMainGUI), this.getDataPaths());
		GUIBuilder builder = new GUIBuilder(this.createGUIBuilderMediator(this.mEngine), this.createDisplayMediator(this.mEngine));
		this.mMainGUI = builder.createPrimaryStyle(this.mMainGUI);
		
		this.mMainGUI.setDifficultyValue(this.readConfigDifficulty());
		this.mMainGUI.getStatusBar().setText(MainGUI.StatusSection.DIFFICULTY, this.mEngine.getDifficultyStatus());
		this.mMainGUI.getStatusBar().setText(MainGUI.StatusSection.PUZZLE_NUM, this.mEngine.getPuzzleNumberStatus());
		this.mMainGUI.getStatusBar().setText(MainGUI.StatusSection.TIMER, this.mEngine.getTimeStatus(0));
		
		mTicker = new Ticker(createTickerObserver(this.mMainGUI, this.mEngine), 60, 60, 1);
		mTicker.startTimer();
		return;
	}
	
	private void retrieveI18NResources()
	{
		try{
			XMLReader reader = XMLUtil.getReaderAbsolute(CONFIG_FILE_PATH, XMLTag.CONFIGS);
			if(reader == null){
				throw new Exception("Unable to read config.xml file.");
			}
			String language = reader.readStringAttribute(XMLTag.LOCALE, XMLAttrib.LANGUAGE);
			String region = reader.readStringAttribute(XMLTag.LOCALE, XMLAttrib.REGION);
			Locale locale = new Locale.Builder().setLanguage(language).setRegion(region).build();
			this.mAppResource = ResourceBundle.getBundle("resources.Application", locale);
			this.mMainResource = ResourceBundle.getBundle("resources.MainGUI", locale);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return;
	}
	
	private HashMap<Difficulty, String> getDataPaths()
	{
		HashMap<Difficulty, String> paths = null;
		try{
			XMLReader reader = XMLUtil.getReaderAbsolute("config.xml", XMLTag.PUZZLES);
			if(reader == null){
				throw new Exception("Unable to read config.xml file.");
			}
			paths = new HashMap<Difficulty, String>();
			paths.put(Difficulty.EASY, reader.readStringAttribute(XMLTag.EASY, XMLAttrib.PATH));
			paths.put(Difficulty.MEDIUM, reader.readStringAttribute(XMLTag.MEDIUM, XMLAttrib.PATH));
			paths.put(Difficulty.HARD, reader.readStringAttribute(XMLTag.HARD, XMLAttrib.PATH));
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return paths;
	}
	
	private Difficulty readConfigDifficulty()
	{
		Difficulty dif = null;
		try{
			XMLReader reader = XMLUtil.getReaderAbsolute("config.xml", XMLTag.CONFIGS);
			if(reader == null){
				throw new Exception("Unable to read config.xml file.");
			}
			String rawText = reader.readString(XMLTag.DIFFICULTY);
			dif = Difficulty.getTypeForName(rawText);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return dif;
	}
	
	public String readImagePath(String xmlTag)
	{
		String path = null;
		try{
			XMLReader reader = XMLUtil.getReaderAbsolute("config.xml", XMLTag.CONFIGS);
			if(reader == null){
				throw new Exception("Unable to read config.xml file.");
			}
			path = reader.readStringAttribute(xmlTag, XMLAttrib.PATH);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return path;
	}
	
	public String readResizeGripPath()
	{
		String path = null;
		try{
			XMLReader reader = XMLUtil.getReaderAbsolute("config.xml", XMLTag.CONFIGS);
			if(reader == null){
				throw new Exception("Unable to read config.xml file.");
			}
			path = reader.readStringAttribute(XMLTag.RESIZE_GRIP, XMLAttrib.PATH);
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		return path;
	}
	
	private void fillBestTimesMap()
	{
		try{
			for(Difficulty dif : Difficulty.values())
			{
				this.mBestTimes.put(dif, new HashMap<Integer, Long>());
			}
			XMLReader reader = XMLUtil.getReaderAbsolute("config.xml", XMLTag.CONFIGS);
			this.mBestTimes = new XMLReadBestTimes(reader).read(this.mBestTimes);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return;
	}
	
	private void saveConfiguration()
	{
		String xml = this.prepareXMLToWrite();
		this.writeConfigFile(xml);
		return;
	}
	
	private String prepareXMLToWrite()
	{
		/* Worst config file ever.  Since most of the properties won't change, this routine is just copying most of it. */ 
		String xml = null;
		try{
			XMLReader reader = XMLUtil.getReaderAbsolute(CONFIG_FILE_PATH, XMLTag.CONFIGS);
			XMLWriter writer = new XMLWriter();
			writer.getEventWriter().add(writer.getEventFactory().createStartDocument());
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.ROOT));
			
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.LOCALE));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.LANGUAGE, reader.readStringAttribute(XMLTag.LOCALE, XMLAttrib.LANGUAGE)));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.REGION, reader.readStringAttribute(XMLTag.LOCALE, XMLAttrib.REGION)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.LOCALE));
			
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.BEST_TIMES));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.BEST_TIMES));
			
			writer.addXMLElement(XMLTag.DIFFICULTY, this.mEngine.getDifficulty().name());
			
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.IMAGES));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.CROSS));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.CROSS, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.CROSS));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.ELBOW));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.ELBOW, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.ELBOW));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.GREEN_GAUGE));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.GREEN_GAUGE, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.GREEN_GAUGE));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.PIPE));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.PIPE, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.PIPE));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.RED_GAUGE));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.RED_GAUGE, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.RED_GAUGE));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.RESIZE_GRIP));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.RESIZE_GRIP, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.RESIZE_GRIP));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.T_JUNCTION));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.T_JUNCTION, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.T_JUNCTION));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.TILE));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.TILE, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.TILE));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.IMAGES));
			
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.PUZZLES));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.EASY));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.EASY, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.EASY));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.MEDIUM));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.MEDIUM, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.MEDIUM));
			writer.getEventWriter().add(writer.getEventFactory().createStartElement("", "", XMLTag.HARD));
			writer.getEventWriter().add(writer.getEventFactory().createAttribute(XMLAttrib.PATH, reader.readStringAttribute(XMLTag.HARD, XMLAttrib.PATH)));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.HARD));
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.PUZZLES));
			
			writer.getEventWriter().add(writer.getEventFactory().createEndElement("", "", XMLTag.ROOT));
			writer.getEventWriter().add(writer.getEventFactory().createEndDocument());
			
			xml = writer.serializeXML(writer.getStringWriter().toString());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return xml;
	}
	
	private void writeConfigFile(String xml)
	{
		try{
			ReadWriteFile.write(CONFIG_FILE_PATH, xml);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void beginAppShutdown()
	{
		this.mTicker.stopTimer();
		this.saveConfiguration();
		this.mMainGUI.dispose();
		System.exit(0);
		return;
	}
	
	private Engine.Mediator createEngineMediator(MainGUI gui)
	{
		return new Engine.Mediator()
		{
			@Override
			public ResourceBundle getAppResource()
			{
				return MainApp.this.mAppResource;
			}
			
			@Override
			public ResourceBundle getMainResource()
			{
				return MainApp.this.mMainResource;
			}
			
			@Override
			public String readImagePath(String xmlTag)
			{
				return MainApp.this.readImagePath(xmlTag);
			}
			
			@Override
			public Dimension getGameBoardSize()
			{
				return gui.getDisplaySize();
			}
			
			@Override
			public boolean getShowCursor()
			{
				return gui.getShowCursor();
			}
			
			@Override
			public Point getMousePosition()
			{
				return gui.getMousePosition();
			}
			
			@Override
			public boolean getMouseIsDragging()
			{
				return gui.getMouseIsDragging();
			}
			
			@Override
			public int getXDiff()
			{
				return gui.getXDiff();
			}
			
			@Override
			public int getYDiff()
			{
				return gui.getYDiff();
			}
			
			@Override
			public void displayDifficultyData(String caption)
			{
				gui.displayDifficultyData(caption);
				return;
			}
			
			@Override
			public void displayDifficultyStatus(String caption)
			{
				gui.getStatusBar().setText(MainGUI.StatusSection.DIFFICULTY, caption);
				return;
			}
			
			@Override
			public void displayTimeDuration(String caption)
			{
				gui.getStatusBar().setText(MainGUI.StatusSection.TIMER, caption);
				return;
			}
			
			@Override
			public void displayPuzzleNumberStatus(String caption)
			{
				gui.getStatusBar().setText(MainGUI.StatusSection.PUZZLE_NUM, caption);
				return;
			}
			
			@Override
			public void beginAppShutdown()
			{
				MainApp.this.beginAppShutdown();
				return;
			}
		};
	}
	
	private GUIBuilder.Mediator createGUIBuilderMediator(Engine engine)
	{
		return new GUIBuilder.Mediator()
		{
			@Override
			public ResourceBundle getAppResource()
			{
				return MainApp.this.mAppResource;
			}
			
			@Override
			public ResourceBundle getMainResource()
			{
				return MainApp.this.mMainResource;
			}
			
			@Override
			public ExitGameObserver createExitGameObserver()
			{
				return engine.createExitGameObserver();
			}
			
			@Override
			public EasyDifficultyObserver createEasyDifficultyObserver()
			{
				return engine.createEasyDifficultyObserver();
			}
			
			@Override
			public FirstPuzzleObserver createFirstPuzzleObserver()
			{
				return engine.createFirstPuzzleObserver();
			}
			
			@Override
			public LastPuzzleObserver createLastPuzzleObserver()
			{
				return engine.createLastPuzzleObserver();
			}
			
			@Override
			public MediumDifficultyObserver createMediumDifficultyObserver()
			{
				return engine.createMediumDifficultyObserver();
			}
			
			@Override
			public HardDifficultyObserver createHardDifficultyObserver()
			{
				return engine.createHardDifficultyObserver();
			}
			
			@Override
			public PreviousPuzzleObserver createPreviousPuzzleObserver()
			{
				return engine.createPreviousPuzzleObserver();
			}
			
			@Override
			public NextPuzzleObserver createNextPuzzleObserver()
			{
				return engine.createNextPuzzleObserver();
			}
			
			@Override
			public ShuffleObserver createShuffleObserver()
			{
				return engine.createShuffleObserver();
			}
			
			@Override
			public SolveObserver createSolveObserver()
			{
				return engine.createSolveObserver();
			}
			
			@Override
			public void partSelectionRequested(Point p)
			{
				engine.partSelectionRequested(p);
				return;
			}
			
			@Override
			public void partRotateRequested(Point p)
			{
				engine.partRotateRequested(p);
				return;
			}
			
			@Override
			public void partUnselectionRequested()
			{
				engine.partUnselectionRequested();
				return;
			}
			
			@Override
			public void partMoveRequested(Point p)
			{
				engine.partMoveRequested(p);
				return;
			}
			
			@Override
			public Rectangle getPuzzleBounds()
			{
				return engine.getBounds();
			}
			
			@Override
			public int getTileWidth()
			{
				return engine.getTileWidth();
			}
			
			@Override
			public String getResizeIconPath()
			{
				return MainApp.this.readResizeGripPath();
			}
			
			@Override
			public void resizePerformed()
			{
				engine.resizeUIPerformed();
				return;
			}
			
			@Override
			public void beginAppShutdown()
			{
				MainApp.this.beginAppShutdown();
			}
		};
	}
	
	private Display.Mediator createDisplayMediator(Engine engine)
	{
		return new Display.Mediator()
		{
			@Override
			public void render(Graphics2D g2d)
			{
				engine.render(g2d);
				return;
			}
		};
	}
	
	private Ticker.Observer createTickerObserver(MainGUI gui, Engine e)
	{
		return new Ticker.Observer()
		{
			@Override
			public void tickerUpdateRequested()
			{
				e.update();
				return;
			}
			
			@Override
			public void tickerRenderRequested()
			{
				gui.repaintDisplay();
				return;
			}
		};
	}
	
	private void initializeLookAndFeel()
	{
		try{
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if("Windows".equals(info.getName())){
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			try{
				JFrame.setDefaultLookAndFeelDecorated(true);
			}catch(Exception ex2){
				ex2.printStackTrace();
				System.exit(1);
			}
		}
		return;
	}
	
	public static void main(String[] args)
	{
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new MainApp();
				//System.out.println(main.core.util.MathUtil.easeInQuad(99.0, 0.0, 5.0, 100.0));
			}
		});
		return;
	}
}

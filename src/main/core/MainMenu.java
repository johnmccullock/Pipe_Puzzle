package main.core;

import java.util.HashMap;

import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar
{
	private HashMap<Difficulty, JRadioButtonMenuItem> mDiffItems = new HashMap<Difficulty, JRadioButtonMenuItem>();
	
	public void setDifficultySelected(Difficulty key)
	{
		this.mDiffItems.get(key).setSelected(true);
		return;
	}
	
	public JRadioButtonMenuItem addDifficultyItem(Difficulty dif, JRadioButtonMenuItem item)
	{
		this.mDiffItems.put(dif, item);
		return item;
	}
}

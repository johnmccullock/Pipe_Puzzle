package main.core;

import java.util.HashMap;

import javax.swing.JRadioButton;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class MainToolBar extends JToolBar
{
	private HashMap<Difficulty, JRadioButton> mDiffButtons = new HashMap<Difficulty, JRadioButton>();
	
	public void setDifficultySelected(Difficulty key)
	{
		this.mDiffButtons.get(key).setSelected(true);
		return;
	}
	
	public JRadioButton addDifficultyButton(Difficulty dif, JRadioButton button)
	{
		this.mDiffButtons.put(dif, button);
		return button;
	}
}

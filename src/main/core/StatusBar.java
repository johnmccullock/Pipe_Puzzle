package main.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import java.util.LinkedHashMap;

import javax.swing.border.Border;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class StatusBar extends JPanel
{
	private JPanel mSectionPanel = null;
	private LinkedHashMap<Object, StatusPane> mPanes = new LinkedHashMap<Object, StatusPane>();
	private ResizeGrip mResizeGrip = null;
	private Insets mInsets = new Insets(0, 0, 0, 0);
	private Insets mLabelInsets = new Insets(0, 0, 0, 0);
	private Border mLabelBorder = null;
	
	public StatusBar(JFrame parent, BufferedImage resizeIcon)
	{
		this.setLayout(new GridBagLayout());
		this.mResizeGrip = new ResizeGrip(parent, resizeIcon);
	}
	
	public void addPanel(Object key, int alignment)
	{
		this.mPanes.put(key, new StatusPane(alignment));
		return;
	}
	
	@Override
	public void doLayout()
	{
		this.removeAll();
		/* Set preferred size here to force empty JLabels to full size. */
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, this.mResizeGrip.getImage().getHeight() + this.mInsets.top + this.mInsets.bottom));
		
		this.mSectionPanel = new JPanel();
		this.mSectionPanel.setLayout(new GridLayout(1, this.mPanes.size(), 0, 0));
		for(Object key : this.mPanes.keySet())
		{
			this.mSectionPanel.add(this.mPanes.get(key));
		}
		this.add(this.mSectionPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, this.mInsets, 0, 0));
		this.add(this.mResizeGrip, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		super.doLayout();
		return;
	}
	
	public void setText(Object key, String text)
	{
		this.mPanes.get(key).setText(text);
		return;
	}
	
	public void setInsets(Insets insets)
	{
		this.mInsets = insets;
		return;
	}
	
	public void setLabelInsets(Insets insets)
	{
		this.mLabelInsets = insets;
		return;
	}
	
	public void setLabelBorder(Border border)
	{
		this.mLabelBorder = border;
		return;
	}
	
	private class StatusPane extends JPanel
	{
		private JLabel mLabel = null;
		
		public StatusPane(int alignment)
		{
			this.setLayout(new GridBagLayout());
			this.setBorder(mLabelBorder);
			this.mLabel = new JLabel("test");
			this.mLabel.setHorizontalAlignment(alignment);
			this.add(this.mLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, mLabelInsets, 0, 0));
			return;
		}
		
		public void setText(String text)
		{
			this.mLabel.setText(text);
			return;
		}
	}
}

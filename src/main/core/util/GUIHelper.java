package main.core.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

/**
 * Handy functions for configuring GUI components.
 *
 * Version 1.3.02 removes hard-coded caption text from TextComponentEditPopup class.
 *
 * Version 1.3.01 adds minor fix to createCommandButton() method, so listener parameter can be null.
 *
 * Version 1.3 adds LabelBasedTableCellRenderer functionality, and ImageIcon option with TextComponentEditPopup.
 *
 * Version 1.2 adds normalizeBoundsToScreen() method.
 *
 * Version 1.1 adds createCommandButton() method.
 *
 * @author John McCullock
 * @version 1.3.02 2017-11-11
 */
@SuppressWarnings("serial")
public class GUIHelper
{
	public static Font getFont(String face, int size, boolean isBold, boolean isItalic)
	{
		Font aFont = null;
		if(isBold && isItalic){
			aFont = new Font(face, Font.BOLD + Font.ITALIC, size);
		}else if(isBold && !isItalic){
			aFont = new Font(face, Font.BOLD, size);
		}else if(!isBold && isItalic){
			aFont = new Font(face, Font.ITALIC, size);
		}else{
			aFont = new Font(face, Font.PLAIN, size);
		}
		return aFont;
	}
	
	public static int maxLengthByFont(String text, Font font)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage temp = gc.createCompatibleImage(100, 100);
		Graphics2D g2d = (Graphics2D)temp.getGraphics();
		return g2d.getFontMetrics(font).stringWidth(text);
	}
	
	public static int maxLengthByFont(String[] items, Font font)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage temp = gc.createCompatibleImage(100, 100);
		Graphics2D g2d = (Graphics2D)temp.getGraphics();
		int longest = 0;
		for(int i = 0; i < items.length; i++)
		{
			longest = Math.max(g2d.getFontMetrics(font).stringWidth(items[i]), longest);
		}
		return longest;
	}
	
	public static int maxLengthByFont(String[] items, Graphics gr)
	{
		int longest = 0;
		for(int i = 0; i < items.length; i++)
		{
			longest = Math.max(gr.getFontMetrics(gr.getFont()).stringWidth(items[i]), longest);
		}
		return longest;
	}
	
	public static int maxHeightByFont(String test, Font font)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage temp = gc.createCompatibleImage(100, 100);
		Graphics2D g2d = (Graphics2D)temp.getGraphics();
		return g2d.getFontMetrics(font).getHeight();
	}
	
	/**
	 * Finds ideal size for uniform-sized buttons based on their longest caption and button font.
	 * @param items String array of all captions for a set of buttons.
	 * @param widthFactor double value to multiply by font and longest caption.
	 * @param heightFactor double value to multiply by font and longest caption.
	 * @param topBottomBorderWidthSum int sum of top and bottom button border widths.
	 * @return Dimension
	 */
	public static Dimension getMaxButtonSize(String[] items, double widthFactor, double heightFactor, int topBottomBorderWidthSum)
	{
		Font temp = UIManager.getDefaults().getFont("Button.font");
		int width = (int)Math.round(maxLengthByFont(items, temp) * widthFactor);
		int height = (int)Math.round((maxHeightByFont("Test", temp) * heightFactor) - topBottomBorderWidthSum);
		return new Dimension(width, height);
	}
	
	/**
	 * Convenience method for creating a specifically sized button.
	 * @param maxSize Dimension for the button.
	 * @param caption String containing the button caption.
	 * @param command String containing the button command.
	 * @param tooltip String containing the button tooltip.
	 * @param listener ActionListener associated with the button. Can be null.
	 * @return JButton
	 */
	public static JButton createCommandButton(Dimension maxSize, String caption, String command, String tooltip, ActionListener listener)
	{
		JButton button = new JButton(caption);
		if(UIManager.getLookAndFeel().getName().equalsIgnoreCase("nimbus")){
			// found at: https://stackoverflow.com/questions/8764602/how-to-override-nimbus-button-margins-for-a-single-button
			UIDefaults def = new UIDefaults();
			def.put("Button.contentMargins", new Insets(0, 0, 0, 0));
			button.putClientProperty("Nimbus.Overrides", def);
		}else{
			button.setMargin(new Insets(0, 0, 0, 0));
		}
		button.setMinimumSize(new Dimension(maxSize.width, maxSize.height));
		button.setMaximumSize(new Dimension(maxSize.width, maxSize.height));
		button.setPreferredSize(new Dimension(maxSize.width, maxSize.height));
		button.setToolTipText(tooltip);
		button.setActionCommand(command);
		if(listener != null){
			button.addActionListener(listener);
		}
		return button;
	}
	
	/**
	 * General popup menu class for java.swing text editing components (i.e.: JTextField, JTextArea, JTextPane, etc.).  It
	 * uses the editing functions already built in to the JTextComponent and Document classes.
	 * @param field JTextComponent Any class which extends the JTextComponent class.
	 * @param includeUndo boolean true to include "Undo" in popup menu, false to omit.
	 * @param includeRedo boolean true to include "Redo" in popup menu, false to omit.
	 * @param includeCut boolean true to include "Cut" in popup menu, false to omit.
	 * @param includeCopy boolean true to include "Copy" in popup menu, false to omit.
	 * @param includePaste boolean true to include "Paste" in popup menu, false to omit.
	 * @param includeSelectAll boolean true to include "Select All" in popup menu, false to omit.
	 * @return TextComponentEditPopup
	 */
	public static TextComponentEditPopup createEditorPopupMenu(JTextComponent field, boolean includeUndo, boolean includeRedo, boolean includeCut, boolean includeCopy, boolean includePaste, boolean includeSelectAll)
	{
		return new TextComponentEditPopup(field, includeUndo, null, null, includeRedo, null, null, includeCut, null, null, includeCopy, null, null, includePaste, null, null, includeSelectAll, null, null);
	}
	
	public static TextComponentEditPopup createEditorPopupMenu(JTextComponent field, boolean includeUndo, String undoCaption, ImageIcon undoIcon, boolean includeRedo, String redoCaption, ImageIcon redoIcon, boolean includeCut, String cutCaption, ImageIcon cutIcon, boolean includeCopy, String copyCaption, ImageIcon copyIcon, boolean includePaste, String pasteCaption, ImageIcon pasteIcon, boolean includeSelectAll, String selectAllCaption, ImageIcon selectAllIcon)
	{
		return new TextComponentEditPopup(field, includeUndo, undoCaption, undoIcon, includeRedo, redoCaption, redoIcon, includeCut, cutCaption, cutIcon, includeCopy, copyCaption, copyIcon, includePaste, pasteCaption, pasteIcon, includeSelectAll, selectAllCaption, selectAllIcon);
	}
	
	private static class TextComponentEditPopup extends JPopupMenu
	{
		private UndoHandler undoHandler = new UndoHandler();
		private UndoManager undoManager = new UndoManager();
		private UndoAction undoAction = null;
		private RedoAction redoAction = null;
		
		public TextComponentEditPopup(JTextComponent field, boolean includeUndo, String undoCaption, ImageIcon undoIcon, boolean includeRedo, String redoCaption, ImageIcon redoIcon, boolean includeCut, String cutCaption, ImageIcon cutIcon, boolean includeCopy, String copyCaption, ImageIcon copyIcon, boolean includePaste, String pasteCaption, ImageIcon pasteIcon, boolean includeSelectAll, String selectAllCaption, ImageIcon selectAllIcon)
		{
			undoAction = new UndoAction(undoCaption);
			redoAction = new RedoAction(redoCaption);
			field.getDocument().addUndoableEditListener(undoHandler);
			
			if(includeUndo){
				JMenuItem item = new JMenuItem(undoCaption);
				item.setAction(undoAction);
				if(undoIcon != null){
					item.setIcon(undoIcon);
				}
				this.add(item);
			}
			if(includeRedo){
				JMenuItem item = new JMenuItem(redoCaption);
				item.setAction(redoAction);
				if(redoIcon != null){
					item.setIcon(redoIcon);
				}
				this.add(item);
			}
			if((includeUndo || includeRedo) && (includeCut || includeCopy || includePaste || includeSelectAll)){
				this.addSeparator();
			}
			if(includeCut){
				JMenuItem item = new JMenuItem(cutCaption);
				if(cutIcon != null){
					item.setIcon(cutIcon);
				}
				item.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						field.cut();
						return;
					}
				});
				this.add(item);
			}
			if(includeCopy){
				JMenuItem item = new JMenuItem(copyCaption);
				if(copyIcon != null){
					item.setIcon(copyIcon);
				}
				item.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						field.copy();
						return;
					}
				});
				this.add(item);
			}
			if(includePaste){
				JMenuItem item = new JMenuItem(pasteCaption);
				if(pasteIcon != null){
					item.setIcon(pasteIcon);
				}
				item.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						field.paste();
						return;
					}
				});
				this.add(item);
			}
			if((includeUndo || includeRedo || includeCut || includeCopy || includePaste) && includeSelectAll){
				this.addSeparator();
			}
			if(includeSelectAll){
				JMenuItem item = new JMenuItem(selectAllCaption);
				if(selectAllIcon != null){
					item.setIcon(selectAllIcon);
				}
				item.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						field.selectAll();
						return;
					}
				});
				this.add(item);
			}
			return;
		}
		
		private class UndoHandler implements UndoableEditListener
		{
			public void undoableEditHappened(UndoableEditEvent e)
			{
				undoManager.addEdit(e.getEdit());
				undoAction.update();
				redoAction.update();
				return;
			}
		}
		
		private class UndoAction extends AbstractAction
		{
			private String mCaption = null;
			
			public UndoAction(String caption)
			{
				super(caption);
				this.mCaption = caption;
				this.setEnabled(false);
				return;
			}
			
			public void actionPerformed(ActionEvent e)
			{
				try{
					undoManager.undo();
				}catch(Exception ex){
					ex.printStackTrace();
				}
				this.update();
				redoAction.update();
				return;
			}
			
			public void update()
			{
				if(undoManager.canUndo()){
					this.setEnabled(true);
					this.putValue(Action.NAME, this.mCaption);
				}else{
					this.setEnabled(false);
					this.putValue(Action.NAME, this.mCaption);
				}
			}
		}
		
		private class RedoAction extends AbstractAction
		{
			private String mCaption = null;
			
			public RedoAction(String caption)
			{
				super(caption);
				this.mCaption = caption;
				this.setEnabled(false);
				return;
			}
			
			public void actionPerformed(ActionEvent e)
			{
				try{
					undoManager.redo();
				}catch(Exception ex){
					ex.printStackTrace();
				}
				this.update();
				undoAction.update();
				return;
			}
			
			public void update()
			{
				if(undoManager.canRedo()){
					this.setEnabled(true);
					this.putValue(Action.NAME, this.mCaption);
				}else{
					this.setEnabled(false);
					this.putValue(Action.NAME, this.mCaption);
				}
			}
		}
	}
	
	/**
	 * General FocusListener which performs "Select All" when text component gains focus.  Uses "Select All" function built into
	 * classes which extend javax.swing JTextComponent.
	 * @param field JTextComponent
	 * @return FocusListener
	 */
	public static FocusListener createSelectAllTextFocusListener(JTextComponent field)
	{
		return new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				field.selectAll();
				return;
			}
			
			public void focusLost(FocusEvent e)
			{
				field.select(field.getText().length(), field.getText().length());
			}
		};
	}
	
	/**
	 * A general ListCellRenderer which adds margins and text alignment.  This implementation only supports text values.
	 * The only alignment values accepted are SwingConstants LEFT, CENTER and RIGHT.
	 * @param alignment int values from SwingConstants LEFT, CENTER and RIGHT constants.
	 * @param margins Insets object with values for top, left, bottom and right.
	 * @return LabelBasedListCellRenderer{@literal <}T{@literal >}
	 */
	public static <T> LabelBasedListCellRenderer<T> createLabelBasedListCellRenderer(int alignment, Insets margins)
	{
		return new LabelBasedListCellRenderer<T>(alignment, margins);
	}
	
	private static class LabelBasedListCellRenderer<T> extends JLabel implements ListCellRenderer<T>
	{
		private int mAlignment = SwingConstants.LEFT;
		private Insets mMargins = null;
		
		public LabelBasedListCellRenderer() { return; }
		
		public LabelBasedListCellRenderer(int alignment, Insets margins)
		{
			this.setAlignment(alignment);
			this.setMargins(margins);
			return;
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus)
		{
			setOpaque(true);
			if(isSelected){
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}else{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setHorizontalAlignment(this.mAlignment);
			if(this.mMargins != null){
				setBorder(BorderFactory.createEmptyBorder(this.mMargins.top, this.mMargins.left, this.mMargins.bottom, this.mMargins.right));
			}
			setText(String.valueOf(value));
			setFont(list.getFont());
			return this;
		}
		
		public void setAlignment(int align)
		{
			if(align != SwingConstants.LEFT && align != SwingConstants.CENTER && align != SwingConstants.RIGHT){
				throw new IllegalArgumentException("Can only accept SwingConstants: LEFT, CENTER and RIGHT.");
			}
			this.mAlignment = align;
			return;
		}
		
		public int getAlignment()
		{
			return this.mAlignment;
		}
		
		public void setMargins(Insets margins)
		{
			this.mMargins = margins;
			return;
		}
		
		public Insets getMargins()
		{
			return this.mMargins;
		}
	}
	
	/**
	 * Clamps a rectangle's x and y coordinates inside the current screen size.
	 * @param bounds Rectangle meant to represent the bound of a frame or dialog.
	 * @return Rectangle
	 */
	public static Rectangle normalizeBoundsToScreen(Rectangle bounds)
	{
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		bounds.x = bounds.x < 0 ? 0 : bounds.x;
		bounds.x = bounds.x + bounds.width > size.width ? size.width - bounds.width : bounds.x;
		bounds.y = bounds.y < 0 ? 0 : bounds.y;
		bounds.y = bounds.y + bounds.height > size.height ? size.height - bounds.height : bounds.y;
		return bounds;
	}
	
	public static LabelBasedTableCellRenderer createLabelBasedTableCellRenderer(int align, Insets margin)
	{
		return new LabelBasedTableCellRenderer(align, margin);
	}
	
	private static class LabelBasedTableCellRenderer extends JLabel implements TableCellRenderer
	{
		private int mAlignment = SwingConstants.LEFT;
		private Insets mMargins = null;
		
		public LabelBasedTableCellRenderer() { return; }
		
		public LabelBasedTableCellRenderer(int align, Insets margin)
		{
			this.setAlignment(align);
			this.setMargins(margin);
			return;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			this.setOpaque(true);
			if(isSelected){
				this.setBackground(table.getSelectionBackground());
			}else{
				this.setBackground(table.getBackground());
			}
			this.setBorder(BorderFactory.createEmptyBorder(this.mMargins.top, this.mMargins.left, this.mMargins.bottom, this.mMargins.right));
			this.setHorizontalAlignment(this.mAlignment);
			this.setText(String.valueOf(value));
			this.setFont(table.getFont());
			return this;
		}
		
		public void setAlignment(int align)
		{
			if(align != SwingConstants.LEFT && align != SwingConstants.CENTER && align != SwingConstants.RIGHT){
				throw new IllegalArgumentException("Can only accept SwingConstants: LEFT, CENTER and RIGHT.");
			}
			this.mAlignment = align;
			return;
		}
		
		public int getAlignment()
		{
			return this.mAlignment;
		}
		
		public void setMargins(Insets margins)
		{
			this.mMargins = margins;
			return;
		}
		
		public Insets getMargins()
		{
			return this.mMargins;
		}
	}
}

package main.core;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Display extends JPanel
{
	private Display.Mediator mMediator = null;
	
	public Display(Display.Mediator mediator)
	{
		this.mMediator = mediator;
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		this.setBackground(java.awt.Color.BLUE);
		//this.setIgnoreRepaint(true);
		return;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if(this.mMediator == null){
			return;
		}
		Graphics2D g2d = (Graphics2D)g;
		
		//g2d.setPaint(java.awt.Color.BLUE);
		//g2d.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		
		this.mMediator.render(g2d);
		g2d.dispose();
		return;
	}
	
	public interface Mediator
	{
		abstract void render(Graphics2D g2d);
	}
}

package main.core;

import java.awt.Graphics2D;

import main.core.util.State;

public interface GameState extends State
{
	abstract void render(Graphics2D g2d);
}

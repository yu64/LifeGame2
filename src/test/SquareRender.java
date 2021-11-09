package test;

import java.awt.Graphics2D;

import canvas2.view.scene.Drawable;

public class SquareRender implements Drawable{

	private int w;
	private int h;

	public void setSize(int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	public void setSquare(Drawable d)
	{

	}

	public void setSquareSize(int squareW, int squareH)
	{

	}

	@Override
	public void draw(Graphics2D g2)
	{

	}

	@FunctionalInterface
	public static interface SquareDrawable extends Drawable{



	}
}

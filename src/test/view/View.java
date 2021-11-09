package test.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.logic.AppLogic;
import canvas2.view.scene.Area;
import canvas2.view.scene.Node;
import test.model.CellData;
import test.model.Model;

public class View {

	private static int MENU_HEIGHT = 30;
	private App app;
	private Model model;
	private Node menu;
	private Area area;

	private Point temp1 = new Point();
	private Point temp2 = new Point();

	public View(App app, Model model)
	{
		this.app = app;
		this.model = model;

		EventManager event = app.getEventManager();
		AppLogic logic = app.getLogic();

		Dimension s = this.app.getWindow().getScreenSize();
		int w = s.width;
		int h = s.height;


		Node root = app.getRootNode();

		this.menu = new Node("menu");
		this.menu.getTransform().translate(0, 0);
		this.menu.add(g2 -> this.drawMenu(g2));
		root.add(this.menu);

		this.area = new Area("areaFrame", "area");
		this.area.getTransform().translate(0, View.MENU_HEIGHT);
		this.area.setShape(new Rectangle(w, h - View.MENU_HEIGHT));
		root.add(this.area);


		Node area = this.area.getInnerNode();
		area.getTransform().translate(0, 0);
		area.add(g2 -> this.drawArea(g2));


	}

	private void drawMenu(Graphics2D g2)
	{
		Dimension s = this.app.getWindow().getScreenSize();
		int w = s.width;

		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, w, View.MENU_HEIGHT);
	}

	private void drawArea(Graphics2D g2)
	{
		Dimension s = this.app.getWindow().getScreenSize();
		int w = s.width;
		int h = s.height - View.MENU_HEIGHT;

		this.temp1.setLocation(0, 0);
		this.temp2.setLocation(w, h);
		Point p1 = this.temp1;
		Point p2 = this.temp2;


		AffineTransform t = this.area.getInnerNode().getTransform();
		try
		{
			t.inverseTransform(p1, p1);
			t.inverseTransform(p2, p2);
		}
		catch (NoninvertibleTransformException e)
		{
			e.printStackTrace();
		}

		CellData data = this.model.getData();
		int chunkWidth = data.getChunkWidth();
		int cellSize = 16;

		int minX = (int) Math.floor(p1.x / cellSize) - 1;
		int minY = (int) Math.floor(p1.y / cellSize) - 1;
		int maxX = (int) Math.ceil(p2.x / cellSize) + 1;
		int maxY = (int) Math.ceil(p2.y / cellSize) + 1;



		g2.setColor(Color.BLACK);
		g2.fillRect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);


		g2.setColor(Color.GREEN);

		for(int x = minX; x <= maxX; x++)
		{
			for(int y = minY; y <= maxY; y++)
			{
				int chunkX = x / chunkWidth;
				int chunkY = y;

				long chunk = data.get(chunkX, chunkY);

				if(chunk == 0L)
				{
					continue;
				}

				if(chunk == 0xFFFF_FFFF_FFFF_FFFFL)
				{
					int cellX = chunkX * chunkWidth;

					g2.fillRect(
							cellX * cellSize,
							chunkY * cellSize,
							cellSize * chunkWidth,
							cellSize
							);

					continue;
				}

				while(chunk != 0L)
				{
					long bit = chunk & (-chunk);
					int index = Long.bitCount(~(bit - 1) ) - 1;
					int cellX = chunkX * chunkWidth + index;

					g2.fillRect(
							cellX * cellSize,
							chunkY * cellSize,
							cellSize,
							cellSize
							);

					chunk = chunk & ~bit;
				}
			}
		}

		if(cellSize * t.getScaleX() < 2)
		{
			return;
		}

		g2.setColor(Color.DARK_GRAY);

		for(int x = minX; x <= maxX; x++)
		{
			g2.drawLine(
					x * cellSize,
					minY * cellSize,
					x * cellSize,
					maxY * cellSize
					);
		}

		for(int y = minY; y <= maxY; y++)
		{
			g2.drawLine(
					minX * cellSize,
					y * cellSize,
					maxX * cellSize,
					y * cellSize
					);
		}

	}



	public Node getMenu()
	{
		return menu;
	}

	public Area getArea()
	{
		return this.area;
	}


}

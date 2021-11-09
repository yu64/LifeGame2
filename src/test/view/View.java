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
		int h = s.height;

		this.temp1.setLocation(0, 0);
		this.temp2.setLocation(w, h - View.MENU_HEIGHT);
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

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);


		g2.setColor(Color.GREEN);
		CellData data = this.model.getData();
		int chunkWidth = data.getChunkWidth();
		int cellSize = 16;

		int minCellX = (int) Math.floor(p1.x / cellSize);
		int minCellY = (int) Math.floor(p1.y / cellSize);
		int maxCellX = (int) Math.floor(p2.x / cellSize);
		int maxCellY = (int) Math.floor(p2.y / cellSize);

		int minChunkX = (int)Math.floor(minCellX / chunkWidth);
		int minChunkY = minCellY;
		int maxChunkX = (int)Math.ceil(maxCellX / chunkWidth);
		int maxChunkY = maxCellY;


		for(int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
		{
			for(int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++)
			{
				long chunk = data.get(chunkX, chunkY);

				if(chunk == 0L)
				{
					continue;
				}

				if(chunk == 0xFFFF_FFFF_FFFF_FFFFL)
				{
					int cellX = chunkX * chunkWidth;
					g2.fillRect(cellX, chunkY, cellSize * chunkWidth, cellSize);
					continue;
				}

				while(chunk != 0L)
				{
					long bit = chunk & (-chunk);
					int index = Long.bitCount(~(bit - 1) ) - 1;
					int cellX = chunkX * chunkWidth + index;

					g2.fillRect(chunkX + cellX, chunkY, cellSize, cellSize);

					chunk = chunk & ~bit;
				}

			}

		}

		System.out.println("ce " + minCellX + " , " + minCellY);
		System.out.println("ce " + maxCellX + " , " + maxCellY);

		System.out.println(minChunkX + " , " + minChunkY);
		System.out.println(maxChunkX + " , " + maxChunkY);

		g2.setColor(Color.BLUE);
		g2.drawRect(0, 0, w, h);
		g2.drawOval(0, 0, w, h);


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

package test.view;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.event.awt.AwtListener;
import canvas2.logic.AppLogic;
import canvas2.util.GraphicsUtil;
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

	private Point mouse = new Point();

	public View(App app, Model model)
	{
		this.app = app;
		this.model = model;


		AppLogic logic = app.getLogic();

		Dimension s = this.app.getWindow().getScreenSize();
		int w = s.width;
		int h = s.height;


		Node root = app.getRootNode();

		this.menu = new Node("menu");
		this.menu.getTransform().translate(0, 0);
		this.menu.add(this::drawMenu);
		root.add(this.menu);

		this.area = new Area("areaFrame", "area");
		this.area.getTransform().translate(0, View.MENU_HEIGHT);
		this.area.setShape(new Rectangle(w, h - View.MENU_HEIGHT));
		root.add(this.area);


		Node innerArea = this.area.getInnerNode();
		innerArea.getTransform().translate(0, 0);
		innerArea.add(this::drawArea);
		innerArea.add(this::drawCursor);

		EventManager event = app.getEventManager();
		event.add(AwtListener.class, MouseEvent.MOUSE_MOVED, this::changeCursor);
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
		//エリアの幅
		Dimension s = this.app.getWindow().getScreenSize();
		int w = s.width;
		int h = s.height - View.MENU_HEIGHT;

		//エリア内の描画範囲を求める。
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

		//セル単位で、画面の描画範囲を求める。
		CellData data = this.model.getData();
		int chunkWidth = data.getChunkWidth();
		int cellSize = data.getCellSize();

		int minX = (int) Math.floor(p1.x / cellSize) - 1;
		int minY = (int) Math.floor(p1.y / cellSize) - 1;
		int maxX = (int) Math.ceil(p2.x / cellSize) + 1;
		int maxY = (int) Math.ceil(p2.y / cellSize) + 1;

		//背景
		g2.setColor(Color.BLACK);
		g2.fillRect(
				p1.x - 1,
				p1.y - 1,
				p2.x - p1.x + 2,
				p2.y - p1.y + 2
				);

		//セルを描画する。
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

		if(cellSize * t.getScaleX() < 3)
		{
			return;
		}

		g2.setColor(Color.DARK_GRAY);

		GraphicsUtil.drawGrid(
				g2,
				minX * cellSize,
				minY * cellSize,
				(maxX - minX) * cellSize,
				(maxY - minY) * cellSize,
				cellSize
				);

	}

	private void changeCursor(float tpf, AWTEvent awt)
	{
		if( !(awt instanceof AWTEvent) )
		{
			return;
		}

		AffineTransform t1 = this.area.getTransform();
		AffineTransform t2 = this.area.getInnerNode().getTransform();

		MouseEvent e = (MouseEvent) awt;
		this.mouse.x = e.getX();
		this.mouse.y = e.getY();


		try
		{
			t1.inverseTransform(this.mouse, this.mouse);
			t2.inverseTransform(this.mouse, this.mouse);
		}
		catch (NoninvertibleTransformException e1)
		{
			e1.printStackTrace();
		}

	}

	private void drawCursor(Graphics2D g2)
	{
		CellData data = this.model.getData();
		int cellSize = data.getCellSize();

		Point p = this.mouse;

		int minX = (p.x / cellSize) * cellSize;
		int minY = (p.y / cellSize) * cellSize;

		g2.setColor(Color.LIGHT_GRAY);
		g2.drawRect(minX, minY, cellSize, cellSize);


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

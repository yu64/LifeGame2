package life_game2.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import canvas2.App;
import canvas2.util.CastUtil;
import canvas2.util.GraphicsUtil;
import canvas2.util.Pool;
import canvas2.util.TransformUtil;
import canvas2.view.JScreen;
import canvas2.view.scene.Node;
import canvas2.view.scene.Pane;
import life_game2.model.CellData;
import life_game2.model.Model;
import life_game2.swing.Menu;

public class View {

	private static int MENU_HEIGHT = 30;
	private App app;
	private Model model;
	private Menu menu;
	private Pane area;

	private Pool pool = new Pool();


	public View(App app, Model model)
	{
		this.app = app;
		this.model = model;

		this.pool.register(
				Point.class,
				Point::new,
				v -> v.setLocation(0, 0),
				2,
				2
				);

		this.pool.register(
				ArrayList.class,
				ArrayList::new,
				v -> v.clear(),
				1,
				1
				);


		Node root = app.getRootNode();


		this.area = new Pane("areaFrame", "area");
		this.area.getTransform().translate(0, View.MENU_HEIGHT);
		this.area.setShape(null);
		root.add(this.area);

		this.menu = new Menu(app, model, View.MENU_HEIGHT);
		JScreen screen = this.app.getWindow().getScreen();
		screen.add(this.menu, BorderLayout.NORTH);

		Node innerArea = this.area.getInnerNode();
		innerArea.getTransform().translate(0, 0);
		innerArea.add(this::drawArea);
		innerArea.add(this::drawCursor);

	}


	private void drawArea(Graphics2D g2)
	{
		//エリアの幅
		Dimension s = this.app.getWindow().getScreenSize();
		int w = s.width;
		int h = s.height - View.MENU_HEIGHT;


		//エリア内の描画範囲を求める。
		Point p1 = this.pool.obtain(Point.class);
		Point p2 = this.pool.obtain(Point.class);
		p1.setLocation(0, 0);
		p2.setLocation(w, h);

		AffineTransform t = this.area.getInnerNode().getTransform();
		TransformUtil.inverseTransform(t, p1, p1);
		TransformUtil.inverseTransform(t, p2, p2);


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
				int chunkX = Math.floorDiv(x, chunkWidth);
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


	private void drawCursor(Graphics2D g2)
	{
		CellData data = this.model.getData();
		int cellSize = data.getCellSize();

		Point cell = this.model.getAreaCell();

		g2.setColor(Color.LIGHT_GRAY);
		g2.drawRect(
				cell.x * cellSize,
				cell.y * cellSize,
				cellSize,
				cellSize
				);

	}


	public  Point2D inverseTransform(Node node, Point2D in, Point2D out)
	{
		Point2D temp = in;
		synchronized(this.pool)
		{
			ArrayList<Node> list = CastUtil.cast(this.pool.obtain(ArrayList.class));

			Node now = node;
			while(now != null)
			{
				list.add(now);
				now = now.getParent();
			}


			for(int i = list.size() - 1; 0 <= i; i--)
			{
				Node n = list.get(i);
				AffineTransform t = n.getTransform();
				temp = TransformUtil.inverseTransform(t, temp, out);
			}

			this.pool.free(list);

		}


		return temp;
	}

	public Menu getMenu()
	{
		return menu;
	}

	public Pane getArea()
	{
		return this.area;
	}


}

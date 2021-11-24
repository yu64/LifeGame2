package life_game2.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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

	private CellDrawer cellDrawer = new CellDrawer();
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
		innerArea.add(this::drawSelectedCursor);

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

		this.cellDrawer.draw(g2, data, minX, minY, maxX, maxY);

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

	private void drawSelectedCursor(Graphics2D g2)
	{
		boolean isSelected = this.model.isSelected();

		if(!isSelected)
		{
			return;
		}

		CellData data = this.model.getData();
		int cellSize = data.getCellSize();

		Rectangle rect = this.model.getSelectedRect();

		g2.setColor(Color.LIGHT_GRAY);
		g2.drawRect(
				rect.x * cellSize,
				rect.y * cellSize,
				rect.width * cellSize,
				rect.height * cellSize
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

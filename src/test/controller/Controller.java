package test.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.event.awt.AwtListener;
import canvas2.logic.AppLogic;
import canvas2.time.TimeInterval;
import canvas2.util.Direction;
import canvas2.util.TransformUtil;
import canvas2.value.KeyFlags;
import canvas2.view.scene.Area;
import test.model.CellData;
import test.model.CellData.Output;
import test.model.Model;
import test.view.View;

public class Controller {

	private App app;
	private Model model;
	private View view;

	private KeyFlags keys;
	private TimeInterval interval;
	private CellData temp = new CellData();

	public Controller(App app, Model model, View view)
	{
		this.app = app;
		this.model = model;
		this.view = view;

		this.keys = new KeyFlags(
				KeyEvent.VK_W,
				KeyEvent.VK_A,
				KeyEvent.VK_S,
				KeyEvent.VK_D
				);

		this.interval = new TimeInterval(500, this::step);

		Class<AwtListener> awt = AwtListener.class;

		EventManager event = app.getEventManager();
		this.keys.registerTo(event);
		event.add(awt, MouseWheelEvent.MOUSE_WHEEL, this::zoom);
		event.add(awt, MouseEvent.MOUSE_PRESSED, this::changeCell);

		AppLogic logic = app.getLogic();
		logic.add(this::scroll);
		logic.add(this.interval);

	}

	private void scroll(float tpf)
	{
		float speed = 1.0F * tpf;

		float x = 0.0F;
		float y = 0.0F;

		if(this.keys.isPressed(KeyEvent.VK_A))
		{
			x += speed;
		}

		if(this.keys.isPressed(KeyEvent.VK_D))
		{
			x += -speed;
		}

		if(this.keys.isPressed(KeyEvent.VK_W))
		{
			y += speed;
		}

		if(this.keys.isPressed(KeyEvent.VK_S))
		{
			y += -speed;
		}

		Area area = this.view.getArea();
		AffineTransform t = area.getInnerNode().getTransform();
		t.translate(x, y);
	}

	private void zoom(float tpf, AWTEvent e)
	{
		if( !(e instanceof MouseWheelEvent) )
		{
			return;
		}

		MouseWheelEvent wheel = (MouseWheelEvent) e;


		Area area = this.view.getArea();
		AffineTransform t = area.getInnerNode().getTransform();

		int r = wheel.getWheelRotation();
		float rate = 0.75F;

		if(0 < r)
		{
			TransformUtil.scale(t, rate, 0, 0);
		}

		if(r < 0)
		{
			TransformUtil.scale(t, 1 / rate, 0, 0);
		}

	}

	private void changeCell(float tpf, AWTEvent awt)
	{
		if( !(awt instanceof AWTEvent) )
		{
			return;
		}

		MouseEvent e = (MouseEvent) awt;
		Point p = e.getPoint();
		AffineTransform t = this.view.getArea().getInnerNode().getTransform();

		try
		{
			t.inverseTransform(p, p);
		}
		catch (NoninvertibleTransformException e1)
		{
			e1.printStackTrace();
		}

		CellData data = this.model.getData();

		int cellX = p.x / data.getCellSize();
		int cellY = p.y / data.getCellSize();

		System.out.println(cellX);
		System.out.println(cellY);

		if(e.getButton() == MouseEvent.BUTTON1)
		{
			data.setCell(true, cellX, cellY);
		}

		if(e.getButton() == MouseEvent.BUTTON2)
		{
			data.setCell(false, cellX, cellY);
		}


	}


	private void step(float tpf)
	{
		this.temp.clear();
		CellData data = this.model.getData();

		for(Output o : data.iterable())
		{
			long x = o.getX();
			long y = o.getY();

			long nextCenter = this.stepChunk(data, x, y);
			this.temp.set(nextCenter, x, y);

			for(Direction d : Direction.values())
			{
				long x2 = x + d.getX();
				long y2 = y + d.getY();

				if(this.temp.get(x2, y2) == 0)
				{
					long nextAround = this.stepChunk(data, x2, y2);
					this.temp.set(nextAround, x2, y2);
				}
			}
		}

		data.clear();
		for(Output o : this.temp.iterable())
		{
			data.set(o.getValue(), o.getX(), o.getY());
		}



	}

	private long stepChunk(CellData data, long x, long y)
	{
		int w = data.getChunkWidth();
		long chunk = data.get(x, y);

		long up = data.get(x, y, Direction.NORTH);
		long center = chunk;
		long down = data.get(x, y, Direction.SOUTH);

		//左側のセルを得るために、右シフトする。
		long upLeft = (up >>> 1) | (data.get(x, y, Direction.NORTH_EAST) << (w - 1));
		long upRight = (up << 1) | (data.get(x, y, Direction.NORTH_WEST) >>> (w - 1));

		long left = (center >>> 1) | (data.get(x, y, Direction.EAST) << (w - 1));
		long right = (center << 1) | (data.get(x, y, Direction.WEST) >>> (w - 1));

		long downLeft = (down >>> 1) | (data.get(x, y, Direction.SOUTH_EAST) << (w - 1));
		long downRight = (down << 1) | (data.get(x, y, Direction.SOUTH_WEST) >>> (w - 1));

		//同じ位置のビットが周囲の状態と等しいはず。
		long[] bitArray = new long[] {up, down, upLeft, upRight, left, right, downLeft, downRight};


		long mask3 = 0x0000_0000_0000_0000L;
		long mask2 = 0x0000_0000_0000_0000L;
		long mask1 = 0x0000_0000_0000_0000L;
		long mask0 = 0xFFFF_FFFF_FFFF_FFFFL;

		for(long v : bitArray)
		{
			mask3 = (mask3 & ~v) | (mask2 & v);
			mask2 = (mask2 & ~v) | (mask1 & v);
			mask1 = (mask1 & ~v) | (mask0 & v);
			mask0 = (mask0 & ~v);
		}

		return mask3 | (center & mask2);
	}






}








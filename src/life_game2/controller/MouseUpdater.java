package life_game2.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import canvas2.App;
import canvas2.core.Updatable;
import canvas2.core.event.Registerable;
import canvas2.event.EventManager;
import canvas2.event.awt.AwtListener;
import canvas2.view.scene.Node;
import life_game2.model.CellData;
import life_game2.model.Model;
import life_game2.view.View;

public class MouseUpdater implements Registerable, AwtListener, Updatable{


	private App app;
	private Point mouse = new Point();

	private Model model;
	private View view;


	public MouseUpdater(App app, Model model, View view)
	{
		this.app = app;
		this.model = model;
		this.view = view;
	}

	@Override
	public void registerTo(EventManager event)
	{
		event.add(AWTEvent.class, MouseEvent.MOUSE_MOVED, this);
	}

	@Override
	public void unregisterTo(EventManager event)
	{
		event.remove(AWTEvent.class, MouseEvent.MOUSE_MOVED, this);
	}

	@Override
	public void act(float tpf, AWTEvent e) throws Exception
	{
		this.updateMouse(tpf, e);
	}

	@Override
	public void update(float tpf) throws Exception
	{
		this.updateTransformMouse(tpf);
	}



	/**
	 * 移動時にマウスの位置を保存する。
	 */
	public void updateMouse(float tpf, AWTEvent awt)
	{
		if( !(awt instanceof MouseEvent) )
		{
			return;
		}

		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		MouseEvent e = (MouseEvent) awt;
		this.mouse = e.getPoint();

		this.updateTransformMouse(tpf);
	}

	/**
	 * マウスの位置を更新する。
	 */
	private void updateTransformMouse(float tpf)
	{
		Point p1 = this.model.getAreaMousePoint();
		Node innerArea = this.view.getArea().getInnerNode();
		this.view.inverseTransform(innerArea, this.mouse, p1);
		this.model.setAreaMousePoint(p1);

		CellData data = this.model.getData();
		int cellSize = data.getCellSize();

		Point p2 = this.model.getAreaCell();
		if(p2 == null)
		{
			p2 = new Point();
		}

		p2.setLocation(
				Math.floorDiv(p1.x, cellSize),
				Math.floorDiv(p1.y, cellSize)
				);

		this.model.setAreaCell(p2);

		JLabel label = this.view.getMenu().getPosLabel();
		label.setText("座標: x= " + p2.x + ", y= " + p2.y);
	}





}

package life_game2.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JToggleButton;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.logic.AppLogic;
import canvas2.state.StateTable;
import canvas2.state.obj.State;
import canvas2.time.TimeInterval;
import canvas2.util.TransformUtil;
import canvas2.util.flag.Flags;
import canvas2.value.KeyFlags;
import canvas2.view.scene.Area;
import canvas2.view.scene.Node;
import life_game2.model.CellData;
import life_game2.model.Model;
import life_game2.view.View;

public class Controller {

	private App app;
	private Model model;
	private View view;

	private KeyFlags keys;
	private TimeInterval stepInterval;

	private Point mouse = new Point();
	private Boolean clickedCell = null;

	private boolean isSelected = false;
	private Point selectedCellPoint1 = new Point();
	private Point selectedCellPoint2 = new Point();

	private StateTable<Mode> table;

	public Controller(App app, Model model, View view)
	{
		this.app = app;
		this.model = model;
		this.view = view;

		this.keys = new KeyFlags(Set.of(
						KeyEvent.VK_W,
						KeyEvent.VK_A,
						KeyEvent.VK_S,
						KeyEvent.VK_D,
						KeyEvent.VK_SPACE,
						KeyEvent.VK_SHIFT
					));

		this.keys.setListener(this::onChangeSpace);

		this.stepInterval = new TimeInterval(
				this.model.getStepWait(),
				new CellStep(model)
				);


		this.table = new StateTable<Mode>(Mode.NO_ACTION);
		this.table.register(Mode.NO_ACTION, Mode.SET_ALIVE_CELL, () -> false);
		this.table.register(Mode.SET_ALIVE_CELL, Mode.NO_ACTION, () -> false);

		this.table.register(Mode.NO_ACTION, Mode.SET_DEAD_CELL, () -> false);
		this.table.register(Mode.SET_DEAD_CELL, Mode.NO_ACTION, () -> false);

		this.table.register(Mode.NO_ACTION, Mode.START_SELECT, () -> false);
		this.table.register(Mode.START_SELECT, Mode.END_SELECT, () -> false);
		this.table.register(Mode.END_SELECT, Mode.SET_ALIVE_CELL, () -> false);
		this.table.register(Mode.END_SELECT, Mode.SET_DEAD_CELL, () -> false);




		Class<AWTEvent> awt = AWTEvent.class;

		EventManager event = app.getEventManager();
		this.keys.registerTo(event);
		event.add(awt, MouseWheelEvent.MOUSE_WHEEL, this::zoom);
		event.add(awt, MouseEvent.MOUSE_MOVED, this::updateMouse);
		event.add(awt, MouseEvent.MOUSE_DRAGGED, this::dragCell);
		event.add(awt, MouseEvent.MOUSE_PRESSED, this::pressCell);
		event.add(awt, MouseEvent.MOUSE_RELEASED, this::releaseCell);

		AppLogic logic = app.getLogic();
		logic.add(this::scroll);
		logic.add(this::updateLocalMouse);
		logic.add(this.stepInterval);
		logic.add(this::updateValueFromModel);
	}

	/**
	 * キーを押して、フラグが切り替わったときに、呼び出される。<br>
	 * スペースキーを押すと、ポーズ状態が反転するようになっている。
	 */
	private void onChangeSpace(Flags<Integer> src, Integer id, boolean prev, boolean next)
	{
		if(id != KeyEvent.VK_SPACE)
		{
			return;
		}

		if(prev && !next)
		{
			return;
		}

		boolean isPause = this.model.isPause();
		this.model.setPause(!isPause);
		JToggleButton playButton = this.view.getMenu().getPlayButton();
		playButton.setSelected(!isPause);
	}

	/**
	 * モデル上の値からオブジェクトの値を変更する。
	 */
	private void updateValueFromModel(float tpf)
	{
		int wait = this.model.getStepWait();
		this.stepInterval.setIntervalTime(wait);

		boolean isPause = this.model.isPause();

		if(isPause)
		{
			this.stepInterval.enablePause();
		}
		else
		{
			this.stepInterval.disablePause();
		}
	}

	private void scroll(float tpf)
	{
		Area area = this.view.getArea();
		AffineTransform t = area.getInnerNode().getTransform();

		float speed = (float) (1.0F * (1 / t.getScaleX()) * tpf);

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

		if(this.keys.isPressed(KeyEvent.VK_SHIFT))
		{
			x *= 3;
			y *= 3;
		}

		t.translate(x, y);
	}

	private void zoom(float tpf, AWTEvent awt)
	{
		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		MouseWheelEvent wheel = (MouseWheelEvent) awt;

		Point p = this.model.getAreaMousePoint();

		Area area = this.view.getArea();
		AffineTransform t = area.getInnerNode().getTransform();

		int r = wheel.getWheelRotation();
		float rate = 0.75F;

		if(0 < r)
		{
			TransformUtil.scale(t, rate, p.x, p.y);
		}

		if(r < 0)
		{
			TransformUtil.scale(t, 1 / rate, p.x, p.y);
		}

	}

	//移動時にマウスの位置を保存する。
	private void updateMouse(float tpf, AWTEvent awt)
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

		this.updateLocalMouse(tpf);
	}


	private void updateLocalMouse(float tpf)
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

	private void pressCell(float tpf, AWTEvent awt)
	{
		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		MouseEvent e = (MouseEvent) awt;
		this.updateMouse(tpf, awt);


		CellData data = this.model.getData();
		Point p = this.model.getAreaCell();

		if(e.getButton() == MouseEvent.BUTTON1)
		{
			this.clickedCell = data.getCell(p.x, p.y);
			data.setCell(!this.clickedCell, p.x, p.y);
		}


		if(!this.isSelected && e.getButton() == MouseEvent.BUTTON3)
		{
			this.isSelected = true;
			this.selectedCellPoint1.setLocation(p);
		}
	}

	private void releaseCell(float tpf, AWTEvent awt)
	{
		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		MouseEvent e = (MouseEvent) awt;
		this.updateMouse(tpf, awt);

		if(e.getButton() == MouseEvent.BUTTON1)
		{
			this.clickedCell = null;
		}

		if(this.isSelected && e.getButton() == MouseEvent.BUTTON3)
		{
			this.isSelected = false;
			System.out.println(this.selectedCellPoint1 + " " + this.selectedCellPoint2);
		}
	}

	private void dragCell(float tpf, AWTEvent awt)
	{
		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		this.updateMouse(tpf, awt);

		CellData data = this.model.getData();
		Point p = this.model.getAreaCell();

		if(this.clickedCell != null)
		{
			data.setCell(!this.clickedCell, p.x, p.y);
		}

		if(this.isSelected)
		{
			this.selectedCellPoint2.setLocation(p);
		}
	}


	private enum Mode implements State {

		NO_ACTION,
		SET_ALIVE_CELL,
		SET_DEAD_CELL,
		START_SELECT,
		END_SELECT,

	}


}








package life_game2.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Set;

import canvas2.App;
import canvas2.core.Updatable;
import canvas2.core.event.Registerable;
import canvas2.event.EventManager;
import canvas2.event.flag.ButtonFlags;
import canvas2.state.StateTable;
import canvas2.state.obj.State;
import life_game2.model.CellData;
import life_game2.model.Model;
import life_game2.view.View;

public class MouseController implements Registerable, Updatable{


	private App app;
	private Model model;
	private View view;


	private MouseUpdater mouseUpdater;
	private ButtonFlags buttons;
	private StateTable<Mode> table;

	private Rectangle selectedRect = new Rectangle();
	private Point selectedStart = new Point();


	public MouseController(App app, Model model, View view)
	{
		this.app = app;
		this.model = model;
		this.view = view;

		this.mouseUpdater = new MouseUpdater(app, model, view);

		this.buttons = new ButtonFlags(Set.of(
				MouseEvent.BUTTON1,
				MouseEvent.BUTTON3
			));

		this.table = new StateTable<Mode>(Mode.INIT);
		this.table.set(Mode.INIT, Mode.SET_ALIVE, this::isDeadPressedLeft);
		this.table.set(Mode.SET_ALIVE, Mode.INIT, this::releasedLeft);

		this.table.set(Mode.INIT, Mode.SET_DEAD, this::isAlivePressedLeft);
		this.table.set(Mode.SET_DEAD, Mode.INIT, this::releasedLeft);

		this.table.set(Mode.INIT, Mode.START_SELECT, this::pressedRight);
		this.table.set(Mode.START_SELECT, Mode.END_SELECT, this::releasedRight);

		this.table.set(Mode.END_SELECT, Mode.SET_ALIVE, this::isDeadPressedLeft);
		this.table.set(Mode.END_SELECT, Mode.SET_DEAD, this::isAlivePressedLeft);
		this.table.set(Mode.END_SELECT, Mode.START_SELECT, this::pressedRight);

		this.table.setChange(Mode.INIT, Mode.START_SELECT, this::startSelected);
		this.table.setChange(Mode.END_SELECT, Mode.START_SELECT, this::startSelected);

		this.buttons.setListener((s, id, p, n) -> this.table.tryMoveState());

	}

	public boolean isSelected()
	{
		return this.table.getState() == Mode.END_SELECT
				|| this.table.getState() == Mode.START_SELECT;
	}

	public Rectangle getSelectedRect()
	{
		return this.selectedRect;
	}

	@Override
	public void registerTo(EventManager event)
	{
		this.buttons.registerTo(event);
		this.mouseUpdater.registerTo(event);
		event.add(AWTEvent.class, MouseEvent.MOUSE_PRESSED, this::doMouseAction);
		event.add(AWTEvent.class, MouseEvent.MOUSE_RELEASED, this::doMouseAction);
		event.add(AWTEvent.class, MouseEvent.MOUSE_DRAGGED, this::doMouseAction);
	}

	@Override
	public void unregisterTo(EventManager event)
	{
		this.buttons.unregisterTo(event);
		this.mouseUpdater.unregisterTo(event);
		event.remove(AWTEvent.class, MouseEvent.MOUSE_PRESSED, this::doMouseAction);
		event.remove(AWTEvent.class, MouseEvent.MOUSE_RELEASED, this::doMouseAction);
		event.remove(AWTEvent.class, MouseEvent.MOUSE_DRAGGED, this::doMouseAction);
	}

	private void startSelected(StateTable<Mode> src, Mode now, Mode prev)
	{
		Point p = this.model.getAreaCell();
		this.selectedStart.setLocation(p);
	}

	private boolean pressedLeft()
	{
		return this.buttons.isPressed(MouseEvent.BUTTON1);
	}

	private boolean releasedLeft()
	{
		return !this.pressedLeft();
	}

	private boolean pressedRight()
	{
		return this.buttons.isPressed(MouseEvent.BUTTON3);
	}

	private boolean releasedRight()
	{
		return !this.pressedRight();
	}

	private boolean isAliveCell()
	{
		CellData data = this.model.getData();
		Point p = this.model.getAreaCell();

		return data.getCell(p.x, p.y);
	}

	private boolean isAlivePressedLeft()
	{
		return this.isAliveCell() && this.pressedLeft();
	}

	private boolean isDeadPressedLeft()
	{
		return !this.isAliveCell() && this.pressedLeft();
	}

	@Override
	public void update(float tpf) throws Exception
	{
		this.mouseUpdater.update(tpf);
	}

	/**
	 * マウス操作中に実行される処理。
	 */
	private void doMouseAction(float tpf, AWTEvent awt)
	{
		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		this.mouseUpdater.updateMouse(tpf, awt);

		CellData data = this.model.getData();
		Point p = this.model.getAreaCell();

		Mode state = this.table.getState();

		if(state == Mode.SET_DEAD)
		{
			data.setCell(false, p.x, p.y);
		}

		if(state == Mode.SET_ALIVE)
		{
			data.setCell(true, p.x, p.y);
		}

		if(state == Mode.START_SELECT)
		{

			this.selectedRect.setFrameFromDiagonal(
					this.selectedStart,
					p
					);

			int w = this.selectedRect.width + 1;
			int h = this.selectedRect.height + 1;
			this.selectedRect.setSize(w, h);

		}

	}

	private enum Mode implements State {

		INIT,
		SET_ALIVE,
		SET_DEAD,
		START_SELECT,
		END_SELECT,

	}





}

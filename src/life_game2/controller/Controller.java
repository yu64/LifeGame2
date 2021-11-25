package life_game2.controller;

import java.awt.Rectangle;

import javax.swing.JLabel;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.logic.AppLogic;
import canvas2.time.TimeInterval;
import life_game2.model.Model;
import life_game2.view.View;

public class Controller {

	private App app;
	private Model model;
	private View view;

	private KeyController keys;
	private MouseController mouse;

	private TimeInterval stepInterval;


	public Controller(App app, Model model, View view)
	{
		this.app = app;
		this.model = model;
		this.view = view;

		this.keys = new KeyController(app, model, view);
		this.mouse = new MouseController(app, model, view);


		this.stepInterval = new TimeInterval(
				this.model.getStepWait(),
				new CellStep(model)
				);


		EventManager event = app.getEventManager();
		this.keys.registerTo(event);
		this.mouse.registerTo(event);

		AppLogic logic = app.getLogic();
		logic.add(this.keys);
		logic.add(this.mouse);
		logic.add(this.stepInterval);
		logic.add(this::syncValue);

	}





	/**
	 * オブジェクトの値を同期する。
	 */
	private void syncValue(float tpf)
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


		Rectangle rect = this.mouse.getSelectedRect();
		this.model.setSelectedRect(rect);

		boolean isSelected = this.mouse.isSelected();
		this.model.setSelected(isSelected);



		float fps = this.app.getLogic().getFps();
		JLabel label = this.view.getMenu().getFpsLabel();

		label.setText(String.format("%.2f", fps));
	}









}








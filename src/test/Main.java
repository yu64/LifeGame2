package test;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.logic.AppLogic;
import canvas2.view.AppWindow;
import canvas2.view.scene.Node;
import test.controller.Controller;
import test.model.CellData;
import test.model.Model;
import test.view.View;

public class Main {

	public static void main(String[] args)
	{
		App app = new App();


		AppWindow win = app.getWindow();
		win.setLocationRelativeTo(null);

		EventManager event = app.getEventManager();
		AppLogic logic = app.getLogic();
		Node root = app.getRootNode();


		Model model = new Model(app);
		View view = new View(app, model);
		Controller con = new Controller(app, model, view);

		CellData data = model.getData();
		data.set(0x5555555555555555L, 0, 0);
		data.set(0xFFFFFFFFFFFFFFFFL, 0, 5);



		app.start();
	}
}

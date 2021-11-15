package test;

import java.awt.event.WindowEvent;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.event.awt.AwtListener;
import canvas2.view.AppWindow;
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
		event.add(AwtListener.class, WindowEvent.WINDOW_CLOSING, (t, v) -> {

			app.close();
		});


		Model model = new Model(app);
		View view = new View(app, model);
		Controller con = new Controller(app, model, view);

		CellData data = model.getData();
		data.set(0x7000_0000_0000_0000L, 0, 1);



		app.start();
	}




}

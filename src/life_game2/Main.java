package life_game2;

import java.awt.AWTEvent;
import java.awt.event.WindowEvent;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.view.AppWindow;
import life_game2.controller.Controller;
import life_game2.model.CellData;
import life_game2.model.Model;
import life_game2.view.View;

public class Main {

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{

		//App app = new App();

		App app = new App();


		AppWindow win = app.getWindow();
		win.setLocationRelativeTo(null);
		win.setTitle("LifeGame Bitboard (ライフゲーム)");

		EventManager event = app.getEventManager();
		event.add(AWTEvent.class, WindowEvent.WINDOW_CLOSING, (t, v) -> {

			app.close();
		});


		Model model = new Model(app);
		View view = new View(app, model);
		Controller con = new Controller(app, model, view);

		CellData data = model.getData();
		data.add(0x7000_0000_0000_0000L, 0, 1);

		Main.setNebula(data, 0);

		app.start();

	}

	private static void setNebula(CellData data, int y)
	{
		data.add(0b110111111L, 0, y);
		data.add(0b110111111L, 0, y + 1);
		data.add(0b110000000L, 0, y + 2);
		data.add(0b110000011L, 0, y + 3);
		data.add(0b110000011L, 0, y + 4);
		data.add(0b110000011L, 0, y + 5);
		data.add(0b000000011L, 0, y + 6);
		data.add(0b111111011L, 0, y + 7);
		data.add(0b111111011L, 0, y + 8);
	}

	private static void setMax(CellData data, int x, int y)
	{
		String s = "";
		s += "...........................\n"
				+ "..................*........\n"
				+ ".................***.......\n"
				+ "............***....**......\n"
				+ "...........*..***..*.**....\n"
				+ "..........*...*.*..*.*.....\n"
				+ "..........*....*.*.*.*.**..\n"
				+ "............*....*.*...**..\n"
				+ "****.....*.*....*...*.***..\n"
				+ "*...**.*.***.**.........**.\n"
				+ "*.....**.....*.............\n"
				+ ".*..**.*..*..*.**..........\n"
				+ ".......*.*.*.*.*.*.....****\n"
				+ ".*..**.*..*..*..**.*.**...*\n"
				+ "*.....**...*.*.*...**.....*\n"
				+ "*...**.*.**..*..*..*.**..*.\n"
				+ "****.....*.*.*.*.*.*.......\n"
				+ "..........**.*..*..*.**..*.\n"
				+ ".............*.....**.....*\n"
				+ ".**.........**.***.*.**...*\n"
				+ "..***.*...*....*.*.....****\n"
				+ "..**...*.*....*............\n"
				+ "..**.*.*.*.*....*..........\n"
				+ ".....*.*..*.*...*..........\n"
				+ "....**.*..***..*...........\n"
				+ "......**....***............\n"
				+ ".......***.................\n"
				+ "........*..................\n"
				+ "...........................";

		data.setFromRect(s, x, y, true);
	}


}

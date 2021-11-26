package life_game2.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Set;

import javax.swing.JToggleButton;

import canvas2.App;
import canvas2.core.Updatable;
import canvas2.event.EventManager;
import canvas2.event.Registerable;
import canvas2.event.awt.AwtListener;
import canvas2.event.flag.KeyFlags;
import canvas2.util.TransformUtil;
import canvas2.util.flag.Flags;
import canvas2.util.flag.Flags.ChangeListener;
import canvas2.view.scene.Pane;
import life_game2.model.CellData;
import life_game2.model.Model;
import life_game2.view.View;

public class KeyController implements Registerable, ChangeListener<Integer>, AwtListener, Updatable{


	private App app;
	private Model model;
	private View view;

	private KeyFlags keys;

	public KeyController(App app, Model model, View view)
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

		this.keys.setChangeListener(this);


	}

	@Override
	public void registerTo(EventManager event)
	{
		this.keys.registerTo(event);
		event.add(AWTEvent.class, MouseWheelEvent.MOUSE_WHEEL, this);
		event.add(AWTEvent.class, KeyEvent.KEY_PRESSED, this);
	}


	@Override
	public void unregisterTo(EventManager event)
	{
		this.keys.unregisterTo(event);
		event.remove(AWTEvent.class, MouseWheelEvent.MOUSE_WHEEL, this);
		event.remove(AWTEvent.class, KeyEvent.KEY_PRESSED, this);
	}

	@Override
	public void act(float tpf, AWTEvent e) throws Exception
	{
		this.zoom(tpf, e);
		this.copy(tpf, e);
		this.paste(tpf, e);
		this.fill(tpf, e);
	}

	@Override
	public void update(float tpf) throws Exception
	{
		this.scroll(tpf);

	}

	@Override
	public void onChange(Flags<Integer> src, Integer id, boolean prev, boolean next, boolean isBefore)
	{
		if(isBefore)
		{
			return;
		}

		this.onChangeSpaceKey(src, id, prev, next);
	}

	/**
	 * キーを押して、フラグが切り替わったときに、呼び出される。<br>
	 * スペースキーを押すと、ポーズ状態が反転するようになっている。
	 */
	private void onChangeSpaceKey(Flags<Integer> src, Integer id, boolean prev, boolean next)
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

	private void fill(float tpf, AWTEvent awt)
	{

		if( !(awt instanceof KeyEvent))
		{
			return;
		}

		KeyEvent e = (KeyEvent) awt;

		if(!e.isControlDown() || !(e.getKeyCode() == KeyEvent.VK_F))
		{
			return;
		}

		boolean isSelected = this.model.isSelected();
		if(!isSelected)
		{
			return;
		}

		Rectangle rect = this.model.getSelectedRect();
		CellData data = this.model.getData();
		Point p = this.model.getAreaCell();

		int w = rect.width;
		int h = rect.height;

		boolean b = data.getCell(p.x, p.y);

		String text = data.createFillRect(w, h, b);
		data.setFromRect(text, rect.x, rect.y, true);
	}


	/**
	 * 選択範囲をコピーする。
	 */
	private void copy(float tpf, AWTEvent awt)
	{
		if( !(awt instanceof KeyEvent))
		{
			return;
		}

		KeyEvent e = (KeyEvent) awt;

		if(!e.isControlDown() || !(e.getKeyCode() == KeyEvent.VK_C))
		{
			return;
		}


		boolean isSelected = this.model.isSelected();
		if(!isSelected)
		{
			return;
		}

		Rectangle rect = this.model.getSelectedRect();

		CellData data = this.model.getData();
		String text = data.getFromRect(rect.x, rect.y, rect.width, rect.height);

		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();

		StringSelection ss = new StringSelection(text);
		clip.setContents(ss, ss);

	}

	private void paste(float tpf, AWTEvent awt)
	{
		if( !(awt instanceof KeyEvent))
		{
			return;
		}

		KeyEvent e = (KeyEvent) awt;

		if(!e.isControlDown() || !(e.getKeyCode() == KeyEvent.VK_V))
		{
			return;
		}

		CellData data = this.model.getData();
		Point p = this.model.getAreaCell();

		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();

		try
		{
			String text = (String)clip.getData(DataFlavor.stringFlavor);
			if(!text.matches(data.getRectPattern()))
			{
				throw new RuntimeException("No cell data");
			}

			boolean mode = this.model.isOverwritePaste();
			data.setFromRect(text, p.x, p.y, mode);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

	}

	/**
	 * スクロール処理
	 */
	private void scroll(float tpf)
	{
		Pane area = this.view.getArea();
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

	/**
	 * ズーム処理
	 */
	private void zoom(float tpf, AWTEvent awt)
	{
		if(awt.getSource() != this.app.getWindow().getScreen())
		{
			return;
		}

		if( !(awt instanceof MouseWheelEvent))
		{
			return;
		}

		MouseWheelEvent wheel = (MouseWheelEvent) awt;

		Point p = this.model.getAreaMousePoint();

		Pane area = this.view.getArea();
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






}

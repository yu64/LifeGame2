package life_game2.controller;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Set;

import javax.swing.JToggleButton;

import canvas2.App;
import canvas2.core.Updatable;
import canvas2.core.event.Registerable;
import canvas2.event.EventManager;
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
				KeyEvent.VK_SHIFT,
				KeyEvent.VK_C,
				KeyEvent.VK_V,
				KeyEvent.VK_CONTROL
			));

		this.keys.setListener(this);


	}

	@Override
	public void registerTo(EventManager event)
	{
		this.keys.registerTo(event);
		event.add(AWTEvent.class, MouseWheelEvent.MOUSE_WHEEL, this);
	}


	@Override
	public void unregisterTo(EventManager event)
	{
		this.keys.unregisterTo(event);
		event.remove(AWTEvent.class, MouseWheelEvent.MOUSE_WHEEL, this);
	}

	@Override
	public void act(float tpf, AWTEvent e) throws Exception
	{
		this.zoom(tpf, e);
	}

	@Override
	public void update(float tpf) throws Exception
	{
		this.scroll(tpf);
		this.copy(tpf);
	}

	@Override
	public void onChange(Flags<Integer> src, Integer id, boolean prev, boolean next)
	{
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

	/**
	 * 選択範囲をコピーする。
	 */
	private void copy(float tpf)
	{
		boolean isSelected = this.model.isSelected();
		if(!isSelected)
		{
			return;
		}

		CellData data = this.model.getData();

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

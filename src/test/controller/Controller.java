package test.controller;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

import canvas2.App;
import canvas2.event.EventManager;
import canvas2.event.awt.AwtListener;
import canvas2.logic.AppLogic;
import canvas2.util.TransformUtil;
import canvas2.value.KeyFlags;
import canvas2.view.scene.Area;
import test.model.Model;
import test.view.View;

public class Controller {

	private App app;
	private Model model;
	private View view;

	private KeyFlags keys;

	public Controller(App app, Model model, View view)
	{
		this.app = app;
		this.model = model;
		this.view = view;

		this.keys = new KeyFlags(
				KeyEvent.VK_W,
				KeyEvent.VK_A,
				KeyEvent.VK_S,
				KeyEvent.VK_D,
				KeyEvent.VK_UP,
				KeyEvent.VK_DOWN

				);

		EventManager event = app.getEventManager();
		this.keys.registerTo(event);
		event.add(KeyEvent.KEY_RELEASED, (AwtListener)this::zoom);

		AppLogic logic = app.getLogic();
		logic.add(this::scroll);

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
		if( !(e instanceof KeyEvent) )
		{
			return;
		}

		KeyEvent keys = (KeyEvent) e;


		Area area = this.view.getArea();
		AffineTransform t = area.getInnerNode().getTransform();

		float rate = 0.5F;

		if(keys.getKeyCode() == KeyEvent.VK_UP)
		{
			TransformUtil.scale(t, rate, 0, 0);
		}

		if(keys.getKeyCode() == KeyEvent.VK_DOWN)
		{
			TransformUtil.scale(t, 1 / rate, 0, 0);
		}



	}











}








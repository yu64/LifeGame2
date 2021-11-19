package life_game2.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

import canvas2.App;
import canvas2.view.JScreen;
import life_game2.model.Model;

public class Menu extends JScreen{

	private JSpinner stepWaitSpinner;
	private JToggleButton playButton;
	private JButton clearButton;
	private JLabel posLabel;

	public Menu(App app, Model model, int height)
	{
		super(g2 -> {});
		this.setDrawable(this::paint);


		Dimension s = app.getWindow().getScreenSize();
		int w = s.width;

		this.setBounds(0, 0, w, height);
		this.setPreferredSize(new Dimension(w, height));
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		JLabel label = new JLabel("遷移間隔(ms)");
		this.add(label);

		this.stepWaitSpinner = this.createStepWaitSpinner(app, model, height);
		this.add(this.stepWaitSpinner);

		this.playButton = this.createPlayButton(app, model, height);
		this.add(this.playButton);

		this.clearButton = this.createClearButton(app, model, height);
		this.add(this.clearButton);

		this.add(Box.createGlue());

		this.posLabel = this.createPosLabel();
		this.add(this.posLabel);

	}


	/**
	 * 遷移間隔を決めるスピナーを作成。
	 */
	private JSpinner createStepWaitSpinner(App app, Model model, int height)
	{

		Dimension s = app.getWindow().getScreenSize();
		int w = s.width;

		SpinnerNumberModel stepWaitModel = new SpinnerNumberModel(
				model.getStepWait(),
				1,
				1000 * 60,
				10
				);

		JSpinner spinner = new JSpinner(stepWaitModel);
		spinner.setMaximumSize(new Dimension(w, height));
		spinner.setPreferredSize(new Dimension(40, height));

		spinner.addChangeListener(e -> {

			model.setStepWait((Integer)spinner.getValue());
		});

		JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
		AbstractFormatter f = editor.getTextField().getFormatter();
		if (f instanceof DefaultFormatter)
		{
		  ((DefaultFormatter) f).setAllowsInvalid(false);
		}


		return spinner;
	}


	private JToggleButton createPlayButton(App app, Model model, int height)
	{
		Dimension s = app.getWindow().getScreenSize();
		int w = s.width;

		JToggleButton playButton = new JToggleButton("停止", false);
		playButton.setMaximumSize(new Dimension(w, height));
		playButton.setBorderPainted(false);
		playButton.setFocusable(false);
		playButton.addChangeListener(e -> {

			model.setPause(playButton.isSelected());
		});


		return playButton;
	}

	private JButton createClearButton(App app, Model model, int height)
	{
		Dimension s = app.getWindow().getScreenSize();
		int w = s.width;

		JButton clearButton = new JButton("全削除");
		clearButton.setMaximumSize(new Dimension(w, height));
		clearButton.setBorderPainted(false);
		clearButton.setFocusable(false);
		clearButton.addActionListener(e -> {

			model.getData().clear();
		});

		return clearButton;
	}

	private JLabel createPosLabel()
	{
		JLabel label = new JLabel();
		return label;
	}


	private void paint(Graphics2D g2)
	{
		Dimension s = this.getSize();
		g2.setBackground(Color.LIGHT_GRAY);
		g2.clearRect(0, 0, s.width, s.height);
	}


	public JSpinner getStepWaitSpinner()
	{
		return this.stepWaitSpinner;
	}

	public JToggleButton getPlayButton()
	{
		return playButton;
	}

	public JButton getClearButton()
	{
		return this.clearButton;
	}

	public JLabel getPosLabel()
	{
		return posLabel;
	}

}

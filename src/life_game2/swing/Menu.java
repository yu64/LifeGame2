package life_game2.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private JCheckBox pasteModeBox;
	private JLabel selectedLabel;
	private JLabel posLabel;
	private JLabel fpsLabel;

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

		this.pasteModeBox = this.createPasteMode(app, model, height);
		this.add(this.pasteModeBox);

		this.add(Box.createGlue());

		this.add(this.createLabel(app, model, height, " 選択: "));
		this.selectedLabel = this.createSelectedLabel(app, model, height);
		this.add(this.selectedLabel);

		this.add(this.createLabel(app, model, height, " 座標: "));
		this.posLabel = this.createPosLabel(app, model, height);
		this.add(this.posLabel);

		this.add(this.createLabel(app, model, height, " FPS: "));
		this.fpsLabel = this.createFPSLabel(app, model, height);
		this.add(this.fpsLabel);

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

	private JCheckBox createPasteMode(App app, Model model, int height)
	{
		Dimension s = app.getWindow().getScreenSize();
		int w = s.width;

		JCheckBox modeBox = new JCheckBox("上書きペースト");
		modeBox.setMaximumSize(new Dimension(w, height));
		modeBox.setFocusable(false);
		modeBox.addChangeListener(e -> {

			model.setOverwritePaste(modeBox.isSelected());
		});

		return modeBox;
	}

	private JLabel createLabel(App app, Model model, int height, String text)
	{
		Dimension s = app.getWindow().getScreenSize();
		int w = s.width;

		JLabel label = new JLabel(text);
		label.setMaximumSize(new Dimension(w, height));
		return label;
	}

	private JLabel createSelectedLabel(App app, Model model, int height)
	{
		JLabel label = this.createLabel(app, model, height, "");
		return label;
	}

	private JLabel createPosLabel(App app, Model model, int height)
	{
		JLabel label = this.createLabel(app, model, height, "");
		return label;
	}

	private JLabel createFPSLabel(App app, Model model, int height)
	{
		JLabel label = this.createLabel(app, model, height, "");
		label.setPreferredSize(new Dimension(100, height));
		label.setHorizontalAlignment(JLabel.RIGHT);
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

	public JCheckBox getPasteModeBox()
	{
		return pasteModeBox;
	}

	public JLabel getSelectedLabel()
	{
		return selectedLabel;
	}

	public JLabel getPosLabel()
	{
		return posLabel;
	}


	public JLabel getFpsLabel()
	{
		return fpsLabel;
	}




}

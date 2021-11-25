package life_game2.model;

import java.awt.Point;
import java.awt.Rectangle;

import canvas2.App;

public class Model {


	private CellData data = new CellData();
	private Point areaMousePoint = new Point();
	private Point areaCell = new Point();
	private int stepWait = 500;
	private boolean isPause = false;

	private Rectangle selectedRect;
	private boolean isSelected = false;

	private boolean isOverwritePaste = false;





	public Model(App app)
	{

	}


	public CellData getData()
	{
		return this.data;
	}

	public Point getAreaMousePoint()
	{
		return areaMousePoint;
	}

	public void setAreaMousePoint(Point areaMousePoint)
	{
		this.areaMousePoint = areaMousePoint;
	}

	public Point getAreaCell()
	{
		return areaCell;
	}

	public void setAreaCell(Point areaCell)
	{
		this.areaCell = areaCell;
	}

	public int getStepWait()
	{
		return stepWait;
	}

	public void setStepWait(int stepWait)
	{
		this.stepWait = stepWait;
	}

	public boolean isPause()
	{
		return isPause;
	}

	public void setPause(boolean isPause)
	{
		this.isPause = isPause;
	}


	public Rectangle getSelectedRect()
	{
		return selectedRect;
	}


	public void setSelectedRect(Rectangle selectedRect)
	{
		this.selectedRect = selectedRect;
	}

	public boolean isSelected()
	{
		return isSelected;
	}

	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}


	public boolean isOverwritePaste()
	{
		return isOverwritePaste;
	}

	public void setOverwritePaste(boolean isOverwritePaste)
	{
		this.isOverwritePaste = isOverwritePaste;
	}




}

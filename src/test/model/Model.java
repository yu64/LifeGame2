package test.model;

import java.awt.Point;

import canvas2.App;

public class Model {

	private CellData data = new CellData();
	private Point areaMousePoint = new Point();
	private Point areaCell = new Point();


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



}

package life_game2.controller;

import canvas2.core.Updatable;
import canvas2.util.Direction;
import life_game2.model.CellData;
import life_game2.model.CellData.Output;
import life_game2.model.Model;

public class CellStep implements Updatable{

	private CellData temp = new CellData();
	private Model model;

	public CellStep(Model model)
	{
		this.model = model;
	}

	@Override
	public void update(float tpf) throws Exception
	{
		this.step(tpf);
	}

	//すべてのセルを遷移させる。
	protected void step(float tpf)
	{

		this.temp.clear();
		CellData data = this.model.getData();

		for(Output o : data.iterable())
		{
			int x = o.getX();
			int y = o.getY();

			long nextCenter = this.stepChunk(data, x, y);
			this.temp.set(nextCenter, x, y);

			for(Direction d : Direction.values())
			{
				int x2 = x + d.getX();
				int y2 = y + d.getY();

				if(this.temp.get(x2, y2) == 0)
				{
					long nextAround = this.stepChunk(data, x2, y2);
					this.temp.set(nextAround, x2, y2);
				}
			}
		}

		synchronized (data)
		{
			data.clear();
			for(Output o : this.temp.iterable())
			{
				data.set(o.getValue(), o.getX(), o.getY());
			}
		}
	}

	//セルをチャンク単位で遷移させる。
	protected long stepChunk(CellData data, int x, int y)
	{
		int w = data.getChunkWidth();
		long chunk = data.get(x, y);

		long up = data.get(x, y, Direction.NORTH);
		long center = chunk;
		long down = data.get(x, y, Direction.SOUTH);

		//左側のセルを得るために、右シフトする。
		long upLeft = (up >>> 1) | (data.get(x, y, Direction.NORTH_EAST) << (w - 1));
		long upRight = (up << 1) | (data.get(x, y, Direction.NORTH_WEST) >>> (w - 1));

		long left = (center >>> 1) | (data.get(x, y, Direction.EAST) << (w - 1));
		long right = (center << 1) | (data.get(x, y, Direction.WEST) >>> (w - 1));

		long downLeft = (down >>> 1) | (data.get(x, y, Direction.SOUTH_EAST) << (w - 1));
		long downRight = (down << 1) | (data.get(x, y, Direction.SOUTH_WEST) >>> (w - 1));

		//同じ位置のビットが周囲の状態と等しいはず。
		long[] bitArray = new long[] {up, down, upLeft, upRight, left, right, downLeft, downRight};


		long mask3 = 0x0000_0000_0000_0000L;
		long mask2 = 0x0000_0000_0000_0000L;
		long mask1 = 0x0000_0000_0000_0000L;
		long mask0 = 0xFFFF_FFFF_FFFF_FFFFL;

		for(long v : bitArray)
		{
			mask3 = (mask3 & ~v) | (mask2 & v);
			mask2 = (mask2 & ~v) | (mask1 & v);
			mask1 = (mask1 & ~v) | (mask0 & v);
			mask0 = (mask0 & ~v);
		}

		return mask3 | (center & mask2);
	}



}

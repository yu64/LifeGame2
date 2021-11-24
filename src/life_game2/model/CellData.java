package life_game2.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import canvas2.util.Direction;

public class CellData {

	private Map<String, Long> data = new HashMap<>();




	public int getChunkWidth()
	{
		return 64;
	}

	public int getCellSize()
	{
		return 16;
	}

	public void clear()
	{
		this.data.clear();
	}

	private long getChunkX(long cellX)
	{
		return Math.floorDiv(cellX, this.getChunkWidth());
	}

	private long getRemainingCellX(long chunkX, long cellX)
	{
		return cellX - (chunkX * this.getChunkWidth());
	}

	public void setCell(boolean b, long cellX, long cellY)
	{
		long chunkX = this.getChunkX(cellX);
		cellX = this.getRemainingCellX(chunkX, cellX);

		this.setCell(b, chunkX, cellY, cellX);
	}

	public void setCell(boolean b, long x, long y, long cellX)
	{

		long chunk = this.get(x, y);
		long mask = 0x8000_0000_0000_0000L >>> cellX;

		if(b)
		{
			chunk = chunk | mask;
		}
		else
		{
			chunk = chunk & ~mask;
		}

		this.set(chunk, x, y);
	}

	public boolean getCell(long cellX, long cellY)
	{
		long chunkX = this.getChunkX(cellX);
		cellX = this.getRemainingCellX(chunkX, cellX);

		return this.getCell(chunkX, cellY, cellX);
	}

	public boolean getCell(long x, long y, long cellX)
	{
		long chunk = this.get(x, y);
		long mask = 0x8000_0000_0000_0000L >>> cellX;

		if((chunk & mask) == mask)
		{
			return true;
		}

		return false;
	}


	public long get(long x, long y, Direction d)
	{
		return this.get(x + d.getX(), y + d.getY());
	}

	public long get(long x, long y)
	{
		long min = Integer.MIN_VALUE;
		long max = Integer.MAX_VALUE;

		if(min >= x || x >= max)
		{
			return 0xFFFFFFFFFFFFFFFFL;
		}

		if(min >= y || y >= max)
		{
			return 0xFFFFFFFFFFFFFFFFL;
		}


		Long out = this.data.get(x + ":" + y);
		if(out == null)
		{
			out = 0L;
		}

		return out;
	}

	public void set(long chunk, long x, long y, Direction d)
	{
		this.set(chunk, x + d.getX(), y + d.getY());
	}

	public void set(long chunk, long x, long y)
	{
		long min = Integer.MIN_VALUE;
		long max = Integer.MAX_VALUE;
		String key = x + ":" + y;

		if(min >= x || x >= max)
		{
			this.data.remove(key);
			return;
		}

		if(min >= y || y >= max)
		{
			this.data.remove(key);
			return;
		}

		if(chunk == 0L)
		{
			this.data.remove(key);
			return;
		}

		this.data.put(key, chunk);
	}

	public void add(long chunk, long x, long y)
	{
		this.set(this.get(x, y) | chunk, x, y);
	}



	public String[] getFromRect(long cellX, long cellY, long width, long height)
	{
		int chunkWidth = this.getChunkWidth();

		long chunkMinX = this.getChunkX(cellX);
		long cellMinX = this.getRemainingCellX(chunkMinX, cellX);

		long chunkMaxX = this.getChunkX(cellX + width);
		long cellMaxX = this.getRemainingCellX(chunkMaxX, cellX + width);


		for(long chunkY = cellY; chunkY <= cellY + height; chunkY++)
		{
			String s = "";

			//左端
			for(long x = chunkWidth - cellMinX; x <= chunkWidth; x++)
			{
				boolean b = this.getCell(chunkMinX, chunkY, x);
				s += (b ? "1" : "0");
			}

			for(long chunkX = chunkMinX + 1; chunkX < chunkMaxX; chunkX++)
			{
				String chunkText = Long.toBinaryString(this.get(chunkX, chunkY));
				int zeroLen = chunkWidth - chunkText.length();
				chunkText = "0".repeat(zeroLen) + chunkText;

				s += chunkText;
			}

			//右端
			for(long x = 0; x <= cellMaxX; x++)
			{
				boolean b = this.getCell(chunkMaxX, chunkY, x);
				s += (b ? "1" : "0");
			}
		}



	}


	public void setAll(String data, long x, long y)
	{

	}





	public Iterator<Output> iterator()
	{
		return new CellIterator();
	}

	public Iterable<Output> iterable()
	{
		return this::iterator;
	}


	public class CellIterator implements Iterator<Output>
	{
		private Output output = new Output();
		private Iterator<Entry<String, Long>> ite;

		private CellIterator()
		{
			this.ite = CellData.this.data.entrySet().iterator();
		}

		@Override
		public boolean hasNext()
		{
			return this.ite.hasNext();
		}

		@Override
		public Output next()
		{
			Entry<String, Long> e = this.ite.next();
			if(e == null)
			{
				return null;
			}

			String s = e.getKey();
			int index = s.indexOf(":");

			long x = Long.parseLong(s.substring(0, index));
			long y = Long.parseLong(s.substring(index + 1));

			this.output.x = x;
			this.output.y = y;
			this.output.chunk = e.getValue();

			return this.output;
		}

	}

	/**
	 * チャンクとその座標を示すクラス<br>
	 * 参照が使いまわされているため、ほかの場所に保存してはならない。
	 *
	 */
	public class Output {

		private long x = 0;
		private long y = 0;
		private long chunk = 0;

		private Output()
		{

		}

		public long getX()
		{
			return x;
		}

		public long getY()
		{
			return y;
		}

		public long getValue()
		{
			return chunk;
		}

	}




}

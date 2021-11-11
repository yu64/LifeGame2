package test.model;

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

	public void setCell(boolean b, long cellX, long cellY)
	{
		long chunkX = cellX / this.getChunkWidth();
		cellX = cellX - (chunkX * this.getChunkWidth());

		this.setCell(b, chunkX, cellY, cellX);
	}

	public void setCell(boolean b, long x, long y, long cellX)
	{

		long chunk = this.get(x, y);
		long mask = 1 << cellX;

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


	public long get(long x, long y, Direction d)
	{
		return this.get(x + d.getX(), y + d.getY());
	}

	public long get(long x, long y)
	{
		long min = Long.MIN_VALUE;
		long max = Long.MAX_VALUE;

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
		long min = Long.MIN_VALUE;
		long max = Long.MAX_VALUE;
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

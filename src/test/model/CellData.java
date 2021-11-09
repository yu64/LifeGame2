package test.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CellData{

	private Map<String, Long> data = new HashMap<>();

	public int getChunkWidth()
	{
		return 64;
	}

	public void setCell(boolean b, long x, long y, long cellY)
	{

		long chunk = this.get(x, y);
		long mask = 1 << cellY;

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

	public void set(long chunk, long x, long y)
	{
		long min = Long.MIN_VALUE;
		long max = Long.MAX_VALUE;

		if(min >= x || x >= max)
		{
			return;
		}

		if(min >= y || y >= max)
		{
			return;
		}

		if(chunk == 0L)
		{
			return;
		}

		this.data.put(x + ":" + y, chunk);
	}



	public static class CellIterator implements Iterator<Integer>
	{
		private int addX;
		private int addY;
		private int x;
		private int y;

		public CellIterator(int addX, int addY)
		{
			this.addX = addX;
			this.addY = addY;
		}

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Integer next()
		{
			return null;
		}
	}





}

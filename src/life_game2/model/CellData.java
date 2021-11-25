package life_game2.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import canvas2.util.Direction;

public class CellData {

	private final char ALIVE = '*';
	private final char DEAD = '.';
	private final char LF = '\n';

	private Map<String, Long> data = new ConcurrentHashMap<>();


	public int getChunkWidth()
	{
		return 64;
	}

	public int getCellSize()
	{
		return 16;
	}

	public String getRectPattern()
	{
		return "(\r\n|\n|\r|[" + this.ALIVE + this.DEAD + this.LF + "])*";
	}

	public void clear()
	{
		this.data.clear();
	}

	public int getChunkX(int cellX)
	{
		return Math.floorDiv(cellX, this.getChunkWidth());
	}

	public int getRemainingCellX(int chunkX, int cellX)
	{
		return cellX - (chunkX * this.getChunkWidth());
	}

	public void setCell(boolean b, int cellX, int cellY)
	{
		int chunkX = this.getChunkX(cellX);
		cellX = this.getRemainingCellX(chunkX, cellX);

		this.setCell(b, chunkX, cellY, cellX);
	}

	public void setCell(boolean b, int x, int y, int cellX)
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

	public boolean getCell(int cellX, int cellY)
	{
		int chunkX = this.getChunkX(cellX);
		cellX = this.getRemainingCellX(chunkX, cellX);

		return this.getCell(chunkX, cellY, cellX);
	}

	public boolean getCell(int x, int y, int cellX)
	{
		long chunk = this.get(x, y);
		long mask = 0x8000_0000_0000_0000L >>> cellX;

		if((chunk & mask) == mask)
		{
			return true;
		}

		return false;
	}


	public long get(int x, int y, Direction d)
	{
		return this.get(x + d.getX(), y + d.getY());
	}

	public long get(int x, int y)
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

	public void set(long chunk, int x, int y, Direction d)
	{
		this.set(chunk, x + d.getX(), y + d.getY());
	}

	public void set(long chunk, int x, int y)
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

	public void add(long chunk, int x, int y)
	{
		this.set(this.get(x, y) | chunk, x, y);
	}

	public String createFillRect(int width, int height, boolean isAlive)
	{
		String c = String.valueOf(isAlive ? this.DEAD : this.ALIVE);
		String text = "";

		for(int i = 0; i < height; i++)
		{
			if(!text.equals(""))
			{
				text += this.LF;
			}

			text += c.repeat(width);
		}

		return text;
	}

	public String getFromRect(int cellX, int cellY, int width, int height)
	{
		//左上のセルをのぞいた値。
		width += -1;
		height += -1;

		int chunkWidth = this.getChunkWidth();

		int chunkMinX = this.getChunkX(cellX);
		int cellMinX = this.getRemainingCellX(chunkMinX, cellX);

		int chunkMaxX = this.getChunkX(cellX + width);
		int cellMaxX = this.getRemainingCellX(chunkMaxX, cellX + width);

		System.out.println(cellMinX + " " + cellMaxX);
		System.out.println(chunkMinX + " " + chunkMaxX);

		String output = "";
		for(int chunkY = cellY; chunkY <= cellY + height; chunkY++)
		{
			if(!output.equals(""))
			{
				output += this.LF;
			}

			String s = "";

			//同一のチャンク内であるとき、
			if(chunkMinX == chunkMaxX)
			{
				for(int x = cellMinX; x <= cellMaxX; x++)
				{
					boolean b = this.getCell(chunkMinX, chunkY, x);
					s += (b ? this.ALIVE : this.DEAD);
				}

				output += s;
				continue;
			}



			//左端
			for(int x = cellMinX; x <= chunkWidth; x++)
			{
				boolean b = this.getCell(chunkMinX, chunkY, x);
				s += (b ? this.ALIVE : this.DEAD);
			}

			//中間
			for(int chunkX = chunkMinX + 1; chunkX < chunkMaxX; chunkX++)
			{
				String chunkText = Long.toBinaryString(this.get(chunkX, chunkY));
				int zeroLen = chunkWidth - chunkText.length();
				chunkText = "0".repeat(zeroLen) + chunkText;

				chunkText = chunkText.replaceAll("0", String.valueOf(this.DEAD));
				chunkText = chunkText.replaceAll("1", String.valueOf(this.ALIVE));

				s += chunkText;
			}

			//右端
			for(int x = 0; x <= cellMaxX; x++)
			{
				boolean b = this.getCell(chunkMaxX, chunkY, x);
				s += (b ? this.ALIVE : this.DEAD);
			}

			output += s;
		}

		return output;
	}


	public void setFromRect(String rectData, int cellX, int cellY, boolean overwrite)
	{

		int x = cellX;
		int y = cellY;

		String p = "(\\n|\\r|\\r\\n)";
		rectData = rectData.replaceAll(p, String.valueOf(this.LF));


		for(char c : rectData.toCharArray())
		{

			if(c == this.LF)
			{
				y++;
				x = cellX;
				continue;
			}


			if(overwrite && c == this.DEAD)
			{
				this.setCell(false, x, y);
			}

			if(c == this.ALIVE)
			{
				this.setCell(true, x, y);
			}


			x++;
		}
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

			int x = Integer.parseInt(s.substring(0, index));
			int y = Integer.parseInt(s.substring(index + 1));

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

		private int x = 0;
		private int y = 0;
		private long chunk = 0;

		private Output()
		{

		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public long getValue()
		{
			return chunk;
		}

	}




}

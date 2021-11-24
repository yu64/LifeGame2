package life_game2.view;

import java.awt.Color;
import java.awt.Graphics2D;

import life_game2.model.CellData;

public class CellDrawer{


	public void draw(Graphics2D g2, CellData data, int minX, int minY, int maxX, int maxY)
	{
		//セル単位で、画面の描画範囲を求める。
		int chunkWidth = data.getChunkWidth();
		int cellSize = data.getCellSize();

		//セルを描画する。
		g2.setColor(Color.GREEN);

		for(int x = minX; x <= maxX; x++)
		{
			for(int y = minY; y <= maxY; y++)
			{
				int chunkX = Math.floorDiv(x, chunkWidth);
				int chunkY = y;

				long chunk = data.get(chunkX, chunkY);

				if(chunk == 0L)
				{
					continue;
				}

				if(chunk == 0xFFFF_FFFF_FFFF_FFFFL)
				{
					int cellX = chunkX * chunkWidth;

					g2.fillRect(
							cellX * cellSize,
							chunkY * cellSize,
							cellSize * chunkWidth,
							cellSize
							);

					continue;
				}

				while(chunk != 0L)
				{
					long bit = chunk & (-chunk);
					int index = Long.bitCount(~(bit - 1) ) - 1;
					int cellX = chunkX * chunkWidth + index;

					g2.fillRect(
							cellX * cellSize,
							chunkY * cellSize,
							cellSize,
							cellSize
							);

					chunk = chunk & ~bit;
				}
			}
		}
	}

}

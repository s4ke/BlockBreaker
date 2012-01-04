package de.hotware.blockbreaker.model.generator;

import java.util.ArrayList;
import java.util.Random;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.WinCondition;
import de.hotware.blockbreaker.model.Block.BlockColor;
import de.hotware.blockbreaker.model.Level.Gravity;

public class LevelGenerator {
	/**
	 * Don't use this, yet
	 */
	@Deprecated
	private static boolean hasSolution(Level pLevel) {
		Level copy = pLevel.clone();
		int count = 0;
		for(int i = 0; i < 6; ++i) {
			for(int j = 0; j < 6; ++j, ++count) {
				copy.killBlock(i, j);
			}
		}		
		return false;
	}
	
	/**
	 * @return randomly created Level
	 */
	public static Level randomUncheckedLevel() {
		ArrayList<Block> list = new ArrayList<Block>();
		Block[][] matrix = new Block[6][6];
        for(int i = 0; i < 6; ++i) {
        	for(int j = 0; j < 6; ++j) {
        		matrix[i][j] = new Block(BlockColor.random(), i, j);
        		list.add(new Block(BlockColor.random()));        		
        	}
        }
        Random random = new Random();
        WinCondition win = new WinCondition(random.nextInt(7),
        		random.nextInt(7),
        		random.nextInt(7),
        		random.nextInt(7),
        		random.nextInt(7));
		Level level = new Level(matrix, Gravity.NORTH, list, win);
		return level;
	}
	
}

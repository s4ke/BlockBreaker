package de.hotware.blockbreaker.model.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import de.hotware.blockbreaker.R.id;
import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.WinCondition;
import de.hotware.blockbreaker.model.Block.BlockColor;
import de.hotware.blockbreaker.model.Level.Gravity;
import de.hotware.blockbreaker.util.misc.Randomizer;

public class LevelGenerator {
	
	private static final int LEVEL_WIDTH = 6;
	private static final int LEVEL_HEIGHT = 6;
	
	/**
	 * @return randomly created Level
	 */
	public static Level randomUncheckedLevel() {
		ArrayList<Block> list = new ArrayList<Block>();
		Block[][] matrix = new Block[6][6];
		fillRestOfMatrixWithRandomBlocks(matrix);
        for(int i = 0; i < 6; ++i) {
        	for(int j = 0; j < 6; ++j) {
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
	
	public static Level createRandomLevel(int pNumberOfMoves) {
		Block[][] matrix = new Block[6][6];
		Random random = new Random();
		WinCondition win = new WinCondition(random.nextInt(7),
	        		random.nextInt(7),
	        		random.nextInt(7),
	        		random.nextInt(7),
	        		random.nextInt(7));
		int[] winArray = win.getWinClone();
		Arrays.sort(winArray);
		
		//fill this thing up!
		
		fillRestOfMatrixWithRandomBlocks(matrix);
		
		ArrayList<Block> repl = createReplacementList(matrix, pNumberOfMoves);
		Level level = new Level(matrix, Gravity.NORTH, repl, win);
		
		return level;
	}
	
	/**
	 * Moves the blocks in the Array around and creates a ReplacementList
	 * @return the Replacementlist for the given matrix
	 */
	private static ArrayList<Block> createReplacementList(Block[][] pMatrix, int pNumberOfMoves) {
		ArrayList<Block> ret = new ArrayList<Block>();
		Gravity grav;
		Block var;
		int x;
		int y;
		Block old;
		for(int j = 0; j < pNumberOfMoves; ++j) {
			grav = Gravity.random();
			x = Randomizer.nextInt(7);
			y = Randomizer.nextInt(7);
			old = pMatrix[x][y];
			ret.add(new Block(old.getColor()));
			switch(grav) {
				case NORTH: {					
					for(int i = LEVEL_HEIGHT; i > 0; --i) {
						var = pMatrix[x][i-1];
						var.setPosition(x,i);
						pMatrix[x][i] = var;
					}
					break;
				}
				case EAST: {
					for(int i = 0; i < pMatrix.length - 1; ++i) {
						var = pMatrix[i+1][y];
						var.setPosition(i, y);
						pMatrix[i][y] = var;
					}
					break;
				}
				case SOUTH: {
					for(int i = LEVEL_WIDTH; i < pMatrix[0].length-1; ++i) {
						var = pMatrix[x][i+1];
						var.setPosition(x, i);
						pMatrix[x][i] = var;
					}
					break;
				}
				case WEST: {
					for(int i = 0; i > 0; --i) {
						var = pMatrix[i-1][y];
						var.setPosition(i, y);
						pMatrix[i][y] = var;
					}
					break;
				}
			}
		}
		return ret;
	}
	
	private static void fillRestOfMatrixWithRandomBlocks(Block[][] pMatrix) {
		for(int i = 0; i < 6; ++i) {
        	for(int j = 0; j < 6; ++j) {
        		if(pMatrix[i][j] == null) {
        			pMatrix[i][j] = new Block(BlockColor.random(), i, j);        
        		}
        	}
        }
	}
	
	private static void fillMatrixWithConditionalBlocks(Block[][] pMatrix, WinCondition pWinCondition) {
		BlockColor blockColor;
		int winCount;
		for(int i = 1; i < 6; ++i) {
			blockColor = BlockColor.numberToColor(i);
			winCount = pWinCondition.getWinCount(i);
			if((Randomizer.nextInt(1) + 1) == 1) {
				if(!setToRow(pMatrix, blockColor, winCount)) {
					setToColumn(pMatrix, blockColor, winCount);
				}
			} else {
				if(!setToColumn(pMatrix, blockColor, winCount)) {
					setToRow(pMatrix, blockColor, winCount);
				}
			}
		}
	}
	
	private static boolean setToRow(Block[][] pMatrix, BlockColor pColor, int pSize) {
		//compute the fitting rows
		int[] help  = new int[pMatrix.length];
		int count = 0;
		for(int i = 0; i < pMatrix.length; ++i) {
			if(fitsInRow(pMatrix, i, pSize)) {
				help[count] = i;
				++count;
			}
		}
		int[] rows = new int[count];
		for(int i = 0; i < count; ++i) {
			rows[i] = help[i];
		}
		
		return false;
	}
	
	private static boolean fitsInRow(Block[][] pMatrix, int pRow, int pSize) {
		int space = 0;
		for(int i = 0; i < pMatrix[pRow].length && space < pSize; ++i) {
			if(pMatrix[pRow][i] == null) {
				++space;
			} else {
				space = 0;
			}
		}
		return space == pSize;
	}
	
	private static boolean setToColumn(Block[][] pMatrix, BlockColor pColor, int pSize) {
		//compute the fitting columns
		int[] help  = new int[pMatrix[0].length];
		int count = 0;
		for(int i = 0; i < pMatrix[0].length; ++i) {
			if(fitsInColumn(pMatrix, i, pSize)) {
				help[count] = i;
				++count;
			}
		}
		int[] cols = new int[count];
		for(int i = 0; i < count; ++i) {
			cols[i] = help[i];
		}
		return false;
	}
	
	private static boolean fitsInColumn(Block[][] pMatrix, int pColumn, int pSize) {
		int space = 0;
		for(int i = 0; i < pMatrix[pColumn].length && space < pSize; ++i) {
			if(pMatrix[i][pColumn] == null) {
				++space;
			} else {
				space = 0;
			}
		}
		return space == pSize;
	}
	
}

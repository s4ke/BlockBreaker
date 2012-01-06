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

public class LevelGenerator {
	
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
		
		//TODO create an possible solution to the stuff above
		
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
		int gravRep = 0;
		Random random = new Random();
		for(int i = 0; i < pNumberOfMoves; ++i) {
			gravRep = random.nextInt(4) + 1;
			
		}
		return null;		
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
	
	private static int[] computeFittingRows(Block[][] pMatrix, int pSize) {
		int[] help  = new int[pMatrix.length];
		int[] ret;
		int count = 0;
		for(int i = 0; i < pMatrix.length; ++i) {
			if(fitsInRow(pMatrix, i, pSize)) {
				help[count] = i;
				++count;
			}
		}
		ret = new int[count];
		for(int i = 0; i < count; ++i) {
			ret[i] = help[i];
		}
		return ret;
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
	
	private static int[] computeFittingColumns(Block[][] pMatrix, int pSize) {
		int[] help  = new int[pMatrix[0].length];
		int[] ret;
		int count = 0;
		for(int i = 0; i < pMatrix[0].length; ++i) {
			if(fitsInColumn(pMatrix, i, pSize)) {
				help[count] = i;
				++count;
			}
		}
		ret = new int[count];
		for(int i = 0; i < count; ++i) {
			ret[i] = help[i];
		}
		return ret;
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

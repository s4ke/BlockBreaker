package de.hotware.blockbreaker.model.generator;

import java.util.ArrayList;
import java.util.Arrays;

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
        WinCondition win = new WinCondition(Randomizer.nextInt(7),
        		Randomizer.nextInt(7),
        		Randomizer.nextInt(7),
        		Randomizer.nextInt(7),
        		Randomizer.nextInt(7));
		Level level = new Level(matrix, Gravity.NORTH, list, win);
		return level;
	}
	
	private static class WinValuePair implements Comparable<WinValuePair> {
		
		private final BlockColor mBlockColor;
		private final int mWinCount;
		
		public WinValuePair(BlockColor pBlockColor, int pWinCount) {
			this.mBlockColor = pBlockColor;
			this.mWinCount = pWinCount;
		}
		
		public BlockColor getBlockColor() {
			return this.mBlockColor;
		}
		
		public int getWinCount() {
			return this.mWinCount;
		}

		@Override
		public int compareTo(WinValuePair pOther) {
			return pOther.getWinCount() - this.mWinCount;
		}
		
	}
	
	public static Level createRandomLevelFromSeed(long pSeed, int pNumberOfMoves) {
		Randomizer.setSeed(pSeed);
		Level level = createRandomLevel(pNumberOfMoves);
		Randomizer.newRandomObject();
		return level;
	}
	
	public static Level createRandomLevel(int pNumberOfMoves) {
		Block[][] matrix = new Block[6][6];
		
		WinCondition win = null;
		
		while(win == null || win.getTotalWinCount() < 10) {
			win = new WinCondition(Randomizer.nextInt(7),
					Randomizer.nextInt(7),
	        		Randomizer.nextInt(7),
	        		Randomizer.nextInt(7),
	        		Randomizer.nextInt(7));
		}
	        		
		
		WinValuePair[] sorting = new WinValuePair[5];
		for(int i = 0; i < sorting.length; ++i) {
			sorting[i] = new WinValuePair(BlockColor.numberToColor(i+1), win.getWinCount(i+1));
		}
		
		Arrays.sort(sorting);
		
		fillMatrixWithConditionalBlocks(matrix, sorting);
		
		fillRestOfMatrixWithRandomBlocks(matrix);
		
		ArrayList<Block> repl = new ArrayList<Block>();
		Level level = new Level(matrix, Gravity.NORTH, repl, win);
		
		while(level.checkWin()) {
			repl.clear();
			createReplacementList(matrix, pNumberOfMoves, repl);
		}
		
		return level;
	}
	
	/**
	 * Moves the blocks in the Array around and creates a ReplacementList
	 * @return the Replacementlist for the given matrix
	 */
	private static void createReplacementList(Block[][] pMatrix, 
			int pNumberOfMoves, 
			ArrayList<Block> pReplacementList) {
		Gravity grav;
		Block var;
		int x;
		int y;
		Block old;
		for(int j = 0; j < pNumberOfMoves; ++j) {
			grav = Gravity.random();
			x = Randomizer.nextInt(6);
			y = Randomizer.nextInt(6);
			old = pMatrix[x][y];
			pReplacementList.add(new Block(old.getColor()));
			switch(grav) {
				case NORTH: {					
					for(int i = LEVEL_HEIGHT - 1; i > 0; --i) {
						var = pMatrix[x][i-1];
						var.setPosition(x,i);
						pMatrix[x][i] = var;
					}
					pMatrix[x][0] = new Block(BlockColor.random(), x, 0);
					break;
				}
				case EAST: {
					for(int i = 0; i < LEVEL_WIDTH - 1; ++i) {
						var = pMatrix[i+1][y];
						var.setPosition(i, y);
						pMatrix[i][y] = var;
					}
					pMatrix[LEVEL_WIDTH - 1][y] = new Block(BlockColor.random(), LEVEL_WIDTH - 1, y);
					break;
				}
				case SOUTH: {
					for(int i = 0; i < LEVEL_HEIGHT - 1; ++i) {
						var = pMatrix[x][i+1];
						var.setPosition(x, i);
						pMatrix[x][i] = var;
					}
					pMatrix[x][LEVEL_HEIGHT - 1] = new Block(BlockColor.random(), x, LEVEL_HEIGHT - 1);
					break;
				}
				case WEST: {
					for(int i = LEVEL_WIDTH - 1; i > 0; --i) {
						var = pMatrix[i-1][y];
						var.setPosition(i, y);
						pMatrix[i][y] = var;
					}
					pMatrix[0][y] = new Block(BlockColor.random(), 0, y);
					break;
				}
			}
		}
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
	
	private static void fillMatrixWithConditionalBlocks(Block[][] pMatrix, WinValuePair[] pWinValuePairs) {
		BlockColor blockColor;
		int winCount;
		for(int i = 0; i < pWinValuePairs.length; ++i) {
			winCount = pWinValuePairs[i].getWinCount();
			if(winCount == 0) {
				break;
			}
			blockColor = pWinValuePairs[i].getBlockColor();
			if((Randomizer.nextInt(1)) == 1) {
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
		
		//compute the fitting positions in the chosen row and set it to a random possible position
		if(count > 0) {
			
			int randomRowNumber = rows[Randomizer.nextInt(count)];
			help = new int[pMatrix.length];
			count = 0;
			for(int i = 0; i < pMatrix.length; ++i) {
				if(fitsInRowPosition(pMatrix, randomRowNumber, i, pSize)) {
					help[count] = i;
					++count;
				}
			}
			if(count > 0) {
				int pos[] = new int[count];
				int randomPosNumber = pos[Randomizer.nextInt(count)];
				setToPositionInRow(pMatrix, randomRowNumber, randomPosNumber, pColor, pSize);
				return true;
			}
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
	
	private static boolean fitsInRowPosition(Block[][] pMatrix, int pRow, int pPosition, int pSize) {
		int space = 0;
		for(int i = 0; i < pMatrix[pRow].length && space < pSize; ++i) {
			if(pMatrix[pRow][i] == null) {
				++space;
			} else {
				break;
			}
		}
		return space == pSize;
	}
	
	private static void setToPositionInRow(Block[][] pMatrix, int pRow, int pPosition, BlockColor pColor, int pSize) {
		for(int i = 0; i < pSize; ++i) {
			pMatrix[pRow][i] = new Block(pColor, pRow, i);
		}
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
		
		//compute the fitting positions in the chosen column and set it to a random possible position
		if(count > 0) {			
			int randomColumnNumber = cols[Randomizer.nextInt(count)];
			help = new int[pMatrix[0].length];
			count = 0;
			for(int i = 0; i < pMatrix[0].length; ++i) {
				if(fitsInColumnPosition(pMatrix, randomColumnNumber, i, pSize)) {
					help[count] = i;
					++count;
				}
			}
			if(count > 0) {
				int pos[] = new int[count];
				int randomPosNumber = pos[Randomizer.nextInt(count)];
				setToPositionInColumn(pMatrix, randomColumnNumber, randomPosNumber, pColor, pSize);
				return true;
			}
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
	
	private static boolean fitsInColumnPosition(Block[][] pMatrix, int pColumn, int pPosition, int pSize) {
		int space = 0;
		for(int i = 0; i < pMatrix[0].length && space < pSize; ++i) {
			if(pMatrix[i][pColumn] == null) {
				++space;
			} else {
				break;
			}
		}
		return space == pSize;
	}
	
	private static void setToPositionInColumn(Block[][] pMatrix, int pColumn, int pPosition, BlockColor pColor, int pSize) {
		for(int i = 0; i < pSize; ++i) {
			pMatrix[i][pColumn] = new Block(pColor, i, pColumn);
		}
	}
	
}

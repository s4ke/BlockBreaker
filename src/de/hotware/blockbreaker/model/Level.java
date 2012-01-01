package de.hotware.blockbreaker.model;

import java.io.Serializable;
import java.util.ArrayList;

import de.hotware.blockbreaker.model.Block.BlockColor;
import de.hotware.blockbreaker.model.IGameEndListener.GameEndEvent;
import de.hotware.blockbreaker.model.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.IGravityListener.GravityEvent;
import de.hotware.blockbreaker.model.INextBlockListener.NextBlockChangedEvent;

/**
 * Class for the Game mechanics in BlockBreaker
 * TODO: GameEndListener
 * @author Martin Braun
 */
public class Level implements Serializable, Cloneable{
	
	////////////////////////////////////////////////////////////////////
	////							Constants						////
	////////////////////////////////////////////////////////////////////
	private static final long serialVersionUID = -1037049912770927906L;	
	protected static final int INFINITE_BLOCKS_LEFT = 999;
	
	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////
	protected Block[][] mMatrix;
	protected Gravity mGravity;
	protected Block mNextBlock;
	protected ArrayList<Block> mReplacementList;
	protected WinCondition mWinCondition;
	protected INextBlockListener mNextBlockListener;
	protected IGameEndListener mGameEndListener;
	protected IGravityListener mGravityListener;
	
	////////////////////////////////////////////////////////////////////
	////							Constructors					////
	////////////////////////////////////////////////////////////////////
	public Level(Block[][] pMatrix, Gravity pGravity, ArrayList<Block> pReplacementList, WinCondition pWinCondition) {
		this.mMatrix = pMatrix;
		this.mGravity = pGravity;
		this.mReplacementList = pReplacementList;
		//TODO: dummy block!
		this.mWinCondition = pWinCondition;
		this.nextBlock();
	}
	
	public Level(Block[][] pMatrix, Gravity pGravity) {
		this.mMatrix = pMatrix;
		this.mGravity = pGravity;
		this.nextBlock();
	}
	
	////////////////////////////////////////////////////////////////////
	////							Methods							////
	////////////////////////////////////////////////////////////////////
	/**
	 * kills a Block from the matrix at the specified position
	 * @return the Block that was added after killing
	 */
	public synchronized Block killBlock(int pX, int pY) {
		Block newBlock = this.mNextBlock;
		if(newBlock.getColor() != BlockColor.NONE) {
			Block var;
			switch(this.mGravity) {
				case NORTH: {
					for(int i = pY; i > 0; --i) {
						var = this.mMatrix[pX][i-1];
						var.setPosition(pX,i);
						this.mMatrix[pX][i] = var;
					}
					newBlock.setPosition(pX,0);
					break;
				}
				case EAST: {
					for(int i = pX; i < this.mMatrix.length-1; ++i) {
						var = this.mMatrix[i+1][pY];
						var.setPosition(i, pY);
						this.mMatrix[i][pY] = var;
					}
					newBlock.setPosition(this.mMatrix.length-1,pY);;
					break;
				}
				case SOUTH: {
					for(int i = pY; i < this.mMatrix[0].length-1; ++i) {
						var = this.mMatrix[pX][i+1];
						var.setPosition(pX, i);
						this.mMatrix[pX][i] = var;
					}
					newBlock.setPosition(pX,this.mMatrix[0].length-1);
					break;
				}
				case WEST: {
					for(int i = pX; i > 0; --i) {
						var = this.mMatrix[i-1][pY];
						var.setPosition(i, pY);
						this.mMatrix[i][pY] = var;
					}
					newBlock.setPosition(0,pY);
					break;
				}
			}
			this.mMatrix[newBlock.getX()][newBlock.getY()] = newBlock;
			this.nextBlock();
			if(this.mWinCondition != null) {
				this.checkWin();
			}	
		}
		notifyAll();
		return newBlock;		
	}
	
	/**
	 * sets the nextBlock (either random or according to the replacement list)
	 */
	protected void nextBlock() {
		if(this.mReplacementList != null) {
			if(this.mReplacementList.size() > 0) {
				this.mNextBlock = this.mReplacementList.remove(0);
			} else {
				this.mNextBlock = new Block(BlockColor.NONE);
			}			
		} else {
			this.mNextBlock = new Block(Block.BlockColor.random());
		}
		if(this.mNextBlockListener != null) {
			this.mNextBlockListener.onNextBlockChanged(new NextBlockChangedEvent(this, this.mNextBlock));
		}
	}
	
	/**
	 * Used for checking if player has won or lost. Only use this if WinCondition has been set!
	 */
	protected void checkWin() {
		boolean win = true; 
		boolean help = false;
		for(int i = 1; i < 6 && win; ++i) {
			for(int j = 0; j < this.mMatrix.length && !help; ++j) {
				help = (help || this.checkRow(j, i) || this.checkColumn(j, i)) ;
			}
			win = win && help;
			help = false;
		}
		if(win){
			this.mGameEndListener.onGameEnd(new GameEndEvent(this,GameEndType.WIN));
		} else if (this.mReplacementList.size() == 0 && this.mNextBlock.getColor() == BlockColor.NONE) {
			this.mGameEndListener.onGameEnd(new GameEndEvent(this,GameEndType.LOSE));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Level clone() {
		return new Level(this.mMatrix.clone(),this.mGravity, (ArrayList<Block>)this.mReplacementList.clone(), (WinCondition) this.mWinCondition.clone());
	}
	
	private boolean checkRow(int pX, int pColorNumber) {
		int winCount = this.mWinCondition.getWinCount(pColorNumber);
		BlockColor colorCheck = BlockColor.numberToColor(pColorNumber);
		int counter = 0;
		for(int i = 0; i < this.mMatrix.length; ++i) {
			if(this.mMatrix[pX][i].getColor() == colorCheck) {
				++counter;
			} else {
				counter = 0;
			}
			if(counter == winCount) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkColumn(int pY, int pColorNumber) {
		int winCount = this.mWinCondition.getWinCount(pColorNumber);
		int counter = 0;
		BlockColor colorCheck = BlockColor.numberToColor(pColorNumber);
		for(int i = 0; i < this.mMatrix[0].length; ++i) {
			if(this.mMatrix[i][pY].getColor() == colorCheck) {
				++counter;
			} else {
				counter = 0;
			}
			if(counter == winCount) {
				return true;
			}
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////
	////							Getter/Setter					////
	////////////////////////////////////////////////////////////////////
	public Block getNextBlock() {
		return this.mNextBlock;
	}
	
	public int getBlocksLeft() {
		if(this.mReplacementList != null) {
			return this.mReplacementList.size();
		} else {
			return INFINITE_BLOCKS_LEFT;
		}
	}
	
	public String getBlocksDisplayText() {
		String turnsLeft = "00" + this.getBlocksLeft();
		int length = turnsLeft.length();
		return turnsLeft.substring(length-3, length);
	}
	
	public synchronized void setGravity(Gravity pGravity) {
		if(this.mGravity != pGravity) {
			this.mGravity = pGravity;
			if(this.mGravityListener != null) {
				this.mGravityListener.onGravityChanged(new GravityEvent(this, pGravity));
			}
		}
		notifyAll();
	}
	
	public synchronized Gravity getGravity() {
		Gravity grav = this.mGravity;
		notifyAll();
		return grav;	
	}
	
	public Block[][] getMatrix() {
		return this.mMatrix;
	}
	
	public WinCondition getWinCondition() {
		return this.mWinCondition;
	}
	
	public void setNextBlockListener(INextBlockListener pNextBlockListener) {
		this.mNextBlockListener = pNextBlockListener;
	}
	
	public void setGameEndListener(IGameEndListener pGameEndListener) {
		this.mGameEndListener = pGameEndListener;
	}
	
	public void setGravityListener(IGravityListener pGravityListener) {
		this.mGravityListener = pGravityListener;
	}
	
	////////////////////////////////////////////////////////////////////
	////							Inner Classes					////
	////////////////////////////////////////////////////////////////////
	public enum Gravity {
		NORTH(0),
		EAST(1),
		SOUTH(2),
		WEST(3);		
		private int mX;		
		private Gravity(int pX) {
			this.mX = pX;
		}
		public int toNumber() {
			return this.mX;
		}
	}
}

package de.hotware.blockbreaker.model;

import java.io.Serializable;

import de.hotware.blockbreaker.model.listeners.IBlockPositionListener;
import de.hotware.blockbreaker.model.listeners.IBlockPositionListener.BlockPositionChangedEvent;
import de.hotware.blockbreaker.util.misc.Randomizer;

/**
 * Representation class of the blocks for storing color information
 * and position
 * @author Martin Braun
 */
public class Block implements Serializable, Cloneable {

	////////////////////////////////////////////////////////////////////
	////							Constants						////
	////////////////////////////////////////////////////////////////////
	private static final long serialVersionUID = 2375568374819126825L;

	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////
	private BlockColor mColor;
	private int mX;
	private int mY;
	private IBlockPositionListener mPositionListener;

	////////////////////////////////////////////////////////////////////
	////							Costructors						////
	////////////////////////////////////////////////////////////////////
	/**
	 * Constructor for first initialization of the Level,
	 * where the x and y position are known
	 */
	public Block(BlockColor pColor, int pX, int pY) {
		this.mColor = pColor;
		this.mX = pX;
		this.mY = pY;
	}

	/**
	 * Constructor for later usage if Level kills a Block <br>
	 * <strong>Attention: x and y have to be set after using this constructor!</strong>
	 */
	public Block(BlockColor pColor) {
		this.mColor = pColor;
	}

	////////////////////////////////////////////////////////////////////
	////							Getter/Setter					////
	////////////////////////////////////////////////////////////////////
	public BlockColor getColor() {
		return this.mColor;
	}

	public int getX() {
		return this.mX;
	}

	public int getY() {
		return this.mY;
	}

	public void setX(int pX) {
		this.mX = pX;
	}

	public void setY(int pY) {
		this.mY = pY;
	}

	/**
	 * Sets position according to the parameters and notifies an existing BlockPositionListener about the changes.
	 */
	public void setPosition(int pX, int pY) {
		int oldX = this.mX;
		int oldY = this.mY;
		this.mX = pX;
		this.mY = pY;
		if(this.mPositionListener != null) {
			this.mPositionListener.onPositionChanged(new BlockPositionChangedEvent(this, oldX, oldY));
		}
	}

	public void setBlockPositionListener(IBlockPositionListener pPositionListener) {
		this.mPositionListener = pPositionListener;
	}

	public IBlockPositionListener getBlockPositionListener() {
		return this.mPositionListener;
	}

	////////////////////////////////////////////////////////////////////
	////							Inner Classes					////
	////////////////////////////////////////////////////////////////////
	public enum BlockColor {
		NONE(0),
		BLUE(1),
		GREEN(2),
		RED(3),
		YELLOW(4),
		PURPLE(5);

		private int mColor;

		public static BlockColor random() {
			return numberToColor(Randomizer.nextInt(5)+1);
		}

		public static BlockColor numberToColor(int pX) {
			switch(pX) {
				case 1:	return BLUE;
				case 2:	return GREEN;
				case 3:	return RED;
				case 4: return YELLOW;
				case 5: return PURPLE;
			}
			return NONE;
		}

		private BlockColor(int pColor) {
			this.mColor = pColor;
		}

		public int toNumber() {
			return this.mColor;
		}		
		
		public static int getBiggestColorNumber() {
			return PURPLE.toNumber();
		}
		
		public static int getLowestColorNumber() {
			return BLUE.toNumber();
		}
	}
}

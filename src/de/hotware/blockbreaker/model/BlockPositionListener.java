package de.hotware.blockbreaker.model;

/**
 * Listener that notifies when a Blocks position has changed.
 * @author Martin Braun
 */
public interface BlockPositionListener {
	/**
	 * Listener method for Position changes
	 * @param pEvt the corresponding BlockPositionEvent
	 */
	public void onPositionChanged(BlockPositionChangedEvent pEvt);
	
	/**
	 * Event for storing the the block that has changed
	 * and where he was before.
	 * @author Martin Braun
	 */
	public class BlockPositionChangedEvent {
		private Block mSource;
		private int mOldX;
		private int mOldY;
		
		public BlockPositionChangedEvent(Block pSource, int pOldX, int pOldY) {
			this.mSource = pSource;
			this.mOldX = pOldX;
			this.mOldY = pOldY;
		}
		
		public Block getSource() {
			return this.mSource;
		}
		
		public int getOldX() {
			return this.mOldX;
		}	
		
		public int getOldY() {
			return this.mOldY;
		}
	}
}

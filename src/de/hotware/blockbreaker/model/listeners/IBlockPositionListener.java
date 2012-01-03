package de.hotware.blockbreaker.model.listeners;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.util.misc.GBaseEvent;

/**
 * Listener that notifies when a Blocks position has changed.
 * @author Martin Braun
 */
public interface IBlockPositionListener {
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
	public class BlockPositionChangedEvent extends GBaseEvent<Block> {
		private int mOldX;
		private int mOldY;

		public BlockPositionChangedEvent(Block pSource, int pOldX, int pOldY) {
			super(pSource);
			this.mOldX = pOldX;
			this.mOldY = pOldY;
		}

		public int getOldX() {
			return this.mOldX;
		}	

		public int getOldY() {
			return this.mOldY;
		}
	}
}

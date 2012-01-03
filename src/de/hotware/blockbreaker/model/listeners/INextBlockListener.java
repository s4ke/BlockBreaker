package de.hotware.blockbreaker.model.listeners;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.util.misc.GBaseEvent;

/**
 * Listener that notifies when the next Block has changed
 * @author Martin Braun
 */
public interface INextBlockListener {

	/**
	 * Listener method for Next Block changes
	 * @param pEvt the corresponding NextBlockChangedEvent
	 */
	public void onNextBlockChanged(NextBlockChangedEvent pEvt);

	/**
	 * Event for storing the the new NextBlock
	 * @author Martin Braun
	 */
	public class NextBlockChangedEvent extends GBaseEvent<Level>{

		private Block mNextBlock;

		public NextBlockChangedEvent(Level pSource, Block pNextBlock) {
			super(pSource);
			this.mNextBlock = pNextBlock;
		}

		public Block getNextBlock() {
			return this.mNextBlock;
		}
	}
}
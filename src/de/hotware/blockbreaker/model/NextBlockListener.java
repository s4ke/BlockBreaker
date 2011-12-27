package de.hotware.blockbreaker.model;

/**
 * Listener that notifies when the next Block has changed
 * @author Martin Braun
 */
public interface NextBlockListener {
	/**
	 * Listener method for Next Block changes
	 * @param pEvt the corresponding NextBlockChangedEvent
	 */
	public void onNextBlockChanged(NextBlockChangedEvent pEvt);
	
	/**
	 * Event for storing the the new NextBlock
	 * @author Martin Braun
	 */
	public class NextBlockChangedEvent {
		private Block mNextBlock;
		private Level mSource;
		
		public NextBlockChangedEvent(Level pSource, Block pNextBlock) {
			this.mSource = pSource;
			this.mNextBlock = pNextBlock;
		}
		public Block getNextBlock() {
			return this.mNextBlock;
		}
		public Level getSource() {
			return this.mSource;
		}
	}
}
package de.hotware.blockbreaker.view;

import de.hotware.blockbreaker.model.Block;

public interface BlockTouchListener {
	public void onBlockTouch(BlockTouchEvent pBTEvt);
	
	public class BlockTouchEvent {
	private BlockSprite mSource;
	private Block mBlock;
	
		public BlockTouchEvent(BlockSprite pSource, Block pBlock) {
			this.mSource = pSource;
			this.mBlock = pBlock;
		}
		
		public BlockSprite getSource() {
			return this.mSource;
		}
		
		public Block getBlock() {
			return this.mBlock;
		}
	}
}



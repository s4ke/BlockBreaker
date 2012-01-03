package de.hotware.blockbreaker.view.listeners;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.view.BlockSprite;

public interface IBlockSpriteTouchListener {

	public void onBlockSpriteTouch(BlockSpriteTouchEvent pEvt);

	public class BlockSpriteTouchEvent {
		private BlockSprite mSource;
		private Block mBlock;

		public BlockSpriteTouchEvent(BlockSprite pSource, Block pBlock) {
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



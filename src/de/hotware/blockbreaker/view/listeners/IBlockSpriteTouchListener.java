package de.hotware.blockbreaker.view.listeners;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.util.misc.GBaseEvent;
import de.hotware.blockbreaker.view.BlockSprite;

public interface IBlockSpriteTouchListener {

	public void onBlockSpriteTouch(BlockSpriteTouchEvent pEvt);

	public class BlockSpriteTouchEvent extends GBaseEvent<BlockSprite>{
		private Block mBlock;

		public BlockSpriteTouchEvent(BlockSprite pSource, Block pBlock) {
			super(pSource);
			this.mBlock = pBlock;
		}
		
		public Block getBlock() {
			return this.mBlock;
		}
	}
}



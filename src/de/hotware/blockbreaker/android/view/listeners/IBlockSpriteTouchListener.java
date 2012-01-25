package de.hotware.blockbreaker.android.view.listeners;

import de.hotware.blockbreaker.android.view.BlockSprite;
import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.util.misc.GBaseEvent;

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



package de.hotware.blockbreaker.view;

import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.pool.GenericPool;

import de.hotware.blockbreaker.model.Block;

public class BlockSpritePool extends GenericPool<BlockSprite> {
	
	private TiledTextureRegion mTiledTextureRegion;

	public BlockSpritePool(TiledTextureRegion pTiledTextureRegion) {
		this.mTiledTextureRegion = pTiledTextureRegion;
	}
	
	@Override
	protected BlockSprite onAllocatePoolItem() {
		return new BlockSprite(UIConstants.BASE_SPRITE_WIDTH, 
				UIConstants.BASE_SPRITE_HEIGHT, 
				this.mTiledTextureRegion.deepCopy());
	}
	
	@Override
	protected void onHandleRecycleItem(final BlockSprite pBlockSprite) {
		pBlockSprite.setIgnoreUpdate(true);
		pBlockSprite.reset();
	}
	
	public BlockSprite obtainBlockSprite(final int pX, int pY, final Block pBlock, final BlockTouchListener pBlockTouchListener) {
		BlockSprite bs = this.obtainPoolItem();
		bs.setIgnoreUpdate(false);
		bs.setPosition(pX, pY);
		bs.setBlock(pBlock);
		bs.setBlockTouchListener(pBlockTouchListener);
		return bs;
	}
}

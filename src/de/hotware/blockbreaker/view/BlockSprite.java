package de.hotware.blockbreaker.view;

import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TiledTextureRegion;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.view.BlockTouchListener.BlockTouchEvent;

public class BlockSprite extends TiledSprite {
	public BlockSprite(Block pBlock, BlockTouchListener pBlockTouchListener, float pX, float pY, float pTileWidth, float pTileHeight,
			TiledTextureRegion pTiledTextureRegion) {
		super(pX, pY, pTileWidth, pTileHeight, pTiledTextureRegion);
		this.mBlock = pBlock;
		this.mBlockTouchListener = pBlockTouchListener;
	}
	
	private BlockTouchListener mBlockTouchListener;
	private Block mBlock;

	@Override
	public boolean onAreaTouched(TouchEvent pAreaTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if(this.mBlockTouchListener != null && pAreaTouchEvent.isActionDown()) {
			this.mBlockTouchListener.onBlockTouch(new BlockTouchEvent(this, this.mBlock));
			return true;
		}
		return false;
	}
}

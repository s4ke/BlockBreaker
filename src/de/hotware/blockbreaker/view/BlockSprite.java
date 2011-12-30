package de.hotware.blockbreaker.view;

import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TiledTextureRegion;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.view.BlockTouchListener.BlockTouchEvent;

public class BlockSprite extends TiledSprite {
	
	private static final float UNDEFINED_LOCATION = -999F;
	
	private BlockTouchListener mBlockTouchListener;
	private Block mBlock;
	
	public BlockSprite(float pX, float pY, float pTileWidth, float pTileHeight,TiledTextureRegion pTiledTextureRegion,
			Block pBlock, 	BlockTouchListener pBlockTouchListener) {
		super(pX, pY, pTileWidth, pTileHeight, pTiledTextureRegion);
		this.mBlock = pBlock;
		this.mBlockTouchListener = pBlockTouchListener;
	}
	
	public BlockSprite(float pTileWidth, float pTileHeight, TiledTextureRegion pTiledTextureRegion) {
		super(UNDEFINED_LOCATION, UNDEFINED_LOCATION, pTileWidth, pTileHeight, pTiledTextureRegion);
	}

	@Override
	public boolean onAreaTouched(TouchEvent pAreaTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if(this.mBlockTouchListener != null && pAreaTouchEvent.isActionDown()) {
			this.mBlockTouchListener.onBlockTouch(new BlockTouchEvent(this, this.mBlock));
			return true;
		}
		return false;
	}
	
	@Override
	public void reset() {
		super.reset();
		this.clearUpdateHandlers();
		this.clearEntityModifiers();
		this.getBlock().setBlockPositionListener(null);
		this.mBlock = null;
		this.mBlockTouchListener = null;
	}
	
	public BlockTouchListener getBlockTouchListener() {
		return this.mBlockTouchListener;
	}

	public void setBlockTouchListener(BlockTouchListener pBlockTouchListener) {
		this.mBlockTouchListener = pBlockTouchListener;
	}

	public Block getBlock() {
		return this.mBlock;
	}

	public void setBlock(Block pBlock) {
		this.mBlock = pBlock;
	}
}

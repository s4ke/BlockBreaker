package de.hotware.blockbreaker.android.view;

import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import de.hotware.blockbreaker.android.view.listeners.IBlockSpriteTouchListener;
import de.hotware.blockbreaker.android.view.listeners.IBlockSpriteTouchListener.BlockSpriteTouchEvent;
import de.hotware.blockbreaker.model.Block;

public class BlockSprite extends TiledSprite {

	private static final float UNDEFINED_LOCATION = -999F;

	private IBlockSpriteTouchListener mBlockSpriteTouchListener;
	private Block mBlock;

	public BlockSprite(float pTileWidth, float pTileHeight, TiledTextureRegion pTiledTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
		super(UNDEFINED_LOCATION, UNDEFINED_LOCATION, pTileWidth, pTileHeight, pTiledTextureRegion, pVertexBufferObjectManager);
	}

	@Override
	public boolean onAreaTouched(TouchEvent pAreaTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if(this.mBlockSpriteTouchListener != null && pAreaTouchEvent.isActionDown()) {
			this.mBlockSpriteTouchListener.onBlockSpriteTouch(new BlockSpriteTouchEvent(this, this.mBlock));
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		super.reset();
		this.clearUpdateHandlers();
		this.clearEntityModifiers();
		this.mBlock = null;
		this.mBlockSpriteTouchListener = null;
	}

	public IBlockSpriteTouchListener getBlockSpriteTouchListener() {
		return this.mBlockSpriteTouchListener;
	}

	public void setBlockSpriteTouchListener(IBlockSpriteTouchListener pBlockTouchListener) {
		this.mBlockSpriteTouchListener = pBlockTouchListener;
	}

	public Block getBlock() {
		return this.mBlock;
	}

	public void setBlock(Block pBlock) {
		this.mBlock = pBlock;
	}
}

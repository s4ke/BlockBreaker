package de.hotware.blockbreaker.view;

import org.andengine.entity.scene.Scene;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.pool.GenericPool;

import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.view.listeners.IBlockSpriteTouchListener;

public class BlockSpritePool extends GenericPool<BlockSprite> {

	private Scene mScene;
	private TiledTextureRegion mTiledTextureRegion;
	private VertexBufferObjectManager mVertexBufferObjectManager;

	public BlockSpritePool(Scene pScene, TiledTextureRegion pTiledTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
		this.mScene = pScene;
		this.mTiledTextureRegion = pTiledTextureRegion;
		this.mVertexBufferObjectManager = pVertexBufferObjectManager;
	}

	@Override
	protected BlockSprite onAllocatePoolItem() {
		BlockSprite bs = new BlockSprite(UIConstants.BASE_SPRITE_WIDTH, 
				UIConstants.BASE_SPRITE_HEIGHT, 
				this.mTiledTextureRegion.deepCopy(), this.mVertexBufferObjectManager);
		this.mScene.attachChild(bs);
		return bs;
	}

	@Override
	protected void onHandleRecycleItem(final BlockSprite pBlockSprite) {
		pBlockSprite.setIgnoreUpdate(true);
		pBlockSprite.setVisible(false);
		this.mScene.unregisterTouchArea(pBlockSprite);
	}

	@Override
	protected void onHandleObtainItem(final BlockSprite pBlockSprite) {
		pBlockSprite.reset();
	}

	public BlockSprite obtainBlockSprite(final int pX, int pY, final Block pBlock, final IBlockSpriteTouchListener pBlockSpriteTouchListener) {
		BlockSprite bs = this.obtainPoolItem();
		bs.setIgnoreUpdate(false);
		bs.setVisible(true);
		bs.setPosition(pX, pY);
		bs.setBlock(pBlock);
		bs.setBlockSpriteTouchListener(pBlockSpriteTouchListener);
		return bs;
	}
}

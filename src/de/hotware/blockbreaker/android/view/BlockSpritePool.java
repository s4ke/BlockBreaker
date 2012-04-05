package de.hotware.blockbreaker.android.view;


import org.andengine.entity.scene.Scene;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;

import de.hotware.blockbreaker.android.andengine.extension.ZIndexScene;
import de.hotware.blockbreaker.android.view.listeners.IBlockSpriteTouchListener;
import de.hotware.blockbreaker.model.Block;

public class BlockSpritePool extends GenericPool<BlockSprite> {
	
	/**
	 * Number of Blocks in the Pool that is most likely to be hit at some point 
	 */
	public static final int BLOCKS_ON_SCENE_ESTIMATE = 45;

	private Scene mScene;
	private ITiledTextureRegion mTiledTextureRegion;
	private VertexBufferObjectManager mVertexBufferObjectManager;
	private ZIndexScene mBlockScene;
	
	public BlockSpritePool(Scene pScene,
			ITiledTextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		this.mScene = pScene;
		this.mTiledTextureRegion = pTiledTextureRegion;
		this.mVertexBufferObjectManager = pVertexBufferObjectManager;
		this.mBlockScene = new ZIndexScene(BLOCKS_ON_SCENE_ESTIMATE);
		this.mBlockScene.setBackgroundEnabled(false);
		//scaleX and scaleY are the same
		this.mBlockScene.setScale(this.mScene.getScaleX());
		this.mBlockScene.setX(this.mScene.getX());
		this.mBlockScene.setY(this.mScene.getY());
		this.mScene.setChildScene(this.mBlockScene);
	}

	@Override
	protected BlockSprite onAllocatePoolItem() {
		BlockSprite bs = new BlockSprite(UIConstants.BASE_SPRITE_WIDTH, 
				UIConstants.BASE_SPRITE_HEIGHT, 
				this.mTiledTextureRegion.deepCopy(),
				this.mVertexBufferObjectManager);
		return bs;
	}

	@Override
	protected void onHandleRecycleItem(final BlockSprite pBlockSprite) {
		pBlockSprite.setIgnoreUpdate(true);
		pBlockSprite.setVisible(false);
		this.mBlockScene.unregisterTouchArea(pBlockSprite);
	}

	/**
	 * makes sure that the BlockSprite that is being returned is always on the lowest
	 * zIndex of all BlockSprites
	 */
	@Override
	protected void onHandleObtainItem(final BlockSprite pBlockSprite) {
		pBlockSprite.reset();
		this.mBlockScene.registerTouchArea(pBlockSprite);
		synchronized(this.mBlockScene) {
			if(!pBlockSprite.hasParent()) {
				this.mBlockScene.insertAtBottom(pBlockSprite);
			} else {
				this.mBlockScene.reInsertAtBottom(pBlockSprite);
			}
		}
	}

	public BlockSprite obtainBlockSprite(float pX, float pY, Block pBlock, IBlockSpriteTouchListener pBlockSpriteTouchListener) {
		BlockSprite bs = this.obtainPoolItem();
		bs.setIgnoreUpdate(false);
		bs.setVisible(true);
		bs.setPosition(pX, pY);
		bs.setBlock(pBlock);
		bs.setBlockSpriteTouchListener(pBlockSpriteTouchListener);
		return bs;
	}

}

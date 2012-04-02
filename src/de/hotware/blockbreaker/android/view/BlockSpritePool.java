package de.hotware.blockbreaker.android.view;

import org.andengine.entity.scene.Scene;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;

import de.hotware.blockbreaker.android.view.listeners.IBlockSpriteTouchListener;
import de.hotware.blockbreaker.model.Block;

public class BlockSpritePool extends GenericPool<BlockSprite> {

	public static final int MIN_Z_INDEX = 0;
	
	private Scene mScene;
	private ITiledTextureRegion mTiledTextureRegion;
	private VertexBufferObjectManager mVertexBufferObjectManager;
	private int mCurrentIndex;
	private Scene mBlockScene;
	
	public BlockSpritePool(Scene pScene,
			ITiledTextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		this.mScene = pScene;
		this.mTiledTextureRegion = pTiledTextureRegion;
		this.mVertexBufferObjectManager = pVertexBufferObjectManager;
		this.mCurrentIndex = Integer.MAX_VALUE;
		this.mBlockScene = new Scene();
		this.mBlockScene.setBackgroundEnabled(false);
		this.mScene.attachChild(this.mBlockScene);
	}

	@Override
	protected BlockSprite onAllocatePoolItem() {
		BlockSprite bs = new BlockSprite(UIConstants.BASE_SPRITE_WIDTH, 
				UIConstants.BASE_SPRITE_HEIGHT, 
				this.mTiledTextureRegion.deepCopy(),
				this.mVertexBufferObjectManager);
		this.mBlockScene.attachChild(bs);
		return bs;
	}

	@Override
	protected void onHandleRecycleItem(final BlockSprite pBlockSprite) {
		pBlockSprite.setIgnoreUpdate(true);
		pBlockSprite.setVisible(false);
		this.mScene.unregisterTouchArea(pBlockSprite);
	}

	/**
	 * makes sure that the BlockSprite that is being returned is always on the lowest
	 * zIndex of all BlockSprites
	 * TODO: maybe the same for removing
	 */
	@Override
	protected void onHandleObtainItem(final BlockSprite pBlockSprite) {
		pBlockSprite.reset();
		this.mScene.registerTouchArea(pBlockSprite);
		synchronized(this.mBlockScene) {
			int newIndex = this.mCurrentIndex--;
			if(this.mCurrentIndex == MIN_Z_INDEX) {
				this.mCurrentIndex = Integer.MAX_VALUE;
				int count = this.mBlockScene.getChildCount();
				for(int i = 0; i < count; ++i) {
					this.mBlockScene.getChild(i).setZIndex(this.mCurrentIndex--);
				}
			}
			pBlockSprite.setZIndex(newIndex);
			this.mBlockScene.sortChildren(true);
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

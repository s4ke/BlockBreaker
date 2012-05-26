package de.hotware.blockbreaker.android.view;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierMatcher;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.adt.list.CircularList;
import org.andengine.util.adt.list.concurrent.SynchronizedList;

import android.content.Context;

import de.hotware.blockbreaker.android.R;
import de.hotware.blockbreaker.android.andengine.extension.ZIndexScene;
import de.hotware.blockbreaker.android.andengine.extension.StretchedResolutionPolicy;
import de.hotware.blockbreaker.android.view.listeners.IBlockSpriteTouchListener;
import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.model.Block.BlockColor;
import de.hotware.blockbreaker.model.Level.Gravity;
import de.hotware.blockbreaker.model.gamehandler.ILevelSceneHandler;
import de.hotware.blockbreaker.model.listeners.IGravityListener;
import de.hotware.blockbreaker.model.listeners.INextBlockListener;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.WinCondition;

public class LevelSceneHandler implements ILevelSceneHandler {

	static final int SPRITE_TEXTURE_HEIGHT = UIConstants.BASE_SPRITE_HEIGHT;
	static final int SPRITE_TEXTURE_WIDTH = UIConstants.BASE_SPRITE_WIDTH;

	static final int HORIZONTAL_SPARE = UIConstants.LEVEL_WIDTH - 6 * SPRITE_TEXTURE_WIDTH - 7;
	static final int HORIZONTAL_GAP =  HORIZONTAL_SPARE/2;
	static final int HORIZONTAL_SIZE = UIConstants.LEVEL_WIDTH - HORIZONTAL_SPARE;
	static final int VERTICAL_SPARE = UIConstants.LEVEL_HEIGHT - 6 * SPRITE_TEXTURE_HEIGHT - 7;
	static final int VERTICAL_GAP =  VERTICAL_SPARE / 2;
	static final int VERTICAL_SIZE = UIConstants.LEVEL_HEIGHT - VERTICAL_SPARE;
	
	private BlockSpriteEntityModifierMatcher 
		mBlockSpriteEntityModifierMatcher = new BlockSpriteEntityModifierMatcher();

	Scene mScene;
	Level mLevel;
	BlockSpritePool mBlockSpritePool;
	TiledSprite mNextBlockSprite;
    TiledSprite mGravityArrowSprite;
	Text mTurnsLeftText;
	Text[] mWinCondText;
	
	Text mTimeText;
	Text mTimeLeftText;
	Text mSeedText;

	IBlockSpriteTouchListener mBlockSpriteTouchListener;
	INextBlockListener mNextBlockListener;
	IGravityListener mGravityListener;
	ITiledTextureRegion mBlockTiledTextureRegion;
	ITiledTextureRegion mArrowTiledTextureRegion;
	Font mUIFont;
	Font mHudFont;
	Context mContext;

	SynchronizedList<BlockSprite> mBlockSpriteList;
	
	VertexBufferObjectManager mVertexBufferObjectManager;
	StretchedResolutionPolicy mStretchedResolutionPolicy;
	
	boolean mIgnoreInput;

	public LevelSceneHandler(Scene pScene,
			VertexBufferObjectManager pVertexBufferObjectManager,
			Font pUIFont,
			Font pHudFont,
			ITiledTextureRegion pBlockTiledTextureRegion,
			ITiledTextureRegion pArrowTiledTextureRegion,
			Context pContext) {
		this.mScene = pScene;
		this.mWinCondText = new Text[BlockColor.getBiggestColorNumber()];
		//size for 45 Sprites is a good beginning
		this.mBlockSpriteList = new SynchronizedList<BlockSprite>(new CircularList<BlockSprite>(
				BlockSpritePool.BLOCKS_ON_SCENE_ESTIMATE));
		this.mVertexBufferObjectManager = pVertexBufferObjectManager;
		this.mIgnoreInput = false;
		this.mUIFont = pUIFont;
		this.mHudFont = pHudFont;
		this.mContext = pContext;
		this.mArrowTiledTextureRegion = pArrowTiledTextureRegion;
		this.mBlockTiledTextureRegion = pBlockTiledTextureRegion;
	}
	
	@Override
	public void setIgnoreInput(boolean pIgnoreInput) {
		this.mIgnoreInput = pIgnoreInput;
	}
	
	@Override
	public void updateLevel(Level pLevel, long pSeed) {
		this.resetScene();
		Gravity grav = this.mLevel.getGravity();
		this.mLevel = pLevel;
		this.mLevel.setGravity(grav);
		this.initPlayField();
		this.mGravityArrowSprite.setCurrentTileIndex(grav.toNumber());
		pLevel.setGravityListener(this.mGravityListener);
		pLevel.setNextBlockListener(this.mNextBlockListener);
		for(int i = 1; i < 6; ++i) {
			this.mWinCondText[i-1].setText(Integer.toString(pLevel.getWinCondition().getWinCount(i)));
		}
		this.mTurnsLeftText.setText(pLevel.getBlocksDisplayText());
		this.mNextBlockSprite.setCurrentTileIndex(pLevel.getNextBlock().getColor().toNumber());
	}
	
	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Level getLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initLevelScene(final Level pLevel, long pSeed) {

		if(this.mLevel != null) {
			throw new IllegalStateException("LevelSceneHandler.initLevelScene(Level): " +
					"LevelSceneHandler can only be initialized once!");
		}
		
		this.mLevel = pLevel;
		
		this.mBlockSpritePool = new BlockSpritePool(this.mScene,
				this.mBlockTiledTextureRegion,
				this.mVertexBufferObjectManager);

		//create surroundings
		final Shape ground = new Rectangle(HORIZONTAL_GAP - 1,
				UIConstants.LEVEL_HEIGHT - VERTICAL_GAP + 1,
				HORIZONTAL_SIZE + 3,
				1,
				this.mVertexBufferObjectManager);
		final Shape roof = new Rectangle(HORIZONTAL_GAP - 1,
				VERTICAL_GAP - 1,
				HORIZONTAL_SIZE + 3,
				1,
				this.mVertexBufferObjectManager);
		final Shape left = new Rectangle(HORIZONTAL_GAP - 1,
				VERTICAL_GAP - 1,
				1,
				VERTICAL_SIZE + 4,
				this.mVertexBufferObjectManager);
		final Shape right = new Rectangle(UIConstants.LEVEL_WIDTH - HORIZONTAL_GAP + 1,
				VERTICAL_GAP - 1,
				1,
				VERTICAL_SIZE + 4,
				this.mVertexBufferObjectManager);
		
		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		//create surroundings end

		//init BlockTouchListener
		this.mBlockSpriteTouchListener = new BasicBlockSpriteTouchListener();
		//init BlockTouchListener end

		//init playfield
		this.initPlayField();
		//init playfield end

		//init UI
		TiledSprite winSpriteHelp;
		for(int i = 0; i < 5; ++i) {
			winSpriteHelp = new TiledSprite(
					5,
					17 + VERTICAL_GAP + (SPRITE_TEXTURE_HEIGHT+5)*i,
					SPRITE_TEXTURE_WIDTH,
					SPRITE_TEXTURE_HEIGHT,
					this.mBlockTiledTextureRegion.deepCopy(),
					this.mVertexBufferObjectManager);
			winSpriteHelp.setCurrentTileIndex(i+1);
			winSpriteHelp.setIgnoreUpdate(true);
			this.mScene.attachChild(winSpriteHelp);
		}

		final WinCondition winCondition = pLevel.getWinCondition();
		Text winDisplayText;
		for(int i = 1; i < 6; ++i) {
			winDisplayText = new Text(
					10 + SPRITE_TEXTURE_WIDTH,
					30 + VERTICAL_GAP + (SPRITE_TEXTURE_HEIGHT+5)*(i-1),
					mUIFont,
					Integer.toString(winCondition.getWinCount(i)), 
					1,
					this.mVertexBufferObjectManager);
			this.mWinCondText[i-1] = winDisplayText;
			this.mScene.attachChild(winDisplayText);
		}

		final Text nextText = new Text(0, 0, mUIFont, 
				mContext.getString(R.string.next),
				this.mVertexBufferObjectManager);
		nextText.setPosition(
				UIConstants.LEVEL_WIDTH - nextText.getWidth() - 13,
				2 + VERTICAL_GAP);
		nextText.setIgnoreUpdate(true);
		this.mScene.attachChild(nextText);

		this.mNextBlockSprite = new TiledSprite(
				UIConstants.LEVEL_WIDTH - SPRITE_TEXTURE_WIDTH - 24,
				nextText.getY() + nextText.getHeight() + 10,
				SPRITE_TEXTURE_WIDTH,
				SPRITE_TEXTURE_HEIGHT,
				mBlockTiledTextureRegion.deepCopy(),
				this.mVertexBufferObjectManager);
		this.mNextBlockSprite.setCurrentTileIndex(pLevel.getNextBlock().getColor().toNumber());
		this.mScene.attachChild(this.mNextBlockSprite);

		final Text turnsText = new Text(0, 0, mUIFont, 
				mContext.getString(R.string.turns),
				this.mVertexBufferObjectManager);
		turnsText.setPosition(
				UIConstants.LEVEL_WIDTH - turnsText.getWidth() - 2,
				this.mNextBlockSprite.getY() + this.mNextBlockSprite.getHeight() + 10);
		turnsText.setIgnoreUpdate(true);
		this.mScene.attachChild(turnsText);

		this.mTurnsLeftText = new Text(0, 0, mUIFont, pLevel.getBlocksDisplayText() , 3, this.mVertexBufferObjectManager);
		this.mTurnsLeftText.setPosition(
				UIConstants.LEVEL_WIDTH - this.mTurnsLeftText.getWidth() - 22,
				turnsText.getY() + turnsText.getHeight() + 10);
		this.mScene.attachChild(this.mTurnsLeftText);

		final TiledSprite nextBlockSprite = this.mNextBlockSprite;
		final Text turnsLeftText = this.mTurnsLeftText;
		pLevel.setNextBlockListener(this.mNextBlockListener = new INextBlockListener() {	

			@Override
			public void onNextBlockChanged(NextBlockChangedEvent pEvt) {
				nextBlockSprite.setCurrentTileIndex(pEvt.getNextBlock().getColor().toNumber());
				turnsLeftText.setText(pEvt.getSource().getBlocksDisplayText());
			}	

		});

		this.mGravityArrowSprite = new TiledSprite(
				UIConstants.LEVEL_WIDTH - SPRITE_TEXTURE_WIDTH - 24,
				turnsLeftText.getY() + turnsLeftText.getHeight() + 10,
				SPRITE_TEXTURE_WIDTH,
				SPRITE_TEXTURE_HEIGHT,
				mArrowTiledTextureRegion.deepCopy(),
				this.mVertexBufferObjectManager) {
			
			@Override
			public boolean onAreaTouched(TouchEvent pAreaTouchEvent,
					float pTouchAreaLocalX,
					float pTouchAreaLocalY) {
				if(pAreaTouchEvent.isActionDown()) {
					LevelSceneHandler.this.mLevel.switchToNextGravity();
				}
				return true;
			}
			
		};
		this.mGravityArrowSprite.setCurrentTileIndex(pLevel.getGravity().toNumber());
		this.mScene.registerTouchArea(mGravityArrowSprite);
		this.mScene.attachChild(mGravityArrowSprite);

		pLevel.setGravityListener(this.mGravityListener = new IGravityListener() {
			
			@Override
			public void onGravityChanged(GravityEvent pEvt) {
				LevelSceneHandler.this.mGravityArrowSprite.setCurrentTileIndex(pEvt.getGravity().toNumber());
			}	   
			
		});
		
		this.mTimeText = new Text(0, 
				0, 
				mUIFont, 
				mContext.getString(R.string.time), 
				this.mVertexBufferObjectManager);
		this.mTimeText.setPosition(
				UIConstants.LEVEL_WIDTH - this.mTimeText.getWidth() - 12,
				this.mGravityArrowSprite.getY() + this.mGravityArrowSprite.getHeight() + 10);
		this.mScene.attachChild(this.mTimeText);
		this.mTimeText.setVisible(false);
		this.mTimeText.setIgnoreUpdate(true);
		
		this.mTimeLeftText = new Text(0,
				0,
				mUIFont,
				"xxxx",
				this.mVertexBufferObjectManager);
		this.mTimeLeftText.setPosition(UIConstants.LEVEL_WIDTH - this.mTimeLeftText.getWidth() - 12,
				this.mTimeText.getY() + this.mTimeLeftText.getHeight() + 10);
		this.mScene.attachChild(this.mTimeLeftText);
		this.mTimeLeftText.setVisible(false);
		
		int maxLength = "Seed: ".length() + Long.toString(Long.MAX_VALUE).length() + 1;
		this.mSeedText = new Text(1,
				UIConstants.LEVEL_HEIGHT - 15,
				this.mHudFont,
				"Seed: " + pSeed,
				maxLength,
				this.mVertexBufferObjectManager);
		this.mScene.attachChild(this.mSeedText);
		//init UI end  
	}

	private void initPlayField() {
		Block[][] matrix = this.mLevel.getMatrix();
		for(int i = 0; i < 6; ++i) {
			for(int j = 0; j < 6; ++j) {
				this.addBlockSprite(matrix[i][j]).registerEntityModifier(new FadeInModifier(UIConstants.SPRITE_FADE_IN_TIME));
			}
		}
	}
	
	public Text getTimeText() {
		return this.mTimeText;
	}
	
	public Text getTimeLeftText() {
		return this.mTimeLeftText;
	}
	
	private void resetScene() {
		for(int i = 0; i < this.mBlockSpriteList.size(); ++i) {
			this.mBlockSpritePool.recyclePoolItem(this.mBlockSpriteList.get(i));
		}
		this.mBlockSpriteList.clear();
	}

	BlockSprite addBlockSprite(final Block pBlock) {
		int x = pBlock.getX();
		int y = pBlock.getY();
		BlockSprite sprite = this.mBlockSpritePool.obtainBlockSprite(
				2 + HORIZONTAL_GAP + x * (SPRITE_TEXTURE_WIDTH + 1),
				2 + VERTICAL_GAP + y * (SPRITE_TEXTURE_HEIGHT + 1),
				pBlock, 
				this.mBlockSpriteTouchListener);
		this.mBlockSpriteList.add(sprite);
		sprite.setCurrentTileIndex(pBlock.getColor().toNumber());
		pBlock.setBlockPositionListener(new BasicBlockPositionListener(sprite));
		return sprite;
	}

	private class BasicBlockSpriteTouchListener implements IBlockSpriteTouchListener {

		@Override
		public void onBlockSpriteTouch(BlockSpriteTouchEvent pEvt) {
			if(!LevelSceneHandler.this.mIgnoreInput) {
				BlockSprite src = pEvt.getSource();
				
				Block oldBlock = pEvt.getBlock();
				int x = oldBlock.getX();
				int y = oldBlock.getY();
	
				final Level levelHelp = LevelSceneHandler.this.mLevel;
				final Block block = LevelSceneHandler.this.mLevel.killBlock(x, y);
	
				if(block.getColor() != BlockColor.NONE && levelHelp == LevelSceneHandler.this.mLevel) {
					LevelSceneHandler.this.mBlockSpriteList.remove(src);
					src.unregisterEntityModifiers(LevelSceneHandler.this.mBlockSpriteEntityModifierMatcher);
					src.registerEntityModifier(new ScaleModifier(
							UIConstants.SPRITE_FADE_OUT_TIME, 1.0F, 0.5F));
					src.registerEntityModifier(new FadeOutModifier(
							UIConstants.SPRITE_FADE_OUT_TIME, 
							new IEntityModifierListener() {
	
								@Override
								public void onModifierStarted(
										IModifier<IEntity> pModifier, IEntity pItem) {
									BlockSprite bs = (BlockSprite) pItem;
									ZIndexScene parent = (ZIndexScene) bs.getParent();
									synchronized(parent) {
										parent.reInsertAtBottom(bs);
									}
									parent.unregisterTouchArea(bs);
								}
			
								@Override
								public void onModifierFinished(
										IModifier<IEntity> pModifier, IEntity pItem) {
									LevelSceneHandler.this.mBlockSpritePool.recyclePoolItem((BlockSprite) pItem);
								}
	
					}));
					
					final BlockSprite bs = LevelSceneHandler.this.addBlockSprite(block);
					bs.unregisterEntityModifiers(LevelSceneHandler.this.mBlockSpriteEntityModifierMatcher);
					bs.registerEntityModifier(new FadeInModifier(UIConstants.SPRITE_FADE_IN_TIME));
					bs.registerEntityModifier(new ScaleModifier(UIConstants.SPRITE_FADE_IN_TIME, 0.5F, 1.0F));
				}
			}
		}		
	}
	
	private static class BlockSpriteEntityModifierMatcher implements IEntityModifierMatcher {

		@Override
		public boolean matches(
				IModifier<IEntity> pModifier) {
			return pModifier instanceof ScaleModifier || pModifier instanceof AlphaModifier;
		}
		
	}
	
}

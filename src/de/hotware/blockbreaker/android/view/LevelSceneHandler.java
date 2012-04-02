package de.hotware.blockbreaker.android.view;

import java.util.Properties;

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

import de.hotware.blockbreaker.android.andengine.extension.ZIndexScene;
import de.hotware.blockbreaker.android.andengine.extension.StretchedResolutionPolicy;
import de.hotware.blockbreaker.android.view.listeners.IBlockSpriteTouchListener;
import de.hotware.blockbreaker.model.Block;
import de.hotware.blockbreaker.model.Block.BlockColor;
import de.hotware.blockbreaker.model.Level.Gravity;
import de.hotware.blockbreaker.model.listeners.IGravityListener;
import de.hotware.blockbreaker.model.listeners.INextBlockListener;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.WinCondition;

public class LevelSceneHandler {

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

	IBlockSpriteTouchListener mBlockSpriteTouchListener;
	INextBlockListener mNextBlockListener;
	IGravityListener mGravityListener;

	SynchronizedList<BlockSprite> mBlockSpriteList;
	
	VertexBufferObjectManager mVertexBufferObjectManager;
	StretchedResolutionPolicy mStretchedResolutionPolicy;
	
	boolean mIgnoreInput;

	public LevelSceneHandler(Scene pScene,
			VertexBufferObjectManager pVertexBufferObjectManager,
			StretchedResolutionPolicy pStretchedResolutionPolicy) {
		this.mScene = pScene;
		this.mWinCondText = new Text[BlockColor.getBiggestColorNumber()];
		this.mBlockSpriteList = new SynchronizedList<BlockSprite>(new CircularList<BlockSprite>());
		this.mVertexBufferObjectManager = pVertexBufferObjectManager;
		this.mIgnoreInput = false;
		this.mStretchedResolutionPolicy = pStretchedResolutionPolicy;
	}
	
	public void setIgnoreInput(boolean pIgnoreInput) {
		this.mIgnoreInput = pIgnoreInput;
	}

	public void initLevelScene(Level pLevel,
			Font pUIFont,
			ITiledTextureRegion pBlockTiledTextureRegion,
			ITiledTextureRegion pArrowTiledTextureRegion,
			Properties pStringProperties) {
		
		this.mLevel = pLevel;
		float marginHorizontal = this.mStretchedResolutionPolicy.getMarginHorizontal();
		float marginVertical = this.mStretchedResolutionPolicy.getMarginVertical();

		this.mBlockSpritePool = new BlockSpritePool(this.mScene,
				pBlockTiledTextureRegion,
				this.mVertexBufferObjectManager);

		//create surroundings
		final Shape ground = new Rectangle(marginHorizontal + HORIZONTAL_GAP - 1,
				UIConstants.LEVEL_HEIGHT - VERTICAL_GAP - marginVertical + 1,
				HORIZONTAL_SIZE + 3,
				1,
				this.mVertexBufferObjectManager);
		final Shape roof = new Rectangle(marginHorizontal + HORIZONTAL_GAP - 1,
				marginVertical + VERTICAL_GAP - 1,
				HORIZONTAL_SIZE + 3,
				1,
				this.mVertexBufferObjectManager);
		final Shape left = new Rectangle(marginHorizontal + HORIZONTAL_GAP - 1,
				marginVertical + VERTICAL_GAP - 1,
				1,
				VERTICAL_SIZE + 4,
				this.mVertexBufferObjectManager);
		final Shape right = new Rectangle(UIConstants.LEVEL_WIDTH - HORIZONTAL_GAP - marginHorizontal + 1,
				marginVertical + VERTICAL_GAP - 1,
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
					marginHorizontal + 5,
					marginVertical + 17 + VERTICAL_GAP + (SPRITE_TEXTURE_HEIGHT+5)*i,
					SPRITE_TEXTURE_WIDTH,
					SPRITE_TEXTURE_HEIGHT,
					pBlockTiledTextureRegion.deepCopy(),
					this.mVertexBufferObjectManager);
			winSpriteHelp.setCurrentTileIndex(i+1);
			winSpriteHelp.setIgnoreUpdate(true);
			this.mScene.attachChild(winSpriteHelp);
		}

		final WinCondition winCondition = pLevel.getWinCondition();
		Text winDisplayText;
		for(int i = 1; i < 6; ++i) {
			winDisplayText = new Text(
					marginHorizontal + 10 + SPRITE_TEXTURE_WIDTH,
					marginVertical + 30 + VERTICAL_GAP + (SPRITE_TEXTURE_HEIGHT+5)*(i-1),
					pUIFont,
					Integer.toString(winCondition.getWinCount(i)), 
					1,
					this.mVertexBufferObjectManager);
			this.mWinCondText[i-1] = winDisplayText;
			this.mScene.attachChild(winDisplayText);
		}

		final Text nextText = new Text(0, 0, pUIFont, 
				pStringProperties.getProperty(UIConstants.NEXT_PROPERTY_KEY),
				this.mVertexBufferObjectManager);
		nextText.setPosition(
				UIConstants.LEVEL_WIDTH - nextText.getWidth() - marginHorizontal - 13,
				marginVertical + 2 + VERTICAL_GAP);
		nextText.setIgnoreUpdate(true);
		this.mScene.attachChild(nextText);

		this.mNextBlockSprite = new TiledSprite(
				UIConstants.LEVEL_WIDTH - SPRITE_TEXTURE_WIDTH - marginHorizontal - 24,
				nextText.getY() + nextText.getHeight() + 10,
				SPRITE_TEXTURE_WIDTH,
				SPRITE_TEXTURE_HEIGHT,
				pBlockTiledTextureRegion.deepCopy(),
				this.mVertexBufferObjectManager);
		this.mNextBlockSprite.setCurrentTileIndex(pLevel.getNextBlock().getColor().toNumber());
		this.mScene.attachChild(this.mNextBlockSprite);

		final Text turnsText = new Text(0, 0, pUIFont, 
				pStringProperties.getProperty(UIConstants.TURNS_PROPERTY_KEY),
				this.mVertexBufferObjectManager);
		turnsText.setPosition(
				UIConstants.LEVEL_WIDTH - turnsText.getWidth() - marginHorizontal - 2,
				this.mNextBlockSprite.getY() + this.mNextBlockSprite.getHeight() + 10);
		turnsText.setIgnoreUpdate(true);
		this.mScene.attachChild(turnsText);

		this.mTurnsLeftText = new Text(0, 0, pUIFont, pLevel.getBlocksDisplayText() , 3, this.mVertexBufferObjectManager);
		this.mTurnsLeftText.setPosition(
				UIConstants.LEVEL_WIDTH - this.mTurnsLeftText.getWidth() - marginHorizontal- 22,
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
				UIConstants.LEVEL_WIDTH - SPRITE_TEXTURE_WIDTH - marginHorizontal - 24,
				marginVertical + turnsLeftText.getY() + turnsLeftText.getHeight() + 10,
				SPRITE_TEXTURE_WIDTH,
				SPRITE_TEXTURE_HEIGHT,
				pArrowTiledTextureRegion.deepCopy(),
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
				pUIFont, 
				pStringProperties.getProperty(UIConstants.TIME_PROPERTY_KEY), 
				this.mVertexBufferObjectManager);
		this.mTimeText.setPosition(
				UIConstants.LEVEL_WIDTH - this.mTimeText.getWidth() - marginHorizontal - 12,
				this.mGravityArrowSprite.getY() + this.mGravityArrowSprite.getHeight() + 10);
		this.mScene.attachChild(this.mTimeText);
		this.mTimeText.setVisible(false);
		this.mTimeText.setIgnoreUpdate(true);
		
		this.mTimeLeftText = new Text(0,
				0,
				pUIFont,
				"xxxx",
				this.mVertexBufferObjectManager);
		this.mTimeLeftText.setPosition(UIConstants.LEVEL_WIDTH - this.mTimeLeftText.getWidth() - marginHorizontal - 12,
				this.mTimeText.getY() + this.mTimeLeftText.getHeight() + 10);
		this.mScene.attachChild(this.mTimeLeftText);
		this.mTimeLeftText.setVisible(false);
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

	public void updateLevel(Level pLevel) {
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
				this.mStretchedResolutionPolicy.getMarginHorizontal() + 2 + HORIZONTAL_GAP + x * (SPRITE_TEXTURE_WIDTH + 1),
				this.mStretchedResolutionPolicy.getMarginVertical() + 2 + VERTICAL_GAP + y * (SPRITE_TEXTURE_HEIGHT + 1),
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
										parent.reInsertAtBottom(parent);
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

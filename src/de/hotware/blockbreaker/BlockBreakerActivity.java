package de.hotware.blockbreaker;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FixedResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.ChangeableText;
import org.andengine.entity.util.FPSCounter;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.sensor.orientation.IOrientationListener;
import org.andengine.sensor.orientation.OrientationData;
import org.andengine.ui.activity.BaseGameActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.hotware.blockbreaker.model.generator.LevelGenerator;
import de.hotware.blockbreaker.model.listeners.IGameEndListener;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.view.UIConstants;
import de.hotware.blockbreaker.view.LevelSceneHandler;

/**
 * (c) 2011 Martin Braun
 * @author Martin Braun
 * @since 14:27 7. Dec 2011
 */
public class BlockBreakerActivity extends BaseGameActivity implements IOrientationListener {
	////////////////////////////////////////////////////////////////////
	////							Constants						////
	////////////////////////////////////////////////////////////////////
	public static final int RESULT_CANCELED = -2;
	public static final int RESULT_RESTART = -3;
	public static final int RESULT_WIN = 1;
	public static final int RESULT_LOSE = 2;
	public static final int RESULT_ERROR = -1;
	public static final String LEVEL_ARG_KEY = "levelarg";
	public static final String IS_ASSET_KEY = "isasset";
	public static final String RESULT_KEY = "result";

	private static final String DEFAULT_LEVEL_PATH = "levels/default.lev";
	private static final boolean USE_MENU_WORKAROUND = Integer.valueOf(android.os.Build.VERSION.SDK) < 7;

	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////
	
	private BitmapTextureAtlas mBlockBitmapTextureAtlas;
	private TiledTextureRegion mBlockTiledTextureRegion;
	private BitmapTextureAtlas mArrowBitmapTextureAtlas;
	private TiledTextureRegion mArrowTiledTextureRegion;
	private BitmapTextureAtlas mSceneBackgroundBitmapTextureAtlas;
	private TextureRegion mSceneBackgroundTextureRegion;
	
	private Camera mCamera;	
	private Scene mLevelScene;
	private Font mFPSFont;
	private Font mSceneUIFont;
	
	private LevelSceneHandler mLevelSceneHandler;
	private Level mBackupLevel;
	private Level mLevel;
	@SuppressWarnings("unused")
	private String mLevelPath = DEFAULT_LEVEL_PATH;
	@SuppressWarnings("unused")
	private boolean mIsAsset = true;

	private IGameEndListener mGameEndListener;

	////////////////////////////////////////////////////////////////////
	////					Overridden Methods						////
	////////////////////////////////////////////////////////////////////

	/**
	 * sets the EngineOptions to the needs of the game
	 */
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT), this.mCamera);
		return engineOptions;
	}

	/**
	 * Load all the textures we need
	 */
	@Override
	public void onCreateResources(OnCreateResourcesCallback pCallback) {

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		//loading block textures
		this.mBlockBitmapTextureAtlas = new BitmapTextureAtlas(276,46,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBlockTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBlockBitmapTextureAtlas, this, "blocks_tiled.png", 0,0, 6,1);
		this.mEngine.getTextureManager().loadTexture(this.mBlockBitmapTextureAtlas);

		//loading arrow sprites
		this.mArrowBitmapTextureAtlas = new BitmapTextureAtlas(512,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mArrowTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mArrowBitmapTextureAtlas, this, "arrow_tiled.png", 0,0, 4,1);
		this.mEngine.getTextureManager().loadTexture(this.mArrowBitmapTextureAtlas);

		//Loading Background
		this.mSceneBackgroundBitmapTextureAtlas = new BitmapTextureAtlas(960,640, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mSceneBackgroundTextureRegion = (TextureRegion) BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mSceneBackgroundBitmapTextureAtlas, this, "background.png", 0, 0);   
		this.mEngine.getTextureManager().loadTexture(this.mSceneBackgroundBitmapTextureAtlas);

		//loading fps font
		BitmapTextureAtlas fpsFontTexture = new BitmapTextureAtlas(256,256, TextureOptions.BILINEAR);
		this.mEngine.getTextureManager().loadTexture(fpsFontTexture);
		FontFactory.setAssetBasePath("font/");
		this.mFPSFont = FontFactory.createFromAsset(fpsFontTexture, this, "Droid.ttf", 12, true, Color.BLACK);   	
		this.mEngine.getFontManager().loadFont(this.mFPSFont);

		//loading scene font
		BitmapTextureAtlas sceneFontTexture = new BitmapTextureAtlas(256,256,TextureOptions.BILINEAR);
		this.mEngine.getTextureManager().loadTexture(sceneFontTexture);
		this.mSceneUIFont = FontFactory.createFromAsset(sceneFontTexture, this, "Plok.ttf", 18, true, Color.BLACK);
		this.mEngine.getFontManager().loadFont(this.mSceneUIFont);	

		pCallback.onCreateResourcesFinished();
	}

	/**
	 * initialization of the Activity is done here.
	 */
	@Override
	public void onCreateScene(OnCreateSceneCallback pCallback) {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		//init some kind of levelchoosing here and save it in a variable.
		
		pCallback.onCreateSceneFinished(new Scene());
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pCallback){	
		this.loadLevel();
		this.drawLevel();
		//TODO: setting scene only for testing purposes!!!
		this.mEngine.setScene(this.mLevelScene);
		pCallback.onPopulateSceneFinished();
	}

	@Override
	public void onResumeGame() {
		if(this.mEngine != null) {
			super.onResumeGame();
			this.enableOrientationSensor(this);
		}
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.disableOrientationSensor();
	}

	/**
	 * Listener method for Changes of the devices Orientation.
	 * sets the Levels Gravity to the overwhelming Gravity
	 */
	@Override
	public void onOrientationChanged(OrientationData pOrientData) {
		if(this.mLevel != null) {
			float pitch,roll;
			pitch = pOrientData.getPitch();
			roll = pOrientData.getRoll();
			if(roll == 0 && pitch == 0) {
				this.mLevel.setGravity(Level.Gravity.NORTH);
			} else if(Math.max(Math.abs(pitch), Math.abs(roll)) == Math.abs(pitch)){
				if(-pitch < 0) {
					this.mLevel.setGravity(Level.Gravity.SOUTH);
				} else {
					this.mLevel.setGravity(Level.Gravity.NORTH);
				}
			} else {
				if(roll < 0) {
					this.mLevel.setGravity(Level.Gravity.EAST);
				} else {
					this.mLevel.setGravity(Level.Gravity.WEST);
				}
			}
		}
	}

	/**
	 * creates the menu defined in the corresponding xml file
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.levelmenu, menu);
		return true;
	}

	/**
	 * shows the Activities Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//TODO use AndEngines Menu System!
		switch(item.getItemId()) {
			case R.id.mainmenu:
			{
				//TODO start Levelchoosing here!
				this.showCancelDialog();
				return true;
			}
			case R.id.restart:
			{
				this.restartLevel();
				return true;
			}
			case R.id.next:
			{
				this.randomLevel();
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	/**
	 * Fix for older SDK versions which don't have onBackPressed()	
	 */
	@Override
	public boolean onKeyDown(int pKeyCode, KeyEvent pEvent) {
		if(USE_MENU_WORKAROUND && 
				pKeyCode == KeyEvent.KEYCODE_BACK && 
				pEvent.getRepeatCount() == 0) {
			this.onBackPressed();
		}
		return super.onKeyDown(pKeyCode, pEvent);
	}

	/**
	 * If the user presses the back button he is asked if he wants to quit
	 */
	@Override
	public void onBackPressed() {
		this.showCancelDialog();
	}
	
	////////////////////////////////////////////////////////////////////
	////					Public Methods							////
	////////////////////////////////////////////////////////////////////
	
	public void updateLevel(String pLevelPath, boolean pIsAsset) {
		this.mLevelPath = pLevelPath;
		this.mIsAsset = pIsAsset;
		this.loadLevel();
		this.mLevel = this.mBackupLevel.clone();
		this.mLevel.setGameEndListener(this.mGameEndListener);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}
	
	public void restartLevel() {
		this.mLevel = this.mBackupLevel.clone();
		this.mLevel.setGameEndListener(this.mGameEndListener);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}
	
	public void randomLevel() {
		this.mBackupLevel = LevelGenerator.createRandomLevel(16);
		this.mLevel = this.mBackupLevel.clone();
		this.mLevel.setGameEndListener(this.mGameEndListener);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}

	////////////////////////////////////////////////////////////////////
	////					Private Methods							////
	////////////////////////////////////////////////////////////////////

	private void drawLevel() {
		final Scene scene = new Scene();
		this.mLevelScene = scene;
		
		this.mLevel = this.mBackupLevel.clone();
		
		if(this.mLevel != null) {       
			this.mLevel.setGameEndListener(this.mGameEndListener = new IGameEndListener() {
				@Override
				public void onGameEnd(final GameEndEvent pEvt) {
					BlockBreakerActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							BlockBreakerActivity.this.showEndDialog(pEvt.getType());
						}
					});							
				}

			});

			this.mLevelSceneHandler = new LevelSceneHandler(scene);

			this.mLevelSceneHandler.initLevelScene(this.mLevel, this.mSceneUIFont,
					this.mBlockTiledTextureRegion,
					this.mArrowTiledTextureRegion);

			final HUD hud = new HUD();
			final FPSCounter counter = new FPSCounter();
			this.mEngine.registerUpdateHandler(counter);
			final ChangeableText fps = new ChangeableText(1, 1, this.mFPSFont , "FPS:", "FPS: XXXXX".length());
			hud.attachChild(fps);
			this.mCamera.setHUD(hud);

			scene.registerUpdateHandler(new TimerHandler(1/20F, true, 
				new ITimerCallback() {
					@Override
					public void onTimePassed(TimerHandler arg0) {
						fps.setText("FPS: " + counter.getFPS());
					}            	
				}
			));
		}
		scene.setBackground(new SpriteBackground(new Sprite(0,0,UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT, this.mSceneBackgroundTextureRegion)));
	}

	private void showEndDialog(final GameEndType pResult) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(pResult.toString() + " Restart?")
		.setCancelable(true)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				BlockBreakerActivity.this.restartLevel();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				//TODO: Testing purposes!
				BlockBreakerActivity.this.randomLevel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void showCancelDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Exit game?")
		.setCancelable(true)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				BlockBreakerActivity.this.finish();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void loadLevel() {
		this.mBackupLevel = LevelGenerator.createRandomLevel(16);
//		try {
//			InputStream stream;
//			if(this.mIsAsset) {
//				AssetManager assetManager = this.getResources().getAssets();
//				stream = assetManager.open(this.mLevelPath);
//				this.mBackupLevel = LevelSerializer.readLevel(stream);
//				//TODO add non asset stuff and move the readLevel part out of the if block
//			}
//		} catch (final Exception e) {
//			BlockBreakerActivity.this.runOnUiThread(new Runnable() {
//				public void run() {
//					AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
//					builder.setMessage(e.getMessage() + "\nLeaving to main menu")
//					.setCancelable(false)
//					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//							BlockBreakerActivity.this.setResult(RESULT_CANCELED);
//							BlockBreakerActivity.this.finish();
//						}
//					});
//					AlertDialog alert = builder.create();
//					alert.show();
//				}
//			});	
//			
//		}
	}
}

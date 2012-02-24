package de.hotware.blockbreaker.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSCounter;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.input.sensor.orientation.IOrientationListener;
import org.andengine.input.sensor.orientation.OrientationData;
import org.andengine.ui.activity.BaseGameActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import de.hotware.blockbreaker.android.view.LevelSceneHandler;
import de.hotware.blockbreaker.android.view.UIConstants;
import de.hotware.blockbreaker.model.generator.LevelGenerator;
import de.hotware.blockbreaker.model.listeners.IGameEndListener;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.util.misc.StreamUtil;

/**
 * (c) 2011-2012 Martin Braun
 * TODO: change behaviours via different GameEndListener, Do we need Timed Mode?
 * TODO: save levels (Preferences checkbox if dialog should appear)
 * @author Martin Braun
 * @since Dec 2011
 */
public class BlockBreakerActivity extends BaseGameActivity implements IOrientationListener {
	////////////////////////////////////////////////////////////////////
	////							Constants						////
	////////////////////////////////////////////////////////////////////
	static final String DEFAULT_LEVEL_PATH = "levels/default.lev";
	static final boolean USE_MENU_WORKAROUND = Integer.valueOf(android.os.Build.VERSION.SDK) < 7;
	
	static final String HIGHSCORE_NUM_KEY = "high_score_num_key";
	static final String HIGHSCORE_PLAYER_KEY = "high_score_player_key";
	
	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////
	static final Random sRandomSeedObject = new Random();
	
	boolean mUseOrientSensor = false;
	boolean mTimeAttackMode = false;
	int mNumberOfTurns = 16;
	int mWinCount = 10;
	
	BitmapTextureAtlas mBlockBitmapTextureAtlas;
	TiledTextureRegion mBlockTiledTextureRegion;
	BitmapTextureAtlas mArrowBitmapTextureAtlas;
	TiledTextureRegion mArrowTiledTextureRegion;
	BitmapTextureAtlas mSceneBackgroundBitmapTextureAtlas;
	TextureRegion mSceneBackgroundTextureRegion;
	
	Properties mStringProperties;
	
	Camera mCamera;	
	Scene mLevelScene;
	Font mMiscFont;
	Font mSceneUIFont;
	
	Text mSeedText;
	
	LevelSceneHandler mLevelSceneHandler;
	Level mBackupLevel;
	Level mLevel;
	
	boolean mIgnoreInput = false;
	
	//currently not used
	String mLevelPath = DEFAULT_LEVEL_PATH;
	boolean mIsAsset = true;
	//not used end

	private BaseGameTypeHandler mGameTypeHandler;

	////////////////////////////////////////////////////////////////////
	////					Overridden Methods						////
	////////////////////////////////////////////////////////////////////
	
	@Override
	public void onStart() {
		super.onStart();
		boolean oldTimeAttackMode = this.mTimeAttackMode;
		int oldNumberOfTurns = this.mNumberOfTurns;
		this.getPrefs();
		if(oldTimeAttackMode ^ this.mTimeAttackMode || this.mGameTypeHandler == null) {
			if(this.mGameTypeHandler != null) {
				this.mGameTypeHandler.cleanUp();
			}
			this.mGameTypeHandler = this.mTimeAttackMode ? new TimeAttackGameHandler() : new DefaultGameHandler();
			if(this.mLevel != null) {
				this.mLevel.setGameEndListener(this.mGameTypeHandler);
				this.mGameTypeHandler.start();
			}
		} else if(oldNumberOfTurns != this.mNumberOfTurns) {
			this.mGameTypeHandler.onNumberOfTurnsPropertyChanged();
		}
	}
	
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
		
		//TODO: Language choosing
		this.mStringProperties = new Properties();
		AssetManager assetManager = this.getResources().getAssets();
		InputStream is = null;
		boolean fail = false;
		try {
			is = assetManager.open(UIConstants.DEFAULT_PROPERTIES_PATH);
			this.mStringProperties.load(is);
		} catch (IOException e) {
			fail = true;
			this.showFailDialog(e.getMessage());
		} finally {
			StreamUtil.closeQuietly(is);
		}
		
		if(fail) {
			this.finish();
		}

		TextureManager textureManager = this.mEngine.getTextureManager();
		
		//loading block textures
		this.mBlockBitmapTextureAtlas = new BitmapTextureAtlas(textureManager, 276, 46, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBlockTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBlockBitmapTextureAtlas, this, "blocks_tiled.png", 0,0, 6,1);
		this.mEngine.getTextureManager().loadTexture(this.mBlockBitmapTextureAtlas);

		//loading arrow sprites
		this.mArrowBitmapTextureAtlas = new BitmapTextureAtlas(textureManager, 512, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mArrowTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mArrowBitmapTextureAtlas, this, "arrow_tiled.png", 0,0, 4,1);
		this.mEngine.getTextureManager().loadTexture(this.mArrowBitmapTextureAtlas);

		//Loading Background
		this.mSceneBackgroundBitmapTextureAtlas = new BitmapTextureAtlas(textureManager, 960, 640, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mSceneBackgroundTextureRegion = (TextureRegion) BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mSceneBackgroundBitmapTextureAtlas, this, "background.png", 0, 0);   
		this.mEngine.getTextureManager().loadTexture(this.mSceneBackgroundBitmapTextureAtlas);

		FontManager fontManager = this.mEngine.getFontManager();
		
		//loading fps font
		BitmapTextureAtlas fpsFontTexture = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
		this.mEngine.getTextureManager().loadTexture(fpsFontTexture);
		FontFactory.setAssetBasePath("font/");
		this.mMiscFont = FontFactory.createFromAsset(fontManager, fpsFontTexture, assetManager, "Droid.ttf", 12, true, Color.BLACK);   	
		this.mEngine.getFontManager().loadFont(this.mMiscFont);

		//loading scene font
		BitmapTextureAtlas sceneFontTexture = new BitmapTextureAtlas(textureManager, 256, 256, TextureOptions.BILINEAR);
		this.mEngine.getTextureManager().loadTexture(sceneFontTexture);
		this.mSceneUIFont = FontFactory.createFromAsset(fontManager, sceneFontTexture, assetManager, "Plok.ttf", 18, true, Color.BLACK);
		this.mEngine.getFontManager().loadFont(this.mSceneUIFont);

		pCallback.onCreateResourcesFinished();
	}

	/**
	 * initialization of the Activity is done here.
	 */
	@Override
	public void onCreateScene(OnCreateSceneCallback pCallback) {
		this.mEngine.registerUpdateHandler(new FPSLogger());		
		pCallback.onCreateSceneFinished(new Scene());
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pCallback) {
		this.loadFirstLevel();
		this.initLevel();
		this.mCamera.getHUD().attachChild(this.mSeedText);
		//TODO: setting scene only for testing purposes!!!
		this.mEngine.setScene(this.mLevelScene);
		pCallback.onPopulateSceneFinished();
		this.mGameTypeHandler.start();
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		if(this.mUseOrientSensor) {
			this.enableOrientationSensor(this);
		} else {
			this.disableOrientationSensor();
		}
		this.mGameTypeHandler.onEnterFocus();
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.disableOrientationSensor();
		this.mGameTypeHandler.onLeaveFocus();
	}

	/**
	 * Listener method for Changes of the devices Orientation.
	 * sets the Levels Gravity to the overwhelming Gravity
	 */
	@Override
	public void onOrientationChanged(OrientationData pOrientData) {
		if(this.mUseOrientSensor) {
			if(this.mLevel != null) {
				float pitch = pOrientData.getPitch();
				float roll = pOrientData.getRoll();
				if(roll == 0 && pitch == 0) {
					this.mLevel.setGravity(Level.Gravity.NORTH);
				} else if(Math.max(Math.abs(pitch), Math.abs(roll)) == Math.abs(pitch)) {
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
	}

	/**
	 * creates the menu defined in the corresponding xml file
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(this.mStringProperties != null) {
			menu.add(Menu.NONE,
					UIConstants.MENU_MENU_ID,
					Menu.NONE,
					this.mStringProperties.getProperty(UIConstants.MENU_PROPERTY_KEY));
			menu.add(Menu.NONE,
					UIConstants.FROM_SEED_MENU_ID,
					Menu.NONE,
					this.mStringProperties.getProperty(UIConstants.FROM_SEED_PROPERTY_KEY));
			menu.add(Menu.NONE,
					UIConstants.RESTART_MENU_ID,
					Menu.NONE,
					this.mStringProperties.getProperty(UIConstants.RESTART_PROPERTY_KEY));
			menu.add(Menu.NONE, 
					UIConstants.NEXT_MENU_ID, 
					Menu.NONE, 
					this.mStringProperties.getProperty(UIConstants.NEXT_PROPERTY_KEY));
			return true;
		}
		return false;
	}

	/**
	 * shows the Activities Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//TODO use AndEngines Menu System!
		switch(item.getItemId()) {
			case UIConstants.MENU_MENU_ID: {
				if(this.mGameTypeHandler.requestLeaveToMenu()) {
	                Intent settingsActivity = new Intent(getBaseContext(),
	                        BlockBreakerPreferencesActivity.class);
	                this.startActivity(settingsActivity);
				}
				return true;
			}
			case UIConstants.FROM_SEED_MENU_ID: {
				if(!this.mTimeAttackMode) {
					this.showInputSeedDialog();
				}
				return true;
			}
			case UIConstants.RESTART_MENU_ID: {
				this.mGameTypeHandler.requestRestart();
				return true;
			}
			case UIConstants.NEXT_MENU_ID:	{
				this.mGameTypeHandler.requestNextLevel();
				return true;
			}
			default: {
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
	
	@Override
	public void onOrientationAccuracyChanged(OrientationData pOrientationData) {
		
	}

	////////////////////////////////////////////////////////////////////
	////					Private/Package Methods					////
	////////////////////////////////////////////////////////////////////
	
	void updateLevel(String pLevelPath, boolean pIsAsset) throws Exception {
		this.mSeedText.setText("");
		this.mLevelPath = pLevelPath;
		this.mIsAsset = pIsAsset;
		this.loadLevelFromAsset();
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(this.mGameTypeHandler);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}
	
	void loadLevelFromAsset() throws Exception{
		throw new Exception("not Implemented!");
	}
	
	void restartLevel() {
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(this.mGameTypeHandler);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}
	
	void randomLevel() {
		long seed = sRandomSeedObject.nextLong();
		this.randomLevelFromSeed(seed);
	}
	
	void randomLevelFromSeed(long pSeed) {
		this.mSeedText.setText("Seed: " + Long.toString(pSeed));
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(pSeed, this.mNumberOfTurns, this.mWinCount);
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(this.mGameTypeHandler);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}

	private void initLevel() {
		final Scene scene = new Scene();
		this.mLevelScene = scene;
		
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		
		this.mLevel.setGameEndListener(this.mGameTypeHandler);
		
		VertexBufferObjectManager vboManager = this.mEngine.getVertexBufferObjectManager();
		
		this.mLevelSceneHandler = new LevelSceneHandler(scene, vboManager);
	
		this.mLevelSceneHandler.initLevelScene(BlockBreakerActivity.this.mLevel, 
		this.mSceneUIFont,
		this.mBlockTiledTextureRegion,
		this.mArrowTiledTextureRegion,
		this.mStringProperties);
		
		final HUD hud = new HUD();
		final FPSCounter counter = new FPSCounter();
		this.mEngine.registerUpdateHandler(counter);
		final int maxLength = "FPS: XXXXXXX".length();
		final Text fps = new Text(1, 
				1, 
				this.mMiscFont, 
				"FPS:", 
				maxLength, 
				vboManager);
		hud.attachChild(fps);
		
		this.mCamera.setHUD(hud);

		scene.registerUpdateHandler(new TimerHandler(1/20F, true, 
			new ITimerCallback() {
				@Override
				public void onTimePassed(TimerHandler arg0) {
					String fpsString = Float.toString(counter.getFPS());
					int length = fpsString.length();
					fps.setText("FPS: " + fpsString.substring(0, length >= 5 ? 5 : length));
				}            	
			}
		));
		scene.setBackground(new SpriteBackground(new Sprite(0,0,UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT, BlockBreakerActivity.this.mSceneBackgroundTextureRegion, vboManager)));
	}

	void showEndDialog(final GameEndType pResult) {
		String resString;
		
		switch(pResult) {
			case WIN: {
				resString = this.mStringProperties.getProperty(UIConstants.WIN_GAME_PROPERTY_KEY);
				break;
			}
			case LOSE: {
				resString = this.mStringProperties.getProperty(UIConstants.LOSE_GAME_PROPERTY_KEY);
				break;
			}
			default: {
				resString = "WTF?";
				break;
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(resString + " " + this.mStringProperties.getProperty(UIConstants.RESTART_QUESTION_PROPERTY_KEY))
		.setCancelable(true)
		.setPositiveButton(this.mStringProperties.getProperty(UIConstants.YES_PROPERTY_KEY), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				BlockBreakerActivity.this.restartLevel();
			}
		})
		.setNegativeButton(this.mStringProperties.getProperty(UIConstants.NO_PROPERTY_KEY), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				//TODO: Testing purposes!
				BlockBreakerActivity.this.randomLevel();
			}
		});
		builder.create().show();
	}
	
	void showCancelDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(this.mStringProperties.getProperty(UIConstants.EXIT_GAME_QUESTION_PROPERTY_KEY))
		.setCancelable(true)
		.setPositiveButton(this.mStringProperties.getProperty(UIConstants.YES_PROPERTY_KEY), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				BlockBreakerActivity.this.finish();
			}
		})
		.setNegativeButton(this.mStringProperties.getProperty(UIConstants.NO_PROPERTY_KEY), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	void showFailDialog(String pMessage) {
		//Don't use Properties here because this is used for failures in property loading as well
		AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
		builder.setMessage(pMessage + "\nQuitting!")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pId) {
				pDialog.dismiss();
				BlockBreakerActivity.this.finish();
			}
		});
		builder.create().show();
	}
	
	void showInputSeedDialog() {
		this.showInputSeedDialog(
				this.mStringProperties.getProperty(UIConstants.INPUT_SEED_QUESTION_PROPERTY_KEY));
	}
	
	void showInputSeedDialog(String pText) {	
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);
		input.setGravity(Gravity.CENTER);
		fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(pText)
		.setView(fl)
		.setCancelable(true)
		.setPositiveButton(this.mStringProperties.getProperty(UIConstants.OK_PROPERTY_KEY), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface pDialog, int pId) {
						try { 
							pDialog.dismiss();
							long seed = Long.parseLong(input.getText().toString());
							BlockBreakerActivity.this.randomLevelFromSeed(seed);	
						} catch (NumberFormatException e) {
							BlockBreakerActivity.this.showInputSeedDialog(
									BlockBreakerActivity.this.mStringProperties.getProperty(
											UIConstants.WRONG_SEED_INPUT_PROPERTY_KEY));					
						}
					}
			})
			.setNegativeButton(this.mStringProperties.getProperty(UIConstants.CANCEL_PROPERTY_KEY),
					new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface pDialog, int pId) {
							pDialog.dismiss();
						}
			}
		);
		builder.create().show();
	}
	
	/**
	 * loads the first randomly generated Level and initializes the SeedText
	 */
	private void loadFirstLevel() {
		long seed = sRandomSeedObject.nextLong();
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(seed, this.mNumberOfTurns, this.mWinCount);
		int maxLength = "Seed: ".length() + Long.toString(Long.MAX_VALUE).length() + 1;
		this.mSeedText = new Text(1,
				UIConstants.LEVEL_HEIGHT - 15,
				this.mMiscFont,
				"Seed: " + seed,
				maxLength,
				this.mEngine.getVertexBufferObjectManager());
	}
	
	 private void getPrefs() {
	     // Get the xml/preferences.xml preferences
		 // Don't save this in a constant, because it will only be used in code here
	     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	     this.mUseOrientSensor = prefs.getBoolean("orient_sens_pref", false);
	     this.mTimeAttackMode = prefs.getBoolean("time_attack_pref", false);
	     this.mNumberOfTurns = Integer.parseInt(prefs.getString("number_of_turns_pref", "16"));
	 }
	 
	////////////////////////////////////////////////////////////////////
	////					Inner Classes & Interfaces				////
	////////////////////////////////////////////////////////////////////
	 
	public abstract class BaseGameTypeHandler implements IGameEndListener {
		 
		 /**
		  * called if Activity loses Focus
		  */
		 public void onLeaveFocus(){}

		 /**
		  * called if Activity gains Focus
		  */
		 public void onEnterFocus(){}
		 
		 /**
		  * called if the user requests the next Level, which is the same as losing in TimeAttack
		  */
		 public void requestNextLevel(){}
		 
		 /**
		  * called if the user requests to leave to the menu Activity
		  * @return true if menu will be shown, false otherwise
		  * @return default version returns true
		  */
		 public boolean requestLeaveToMenu(){return true;}
		 
		 /**
		  * called if the user requests a restart of the game
		  */
		 public void requestRestart(){}
		 
		 /**
		  * called upon first start of the game
		  */
		 public void start(){}
		 
		 /**
		  * called when before the GameHandler is changed
		  */
		 public void cleanUp(){}
		 
		 /**
		  * called if the number of turns property has changed, only used for notifying, no information
		  */
		 public void onNumberOfTurnsPropertyChanged(){}
		 
	 }
	 
	 private class DefaultGameHandler extends BaseGameTypeHandler {
		 
		@Override
		public void onGameEnd(final GameEndEvent pEvt) {
			BlockBreakerActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					BlockBreakerActivity.this.showEndDialog(pEvt.getType());
				}
			});
		}

		@Override
		public void requestRestart() {
			BlockBreakerActivity.this.restartLevel();
		}

		@Override
		public void requestNextLevel() {
			BlockBreakerActivity.this.randomLevel();
		}

		@Override
		public void cleanUp() {
			BlockBreakerActivity.this.randomLevel();
		}
		 
	 }
	 
	 //TODO: Implement all this stuff
	 private class TimeAttackGameHandler extends BaseGameTypeHandler {
		 
		private static final int DEFAULT_DURATION_IN_SECONDS = 120;
		private static final int DEFAULT_NUMBER_OF_ALLOWED_LOSES = 2;
		
		int mDurationInSeconds;
		int mNumberOfAllowedLoses;
		int mGamesLost;
		int mGamesWon;
		TimerHandler mTimeMainHandler;
		TimerHandler mTimeUpdateHandler;
		Text mTimeText;
		Text mTimeLeftText;
		boolean mHasFocus;
		
		public TimeAttackGameHandler() {
			this(DEFAULT_DURATION_IN_SECONDS, DEFAULT_NUMBER_OF_ALLOWED_LOSES);
		}
		
		public TimeAttackGameHandler(int pDurationInSeconds, int pNumberOfAllowedLoses) {
			this.mDurationInSeconds = pDurationInSeconds;
			this.mNumberOfAllowedLoses = pNumberOfAllowedLoses;
			this.mGamesWon = 0;
			this.mGamesLost = 0;
			this.mTimeMainHandler = new TimerHandler(this.mDurationInSeconds, new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					TimeAttackGameHandler.this.onTimeAttackEnd();
				}
				
			});
			this.mTimeUpdateHandler = new TimerHandler(1.0F, true, new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					if(!TimeAttackGameHandler.this.mTimeMainHandler.isTimerCallbackTriggered()) {
						TimeAttackGameHandler.this.mTimeLeftText.setText(
								Integer.toString((int)Math.round(
										TimeAttackGameHandler.this.mDurationInSeconds - TimeAttackGameHandler.this.mTimeMainHandler.getTimerSecondsElapsed())));
					} else {
						TimeAttackGameHandler.this.mTimeLeftText.setText(Integer.toString(0));
						pTimerHandler.setAutoReset(false);
					}
				}
				
			});
		}

		@Override
		public void onGameEnd(GameEndEvent pEvt) {
			switch(pEvt.getType()) {
				case WIN: {
					++this.mGamesWon;
					BlockBreakerActivity.this.randomLevel();
					break;
				}
				case LOSE: {
					this.requestNextLevel();
					break;
				}
			}
		}
		
		@Override
		public void onEnterFocus() {
			if(!this.mHasFocus) {
				this.mHasFocus = true;
				BlockBreakerActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
						builder.setMessage(BlockBreakerActivity.this.mStringProperties.getProperty(UIConstants.TIME_ATTACK_START_TEXT_PROPERTY_KEY))
						.setCancelable(false)
						.setPositiveButton(BlockBreakerActivity.this.mStringProperties.getProperty(UIConstants.START_PROPERTY_KEY), 
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface pDialog, int pId) {
										pDialog.dismiss();
										BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
										BlockBreakerActivity.this.mEngine.registerUpdateHandler(TimeAttackGameHandler.this.mTimeMainHandler);
										BlockBreakerActivity.this.mEngine.registerUpdateHandler(TimeAttackGameHandler.this.mTimeUpdateHandler);
									}
								}
						);
						builder.create().show();
					}
					
				});
			}
		}
		
		@Override
		public void onLeaveFocus() {
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(true);
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(this.mTimeMainHandler);
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(this.mTimeUpdateHandler);
			this.mHasFocus = false;
		}
		
		@Override
		public boolean requestLeaveToMenu() {
			return true;
		}

		@Override
		public void requestRestart() {
			this.reset();
			BlockBreakerActivity.this.randomLevel();
		}

		@Override
		public void requestNextLevel() {
			++this.mGamesLost;
			if(this.mGamesLost <= this.mNumberOfAllowedLoses) {
				BlockBreakerActivity.this.randomLevel();
			} else {
				this.showTimeAttackEndDialog();
			}
		}
		
		@Override
		public void start() {
			this.mTimeLeftText = BlockBreakerActivity.this.mLevelSceneHandler.getTimeLeftText();
			this.mTimeLeftText.setVisible(true);
			this.mTimeLeftText.setText(Integer.toString(this.mDurationInSeconds));
			this.mTimeText = BlockBreakerActivity.this.mLevelSceneHandler.getTimeText();
			this.mTimeText.setVisible(true);
		}

		@Override
		public void cleanUp() {
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(this.mTimeMainHandler);
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(this.mTimeUpdateHandler);
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
			BlockBreakerActivity.this.randomLevel();
			this.mTimeLeftText.setVisible(false);
			this.mTimeLeftText.setText("");
			this.mTimeText.setVisible(false);
		}

		@Override
		public void onNumberOfTurnsPropertyChanged() {
			this.reset();
		}
		
		public void onTimeAttackEnd() {
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(true);
			this.showTimeAttackEndDialog();
		}
			
		private void showTimeAttackEndDialog() {
			BlockBreakerActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
					builder.setMessage(
							BlockBreakerActivity.this.mStringProperties.getProperty(UIConstants.GAME_OVER_TEXT_PROPERTY_KEY)
							+ "\n" + BlockBreakerActivity.this.mStringProperties.getProperty(UIConstants.COMPLETED_LEVELS_PROPERTY_KEY)
							+ ":\n" + TimeAttackGameHandler.this.mGamesWon
							+ "\n" + BlockBreakerActivity.this.mStringProperties.getProperty(UIConstants.LOST_LEVELS_TEXT_PROPERTY_KEY)
							+ ":\n" + TimeAttackGameHandler.this.mGamesLost)
					.setCancelable(true)
					.setPositiveButton(BlockBreakerActivity.this.mStringProperties.getProperty(UIConstants.RESTART_PROPERTY_KEY), 
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface pDialog, int pId) {
									pDialog.dismiss();
									TimeAttackGameHandler.this.requestRestart();
								}
							}
					);
					builder.create().show();
				}
			});
		}
		
		private void reset() {
			this.mGamesWon = 0;
			this.mGamesLost = 0;
			this.mTimeMainHandler.reset();
			this.mTimeUpdateHandler.reset();
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
		}
		 
	 }
	 
}
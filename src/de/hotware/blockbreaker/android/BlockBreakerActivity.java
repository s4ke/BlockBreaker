package de.hotware.blockbreaker.android;

import java.util.Random;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import de.hotware.blockbreaker.android.andengine.extension.StretchedResolutionPolicy;
import de.hotware.blockbreaker.android.highscore.HighscoreManager;
import de.hotware.blockbreaker.android.view.LevelSceneHandler;
import de.hotware.blockbreaker.android.view.UIConstants;
import de.hotware.blockbreaker.model.generator.LevelGenerator;
import de.hotware.blockbreaker.model.listeners.IGameEndListener;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.Level;

/**
 * (c) 2011-2012 Martin Braun
 * TODO: save levels (Preferences checkbox if dialog should appear)
 * TODO: maybe use factory methods instead of new instances for GameTypeHandlers in extra
 * 		 class with methods. Cache instances!
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
	
	static final int DEFAULT_NUMBER_OF_TURNS = 16;
	
	static final int DEFAULT_WIN_COUNT = 10;
	static final int EASY_WIN_COUNT = 10;
	static final int MEDIUM_WIN_COUNT = 13;
	static final int HARD_WIN_COUNT = 16;
	
	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////
	
	static final Random sRandomSeedObject = new Random();

	boolean mUseOrientSensor = false;
	boolean mTimeAttackMode = false;
	int mNumberOfTurns = DEFAULT_NUMBER_OF_TURNS;
	float mResolutionScale;

	ITiledTextureRegion mBlockTiledTextureRegion;
	BitmapTextureAtlas mArrowBitmapTextureAtlas;
	TiledTextureRegion mArrowTiledTextureRegion;
	BitmapTextureAtlas mSceneBackgroundBitmapTextureAtlas;
	TextureRegion mSceneBackgroundTextureRegion;

	Camera mCamera;	
	Scene mLevelScene;
	Font mMiscFont;
	Font mSceneUIFont;

	Text mSeedText;

	LevelSceneHandler mLevelSceneHandler;
	Level mBackupLevel;
	Level mLevel;

	boolean mIgnoreInput = false;
	Difficulty mDifficulty = Difficulty.EASY;
	
	String mPlayerName;
	HighscoreManager mHighscoreManager = new HighscoreManager(this);

	//currently not used
	String mLevelPath = DEFAULT_LEVEL_PATH;
	boolean mIsAsset = true;
	//not used end

	private BaseGameTypeHandler mGameTypeHandler;

	////////////////////////////////////////////////////////////////////
	////					Overridden Methods						////
	////////////////////////////////////////////////////////////////////

	@Override
	public void onResume() {
		super.onResume();
		boolean oldTimeAttackMode = this.mTimeAttackMode;
		int oldNumberOfTurns = this.mNumberOfTurns;
		this.getPrefs();
		if(oldTimeAttackMode ^ this.mTimeAttackMode || this.mGameTypeHandler == null) {
			if(this.mGameTypeHandler != null) {
				this.mGameTypeHandler.cleanUp();
			}
			//hier vll die vorinstanziierten GameHandler statt neuer Instanzen
			this.mGameTypeHandler = this.mTimeAttackMode ? new TimeAttackGameTypeHandler() : new DefaultGameTypeHandler();
			//no level has yet been created nor a LevelSceneHandler which is needed in some GameTypeHandlers
			if(this.mLevel != null) {
				this.mLevel.setGameEndListener(this.mGameTypeHandler);
				this.mGameTypeHandler.init();
			}
		} else if(oldNumberOfTurns != this.mNumberOfTurns) {
			this.mGameTypeHandler.onNumberOfTurnsPropertyChanged();
		}
		if(this.mLevel != null) {
			//call on enter focus, if the game has already started once (outside of the andengine lifecycle!!!)
			this.mGameTypeHandler.onEnterFocus();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		//this has to be done here outside of the andengine lifecycle
		this.mGameTypeHandler.onLeaveFocus();
	}

	/**
	 * sets the EngineOptions to the needs of the game
	 */
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
				new StretchedResolutionPolicy(UIConstants.LEVEL_WIDTH,
						UIConstants.LEVEL_HEIGHT),
				this.mCamera);
		return engineOptions;
	}

	/**
	 * Load all the textures we need
	 */
	@Override
	public void onCreateResources(OnCreateResourcesCallback pCallback) {

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		AssetManager assetManager = this.getResources().getAssets();
		
		TextureManager textureManager = this.mEngine.getTextureManager();
		this.mResolutionScale = ((StretchedResolutionPolicy) this.mEngine.getEngineOptions().getResolutionPolicy()).getScale();
		
		{
			SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			int width = (int) (276 * this.mResolutionScale);
			int height = (int)(46 * this.mResolutionScale);
			BitmapTextureAtlas blockTextureAtlas = new BitmapTextureAtlas(textureManager,
					width,
					height,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mBlockTiledTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
					blockTextureAtlas, this, "blocks_tiled.svg", width, height, 0,0, 6,1);
			this.mEngine.getTextureManager().loadTexture(blockTextureAtlas);
		}

		{
			//loading arrow sprites
			this.mArrowBitmapTextureAtlas = new BitmapTextureAtlas(textureManager, 512, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mArrowTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.
					createTiledFromAsset(this.mArrowBitmapTextureAtlas, this, "arrow_tiled.png", 0,0, 4,1);
			this.mEngine.getTextureManager().loadTexture(this.mArrowBitmapTextureAtlas);
		}

		{
			//Loading Background
			this.mSceneBackgroundBitmapTextureAtlas = new BitmapTextureAtlas(textureManager, 960, 640, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mSceneBackgroundTextureRegion = (TextureRegion) BitmapTextureAtlasTextureRegionFactory.
					createFromAsset(this.mSceneBackgroundBitmapTextureAtlas, this, 
					"background.png", 0, 0);   
			this.mEngine.getTextureManager().loadTexture(this.mSceneBackgroundBitmapTextureAtlas);
		}

		FontManager fontManager = this.mEngine.getFontManager();
		{
			//loading misc font
			//TODO: Fix resizing of Fonts
			BitmapTextureAtlas miscFontTexture = new BitmapTextureAtlas(textureManager,
					(int)(256/* * this.mResolutionScale */),
					(int)(256/* * this.mResolutionScale */),
					TextureOptions.BILINEAR);
			this.mEngine.getTextureManager().loadTexture(miscFontTexture);
			FontFactory.setAssetBasePath("font/");
			this.mMiscFont = FontFactory.createFromAsset(fontManager,
					miscFontTexture,
					assetManager,
					"Droid.ttf",
					12 /* * this.mResolutionScale */,
					true,
					Color.BLACK);   	
			this.mEngine.getFontManager().loadFont(this.mMiscFont);
		}

		{
			//loading scene font
			//TODO: Fix resizing of Fonts
			BitmapTextureAtlas sceneFontTexture = new BitmapTextureAtlas(textureManager,
					(int)(256/* * this.mResolutionScale */),
					(int)(256/* * this.mResolutionScale */),
					TextureOptions.BILINEAR);
			this.mEngine.getTextureManager().loadTexture(sceneFontTexture);
			this.mSceneUIFont = FontFactory.createFromAsset(fontManager,
					sceneFontTexture,
					assetManager,
					"Plok.ttf",
					18 /* * this.mResolutionScale */,
					true,
					Color.BLACK);
			this.mEngine.getFontManager().loadFont(this.mSceneUIFont);
		}
		
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
		this.initLevel();
		pCallback.onPopulateSceneFinished();
		this.mGameTypeHandler.init();
		this.mGameTypeHandler.onEnterFocus();
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		if(this.mUseOrientSensor) {
			this.enableOrientationSensor(this);
		} else {
			this.disableOrientationSensor();
		}
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.disableOrientationSensor();
	}

	/**
	 * Listener method for Changes of the devices Orientation.
	 * sets the Levels Gravity to the overwhelming Orientation
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
	 * creates the menu for on hardware menu click
	 * not being done in a xml file because I don't know if
	 * I will change this to a more fancy style of menu!
	 * TODO: REALLY CHANGE THIS!!! Android 3.0 deprecated the 
	 * menu button!
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE,
				UIConstants.MENU_MENU_ID,
				Menu.NONE,
				this.getString(R.string.menu));
		menu.add(Menu.NONE,
				UIConstants.FROM_SEED_MENU_ID,
				Menu.NONE,
				this.getString(R.string.from_seed));
		menu.add(Menu.NONE,
				UIConstants.RESTART_MENU_ID,
				Menu.NONE,
				this.getString(R.string.restart));
		menu.add(Menu.NONE, 
				UIConstants.NEXT_MENU_ID, 
				Menu.NONE, 
				this.getString(R.string.next));
		return true;
	}

	/**
	 * shows the Activities Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case UIConstants.MENU_MENU_ID: {
				if(this.mGameTypeHandler.requestLeaveToMenu()) {
					Intent settingsActivity = new Intent(this.getBaseContext(),
							BlockBreakerPreferencesActivity.class);
					this.startActivity(settingsActivity);
				}
				return true;
			}
			case UIConstants.FROM_SEED_MENU_ID: {
				this.mGameTypeHandler.requestSeedInput();
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

	/**
	 * restarts the Level by creating a deep copy of the backup level
	 * and making it the current level. it also sets the GameEndListener
	 * correctly and updates the LevelSceneHandlers Level
	 */
	void restartLevel() {
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(this.mGameTypeHandler);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}

	/**
	 * changes the current Level to a completely
	 * randomly generated Level
	 */
	void randomLevel() {
		long seed = sRandomSeedObject.nextLong();
		this.randomLevelFromSeed(seed);
	}

	/**
	 * changes the current Level to a Level from seed
	 * @param pSeed
	 */
	void randomLevelFromSeed(long pSeed) {
		this.mSeedText.setText("Seed: " + Long.toString(pSeed));
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(pSeed, 
				this.mNumberOfTurns, 
				this.mDifficulty.getWinCount());
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();
		this.mLevel.setGameEndListener(this.mGameTypeHandler);
		this.mLevelSceneHandler.updateLevel(this.mLevel);
	}

	/**
	 * initializes the first Level, only to be called on first startup!
	 */
	private void initLevel() {
		
		//init the first Level
		long seed = sRandomSeedObject.nextLong();
		this.mBackupLevel = LevelGenerator.createRandomLevelFromSeed(seed, 
				this.mNumberOfTurns, 
				this.mDifficulty.getWinCount());
		
		final Scene scene = new Scene();
		this.mLevelScene = scene;
		
		StretchedResolutionPolicy policy = 
				(StretchedResolutionPolicy) this.mEngine.getEngineOptions().getResolutionPolicy();
		scene.setScale(this.mResolutionScale);
		scene.setX(policy.getPaddingHorizontal());
		scene.setY(policy.getPaddingVertical());
		this.mCamera.set(0, 0, policy.getDeviceCameraWidth(), policy.getDeviceCameraHeight());
		
		this.mLevel = this.mBackupLevel.copy();
		this.mLevel.start();

		this.mLevel.setGameEndListener(this.mGameTypeHandler);

		VertexBufferObjectManager vboManager = this.mEngine.getVertexBufferObjectManager();

		this.mLevelSceneHandler = new LevelSceneHandler(scene, vboManager);

		//ignore input, gamehandlers will have to handle starting on their own
		this.mLevelSceneHandler.setIgnoreInput(true);

		this.mLevelSceneHandler.initLevelScene(BlockBreakerActivity.this.mLevel, 
				this.mSceneUIFont,
				this.mBlockTiledTextureRegion,
				this.mArrowTiledTextureRegion,
				this.getBaseContext());

		HUD hud = new HUD();
		hud.setY(this.mLevelScene.getY());
		//scaleX and scaleY are the same!!!
		hud.setScale(this.mResolutionScale);
		this.mCamera.setHUD(hud);
		scene.setBackground(new SpriteBackground(new Sprite(0,0,policy.getDeviceCameraWidth(), 
				policy.getDeviceCameraHeight(), 
				BlockBreakerActivity.this.mSceneBackgroundTextureRegion,
				vboManager)));
		
		int maxLength = "Seed: ".length() + Long.toString(Long.MAX_VALUE).length() + 1;
		this.mSeedText = new Text(1,
				UIConstants.LEVEL_HEIGHT - 15,
				this.mMiscFont,
				"Seed: " + seed,
				maxLength,
				this.mEngine.getVertexBufferObjectManager());
		hud.attachChild(this.mSeedText);
		
		this.mEngine.setScene(this.mLevelScene);
	}

	void showCancelDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(this.getString(R.string.exit_game_question))
		.setCancelable(true)
		.setPositiveButton(this.getString(R.string.yes),
				new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface pDialog, int pId) {
						pDialog.dismiss();
						BlockBreakerActivity.this.finish();
					}
			
		})
		.setNegativeButton(this.getString(R.string.no), 
				new DialogInterface.OnClickListener() {
			
					@Override
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
			
			@Override
			public void onClick(DialogInterface pDialog, int pId) {
				BlockBreakerActivity.this.finish();
			}
			
		});
		builder.create().show();
	}

	void showInputSeedDialog() {
		this.showInputSeedDialog(this.getString(R.string.input_seed_question));
	}

	void showInputSeedDialog(String pText) {	
		FrameLayout fl = new FrameLayout(this);
		final EditText input = new EditText(this);
		input.setGravity(Gravity.CENTER);
		fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(pText)
		.setView(fl)
		.setCancelable(true)
		.setPositiveButton(this.getString(R.string.ok), 
				new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface pDialog, int pId) {
						try {
							long seed = Long.parseLong(input.getText().toString());
							BlockBreakerActivity.this.randomLevelFromSeed(seed);
							pDialog.dismiss();
						} catch (NumberFormatException e) {
							BlockBreakerActivity.this.showInputSeedDialog(
									BlockBreakerActivity.this.getString(R.string.wrong_seed_input_text));					
						}
					}
					
		})
		.setNegativeButton(this.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {	
			
					@Override
					public void onClick(DialogInterface pDialog, int pId) {
						pDialog.dismiss();
					}
					
		});
		builder.create().show();
	}

	/**
	 * gets the shared preferences and changes some boolean variables according to that
	 */
	private void getPrefs() {
		// Get the xml/preferences.xml preferences
		// Don't save this in a constant, because it will only be used in code here
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		this.mUseOrientSensor = prefs.getBoolean("orient_sens_pref", false);
		this.mTimeAttackMode = prefs.getBoolean("time_attack_pref", false);
		this.mNumberOfTurns = Integer.parseInt(prefs.getString("number_of_turns_pref", "16"));
		this.mDifficulty = Difficulty.numberToDifficulty(
				Integer.parseInt(prefs.getString("difficulty_pref", "0")));
		this.mPlayerName = prefs.getString("input_name_pref", "Player");
		if(this.mPlayerName.length() == 0) {
			this.mPlayerName = "Player";
		}
		if(this.mPlayerName.length() > 10) {
			this.mPlayerName = this.mPlayerName.substring(0, 10);
		}
		this.mHighscoreManager.ensureNameExistsInDB(this.mPlayerName);
	}

	////////////////////////////////////////////////////////////////////
	////					Inner Classes & Interfaces				////
	////////////////////////////////////////////////////////////////////
	
	/**
	 * The BaseGameTypeHandler class created for more easy implementation
	 * of new game modes. All important Events should be handled here or 
	 * at least have a requestMethod which returns a boolean.
	 */
	public abstract class BaseGameTypeHandler implements IGameEndListener {

		/**
		 * called if Activity loses Focus
		 */
		public void onLeaveFocus() {
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(true);
		}

		public void requestSeedInput() { }

		/**
		 * called if Activity gains Focus
		 */
		public void onEnterFocus() {
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
		}

		/**
		 * called if the user requests the next Level, which is the same as losing in TimeAttack
		 */
		public void requestNextLevel() {}

		/**
		 * called if the user requests to leave to the menu Activity
		 * @return true if menu will be shown, false otherwise
		 * @return default version returns true
		 */
		public boolean requestLeaveToMenu() {return true;}

		/**
		 * called if the user requests a restart of the game
		 */
		public void requestRestart() {}

		/**
		 * called upon first start of the game
		 */
		public void init() {}

		/**
		 * called when before the GameHandler is changed
		 */
		public void cleanUp() {}

		/**
		 * called if the number of turns property has changed, only used for notifying, no information
		 */
		public void onNumberOfTurnsPropertyChanged() {}

	}

	/**
	 * The DefaultGameHandler
	 * @author Martin Braun
	 */
	private class DefaultGameTypeHandler extends BaseGameTypeHandler {

		@Override
		public void onGameEnd(final GameEndEvent pEvt) {
			BlockBreakerActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					DefaultGameTypeHandler.this.showEndDialog(pEvt.getType());
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

		private void showEndDialog(final GameEndType pResult) {
			String resString;

			switch(pResult) {
				case WIN: {
					resString = BlockBreakerActivity.this.getString(R.string.win_text);
					break;
				}
				case LOSE: {
					resString = BlockBreakerActivity.this.getString(R.string.lose_text);
					break;
				}
				default: {
					resString = "WTF?";
					break;
				}
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
			builder.setMessage(resString + " " + BlockBreakerActivity.this.getString(R.string.restart_question))
					.setCancelable(true)
					.setPositiveButton(BlockBreakerActivity.
							this.getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface pDialog, int pId) {
									pDialog.dismiss();
									BlockBreakerActivity.this.restartLevel();
								}
								
					})
					.setNegativeButton(BlockBreakerActivity.this.getString(R.string.no),
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface pDialog, int pId) {
									pDialog.dismiss();
									BlockBreakerActivity.this.randomLevel();
								}
								
					});
			builder.create().show();
		}

		@Override
		public void requestSeedInput() {
			BlockBreakerActivity.this.showInputSeedDialog();
		}

	}

	/**
	 * The TimeAttackGameHandler
	 * @author Martin Braun
	 */
	private class TimeAttackGameTypeHandler extends BaseGameTypeHandler {

		//Time Constants
		private static final int DEFAULT_DURATION_IN_SECONDS = 120;
		private static final int GAME_WIN_TIME_BONUS_IN_SECONDS = 15;
		
		//Game specific Constants
		private static final int DEFAULT_NUMBER_OF_ALLOWED_LOSES = 2;
		private static final int GAME_WIN_POINT_BONUS = 100;
		private static final int BLOCK_LEFT_POINT_BONUS = 10;
		private static final int GAME_LOSE_POINT_BONUS = -50;

		int mDurationInSeconds;
		int mTimePassedInSeconds;
		int mNumberOfAllowedLoses;
		int mGamesLost;
		int mGamesWon;
		TimerHandler mTimeUpdateHandler;
		Text mStatusText;
		Text mTimeText;
		Text mTimeLeftText;
		int mScore;

		public TimeAttackGameTypeHandler() {
			this(DEFAULT_DURATION_IN_SECONDS, DEFAULT_NUMBER_OF_ALLOWED_LOSES);
		}

		public TimeAttackGameTypeHandler(int pDurationInSeconds, int pNumberOfAllowedLoses) {
			this.mDurationInSeconds = pDurationInSeconds;
			this.mNumberOfAllowedLoses = pNumberOfAllowedLoses;
			this.mGamesWon = 0;
			this.mGamesLost = 0;
			this.mScore = 0;
			this.mTimePassedInSeconds = 0;
			this.mTimeUpdateHandler = new TimerHandler(1.0F, true, new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					synchronized(TimeAttackGameTypeHandler.this) {
						int timeLeft = (int)Math.round(
								TimeAttackGameTypeHandler.this.mDurationInSeconds - 
								(++TimeAttackGameTypeHandler.this.mTimePassedInSeconds));
						TimeAttackGameTypeHandler.this.mTimeLeftText.setText(Integer.toString(timeLeft));
						if(timeLeft <= 0) {
							pTimerHandler.setAutoReset(false);
							pTimerHandler.setTimerCallbackTriggered(true);
							TimeAttackGameTypeHandler.this.onTimeAttackEnd();
						}
					}
				}

			});
		}

		@Override
		public void onGameEnd(GameEndEvent pEvt) {
			switch(pEvt.getType()) {
				case WIN: {
					this.mScore = this.mScore + GAME_WIN_POINT_BONUS + 
							BlockBreakerActivity.this.mLevel.getBlocksLeft() * BLOCK_LEFT_POINT_BONUS;
					synchronized(this) {
						this.mTimePassedInSeconds -= GAME_WIN_TIME_BONUS_IN_SECONDS;
					}
					++this.mGamesWon;
					BlockBreakerActivity.this.randomLevel();
					this.updateStatusText();
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
			//assure that some settings are at default just for this gamemode
			BlockBreakerActivity.this.mDifficulty = Difficulty.EASY;
			//and the rest
			if(this.mTimePassedInSeconds < this.mDurationInSeconds
					&& this.mGamesLost < this.mNumberOfAllowedLoses) {
				BlockBreakerActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
						builder.setMessage(BlockBreakerActivity.this.getString(R.string.time_attack_start_text))
						.setCancelable(false)
						.setPositiveButton(BlockBreakerActivity.this.getString(R.string.start), 
								new DialogInterface.OnClickListener() {
							
									@Override
									public void onClick(DialogInterface pDialog, int pId) {
										BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
										BlockBreakerActivity.this.mEngine.registerUpdateHandler(TimeAttackGameTypeHandler.this.mTimeUpdateHandler);
										pDialog.dismiss();
									}

								});
						builder.create().show();
					}

				});
			} else {
				this.showTimeAttackEndDialog();
			}
		}

		@Override
		public void onLeaveFocus() {
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(true);
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(this.mTimeUpdateHandler);
		}

		@Override
		public boolean requestLeaveToMenu() {
			return true;
		}

		@Override
		public void requestRestart() {
			//make sure everything is set back to normal
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(TimeAttackGameTypeHandler.this.mTimeUpdateHandler);
			this.reset();
			BlockBreakerActivity.this.randomLevel();
			//ready, set go!
			BlockBreakerActivity.this.mEngine.registerUpdateHandler(TimeAttackGameTypeHandler.this.mTimeUpdateHandler);
		}

		@Override
		public void requestNextLevel() {
			this.mScore = this.mScore + GAME_LOSE_POINT_BONUS;
			this.updateStatusText();
			++this.mGamesLost;
			if(this.mGamesLost <= this.mNumberOfAllowedLoses) {
				BlockBreakerActivity.this.randomLevel();
			} else {
				this.onTimeAttackEnd();
			}
		}

		@Override
		public void init() {
			this.mTimeLeftText = BlockBreakerActivity.this.mLevelSceneHandler.getTimeLeftText();
			this.mTimeLeftText.setVisible(true);
			this.mTimeLeftText.setIgnoreUpdate(false);
			this.mTimeLeftText.setText(Integer.toString(this.mDurationInSeconds));
			this.mTimeText = BlockBreakerActivity.this.mLevelSceneHandler.getTimeText();
			this.mTimeText.setVisible(true);
			VertexBufferObjectManager vbo = BlockBreakerActivity.this.mEngine.getVertexBufferObjectManager();
			this.mStatusText = new Text(
					5,
					3,
					BlockBreakerActivity.this.mMiscFont,
					"",
					15,
					vbo);
			this.updateStatusText();
			BlockBreakerActivity.this.mCamera.getHUD().attachChild(this.mStatusText);
		}

		@Override
		public void cleanUp() {
			BlockBreakerActivity.this.mEngine.unregisterUpdateHandler(this.mTimeUpdateHandler);
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
			BlockBreakerActivity.this.randomLevel();
			this.mTimeLeftText.setVisible(false);
			this.mTimeLeftText.setIgnoreUpdate(true);
			this.mTimeLeftText.setText("");
			this.mTimeText.setVisible(false);
			this.mStatusText.detachSelf();
		}

		@Override
		public void onNumberOfTurnsPropertyChanged() {
			this.reset();
		}

		public void onTimeAttackEnd() {
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(true);
			this.mTimeUpdateHandler.setAutoReset(false);
			BlockBreakerActivity.this.mHighscoreManager.
				createTimeAttackEntry(BlockBreakerActivity.this.mPlayerName,
					this.mGamesWon, this.mGamesLost, this.mScore);
			this.showTimeAttackEndDialog();
		}

		private void showTimeAttackEndDialog() {
			BlockBreakerActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
					builder.setMessage(
							BlockBreakerActivity.this.getString(R.string.game_over_text)
							+ "\n" + BlockBreakerActivity.this.getString(R.string.score_text)
							+ ":\n" + TimeAttackGameTypeHandler.this.mScore
							+ "\n" + BlockBreakerActivity.this.getString(R.string.completed_levels_text)
							+ ":\n" + TimeAttackGameTypeHandler.this.mGamesWon
							+ "\n" + BlockBreakerActivity.this.getString(R.string.lost_levels_text)
							+ ":\n" + TimeAttackGameTypeHandler.this.mGamesLost)
							.setCancelable(true)
							.setPositiveButton(BlockBreakerActivity.this.getString(R.string.restart), 
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface pDialog, int pId) {
											//a restart has been requested
											TimeAttackGameTypeHandler.this.requestRestart();
											pDialog.dismiss();
										}
							});
					builder.create().show();
				}

			});
		}

		private void reset() {
			this.mScore = 0;
			this.mGamesWon = 0;
			this.mGamesLost = 0;
			this.mTimePassedInSeconds = 0;
			this.mTimeUpdateHandler.reset();
			this.mTimeUpdateHandler.setAutoReset(true);
			BlockBreakerActivity.this.mLevelSceneHandler.setIgnoreInput(false);
			this.updateStatusText();
		}

		private void updateStatusText() {
			this.mStatusText.setText("Score: " + this.mScore);
		}

	}
	
	/**
	 * enum for Difficulty Settings to make preferences stuff more easy
	 * @author Martin Braun
	 */
	public enum Difficulty {
		EASY(BlockBreakerActivity.EASY_WIN_COUNT),
		MEDIUM(BlockBreakerActivity.MEDIUM_WIN_COUNT),
		HARD(BlockBreakerActivity.HARD_WIN_COUNT),
		DEFAULT(BlockBreakerActivity.DEFAULT_WIN_COUNT);
		
		private int mWinCount;
		
		private Difficulty(int pWinCount) {
			this.mWinCount = pWinCount;
		}
		
		public int getWinCount() {
			return this.mWinCount;
		}
		
		public static Difficulty numberToDifficulty(int pNumber) {
			switch(pNumber) {
				case 0: {
					return EASY;
				}
				case 1: {
					return MEDIUM;
				}
				case 2: {
					return HARD;
				}
				default: {
					return DEFAULT;
				}
			}
		}
		
	}

}
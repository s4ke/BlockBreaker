package de.hotware.blockbreaker.android;

import java.io.File;
import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.FileBitmapTextureAtlasSource;
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
import de.hotware.blockbreaker.android.andengine.extension.StretchedResolutionPolicy.ScaleInfo;
import de.hotware.blockbreaker.android.highscore.HighscoreManager;
import de.hotware.blockbreaker.android.view.LevelSceneHandler;
import de.hotware.blockbreaker.android.view.UIConstants;
import de.hotware.blockbreaker.model.gamehandler.BaseGameTypeHandler;
import de.hotware.blockbreaker.model.gamehandler.DefaultGameTypeHandler;
import de.hotware.blockbreaker.model.gamehandler.IHighscoreManager;
import de.hotware.blockbreaker.model.gamehandler.TimeAttackGameTypeHandler;
import de.hotware.blockbreaker.model.gamehandler.DefaultGameTypeHandler.IDefaultViewControl;
import de.hotware.blockbreaker.model.gamehandler.TimeAttackGameTypeHandler.ITimeAttackViewControl;
import de.hotware.blockbreaker.model.gamehandler.IBlockBreakerMessageView;
import de.hotware.blockbreaker.model.listeners.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.util.TextureUtil;

/**
 * (c) 2011-2012 Martin Braun
 * TODO: save levels (Preferences checkbox if dialog should appear)
 * TODO: maybe use factory methods instead of new instances for GameTypeHandlers in extra
 * 		 class with methods. Cache instances!
 * TODO: implement ITimeAttackViewControl methods
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
	
	static final int GRAPHICS_VERSION = 1;
	
	////////////////////////////////////////////////////////////////////
	////							Fields							////
	////////////////////////////////////////////////////////////////////

	boolean mUseOrientSensor = false;
	boolean mTimeAttackMode = false;
	int mGraphicsVersion = -1;
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
	
	String mPlayerName;
	IHighscoreManager mHighscoreManager = new HighscoreManager(this);
	ScaleInfo mScaleInfo;

	//currently not used
	String mLevelPath = DEFAULT_LEVEL_PATH;
	boolean mIsAsset = true;

	private BaseGameTypeHandler mGameTypeHandler;
	private TimeAttackGameTypeHandler mTimeAttackGameTypeHandler = 
			new TimeAttackGameTypeHandler(this.mAndroidTimeUpdater, new AndroidTimeAttackViewControl(), this.mHighscoreManager);
	private DefaultGameTypeHandler mDefaultGameTypeHandler = new DefaultGameTypeHandler(new AndroidDefaultViewControl());
	private int mNumberOfTurns;
	private AndroidTimeUpdater mAndroidTimeUpdater = new AndroidTimeUpdater();

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
			this.mGameTypeHandler = this.mTimeAttackMode ?  this.mTimeAttackGameTypeHandler : this.mDefaultGameTypeHandler;
			//no level has yet been created nor a LevelSceneHandler which is needed in some GameTypeHandlers
			if(this.mLevel != null) {
				this.mLevel.setGameEndListener(this.mGameTypeHandler);
				this.mGameTypeHandler.init(this.mLevelSceneHandler);
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
						UIConstants.LEVEL_HEIGHT,
						this.mScaleInfo = new ScaleInfo()),
				this.mCamera);
		return engineOptions;
	}

	/**
	 * Load all the textures we need
	 */
	@Override
	public void onCreateResources(OnCreateResourcesCallback pCallback) {
		
		//set the TimeUpdaters Engine
		this.mAndroidTimeUpdater.setEngine(this.mEngine);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		this.mGraphicsVersion = prefs.getInt("graphics_version_pref", -1);

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		AssetManager assetManager = this.getResources().getAssets();
		
		TextureManager textureManager = this.mEngine.getTextureManager();
		this.mResolutionScale = this.mScaleInfo.getScale();
		
		{
			int width = (int) (276 * this.mResolutionScale);;
			int height = (int) (46 * this.mResolutionScale);
			String filesDir = this.getFilesDir().toString();
			if(this.mGraphicsVersion != GRAPHICS_VERSION) {
				try {
					TextureUtil.saveSVGToPNG(this, "gfx/blocks_tiled.svg",
							width,
							height,
							 filesDir + "/gfx/",
							"blocks_tiled.png");
				} catch (IOException e) {
					this.finish();
				}
			}
			BitmapTextureAtlas blockTextureAtlas = new BitmapTextureAtlas(textureManager,
					width,
					height,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	        FileBitmapTextureAtlasSource bitmapSource = FileBitmapTextureAtlasSource.create(
	        		new File(filesDir + "/gfx/blocks_tiled.png"));
	       	this.mBlockTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromSource(blockTextureAtlas,
	        		bitmapSource,
	        		0,
	        		0,
	        		6,
	        		1);
			this.mEngine.getTextureManager().loadTexture(blockTextureAtlas);
			prefs.edit().putInt("graphics_version_pref", GRAPHICS_VERSION).commit();
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
		menu.add(Menu.NONE,
				UIConstants.TUTORIAL_ID,
				Menu.NONE,
				this.getString(R.string.tutorial));
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
			case UIConstants.TUTORIAL_ID: {
				this.showTutorial();
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
		this.showExitDialog();
	}

	@Override
	public void onOrientationAccuracyChanged(OrientationData pOrientationData) {

	}

	/**
	 * initializes the first Level, only to be called on first startup!
	 */
	private void initLevel() {
		
		final Scene scene = new Scene();
		this.mLevelScene = scene;
		
		scene.setScale(this.mResolutionScale);
		scene.setX(this.mScaleInfo.getPaddingHorizontal());
		scene.setY(this.mScaleInfo.getPaddingVertical());
		this.mCamera.set(0, 0, this.mScaleInfo.getDeviceCameraWidth(),
				this.mScaleInfo.getDeviceCameraHeight());

		VertexBufferObjectManager vboManager = this.mEngine.getVertexBufferObjectManager();

		this.mLevelSceneHandler = new LevelSceneHandler(scene,
				vboManager,
				this.mSceneUIFont,
				this.mMiscFont,
				this.mBlockTiledTextureRegion,
				this.mArrowTiledTextureRegion,
				this.getBaseContext());


		this.mGameTypeHandler.init(this.mLevelSceneHandler);

		HUD hud = new HUD();
		hud.setY(this.mLevelScene.getY());
		//scaleX and scaleY are the same!!!
		hud.setScale(this.mResolutionScale);
		this.mCamera.setHUD(hud);
		scene.setBackground(new SpriteBackground(new Sprite(0,0,this.mScaleInfo.getDeviceCameraWidth(), 
				this.mScaleInfo.getDeviceCameraHeight(), 
				BlockBreakerActivity.this.mSceneBackgroundTextureRegion,
				vboManager)));

		this.mEngine.setScene(this.mLevelScene);
	}
	
	/**
	 * gets the shared preferences and changes some boolean variables according to that
	 */
	private void getPrefs() {
		// Get the xml/preferences.xml preferences
		// Don't save this in a constant, because it will only be used in code here
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		this.mUseOrientSensor = prefs.getBoolean("orient_sens_pref", true);
		this.mTimeAttackMode = prefs.getBoolean("time_attack_pref", false);
		this.mNumberOfTurns = Integer.parseInt(prefs.getString("number_of_turns_pref", "16"));
		this.mGameTypeHandler.setDifficulty(BaseGameTypeHandler.Difficulty.numberToDifficulty(
				Integer.parseInt(prefs.getString("difficulty_pref", "0"))));
		this.mPlayerName = prefs.getString("input_name_pref", "Player");
		if(this.mPlayerName.length() == 0) {
			this.mPlayerName = "Player";
		}
		if(this.mPlayerName.length() > 10) {
			this.mPlayerName = this.mPlayerName.substring(0, 10);
		}
		this.mHighscoreManager.ensureNameExistsInDB(this.mPlayerName);
	}
	
	public class BlockBreakerMessageAndroidView implements IBlockBreakerMessageView {

		@Override
		public void showExitDialog() {
			BlockBreakerActivity.this.showExitDialog();
		}

		@Override
		public void showFailDialogAndQuit(String pMessage) {
			BlockBreakerActivity.this.showFailDialogAndQuit(pMessage);
		}

		@Override
		public void showInputSeed(IInputSeedCallback pCallback) {
			this.showInputSeedDialog(BlockBreakerActivity.this.getString(R.string.input_seed_question),
					pCallback);
		}

		private void showInputSeedDialog(String pText, final IInputSeedCallback pCallback) {	
			FrameLayout fl = new FrameLayout(BlockBreakerActivity.this);
			final EditText input = new EditText(BlockBreakerActivity.this);
			input.setGravity(Gravity.CENTER);
			fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, 
					FrameLayout.LayoutParams.WRAP_CONTENT));

			AlertDialog.Builder builder = new AlertDialog.Builder(BlockBreakerActivity.this);
			builder.setMessage(pText)
			.setView(fl)
			.setCancelable(true)
			.setPositiveButton(BlockBreakerActivity.this.getString(R.string.ok), 
					new DialogInterface.OnClickListener() {
				
						@Override
						public void onClick(DialogInterface pDialog, int pId) {
							try {
								long seed = Long.parseLong(input.getText().toString());
								pCallback.onSeedChosen(seed);
								//FIXME: BlockBreakerActivity.this.randomLevelFromSeed(seed);
								pDialog.dismiss();
							} catch (NumberFormatException e) {
								BlockBreakerMessageAndroidView.this.showInputSeedDialog(
										BlockBreakerActivity.this.getString(R.string.wrong_seed_input_text),
										pCallback);					
							}
						}
						
			})
			.setNegativeButton(BlockBreakerActivity.this.getString(R.string.cancel),
					new DialogInterface.OnClickListener() {	
				
						@Override
						public void onClick(DialogInterface pDialog, int pId) {
							pDialog.dismiss();
						}
						
			});
			builder.create().show();
		}
		
	}

	void showExitDialog() {
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

	void showFailDialogAndQuit(String pMessage) {
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
	
	/**
	 * shows the tutorial to the user
	 */
	public void showTutorial() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getString(R.string.tutorial));
		builder.setMessage(this.getString(R.string.tutorial_text))
		.setCancelable(true)
		.setPositiveButton(BlockBreakerActivity.
				this.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface pDialog, int pId) {
						pDialog.dismiss();
					}
					
		});
		builder.create().show();
	}
	
	public class AndroidDefaultViewControl implements IDefaultViewControl {

		@Override
		public void showEndDialog(GameEndType pGameEnding,
				final IDefaultGameEndCallback pCallback) {
			String resString;
	
			switch(pGameEnding) {
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
					.setPositiveButton(BlockBreakerActivity.this.getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface pDialog, int pId) {
									pDialog.dismiss();
									pCallback.onEndDialogFinished(true);
								}
								
					})
					.setNegativeButton(BlockBreakerActivity.this.getString(R.string.no),
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface pDialog, int pId) {
									pCallback.onEndDialogFinished(false);
								}
								
					});
			builder.create().show();
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////////////
	/////////////////// Methods for TimeAttack			/////////////////////////
	/////////////////////////////////////////////////////////////////////////////
	
	public class AndroidTimeAttackViewControl implements ITimeAttackViewControl {
		
		private Text mTimeLeftText;
		private Text mStatusText;
	
		@Override
		public void showTimeAttackEndDialog(ITimeAttackEndDialogCallback pCallback) {
			
		}
	
		@Override
		public void showTimeAttackStartDialog(
				ITimeAttackStartDialogCallback pCallback) {
			
		}
	
		@Override
		public void setTimeLeft(float pTimeLeft) {
			this.mTimeLeftText.setText("" + (int)pTimeLeft);
		}
	
		@Override
		public void setScoreText(int pScore) {
			this.mStatusText.setText("Score: " + pScore);
		}
	
		@Override
		public void init() {
			this.mTimeLeftText = BlockBreakerActivity.this.mLevelSceneHandler.getTimeLeftText();
			this.mTimeLeftText.setVisible(true);
			if(this.mStatusText == null) {
				this.mStatusText = new Text(
					BlockBreakerActivity.this.mScaleInfo.getPaddingHorizontal() + 5,
					BlockBreakerActivity.this.mScaleInfo.getPaddingVertical() + 3,
					BlockBreakerActivity.this.mMiscFont,
					"",
					15,
					BlockBreakerActivity.this.getVertexBufferObjectManager());
				BlockBreakerActivity.this.mCamera.getHUD().attachChild(this.mStatusText);
			}
			this.mStatusText.setVisible(true);
		}
	
		@Override
		public void cleanUp() {
			this.mTimeLeftText.setVisible(false);
			this.mStatusText.setVisible(false);
		}
	}

}
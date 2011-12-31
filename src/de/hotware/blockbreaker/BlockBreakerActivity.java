package de.hotware.blockbreaker;

import java.io.InputStream;

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
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.hotware.blockbreaker.model.IGameEndListener;
import de.hotware.blockbreaker.model.IGameEndListener.GameEndEvent.GameEndType;
import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.LevelSerializer;
import de.hotware.blockbreaker.view.UIConstants;
import de.hotware.blockbreaker.view.LevelSceneFactory;

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
	    
    private Camera mCamera;
	private BitmapTextureAtlas mBlockBitmapTextureAtlas;
    private TiledTextureRegion mBlockTiledTextureRegion;
    private BitmapTextureAtlas mArrowBitmapTextureAtlas;
    private TiledTextureRegion mArrowTiledTextureRegion;
    private BitmapTextureAtlas mSceneBackgroundBitmapTextureAtlas;
    private TextureRegion mSceneBackgroundTextureRegion;
    private Level mLevel;
    private Font mFPSFont;
	private Font mSceneFont;
	private BitmapTextureAtlas mFPSFontTexture;
	private BitmapTextureAtlas mSceneFontTexture;
    
	////////////////////////////////////////////////////////////////////
	////					Overridden Methods						////
	////////////////////////////////////////////////////////////////////

	/**
	 * sets the EngineOptions to the needs of the game
	 */
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new FixedResolutionPolicy(UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT), this.mCamera);
        return engineOptions;
	}
	
	/**
	 * Load all the textures we need
	 * TODO maybe move this to LevelChooserActivity and
	 * 		send the textures to the activity instead of reloading them 
	 * 		everytime it is being restarted
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
	    this.mFPSFontTexture = new BitmapTextureAtlas(256,256, TextureOptions.BILINEAR);
		this.mEngine.getTextureManager().loadTexture(this.mFPSFontTexture);
	    FontFactory.setAssetBasePath("font/");
	    this.mFPSFont = FontFactory.createFromAsset(this.mFPSFontTexture, this, "Droid.ttf", 12, true, Color.BLACK);   	
		this.mEngine.getFontManager().loadFont(this.mFPSFont);
	    
		//loading scene font
		this.mSceneFontTexture = new BitmapTextureAtlas(256,256,TextureOptions.BILINEAR);
		this.mEngine.getTextureManager().loadTexture(this.mSceneFontTexture);
		this.mSceneFont = FontFactory.createFromAsset(this.mSceneFontTexture, this, "Plok.ttf", 18, true, Color.BLACK);
		this.mEngine.getFontManager().loadFont(this.mSceneFont);	
		
		pCallback.onCreateResourcesFinished();
	}
	
	/**
	 * initialization of the Activity is done here.
	 */
	@Override
	public void onCreateScene(OnCreateSceneCallback pCallback) {
		this.mEngine.registerUpdateHandler(new FPSLogger());
	        
        Bundle args = this.getIntent().getExtras();
        String levelPath = DEFAULT_LEVEL_PATH;
        boolean isAsset = false;
        
        if(args != null) {
        	levelPath = args.getString(LEVEL_ARG_KEY);
        	isAsset = args.getBoolean(IS_ASSET_KEY);
        }
        
        Scene sc = this.drawLevel(levelPath, isAsset);
        pCallback.onCreateSceneFinished(sc);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pCallback){
		pCallback.onPopulateSceneFinished();
	}
    
    @Override
    public void onResumeGame() {
        super.onResumeGame();
        this.enableOrientationSensor(this);
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
		switch(item.getItemId()) {
			case R.id.mainmenu:
			{
				this.showExitDialog();
				return true;
			}
			case R.id.restart:
			{
				this.setResult(RESULT_RESTART);
				this.finish();
				return true;
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
		if(USE_MENU_WORKAROUND
			&& pKeyCode == KeyEvent.KEYCODE_BACK 
			&& pEvent.getRepeatCount() == 0) {
			this.onBackPressed();
		}
		return super.onKeyDown(pKeyCode, pEvent);
	}
	
	/**
	 * If the user presses the back button he is asked if he wants to quit
	 */
	@Override
	public void onBackPressed() {
		this.showExitDialog();
	}
	
	////////////////////////////////////////////////////////////////////
	////					Private Methods							////
	////////////////////////////////////////////////////////////////////
	
	private Scene drawLevel(final String pLevelPath, final boolean pIsAsset) {
		try {
        	InputStream stream;
        	if(pIsAsset) {
	        	AssetManager assetManager = this.getResources().getAssets();
	        	stream = assetManager.open(pLevelPath);
	        	this.mLevel = LevelSerializer.readLevel(stream);
	        	//TODO add non asset stuff and move the readLevel part out of the if block
			}
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.getMessage() + "\nLeaving to main menu")
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                BlockBreakerActivity.this.setResult(RESULT_CANCELED);
			                BlockBreakerActivity.this.finish();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}
        
        Scene scene;
        if(this.mLevel != null) {       
	        this.mLevel.setGameEndListener(new IGameEndListener() {
				@Override
				public void onGameEnd(final GameEndEvent pEvt) {
					BlockBreakerActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							BlockBreakerActivity.this.showEndDialog(pEvt.getType() == GameEndType.WIN ? RESULT_WIN : RESULT_LOSE);
						}
					});							
				}
	        	
	        });
	        
	        scene = LevelSceneFactory.createLevelScene(this.mLevel,
	        	  	this.mEngine,
	        		this.mSceneFont, 
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
        } else {
        	scene = new Scene();
        }
        scene.setBackground(new SpriteBackground(new Sprite(0,0,UIConstants.LEVEL_WIDTH, UIConstants.LEVEL_HEIGHT, this.mSceneBackgroundTextureRegion)));
        return scene;
	}
	
	/**
	 * shows a Dialog which asks the user if he wants to exit the Activity
	 */
	private void showExitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Exit level?")
		       .setCancelable(true)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface pDialog, int id) {
		                pDialog.dismiss();
		        	   	BlockBreakerActivity.this.setResult(RESULT_CANCELED);
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
	
	private void showEndDialog(final int pResult) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Exit level?")
		       .setCancelable(true)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface pDialog, int pId) {
		                pDialog.dismiss();
		        	   	BlockBreakerActivity.this.setResult(pResult);
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
}

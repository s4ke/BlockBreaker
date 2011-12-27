package de.hotware.blockbreaker;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.content.Intent;

public class SplashScreenActivity extends SimpleBaseGameActivity{
	
	private static final float WAIT_TIME_SECONDS = 2.0F;
	private static final int WIDTH  = 480;
	private static final int HEIGHT = 320;
	
	private TextureRegion mLoadingScreenTextureRegion;

	@Override
	public EngineOptions onCreateEngineOptions() {
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
				new RatioResolutionPolicy(WIDTH,HEIGHT),
				new Camera(0,0,WIDTH,HEIGHT));
	}

	@Override
	protected void onCreateResources() {
		//Loading the Loading Screen splash
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
	    final BitmapTextureAtlas atlas = new BitmapTextureAtlas(960,640, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	    this.mLoadingScreenTextureRegion = (TextureRegion) BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, this, "splash.png", 0, 0);   
	    this.mEngine.getTextureManager().loadTexture(atlas);
	}

	@Override
	protected Scene onCreateScene() {
		final Scene scene = new Scene();
		scene.setBackground(new SpriteBackground(new Sprite(0,0,WIDTH, HEIGHT, this.mLoadingScreenTextureRegion)));
        scene.registerUpdateHandler(new TimerHandler(WAIT_TIME_SECONDS, new ITimerCallback() {
			
        	@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
        		Intent intent = new Intent(SplashScreenActivity.this, LevelChooserActivity.class);
				SplashScreenActivity.this.finish();
        		SplashScreenActivity.this.startActivity(intent);
			}
        	
        }));
		return scene;
	}

}

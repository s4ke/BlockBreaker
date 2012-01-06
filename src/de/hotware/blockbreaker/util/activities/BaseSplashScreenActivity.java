package de.hotware.blockbreaker.util.activities;

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

import android.app.Activity;
import android.content.Intent;

public abstract class BaseSplashScreenActivity extends SimpleBaseGameActivity{

	private TextureRegion mLoadingScreenTextureRegion;
	private int mWidth;
	private int mHeight;

	protected abstract float getWaitTime();
	protected abstract int getHeight();
	protected abstract int getWidth();
	protected abstract String getGfxImagePath();
	protected abstract ScreenOrientation getScreenOrientation();
	protected abstract Class<? extends Activity> getFollowUpActivity();

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mHeight = this.getHeight();
		this.mWidth =  this.getWidth();
		return new EngineOptions(true, this.getScreenOrientation(), 
				new RatioResolutionPolicy(this.mWidth,this.mHeight),
				new Camera(0,0,this.mWidth,this.mHeight));
	}

	@Override
	protected void onCreateResources() {
		//Loading the Loading Screen splash
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		final BitmapTextureAtlas atlas = new BitmapTextureAtlas(960,640, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mLoadingScreenTextureRegion = (TextureRegion) BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, this, this.getGfxImagePath(), 0, 0);   
		this.mEngine.getTextureManager().loadTexture(atlas);
	}

	@Override
	protected Scene onCreateScene() {
		final Scene scene = new Scene();
		scene.setBackground(new SpriteBackground(new Sprite(0,0,this.mWidth, this.mHeight, this.mLoadingScreenTextureRegion)));
		scene.registerUpdateHandler(new TimerHandler(this.getWaitTime(), new ITimerCallback() {

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				Intent intent = new Intent(BaseSplashScreenActivity.this, BaseSplashScreenActivity.this.getFollowUpActivity());
				BaseSplashScreenActivity.this.finish();
				BaseSplashScreenActivity.this.startActivity(intent);
			}

		}));
		return scene;
	}
	
	@Override
	public void onResumeGame() {
		if(this.mEngine != null) {
			super.onResumeGame();
		}
	}

}

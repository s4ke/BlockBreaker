package de.hotware.blockbreaker.android;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.res.AssetManager;
import android.graphics.Color;

import de.hotware.blockbreaker.android.andengine.extension.StretchedResolutionPolicy;
import de.hotware.blockbreaker.android.andengine.extension.StretchedResolutionPolicy.ScaleInfo;
import de.hotware.blockbreaker.android.view.UIConstants;

public class BlockBreakerMenuActivity extends BaseGameActivity {

	Camera mCamera;
	ScaleInfo mScaleInfo;
	Font mSceneUIFont;

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

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
		FontManager fontManager = this.mEngine.getFontManager();
		TextureManager textureManager = this.mEngine.getTextureManager();
		AssetManager assetManager = this.getAssets();
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
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
		
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) {
		
	}

}

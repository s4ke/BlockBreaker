package de.hotware.blockbreaker;

import org.andengine.engine.options.EngineOptions.ScreenOrientation;

import android.app.Activity;

import de.hotware.blockbreaker.util.activities.BaseSplashScreenActivity;

public class BlockBreakerLogoSplashScreenActivity extends BaseSplashScreenActivity{

	private static final float WAIT_TIME_SECONDS = 2.0F;
	private static final int WIDTH  = 480;
	private static final int HEIGHT = 320;

	@Override
	protected float getWaitTime() {
		return WAIT_TIME_SECONDS;
	}

	@Override
	protected int getHeight() {
		return HEIGHT;
	}

	@Override
	protected int getWidth() {
		return WIDTH;
	}

	@Override
	protected String getGfxImagePath() {
		return "bbsplash.png";
	}

	@Override
	protected ScreenOrientation getScreenOrientation() {
		return ScreenOrientation.LANDSCAPE_FIXED;
	}

	@Override
	protected Class<? extends Activity> getFollowUpActivity() {
		return LevelChooserActivity.class;
	}	
}

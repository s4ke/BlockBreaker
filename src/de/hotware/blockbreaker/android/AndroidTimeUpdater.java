package de.hotware.blockbreaker.android;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;

import de.hotware.blockbreaker.model.gamehandler.ITimeUpdater;

public class AndroidTimeUpdater implements ITimeUpdater, ITimerCallback {
	
	protected Engine mEngine;
	protected TimerHandler mTimerHandler;
	protected TimerHandler mUpdateTimerHandler;
	protected float mTime;
	protected float mUpdateTime;
	protected ITimePassedCallback mCallback;
	protected boolean mPaused;
	protected boolean mStarted;
	
	public AndroidTimeUpdater() {
		this(null);
	}
	
	public AndroidTimeUpdater(Engine pEngine) {
		this.mPaused = false;
		this.mStarted = false;
		this.mEngine = pEngine;
		this.mTimerHandler = new TimerHandler(0, this);
		this.mUpdateTimerHandler = new TimerHandler(0, true, this);
	}

	@Override
	public void setTimePassedCallback(ITimePassedCallback pCallback) {
		if(this.mStarted) {
			throw new IllegalStateException(AndroidTimeUpdater.class.toString() + 
					".setUpdateTime(float): The Updating has already been started!");
		}
		this.mCallback = pCallback;
	}

	@Override
	public void setTime(float pTime) {
		if(this.mStarted) {
			throw new IllegalStateException(AndroidTimeUpdater.class.toString() + 
					".setUpdateTime(float): The Updating has already been started!");
		}
		this.mTime = pTime;
	}

	@Override
	public void setUpdateTime(float pUpdateTime) {
		if(this.mStarted) {
			throw new IllegalStateException(AndroidTimeUpdater.class.toString() + 
					".setUpdateTime(float): The Updating has already been started!");
		}
		this.mUpdateTime = pUpdateTime;
	}

	@Override
	public void reset() {
		this.mStarted = false;
		this.mPaused = false;
		this.mUpdateTimerHandler.setAutoReset(true);
		this.mUpdateTimerHandler.reset();
		this.mTimerHandler.reset();
	}

	@Override
	public void start() {
		if(!this.mPaused) {
			this.mStarted = true;
			this.mTimerHandler.setTimerSeconds(this.mTime);
			this.mUpdateTimerHandler.setTimerSeconds(this.mUpdateTime);
		}
		this.registerHandlers();
	}

	@Override
	public void pause() {
		if(this.mStarted) {
			this.unregisterHandlers();
			this.mPaused = true;
		}
	}

	@Override
	public void stop() {
		if(this.mStarted) {
			this.unregisterHandlers();
			this.reset();
		}
	}

	@Override
	public void onTimePassed(TimerHandler pTimerHandler) {
		if(pTimerHandler == this.mTimerHandler) {
			this.mCallback.onTimeEnd();
			this.stop();
		} else {
			this.mCallback.onTimePassed((int)this.mTimerHandler.getTimerSecondsElapsed());
		}
	}
	
	public void setEngine(Engine pEngine) {
		this.mEngine = pEngine;
	}
	
	private void unregisterHandlers() {
		this.mEngine.unregisterUpdateHandler(this.mUpdateTimerHandler);
		this.mEngine.unregisterUpdateHandler(this.mTimerHandler);
	}
	
	private void registerHandlers() {
		if(this.mUpdateTime > 0.0) {
			this.mEngine.registerUpdateHandler(this.mUpdateTimerHandler);
		}
		this.mEngine.registerUpdateHandler(this.mTimerHandler);
	}

}

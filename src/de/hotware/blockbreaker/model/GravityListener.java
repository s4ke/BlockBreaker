package de.hotware.blockbreaker.model;

import de.hotware.blockbreaker.model.Level.Gravity;

public interface GravityListener {
	
	public void onGravityChanged(GravityEvent pEvt);
	
	public class GravityEvent {
		private Level mSource;
		private Gravity mGravity;
		public GravityEvent(Level pSource, Gravity pGravity) {
			this.mSource = pSource;
			this.mGravity = pGravity;
		}
		public Level getSource() {
			return this.mSource;
		}
		public Gravity getGravity() {
			return this.mGravity;
		}
	}

}

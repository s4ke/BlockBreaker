package de.hotware.blockbreaker.model.listeners;

import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.model.Level.Gravity;
import de.hotware.blockbreaker.util.misc.GBaseEvent;

public interface IGravityListener {

	public void onGravityChanged(GravityEvent pEvt);

	public class GravityEvent extends GBaseEvent<Level>{

		private Gravity mGravity;

		public GravityEvent(Level pSource, Gravity pGravity) {
			super(pSource);
			this.mGravity = pGravity;
		}

		public Gravity getGravity() {
			return this.mGravity;
		}
	}

}

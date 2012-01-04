package de.hotware.blockbreaker.model.listeners;

import de.hotware.blockbreaker.model.Level;
import de.hotware.blockbreaker.util.misc.GBaseEvent;

public interface IGameEndListener {
	/**
	 * Listener method for Game End
	 * @param pEvt the corresponding GameEndEvent
	 */
	public void onGameEnd(GameEndEvent pEvt);

	/**
	 * Event for storing the the EndType
	 * @author Martin Braun
	 */
	public class GameEndEvent extends GBaseEvent<Level>{

		private GameEndType mType;

		public GameEndEvent(Level pSource, GameEndType pType) {
			super(pSource);
			this.mType = pType;
		}		

		public GameEndType getType() {
			return this.mType;
		}

		public enum GameEndType{
			WIN,
			LOSE;
			public String toString() {
				switch(this) {
					case WIN:
						return "You win!";
					case LOSE:
						return "You lose!";
					default:
						return "WTF?";
				}
			}
		}
	}
}

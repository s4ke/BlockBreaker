package de.hotware.blockbreaker.model;

public interface GameEndListener {
	/**
	 * Listener method for Game End
	 * @param pEvt the corresponding GameEndEvent
	 */
	public void onGameEnd(GameEndEvent pEvt);
	/**
	 * Event for storing the the EndType
	 * @author Martin Braun
	 */
	public class GameEndEvent {
		public enum GameEndType{
			WIN,
			LOSE
		}
		private Level mSource;
		private GameEndType mType;
		public GameEndEvent(Level pSource, GameEndType pType) {
			this.mSource = pSource;
			this.mType = pType;
		}		
		public Level getSource() {
			return this.mSource;
		}
		public GameEndType getType() {
			return this.mType;
		}
	}
}

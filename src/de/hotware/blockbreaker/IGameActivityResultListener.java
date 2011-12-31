package de.hotware.blockbreaker;

import de.hotware.blockbreaker.util.misc.GBaseEvent;

public interface IGameActivityResultListener {

	public void onGameActivityResult(GameActivityResultEvent pEvt);
	
	public class GameActivityResultEvent extends GBaseEvent<BlockBreakerActivity>{
		
		private GameActivityResultType mType;
		
		public GameActivityResultEvent(BlockBreakerActivity pSource, GameActivityResultType pType) {
			super(pSource);
			this.mType = pType;
		}
		
		public GameActivityResultType getType() {
			return this.mType;
		}
		
		public enum GameActivityResultType{
			RESULT_CANCELED,
			RESULT_RESTART,
			RESULT_WIN,
			RESULT_LOSE,
			RESULT_ERROR;
		}
	}

}

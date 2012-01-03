package de.hotware.blockbreaker.model.gen;

import de.hotware.blockbreaker.model.Level;

@Deprecated
@SuppressWarnings("unused")
public class LevelGenerator {
	/**
	 * Don't use this, yet
	 */
	private static boolean hasSolution(Level pLevel) {
		Level copy = pLevel.clone();
		int count = 0;
		for(int i = 0; i < 6; ++i) {
			for(int j = 0; j < 6; ++j, ++count) {
				copy.killBlock(i, j);
			}
		}		
		return false;
	}
}

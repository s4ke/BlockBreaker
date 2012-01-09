package de.hotware.blockbreaker.util.misc;

import java.util.Random;

public class Randomizer{
	
	private static final Random RANDOM = new Random();
	
	public static int nextInt(int pX) {
		return RANDOM.nextInt(pX);
	}

}

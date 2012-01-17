package de.hotware.blockbreaker.util.misc;

import java.util.Random;

public class Randomizer {
	
	private static Random sRandom = new Random();
	
	public static void setSeed(long pSeed) {
		sRandom.setSeed(pSeed);
	}
	
	public static void newRandomObject() {
		sRandom = new Random();
	}
	
	public static int nextInt(int pX) {
		return sRandom.nextInt(pX);
	}

}

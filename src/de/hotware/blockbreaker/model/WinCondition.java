package de.hotware.blockbreaker.model;

import java.io.Serializable;

public class WinCondition implements Serializable, Cloneable {
	private static final long serialVersionUID = 191894878776646913L;

	private int[] mWin;

	public WinCondition(int pBlue,
			int pGreen,
			int pRed,
			int pYellow,
			int pPurple) {
		this.mWin = new int[5];
		this.mWin[0] = pBlue;
		this.mWin[1] = pGreen;
		this.mWin[2] = pRed;
		this.mWin[3] = pYellow;
		this.mWin[4] = pPurple;
	}

	private WinCondition(int[] pWin) {
		this.mWin = pWin;
	}

	public int getWinCount(int pColorNumber) {
		return this.mWin[pColorNumber - 1];
	}
	
	public int getTotalWinCount() {
		int ret = 0;
		for(int i = 0; i < this.mWin.length; ++i) {
			ret += this.mWin[i];
		}
		return ret;
	}

	@Override
	public WinCondition clone() {
		return new WinCondition(this.mWin.clone());
	}
	
	public int[] getWinClone() {
		return this.mWin.clone();
	}
}

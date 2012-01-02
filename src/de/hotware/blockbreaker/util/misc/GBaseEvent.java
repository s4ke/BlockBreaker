package de.hotware.blockbreaker.util.misc;

public abstract class GBaseEvent<T> {
	protected T mSource;

	public GBaseEvent(T pSource) {
		this.mSource = pSource;
	}

	public T getSource() {
		return this.mSource;
	}
}

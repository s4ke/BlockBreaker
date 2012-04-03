package de.hotware.blockbreaker.android.andengine.extension;

import java.util.Comparator;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.util.GLState;
import org.andengine.util.adt.list.SmartList;

/**
 * Scene in which sprites can be reinserted, rather than sorted,
 * does this in a synchronized way. Sorting can't be done via
 * the sortChildren Methods, those are just dummies
 * @author Martin Braun
 */
public class ZIndexScene extends Scene {
	
	public ZIndexScene() {
		super();
	}
	
	public ZIndexScene(int pMinimumCapacity) {
		super();
		this.mChildren = new SmartList<IEntity>(pMinimumCapacity);
	}
	
	public synchronized void ensureCapacity(int pMinimumCapacity) {
		this.mChildren.ensureCapacity(pMinimumCapacity);
	}
	
	public synchronized void reInsertAtTop(IEntity pEntity) {
		if(this.mChildren.remove(pEntity)) {
			this.mChildren.add(pEntity);
		} else {
			throw new IllegalStateException(ZIndexScene.class.toString() +
				".reInsertAtTop(IEntity): pEntity isn't attached, yet");
		}
	}
	
	public synchronized void insertAtTop(IEntity pEntity) {
		this.mChildren.add(pEntity);
	}
	
	public synchronized void reInsertAtBottom(IEntity pEntity) {
		this.reInsertAt(0, pEntity);
	}
	
	public synchronized void insertAtBottom(IEntity pEntity) {
		this.insertAt(0, pEntity);
	}
	
	public synchronized void reInsertAt(int pX, IEntity pEntity) {
		if(this.mChildren.remove(pEntity)) {
			this.mChildren.add(pX, pEntity);
		} else {
			throw new IllegalStateException(ZIndexScene.class.toString() +
					".reInsertAt(int, IEntity): pEntity isn't attached, yet");
		}
	}
	
	public synchronized void insertAt(int pX, IEntity pEntity) {
		this.mChildren.add(pX, pEntity);
	}
	
	@Override
	public synchronized void onManagedDraw(GLState pGLState, Camera pCamera) {
		super.onManagedDraw(pGLState, pCamera);
	}
	
	@Override
	public synchronized void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
	}
	
	/**
	 * not supported in this class
	 */
	@Override
	public void sortChildren() {
		throw new UnsupportedOperationException(ZIndexScene.class.toString() +
				".sortChildren(): This Scene can't sort its children");
	} 
	
	/**
	 * not supported in this class
	 */
	@Override
	public void sortChildren(boolean pImmediate) {
		throw new UnsupportedOperationException(ZIndexScene.class.toString() + 
				".sortChildren(boolean): This Scene can't sort its children");
	}
	
	/**
	 * not supported in this class
	 */
	@Override
	public void sortChildren(Comparator<IEntity> pEntityComparator) { 
		throw new UnsupportedOperationException(ZIndexScene.class.toString() + 
				".sortChildren(Comparator<IEntity>): This Scene can't sort its children");
	}
	
}
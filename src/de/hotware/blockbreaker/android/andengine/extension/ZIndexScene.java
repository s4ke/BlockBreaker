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
 * TODO: Check if Entities already have a parent?
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
		pEntity.setParent(this);
		pEntity.onAttached();
	}
	
	public synchronized void reInsertAtBottom(IEntity pEntity) {
		if(this.mChildren.remove(pEntity)) {
			this.mChildren.add(0, pEntity);
		} else {
			throw new IllegalStateException(ZIndexScene.class.toString() +
				".reInsertAtBottom(IEntity): pEntity isn't attached, yet");
		}
	}
	
	public synchronized void insertAtBottom(IEntity pEntity) {
		this.mChildren.add(0, pEntity);
		pEntity.setParent(this);
		pEntity.onAttached();
	}
	
	/**
	 * reInserts the block at the given Position. If the given
	 * Position is bigger than the the current size or less than zero
	 * the insertion fails
	 * @param pX
	 * @param pEntity
	 */
	public synchronized boolean reInsertAt(int pX, IEntity pEntity) {
		if(this.mChildren.remove(pEntity)) {
			try {
				this.mChildren.add(pX, pEntity);
				return true;
			} catch (IndexOutOfBoundsException e) {
				return false;
			}
		} else {
			throw new IllegalStateException(ZIndexScene.class.toString() +
					".reInsertAt(int, IEntity): pEntity isn't attached, yet");
		}
	}
	
	/**
	 * inserts the block at the given Position. If the given
	 * Position is bigger than the the current size or less than zero
	 * the insertion fails
	 * @param pX
	 * @param pEntity
	 */
	public synchronized boolean insertAt(int pX, IEntity pEntity) {
		try {
			this.mChildren.add(pX, pEntity);
			pEntity.setParent(this);
			pEntity.onAttached();
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
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
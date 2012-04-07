package de.hotware.blockbreaker.android.andengine.extension;

import org.andengine.engine.options.resolutionpolicy.BaseResolutionPolicy;
import org.andengine.opengl.view.RenderSurfaceView;

import android.view.View.MeasureSpec;


/**
 * ResolutionPolicy that doesn't scale at all but saves some data
 * for resizing more easily
 * @author Martin Braun
 */
public class StretchedResolutionPolicy extends BaseResolutionPolicy {
 
        private float mCameraWidth;
        private float mCameraHeight;
        private float mRatio;
        private float mScale;
        private float mDeviceCameraWidth;
        private float mDeviceCameraHeight;
        
    	/**
    	 * Pixels returns the padding for each top and bottom
    	 */
        private float mPaddingVertical;
        /**
         * padding for each left and right
         */
        private float mPaddingHorizontal;
 
        public StretchedResolutionPolicy(float pCameraWidth,
        		float pCameraHeight) {
            this.mCameraWidth = pCameraWidth;
            this.mCameraHeight = pCameraHeight;
            this.mRatio = this.mCameraWidth / pCameraHeight;
           
            this.mPaddingVertical = 0;
            this.mPaddingHorizontal = 0;
        }
       
        /**
    	 * @return returns the padding for each top and bottom
		*/
        public float getPaddingVertical() {
        	return this.mPaddingVertical;
        }
 
        /**
    	 * @return returns the padding for each left and right
    	 */
        public float getPaddingHorizontal() {
        	return this.mPaddingHorizontal;
        }
        
        /**
         * @return the scale that can be used for resizing properly
         */
        public float getScale() {
        	return this.mScale;
        }
        
        public float getDeviceCameraWidth() {
        	return this.mDeviceCameraWidth;
        }
        
        public float getDeviceCameraHeight() {
        	return this.mDeviceCameraHeight;
        }
        
        @Override
        public void onMeasure(RenderSurfaceView pRenderSurfaceView,
        		int pWidthMeasureSpec,
        		int pHeightMeasureSpec) {
        	BaseResolutionPolicy.throwOnNotMeasureSpecEXACTLY(pWidthMeasureSpec, pHeightMeasureSpec);
        	
        	int specWidth = MeasureSpec.getSize(pWidthMeasureSpec);
    		int specHeight = MeasureSpec.getSize(pHeightMeasureSpec);

    		this.mDeviceCameraWidth = specWidth;
    		this.mDeviceCameraHeight = specHeight;
    		
    		float desiredRatio = this.mRatio;
    		float realRatio = (float)specWidth / specHeight;
    		
    		if(realRatio > desiredRatio) {
    			this.mPaddingHorizontal = 
    					(specWidth - (this.mScale = specHeight / this.mCameraHeight) * this.mCameraWidth) / 2;
    			this.mPaddingVertical = 0;
    		} else if (realRatio < desiredRatio) {
    			this.mPaddingVertical = 
    					(specHeight - (this.mScale = specWidth / this.mCameraWidth) * this.mCameraHeight) / 2;
    			this.mPaddingHorizontal = 0;
    		} else {
    			this.mScale = 1.0f;
    		}
    		
    		pRenderSurfaceView.setMeasuredDimensionProxy(specWidth, specHeight);
        }
        
}
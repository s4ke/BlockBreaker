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
	    private ScaleInfo mScaleInfo;
	
	    /**
	     * @param pCameraWidth
	     * @param pCameraHeight
	     * @param pScaleInfo Object that will contain all information about the scale
	     */
        public StretchedResolutionPolicy(float pCameraWidth,
        		float pCameraHeight,
        		ScaleInfo pScaleInfo) {
            this.mCameraWidth = pCameraWidth;
            this.mCameraHeight = pCameraHeight;
            this.mRatio = this.mCameraWidth / pCameraHeight;
            this.mScaleInfo = pScaleInfo;
        }
        
        @Override
        public void onMeasure(RenderSurfaceView pRenderSurfaceView,
        		int pWidthMeasureSpec,
        		int pHeightMeasureSpec) {
        	BaseResolutionPolicy.throwOnNotMeasureSpecEXACTLY(pWidthMeasureSpec, pHeightMeasureSpec);
        	
        	int specWidth = MeasureSpec.getSize(pWidthMeasureSpec);
    		int specHeight = MeasureSpec.getSize(pHeightMeasureSpec);

    		this.mScaleInfo.mDeviceCameraWidth = specWidth;
    		this.mScaleInfo.mDeviceCameraHeight = specHeight;
    		
    		float desiredRatio = this.mRatio;
    		float realRatio = (float)specWidth / specHeight;
    		
    		if(realRatio > desiredRatio) {
    			this.mScaleInfo.mPaddingHorizontal = 
    					(specWidth - (this.mScaleInfo.mScale = specHeight / this.mCameraHeight)
    							* this.mCameraWidth) / 2;
    			this.mScaleInfo.mPaddingVertical = 0;
    		} else if (realRatio < desiredRatio) {
    			this.mScaleInfo.mPaddingVertical = 
    					(specHeight - (this.mScaleInfo.mScale = specWidth / this.mCameraWidth)
    							* this.mCameraHeight) / 2;
    			this.mScaleInfo.mPaddingHorizontal = 0;
    		} else {
    			this.mScaleInfo.mScale = 1.0f;
    		}
    		
    		pRenderSurfaceView.setMeasuredDimensionProxy(specWidth, specHeight);
        }
        
        /**
         * Wrapper class for the ScaleInfo so the user of StretchedResolutionPolicy
         * doesn't have to use the bulky StretchedResolutionPolicy class everywhere
         */
        public static class ScaleInfo {
        	
        	public ScaleInfo() {
        		this.mPaddingVertical = 0.0f;
        		this.mPaddingHorizontal = 0.0f;
        		this.mScale = 0.0f;
        	}
        	
            private float mScale;
            private float mDeviceCameraWidth;
            private float mDeviceCameraHeight;
            
            /**
             * @return the padding for each top and bottom
             */
            private float mPaddingVertical;
            /**
             * @return padding for each left and right
             */
            private float mPaddingHorizontal;
            
            /**
        	 * @return the padding for each top and bottom
    		*/
            public float getPaddingVertical() {
            	return this.mPaddingVertical;
            }
     
            /**
        	 * @return the padding for each left and right
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
            
            /**
             * @return the devices CameraWidth
             */
            public float getDeviceCameraWidth() {
            	return this.mDeviceCameraWidth;
            }
            
            /**
             * @return the devices CameraHeight
             */
            public float getDeviceCameraHeight() {
            	return this.mDeviceCameraHeight;
            }
            
        }
        
}
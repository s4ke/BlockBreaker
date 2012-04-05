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
        private float mDeviceRatio;
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
 
        public StretchedResolutionPolicy(float pCameraWidth, float pCameraHeight) {   
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
        
        public float getDeviceRatio() {
        	return this.mDeviceRatio;
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
    					(specWidth - (this.mDeviceRatio = specHeight / this.mCameraHeight) * this.mCameraWidth) / 2;
    			this.mPaddingVertical = 0;
    		} else if (realRatio < desiredRatio) {
    			this.mPaddingVertical = 
    					(specHeight - (this.mDeviceRatio = specWidth / this.mCameraWidth) * this.mCameraHeight) / 2;
    			this.mPaddingHorizontal = 0;
    		}
    		
    		pRenderSurfaceView.setMeasuredDimensionProxy(specWidth, specHeight);
        }
 
 
//        @Override
//        public void onMeasure(RenderSurfaceView pRenderSurfaceView,
//        		int pWidthMeasureSpec,
//        		int pHeightMeasureSpec) {
//                BaseResolutionPolicy.throwOnNotMeasureSpecEXACTLY(pWidthMeasureSpec, pHeightMeasureSpec);
// 
//                float measuredWidth = MeasureSpec.getSize(pWidthMeasureSpec);
//                float measuredHeight = MeasureSpec.getSize(pHeightMeasureSpec);
//                
//                float nCamRatio = this.mCameraWidth / this.mCameraHeight;
//                float nCanvasRatio = measuredWidth / measuredHeight;
//                
//                if(nCanvasRatio < nCamRatio) {
//                        // Scale to fit height, width will crop
//                        measuredWidth = measuredHeight * nCamRatio;
//                        this.mMarginHorizontal = (this.mCameraWidth - this.mCameraHeight * nCanvasRatio) / 2.0f;
//                } else if(nCanvasRatio > nCamRatio){
//                        // Scale to fit width, height will crop
//                        measuredHeight = measuredWidth / nCamRatio;
//                        this.mMarginVertical = (this.mCameraHeight - this.mCameraWidth / nCanvasRatio) / 2.0f;
//                }
//                pRenderSurfaceView.setMeasuredDimensionProxy((int)measuredWidth, (int)measuredHeight);
//        }
        
}
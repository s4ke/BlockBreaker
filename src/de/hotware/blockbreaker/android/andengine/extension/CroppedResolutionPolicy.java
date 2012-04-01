package de.hotware.blockbreaker.android.andengine.extension;

import org.andengine.engine.options.resolutionpolicy.BaseResolutionPolicy;
import org.andengine.opengl.view.RenderSurfaceView;
 
import android.view.View.MeasureSpec;

/**
 * @author initial from andengine forums (jgibbs), changes by Martin Braun
 */
public class CroppedResolutionPolicy extends BaseResolutionPolicy {
 
        private final float mCameraWidth;
        private final float mCameraHeight;
    	/**
    	 * Pixels from top of canvas to visible area, and from bottom of canvas to visible area
    	 */
        private float mMarginVertical;
        /**
         * Pixels from left of canvas to visible area, and from right of canvas to visible area
         */
        private float mMarginHorizontal;
 
        public CroppedResolutionPolicy(float pCameraWidth, float pCameraHeight)
        {
                this.mCameraWidth = pCameraWidth;
                this.mCameraHeight = pCameraHeight;
               
                this.mMarginVertical = 0;
                this.mMarginHorizontal = 0;
        }
       
        /**
    	 * @return Pixels from top of canvas to visible area, and from bottom of canvas to visible area
    	 */
        public float getMarginVertical() {
                return this.mMarginVertical;
        }
 
        /**
         * @return Pixels from left of canvas to visible area, and from right of canvas to visible area
         */
        public float getMarginHorizontal() {
                return this.mMarginHorizontal;
        }
 
 
        @Override
        public void onMeasure(RenderSurfaceView pRenderSurfaceView, int pWidthMeasureSpec, int pHeightMeasureSpec) {
                BaseResolutionPolicy.throwOnNotMeasureSpecEXACTLY(pWidthMeasureSpec, pHeightMeasureSpec);
 
                float measuredWidth = MeasureSpec.getSize(pWidthMeasureSpec);
                float measuredHeight = MeasureSpec.getSize(pHeightMeasureSpec);
                
                float nCamRatio = this.mCameraWidth / this.mCameraHeight;
                float nCanvasRatio = measuredWidth / measuredHeight;
               
                if(measuredWidth / measuredHeight < nCamRatio ) {
                        // Scale to fit height, width will crop
                        measuredWidth =measuredHeight * nCamRatio;
                        this.mMarginHorizontal = (this.mCameraWidth - this.mCameraHeight * nCanvasRatio) / 2.0f;
                } else {
                        // Scale to fit width, height will crop
                        measuredHeight = measuredWidth / nCamRatio;
                        this.mMarginVertical = (this.mCameraHeight - this.mCameraWidth / nCanvasRatio) / 2.0f;
                }
                pRenderSurfaceView.setMeasuredDimensionProxy((int)measuredWidth, (int)measuredHeight);
        }
        
}
package de.hotware.blockbreaker.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.andengine.extension.svg.opengl.texture.atlas.bitmap.source.SVGAssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.bitmap.source.PictureBitmapTextureAtlasSource;

import de.hotware.blockbreaker.util.misc.StreamUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class TextureUtil {
	
	/**
	 * renders a SVG and then saves it to the given Path
	 * @param pContext
	 * @param pAssetPath
	 * @param pWidth
	 * @param pHeight
	 * @param pFilePath
	 * @param pFileName
	 * @throws IOException
	 */
	public static void saveSVGToPNG(Context pContext,
			String pAssetPath,
			int pWidth,
			int pHeight,
			String pFilePath,
			String pFileName) throws IOException {
		
	    PictureBitmapTextureAtlasSource textureSource = new SVGAssetBitmapTextureAtlasSource(pContext,
	    		pAssetPath,
	    		pWidth,
	    		pHeight);
	    
	    Bitmap bitmap = textureSource.onLoadBitmap(Config.ARGB_8888);
        File dir = new File(pFilePath);
        dir.mkdirs();
        File file = new File(dir, pFileName);
        
        FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
	        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
	        out.flush();
		} finally {
			StreamUtil.closeQuietly(out);
		}
	}
	
}


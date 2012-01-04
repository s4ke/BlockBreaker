package de.hotware.blockbreaker.model.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.hotware.blockbreaker.model.Level;

public class LevelSerializer {

	public static void saveLevel(Level pLevel, String pPath) throws IOException {
		//write an level without any listeners!
		Level save = pLevel.clone();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		fos = new FileOutputStream(pPath);
		out = new ObjectOutputStream(fos);
		out.writeObject(save);
		out.close();
	}

	public static Level readLevel(InputStream is) throws IOException, ClassNotFoundException{
		ObjectInputStream in = null;
		Level ret = null;
		in = new ObjectInputStream(is);
		ret = (Level) in.readObject();
		in.close();
		return ret;
	}
}

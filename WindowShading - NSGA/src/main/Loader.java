package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Loader
{
	private static String filename = "solutions.bin";
	
	public static double[][] load() 
	{
		double[][] sols = null;
		
		try (ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(new File(filename))))
		{
			sols = (double[][]) ois.readObject();
			ois.close();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		
		return sols;
	}
}

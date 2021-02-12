package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * File loader class.
 * 
 * @author Ernest Vanmosuinck
 */
public class Loader
{
	/** The file name to extract the data from. */
	private static String filename = "solutions.bin";
	
	/**
	 * This method will load the 2D array stored in a project binary file. 
	 * The file name and location is preset by the program.
	 * 
	 * @return 2D array of pre-evaluated solutions.
	 */
	public static double[][] load()  
	{
		double[][] sols = null;
		
		try (ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(new File(filename))))
		{
			sols = (double[][]) ois.readObject();
			ois.close();
		}
		catch (IOException | ClassNotFoundException e) 
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return sols;
	}
}

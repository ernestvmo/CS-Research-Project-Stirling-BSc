package regression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

import Optimisation.NSGA2_E;

public class RegressionMain
{
	public static void main(String[] args)
	{
		double[][] set = loadSolutions("solutions.bin");
		
//		System.out.println(set.length);
		
		Model model = new Model(set);
		model.build();
		
		System.out.println("** MLP **");
		System.out.println(model.getEvaluation().toSummaryString());
		
//		boolean[] alleles = new boolean[120];
//		for (int i = 0; i < alleles.length; i++) {
//			alleles[i] = new Random().nextBoolean();
//		}
//		
//		System.out.println(model.predict(alleles));
		
		
		NSGA2_E nsga = new NSGA2_E();
		nsga.setModel(model);
		nsga.go();
	}
	
	
	private static double[][] loadSolutions(String filename)
	{
		double[][] sols = null;
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(filename))))
		{
			sols = (double[][]) ois.readObject();
		}
		catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
		}
//		finally
//		{
//			ois.close();
//		}
		
		return sols;
	}
}
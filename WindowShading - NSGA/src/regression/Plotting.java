package regression;

import java.util.ArrayList;
import java.util.List;

import Optimisation.Individual;

public class Plotting 
{
	public static double hypervolume(Individual[] lastGen)
	{
		List<Individual> paretoOptimals = new ArrayList<>();
		
		for (Individual i : lastGen)
			if (i.rank == 0)
				paretoOptimals.add(i);
		
		double[][] paretoPoints = new double[paretoOptimals.size()][2];
		for (int i = 0; i < paretoOptimals.size(); i++)
		{
			paretoPoints[i][0] = paretoOptimals.get(i).getFitness1();
			paretoPoints[i][1] = paretoOptimals.get(i).getFitness2();
		}
		
		for(double[] pp : paretoPoints) {
			for (double p : pp)
				System.out.print(p + " ");
			System.out.println();
		}
		
		double[] referencePoint = {50000, 100000};
		
		
		double volume = 0;
		
		for (int i = 0; i < paretoPoints.length; i++) 
		{
			volume += ((referencePoint[0] - paretoPoints[i][0]) * (referencePoint[1] - paretoPoints[i][1]));
			referencePoint[0] -= referencePoint[0] - paretoPoints[i][0];
		}
		
		return volume;
	}
}

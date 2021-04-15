package plotting;

import java.util.ArrayList;
import java.util.List;

import Optimisation.Individual;

public class HyperVolume
{
	/**
	 * This method calculates the hypervolume of the passed population from a set reference point.
	 * The reference point is set to {100000, 50000}, as those two values are way off the possible values of the energy and cost. 
	 * 
	 * @param population The population to calculate the hypervolume of.
	 * @return The calculated hypervolume.
	 */
	public static double hypervolume(Individual[] population)
	{
		List<Individual> paretoOptimals = new ArrayList<>();

		for (Individual i : population)
			if (i.rank == 0)// save all pareto-optimal points from the population
				paretoOptimals.add(i);

		double[][] paretoPoints = new double[paretoOptimals.size()][2];
		for (int i = 0; i < paretoOptimals.size(); i++) 
		{// save the energy and cost of the pareto-optimal points
			paretoPoints[i][0] = paretoOptimals.get(i).getFitness1();
			paretoPoints[i][1] = paretoOptimals.get(i).getFitness2();
		}

		// reference point out of the possible bounds
		double[] referencePoint = {50000, 50000};
		double volume = 0;
		
		for (int i = 0; i < paretoPoints.length; i++)
		{
			volume += ((referencePoint[0] - paretoPoints[i][0])  // calculate the area from the pareto
					* (referencePoint[1] - paretoPoints[i][1])); // point and the reference point.
			referencePoint[0] -= referencePoint[0] - paretoPoints[i][0]; // move the reference point.
		}

		return volume;
	}
}
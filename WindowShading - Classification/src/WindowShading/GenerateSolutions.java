package WindowShading;

import java.util.Arrays;
import java.util.Random;

import Optimisation.Individual;

public class GenerateSolutions
{
	private static WindowShadingFitnessFunction ff;
	
	public static void main(String[] args)
	{
		int numberOfSolutions = 100;
		int numberOfWindows = 120;
		
		boolean [][] windows = generateRandomWindows(numberOfSolutions, numberOfWindows);
		double [] energy = new double[numberOfSolutions];
		
		ff = new WindowShadingFitnessFunction(false, true);
		
//		for (boolean [] w : windows)
//		{
//			System.out.println(Arrays.toString(w));
//		}
		
		for (int i = 0; i < numberOfSolutions; i++)
		{
			energy[i] = calculateEnergy(windows[i]);
		}

		/////////////////////////////////////////////
		
		double[][] windowsWithEnergy = new double[numberOfSolutions][numberOfWindows + 1];
		
		for (int i = 0; i < windowsWithEnergy.length; i++)
		{
			windowsWithEnergy[i] = combineArrays(windows[i], energy[i]);
		}
		
		for (double [] w : windowsWithEnergy)
		{
			System.out.println(Arrays.toString(w));
		}
	}

	private static boolean[][] generateRandomWindows(int POPULATION_COUNT, int WINDOW_COUNT)
	{
		int count = 1;
		int pop_count = 0;
		Random rnd = new Random();
		boolean [][] solutions = new boolean[POPULATION_COUNT][WINDOW_COUNT];
		
		for (int p = 0; p < POPULATION_COUNT; p++)
		{
//			System.out.println(p);
			for (int i = 0; i < WINDOW_COUNT; i++)
			{
				solutions[p][i] = rnd.nextDouble() < ((double) p / POPULATION_COUNT);
			}
		}
		
		return solutions;
	}
	
	private static double calculateEnergy(boolean [] windows)
	{
		double energyVals = 0.0;
		
		Individual ind = new Individual(ff, windows);
		ff.evaluate(ind);
		energyVals = ind.getFitness1();
//		System.out.println(energyVals);
		
		return energyVals;
	}
	
	private static double[] combineArrays(boolean [] windows, double energy)
	{
		double [] combined = new double[windows.length + 1];
		
		for (int i = 0; i < windows.length; i++)
		{
			if (windows[i])
				combined[i] = 1;
			else
				combined[i] = 0;
		}
		
		combined[combined.length - 1] = energy;
		
		return combined;
	}

}

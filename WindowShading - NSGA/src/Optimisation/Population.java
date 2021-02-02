package Optimisation;

import java.util.List;
import java.util.Random;

public class Population
{
	private Individual[] population;
	private FitnessFunction fitnessFunction;
	private Random r;
	private final int problemSize = 120;
	
	public Population(FitnessFunction ff, int populationSize)
	{
		population = new Individual[populationSize];
		fitnessFunction = ff;
		r = new Random();
		
		initialize();
	}
	
	
	private void initialize()
	{
		for (int i = 0; i < population.length; i++)
		{
			population[i] = new Individual(fitnessFunction, problemSize, r);
		}
	}
	
	public Individual get(int index)
	{
		return population[index];
	}
	
	public int length()
	{
		return population.length;
	}
	
	public Individual[] toArray()
	{
		return population;
	}
}

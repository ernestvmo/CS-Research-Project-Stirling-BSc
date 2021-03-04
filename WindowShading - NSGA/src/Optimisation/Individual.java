package Optimisation;

import java.util.Comparator;
import java.util.Random;
import java.util.Set;

import regression.Model;

/**
 * Bit string individuals with 2 objectives
 * 
 * @author Ernest Vanmosuinck
 */
public class Individual
{
	private boolean[] alleles;
	private double fitness1;
	private double fitness2;
	private double overallConstraintViolation;
	private FitnessFunction ff;

	// NSGA-II specific vars
	public Set<Individual> dominatedSet;
	public int dominationCount;
	public int rank;

	public double distance;

	/**
	 * Constructor for an Individual solution.
	 * This constructor is only used when using the surrogate model to evaluate fitness.
	 * 
	 * @param size The number of windows in the solution.
	 * @param r Random object to generate a state of randomness in the solution.
	 */
	public Individual(int size, Random r)
	{
		boolean[] a = new boolean[size];
//		for (int i = 0; i < a.length; i++)
//		{
//			a[i] = r.nextBoolean();
//		}
		for (int i = 0; i < a.length; i++)
		{
			a[i] = r.nextDouble() < ((double) i / 100);
		}
		
		init(a);
	}
	
	/**
	 * Constructor for an Individual solution.
	 * This constructor is only used when using EnergyPlus to evaluate fitness.
	 * 
	 * @param ff FitnessFunction to evaluate the solution, fitness values.
	 * @param size The number of windows in the solution.
	 * @param r Random object to generate a state of randomness in the solution.
	 */
	public Individual(FitnessFunction ff, int size, Random r)
	{
		boolean[] a = new boolean[size];
//		for (int i = 0; i < a.length; i++)
//		{
//			a[i] = r.nextBoolean();
//		}
		for (int i = 0; i < a.length; i++)
		{
			a[i] = r.nextDouble() < ((double) i / 100);
		}

		init(ff, a);
	}

	/**
	 * Constructor for an Individual solution from a set windows.
	 * This constructor is only used when using the surrogate model to evaluate fitness.
	 * 
	 * @param alleles The array of boolean representing windows.
	 */
	public Individual(boolean[] alleles)
	{
		init(alleles);
	}
	
	/**
	 * Constructor for an Individual solution.
	 * This constructor is only used when using EnergyPlus to evaluate fitness.
	 * 
	 * @param ff FitnessFunction to evaluate the solution, fitness values.
	 * @param alleles The array of boolean representing windows.
	 */
	public Individual(FitnessFunction ff, boolean[] alleles)
	{
		init(ff, alleles);
	}

	/**
	 * Initialises values to their default state.
	 * 
	 * @param alleles The array of boolean representing windows.
	 */
	private void init(boolean[] alleles)
	{
		this.alleles = alleles;
		fitness1 = Double.NaN;
		fitness2 = Double.NaN;
		overallConstraintViolation = Double.NaN;
	}
	
	/**
	 * Initialises values to their default state.
	 * 
	 * @param ff FitnessFunction to evaluate the solution, fitness values.
	 * @param alleles The array of boolean representing windows.
	 */
	private void init(FitnessFunction ff, boolean[] alleles)
	{
		this.ff = ff;
		this.alleles = alleles;
		fitness1 = Double.NaN;
		fitness2 = Double.NaN;
		overallConstraintViolation = Double.NaN;
	}

	/**
	 * call the evaluator and evaluate - regardless of whether or not it's
	 * already been done.
	 */
	public void energyPlusEvaluate(FitnessFunction ff)
	{
		FitnessFunction.MOFitness f = ff.evaluate(this);
		this.fitness1 = f.fitness1;
//		this.fitness2 = f.fitness2;
//		this.overallConstraintViolation = f.overallConstraintViolation;
		
		int count = 0;
		for (int i = 0; i < alleles.length; i++)
			count += alleles[i] ? 1 : 0;
		this.fitness2 = 100 * (120 - count) + 350 * count;
	}

	/**
	 * Evaluate the fitness (energy and cost).
	 * Cost can range between 12000 and 42000.
	 * 
	 * @param model The surrogate model.
	 */
	public void surrogateEvaluate(Model model)
	{
		int count = 0;
		
		for (int i = 0; i < alleles.length; i++)
			count += alleles[i] ? 1 : 0;

		double cost = 100 * (120 - count) + 350 * count;
		
		this.fitness1 = model.predict(alleles);
		this.fitness2 = cost;
	}

	/**
	 * Accessor method for the windows array of boolean.
	 * 
	 * @return The array of boolean representing windows.
	 */
	public boolean[] getAlleles()
	{
		return alleles;
	}

	/**
	 * Mutator method for the windows array of boolean.
	 * 
	 * @param alleles The array of boolean representing windows.
	 */
	public void setAlleles(boolean[] alleles)
	{
		this.alleles = alleles;
	}

	/**
	 * This method determines if an individual dominates another individual.
	 * An individual dominates another if he is no worse than the other in all objectives and strictly better in at least one.
	 * 
	 * @param that The other individual to compare to.
	 * @return {@code true} if the individual dominates, {@code false} otherwise.
	 */
	public boolean dominates(Individual that)
	{
		// this dominates that if: this is feasible and that is not, or this and
		// that
		// are both infeasible but this has a smaller overall violation,
		// or this and that are both feasible and this dominates that in terms
		// of
		// objectives

		// first check constraints
		boolean thisDominatesThat = false;

		thisDominatesThat = (
				(this.fitness1 <= that.fitness1) && (this.fitness2 <= that.fitness2)) 
					&& 
				((this.fitness1 < that.fitness1) || (this.fitness2 < that.fitness2));
		
		return thisDominatesThat;
	}

	/**
	 * Accessor method for the first fitness (energy).
	 * 
	 * @return The energy consumption of an individual.
	 */
	public double getFitness1()
	{
//		if (Double.isNaN(fitness1))
//		{
//			forceEvaluate();
//		}

		return fitness1;
	}

	/**
	 * Accessor method for the second fitness (cost).
	 * 
	 * @return The production cost of an individual.
	 */
	public double getFitness2()
	{
//		if (Double.isNaN(fitness2))
//		{
//			forceEvaluate();
//		}

		return fitness2;
	}

	/**
	 * 
	 * @return
	 */
	public double getOverallConstraintViolation()
	{
//		if (Double.isNaN(overallConstraintViolation))
//		{
//			forceEvaluate();
//		}

		return overallConstraintViolation;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		
		for (int i = 0; i < alleles.length; i++) {
			if (alleles[i])
				buff.append("1");
			else
				buff.append("0");
		}
		buff.append(" : " + fitness1 + ", " + fitness2 + ", " + rank);
		
		return buff.toString();
	}
	
	/**
	 * This class acts as comparator for the second fitness value.
	 */
	public static class Objective1Comparator implements Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			double diff = i1.fitness1 - i2.fitness1;
			
			if (diff > 0)
				return 1;
			else if (diff < 0)
				return -1;
			else
				return 0;
		}
	}

	/**
	 * This class acts as comparator for the second fitness value.
	 */
	public static class Objective2Comparator implements Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			double diff = i1.fitness2 - i2.fitness2;

			if (diff > 0)
				return 1;
			else if (diff < 0)
				return -1;
			else
				return 0;
		}
	}

	/**
	 * This class acts as comparator for non domination.
	 */
	public static class NonDominationComparator
			implements
				Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			return i1.dominationCount - i2.dominationCount;
		}
	}

	/**
	 * This class acts as comparator for the Crowding Distance.
	 */
	public static class CrowdingDistanceComparator
			implements
				Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			double diff = (-1) * (i1.distance - i2.distance); // sort in
																// descending
																// order
			if (diff > 0)
				return 1;
			else if (diff < 0)
				return -1;
			else
				return 0;
		}
	}
}
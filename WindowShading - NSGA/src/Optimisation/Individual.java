/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Optimisation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

import regression.Model;

/**
 * Bit string individuals with 2 objectives
 * 
 * @author cvaeib
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

	public Individual(FitnessFunction ff, int size, java.util.Random r)
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

	public Individual(FitnessFunction ff, boolean[] alleles)
	{
		init(ff, alleles);
	}

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
//	public void forceEvaluate()
//	{
//		FitnessFunction.MOFitness f = ff.evaluate(this);
//		this.fitness1 = f.fitness1;
//		this.fitness2 = f.fitness2;
//		this.overallConstraintViolation = f.overallConstraintViolation;
//	}

	/**
	 * only evaluate if necessary (not already done). This allows it to be done
	 * at a better time for the algorithm (e.g. as part of a batch within
	 * threads instead of when the fitnesses are next retrieved)
	 */
//	public void evaluate()
//	{
//		if (Double.isNaN(fitness1) || Double.isNaN(fitness2)
//				|| Double.isNaN(overallConstraintViolation))
//		{
//			forceEvaluate();
//		}
//	}
	
	public void evaluate_bis(Model model)
	{
		int count = 0;
		
		for (int i = 0; i < alleles.length; i++)
			count += alleles[i] ? 1 : 0;

		double cost = 100 * (120 - count) + 350 * count;
		
		this.fitness1 = cost;
		this.fitness2 = model.predict(alleles);
	}

	public boolean[] getAlleles()
	{
		return alleles;
	}

	public void setAlleles(boolean[] alleles)
	{
		this.alleles = alleles;
	}

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
		
//		thisDominatesThat |= (this.isFeasible() && !that.isFeasible());
//		thisDominatesThat |= (!this.isFeasible() && !this.isFeasible()
//				&& (this.getOverallConstraintViolation() < that
//						.getOverallConstraintViolation()));
//
//		thisDominatesThat |= (this.isFeasible() && that.isFeasible()
//				&& (((this.getFitness1() <= that.getFitness1())
//						&& (this.getFitness2() < that.getFitness2()))
//						|| ((this.getFitness1() < that.getFitness1()) && (this
//								.getFitness2() <= that.getFitness2()))));

		return thisDominatesThat;
	}

	public double getFitness1()
	{
//		if (Double.isNaN(fitness1))
//		{
//			forceEvaluate();
//		}

		return fitness1;
	}

	public double getFitness2()
	{
//		if (Double.isNaN(fitness2))
//		{
//			forceEvaluate();
//		}

		return fitness2;
	}

	public double getOverallConstraintViolation()
	{
//		if (Double.isNaN(overallConstraintViolation))
//		{
//			forceEvaluate();
//		}

		return overallConstraintViolation;
	}

	public boolean isFeasible()
	{
		return this.getOverallConstraintViolation() <= 0;
	}

	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		
		for (int i = 0; i < alleles.length; i++) {
			if (alleles[i])
				buff.append("1");
			else
				buff.append("0");
		}
		
		return buff.toString();
	}
	
	public static class Objective1Comparator implements Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			double diff = i1.fitness1 - i2.fitness1;
			// done this way in case difference is very small
			if (diff > 0)
			{
				return 1;
			} else if (diff < 0)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}

	public static class Objective2Comparator implements Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			double diff = i1.fitness2 - i2.fitness2;
			// done this way in case difference is very small
			if (diff > 0)
			{
				return 1;
			} else if (diff < 0)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}

	public static class NonDominationComparator
			implements
				Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			return i1.dominationCount - i2.dominationCount;
		}
	}

	public static class CrowdingDistanceComparator
			implements
				Comparator<Individual>
	{
		public int compare(Individual i1, Individual i2)
		{
			double diff = (-1) * (i1.distance - i2.distance); // sort in
																// descending
																// order
			// done this way in case difference is very small
			if (diff > 0)
			{
				return 1;
			} else if (diff < 0)
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
}
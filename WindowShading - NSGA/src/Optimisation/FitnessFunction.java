/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Optimisation;

public abstract class FitnessFunction
{
	/**
	 * evaluate specified individual and return the fitness as appropriate (also
	 * evals constraints)
	 */
	public abstract MOFitness evaluate(Individual i);

	private int problemSize;

	protected FitnessFunction(int problemSize)
	{
		this.problemSize = problemSize;
	}

	/**
	 * a means to get the number of evaluations performed. Implement how this is
	 * kept track of yourself.
	 */
	public abstract int getEvals();

	public int getProblemSize()
	{
		return problemSize;
	}

	public static class MOFitness
	{
		public double fitness1;
		public double fitness2;

		/** set to zero if unconstrained */
		public double overallConstraintViolation;
	}
}

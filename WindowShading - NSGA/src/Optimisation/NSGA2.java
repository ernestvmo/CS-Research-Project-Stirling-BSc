package Optimisation;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import WindowShading.WindowShadingFitnessFunction;

//TODO ADD CONSTRAINTS!

/**
 *
 * @author cvaeib
 */
public class NSGA2
{
	public static void main(String[] args)
	{
		new NSGA2().go();
	}

	private Random r;
	private FitnessFunction ff;

	public NSGA2()
	{
		r = new Random();
	}

	public void go()
	{
		VisualisePopulation vp = new VisualisePopulation();

		ff = new WindowShadingFitnessFunction(false, false);
		int problemSize = ff.getProblemSize();

		// parameters for NSGA-II:
		int N = 100; // population size
		double selectionProbability = .75; // 1 = always pick better solution; 0 =
											// always pick worst
		double mutationProbability = .5 / problemSize; // probability of
														// mutation for each bit
		double crossoverProbability = 0.9;
		int maxEvals = 10;
		long waitBetweenGens = 0; // slows down evolution if we want to display
									// visual plot of population (delay in
									// milliseconds)
		int numThreads = 2; // works best if pop size is a multiple of this as
							// it fairly crudely divides up the pop between
							// threads.

		// Algorithm implementation begins here.

		// init population
		Individual[] P = new Individual[N];
		for (int i = 0; i < P.length; i++)
		{
			P[i] = new Individual(ff, problemSize, r);
		}

		evaluatePopulation(P, numThreads);

		// sort population based on nondomination
		P = flattenFronts(fastNonDominatedSort(P)); // populate nondom counts
													// and sort
		System.out.println("after " + P.length);
		// create offspring population

		Individual[] Q = newOffspringPop(P, selectionProbability,
				crossoverProbability, mutationProbability);
		evaluatePopulation(Q, numThreads);
// TODO
		// for each generation...
		boolean done = false;
		int gen = 0;
		while (!done)
		{
			// create combined population R
			Individual[] R = new Individual[P.length + Q.length];
			int rPointer = 0;
			for (int i = 0; i < P.length; i++)
			{
				R[rPointer++] = P[i];
			}
			for (int i = 0; i < Q.length; i++)
			{
				R[rPointer++] = Q[i];
			}

			
			System.out.println(R.length);
			// sort new population into fronts
			List<List<Individual>> fronts = fastNonDominatedSort(R); // populate
																		// nondom
																		// counts

			// copy fronts into new parental set
			P = new Individual[N];
			int pointer = 0;
			List<Individual> nextFront = fronts.remove(0);
			while (pointer + nextFront.size() <= P.length)
			{
				for (int i = 0; i < nextFront.size(); i++)
				{
					P[pointer++] = nextFront.get(i);
				}
				nextFront = fronts.remove(0);
			}

			// remaining front needs sorted on crowding distance assignment
			if (pointer < P.length)
			{ // only bother if we want to add any of the next front
				crowdingDistanceAssignment(nextFront);
				Individual[] nextFrontArray = nextFront
						.toArray(new Individual[nextFront.size()]);
				Arrays.sort(nextFrontArray,
						new Individual.CrowdingDistanceComparator());

				int i = 0;

				// copy necessary part of next front into new population
				for (; pointer < P.length; pointer++)
				{
					P[pointer] = nextFrontArray[i++];
				}
			}

			// make new offspring population
			Q = newOffspringPop(P, selectionProbability, crossoverProbability,
					mutationProbability);
			evaluatePopulation(Q, numThreads);

			// stopping criteria
			done = ff.getEvals() >= maxEvals;
			System.out.println("Done gen " + gen++);
			vp.updatePopulation(P);
			try
			{
				Thread.sleep(waitBetweenGens);
			}
			catch (InterruptedException e)
			{
			}
		}

		// we're done. Output final population!
		System.out.println("Done. final pop:");
		for (Individual i : P)
		{
			for (boolean b : i.getAlleles())
			{
				System.out.print(b ? 1 : 0);
			}

			System.out.println(
					": O1: " + i.getFitness1() + " : O2: " + i.getFitness2());
		}
	}

	/**
	 * @returns a list of fronts, but it also sets the ranks so you can use
	 *          those instead if you want
	 */
	private List<List<Individual>> fastNonDominatedSort(Individual[] pop)
	{
		// init
		Set<Individual> assigned = new HashSet<Individual>(pop.length); 
		// keep track of what's been already assigned to a front
		List<List<Individual>> fronts = new ArrayList<List<Individual>>();
		fronts.add(new ArrayList<Individual>());// init first front

		// ====== block A =========
		// loop over all individuals in population and fill dom counts / sets
		for (int p = 0; p < pop.length; p++)
		{
			pop[p].dominationCount = 0;
			pop[p].dominatedSet = new HashSet<Individual>();

			for (int q = 0; q < pop.length; q++)
			{
				if (p != q)
				{ // don't compare with self
					if (pop[p].dominates(pop[q]))
					{ // if p dominates q
						pop[p].dominatedSet.add(pop[q]); // add q to the set of
															// solutions
															// dominated by p
					} else if (pop[q].dominates(pop[p]))
					{
						pop[p].dominationCount++; // increment the domination
													// counter of p
					}
				}
			}

			if (pop[p].dominationCount == 0)
			{ // p belongs to the first front
				pop[p].rank = 0; // we're going with base 0 not 1
				fronts.get(0).add(pop[p]);
				assigned.add(pop[p]);
			}
		}

		// =========== block B ==========
		int i = 0; // initialise the front counter; using base 0 not 1

		for (List<Individual> fi = fronts.get(i); fi.size() > 0; fi = fronts
				.get(i))
		{ // essentially while Fi != 0, but with inits
			List<Individual> Q = new ArrayList<Individual>(); // Used to store
																// members of
																// the next
																// front
			for (Individual p : fi)
			{
				for (Individual q : p.dominatedSet)
				{
					q.dominationCount--;
					if (q.dominationCount == 0)
					{
						q.rank = i + 1;
						Q.add(q);
					}
				}
			}
			i++;
			fronts.add(i, Q);
		}

		System.out.println("hey " + fronts.size() + " " + fronts.get(0).size());
		return fronts;
	}

	/**
	 * take the list of fronts and return the individuals in a sorted (ascending
	 * non-dom rank) array instead
	 */
	private Individual[] flattenFronts(List<List<Individual>> fronts)
	{
		List<Individual> l = new ArrayList<Individual>();
		for (List<Individual> front : fronts)
		{
			for (Individual i : front)
			{
				l.add(i);
			}
		}

		System.out.println("P.size() " + l.size());
		return l.toArray(new Individual[l.size()]);
	}

	private void crowdingDistanceAssignment(List<Individual> individuals)
	{
		Individual[] I = individuals
				.toArray(new Individual[individuals.size()]);

		int l = I.length; // number of solutions in I

		for (Individual i : I)
		{
			i.distance = 0;
		}

		// for each objective m...
		// obj 1
		Arrays.sort(I, new Individual.Objective1Comparator()); // sort using
																// objective
																// value
		I[0].distance = I[l - 1].distance = Double.POSITIVE_INFINITY;
		double fitness1Range = I[l - 1].getFitness1() - I[0].getFitness1();
		for (int i = 1; i < l - 1; i++)
		{
			I[i].distance += (I[i + 1].getFitness1() - I[i - 1].getFitness1())
					/ fitness1Range;
		}

		// obj 2
		Arrays.sort(I, new Individual.Objective2Comparator()); // sort using
																// objective
																// value
		I[0].distance = I[l - 1].distance = Double.POSITIVE_INFINITY;
		double fitness2Range = I[l - 1].getFitness2() - I[0].getFitness2();
		for (int i = 1; i < l - 1; i++)
		{
			I[i].distance += (I[i + 1].getFitness2() - I[i - 1].getFitness2())
					/ fitness2Range;
		}
	}

	/**
	 * @return an individual using a 2 ind tournament selection; probability is
	 *         the chance that the one with the lowest nondom rank is chosen
	 */
	private Individual binaryTournamentSelection(Individual[] pop,
			double probability)
	{
		Individual i1 = pop[r.nextInt(pop.length)];
		Individual i2 = pop[r.nextInt(pop.length)];

		if (r.nextDouble() < probability)
		{
			if (i1.rank <= i2.rank)
			{
				return i1;
			} else
			{
				return i2;
			}
		} else
		{
			if (i1.rank <= i2.rank)
			{
				return i2;
			} else
			{
				return i1;
			}
		}
	}

	/** return a copy of the individual, with possible mutations */
	private Individual cloneIndividualWithPossibleMutation(Individual i,
			double bitwiseMutationRate)
	{
		boolean[] a = i.getAlleles();
		boolean[] b = new boolean[a.length];
		for (int j = 0; j < a.length; j++)
		{
			if (r.nextDouble() < bitwiseMutationRate)
			{
				b[j] = !a[j];
			} else
			{
				b[j] = a[j];
			}
		}

		Individual i2 = new Individual(ff, b);

		return i2;
	}

	/** return copies of the 2 individuals, with possible crossover */
	private Individual[] cloneIndividualWithPossibleCrossover(Individual i1,
			Individual i2, double crossoverRate)
	{
		boolean[] a1 = i1.getAlleles();
		boolean[] a2 = i2.getAlleles();
		boolean[] b1 = new boolean[a1.length];
		boolean[] b2 = new boolean[a2.length];
		int crossoverPoint = a1.length;
		if (r.nextDouble() < crossoverRate)
		{
			crossoverPoint = r.nextInt(a1.length);
		}

		for (int j = 0; j < a1.length; j++)
		{
			if (j < crossoverPoint)
			{
				b1[j] = a1[j];
				b2[j] = a2[j];
			} else
			{
				b1[j] = a2[j];
				b2[j] = a1[j];
			}
		}

		Individual[] rval = new Individual[2];
		rval[0] = new Individual(ff, b1);
		rval[1] = new Individual(ff, b2);

		return rval;
	}

	private Individual[] newOffspringPop(Individual[] P,
			double selectionProbability, double crossoverProbability,
			double mutationProbability)
	{
		Individual[] Q = new Individual[P.length];
		for (int i = 0; i < Q.length; i += 2)
		{
			Individual parent1 = binaryTournamentSelection(P,
					selectionProbability);
			Individual parent2 = binaryTournamentSelection(P,
					selectionProbability);
			Individual[] offspring = cloneIndividualWithPossibleCrossover(
					parent1, parent2, crossoverProbability);
			offspring[0] = cloneIndividualWithPossibleMutation(offspring[0],
					mutationProbability);
			offspring[1] = cloneIndividualWithPossibleMutation(offspring[1],
					mutationProbability);

			Q[i] = offspring[0];
			if (i < Q.length - 1)
			{ // just in case it's an odd size of population
				Q[i + 1] = offspring[1];
			}
		}

		return Q;
	}

	/** @return number of evals consumed */
	private int evaluatePopulation(Individual[] pop, int threads)
	{
		int numPerThread = pop.length / threads;
		EvaluationThread[] et = new EvaluationThread[threads];
		for (int i = 0; i < threads; i++)
		{
			et[i] = new EvaluationThread(pop, i * numPerThread,
					(i < threads - 1) ? (i + 1) * numPerThread : pop.length);
			et[i].start();
		}

		int evals = 0;
		for (int i = 0; i < threads; i++)
		{
			try
			{
				et[i].join();
			}
			catch (InterruptedException e)
			{
				if (et[i].isAlive())
				{
					i--; // try again if interrupted!
				}
			}

			if (!et[i].isAlive())
			{
				evals += et[i].evalsConsumed;
			}
		}

		return evals;
	}

	/** crude for now - divide the solutions up evenly between the threads */
	private class EvaluationThread extends Thread
	{
		private Individual[] pop;
		/** inclusive */
		private int start;
		/** exclusive */
		private int end;

		private int evalsConsumed;

		public EvaluationThread(Individual[] pop, int start, int end)
		{
			this.pop = pop;
			this.start = start;
			this.end = end;
			this.evalsConsumed = 0;
		}

		public void run()
		{
			evalsConsumed = 0;
			for (int i = start; i < end; i++)
			{
				Individual s = pop[i];
				s.evaluate();
				
			}
		}
	}
}

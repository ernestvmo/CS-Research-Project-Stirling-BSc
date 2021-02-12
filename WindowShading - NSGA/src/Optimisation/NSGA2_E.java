package Optimisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import WindowShading.WindowShadingFitnessFunction;
import plotting.Plotting;
import regression.Model;

public class NSGA2_E
{
	private FitnessFunction ff;
	private Random r;

	private int windowsCount = 120;
	private int numSolutions = 100;
	private int numThreads = 10;

	private int maxEvals = 5000;

	private double selectionRate = 0.5;
	private double crossoverRate = 0.5;
	private double mutationRate = 0.25; // 0 = low, 1 = high
	private Model model;

	private Individual[] first;
	private Individual[] last;

	// public static void main(String[] args)
	// {
	// new NSGA2_E().go();
	// }

	public NSGA2_E()
	{
		ff = new WindowShadingFitnessFunction(false, false);
		r = new Random();
	}

	public void go()
	{
		System.out.println("started");

		VisualisePopulation vp = new VisualisePopulation();
		// 1 - initialize random population

		Individual[] initial = new Individual[numSolutions];
		for (int i = 0; i < initial.length; i++)
		{
			initial[i] = new Individual(ff, windowsCount, r);
		}

		evaluatePopulation(initial);
		// ** NON-DOMINATED SORT **
		initial = ascendList(nonDominatedSort(initial));
		// ** LOOP **
		Individual[] offspring = createOffspring(initial);
		evaluatePopulation(offspring);

		// System.out.println("TEST OFFSPRING " + offspring[0].getFitness1() + "
		// " + offspring[0].getFitness2());

		for (Individual i : initial)
		{
			System.out.println(i.getFitness1() + " " + i.getFitness2());
		}

		double f = Plotting.hypervolume(initial);

		int currentEval = 0;
		while (currentEval < maxEvals)
		{
			Individual[] R = new Individual[initial.length + offspring.length];
			int p = 0;
			for (int i = 0; i < initial.length; i++)
			{
				R[p++] = initial[i];
			}
			for (int i = 0; i < offspring.length; i++)
			{
				R[p++] = offspring[i];
			}

			// for (Individual t : R) {
			// System.out.println("INDIVS IN R : " + t.getFitness1() + " " +
			// t.getFitness2() + " " + t.toString());
			// }

			// System.out.println(R.length);

			int counter = 0;
			// for (Individual i : R)
			// {System.out.print(counter++ + " ");
			// for (boolean c : i.getAlleles())
			// {
			// System.out.print(c ? "1" : "0");
			// }
			// System.out.println(" " + i.getFitness1() + " " +
			// i.getFitness2());
			// }

			// System.out.println("TESTTESTTES R " +
			// R[initial.length].getFitness1() + " " +
			// R[initial.length].getFitness2());

			List<List<Individual>> fronts = nonDominatedSort(R);

			initial = new Individual[numSolutions];
			int pointer = 0;
			List<Individual> nextFront = fronts.remove(0);
			while (pointer + nextFront.size() <= initial.length)
			{
				for (int i = 0; i < nextFront.size(); i++)
				{
					initial[pointer] = nextFront.get(i);
					pointer++;
				}
				nextFront = fronts.remove(0);
			}

			if (pointer < initial.length)
			{
				crowdingDistance(nextFront);
				Individual[] nextFrontArray = nextFront
						.toArray(new Individual[nextFront.size()]);

				Arrays.sort(nextFrontArray,
						new Individual.CrowdingDistanceComparator());

				for (int i = 0; pointer < initial.length; i++)
				{
					initial[pointer] = nextFrontArray[i];
					pointer++;
				}
			}

			offspring = createOffspring(initial);
			evaluatePopulation(offspring);

			currentEval++;
			// System.out.println("eval: " + currentEval);
		}

		System.out.println("DONE");
		vp.updatePopulation(initial);
		// displayPopulation(initial);

		for (Individual i : initial)
		{
			System.out.println(i.getFitness1() + " " + i.getFitness2());
		}

		double l = Plotting.hypervolume(initial);
		System.out.println("First: " + f);
		System.out.println("Last:  " + l);
		System.out.println("Improvement: " + (l - f));
	}

	private void evaluatePopulation(Individual[] P)
	{
		EvaluationThread[] evals = new EvaluationThread[numThreads];
		int numberPerThreads = numSolutions / numThreads;

		for (int i = 0; i < numThreads; i++)
		{
			evals[i] = new EvaluationThread(P, numberPerThreads * i,
					((i < numThreads - 1)
							? (numberPerThreads * (i + 1))
							: P.length));
			evals[i].start();
		}

		int evalNum = 0;
		for (int i = 0; i < numThreads; i++)
		{
			try
			{
				evals[i].join();
			}
			catch (InterruptedException e)
			{
				// TODO: handle exception
				if (evals[i].isAlive())
					i--;
			}

			if (!evals[i].isAlive())
				evalNum += evals[i].consumed;
		}
	}

	private Individual[] ascendList(List<List<Individual>> fronts)
	{
		List<Individual> P = new ArrayList<>();

		for (List<Individual> front : fronts)
			for (Individual i : front)
				P.add(i);
		// System.out.println("P.size() " + P.size());
		return P.toArray(new Individual[P.size()]);
	}

	private List<List<Individual>> nonDominatedSort(Individual[] pop)
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

		// System.out.println("hey " + fronts.size() + " " +
		// fronts.get(0).size());
		return fronts;
		// System.out.println("front " + P.length);
		// // We create a set to keep track of individuals already assigned
		// Set<Individual> alreadyAssigned = new HashSet<>();
		// // List to keep track of the different fronts and their solutions
		// List<List<Individual>> fronts = new ArrayList<>();
		//
		// // Initialize the front "0" of best solutions.
		// fronts.add(new ArrayList<>());
		//
		// for (int p = 0; p < P.length; p++)
		// {
		// P[p].dominationCount = 0;
		// P[p].dominatedSet = new HashSet<>();
		//
		// for (int q = 0; q < P.length; q++)
		// {
		// if (p != q)
		// {
		// if (P[p].dominates(P[q]))
		// {
		// P[p].dominatedSet.add(P[q]);
		// }
		// else if (P[q].dominates(P[p]))
		// {
		// P[p].dominationCount += 1;
		// }
		// }
		//
		// if (P[p].dominationCount == 0)
		// {
		// // the solution is not dominated by any other
		// // it belongs to the best rank
		// P[p].rank = 0;
		// alreadyAssigned.add(P[p]);
		// fronts.get(0).add(P[p]);
		// }
		// }
		// }
		//
		// System.out.println("front 2 " + P.length);
		//
		// // Assign rest of fronts
		// int i = 0;
		// for (List<Individual> fi = fronts.get(i); fi.size() > 0; fi =
		// fronts.get(i))
		// {
		// List<Individual> Q = new ArrayList<>();
		//
		// for (Individual p : fi)
		// {
		// for (Individual q : p.dominatedSet)
		// {
		// q.dominationCount--;
		//
		// if (q.dominationCount == 0)
		// {
		// q.rank = i + 1;
		// Q.add(q);
		// }
		// }
		// }
		//
		// i++;
		// fronts.add(i, Q);
		// }
		//
		// System.out.println("hey " + fronts.size() + " " +
		// fronts.get(0).size());
		// return fronts;
	}

	private void crowdingDistance(List<Individual> individuals)
	{
		Individual[] I = individuals
				.toArray(new Individual[individuals.size()]);

		int l = I.length;

		for (Individual i : I)
		{
			i.distance = 0;
		}

		Arrays.sort(I, new Individual.Objective1Comparator());

		I[0].distance = Double.POSITIVE_INFINITY;
		I[l - 1].distance = Double.POSITIVE_INFINITY;

		double fitness1Range = I[l - 1].getFitness1() - I[0].getFitness1();
		for (int j = 1; j < l - 1; j++)
		{
			I[j].distance += (I[j + 1].getFitness1() - I[j - 1].getFitness1())
					/ fitness1Range;
		}

		Arrays.sort(I, new Individual.Objective2Comparator());

		I[0].distance = Double.POSITIVE_INFINITY;
		I[l - 1].distance = Double.POSITIVE_INFINITY;

		double fitness2Range = I[l - 1].getFitness2() - I[0].getFitness2();
		for (int j = 1; j < l - 1; j++)
		{
			I[j].distance += (I[j + 1].getFitness2() - I[j - 1].getFitness2())
					/ fitness2Range;
		}

	}

	private Individual[] createOffspring(Individual[] parents)
	{
		Individual[] nextPopulation = new Individual[parents.length];

		for (int i = 0; i < nextPopulation.length; i += 2)
		{// replace half the population
			// Parent 1
			Individual parent1 = parentSelection(parents);
			// Parent 2
			Individual parent2 = parentSelection(parents);

			// Crossover
			// TODO: play with different crossover strategies?
			Individual[] offspring = crossover(parent1, parent2);

			// Mutation
			offspring[0] = mutateOffspring(offspring[0]);
			offspring[1] = mutateOffspring(offspring[1]);

			nextPopulation[i] = offspring[0];
			if (i < nextPopulation.length - 1)
				nextPopulation[i + 1] = offspring[1];
		}

		return nextPopulation;
	}

	private Individual parentSelection(Individual[] P)
	{
		int i = r.nextInt(P.length), j = r.nextInt(P.length);

		while (i == j)
			j = r.nextInt(P.length);

		Individual p1 = P[i];
		Individual p2 = P[j];

		if (r.nextDouble() < selectionRate)
		{
			if (p1.rank <= p2.rank)
				return p1;
			else
				return p2;
		} else
		{
			if (p1.rank <= p2.rank)
				return p2;
			else
				return p1;
		}
	}

	private Individual[] crossover(Individual parent1, Individual parent2)
	{
		boolean[] alleles1 = parent1.getAlleles();
		boolean[] alleles2 = parent2.getAlleles();

		boolean[] next1 = new boolean[alleles1.length];
		boolean[] next2 = new boolean[alleles1.length];

		// crossover
		for (int b = 0; b < next1.length; b++)
		{
			if (r.nextDouble() < crossoverRate)
				next1[b] = alleles1[b];
			else
				next1[b] = alleles2[b];
		}
		for (int b = 0; b < next2.length; b++)
		{
			if (r.nextDouble() < crossoverRate)
				next2[b] = alleles1[b];
			else
				next2[b] = alleles2[b];
		}

		Individual[] generatedOffspring = new Individual[2];
		generatedOffspring[0] = new Individual(ff, next1);
		generatedOffspring[1] = new Individual(ff, next2);

		return generatedOffspring;
	}

	private Individual mutateOffspring(Individual offspring)
	{
		boolean[] alleles = offspring.getAlleles();

		boolean[] mutated = new boolean[alleles.length];

		for (int i = 0; i < mutated.length; i++)
		{
			if (r.nextDouble() < mutationRate)
				mutated[i] = !alleles[i];
			else
				mutated[i] = alleles[i];
		}

		return new Individual(ff, mutated);
	}

	class EvaluationThread extends Thread
	{
		private Individual[] individuals;

		private int startIndex;

		private int endIndex;

		private int consumed;

		public EvaluationThread(Individual[] individuals, int startIndex,
				int endIndex)
		{
			this.individuals = individuals;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.consumed = 0;
		}

		public void run()
		{
			consumed = 0;
			for (int j = startIndex; j < endIndex; j++)
			{
				Individual i = individuals[j];
				i.evaluate_bis(model); // TODO
				// System.out.println("SINGLE INDIVIDUAL : " + i.getFitness1() +
				// " " + i.getFitness2());
			}
		}
	}

	public void setModel(Model m)
	{
		this.model = m;
	}

	private void displayPopulation(Individual[] P)
	{
		for (Individual i : P)
		{
			for (int j = 0; j < i.getAlleles().length; j++)
			{
				System.out.print(i.getAlleles()[j] ? "1" : "0");
			}
			System.out.println();
		}
	}
}

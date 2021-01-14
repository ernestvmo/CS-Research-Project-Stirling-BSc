package Optimisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import WindowShading.WindowShadingFitnessFunction;

public class NSGA2_E
{
	private FitnessFunction fitnessFunction;
	private Random r;
	
	public static void main(String[] args)
	{
		new NSGA2_E().go();
		System.out.println("terminated");
	}
	
	public NSGA2_E()
	{
		// TODO Auto-generated constructor stub
		r = new Random();
	}
	
	public void go()
	{
		VisualisePopulation vp = new VisualisePopulation();
		
		fitnessFunction = new WindowShadingFitnessFunction(false, true);
		int problemSize = fitnessFunction.getProblemSize();
		
		// NSGA-II parameters
		int populationSize = 100;
		
		double selectionProbability = 5.0;
		double mutationProbability = 5.0;
		double crossoverProbability = 0.5;
		
		int maximumEvaluations = 10;
		
		long waitBetweenGenerations = 0;
		
		int numThreads = 4; /** works best if pop size is a multiple of this as it fairly crudely divides up the pop between threads. */
		
		////////////////////////////////////////////////////////////////////
		
		// Algorithm Section
		
		// generate initial population of random solutions
		Population P = new Population(populationSize, fitnessFunction, problemSize, r);
		P.initialize();
		
		
		// evaluate solutions
		evaluatePopulation(P, numThreads);
		
		// non-dominated sort
		P = flattenFront(fastNonDominatedSort(P));
		
		
		// create offspring solution
		Population offspringPopulation = newOffspringPopulation(P, selectionProbability, crossoverProbability, mutationProbability);
		evaluatePopulation(offspringPopulation, numThreads);
		
		// TODO: loop
		
		// LOOP
		boolean done = false;
		int gen = 0;
		
		while (!done)
		{
			System.out.println(gen+1);
			Population combined = new Population(P, offspringPopulation);

			List<List<Individual>> fronts = fastNonDominatedSort(combined);

			P = new Population(populationSize);
			int pointer = 0;
			List<Individual> nextFront = fronts.remove(0);
			
			while (pointer + nextFront.size() <= P.getLength())
			{
				for (int i = 0; i < nextFront.size(); i++)
				{
					P.setIndividual(pointer++, nextFront.get(i));
				}
			}
			
			if (pointer < P.getLength())
			{
				crowdingDistanceAssignment(nextFront);
				Individual[] nextFrontArray = nextFront.toArray(new Individual[nextFront.size()]);
				Arrays.sort(nextFrontArray, new Individual.CrowdingDistanceComparator());
				
				
				for (int i = 0; pointer < P.getLength(); pointer++)
				{
					P.setIndividual(pointer, nextFrontArray[i++]);
				}
			}
			
			offspringPopulation = newOffspringPopulation(P, selectionProbability, crossoverProbability, mutationProbability);
			evaluatePopulation(offspringPopulation, numThreads);
			
			done = fitnessFunction.getEvals() >= maximumEvaluations;
			System.out.println("done generation " + gen++);
			vp.updatePopulation(P.toArray());
			
			try {
				Thread.sleep(waitBetweenGenerations);
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
			
		System.out.println("finished. final population: ");
		for (Individual i : P.toArray())
		{
			for (boolean b : i.getAlleles())
				System.out.print(b ? 1 : 0);
			
			System.out.println(" -> energy: " + i.getFitness1() + " cost: " + i.getFitness2());
		}
	}
	
	private void crowdingDistanceAssignment(List<Individual> nextFront)
	{
		Individual[] individuals = nextFront.toArray(new Individual[nextFront.size()]);
		
		int l = individuals.length;
		
		for (Individual i : individuals)
		{
			i.distance = 0;
		}
		
		
		Arrays.sort(individuals, new Individual.Objective1Comparator());
		
		individuals[0].distance = Double.POSITIVE_INFINITY;
		individuals[l - 1].distance = Double.POSITIVE_INFINITY;
		
		double fitness1Range = individuals[l - 1].getFitness1() - individuals[0].getFitness1();
		
		for (int i = 1; i < l - 1; i++)
		{
			individuals[i].distance += (individuals[i + 1].getFitness1() - individuals[i - 1].getFitness1()) / fitness1Range;
		}
		
		
		Arrays.sort(individuals, new Individual.Objective2Comparator());
		
		individuals[0].distance = Double.POSITIVE_INFINITY;
		individuals[l - 1].distance = Double.POSITIVE_INFINITY;
		
		double fitness2Range = individuals[l - 1].getFitness2() - individuals[0].getFitness2();

		for (int i = 1; i < l - 1; i++)
		{
			individuals[i].distance += (individuals[i + 1].getFitness2() - individuals[i - 1].getFitness2()) / fitness2Range;
		}
	}
	
	private Population flattenFront(List<List<Individual>> fronts)
	{
		List<Individual> l = new ArrayList<Individual>();
		
		for (List<Individual> front : fronts)
		{
			for (Individual i : front)
			{
				l.add(i);
			}
		}
		
		return new Population(l.toArray(new Individual[l.size()]));
	}
	
	private Population newOffspringPopulation(Population parents, double selectionProbability, double crossoverProbability, double mutationProbability)
	{
		Population Q = new Population(parents.getLength());
		for (int i = 0; i < Q.getLength(); i++)
		{
			Individual parent1 = binaryTournamentSelection(parents, selectionProbability);
			Individual parent2 = binaryTournamentSelection(parents, selectionProbability);
			
			Individual[] offspring = cloneIndividualWithPossibleCrossover(parent1, parent2, crossoverProbability);
			
			offspring[0] = cloneIndividualWithPossibleMutation(offspring[0], mutationProbability);
			offspring[1] = cloneIndividualWithPossibleMutation(offspring[1], mutationProbability);
		
			Q.setIndividual(i, offspring[0]);
			if (i < Q.getLength() - 1)
			{// just in case it's an odd size of population
				Q.setIndividual(i + 1, offspring[1]);
			}
		}
		
		return Q;
	}
	
	private Individual binaryTournamentSelection(Population parent, double selectionProbability)
	{
		Individual i1 = parent.get(r.nextInt(parent.getLength()));
		Individual i2 = parent.get(r.nextInt(parent.getLength()));
		
		if (r.nextDouble() < selectionProbability)
		{
			if (i1.rank <= i2.rank)
				return i1;
			else
				return i2;
		}
		else
		{
			if (i1.rank <= i2.rank)
				return i2;
			else
				return i1;
		}
	}
	
	private Individual[] cloneIndividualWithPossibleCrossover(Individual parent1, Individual parent2, double crossoverRate)
	{
		boolean[] a1 = parent1.getAlleles();
		boolean[] a2 = parent2.getAlleles();
		
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
			}
			else
			{
				b1[j] = a2[j];
				b2[j] = a1[j];
			}
		}
		
		Individual[] rval = new Individual[2];
		rval[0] = new Individual(fitnessFunction, b1);
		rval[1] = new Individual(fitnessFunction, b2);
		
		return rval;
	}
	
	private Individual cloneIndividualWithPossibleMutation(Individual i, double mutationRate)
	{
		boolean[] a = i.getAlleles();
		boolean[] b = new boolean[a.length];
		
		for (int j = 0; j < a.length; j++)
		{
			if (r.nextDouble() < mutationRate)
				b[j] = !a[j];
			else
				b[j] = a[j];
		}
		
		Individual mutated = new Individual(fitnessFunction, b);
		
		return mutated;
	}
	
	private List<List<Individual>> fastNonDominatedSort(Population pop)
	{
		Set<Individual> assigned = new HashSet<>(pop.getLength());
		// keep track of what has already been assigned to a front
		
		List<List<Individual>> fronts = new ArrayList<List<Individual>>();
		fronts.add(new ArrayList<Individual>()); // initialize first front
		
		
		// block A
		// loop over all individuals in population and fill dom counts / sets
		for (int p = 0; p < pop.getLength(); p++)
		{
			pop.get(p).dominationCount = 0;
			pop.get(p).dominatedSet = new HashSet<Individual>();
			
			for (int q = 0; q < pop.getLength(); q++)
			{
				if (p != q)
				{// don't compare with self
					if (pop.get(p).dominates(pop.get(q)))
					{// if p dominates q
						pop.get(p).dominatedSet.add(pop.get(q));
						// add q to the set of solutions dominated by p
					}
					else if (pop.get(q).dominates(pop.get(p)))
					{
						pop.get(p).dominationCount++;
						// increment the domination counter of p
					}
				}
			}
			
			if (pop.get(p).dominationCount == 0)
			{// p belongs to first front
				pop.get(p).rank = 0;
				fronts.get(0).add(pop.get(p));
				assigned.add(pop.get(p));
			}
		}
		
		
		// block B
		int i = 0; // initialize the front counter; using base 0 not 1
		
		for (List<Individual> frontI = fronts.get(i); frontI.size() > 0; frontI = fronts.get(i))
		{
			List<Individual> nextF = new ArrayList<Individual>();
			// used to store members of the next front
			
			for (Individual p : frontI)
			{
				for (Individual q : p.dominatedSet)
				{
					q.dominationCount--;
					if (q.dominationCount == 0)
					{
						q.rank = i + 1;
						nextF.add(q);
					}
				}
			}
			
			i++;
			fronts.add(i, nextF);
		}
		
		
		return fronts;
	
	}
	
	private int evaluatePopulation(Population pop, int threads)
	{
		int numPerThread = pop.getLength() / threads;
		EvaluationThread[] evalThread = new EvaluationThread[threads];
		
		for (int i = 0; i < threads; i++)
		{
			evalThread[i] = new EvaluationThread(pop, i * numPerThread, (i < threads - 1) ? (i + 1) * numPerThread : pop.getLength());
			evalThread[i].start();
		}
		
		int evals = 0;
		for (int i = 0; i < threads; i++)
		{
			try {
				evalThread[i].join();
			}
			catch (Exception e) 
			{
				// TODO: handle exception
				if (evalThread[i].isAlive())
					i--;
			}
			
			
			if (!evalThread[i].isAlive())
				evals += evalThread[i].evalsConsumed;
		}
		
		return evals;
	}
	
	private class EvaluationThread extends Thread
	{
		private Population pop;
		
		private int start;
		
		private int end;
		
		private int evalsConsumed;
		
		public EvaluationThread(Population pop, int start, int end)
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
				Individual s = pop.get(i);
				s.evaluate();
			}
		}
	}
}

































package Optimisation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import WindowShading.WindowShadingFitnessFunction;
import main.Loader;
import plotting.HyperVolume;
import regression.Model;
import ui.FacadeUI;
import ui.VisualisePopulation_E;

/**
 * Class for the Non-dominated Sorting Genetic Algorithm.
 * 
 * @author Ernest Vanmosuinck
 */
public class NSGA2_E
{
	private FitnessFunction ff;
	/** Random object. */
	private Random r;

	/** The number of windows on a building's façade. */
	private int windowsCount = 120;
	/** Number of solutions per evaluations. */
	private int numSolutions = 100;
	/** Number of threads running at the same time. */
	private int numThreads = 10;
	/** Total number of evaluations. */
	private int maxEvals = 1;
	
	// ************* NSGA-2 options *************
	/** Selection rate. */
	private double selectionRate = 0.5;
	/** Crossover rate. */
	private double crossoverRate = 0.5;
	/** Mutation rate. */
	private double mutationRate = 0.25; // 0 = low, 1 = high
	
	/** Surrogate model object. */
	private Model model;
	
	private double[][] presetData;

	private boolean energyplus = false;
	private boolean condition;
	private boolean useCondition = false;

	private Instant start;
	private Instant end;
	
	/**
	 * Constructor object for the NSGA.
	 */
	public NSGA2_E()
	{
		ff = new WindowShadingFitnessFunction(false, true);
		r = new Random();
		// capture the time when the NSGA starts
		start = Instant.now();
	}

	/**
	 * This method is used to train the model before starting the NSGA-2.
	 */
	public void prebuildModel()
	{
		presetData = Loader.load();
		model = new Model(presetData);
		model.go();
		try
		{
			System.out.println("Model eval: " + model.getEvaluation().correlationCoefficient());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to start the optimization algorithm.
	 */
	public void go()
	{
		System.out.println("started NSGA-II");

		VisualisePopulation_E vp = new VisualisePopulation_E();
		
		
		Individual[] initial = new Individual[numSolutions];
		initial[0] = new Individual(new boolean[windowsCount]);
		for (int i = 1; i < initial.length; i++)
		{
			initial[i] = new Individual(windowsCount, r);
		}
		evaluatePopulation(initial, energyplus);
		initial = ascendList(nonDominatedSort(initial));
		
		
		
		Individual[] offspring = createOffspring(initial);
		evaluatePopulation(offspring, energyplus);

		
		double firstPopulationHypervolume = HyperVolume.hypervolume(initial);

		
		int currentEval = 0;
		while (currentEval < maxEvals)
		{
			if (useCondition)
			{
				condition = currentEval % (maxEvals / 4) == 0;
				
				if (condition && currentEval != 0)
				{// if the condition is true, the system will re-train the model from the dataset, adding a population evaluated using E+
					// create a copy for current population
					Individual[] copy = new Individual[initial.length];
					for (int i = 0; i < copy.length; i++)
						copy[i] = new Individual(initial[i].getAlleles());
					// evaluate copy
					evaluatePopulation(copy, true);
					
					// create a 2D arrays for all the individuals 120 windows + energy consumption.
					double[][] solutionsToAdd = new double[initial.length][initial[0].getAlleles().length + 1];
					for (int i = 0; i < initial.length; i++)
					{// assign each solutions
						for (int j = 0; j < initial[i].getAlleles().length; j++)
							solutionsToAdd[i][j] = initial[i].getAlleles()[j] ? 1 : 0;
						solutionsToAdd[i][solutionsToAdd[i].length - 1] = initial[i].getFitness1();
					}
					
					// create a new training data 2D array
					double[][] newTrainingData = new double[presetData.length + solutionsToAdd.length][presetData[0].length];
					int tempPointer = 0;
					for (int i = 0; i < presetData.length; i++)
						newTrainingData[tempPointer++] = presetData[i];
					for (int i = 0; i < solutionsToAdd.length; i++)
						newTrainingData[tempPointer++] = solutionsToAdd[i];
					
					// set training data to new data
					presetData = newTrainingData;
					
					// set the model's training data
					model.setSet(newTrainingData);
					// build the model again
					model.go();
					
//					try
//					{
//						System.out.println("Model eval: " + model.getEvaluation().correlationCoefficient());
//					}
//					catch (Exception e)
//					{
//						e.printStackTrace();
//					}
				}
			}
			
			
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

			List<List<Individual>> fronts = nonDominatedSort(R);

			initial = new Individual[numSolutions];
			int pointer = 0;
			List<Individual> nextFront = fronts.remove(0);
			while (pointer + nextFront.size() <= initial.length)
			{
				for (int i = 0; i < nextFront.size(); i++)
					initial[pointer++] = nextFront.get(i);
				nextFront = fronts.remove(0);
			}

			if (pointer < initial.length)
			{
				crowdingDistance(nextFront);
				Individual[] nextFrontArray = nextFront.toArray(new Individual[nextFront.size()]);

				Arrays.sort(nextFrontArray, new Individual.CrowdingDistanceComparator());

				for (int i = 0; pointer < initial.length; i++)
				{
					initial[pointer] = nextFrontArray[i];
					pointer++;
				}
			}

			offspring = createOffspring(initial);
			evaluatePopulation(offspring, energyplus);

			
			
			currentEval++;
			// System.out.println("eval: " + currentEval);
		}
		
		// capture the time when the loop ends
		end = Instant.now();

		System.out.println("DONE");
		vp.updatePopulation(initial);

		double lastPopulationHypervolume = HyperVolume.hypervolume(initial);
		
		System.out.println("First pop HV: " + firstPopulationHypervolume);
		System.out.println("Last pop HV:  " + lastPopulationHypervolume);
		System.out.println("Improvement HV: " + (lastPopulationHypervolume - firstPopulationHypervolume));
		System.out.println("Percentage HV: " + (lastPopulationHypervolume - firstPopulationHypervolume)/ lastPopulationHypervolume*100);
		
		// Display the pareto solutions
		for (Individual i : initial)
			if (i.rank == 0)
				new FacadeUI(i);
		
		Individual[] energyPlus = new Individual[initial.length];
		for (int i = 0; i < energyPlus.length; i++)
			energyPlus[i] = new Individual(initial[i].getAlleles());
		evaluatePopulation(energyPlus, true);
		
		System.out.println(Duration.between(start, end));
		
//		calculateCorrelations(initial, energyPlus);
		
//		ResultsWriter.writeResults(maxEvals, initial, energyPlus, firstPopulationHypervolume, lastPopulationHypervolume, Duration.between(start, end), useCondition);
	}

	/**
	 * This method calculates calls the method that calculte  the MAE and Spearman correlation.
	 * 
	 * @param surrogate The last population evaluated with the model.
	 * @param energyplus The last population evaluated with EnergyPlus.
	 */
	private void calculateCorrelations(Individual[] surrogate, Individual[] energyplus)
	{
		System.out.println("Surrogate");
		double[] surrogateFitness = new double[surrogate.length];
		for (int i = 0; i < surrogate.length; i++)
		{
			surrogateFitness[i] = surrogate[i].getFitness1();
			System.out.println(surrogate[i].toString());
		}
		
		System.out.println("EnergyPlus");
		double[] energyFitness = new double[energyplus.length];
		for (int i = 0; i < energyplus.length; i++)
		{
			energyFitness[i] = energyplus[i].getFitness1();
			System.out.println(energyplus[i].toString());
		}
			
		// DIFFERENT CORRELATIONS SECTIONS
		System.out.println("MAE : " + calculateMAE(energyFitness, surrogateFitness));
		System.out.println("SPEARMAN : " + calculateSpearmanCorrel(energyFitness, surrogateFitness));
	}
	
	/**
	 * This method calculates the average MAE.
	 * 
	 * @param energyF The last population evaluated with EnergyPlus.
	 * @param surrogateF The last population evaluated with the model.
	 * 
	 * @return The Mean Absolute Error.
	 */
	private double calculateMAE(double[] energyF, double[] surrogateF)
	{
		double mae = 0;
		for (int i = 0; i < energyF.length; i++)
			mae += (energyF[i] - surrogateF[i]);
		
		mae /= energyF.length;
		
		return mae;
	}
	
	/**
	 * This method returns the Spearman correlation for passed parameters.
	 * 
	 * @param energyF The last population evaluated with EnergyPlus.
	 * @param surrogateF The last population evaluated with the model.
	 * 
	 * @return The Spearman correlation.
	 */
	private double calculateSpearmanCorrel(double[] energyF, double[] surrogateF)
	{
		return new SpearmansCorrelation().correlation(energyF, surrogateF);
 	}
	
	/**
	 * This method evaluates the passed array of Individuals.
	 * Uses Threads to evaluate a population faster.
	 * 
	 * @param P The population to evaluate.
	 * @param energyplus a boolean value that determines if the evaluator need to use EnergyPlus. {@code true} if the evaluator uses EnergyPlus, {@code false} otherwise.
	 */
	private void evaluatePopulation(Individual[] P, boolean energyplus)
	{
		EvaluationThread[] evals = new EvaluationThread[numThreads];
		int numberPerThreads = numSolutions / numThreads;

		if (!energyplus)
			for (int i = 0; i < numThreads; i++)
			{
				evals[i] = new EvaluationThread(P, numberPerThreads * i, ((i < numThreads - 1) ? (numberPerThreads * (i + 1)) : P.length));
				evals[i].start();
			}
		else
			for (int i = 0; i < numThreads; i++)
			{
				evals[i] = new EvaluationThread(P, numberPerThreads * i, ((i < numThreads - 1) ? (numberPerThreads * (i + 1)) : P.length), true);
				evals[i].start();
			}

		for (int i = 0; i < numThreads; i++)
		{
			try
			{
				evals[i].join();
			}
			catch (InterruptedException e)
			{
				if (evals[i].isAlive())
					i--;
			}
		}
	}

	/**
	 * This method turns the passed front into an array of Individuals.
	 * 
	 * @param fronts The front of individuals.
	 * 
	 * @return An array of Individual objects.
	 */
	private Individual[] ascendList(List<List<Individual>> fronts)
	{
		List<Individual> P = new ArrayList<>();

		for (List<Individual> front : fronts)
			for (Individual i : front)
				P.add(i);

		return P.toArray(new Individual[P.size()]);
	}

	/**
	 * This method sorts Individuals in order of their domination rank over each other using the non-dominated sort.
	 * Individuals non dominated by others will be attributed the rank 0, the next ones rank 1, etc.
	 * 
	 * @param pop The population to sort.
	 * @return A list of list of fronts.
	 */
	private List<List<Individual>> nonDominatedSort(Individual[] pop)
	{
		// Initialize
		Set<Individual> assigned = new HashSet<Individual>(pop.length);
		
		// Keep track of what's been already assigned to a front
		List<List<Individual>> fronts = new ArrayList<List<Individual>>();
		fronts.add(new ArrayList<Individual>());// init first front

		// ====== block A =========
		// loop over all individuals in population and fill dom counts / sets
		for (int _this = 0; _this < pop.length; _this++)
		{
			pop[_this].dominationCount = 0;
			pop[_this].dominatedSet = new HashSet<Individual>();

			for (int _that = 0; _that < pop.length; _that++)
			{
				if (_this != _that)
				{ // don't compare with self
					if (pop[_this].dominates(pop[_that]))
					{ // if _this dominates _that
						pop[_this].dominatedSet.add(pop[_that]); // add _that to the set of solutions dominated by _this
					} 
					else if (pop[_that].dominates(pop[_this]))
					{
						pop[_this].dominationCount++; // increment the domination counter of _this
					}
				}
			}

			if (pop[_this].dominationCount == 0)
			{ // _this belongs to the first front
				pop[_this].rank = 0; 
				fronts.get(0).add(pop[_this]);
				assigned.add(pop[_this]);
			}
		}

		// =========== block B ==========
		int frontCounter = 0; // initialise the front counter; using base 0 not 1
		for (List<Individual> fi = fronts.get(frontCounter); fi.size() > 0; fi = fronts.get(frontCounter))
		{ 
			List<Individual> Q = new ArrayList<Individual>(); // Used to store members of the next front
			for (Individual p : fi)
			{
				for (Individual q : p.dominatedSet)
				{
					q.dominationCount--;
					if (q.dominationCount == 0)
					{
						q.rank = frontCounter + 1;
						Q.add(q);
					}
				}
			}
			frontCounter++;
			fronts.add(frontCounter, Q);
		}

		return fronts;
	}

	/**
	 * This method calculates the crowding distance for the List of fronts.
	 * 
	 * @param individuals The front to calculate the distance.
	 */
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

	/**
	 * Creates an offspring population from the passed population.
	 * 
	 * @param parents The parent population to create the offspring population from.
	 * @return The offspring population.
	 */
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

	/**
	 * Selects the parents for creating the offspring individual based on the selection rate.
	 * 
	 * @param P The parent population.
	 * @return A random individual from the population.
	 */
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

	/**
	 * Picks which allele is carried over from the parent based on the crossover rate.
	 * 
	 * @param parent1 The first parent.
	 * @param parent2 The second parent.
	 * @return The offspring population.
	 */
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
		generatedOffspring[0] = new Individual(next1);
		generatedOffspring[1] = new Individual(next2);

		return generatedOffspring;
	}

	/**
	 * Mutates the individual based on the mutation rate.
	 * 
	 * @param offspring The offspring individual.
	 * @return The possibly mutated offspring individual.
	 */
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

		return new Individual(mutated);
	}

	/**
	 * EvaluationThread class that will evaluate a population.
	 * 
	 * @author Ernest Vanmosuinck
	 */
	class EvaluationThread extends Thread
	{
		/** The population to evaluate. */
		private Individual[] individuals;
		/** The index in the array to start evaluating from. */
		private int startIndex;
		/** The index in the array to start evaluating. */
		private int endIndex;
		/** Whether or not the evaluation has to use EnergyPlus. */
		private boolean energyplus;

		/**
		 * Constructor for the EvaluationThread object. 
		 * Uses the surrogate model to evaluate fitness.
		 * 
		 * @param individuals The population to evaluate.
		 * @param startIndex The index to start evaluating from.
		 * @param endIndex The index to stop evaluating from.
		 */
		public EvaluationThread(Individual[] individuals, int startIndex, int endIndex)
		{
			this.individuals = individuals;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.energyplus = false;
		}
		
		/**
		 * Constructor for the EvaluationThread object. 
		 * Uses EnergyPlus to evaluate fitness.
		 * 
		 * @param individuals The population to evaluate.
		 * @param startIndex The index to start evaluating from.
		 * @param endIndex The index to stop evaluating from.
		 */
		public EvaluationThread(Individual[] individuals, int startIndex, int endIndex, boolean energyplus)
		{
			this.individuals = individuals;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.energyplus = true;
		}

		public void run()
		{
			for (int j = startIndex; j < endIndex; j++)
			{
				Individual i = individuals[j];
				if (!energyplus)
					i.surrogateEvaluate(model);
				else
					i.energyPlusEvaluate(ff);
				// System.out.println("SINGLE INDIVIDUAL : " + i.getFitness1() + " " + i.getFitness2());
			}
		}
	}

}
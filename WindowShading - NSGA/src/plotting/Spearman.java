 package plotting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.*;

public class Spearman
{
	SpearmansCorrelation sp;
	
	private double[] energyPlusFitness;
	private double[] surrogateFitness;
	
	// Spearman parameters
	private Rank[] rankedEnergyPlus;
	private Rank[] rankedSurrogate;
	private double coef;
	
	public Spearman(double[] energyPlusF, double[] surrogateF)
	{
		sp = new SpearmansCorrelation();
		this.energyPlusFitness = energyPlusF;
		this.surrogateFitness = surrogateF;
	}
	
	public double calcCorrel()
	{
		return sp.correlation(energyPlusFitness, surrogateFitness);
	}
	
//	public Spearman(double[] energyPlusF, double[] surrogateF)
//	{
//		rankedEnergyPlus = assignRank(energyPlusF);
//		rankedSurrogate = assignRank(surrogateF);
//	}
	
	private Rank[] assignRank(double[] fitnessVals)
	{
		List<Double> fitnessList = new ArrayList<>();
		
		for (double f : fitnessVals)
			fitnessList.add(f);
		
		Collections.sort(fitnessList);
		Collections.reverse(fitnessList);
		
		Rank[] ranks = new Rank[fitnessList.size()];
		
		for (int i = 0; i < fitnessVals.length; i++)
			ranks[i] = new Rank(fitnessList.indexOf(fitnessVals[i]) + 1, fitnessVals[i]);
			
		return ranks;
	}
	
	public double calculateCorrelation()
	{
		double[] d = new double[rankedEnergyPlus.length];
		double[] d2 = new double[rankedEnergyPlus.length];
		for (int i = 0; i < d.length; i++)
			d[i] = (rankedEnergyPlus[i].getRank() - rankedSurrogate[i].getRank());
		for (int i = 0; i < d.length; i++)
		{
			d2[i] = d[i] * d[i]; // TODO fix
			System.out.println(d[i]);
		}
		
		double sum = 0;
		for (int i = 0; i < d.length; i++)
			sum += d2[i];
		
		System.out.println("SUM : " + sum);
		
		double size = rankedEnergyPlus.length;
		coef = 1 - ((6 * sum) / (size * ((size * size) - 1)));
		
		return coef;
	}
}


class Rank  
{
	private int rank;
	private double fitness;
	
	public Rank(int r, double val)
	{
		this.rank = r;
		this.fitness = val;
	}

	public double getRank()
	{
		return rank;
	}
	
	public String toString()
	{
		return fitness + " " + rank;
	}
}
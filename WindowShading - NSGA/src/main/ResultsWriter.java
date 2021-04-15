package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import Optimisation.Individual;


/**
 * Used to write the results obtained into a text file.
 * @author Ernest Vanmosuinck
 *
 */
public class ResultsWriter
{	
	//public static void writeResults(int evalcount, Individual [] surrogate, Individual[] energyPlus, double fh, double lh)
	public static void writeResults(int evalcount, Individual [] surrogate, Individual[] energyPlus, double fh, double lh, Duration timetaken, boolean conditionUsed)
	{
		System.out.println("here here");
		
		try {
			String title = LocalDateTime.now().toString().replace("-", "_");
			title = title.replace(":", "_");
			title = title + "_" + evalcount;
			File f = new File("C:/Users/558386/Desktop/Files/" + evalcount + (conditionUsed ? "_used/":"_notUsed/") + title + ".txt");
			PrintWriter pw = null;
//			System.out.println("file created");
			
			pw = new PrintWriter(new FileOutputStream(f));

			pw.write("Evaluation counts : " + evalcount);
			pw.write(System.lineSeparator());
			
			pw.write("Surrogate");
			for (Individual s : surrogate)
			{
				pw.write("Energy : " + s.getFitness1() + ", Cost " + s.getFitness2() + ", Rank : " + s.rank);
				pw.write(System.lineSeparator());
			}
			
			pw.write("EnergyPlus");
			for (Individual e : energyPlus)
			{
				pw.write("Energy : " + e.getFitness1() + ", Cost " + e.getFitness2() + ", Rank : " + e.rank);
				pw.write(System.lineSeparator());
			}
			
			pw.write("Hypervolume : " + (lh - fh));
			pw.write(System.lineSeparator());
			
			pw.write("MAE : " + calculateMAE(surrogate, energyPlus));
			pw.write(System.lineSeparator());
			pw.write("Spearman : " + calculateSpearman(surrogate, energyPlus));
			pw.write(System.lineSeparator());
			pw.write(System.lineSeparator());
			pw.write("Time taken : " + timetaken.toString());
			pw.write(System.lineSeparator());
			pw.write("Used EnergyPlus condition? " + (conditionUsed ? "yes" : "no"));
			pw.write(System.lineSeparator());
			pw.close();
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param surrogate
	 * @param energy
	 * @return
	 */
	private static double calculateMAE(Individual[] surrogate, Individual[] energy)
	{
		System.out.println("Surrogate");
		double[] surrogateFitness = new double[surrogate.length];
		for (int i = 0; i < surrogate.length; i++)
		{
			surrogateFitness[i] = surrogate[i].getFitness1();
			System.out.println(surrogate[i].toString());
		}
		
		System.out.println("EnergyPlus");
		double[] energyFitness = new double[energy.length];
		for (int i = 0; i < energy.length; i++)
		{
			energyFitness[i] = energy[i].getFitness1();
			System.out.println(energy[i].toString());
		}
		
		return calculateMAEindiv(energyFitness, surrogateFitness);
	}
	
	
	private static double calculateSpearman(Individual[] surrogate, Individual[] energy)
	{
		System.out.println("Surrogate");
		double[] surrogateFitness = new double[surrogate.length];
		for (int i = 0; i < surrogate.length; i++)
		{
			surrogateFitness[i] = surrogate[i].getFitness1();
			System.out.println(surrogate[i].toString());
		}
		
		System.out.println("EnergyPlus");
		double[] energyFitness = new double[energy.length];
		for (int i = 0; i < energy.length; i++)
		{
			energyFitness[i] = energy[i].getFitness1();
			System.out.println(energy[i].toString());
		}
			
		return new SpearmansCorrelation().correlation(surrogateFitness, energyFitness);
	}
	
	private static double calculateMAEindiv(double[] energyF, double[] surrogateF)
	{
		double mae = 0;
		for (int i = 0; i < energyF.length; i++)
			mae += (energyF[i] - surrogateF[i]);
		
		mae /= energyF.length;
		
		return mae;
	}
	
}

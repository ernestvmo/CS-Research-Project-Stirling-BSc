package WindowShading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

import Optimisation.Individual;

public class WindowShadingProblem
{
	/** 120 windows in facade, unlikely to change */
	protected static final int WINDOW_NUMBER = 120;

	/** default path to E+ run script */
	protected String energyPlusPath;

	/**
	 * working directory; a place to store the evaluated IDFs and outputs -
	 * subdirs made for each simulation
	 */
	private volatile String workingDir; // = "C:\\sb\\WindowShading";

	/** read this file to get template idf file */
	public volatile String templatePath; // = OUTPUT_PATH +
											// "\\WindowShadingFileWithOverhangsAndFins.tpt";

	private static final String INPUTFILE_NAME_NOEXTENSION = "Shading";
	private static final String INPUTFILE_NAME = INPUTFILE_NAME_NOEXTENSION
			+ ".idf"; // name given to EP input files
	private static String OUTPUTFILE_NAME;// = "ShadingMeter.csv"; // name given
											// to EP output files - depends on
											// OS!
	private static volatile String WEATHERFILE_NAME = "USA_IL_Chicago-OHare.Intl.AP.725300_TMY3";
	private static final String DIR_SEPARATOR = File.separator;

	/** used to name the temp dirs and keep track of evaluations */
	protected static volatile int evalCount = 0;

	/** are we applying the window aspect ratio constraint? */
	private boolean constrained;

	// memory stuff
	private boolean useMemory;
	private int memorySize;
	private volatile int nextMemoryPosition;
	private boolean memoryFull;
	private boolean[][] memoryPopulation;
	private double[] memoryEnergy;
	private double[] memoryCost;
	private int[] memoryIDs;

	private boolean onWindows = true;

	private boolean showProgress;

	protected static final boolean[] ALL_FALSE = new boolean[WINDOW_NUMBER]; // used
																				// to
																				// switch
																				// off
																				// all
																				// fins
	static
	{
		java.util.Arrays.fill(ALL_FALSE, false);
	}

	public WindowShadingProblem(boolean onWindows, String workingDir,
			String templatePath, boolean constrained, boolean useMemory,
			int memorySize)
	{
		this.onWindows = onWindows;
		setDefaults();
		this.useMemory = useMemory;
		if (useMemory)
		{
			setupMemory(memorySize);
		}

		this.constrained = constrained;
		this.workingDir = workingDir;
		this.templatePath = templatePath;
		OUTPUTFILE_NAME = onWindows ? "ShadingMeter.csv" : "Shadingmtr.csv";
	}

	private void setDefaults()
	{
		if (onWindows)
		{
			this.energyPlusPath = "N:\\EnergyPlusV9-3-0\\RunEPlus.bat";
		} else
		{
			this.energyPlusPath = "EnergyPlus";
		}
		this.showProgress = false;
	}

	/** set to true to output eval numbers as they are evaluated */
	public void setShowProgress(boolean showProgress)
	{
		this.showProgress = showProgress;
	}

	private void setupMemory(int memorySize)
	{
		this.memorySize = memorySize;
		this.memoryPopulation = new boolean[memorySize][];
		this.memoryEnergy = new double[memorySize];
		this.memoryCost = new double[memorySize];
		this.memoryIDs = new int[memorySize];
		Arrays.fill(this.memoryIDs, -1);
		this.memoryFull = false;
		this.nextMemoryPosition = 0;
	}

	public void setEnergyPlusPath(String energyPlusPath)
	{
		this.energyPlusPath = energyPlusPath;
	}

	public static void setWeatherFileName(String weatherFileName)
	{
		WEATHERFILE_NAME = weatherFileName;
	}

	/**
	 * evaluate the objectives for a solution; the solution is specified as a
	 * 240 bit array
	 */
	public WindowShadingProblem.WindowShadingFitness evaluateObjectives(
			boolean[] solution)
	{
		WindowShadingFitness rval = new WindowShadingFitness();

		// if we evaluated the same solution before, but it's a different object
		// instance, it'll be in the memory, so look there first
		if (this.useMemory)
		{
			int memPos = checkMemory(solution);
			if (memPos > -1)
			{
				rval.cost = memoryCost[memPos];
				rval.energy = memoryEnergy[memPos];
				rval.retrievedFromMemory = true;
				rval.solutionNumber = memoryIDs[memPos];

				return rval;
			}
		}

		// make a bitstring for the windows
		boolean[] bitstringWindows = new boolean[WINDOW_NUMBER];
		for (int i = 0; i < bitstringWindows.length; i++)
		{
			bitstringWindows[i] = solution[i];
		}
		// make a bitstring for the overhangs
		boolean[] bitstringOverhangs = new boolean[WINDOW_NUMBER];
		for (int i = 0; i < bitstringOverhangs.length; i++)
		{
//			bitstringOverhangs[i] = solution[i + WINDOW_NUMBER];
			bitstringOverhangs[i] = false;
		}

		boolean[] bitstringFins = ALL_FALSE;
		boolean bitstringFinsLeftRight = false;

		// what run are we on?
		final int localEvalNo;
		synchronized (energyPlusPath)
		{
			localEvalNo = evalCount++;
		}

		rval.solutionNumber = localEvalNo;
		rval.retrievedFromMemory = false;

		// useful for debug...
		if (showProgress)
		{
			synchronized (System.out)
			{
				System.out.println("Eval number " + localEvalNo);
			}
		}

		// the difficult bit is objective 1, we need to generate an EP input
		// file from the template, run EP and parse the output...
		double energy = energy(bitstringWindows, bitstringOverhangs,
				bitstringFins, bitstringFinsLeftRight, localEvalNo);
		double cost = cost(bitstringWindows, bitstringOverhangs, bitstringFins);

		rval.energy = energy;
		rval.cost = cost;

		// add to memory
		if (useMemory)
		{
			addToMemory(solution, energy, cost, localEvalNo);
		}

		// we're done!
		return rval;
	}

	/**
	 * @return an array holding the constraint violations if 0 - no violation;
	 *         anything > 0 is a violation these aren't wrapped into a total
	 *         violation value here because some algorithms like to have
	 *         separate constraint counts and we want to allow for possible
	 *         future implementation of them.
	 */
	public double[] constraints(boolean[] bitstringWindows)
	{
		if (!constrained)
		{
			return new double[]{0, 0};
		}

		WindowConstraintEvaluator cons = new WindowConstraintEvaluator();
		try
		{
			double constr1 = cons.evaluateConstraintFunction(bitstringWindows,
					"0,120,aspectRatio.MIN,-1.0", 1.5);
			double constr2 = cons.evaluateConstraintFunction(bitstringWindows,
					"0,120,aspectRatio.MAX,1.0", 1.75);
			return new double[]{Math.max(0, constr1), Math.max(0, constr2)}; // flatten
																				// the
																				// values,
																				// anything
																				// less
																				// than
																				// 0
																				// is
																				// not
																				// a
																				// violation
																				// anyway.
		}
		catch (Exception e)
		{
			System.err.println("problem with constr calc:");
			e.printStackTrace();
		}

		return new double[0];
	}

	/** objective 2 */
	protected double cost(boolean[] windows, boolean[] overhangs,
			boolean[] fins)
	{
		int numberOfWindows = 0;
		int numberOfOverhangs = 0;
		int numberOfFins = 0;
		// only count overhangs / fins if window is present
		for (int i = 0; i < WINDOW_NUMBER; i++)
		{
			numberOfWindows += windows[i] ? 1 : 0;
			numberOfOverhangs += overhangs[i] && windows[i] ? 1 : 0;
			numberOfFins += fins[i] && windows[i] ? 1 : 0;
		}

		double windowCost = 112 * (120 - numberOfWindows)
				+ 350 * numberOfWindows;
		double overhangCost = 128 * numberOfOverhangs;
		double finCost = 128 * numberOfFins;

		double totalCost = windowCost + overhangCost + finCost;

		return totalCost;
	}

	protected double energy(boolean[] bitstringWindows,
			boolean[] bitstringOverhangs, boolean[] bitstringFins,
			boolean bitstringFinsLeftRight, int localEvalNo)
	{
		// make output dir
		String outputPath = workingDir + DIR_SEPARATOR
				+ filenameZeroPad(localEvalNo);
		new File(outputPath).mkdir();

		// make EP file from template
		String templateFilename = templatePath;
		String epInputFilename = outputPath + DIR_SEPARATOR + INPUTFILE_NAME;
		makeFileFromTemplate(templateFilename, epInputFilename,
				bitstringWindows, bitstringOverhangs, bitstringFins,
				bitstringFinsLeftRight);

		// now run EPlus
		String[] cmd;
		if (onWindows)
		{
			String weatherfile = WEATHERFILE_NAME.toLowerCase().endsWith(".epw")
					? ".." + DIR_SEPARATOR
							+ WEATHERFILE_NAME.substring(0,
									WEATHERFILE_NAME.length() - 4)
					: WEATHERFILE_NAME;
			cmd = new String[]{energyPlusPath,
					"." + DIR_SEPARATOR + INPUTFILE_NAME_NOEXTENSION,
					weatherfile};
		} else
		{
			String weatherfile = !WEATHERFILE_NAME.toLowerCase().endsWith(
					".epw") ? WEATHERFILE_NAME + ".epw" : WEATHERFILE_NAME;
			cmd = new String[]{energyPlusPath, "-r", "-p",
					INPUTFILE_NAME_NOEXTENSION, "-w",
					".." + DIR_SEPARATOR + weatherfile,
					"." + DIR_SEPARATOR + "Shading.idf"};
		}

		try
		{
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(new File(outputPath));
			Process p = pb.start();
			BufferedReader r = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			// @SuppressWarnings("unused")
			String line;
			while (((line = r.readLine()) != null))
			{
				if (showProgress)
					System.out.println(line);
			}
			try
			{
				p.waitFor();
			}
			catch (InterruptedException e)
			{
			}
			// System.out.println("evald 1");
		}
		catch (IOException e)
		{
			System.err.println("Problem executing EP. Cmd: ");
			for (String string : cmd)
			{
				System.err.print(string);
			}
			System.err.println();
			e.printStackTrace();
		}

		// now parse output for objective
		double energy = getEnergyFromOutputFile(
				outputPath + DIR_SEPARATOR + OUTPUTFILE_NAME);

		// finally, tidy up
		tidyOutputPath(outputPath);

		return energy;
	}

	private double getEnergyFromOutputFile(String filename)
	{
		double rval = Double.MAX_VALUE; // make it high energy so if there's a
										// failure the solution is just
										// discarded
		BufferedReader in;
		try
		{
			// open template file
//			System.out.println(filename);
			in = new BufferedReader(new FileReader(filename));

			// skip first line
			in.readLine();

			String line = in.readLine(); // get line 2 (design day 1)
			String[] vars = line.split(",");

			// add columns 2,3,4 and divide by 3.6E6
			double a = Double.parseDouble(vars[1]);
			double b = Double.parseDouble(vars[2]);
			double c = Double.parseDouble(vars[3]);

			double a2 = 0;
			double b2 = 0;
			double c2 = 0;

			try
			{
				line = in.readLine(); // get line 3 (design day 2)
				if (line != null)
				{ // line is empty if doing full year run
					vars = line.split(",");

					// add columns 2,3,4 and divide by 3.6E6
					a2 = Double.parseDouble(vars[1]);
					b2 = Double.parseDouble(vars[2]);
					c2 = Double.parseDouble(vars[3]);
				}
			}
			catch (IOException e2)
			{
			} // if we couldn't read the third line, don't bother.

			rval = (a + b + c + a2 + b2 + c2) / 3.6E6;

			in.close();
		}
		catch (IOException e)
		{
			System.err.println(
					"errors when reading output file..." + e.toString());
			e.printStackTrace();
		}
		catch (NumberFormatException e)
		{
			System.err.println("error parsing output file");
			e.printStackTrace();
		}
		catch (Exception e)
		{
			System.err.println("Unknown error when reading output file");
			e.printStackTrace();
		}

		return rval;
	}

	private void makeFileFromTemplate(String templatePath, String outputPath,
			boolean[] windows, boolean[] overhangs, boolean[] fins,
			boolean finsLeftRight)
	{
		BufferedReader in;
		PrintStream out;
		try
		{
			// open template file
			in = new BufferedReader(new FileReader(templatePath));
			// open output file
			out = new java.io.PrintStream(
					new java.io.FileOutputStream(outputPath));

			// loop over all lines in the file
			String line;
			while ((line = in.readLine()) != null)
			{
				// line starts with a BOP_ marker?
				if (line.startsWith("BOP_NorthAxis"))
				{
					line = line.replaceFirst("BOP_NorthAxis", "0.0"); // fixed
																		// for
																		// now
				} else if (line.contains("BOP_"))
				{ // line contains an indexed BOP_ marker?
					// what's the marker?
					// String rawkey = line.substring(line.indexOf("BOP_") + 4,
					// line.indexOf("]"));
					// String[] keyParts = rawkey.split("\\[");
					// int index = Integer.parseInt(keyParts[1]) - 1;
					// if (keyParts[0].equals("WindowWall")) {
					// line = (windows[index] ? "F" : "!") +
					// line.substring(line.indexOf("]")+1);
					// } else if (keyParts[0].equals("Overhang")) {
					// line = ((overhangs[index] && windows[index]) ? "S" : "!")
					// + line.substring(line.indexOf("]")+1);
					// } else if (keyParts[0].equals("Fin")) {
					// line = (((finsLeft[index] || finsRight[index]) &&
					// windows[index]) ? "S" : "!") +
					// line.substring(line.indexOf("]")+1);
					// } else if (keyParts[0].equals("FinLeft")) { // bit below
					// allows for commenting out all three lines if neither fine
					// is present; otherwise we include the line but with a 0.0
					// size
					// line = (finsLeft[index] && windows[index] ? "0.3" :
					// finsRight[index] && windows[index]?"0.0":"!") +
					// line.substring(line.indexOf("]")+1);
					// } else if (keyParts[0].equals("FinRight")) {
					// line = (finsRight[index] && windows[index] ? "0.3" :
					// finsLeft[index] && windows[index]?"0.0":"!") +
					// line.substring(line.indexOf("]")+1);
					// }

					// split line by "BOP_"
					String[] parts = line.split("BOP_");

					// now, for each of the bits of the split line, parse for
					// tokens and replace as appropriate
					line = "";
					for (String part : parts)
					{
						// split by [ and ] to get the key name, number, and
						// bits after
						String[] keyParts = part.split("[\\[\\]]");

						if (keyParts.length > 1)
						{ // ie a key found
							int index = Integer.parseInt(keyParts[1]) - 1;

							if (keyParts[0].equals("WindowWall"))
							{
								line += (windows[index] ? "F" : "!");
							} else if (keyParts[0].equals("Overhang"))
							{
								line += ((overhangs[index] && windows[index])
										? "S"
										: "!");
							} else if (keyParts[0].equals("Fin"))
							{
								line += ((fins[index] && windows[index])
										? "S"
										: "!");
							} else if (keyParts[0].equals("FinLeft"))
							{
								line += (!finsLeftRight && fins[index]
										&& windows[index] ? "0.3" : "0.0");
							} else if (keyParts[0].equals("FinRight"))
							{
								line += (finsLeftRight && fins[index]
										&& windows[index] ? "0.3" : "0.0");
							}

							// always add last bit
							line += keyParts[2];
						} else
						{
							line += keyParts[0];
						}
					}
				}

				// output line
				out.println(line);
			}

			in.close();
			out.close();
		}
		catch (IOException e)
		{
			System.err.println(
					"errors when generating input file..." + e.toString());
			e.printStackTrace();
		}
	}

	/** delete output directory once we're done. */
	private void tidyOutputPath(String path)
	{
		// check folder exists before proceeding
		File file = new File(path);
		if (file.exists() && file.isDirectory())
		{
			deleteDirectory(file);
		}
	}

	static public boolean deleteDirectory(File path)
	{
		if (path.exists())
		{
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					deleteDirectory(files[i]);
				} else
				{
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private static String filenameZeroPad(int number)
	{
		if (number < 10)
		{
			return "0000000" + number;
		} else if (number < 100)
		{
			return "000000" + number;
		} else if (number < 1000)
		{
			return "00000" + number;
		} else if (number < 10000)
		{
			return "0000" + number;
		} else if (number < 100000)
		{
			return "000" + number;
		} else if (number < 1000000)
		{
			return "00" + number;
		} else if (number < 10000000)
		{
			return "0" + number;
		} else
		{
			return Integer.toString(number);
		}
	}

	/** @return -1 if not found, inde otherwise */
	private synchronized int checkMemory(boolean[] chrom)
	{
		// work backwards through memory as matches will be nearer end
		for (int i = (memoryFull ? memorySize : nextMemoryPosition)
				- 1; i >= 0; i--)
		{
			boolean match = true;
			// iterate over all solns in memory
			boolean[] cmp = memoryPopulation[i];
			for (int j = 0; match && (j < chrom.length); j++)
			{
				match &= (cmp[j] == chrom[j]);
			}

			if (match)
			{
				return i;
			}
		}

		return -1;
	}

	private synchronized void addToMemory(boolean[] chrom, double energy,
			double cost, int id)
	{
		memoryPopulation[nextMemoryPosition] = chrom;
		memoryEnergy[nextMemoryPosition] = energy;
		memoryCost[nextMemoryPosition] = cost;
		memoryIDs[nextMemoryPosition] = id;

		if (nextMemoryPosition >= (memorySize + 1))
		{
			nextMemoryPosition = 0;
		} else
		{
			nextMemoryPosition++;
		}
	}

	/** a wrapper class for the two objectives resulting from an evaluation */
	public static final class WindowShadingFitness
	{
		public double energy;
		public double cost;

		/**
		 * true if this solution wasn't evaluated from scratch, but had been
		 * evaluated before
		 */
		public boolean retrievedFromMemory;

		/**
		 * this evaluation number used for this solution (whether retrieved from
		 * memory or not)
		 */
		public int solutionNumber;
	}
}

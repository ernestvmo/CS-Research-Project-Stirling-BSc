package WindowShading;

import java.util.Random;

import Optimisation.FitnessFunction;
import Optimisation.Individual;

/**
 * just a wrapper for the generic WindowShadingProblem to make it fit into the
 * NSGA-II code
 */
public class WindowShadingFitnessFunction extends FitnessFunction
{
	private WindowShadingProblem wsp;
	private int evals;
	private boolean constraintShortcut;
	private boolean constrained;

	public WindowShadingFitnessFunction(boolean constrained, boolean fullYear)
	{
		super(WindowShadingProblem.WINDOW_NUMBER * 2); // *2 to allow for
														// overhangs

		// change the path below to match where the templates are stored
		// the first line is the full year sim - a lot slower (in the order of
		// 20-50x
		// slower) but more realistic problem with a harder to find Pareto front
		// second line is the design day sim, easier optimisation problem (both
		// problems
		// have the same constraints though)
		// the other params - constrained, useMemory and memorySize can probably
		// be left
		// as they are.
		// the first param - onWindows - is hopefully obvious!
		// wsp = new WindowShadingProblem(true, "c:\\sb\\WindowShading",
		// "c:\\sb\\WindowShading\\WindowShadingFileWithOverhangsAndFins-fullyear03Overhang.tpt",
		// true, false, 0);
		// wsp = new WindowShadingProblem(true, "c:\\sb\\WindowShading",
		// "c:\\sb\\WindowShading\\WindowShadingFileWithOverhangsAndFins03Overhang.tpt",
		// true, true, 10000);
		boolean onWindows = System.getProperty("os.name").contains("Win");
		String workingDirectory = onWindows
				? "."
				: "/home/sbr/workspace/WindowShadingTest";
		if (fullYear)
		{
			wsp = new WindowShadingProblem(onWindows, workingDirectory,
					"./WindowShadingFileWithOverhangsAndFins-fullyear03Overhang.tpt",
					true, false, 0);
		} else
		{
			wsp = new WindowShadingProblem(onWindows, workingDirectory,
					"./WindowShadingFileWithOverhangsAndFins03Overhang.tpt",
					true, true, 10000);
		}
		this.constrained = constrained;
		wsp.setShowProgress(false); // for debug
		this.evals = 0;
		this.constraintShortcut = true; // set to false to always calc
										// objectives even in infeasible
										// solutions
	}

	@Override
	public MOFitness evaluate(Individual i)
	{
		MOFitness mof = new MOFitness();

		if (constrained)
		{
			double[] constr = wsp.constraints(i.getAlleles());
			double totalViolation = 0;
			for (double d : constr)
			{
				if (d > 0)
				{ // violation
					totalViolation += d;
				}
			}
			mof.overallConstraintViolation = totalViolation;
		} else
		{
			mof.overallConstraintViolation = 0;
		}

		if (!this.constraintShortcut || (mof.overallConstraintViolation == 0))
		{
			WindowShadingProblem.WindowShadingFitness f = wsp
					.evaluateObjectives(i.getAlleles());
			if (!f.retrievedFromMemory)
			{
				this.evals++;
			}

			mof.fitness1 = f.energy;
			mof.fitness2 = f.cost;
		}

		return mof;
	}

	@Override
	public int getEvals()
	{
		return this.evals;
	}
}

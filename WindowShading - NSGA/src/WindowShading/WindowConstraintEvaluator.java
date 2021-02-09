package WindowShading;
import bop.models.window.cellular.CellularWindow;

public class WindowConstraintEvaluator
{

	public WindowConstraintEvaluator()
	{
		super();
	}

	/**
	 * trimmed version of constraint evaluator from BareboneBOP
	 * 
	 * typeData sequence: [0] index of first wall element in xsol [1] index of
	 * last wall element in xsol [2] function type [3] function multiplier
	 * (converts between <= constraint and >= constraint)
	 */
	public double evaluateConstraintFunction(boolean[] soln, String typeData,
			double bound) throws Exception
	{
		double[] xsol = new double[soln.length];
		for (int i = 0; i < soln.length; i++)
		{
			xsol[i] = soln[i] ? 1 : 0;
		}

		double ret = 0.0;
		// Wall width
		int width = 15;
		int height = 0;
		// String[] names = input.getNonSecificDataNames();
		// String[] values = input.getNonSecificDataValues();
		// for (int i=0; i<names.length; i++) {
		// if (names[i].matches("NumberCellsWide")) {width =
		// Integer.parseInt(values[i]); break;}
		// }

		// Wall vector length and position of wall variables in xsol
		String[] f_type = typeData.split(",");
		f_type[0].trim();
		f_type[1].trim();
		f_type[2].trim();
		f_type[3].trim();
		int iStrt = Integer.parseInt(f_type[0]);
		int iEnd = Integer.parseInt(f_type[1]);
		// Wall height
		height = (iEnd - iStrt + 1) / width;

		// Translate wall vector to matrix - invert for difference in origin
		int[][] wall = new int[height][width];
		int i_w = -1;
		int i_h = 0;
		for (int i = iStrt; i < iEnd; i++)
		{
			++i_w;
			if (i_w == width)
			{
				i_w = 0;
				++i_h;
			}
			// wall[i_h][i_w] = (int) xsol[i];
			wall[i_h][i_w] = (int) StrictMath.round(xsol[i]);
		}
		// Invert
		int[] temp = new int[width];
		for (int i = 0; i < height; ++i)
		{
			for (int j = 0; j < width; ++j)
			{
				temp[j] = wall[i][j];
			}
			for (int j = 0; j < width; ++j)
			{
				wall[i][j] = wall[(height - 1 - i)][j];
				wall[(height - 1 - i)][j] = temp[j];
			}
		}

		CellularWindow win = new CellularWindow(wall);

		// Calculate function value
		if (f_type[2].matches("number"))
		{
			ret = Double.parseDouble(f_type[3]) * Double.parseDouble(f_type[3])
					* (double) win.getNumberOfWindows() - bound;
		}

		// Window Area
		else if (f_type[2].matches("area.MIN"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getWindowPercentArea(win.MIN) - bound);
		} else if (f_type[2].matches("area.MAX"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getWindowPercentArea(win.MAX) - bound);
		} else if (f_type[2].matches("area.MEAN"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getWindowPercentArea(win.MEAN) - bound);
		} else if (f_type[2].matches("area.TOT"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getWindowPercentArea(win.TOT) - bound);
		}

		// Rectangular density
		else if (f_type[2].matches("rectangularDensity.MIN"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getDensityRatio(win.MIN) - bound);
		} else if (f_type[2].matches("rectangularDensity.MAX"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getDensityRatio(win.MAX) - bound);
		} else if (f_type[2].matches("rectangularDensity.MEAN"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getDensityRatio(win.MEAN) - bound);
		}

		// Aspect Ratio
		else if (f_type[2].matches("aspectRatio.MIN"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getAspectRatio(win.MIN) - bound);
		} else if (f_type[2].matches("aspectRatio.MAX"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getAspectRatio(win.MAX) - bound);
		} else if (f_type[2].matches("aspectRatio.MEAN"))
		{
			ret = Double.parseDouble(f_type[3])
					* (win.getAspectRatio(win.MEAN) - bound);
		}

		else
			throw new Exception(
					"WindowConstraintEvaluator: unknown metric; " + f_type[2]);

		return ret;
	}

}

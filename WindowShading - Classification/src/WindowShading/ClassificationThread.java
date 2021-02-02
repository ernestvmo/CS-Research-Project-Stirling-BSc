package WindowShading;

import weka.classifiers.Evaluation;

public class ClassificationThread extends Thread
{
	private double defaultLearningRate = 0.3;
	private double defaultMomemtum = 0.2;
	private int defaultLearningTime  = 500;
	private String defaultHiddenLayer = "a";
	private int folds = 5;
	
	private double upper;
	private double lower;
	private String focus;
	private double constant;
	
	public ClassificationThread(double val)
	{
		this.constant = val;
	}
	
	public ClassificationThread(int lower, int upper)
	{
		this.lower = lower;
		this.upper = upper;
	}
	
	public ClassificationThread(String focus, int lower, int upper, double constant)
	{
		this.focus = focus;
		this.upper = upper;
		this.lower = lower;
		this.constant = constant;
	}
	
	public void runn()
	{
		double bestCoef = 0;
		String bestRate = null;
		
		System.out.println("thread started");

		try
		{
			Classifier test;
			for (double i = lower; i <= upper; i++)
			{
				test = new Classifier(i / 100, defaultMomemtum, defaultLearningTime, defaultHiddenLayer, folds);
				test.classify(0);
				
				double coef = test.getEvaluation().correlationCoefficient();
				
				if (coef > bestCoef)
				{
					bestCoef = coef;
					bestRate = String.valueOf(i / 100);
				}
				System.out.println(i/100 + "  -  " + coef);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Coef: " + bestCoef + " | learning rate: " + bestRate);
	}
	
	public void run()
	{
		double bestD = 0;
		String bestMomentum = null;
		
		System.out.println("thread started");
		
		try
		{
			for (double i = 0; i <= 100; i++)
			{
				Classifier test;

				test = new Classifier(constant, i / 100, defaultLearningTime, defaultHiddenLayer, folds);
				test.classify(0);

				double coef = test.getEvaluation().correlationCoefficient();

				if (coef > bestD)
				{
					bestD = coef;
					bestMomentum = String.valueOf(i / 100);
				}
				System.out.println(i / 100 + " " + coef);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 

		System.out.println("Coef: " + bestD + " | learning rate: " + bestMomentum);

	}	
}

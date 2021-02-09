package WindowShading;

import weka.classifiers.Evaluation;

public class ClassificationThread extends Thread
{
	private double defaultLearningRate = 0.3;
	private double defaultMomemtum = 0.2;
	private int defaultLearningTime  = 500;
	private String defaultHiddenLayer = "60";
	private int folds = 5;
	
	private double upper;
	private double lower;
	private String focus;
	private double constant;
	
	public ClassificationThread(double val)
	{
		this.constant = val;
	}
	
	public ClassificationThread(String focus, int lower, int upper, double constant)
	{
		this.focus = focus;
		this.upper = upper;
		this.lower = lower;
		this.constant = constant;
	}
	
	public void run()
	{
		double bestD = 0;
		String bestLearningRate = null;
		
		System.out.println("thread started");
		
		try
		{
			for (double i = lower; i <= upper; i++)
			{
				Classifier test;

				if (focus.equals("learningRate"))
					test = new Classifier(constant, i / 100, defaultLearningTime, defaultHiddenLayer, folds);
				else
					test = new Classifier(i / 100, constant, defaultLearningTime, defaultHiddenLayer, folds);

				test.classify(0);

				double coef = test.getEvaluation().correlationCoefficient();

				if (coef > bestD)
				{
					bestD = coef;
					bestLearningRate = String.valueOf(i / 100);
				}
				System.out.println(i / 100 + " " + coef);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 

		if (focus.equals("learningRate"))
			System.out.println("Coef: " + bestD + " | learning rate: " + bestLearningRate);
		else
			System.out.println("Coef: " + bestD + " | momentum: " + bestLearningRate);
		
	}	
}

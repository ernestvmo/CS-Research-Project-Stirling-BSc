package WindowShading;

public class ClassificationThread extends Thread
{
	private double defaultLearningRate = 0.3;
	private double defaultMomemtum = 0.2;
	private int defaultLearningTime  = 500;
	private String defaultHiddenLayer = "a";
	private int folds = 5;
	
	private double upper;
	private double lower;
	
	public ClassificationThread(int lower, int upper)
	{
		this.upper = upper;
		this.lower = lower;
	}
	
	public void run()
	{
		double bestD = 0;
		String bestLearningRate = null;
		String bestMomentum = null;
		
		System.out.println("thread started");
		
		try
		{

			for (double i = lower; i < upper; i++)
			{
				for (double j = 1; j <= 99; j++)
				{
					Classifier test = new Classifier(i / 100, j / 100, defaultLearningTime, defaultHiddenLayer, folds);
					test.classify(0);

					double coef = test.getEvaluation().correlationCoefficient();

					if (coef > bestD)
					{
						bestD = coef;
						bestLearningRate = String.valueOf(i / 100);
						bestMomentum = String.valueOf(j / 100);
					}
					System.out.println(i / 100 + " " + j / 100 + " " + coef);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		} 

		System.out.println("Coef: " + bestD + " | learning rate: " + bestLearningRate + " | momentum: " + bestMomentum);
	}	
}

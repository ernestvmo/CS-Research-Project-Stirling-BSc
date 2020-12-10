package WindowShading;

import weka.classifiers.Evaluation;

public class RegressionClass
{
	public static void main(String[] args) throws Exception
	{
		double defaultLearningRate = 0.3;
		double defaultMomemtum = 0.2;
		int defaultLearningTime  = 500;
		String defaultHiddenLayer = "a";
		
		int folds = 5;
		
//		Classifier defaultClassified = new Classifier(0.35, 0.281, 250, "a", 5);
//		System.out.println("Default");
//		defaultClassified.classify(1);
		
		int x;
		
		// Current best = 0.6392
		
		Classifier defaultClassifier = new Classifier();
		Classifier optimized = new Classifier(0.35, 0.281, 250, "o", folds);
		
		Classifier[] classifiers = new Classifier[10];
		
		double bestD;
		String bestS;

		System.out.println("started");
		bestD = 0;
		bestS = "";
		
		for (double i = 10; i <= 30; i++)
		{
			Classifier test = new Classifier(i / 100, defaultMomemtum, defaultLearningTime, defaultHiddenLayer, folds);
			test.classify(0);
			
			double coef = test.getEvaluation().correlationCoefficient();
			
			if (coef > bestD)
			{
				bestD = coef;
				bestS = String.valueOf(i / 10);
			}
			System.out.println(coef);
		}
		
			System.out.println("population all ---> Coef: "+ bestD + ", learning rate: " + bestS);
		
//		// default
//		defaultClassifier.classify(0);
//		Evaluation de0 = defaultClassifier.getEvaluation();
//		System.out.println(de0.correlationCoefficient());
//		System.out.println("/////////////////////////////");
//		defaultClassifier.classify(1);
//		Evaluation de1 = defaultClassifier.getEvaluation();
//		System.out.println(de1.correlationCoefficient());
//		System.out.println("/////////////////////////////");
//		defaultClassifier.classify(2);
//		Evaluation de2 = defaultClassifier.getEvaluation();
//		System.out.println(de2.correlationCoefficient());
//		System.out.println("/////////////////////////////");
//		defaultClassifier.classify(3);
//		Evaluation de3 = defaultClassifier.getEvaluation();
//		System.out.println(de3.correlationCoefficient());
//		System.out.println("/////////////////////////////");
//		
//		// optimized
//		optimized.classify(1);
//		Evaluation e1 = optimized.getEvaluation();
//		System.out.println("/////////////////////////////");
//		optimized.classify(2);
//		Evaluation e2 = optimized.getEvaluation();
//		System.out.println("/////////////////////////////");
//		optimized.classify(3);
//		Evaluation e3 = optimized.getEvaluation();
//		System.out.println("/////////////////////////////");
		
		
	}
}

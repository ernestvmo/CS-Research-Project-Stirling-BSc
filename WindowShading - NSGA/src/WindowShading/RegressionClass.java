package WindowShading;

import java.util.Arrays;

import weka.classifiers.Evaluation;

public class RegressionClass
{
	public static void main(String[] args) throws Exception
	{
//		ClassificationThread t1 = new ClassificationThread("learningRate", 0, 100, 0.26); 
//		// best momentum: 0.59  --->  0.7934851522089069
//		ClassificationThread t2 = new ClassificationThread("momentum", 0, 100, 0.75);     
//		// best learning rate: 0.83  ---> 0.7989381953434337
//		
//		t1.start();
//		t2.start();
		double defaultLearningRate = 0.3;
		double defaultMomemtum = 0.2;
		int defaultLearningTime  = 500;
		String defaultHiddenLayer = "60";
		int folds = 5;
		
//		Classifier classifier = new Classifier(0.83, 0.59, 500, "a", 5);
//		classifier.classify(0);
//		double coef = classifier.getEvaluation().correlationCoefficient();
//		System.out.println("Coefficient: " + coef);
		
		
			double bestCoef = 0;
			String bestVal = null;
			Classifier test;
			
		for (double i = 0; i <= 100; i += 10)
		{
			test = new Classifier(i / 100, defaultMomemtum, defaultLearningTime, defaultHiddenLayer, folds);
			test.classify(0);
			
			double coef = test.getEvaluation().correlationCoefficient();
			
			if (coef > bestCoef)
			{
				bestCoef = coef;
				bestVal = String.valueOf(i / 100);
			}
			System.out.println(coef);
		}
		
		System.out.println("best: " + bestCoef + " with learning rate: " + bestVal);
		
		
//		double defaultLearningRate = 0.3;
//		double defaultMomemtum = 0.2;
//		int defaultLearningTime  = 500;
//		String defaultHiddenLayer = "a";
//		
//		int folds = 5;
//		
//		Classifier defaultClassified = new Classifier(0.35, 0.281, 250, "a", 5);
//		System.out.println("Default");
//		defaultClassified.classify(1);
//		
//		int x;
//		
//		// Current best = 0.6392
//		
//		Classifier defaultClassifier = new Classifier();
//		Classifier optimized = new Classifier(0.35, 0.281, 250, "o", folds);
//		
//		Classifier[] classifiers = new Classifier[10];
//		
//		double[] bestD = new double[4];
//		String[] bestS = new String[4];
//		
//		System.out.println("started");
//		
//		
//		for (int t = 0; t < 4; t++)
//		{
//			bestD[t] = 0;
//			bestS[t] = "";
//			
//			for (double i = 0; i <= 10; i++)
//			{
//				Classifier test = new Classifier(defaultLearningRate, i / 10, defaultLearningTime, defaultHiddenLayer, folds);
//				test.classify(t);
//
//				double coef = test.getEvaluation().correlationCoefficient();
//
//				if (coef > bestD[t])
//				{
//					bestD[t] = coef;
//					bestS[t] = String.valueOf(i / 10);
//				}
//				System.out.println(coef);
//			}
//		
//		}
//		
//		for (int t = 0; t < 4; t++)
//		{
//			System.out.println("population all ---> Coef: "+ bestD[t] + ", momentum: " + bestS[t]);
//		}
//		
//		double bestD;
//		String bestS;
//		
//		System.out.println("started");
//		bestD = 0;
//		bestS = "";
//		
//		for (double i = 1; i <= 99; i++)
//		{
//			Classifier test = new Classifier(0.19, i / 100, defaultLearningTime, defaultHiddenLayer, folds);
//			test.classify(0);
//			
//			double coef = test.getEvaluation().correlationCoefficient();
//			
//			if (coef > bestD)
//			{
//				bestD = coef;
//				bestS = String.valueOf(i / 100);
//			}
//			System.out.println(coef);
//		}
//		
//		System.out.println("population all ---> Coef: "+ bestD + ", learning rate: " + bestS);
//
//		double bestD;
//		String bestS;
//
//		System.out.println("started");
//		bestD = 0;
//		bestS = "";
//				
//		
//		for (double i = 1; i <= 99; i++)
//		{
//			for (double j = 1; j <= 99; j++)
//			{
//				Classifier test = new Classifier(i / 100, j / 100, defaultLearningTime, defaultHiddenLayer, folds);
//				test.classify(0);
//				
//				double coef = test.getEvaluation().correlationCoefficient();
//				
//				if (coef > bestD)
//				{
//					bestD = coef;
//					bestS = String.valueOf(i / 100);
//				}
//				System.out.println(i / 100 + " " + j / 100 + " " + coef);
//			}
//		}
//		
//		System.out.println("population all ---> Coef: "+ bestD + ", learning rate: " + bestS);
//		
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

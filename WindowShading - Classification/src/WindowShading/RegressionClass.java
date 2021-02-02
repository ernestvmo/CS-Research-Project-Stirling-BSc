package WindowShading;

import java.util.Arrays;

import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

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
		
//		ClassificationThread lowRate = new ClassificationThread(0, 50);
		ClassificationThread lowRate = new ClassificationThread(0.01); // 0.0
		ClassificationThread lowRate2 = new ClassificationThread(0.25); // 0.0
//		ClassificationThread highRate = new ClassificationThread(51, 100);
		ClassificationThread highRate = new ClassificationThread(0.75); // 0.92
		
		lowRate.start(); // best 0.01 0.0
		lowRate2.start(); // best 0.25 0.92
		highRate.start(); // best 0.75 0.91
		
//		double defaultLearningRate = 0.3;
//		double defaultMomemtum = 0.2;
//		int defaultLearningTime  = 500;
//		String defaultHiddenLayer = "60";
//		int folds = 5;
		
//		Classifier classifier = new Classifier(0.83, 0.59, 500, "a", 5);
//		classifier.classify(0);
//		double coef = classifier.getEvaluation().correlationCoefficient();
//		System.out.println("Coefficient: " + coef);
		
		
//			double bestCoef = 0;
//			String bestVal = null;
//			Classifier test;
//			
//		for (double i = 0; i <= 100; i += 10)
//		{
//			test = new Classifier(i / 100, defaultMomemtum, defaultLearningTime, defaultHiddenLayer, folds);
//			test.classify(0);
//			
//			double coef = test.getEvaluation().correlationCoefficient();
//			
//			if (coef > bestCoef)
//			{
//				bestCoef = coef;
//				bestVal = String.valueOf(i / 100);
//			}
//			System.out.println(coef);
//		}
		
//		System.out.println("best: " + bestCoef + " with learning rate: " + bestVal);
		
		
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
	}
	
	public static Instances getDataSet()
	{
		
	}
}

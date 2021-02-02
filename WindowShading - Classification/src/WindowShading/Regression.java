package WindowShading;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
//import weka.classifiers.MultipleClassifiersCombiner;
//import weka.classifiers.SingleClassifierEnhancer;
//import weka.classifiers.bayes.BayesNet;
//import weka.classifiers.bayes.NaiveBayes;
//import weka.classifiers.bayes.NaiveBayesMultinomial;
//import weka.classifiers.bayes.NaiveBayesMultinomialText;
//import weka.classifiers.functions.LinearRegression;
//import weka.classifiers.functions.Logistic;
//import weka.classifiers.functions.MultilayerPerceptron;
//import weka.classifiers.functions.SMO;
//import weka.classifiers.functions.SMOreg;
//import weka.classifiers.functions.SimpleLinearRegression;
//import weka.classifiers.functions.SimpleLogistic;
//import weka.classifiers.functions.VotedPerceptron;
//import weka.classifiers.lazy.IBk;
//import weka.classifiers.lazy.KStar;
//import weka.classifiers.meta.AdaBoostM1;
//import weka.classifiers.misc.SerializedClassifier;
//import weka.classifiers.pmml.consumer.PMMLClassifier;
//import weka.classifiers.rules.DecisionTable;
//import weka.classifiers.rules.JRip;
//import weka.classifiers.rules.OneR;
//import weka.classifiers.rules.PART;
//import weka.classifiers.rules.ZeroR;
//import weka.classifiers.trees.DecisionStump;
//import weka.classifiers.trees.HoeffdingTree;
//import weka.classifiers.trees.J48;
//import weka.classifiers.trees.LMT;
import weka.classifiers.trees.M5P;
//import weka.classifiers.trees.REPTree;
//import weka.classifiers.trees.RandomTree;
//import weka.classifiers.trees.lmt.LogisticBase;
//import weka.classifiers.trees.m5.M5Base;
//import weka.classifiers.trees.m5.PreConstructedLinearModel;
//import weka.classifiers.trees.m5.RuleNode;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
//import weka.core.Option;
//import weka.core.Utils;

public class Regression
{
	private final int WINDOWS_COUNT = 120;
	private SolutionsLoader sl;
	
	// MLP options
	private double learningRate;
	private double momentum;
	private int trainingTime;
	private String hiddenLayers;
	
	private String[] mlpOptions;
	
	// Cross validation param
	private int numFolds;
	
	private Evaluation e;
	
	public Regression()
	{
		this.learningRate = 0.3;
		this.momentum = 0.2;
		this.trainingTime = 500;
		this.hiddenLayers = "a";
		this.numFolds = 5;
	}
	
	public Regression(double learningRate, double momentum, int trainingTime, String hiddenLayer, int folds)
	{
		this.learningRate = learningRate;
		this.momentum = momentum;
		this.trainingTime = trainingTime;
		this.hiddenLayers = hiddenLayer;
		this.numFolds = folds;
	}
	
	public Regression(String [] options, int folds)
	{
		this.mlpOptions = options;
		this.numFolds = folds;
	}
	
	public void classify(int setNum)
	{
		sl = new SolutionsLoader();
		
		double[][] set = loadSet(setNum);
		
		mlpClassification(set);
	}
	
	private double[][] loadSet(int setNum)
	{
		switch (setNum)
		{
			case 1 :
				return sl.getSet1();
			case 2 :
				return sl.getSet2();
			case 3 :
				return sl.getSet3();
			case 0 :
				return sl.getAll();
			default :
				return null;
		}
	}
	
	private void mlpClassification(double [][] set)
	{
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 1; i < WINDOWS_COUNT + 1; i++)
		{
			attributes.add(new Attribute("W" + i)); // Column name
		}
		
		Attribute classAttribute = new Attribute("Energy");
		attributes.add(classAttribute);
		
		
		///////////////////////////////////////////////////
		// Training Set
		Instances trainingSet = new Instances("Training Set", attributes, set.length);
		
		for (int i = 0; i < set.length; i++)
		{
			trainingSet.add(new DenseInstance(1.0, set[i]));
		}
		trainingSet.setClass(classAttribute);
		
		
		// Test Set
		Instances testSet = new Instances("Test Set", attributes, set.length);
		
		for (int i = 0; i < set.length; i++)
		{
			testSet.add(new DenseInstance(1.0, set[i]));
		}
		testSet.setClass(classAttribute);
		
		
		mlpSection(trainingSet, testSet, set.length);
	}
	
	private void mlpSection(Instances trainingSet, Instances testSet, int population_size)
	{
		// Different Classifiers tried on their default settings
		// DecisionStump - 0.40
		// DecisionTable - 0.5697
		// HoeffdingTree - cant handle numeric
		// IBk			 - 0.6536
		// J48			 - cant handle numeric
		// JRip			 - cant handle numeric
		// KStar		 - 0.6649
		// LinearRegression				 - error thrown
		// LMT			 - cant handle numeric
		// LogisticBase	 - 0
		// M5P			 - 0.6941
		// NaiveBayes	 - cant handle numeric
		// NaiveBayesMultinomial		 - cant handle numeric
		// NaiveBayesMultinomialText	 - cant handle numeric
		// OneR			 - cant handle numeric
		// PART			 - cant handle numeric
		// RandomTree	 - 0.6712
		// REPTree		 - 0.5972
		// SerializedClassifier 		- access denied
		// SimpleLinearRegression 		- 0.4016
		// SimpleLogistic 				- cant handle numeric
		// SMO			 - cant handle numeric
		// SMOreg		 - 0.2991
		// VotedPerceptron				- cant handle numeric
		// ZeroR		 - -0.235
		
		
		double [] predictions = new double[population_size];
	
//		try 
//		{
//			M5P classifier = new M5P();
//
////			for (Enumeration<Option> o = classifier.listOptions(); o.hasMoreElements();)
////				System.out.println(o.nextElement().description());
//			
//			classifier.buildClassifier(trainingSet);
//			Evaluation e = new Evaluation(trainingSet);
//			e.crossValidateModel(classifier, trainingSet, numFolds, new Random(1));
//			System.out.println(e.toSummaryString());
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
		
		try
		{
			MultilayerPerceptron mlp = new MultilayerPerceptron();
			
			// set options
			mlp.setLearningRate(learningRate);
			mlp.setMomentum(momentum);
			mlp.setTrainingTime(trainingTime);
			mlp.setHiddenLayers(hiddenLayers);
			
			// or can be done this way
//			mlp.setOptions(mlpOptions);
			
			
			
			// Build MLP classifier
			mlp.buildClassifier(trainingSet);
			
			
			// Evaluate model
			e = new Evaluation(trainingSet);
			e.crossValidateModel(mlp, trainingSet, numFolds, new Random(1));
//			System.out.println(e.toSummaryString());
			
			
			// Predict
			double mse = 0;
			
			for (int i = 0; i < predictions.length; i++)
			{
				double prediction = mlp.classifyInstance(testSet.instance(i));
				predictions[i] = testSet.instance(i).classValue();
				mse += Math.pow(prediction - testSet.instance(i).classValue(), 2);
			}
			
//			System.out.println("Mean Square Error: " + Math.sqrt(mse / population_size));
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public Evaluation getEvaluation()
	{
		return e;
	}


	

}

























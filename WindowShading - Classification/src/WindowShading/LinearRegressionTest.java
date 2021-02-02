package WindowShading;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class LinearRegressionTest
{
	SolutionsLoader sl;
	
	public static void main(String[] args)
	{
		try
		{
			new LinearRegressionTest().process();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public LinearRegressionTest()
	{
		// TODO Auto-generated constructor stub
		sl = new SolutionsLoader();
	}
	
	
	public Instances getTrainingDataSet(double[][] set)
	{
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 1; i <= 120; i++)
		{//columns
			attributes.add(new Attribute("W"+i));
		}
		
		Attribute classAttribute = new Attribute("Energy");
		attributes.add(classAttribute);
		
		
		Instances trainingSet = new Instances("Trainint", attributes, 0);
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		
		for (int i = 0; i < set.length; i++)
		{
			trainingSet.add(new DenseInstance(1.0, set[i]));
		}
		
		return trainingSet;
	}
	
	public Instances getTestDataSet(double[][] set)
	{
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 1; i <= 120; i++)
		{//columns
			attributes.add(new Attribute("W"+i));
		}
		
		Attribute classIndex = new Attribute("Energy");
		attributes.add(classIndex);
		
		Instances testSet = new Instances("Test",  attributes, 0);
		testSet.setClassIndex(testSet.numAttributes() - 1);
		
		for (int i = 0; i < set.length; i++)
		{
			testSet.add(new DenseInstance(1.0, set[i]));
		}
		
		return testSet;
	}
	
	public void process() throws Exception
	{
		Instances trainingSet = getTrainingDataSet(sl.getSet1());
		Instances testingSet = getTestDataSet(sl.getSet3());
		
		
		Classifier classifier = new LinearRegression();
		
		classifier.buildClassifier(trainingSet);
		
		Evaluation eval = new Evaluation(trainingSet);
		eval.evaluateModel(classifier, testingSet);
		
		
		System.out.println("Linear Regression with dataset");
		System.out.println(eval.toSummaryString());
		System.out.println(" the expression for the input data");
		System.out.println(classifier);
		
	}
}

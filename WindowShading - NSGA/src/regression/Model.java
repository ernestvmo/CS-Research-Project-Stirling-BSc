package regression;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class Model
{
	private Evaluation e;
	private double[][] set;
	
	
	public Model(double[][] set)
	{
		this.set = set;
	}
	
	public void build()
	{
		ArrayList<Attribute> attributes = createModelAttributes();
		
		Instances trainingSet = createSet(attributes, set);
		
		// TODO delete - temporary
		double[][] temp = getTen(set);
		Instances testSet = createSet(attributes, temp);
		
		classify(trainingSet, testSet);
	}
	
	private ArrayList<Attribute> createModelAttributes()
	{
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 1; i <= 120; i++)
		{
			attributes.add(new Attribute("W" + i));
		}
		
		Attribute classAttribute = new Attribute("Energy");
		attributes.add(classAttribute);
		
		return attributes;
	}
	
	private Instances createSet(ArrayList<Attribute> attributes, double[][] solutionSet)
	{
		Instances instances = new Instances("Training Set", attributes, solutionSet.length);
		
		for (int i = 0; i < solutionSet.length; i++)
		{
			instances.add(new DenseInstance(1.0, solutionSet[i]));
		}
		instances.setClass(attributes.get(attributes.size() - 1));
		
		return instances;
	}
	
	private double[][] getTen(double[][] set)
	{
		Random r = new Random();
		
		double[][] newSet = new double[100][set[0].length];
		
		for (int i = 0; i < newSet.length; i++)
		{
			newSet[i] = set[r.nextInt(set.length)];
		}
		
		return newSet;
	}
	
	private void classify(Instances trainingSet, Instances testSet)
	{
		try
		{
			MultilayerPerceptron mlp = new MultilayerPerceptron();
			mlp.buildClassifier(trainingSet);
			
			e = new Evaluation(trainingSet);
			e.evaluateModel(mlp, testSet);
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

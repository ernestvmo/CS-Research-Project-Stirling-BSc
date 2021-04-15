package regression;

import java.util.ArrayList;
import java.util.Random;

import main.Loader;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.misc.SerializedClassifier;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.lmt.LogisticBase;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.pmml.jaxbbindings.NaiveBayesModel;

public class Classifier
{
	/** The data set used to train the model. */
	private double[][] set;
	/** The Artificial Neural Network (model) object. */
	private MultilayerPerceptron mlp;
	/** The instances build from the data set. */
	private Instances trainingSet;
	/** Evaluation object of the trained model. */
	private Evaluation evaluation;
	
	public static void main(String[] args)
	{
		Classifier c = new Classifier(Loader.load());
		c.go();
		try
		{
			System.out.println(c.getEvaluation().correlationCoefficient());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor for the Model object.
	 * 
	 * @param set The dataset of pre-evaluated solutions.
	 */
	public Classifier(double[][] set)
	{
		this.set = set;
	}
	
	/**
	 * Trains the model.
	 */
	public void go()
	{
		ArrayList<Attribute> attributes = createModelAttributes();
		
		trainingSet = createSet(attributes, set);
		
		// TODO delete - temporary
		double[][] temp = getTen(set);
		Instances testSet = createSet(attributes, temp);
		build(trainingSet, testSet);
	}
	
	/**
	 * Create the model's attribute list.
	 * 
	 * @return The attributes' list.
	 */
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
	
	/**
	 * Create an Instances object from the attributes' list and the data set.
	 * 
	 * @param attributes The model's attributes' list.
	 * @param solutionSet The data set of pre-evaluated solutions. 
	 * @return An Instances object of the data.
	 */
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
	
	/**
	 * Randomly gets 10 solutions from the data set.
	 * 
	 * @param set The data set to pick solutions from.
	 * @return A 2D array of solutions.
	 */
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
	
	public void retrain(double[][] alleles)
	{
		addToInstances(alleles);
		
		build(trainingSet, null);
		
		try {
			Evaluation evaluation = new Evaluation(trainingSet);
			evaluation.evaluateModel(mlp, createSet(createModelAttributes(), getTen(alleles)));
			evaluation.toSummaryString();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addToInstances(double[][] set)
	{
		for (int i = 0; i < set.length; i++)
		{
			trainingSet.add(new DenseInstance(1.0, set[i]));
		}
	}
	
	/**
	 * Build the model using the training set.
	 * 
	 * @param trainingSet Instances object containing the data set.
	 * @param testSet Solutions to classify to evaluate the model.
	 */
	private void build(Instances trainingSet, Instances testSet)
	{
		try
		{
			mlp = new MultilayerPerceptron();
			mlp.buildClassifier(trainingSet);
			
			if (testSet != null)
			{
				evaluation = new Evaluation(trainingSet);
				evaluation.evaluateModel(mlp, testSet);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Predict the energy consumption of the passed in boolean array.
	 * 
	 * @param alleles An array of booleans representing the windows of a layout.
	 * @return The predicted energy consumption.
	 */
	public double predict(boolean[] alleles) 
	{
		double[] temp = new double[alleles.length + 1];
		for (int i = 0; i < alleles.length; i++) {
			temp[i] = alleles[i] ? 1 : 0;
		}
		
		Instances instances = new Instances("Training Set", createModelAttributes(), 1);
		instances.add(new DenseInstance(1.0, temp));
		instances.setClassIndex(trainingSet.numAttributes() - 1);
		
		double prediction = 0;
		
		while (prediction < 10000 /*min val*/ || Double.isNaN(prediction)) {
			try {
				prediction = mlp.classifyInstance(instances.instance(0));
			} 
			catch (NullPointerException ne)
			{
				// System.out.println(ne.getCause());// TODO ignore this, no idea
													// why it gets thrown
			}
			catch (Exception e)
			{
				System.out.println(instances.instance(0));
				e.printStackTrace();
			}
		}
		return prediction;
	}
	
	/**
	 * Accessor method for the Evaolution object.
	 * @return The Evalution object.
	 */
	public Evaluation getEvaluation()
	{
		return evaluation;
	}

	/**
	 * Mutator method for the data set used to build the model.
	 * @param set
	 */
	public void setSet(double[][] set)
	{
		this.set = set;
	}
}
package WindowShading;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVLoader;
import weka.classifiers.functions.*;

public class WekaConnect
{
//	FileReader trainreader;
//	Instances train;
//	String trainFilePath = "C:\\Users\\558386\\Desktop\\TestData.csv";
//	
//	
//	MultilayerPerceptron mlp;
//	double learningRate = 0.3;
//	double momentum = 0.2;
//	int trainingTime = 500;
//	String hiddenLayers = "a";
//	
//	String dataToPredictFilePath = "C:\\Users\\558386\\Desktop\\Data.csv";
//	
//	String outputFilePath = "C:\\Users\\558386\\Desktop\\Output.csv";
	
	public static void main(String[] args) {
		simpleWekaTrain();
	}
	
	public static void simpleWekaTrain()
	{
		FileReader trainreader;
		Instances train;
		String trainFilePath = "C:\\Users\\558386\\Desktop\\TestData.csv";
		
		
		MultilayerPerceptron mlp;
		double learningRate = 0.3;
		double momentum = 0.2;
		int trainingTime = 500;
		String hiddenLayers = "a";
		
		String dataToPredictFilePath = "C:\\Users\\558386\\Desktop\\Data.csv";
		
		String outputFilePath = "C:\\Users\\558386\\Desktop\\Output.csv";
		
		try {
//			trainreader = new FileReader(trainFilePath);
			CSVLoader loader = new CSVLoader();
			loader.setFile(new File(trainFilePath));
			
//			train = new Instances(trainreader);
			train = loader.getDataSet();
			train.setClassIndex(train.numAttributes()-2);
//			train.setAttributeWeight(train.numAttributes()-1, 0);
			
			mlp = new MultilayerPerceptron();
			
			// Do following accordingly
			mlp.setLearningRate(learningRate);
			mlp.setMomentum(momentum);
			mlp.setTrainingTime(trainingTime);
			mlp.setHiddenLayers(hiddenLayers);
			mlp.buildClassifier(train);
			
			// or use following
//			mlp.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 2000 -V 0 -S 0 -E 20 -H 3"));

			
			Evaluation eval = new Evaluation(train);
//			eval.evaluateModel(mlp, train);
			eval.crossValidateModel(mlp,  train,  10, new Random(1)); // cross validation
			System.out.println(eval.errorRate());
			System.out.println(eval.toSummaryString());
			
			
			// Evaluating, Predicting unlabeled data
//			Instances dataPrediction = new Instances(new BufferedReader(new FileReader(dataToPredictFilePath)));
			CSVLoader loader2 =  new CSVLoader();
			loader2.setFile(new File(dataToPredictFilePath));
			Instances dataPrediction = loader.getDataSet();
//			dataPrediction.setClassIndex(dataPrediction.numAttributes()-1);
			Instances predictdata = new Instances(dataPrediction);
			
			// Prediction part
			for (int i = 0; i < dataPrediction.numInstances(); i++)
			{
				double clsLabel = mlp.classifyInstance(dataPrediction.instance(i));
				System.out.println(clsLabel);
				predictdata.instance(i).setClassValue(clsLabel);
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
			writer.write(predictdata.toString());
			writer.newLine();
			writer.flush();
			writer.close();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		
	}
	
}


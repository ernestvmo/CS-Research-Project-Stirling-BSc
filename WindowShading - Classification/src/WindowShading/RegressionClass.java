package WindowShading;

public class RegressionClass
{
	public static void main(String[] args)
	{
		int folds = 5;
		
//		Classifier defaultClassified = new Classifier(0.35, 0.281, 250, "a", 5);
//		System.out.println("Default");
//		defaultClassified.classify(1);
		
		// Current best = 0.6392
		Classifier defaultClassifier = new Classifier();
		Classifier optimized = new Classifier(0.35, 0.281, 250, "o", folds);
		
		
		
		// default
		defaultClassifier.classify(1);
		System.out.println("/////////////////////////////");
		defaultClassifier.classify(2);
		System.out.println("/////////////////////////////");
		defaultClassifier.classify(3);
		System.out.println("/////////////////////////////");
		
		// optimized
		optimized.classify(1);
		System.out.println("/////////////////////////////");
		optimized.classify(2);
		System.out.println("/////////////////////////////");
		optimized.classify(3);
		System.out.println("/////////////////////////////");
		
		
	}
}

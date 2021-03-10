package main;
import Optimisation.NSGA2_E;
import regression.Model;

/**
 * 
 * @author Ernest Vanmosuinck
 *
 */
public class SystemManager
{
	/** The surrogate model we use to classify solutions. */
	private Model model;
	/** Genetic Algorithm used to optimize the solutions. */
	private NSGA2_E nsga;
	
	public static void main(String[] args)
	{
		SystemManager sm = new SystemManager();
//		sm.trainModel();
		sm.go();
	}
	
	/** 
	 * Constructor object for the SystemManager. 
	 */
	public SystemManager(double d)
	{
		model = new Model(Loader.load());
		nsga = new NSGA2_E();
	}
	
	public SystemManager()
	{
		nsga = new NSGA2_E();
		nsga.prebuildModel();
		
	}
	
	/** 
	 * This method will build the surrogate model and assign it to the GA. 
	 */
//	public void trainModel()
//	{
//		model.go();
//		nsga.setModel(model);
//	}
	
	/**
	 * This method starts the GA.
	 */
	public void go()
	{
		nsga.go();
		System.out.println("done");
	}
}

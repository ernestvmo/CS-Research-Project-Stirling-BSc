package main;

import Optimisation.NSGA2_E;

/**
 * 
 * @author Ernest Vanmosuinck
 *
 */
public class SystemManager
{
	/** Genetic Algorithm used to optimize the solutions. */
	private NSGA2_E nsga;
	
	public static void main(String[] args)
	{
		for (int i = 0; i < 1; i++)
		{
			SystemManager sm = new SystemManager();
	//		sm.trainModel();
			sm.go();
		}
	}
	
	/** 
	 * Constructor object for the SystemManager. 
	 */
	public SystemManager()
	{
		nsga = new NSGA2_E();
		nsga.prebuildModel();
		
	}
	
	/**
	 * This method starts the GA.
	 */
	public void go()
	{
		nsga.go();
		System.out.println("done");
	}
}

package main;
import Optimisation.NSGA2_E;
import regression.Model;

public class SystemManager
{
	private Model model;
	private NSGA2_E nsga;
	
	public static void main(String[] args)
	{
		SystemManager sm = new SystemManager();
		sm.trainModel();
		sm.go();
	}
	
	public SystemManager()
	{
		model = new Model(Loader.load());
		nsga = new NSGA2_E();
	}
	
	public void trainModel()
	{
		model.build();
		nsga.setModel(model);
	}
	
	public void go()
	{
		nsga.go();
		System.out.println("done");
	}
}

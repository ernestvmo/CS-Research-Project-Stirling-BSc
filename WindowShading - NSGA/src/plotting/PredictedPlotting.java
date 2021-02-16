package plotting;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class PredictedPlotting extends JFrame
{
	// data for boxplotting
	private double[] energyPlusValues;
	private double[] surrrogateValues;
	
	private JFrame frame;
	private JPanel panel;
	
	public static void main(String[] args) 
	{
		PredictedPlotting boxplot = new PredictedPlotting();
		boxplot.plot(boxplot.getEnergyPlusValues(), boxplot.surrrogateValues);
	}
	
	public double[] getEnergyPlusValues()
	{
		return energyPlusValues;
	}

	public double[] getSurrrogateValues()
	{
		return surrrogateValues;
	}

	public PredictedPlotting(double[] energyPlus, double[] surrogate)
	{
		this.energyPlusValues = energyPlus;
		this.surrrogateValues = surrogate;
	}
	
	public PredictedPlotting()
	{
		this.energyPlusValues = randomVs(20);
		this.surrrogateValues = randomVs(20);
		init(energyPlusValues, surrrogateValues);
	}
	
	private double[] randomVs(int size)
	{
		double[] rs = new double[size];
		Random random = new Random();
		
		for (int i = 0; i < size; i++)
			rs[i] = random.nextInt(100) + 1;
		
		return rs;
	}
	
	public void init(double[] v1, double[] v2)
	{
//		frame = new JFrame("Box Plot");
		this.setSize(500, 500);
		this.setTitle("Box Plot");
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void plot(double[] A, double[] P)
	{
		System.out.println("HERE");
		
	}
	
	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.white);
		Rectangle bounds = this.getBounds();
		// draw axes
		int axisOffset = 60;
		g2.setColor(Color.gray);
		int xAxisYPos = bounds.y + (bounds.height - axisOffset);
		int yAxisXPos = bounds.x + axisOffset;
		g2.drawLine(bounds.x, xAxisYPos, bounds.x + bounds.width, xAxisYPos); // x-axis
		g2.drawLine(yAxisXPos, bounds.y, yAxisXPos, bounds.height); // y-axis
		
	}
}

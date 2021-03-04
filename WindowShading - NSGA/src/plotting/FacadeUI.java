package plotting;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Random;

import javax.swing.border.LineBorder;

import Optimisation.Individual;

import java.awt.Color;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;

public class FacadeUI
{

	private JFrame frame;

	/**
	 * Create the application.
	 */
	public FacadeUI(Individual i)
	{
		initialize(i.getAlleles(), i.getFitness1(), i.getFitness2(), i.hashCode());
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * TODO : * add the cost and energy values below the graph
	 *        * better title
	 */
	private void initialize(boolean[] windows, double energy, double cost, int title)
	{
		frame = new JFrame();
		frame.setTitle("Individual " + title);
		frame.setBounds(100, 100, 300, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panelWindows = new JPanel();
		frame.getContentPane().add(panelWindows, BorderLayout.CENTER);
		GridBagLayout gbl_panelWindows = new GridBagLayout();
		gbl_panelWindows.columnWidths = new int[]{0, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 0};
		gbl_panelWindows.rowHeights = new int[]{0, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 0};
		gbl_panelWindows.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		gbl_panelWindows.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		panelWindows.setLayout(gbl_panelWindows);
		
		JPanel panelLabels = new JPanel();
		frame.getContentPane().add(panelLabels, BorderLayout.SOUTH);
		panelLabels.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panelLabels.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 10, 0, 0, 30, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblEnergy = new JLabel("Energy :");
		GridBagConstraints gbc_lblEnergy = new GridBagConstraints();
		gbc_lblEnergy.anchor = GridBagConstraints.EAST;
		gbc_lblEnergy.insets = new Insets(0, 0, 5, 5);
		gbc_lblEnergy.gridx = 1;
		gbc_lblEnergy.gridy = 2;
		panel.add(lblEnergy, gbc_lblEnergy);
		
		JLabel lblEnergyValue = new JLabel();
		GridBagConstraints gbc_lblEnergyValue = new GridBagConstraints();
		gbc_lblEnergyValue.anchor = GridBagConstraints.WEST;
		gbc_lblEnergyValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblEnergyValue.gridx = 2;
		gbc_lblEnergyValue.gridy = 2;		
		lblEnergyValue.setText(String.valueOf(energy));
		panel.add(lblEnergyValue, gbc_lblEnergyValue);
		
		JLabel lblCost = new JLabel("Cost :");
		GridBagConstraints gbc_lblCost = new GridBagConstraints();
		gbc_lblCost.anchor = GridBagConstraints.EAST;
		gbc_lblCost.insets = new Insets(0, 0, 5, 5);
		gbc_lblCost.gridx = 1;
		gbc_lblCost.gridy = 3;
		panel.add(lblCost, gbc_lblCost);
		
		JLabel lblCostValue = new JLabel();
		GridBagConstraints gbc_lblCostValue = new GridBagConstraints();
		gbc_lblCostValue.anchor = GridBagConstraints.WEST;
		gbc_lblCostValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblCostValue.gridx = 2;
		gbc_lblCostValue.gridy = 3;
		lblCostValue.setText(String.valueOf(cost));
		panel.add(lblCostValue, gbc_lblCostValue);
		
		
		
		JPanel [] lbls = new JPanel[windows.length];
		GridBagConstraints[] gridbag_constraints = new GridBagConstraints[windows.length];
		
		int floor = 0;
		int nextFloor = 10;
		int row = 0;
		
		for (int i = 0; i < windows.length; i++)
		{
			if (i == nextFloor)
			{
				floor++;
				nextFloor += 10;
				row = 0;
			}
			
			lbls[i] = new JPanel();
			lbls[i].setBackground(windows[i] ? Color.GRAY : Color.WHITE);
			
//			if (i == 10)
				lbls[i].setBorder(new LineBorder(Color.black));
			
			gridbag_constraints[i] = new GridBagConstraints();
			gridbag_constraints[i].insets = new Insets(0,0,0,0);
			gridbag_constraints[i].fill = GridBagConstraints.BOTH;
			gridbag_constraints[i].gridx = row + 1;
			gridbag_constraints[i].gridy = floor + 1;
			
			panelWindows.add(lbls[i], gridbag_constraints[i]);
			row++;
		}
		
		
		frame.setVisible(true);
	}

}

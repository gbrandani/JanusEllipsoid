import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A control panel that provides controls for the user to set stuff
 * 
 * These controls call methods in the controller object of type <code>InteractiveJanusEllipsoid</code> which
 * is expected to perform the appropriate action.
 * 
 * @author Giovanni Brandani
 *
 */
public class JanusEllipsoidControlPanel extends JPanel {
	
	// A reference to the controller object that we are attached to
	private InteractiveJanusEllipsoid controller;
	
	// Number formatters to display currently set values of parameters in a sensible way
	private DecimalFormat roundToTwoDP     = new DecimalFormat("0.00");
	private DecimalFormat scientificFormat = new DecimalFormat("0.##E0");
	
	// User interface objects to show in the panel
	private JLabel      alphaLabel     = new JLabel("alpha = 0");
	private JSlider     alphaSlider    = new JSlider(0, 180, 1);  // Select angle alpha of the patch
	private JLabel      zaxisLabel     = new JLabel("Lz");
	private NumberField zaxisField     = new NumberField(1.0, roundToTwoDP);
	private JLabel      xaxisLabel     = new JLabel("Lx,Ly");
	private NumberField xaxisField     = new NumberField(1.0, roundToTwoDP);
	private JLabel      sigmadzLabel   = new JLabel("dz");
	private NumberField sigmadzField   = new NumberField(1.0, scientificFormat);
	private JLabel      sigmadphiLabel = new JLabel("dphi");
	private NumberField sigmadphiField = new NumberField(1.0, scientificFormat);
	private JLabel      gammaLabel     = new JLabel("gamma");
	private NumberField gammaField     = new NumberField(1.0, roundToTwoDP);
	private JLabel      costhetaALabel = new JLabel("cos(thetaA)");
	private NumberField costhetaAField = new NumberField(1.0, roundToTwoDP);
	private JLabel      costhetaPLabel = new JLabel("cos(thetaP)");
	private NumberField costhetaPField = new NumberField(1.0, roundToTwoDP);
	private JButton   startStopButton  = new JButton("Start");

	//   CONSTRUCTOR
	
	/**
	 * Create a control panel to start/stop the simulation and alter alpha and MC step
	 * 
	 * 
	 * @param attachedTo controller object to interact with
	 */
	public JanusEllipsoidControlPanel(InteractiveJanusEllipsoid attachedTo) {
		controller = attachedTo;
				
		// Attach event handlers to the controls
		
		// The start/stop button starts the simulation if it is not running, and stops it if it is
		startStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(controller.isRunning()) controller.stopSimulation();
				else controller.startSimulation();
			}			
		});

		// The alpha slider sets the angle of the patch
		alphaLabel.setText( String.format("alpha = %3d", (int)(180.*controller.state.getAlpha()/Math.PI)) );
		alphaSlider.setValue( (int)(180.*controller.state.getAlpha()/Math.PI) );
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Convert the slider position to the appropriate decimal value
				double val = (double)alphaSlider.getValue()/(double)1;
				// set alpha
				controller.setEllipsoid(controller.state.getAxis(), Math.PI*val/180.);
				// update the label to reflect the change
				alphaLabel.setText( String.format("alpha = %3d", (int)val) );
			}
		});


		// field - controller interaction
		zaxisField.setValue(controller.state.getAxis().a[2]);
		zaxisField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				Vector3d axis = new Vector3d(controller.state.getAxis());
				axis.a[2] = Double.valueOf(zaxisField.getValue());
				controller.setEllipsoid( axis, controller.state.getAlpha());
			}
		});

		// field - controller interaction
		xaxisField.setValue(controller.state.getAxis().a[0]);
		xaxisField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				Vector3d axis = new Vector3d(controller.state.getAxis());
				axis.a[0] = Double.valueOf(xaxisField.getValue());
				axis.a[1] = axis.a[0];
				controller.setEllipsoid( axis, controller.state.getAlpha());
			}
		});

		// field - controller interaction
		sigmadzField.setValue(controller.getSigmadz());
		sigmadzField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				controller.setSigmadz(Double.valueOf(sigmadzField.getValue()));
			}
		});

		// field - controller interaction
		sigmadphiField.setValue(controller.getSigmadphi());
		sigmadphiField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				controller.setSigmadphi(Double.valueOf(sigmadphiField.getValue()));
			}
		});

		// field - controller interaction
		gammaField.setValue(controller.state.gamma);
		gammaField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				controller.setGamma(Double.valueOf(gammaField.getValue()));
			}
		});

		// field - controller interaction
		costhetaAField.setValue(controller.state.costhetaA);
		costhetaAField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				controller.setCosthetaA(Double.valueOf(costhetaAField.getValue()));
			}
		});

		// field - controller interaction
		costhetaPField.setValue(controller.state.costhetaP);
		costhetaPField.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Note, we need to cast the generic Number object to a Double here
				controller.setCosthetaP(Double.valueOf(costhetaPField.getValue()));
			}
		});

		add(alphaLabel);
		add(alphaSlider);
		add(zaxisLabel);
		add(zaxisField);
		add(xaxisLabel);
		add(xaxisField);
		add(sigmadzLabel);
		add(sigmadzField);
		add(sigmadphiLabel);
		add(sigmadphiField);
		add(gammaLabel);
		add(gammaField);
		add(costhetaALabel);
		add(costhetaAField);
		add(costhetaPLabel);
		add(costhetaPField);
		add(startStopButton);
	}

	//  MESSAGES
	
	/**
	 * This is received whenever the simulation displayed by this view has started running
	 */
	public void simulationStarted() {
		// Change the start/stop button to reflect the fact it now stops the simulation
		startStopButton.setText("Stop");
		// Dim the other controls
		alphaSlider.setEnabled(false);
		zaxisField.setEnabled(false);
		xaxisField.setEnabled(false);
		sigmadzField.setEnabled(false);
		sigmadphiField.setEnabled(false);
		gammaField.setEnabled(false);
		costhetaAField.setEnabled(false);
		costhetaPField.setEnabled(false);
	}

	/**
	 * This is received whenever the simulation displayed by this view has stopped running
	 */
	public void simulationFinished() {
		// Change the start/stop button to reflect the fact it now starts the simulation
		startStopButton.setText("Start");
		// Enable the other controls
		alphaSlider.setEnabled(true);
		zaxisField.setEnabled(true);
		xaxisField.setEnabled(true);
		sigmadzField.setEnabled(true);
		sigmadphiField.setEnabled(true);
		gammaField.setEnabled(true);
		costhetaAField.setEnabled(true);
		costhetaPField.setEnabled(true);
	}
	
}

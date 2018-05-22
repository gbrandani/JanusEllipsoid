import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 * The InteractiveJanusEllipsoid is a class that manages a fully interactive version of the JanusEllipsoid simulation.
 * It is a controller object in the 'Model-View-Controller' pattern. It acts as a broker between the InteractiveJanusEllipsoidViewer
 * and JanusEllipsoidControlPanel (the views) that the user interacts with, and the JanusEllipsoid class (the model, borrowed
 * from the noninteractive versions of the simulation) that handles the actual simulation of the physics  
 * 
 * @author Giovanni Brandani
 *
 */

public class InteractiveJanusEllipsoid extends JFrame {

	// Current simulation state
	private JanusEllipsoid je = null;
	
	// sigmas for MC dynamics
	private double sigmadz   = 0.02;
	private double sigmadphi = 0.02;

	// A local copy of the simulation state: this is needed to avoid concurrency problems
	public JanusEllipsoid state = new JanusEllipsoid(new Vector3d(1.4,1.4,2.6), Math.PI*0.28, JanusEllipsoid.DEFAULTNGRID, 10, -0.7, 0.5);
	
	// The user interface objects we interact with
	private InteractiveJanusEllipsoidViewer viewer = new InteractiveJanusEllipsoidViewer(this);
	private JanusEllipsoidControlPanel  controls   = new JanusEllipsoidControlPanel(this);
	
	// Background thread to run the simulation in
	private Thread background = null;

	
	//  CONSTRUCTOR
	
	/**
	 * This constructor creates and displays the window containing the view of the JanusEllipsoid
	 * and controls that allow it to be manipulated by the user
	 */
	public InteractiveJanusEllipsoid() {
		// Create the frame (window)
		super("Interactive Janus Ellipsoid");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// set default initial condition as in BslA
//		state = new JanusEllipsoid(new Vector3d(1.4,1.4,2.6), Math.PI*0.28, JanusEllipsoid.DEFAULTNGRID);
//		state.gamma     = 10;	// surface tension in martini
//		state.costhetaA =-0.6;
//		state.costhetaP = 0.6;
		
		// Lay out the viewer and controls
		getContentPane().add(viewer, BorderLayout.CENTER);
		getContentPane().add(controls, BorderLayout.SOUTH);
		pack();

		// Open the window
		setVisible(true);
				
		// Everything now happens at the behest of the user: other methods in this class will be called
		// as the user clicks buttons etc - we don't need to do anything else now
	}
	
	//  ACCESSORS
	
	/**
	 * Obtain the current state of the simulation. Note, this is done in a thread-safe way, so the
	 * state returned will be consistent
	 * 
	 * @return the masses, positions and velocities of the ellipsoid bobs as an array
	 * 
	 */
	public RigidBodyState getRigidBodyState() {
		// If the simulation is running, we will need to update our local copy of the state
		if(background != null) {
			// Obtain the lock on the JanusEllipsoid object to ensure the state is consistent
			synchronized(je) {
				state.center = new Vector3d(je.center);
				state.rot    = new Quaternion(je.rot);
			}
		}
		return new RigidBodyState(state.center, state.rot);
	}
	
	/**
	 * Setters
	 */
	public void setCenter(Vector3d r) {
		if(background != null) return;
		state.center = new Vector3d(r);
	}
	public void setRot(Quaternion q) {
		if(background != null) return;
		state.rot = new Quaternion(q);
	}
	public void setEllipsoid(Vector3d axis, double alpha) {
		if(background != null) return;
		state.resetEllipsoid(new Vector3d(axis), alpha, JanusEllipsoid.DEFAULTNGRID);
	}
	public void setGamma(double gamma) {
		if(background != null) return;
		state.gamma = gamma;
	}
	public void setCosthetaA(double costhetaA) {
		if(background != null) return;
		state.costhetaA = costhetaA;
	}
	public void setCosthetaP(double costhetaP) {
		if(background != null) return;
		state.costhetaP = costhetaP;
	}
	public void setSigmadz(double sigma) {
		this.sigmadz = sigma;
	}
	public void setSigmadphi(double sigma) {
		this.sigmadphi = sigma;
	}
	
	/**
	 * Getters
	 */
	public double getSigmadz() {
		return sigmadz;
	}
	public double getSigmadphi() {
		return sigmadphi;
	}
	
	/**
	 * Determine whether the simulation is running or not
	 * 
	 * @return true if simulation running, false otherwise
	 */
	public boolean isRunning() {
		return background != null;
	}
	
	//  ACTIONS
	
	/** 
	 * Start running a simulation in the background. Does nothing if the simulation is already running.
	 */
	public void startSimulation() {
		// Return immediately if there is a background thread running
		if(background != null) return;
		// Set up a new simulation with the desired initial condition (since this may be different from the state in which the last simulation ended)
		je = new JanusEllipsoid(state);
		// Run it in a new background thread
		background = new Thread() {
			@Override
			public void run() {

				// Communicate with the view objects on the Swing thread
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Inform the view objects that the simulation has started
						controls.simulationStarted();
						viewer.simulationStarted();
					}
				});

				// Repeatedly iterate the equations of motion until the thread is interrupted
				while(!isInterrupted()) {
					je.iterate((int)1e2, sigmadz, sigmadphi);
				}
				// Update the local copy of the state with that of the JanusEllipsoid object
				getRigidBodyState();
				
				// Communicate with the view objects on the Swing thread
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Remove the reference to the background thread (note, we do this
						// on the Swing thread to avoid concurrency problems)
						background = null;
						// Inform the view objects that the simulation has finished
						viewer.simulationFinished();
						controls.simulationFinished();
					}
				});
				
			}
		};
		background.start();
	}
	
	public void stopSimulation() {
		// Check the a background thread is actually running
		if(background == null) return;
		// Send the interrupt message to the background thread to stop it at a convenient point
		background.interrupt();
	}
	
	
	//  MAIN METHOD
	
	/**
	 * Creates and runs the interactive simulation
	 * 
	 * @param args command-line arguments, ignored in the interactive simulation since everything is set up via the user interface
	 */
	public static void main(String args[]) {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		// We need to switch all further execution to the Swing thread, as everything is now at the control of the user through the Swing interface
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new InteractiveJanusEllipsoid();
			}
		});
		
	}
	
}

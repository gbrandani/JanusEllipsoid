import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

/**
 * A noninteractive version of the ellipsoid viewer. This class provides a custom Swing component whose
 * <code>paintComponent</code> method displays the start of the <code>JanusEllipsoid</code> object
 * that is supplied to the constructor when this object is created.
 * 
 * This class also provides a <code>main</code> method that demonstrates the use of the custom
 * component embedded within a <code>JFrame</code>
 * 
 * @author Giovanni Brandani
 *
 */
public class JanusEllipsoidApplet extends JApplet {
//public class JanusEllipsoidApplet {

	/**
	 * Main method for setting up an initial condition, iterating the equations of motion, and displaying a the system
	 * 
	 * @param args	command line arguments, currently ignored
	 */
	//public static void main(String args[]) {
	public void init() {
		
		// Create the default double-ellipsoid condition
		// set axis and other parameters as in BslA
		JanusEllipsoid je = new JanusEllipsoid(new Vector3d(1.4,1.4,2.6), Math.PI*0.28, 100);
		je.gamma     = 12;	// surface tension in martini
		je.costhetaA =-0.8;
		je.costhetaP = 0.5;	// about the value for BslA

		// check ration between areas
		//System.out.printf("%f\t%f\t%f\n", je.Sao+je.Saw, je.Spo+je.Spw, (je.Sao+je.Saw)/(je.Spo+je.Spw));
		
		// Set up a Frame (window) to open on the screen
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Janus ellipsoid");
		frame.setPreferredSize(new Dimension(600,600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Create a ellipsoid viewer, and place this within the frame
		JanusEllipsoidViewer view = new JanusEllipsoidViewer(je);
		frame.getContentPane().add(view);
		
		// Lay out the frame's contents, and put it on screen
		frame.pack();
		frame.setVisible(true);
		
		// Do timesteps of 1e-7 second, and update the viewer every 1/100s; keep running forever
		
		for(;;) {
			je.iterate((int)1e2, 1e-2, 1e-2);
			// Request repaint, and wait for it to actually have been done before proceeding
			synchronized(je) {

				// Uncomment the following line to help identify concurrency problems
//				System.out.println("Request redraw at t="+je.getTime());

				view.repaint();
				try {
					je.wait();
				}
				catch (Exception ignore) { }
			}
		}
				
	}
	
}

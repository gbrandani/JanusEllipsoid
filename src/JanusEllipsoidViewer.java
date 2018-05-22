import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

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
public class JanusEllipsoidViewer extends JComponent {

	/**
	 * constants
	 */
	private static final Vector3d XAXIS = new Vector3d(1.,0.,0.);	// x axis
	private static final Vector3d YAXIS = new Vector3d(0.,1.,0.);	// y axis
	private static final Vector3d ZAXIS = new Vector3d(0.,0.,1.);	// z axis
	
	/**
	 * Field to hold the ellipsoid whose state we want to view
	 */
	private JanusEllipsoid je;

	// CONSTRUCTOR
	
	/**
	 * Create a JanusEllipsoid viewer on top of the double ellipsoid
	 * state object passed as the argument to the constructor
	 * 
	 * @param jeview Janus ellipsoid state to view
	 */
	public JanusEllipsoidViewer(JanusEllipsoid jeview) {
		je = jeview;
		setOpaque(true);
		setBackground(Color.WHITE);
	}
	
	// PAINTING
	
	/**
	 * This is the method in which all the drawing happens. It is called automatically by Swing when the component needs to
	 * be drawn or updated.
	 * 
	 * @param g current graphics context (an object that provides the drawing methods)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		// First, get the dimensions of the component
		int width = getWidth(), height = getHeight();

		// cast to Graphics2D for the rotation of the ellipse
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform old = g2d.getTransform();

		// Clear the background if we are an opaque component
		if(isOpaque()) {
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, width, height);
		}
		
		// Set up the coordinate system; the idea is that the real-space point (x,y) maps to the onscreen point (ox+sx*x, oy+sy*y)
		// where (ox,oy) is the position of the origin, and sx, sy are scale factors

		// The origin goes at the centre; in Swing, the y coordinate increases from 0 at the top to height at the bottom
		double ox = (double)width/2.0, oy = (double)height/2.0;
		// Fit the full possible vertical length of the ellipsoid into the display
		// We need a minus sign here to take into account that a negative real-space coordinate is a positive component-space coordinate
		double sy = -(double)height/2.0/je.getMaximumLength();
		// Set the x scaling to be the same as the y scaling - and correct for the minus sign
		double sx = -sy;
		
		// Get the real-space positions of the two bobs: we need to acquire the lock on the JanusEllipsoid object
		// to ensure that the state is consistent; we also notify any waiting threads that the data have been obtained
		Vector3d center;
		Quaternion rot;
		synchronized(je) {
			// Uncomment the following line to help identify concurrency problems
//			System.out.println("Drawing position at t="+je.getTime());
			center = je.center;
			rot    = je.rot;
			// Uncomment the following line to help identify concurrency problems
//			System.out.println("Data received, t="+je.getTime());
			je.notifyAll();
		}

		// rescaled axis
		double rx = sx*je.getAxis().a[0];
		double ry = sx*je.getAxis().a[2];
		// rescaled center
		double cx = sx*center.a[0];
		double cy = sy*center.a[2];
		
		// Draw the interface at z=0
		g2d.setColor(Color.BLUE);
		g2d.fillRect(0, height/2, width, height/2);

		// orientation of the ellipse
//		Vector3d xrotated = rot.rotate(XAXIS);
//		Vector3d yrotated = rot.rotate(YAXIS);
		Vector3d zrotated = rot.rotate(ZAXIS);
		double theta = Math.acos(Vector3d.dot(zrotated, ZAXIS));
//		double phi  = Math.atan2(Vector3d.dot(yrotated, ZAXIS), Vector3d.dot(xrotated, ZAXIS));
		
		// Now add the ellipsoid
		g2d.translate(cx, cy);
		g2d.rotate(theta,  ox, oy);
//		if(phi>=0.) g2d.rotate(theta,  ox, oy);
//		else        g2d.rotate(-theta, ox, oy);
		g2d.setColor(Color.RED);
		g2d.fillOval((int)(ox-rx), (int)(oy-ry), (int)(2.0*rx), (int)(2.0*ry));
		g2d.setColor(Color.YELLOW);
		int alpha = (int)(180.*je.getAlpha()/Math.PI);
		g2d.fill(new Arc2D.Double((int)(ox-rx), (int)(oy-ry), (int)(2.0*rx), (int)(2.0*ry), 90-alpha, 2*alpha, Arc2D.CHORD));
		g2d.setTransform(old);

	}

	/**
	 * Main method for setting up an initial condition, iterating the equations of motion, and displaying a the system
	 * 
	 * @param args	command line arguments, currently ignored
	 */
	public static void main(String args[]) {
		
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

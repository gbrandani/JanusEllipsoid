import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * An interactive version of the pendulum viewer. This class interacts with a <code>JanusEllipsoid</code> object
 * indirectly via the controller object passed to the constructor when it is created.  It uses a <code>MouseListener</code>
 * to track mouse movements and thereby manipulation of the initial positions and velocities via methods provided by the
 * <code>InteractiveJanusEllipsoid</code> class.
 * 
 * @author Giovanni Brandani
 *
 */
public class InteractiveJanusEllipsoidViewer extends JComponent {
	
	/**
	 * constants
	 */
	private static final double BOB_RADIUS = 10;
	private static final int ANIMATION_DELAY = 50;	// Number of milliseconds between frames of an animation. 50ms = 20fps
	private static final Vector3d XAXIS = new Vector3d(1.,0.,0.);	// x axis
	private static final Vector3d YAXIS = new Vector3d(0.,1.,0.);	// y axis
	private static final Vector3d ZAXIS = new Vector3d(0.,0.,1.);	// z axis

	/**
	 * Field to hold the pendulum whose state we want to view
	 */
	private InteractiveJanusEllipsoid controller;
	
	/**
	 * A timer object to perform the automatic updating of the window while the simulation is running
	 */
	private Timer autoUpdater = new Timer(ANIMATION_DELAY, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) { repaint(); }		
	});
	
	//  USER INTERACTION
	
	/**
	 * A hotspot is a circular draggable region, centred on x,y; radius r 
	 */
	private static class Hotspot {
		int x, y, r;
	}
	
	// We will always have 2 hotspots, one to move the center ad one to rotate
	private Hotspot[] hotspot = new Hotspot[2];
	private int activeHotspot = -1; // which hotspot are we dragging? (-1 = none)
	private Point displacement = new Point(0,0); // displacement of active hotspot
	
	/**
	 * A class that observes mouse movements to allow the user to set the bob positions
	 */
	private MouseAdapter mouseFollower = new MouseAdapter() {
		private Point lastClick = null; // start of drag, if drag in progress

		@Override
		public void mousePressed(MouseEvent e) {
			lastClick = e.getPoint();
			// Process the hotspots in REVERSE order, so that he who is plotted last (frontmost) is detected first
			for(int i=hotspot.length-1; i>=0; i--) {
				int dx = lastClick.x - hotspot[i].x, dy = lastClick.y - hotspot[i].y;
				if(dx*dx + dy*dy <= hotspot[i].r*hotspot[i].r) {
					activeHotspot = i;
					displacement.x = displacement.y = 0;
					addMouseMotionListener(mouseFollower);
					break;
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(activeHotspot == -1) return;
			Point p = e.getPoint();
			displacement.x = p.x - lastClick.x;
			displacement.y = p.y - lastClick.y;
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(activeHotspot == -1) return;
			Point p = e.getPoint();
			displacement.x = p.x - lastClick.x;
			displacement.y = p.y - lastClick.y;
			hotspotDragEnded();
			removeMouseMotionListener(mouseFollower);
			activeHotspot = -1;
			repaint();
		}
		
	};
	
	// CONSTRUCTOR
	
	/**
	 * Create a JanusEllipsoid viewer on top of the double pendulum
	 * state object passed as the argument to the constructor
	 * 
	 * @param attachedTo the interactive double pendulum controller that we are attached to
	 */
	public InteractiveJanusEllipsoidViewer(InteractiveJanusEllipsoid attachedTo) {
		controller = attachedTo;
		setOpaque(true);
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(600,600));
		// Set up hotspots
		for(int i=0; i<hotspot.length; i++) {
			hotspot[i] = new Hotspot();
		}
		// Listen for mouse events
		addMouseListener(mouseFollower);
	}
	
	// COORDINATE SYSTEM AND HOTSPOTS
	
	/**
	 * Position of origin (ox, oy) and scale factors (sx, sy) in transformation from real coordinates (x,y) to
	 * screen coordinates (ox+sx*x, oy+sy*y) 
	 */
	private double ox, oy, sx, sy;
	private int width, height;
	
	private void setupCoordinateSystem() {
		width  = getWidth();
		height = getHeight();
		// Origin goes in the centre of the window
		ox = (double)width/2.0;
		oy = (double)height/2.0;
		// minus sign here because a negative real-space coordinate is a positive component-space coordinate
		sy = -(double)height/2.0/controller.state.getMaximumLength();
		// Set the x scaling to be the same as the y scaling - and correct for the minus sign
		sx = -sy;
	}

	private void hotspotDragEnded() {
		// Use the displacement of the hotspot and current coordinate system to work out the new state to pass to the controller
		RigidBodyState state = controller.getRigidBodyState();
		if(activeHotspot==0) {	// change the z component of center of the ellipsoid
			state.center.a[2] = state.center.a[2] + (double)(displacement.y/sy);
			controller.setCenter(state.center);
		}
		else if(activeHotspot==1) {	// change the orientation of the ellipsoid
			double rx = sx*controller.state.getAxis().a[0];
			double ry = sx*controller.state.getAxis().a[2];
			Vector3d zrotated = state.rot.rotate(ZAXIS);
			double theta = Math.acos(Vector3d.dot(zrotated, ZAXIS));
			double tmpx = Math.sin(theta)*ry+displacement.x;
			double tmpy = Math.cos(theta)*ry-displacement.y;
			theta = Math.acos( tmpy/Math.sqrt(tmpx*tmpx+tmpy*tmpy) );
			state.rot = new Quaternion( Math.cos(theta/2.), Vector3d.mult(XAXIS, Math.sin(theta/2.)));
			controller.setRot(state.rot);
		}
	}
	
	// PAINTING
	
	protected void paintComponent(Graphics g) {

		// Clear the background if we are an opaque component
		if(isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		setupCoordinateSystem();
		
		// cast to Graphics2D for the rotation of the ellipse
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform old = g2d.getTransform();

		// Get the most recent simulation state: all thread-safety issues are delegated to the controller object, so
		// we don't need any synchronized blocks here
		RigidBodyState state = controller.getRigidBodyState();

		// rescaled axis
		double rx = sx*controller.state.getAxis().a[0];
		double ry = sx*controller.state.getAxis().a[2];
		// rescaled center
		double cx = ox+sx*state.center.a[0];
		double cy = oy+sy*state.center.a[2];
		// orientation of the ellipse
		Vector3d zrotated = state.rot.rotate(ZAXIS);
		double theta = Math.acos(Vector3d.dot(zrotated, ZAXIS));

		if(!controller.isRunning()) {
			// Compute hotspot positions (NOTE: this is slightly inefficient, only really need to do this when the state is updated)
			// hotspot at the center of the ellipsoid
			hotspot[0].x = (int)cx;
			hotspot[0].y = (int)cy;
			hotspot[0].r = (int)BOB_RADIUS;
			// hotspot on the cap of the ellipsoid
			hotspot[1].x = (int)(cx+Math.sin(theta)*ry);	
			hotspot[1].y = (int)(cy-Math.cos(theta)*ry);	// sign correct the orientation of the y axis
			hotspot[1].r = (int)BOB_RADIUS;
			// If we happen to be dragging one of the particles, move it before drawing
			if(activeHotspot == 0) {	// translate
				cy += displacement.y;
			}
			else if(activeHotspot == 1) {	// rotate
				double tmpx = Math.sin(theta)*ry+displacement.x;
				double tmpy = Math.cos(theta)*ry-displacement.y;
				theta = Math.acos( tmpy/Math.sqrt(tmpx*tmpx+tmpy*tmpy) );
			}
		}
		
		// Draw the interface at z=0
		g2d.setColor(Color.BLUE);
		g2d.fillRect(0, height/2, width, height/2);
		g.drawString( "Oil",   30, 30);
		g2d.setColor(Color.WHITE);
		g.drawString( "Water", 30, height-30);

		// Now add the ellipsoid
		g2d.rotate(theta,  cx, cy);
		g2d.setColor(Color.RED);
		g2d.fillOval((int)(cx-rx), (int)(cy-ry), (int)(2.0*rx), (int)(2.0*ry));
		g2d.setColor(Color.YELLOW);
		int alpha = (int)(180.*controller.state.getAlpha()/Math.PI);
		g2d.fill(new Arc2D.Double((int)(cx-rx-1), (int)(cy-ry-1), (int)(2.0*rx+1), (int)(2.0*ry+1), 90-alpha, 2*alpha, Arc2D.CHORD));
		g2d.setTransform(old);

		// finally, paint the hotspots
		if(!controller.isRunning()) {
			g2d.setColor(Color.BLACK);
			g2d.fillOval((int)(cx-BOB_RADIUS), (int)(cy-BOB_RADIUS), (int)(2.0*BOB_RADIUS), (int)(2.0*BOB_RADIUS));
			g2d.fillOval((int)(cx+Math.sin(theta)*ry-BOB_RADIUS), (int)(cy-Math.cos(theta)*ry-BOB_RADIUS), (int)(2.0*BOB_RADIUS), (int)(2.0*BOB_RADIUS));
		}

	}


	//  MESSAGES
	
	/**
	 * This is received whenever the simulation displayed by this view has started running
	 */
	public void simulationStarted() {
		// Stop following the mouse while the simulation is running
		removeMouseListener(mouseFollower);
		// Ensure display is up-to-date
		repaint();
		// Send repaint messages at regular intervals using the autoUpdater Timer object
		autoUpdater.restart();
	}

	/**
	 * This is received whenever the simulation displayed by this view has stopped running
	 */
	public void simulationFinished() {
		// Stop sending regular repaint messages
		autoUpdater.stop();
		// Ensure display is up-to-date
		repaint();
		// Start following the mouse again
		addMouseListener(mouseFollower);
	}
	
}

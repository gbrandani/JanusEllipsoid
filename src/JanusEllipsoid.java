/**
 * 
 * Representation and dynamics of a Janus ellipsoidal particle
 * located at an interface between water and oil.
 * 
 * @author Giovanni Brandani
 *
 */

public class JanusEllipsoid {

	// STATE

	/**
	 * constants
	 */
	public static final int DIM = 3;	// dimensionality
	public static final int DEFAULTNGRID = 100;	// default ngrid
	public static final double ZINTER = 0.0;	// Z coordinate of the interface

	/**
	 * Fields for the state of our system
	 * and the interaction with the interface
	 * the quaternion is initialised as a rotation of 0 degrees
	 */
	private Vector3d axis = new Vector3d(0.,0.,0.);	// a,b,c half axis of the ellipsoid
	private double alpha = 0.;	// angle that defines the position of the apolar cap
	private Vector3d[] grid;	// grid of points on the surface of the ellipse
	private double[] dAgrid;	// elements of area corresponding to each point on the surface
	private int[]  typegrid;	// type of the point on the grid: apolar 0 or polar 1
	public Vector3d center = new Vector3d(0.,0.,0.);	// center of the ellipsoid
	public Quaternion rot  = new Quaternion(1., new Vector3d(0.,0.,0.));	// rotation of the ellipsoid wrt the x,y,z axis
	public double gamma = 0.;	// gammaOW; oil-water surface tension in units of kT
	public double costhetaP = 0.;	// gammaOW cos(thetaP) = gammaPO - gammaPW; polar-oil,  polar-water  surface tensions
	public double costhetaA = 0.;	// gammaOW cos(thetaA) = gammaPO - gammaPW; apolar-oil, apolar-water surface tensions
	public double time = 0.;	// current time
	private double Sao=0., Spo=0., Saw=0., Spw=0., Si=0.;	// areas of each side of the particle in contact with each solvent

	// CONSTRUCTORS

	/**
	 * Basic constructor
	 * 
	 * @param NONE
	 */
//	public JanusEllipsoid() {
		// nothing to do
//	}

	/**
	 * Standard constructor
	 * 
	 * @param axis		half axis lengths
	 * @param alpha		angle that defines the patch
	 * @param ngrid		resolution of the surface points
	 */
	public JanusEllipsoid(Vector3d axis, double alpha, int ngrid) {
		this.axis  = new Vector3d(axis);
		this.alpha = alpha;
		this.setupGrid(ngrid);
		this.updateAreas();
	}

	/**
	 * Complete constructor
	 * 
	 * @param axis		half axis lengths
	 * @param alpha		angle that defines the patch
	 * @param ngrid		resolution of the surface points
	 */
	public JanusEllipsoid(Vector3d axis, double alpha, int ngrid, double gamma, double costhetaA, double costhetaP) {
		this.axis  = new Vector3d(axis);
		this.alpha = alpha;
		this.setupGrid(ngrid);
		this.updateAreas();
		this.gamma = gamma;
		this.costhetaA = costhetaA;
		this.costhetaP = costhetaP;
	}

	/**
	 * Copy constructor
	 * 
	 * @param je	Janus ellipsoid to be copied
	 */
	public JanusEllipsoid(JanusEllipsoid je) {
		this.axis = new Vector3d(je.getAxis());
		this.alpha = je.getAlpha();
		this.grid = new Vector3d[je.getGrid().length];
		this.dAgrid = new double[je.getGrid().length];
		this.typegrid =  new int[je.getGrid().length];
		for(int k=0; k<je.getGrid().length; k++) {
			this.grid[k] = new Vector3d(je.getGrid()[k]);
			this.dAgrid[k] = je.getdAgrid()[k];
			this.typegrid[k] = je.getTypegrid()[k];
		}
		this.center = new Vector3d(je.center);
		this.rot    = new Quaternion(je.rot);
		this.gamma     = je.gamma;
		this.costhetaP = je.costhetaP;
		this.costhetaA = je.costhetaA;
		this.time      = je.time;
		this.updateAreas();
	}

	// ACCESSORS

	/**
	 * Set the grid of points on the surface of the ellipse
	 *
	 * @param ngrid
	 */
	public void setupGrid(int ngrid) {
		int i,j;
		double theta,phi;
		Vector3d v1 = new Vector3d();	// = d r(theta,phi) / d theta
		Vector3d v2 = new Vector3d();	// = d r(theta,phi) / d phi
		this.grid = new Vector3d[ngrid*ngrid];
		this.dAgrid = new double[this.grid.length];
		this.typegrid =  new int[this.grid.length];
		for(int k=0; k<this.grid.length; k++) {
			i = k/ngrid;
			j = k%ngrid;
			theta =  Math.PI*(i+0.5)/ngrid;
			phi = 2.*Math.PI*j/ngrid;
			this.grid[k] = new Vector3d();
			this.grid[k].a[0] = this.axis.a[0]*Math.sin(theta)*Math.cos(phi);
			this.grid[k].a[1] = this.axis.a[1]*Math.sin(theta)*Math.sin(phi);
			this.grid[k].a[2] = this.axis.a[2]*Math.cos(theta);
			v1.a[0] = this.axis.a[0]*Math.cos(theta)*Math.cos(phi);
			v1.a[1] = this.axis.a[1]*Math.cos(theta)*Math.sin(phi);
			v1.a[2] =-this.axis.a[2]*Math.sin(theta);
			v2.a[0] =-this.axis.a[0]*Math.sin(theta)*Math.sin(phi);
			v2.a[1] = this.axis.a[1]*Math.sin(theta)*Math.cos(phi);
			v2.a[2] = 0.;
			this.dAgrid[k] = Vector3d.cross(v1,v2).norm() * 2.*Math.PI*Math.PI/ngrid/ngrid;
			if(theta<this.alpha) this.typegrid[k] = 0;	// apolar point
			else                 this.typegrid[k] = 1;	// polar  point
		}
	}

	/**
	 * Getters
	 *
	 */
	public Vector3d getAxis()   { return new Vector3d(this.axis); }
	public double getAlpha()    { return this.alpha; }
	public Vector3d[] getGrid() { return this.grid; }
	public double[] getdAgrid() { return this.dAgrid; }
	public int[] getTypegrid()  { return this.typegrid; }
	public double getSao()      { return this.Sao; }
	public double getSpo()      { return this.Spo; }
	public double getSaw()      { return this.Saw; }
	public double getSpw()      { return this.Spw; }
	public double getSi()       { return this.Si;  }

	/**
	 * Setters
	 * 
	 * @param axis: half axis lengths
	 * @param alpha: angle that defines the patch
	 * @param ngrid		resolution of the surface points
	 */
	public void resetEllipsoid(Vector3d axis, double alpha, int ngrid) {
		this.axis  = new Vector3d(axis);
		this.alpha = alpha;
		this.setupGrid(ngrid);
		this.updateAreas();
	}

	/**
	 * Get twice the maximum of the axis, for the visualisation
	 * 
	 * @return twice the maximum axis of the ellipsoid
	 */
	public double getMaximumLength() {
		double max = 0.;
		for(int i=0; i<this.DIM; i++) if(2.*this.axis.a[i]>max) max=2.*this.axis.a[i];
		return max;
	}

	// COMPUTES

	/**
	 * Compute the free energy of the system
	 * Delta G = gammaOW ( S_AO cos(thetaA) + S_PO cos(thetaP) - A_I )
	 */
	public double energy() {
		double ener = this.gamma*( this.Sao*this.costhetaA + this.Spo*this.costhetaP - this.Si );	// total energy
		return ener;
	}

	/**
	 * Get area of each side of the particle in each environment
	 *
	 */
	public void updateAreas() {
		this.Sao = 0.;	// S_AO  apolar side in oil
		this.Spo = 0.;	// S_PO  polar  side in oil
		this.Saw = 0.;	// S_AW  apolar side in water
		this.Spw = 0.;	// S_PW  polar  side in water
		this.Si  = 0.;	// S_I   area occupied by the protein instead of the interface
		Vector3d r = new Vector3d();	// point on the surface
		// compute S_ao,po,aw,pw
		for(int i=0; i<this.grid.length; i++) {
			r = this.rot.rotate(this.grid[i]);
			if((r.a[2]+this.center.a[2])>this.ZINTER) {	// the point is in oil
				if(this.typegrid[i]==0) Sao += this.dAgrid[i];	// the point is in the apolar side
				else                    Spo += this.dAgrid[i];	// the point is in the polar  side
			}
			else {				// the point is in water
				if(this.typegrid[i]==0) Saw += this.dAgrid[i];	// the point is in the apolar side
				else                    Spw += this.dAgrid[i];	// the point is in the polar  side
			}
		}
		// compute Si
		// the distance of the interface from the center and the normal to the interface
		double dist = center.a[2];
		// the unit vector normal to the interface (z axis) in the reference frame of the rotated ellipsoid
		Vector3d n = this.rot.inverse().rotate(new Vector3d(0.,0.,1.));
		this.Si = PlaneEllipsoidIntersec.area(dist, n, this.axis);
	}

	// DYNAMICS

	/**
	 * Perform n timesteps, each of length h, of the integration algorithm.
	 * A rotation of phi around an axis v is given by the quaternion:
	 * q = cos(phi/2) + sin(phi/2) v
	 * 
	 * @param n          number of timesteps to iterate
	 * @param sigmadz    sigma of the infinitesimal dispacement along z
	 * @param sigmadphi  sigma of the infinitesimal angle of rotation
	 */
	public void iterate(int n, double sigmadz, double sigmadphi) {
		double en0, en1;	// initial and final energies
		double dz;		// infinitesimal dispacement along z
		double dphi, cosdphihalf, sindphihalf;	// infinitesimal angle of rotation
		double axcostheta, axsintheta, axphi;	// for the axis of rotation
		Quaternion drot = new Quaternion();	// infinitesimal quaternion rotation
		Vector3d drotim = new Vector3d();	// imaginary part of the quaternion rotation
		double oldSao, oldSpo, oldSaw, oldSpw, oldSi;	// old areas
		this.updateAreas();	// so that you can compute the energy in the first step
		oldSao = this.Sao;
		oldSpo = this.Spo;
		oldSaw = this.Saw;
		oldSpw = this.Spw;
		oldSi  = this.Si;
		for(int step=0; step<n; step++) {
			// Here you can do stuff as long as you dont update the state
			en0 = energy();		// compute the initial energy
			// generate a random dispacement and rotation
			dz   = Mathroutines.gaussrand()*sigmadz;
			dphi = Mathroutines.gaussrand()*sigmadphi;
			cosdphihalf = Math.cos(0.5*dphi);
			sindphihalf = Math.sin(0.5*dphi);
			axcostheta  = 2.*Math.random()-1.;
			axsintheta  = Math.sqrt(1.-axcostheta*axcostheta);
			axphi       = 2.*Math.PI*Math.random();
			drotim.a[0] = sindphihalf * axsintheta*Math.cos(axphi);
			drotim.a[1] = sindphihalf * axsintheta*Math.sin(axphi);
			drotim.a[2] = sindphihalf * axcostheta;
			drot.re = cosdphihalf;
			drot.im = drotim;
			// Here we start updating state, so to make this thread-safe, we need to obtain a lock to continue
			synchronized(this) {
				// update the position and the orientation
				this.center.a[2] += dz;
				this.rot = Quaternion.prod(drot,rot);
				this.updateAreas();
				en1 = energy();
				if( Math.random()>Math.exp(-(en1-en0)) ) {
					this.center.a[2] += -dz;
					this.rot = Quaternion.prod(drot.inverse(),rot);
					this.Sao = oldSao;
					this.Spo = oldSpo;
					this.Saw = oldSaw;
					this.Spw = oldSpw;
					this.Si  = oldSi;
				}
				this.rot.correct();	// FIXME: correct for numerical errors, does it work?
				this.time += 1.;
				oldSao = this.Sao;
				oldSpo = this.Spo;
				oldSaw = this.Saw;
				oldSpw = this.Spw;
				oldSi  = this.Si;
			}
			// The lock is now released, and other threads are ok to read the state of the system
		}
	}

	//   MAIN METHOD

	/**
	 * Main method
	 */
	public static void main(String args[]) {

		// set up initial parameters as in BslA
		double Lx = 1.4;
		double Ly = 1.4;
		double Lz = 2.6;
		double alpha = 50.4;	// angle fo the patch
		double gamma = 12.;	// water oil surface tension in kT
		double costhetaA =-0.2;	// apolar side
		double costhetaP = 0.5;	// polar side
		double dz = 0.01;
		double dphi = 0.01;
		int dt = 100;
		int nsteps = 10000;

		// parse command line arguments
		System.out.println("# Usage: java JanusEllipsoid [-LxLy val] [-Lz val] [-alpha val] [-gamma val] [-costhetaA val] [-costhetaP val] [-dz val] [-dphi val] [-dt val] [-nsteps val]");
		for(int i=0; i<args.length; i++) {
			if( args[i].equals("-LxLy") ) {
				try { Lx = Ly = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-Lz") ) {
				try { Lz = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-alpha") ) {
				try { alpha = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-gamma") ) {
				try { gamma = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-costhetaA") ) {
				try { costhetaA = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-costhetaP") ) {
				try { costhetaP = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-dz") ) {
				try { dz = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-dphi") ) {
				try { dphi = Double.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-dt") ) {
				try { dt = Integer.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
			if( args[i].equals("-nsteps") ) {
				try { nsteps = Integer.valueOf(args[i+1]); }
				catch(Exception e) { System.out.println("# Command line argument error"); System.exit(-1); }
			}
		}

		JanusEllipsoid janus = new JanusEllipsoid(new Vector3d(Lx,Ly,Lz), Math.PI*alpha/180., JanusEllipsoid.DEFAULTNGRID, gamma, costhetaA, costhetaP);
		janus.updateAreas();
		System.out.print("# Generated by: java JanusEllipsoid");
		for(int i=0; i<args.length; i++) System.out.print( " " + args[i] );
		System.out.println("");
		System.out.println("# Lx = "          + janus.getAxis().a[0] );
		System.out.println("# Ly = "          + janus.getAxis().a[1] );
		System.out.println("# Lz = "          + janus.getAxis().a[2] );
		System.out.println("# alpha = "       + 180.*janus.getAlpha()/Math.PI );
		System.out.println("# gamma = "       + janus.gamma );
		System.out.println("# cos(thetaA) = " + janus.costhetaA );
		System.out.println("# cos(thetaP) = " + janus.costhetaP );
		System.out.println("# Area(cap) = "   + (janus.getSao()+janus.getSaw()) );
		System.out.println("# Area(nocap) = " + (janus.getSpo()+janus.getSpw()) );
		System.out.println("# dz = "          + dz );
		System.out.println("# dphi = "        + dphi );
		System.out.println("# dt = "          + dt );

		double theta = 0.;	// angle to the interface
		final Vector3d ZAXIS = new Vector3d(0.,0.,1.);
		Vector3d zrotated = new Vector3d(ZAXIS);

		long startTime = System.currentTimeMillis();
		System.out.println("# time z(nm) theta(rad) energy(kT) energy(kT) Sao(nm^2) Spo(nm^2) Si(nm^2)");
		for(int i=0; i<nsteps; i++) {
			zrotated = janus.rot.rotate(ZAXIS);
	                theta = Math.acos(Vector3d.dot(zrotated, ZAXIS));
			System.out.printf("%d\t%f\t%f\t%f\t%f\t%f\t%f\n", i, janus.center.a[2], theta, janus.energy(), janus.getSao(), janus.getSpo(), janus.getSi());
			janus.iterate(dt, dz, dphi);
		}
		long endTime = System.currentTimeMillis();
		//System.out.println("That took " + (endTime - startTime) + " milliseconds");

	}
}

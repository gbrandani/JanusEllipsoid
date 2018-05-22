/**
 * This is an implementation of a quaternion
 * 
 * @author Giovanni Brandani
 *
 */
public class Quaternion {
	
	//  FIELDS
	
	public double re = 0.;			// real part
	public Vector3d im = new Vector3d();	// imaginary part
	
	//  CONSTRUCTORS
	
	/**
	 * The default constructor creates a quaternion equal to zero
	 */
	public Quaternion() {
		// nothing to do here
	}
	
	/**
	 * This alternative constructor allows one to create a quaternion with
	 * arbitrary elements
	 * 
	 * @param re real part
	 * @param im imaginary part
	 */
	public Quaternion(double re, Vector3d im) {
		this.re = re;
		this.im = new Vector3d(im);
	}

	/**
	 * Copy constructor
	 * 
	 * @param q
	 */
	public Quaternion(Quaternion q) {
		this.re = q.re;
		this.im = new Vector3d(q.im);
	}
	
	//   ACCESSORS

	/**
	 * Correct for the fact that this is a rotation
	 * q = cos(theta/2) + sin(theta/2) v
	 */
	public void correct() {
		double sinhalf = Math.sqrt(1.-this.re*this.re);	// norm of the imaginary part has to be this
		double normim = this.im.norm();		// what the norm is now
		if(normim>0.00000001) this.im = Vector3d.mult(this.im, sinhalf/normim);
	}
	
	/**
	 * Get the opposite quaternion
	 * 
	 * @return the opposite quaternion
	 */
	public Quaternion inverse() {
		return new Quaternion(this.re, this.im.minus());
	}

	/**
	 * Rotate a target vector
	 *
	 * @return the rotated vector vr = q v q-1
	 */
	/*public Vector3d rotate(Vector3d v) {
		Quaternion vq = new Quaternion(0., v);
		Quaternion vqr = Quaternion.prod(this, Quaternion.prod(vq, this.inverse()));
		return new Vector3d(vqr.im);
	}*/

	/**
	 * Fast rotation of a target vector
	 *
	 * @return the rotated vector vr = q v q-1
	 */
	public Vector3d rotate(Vector3d v) {
		Quaternion vq = new Quaternion(0., v);
		Quaternion vqr = Quaternion.prod(this, Quaternion.prod(vq, this.inverse()));
		return new Vector3d(vqr.im);
	}

	//  STATIC METHODS
	
	/**
	 * Add up two quaternions
	 * 
	 * @param v first  quaternion
	 * @param w second quaternion
	 * @return v plus w
	 */
	public static Quaternion add(Quaternion v, Quaternion w) {
		return new Quaternion(v.re+w.re, Vector3d.add(v.im,w.im));
	}

	/**
	 * Hamilton product of two quaternions
	 * 
	 * @param v first  quaternion
	 * @param w second quaternion
	 * @return v w
	 */
	/*public static Quaternion prod(Quaternion v, Quaternion w) {
		double   r  = v.re*w.re - Vector3d.dot(v.im,w.im);
		Vector3d i1 = v.im.mult(w.re);
		Vector3d i2 = w.im.mult(v.re);
		Vector3d i3 = Vector3d.cross(v.im,w.im);
		Vector3d i  = Vector3d.add(Vector3d.add(i1,i2), i3);
		return new Quaternion(r,i);
	}*/

	/**
	 * Fast Hamilton product of two quaternions
	 * q1 q2 =    a_1*a_2 - b_1*b_2 - c_1*c_2 - d_1*d_2
	 *         + (a_1*b_2 + b_1*a_2 + c_1*d_2 - d_1*c_2)i
	 *         + (a_1*c_2 - b_1*d_2 + c_1*a_2 + d_1*b_2)j
	 *         + (a_1*d_2 + b_1*c_2 - c_1*b_2 + d_1*a_2)k
	 * 
	 * @param q1 first  quaternion
	 * @param q2 second quaternion
	 * @return q1 q2
	 */
	public static Quaternion prod(Quaternion q1, Quaternion q2) {
		Quaternion q = new Quaternion();
		q.re      = q1.re*q2.re - q1.im.a[0]*q2.im.a[0] - q1.im.a[1]*q2.im.a[1] - q1.im.a[2]*q2.im.a[2];
		q.im.a[0] = q1.re*q2.im.a[0] + q1.im.a[0]*q2.re + q1.im.a[1]*q2.im.a[2] - q1.im.a[2]*q2.im.a[1];
		q.im.a[1] = q1.re*q2.im.a[1] - q1.im.a[0]*q2.im.a[2] + q1.im.a[1]*q2.re + q1.im.a[2]*q2.im.a[0];
		q.im.a[2] = q1.re*q2.im.a[2] + q1.im.a[0]*q2.im.a[1] - q1.im.a[1]*q2.im.a[0] + q1.im.a[2]*q2.re;
		return q;
	}

	/**
	 * Provide a string representation of the quaternion; this allows the quaternion
	 * to be used anywhere where a string is expected.
	 */
	@Override
	public String toString() {
		return "( " + this.re + ", " + this.im + " )";
	}

	// MAIN

	/**
	 * Main method for testing the class
	 */
        public static void main(String args[]) {
		// test product
		//Quaternion q1 = new Quaternion(1., new Vector3d(0.,0.,2.));
		//Quaternion q2 = new Quaternion(1., new Vector3d(0.,1.,0.));
		//System.out.printf("%s\n", Quaternion.prod(q1,q2));
		// test rotation
		double theta = Math.PI/4.;	// rotate by 180 degrees
		Vector3d axis = new Vector3d(0.,0.,1.);	// rotate around z axis
		Quaternion rot = new Quaternion(Math.cos(theta/2.), Vector3d.mult(axis, Math.sin(theta/2.)));
		Vector3d v = new Vector3d(0.,1.,0.);
		Vector3d vr = rot.rotate(v);
		System.out.printf("%s\n", v);
		System.out.printf("%s\n", rot);
		System.out.printf("%s\n", vr);
	}

}

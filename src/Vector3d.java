/**
 * This is an implementation of a vector
 * 
 * @author Giovanni Brandani
 *
 */
public class Vector3d {
	
	//  FIELDS
	
	// The x, y and z coordinates
	public static final int DIM = 3;	// dimensionality
	public double[] a = new double[DIM];	// x,y,z components
	
	//  CONSTRUCTORS
	
	/**
	 * The default constructor creates a vector of zero length
	 */
	public Vector3d() {
		for(int i=0; i<DIM; i++) this.a[i]=0.0;
	}
	
	/**
	 * This alternative constructor allows one to create a vector with
	 * arbitrary elements
	 * 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param z z-coordinate
	 */
	public Vector3d(double x, double y, double z) {
		this.a[0] = x; this.a[1] = y; this.a[2] = z;
	}

	/**
	 * Copy constructor
	 * 
	 * @param v
	 */
	public Vector3d(Vector3d v) {
		this.a[0] = v.a[0]; this.a[1] = v.a[1]; this.a[2] = v.a[2];
	}
	
	//   ACCESSORS

	/**
	 * Get the square of the length of the vector
	 * 
	 * @return	square length of vector
	 */
	public double norm2() {
		return dot(this,this);
	}
	
	/**
	 * Get the length of the vector, i.e., sqrt(norm2)
	 * 
	 * @return	length of the vector
	 */
	public double norm() {
		return Math.sqrt(dot(this,this));
	}

	/**
	 * Get the opposite of the vector
	 */
	public Vector3d minus() {
		return new Vector3d(-this.a[0], -this.a[1], -this.a[2]);
	}

	/**
	 * Multiply this vector by a scalar
	 * 
	 * @param s scalar to multiply by
	 * @return s times v
	 */
	public Vector3d mult(double s) {
		return new Vector3d(s*this.a[0], s*this.a[1], s*this.a[2]);
	}
	
	//  STATIC METHODS COMBINING PAIRS OF VECTORS, OR A SCALAR WITH A VECTOR
	
	/**
	 * Form the sum of two vectors
	 * 
	 * @param v first vector
	 * @param w second vector
	 * @return v plus w
	 */
	public static Vector3d add(Vector3d v, Vector3d w) {
		return new Vector3d(v.a[0]+w.a[0], v.a[1]+w.a[1], v.a[2]+w.a[2]);
	}

	/**
	 * Form the difference between two vectors
	 * 
	 * @param v first vector
	 * @param w second vector
	 * @return v minus w
	 */
	public static Vector3d sub(Vector3d v, Vector3d w) {
		return new Vector3d(v.a[0]-w.a[0], v.a[1]-w.a[1], v.a[2]-w.a[2]);
	}

	/**
	 * Form the dot product of two vectors
	 * 
	 * @param v first vector
	 * @param w second vector
	 * @return v dot w
	 */
	public static double dot(Vector3d v, Vector3d w) {
		return v.a[0]*w.a[0] + v.a[1]*w.a[1] + v.a[2]*w.a[2];
	}
	
	/**
	 * Form the cross product of two vectors
	 * 
	 * @param v first vector in cross product
	 * @param w second vector in cross product
	 * @return v cross w
	 */
	public static Vector3d cross(Vector3d v, Vector3d w) {
		return new Vector3d(v.a[1]*w.a[2] - v.a[2]*w.a[1], v.a[2]*w.a[0] - v.a[0]*w.a[2], v.a[0]*w.a[1] - v.a[1]*w.a[0]);
	}
	
	/**
	 * Multiply a vector by a scalar
	 * 
	 * @param v vector to multiply
	 * @param s scalar to multiply by
	 * @return s times v
	 */
	public static Vector3d mult(Vector3d v, double s) {
		return new Vector3d(s*v.a[0], s*v.a[1], s*v.a[2]);
	}
	
	/**
	 * Divide all components of a vector by a scalar
	 * 
	 * @param v vector to divide
	 * @param s factor to divide by
	 * @return (1/s) times v
	 */
	public static Vector3d divide(Vector3d v, double s) {
		return mult(v, 1.0/s);
	}

	/**
	 * Provide a string representation of the vector; this allows the vector
	 * to be used anywhere where a string is expected.
	 */
	@Override
	public String toString() {
		return "( " + a[0] + ", " + a[1] + ", " + a[2] + " )";
	}

}

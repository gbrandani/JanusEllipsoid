/**
 * This is an implementation of a quaternion
 * 
 * @author Giovanni Brandani
 *
 */
public class RigidBodyState {
	
	//  FIELDS
	public Vector3d center = new Vector3d();
	public Quaternion rot  = new Quaternion();
	
	//  CONSTRUCTORS
	
	/**
	 * The default constructor creates a quaternion equal to zero
	 */
	public RigidBodyState() {
		// nothing to do here
	}
	
	/**
	 * This alternative constructor allows one to create a state with
	 * arbitrary elements
	 */
	public RigidBodyState(Vector3d v, Quaternion q) {
		this.center = new Vector3d(v);
		this.rot    = new Quaternion(q);
	}

	/**
	 * Copy constructor
	 */
	public RigidBodyState(RigidBodyState b) {
		this.center = new Vector3d(b.center);
		this.rot    = new Quaternion(b.rot);
	}

}

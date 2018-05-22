/**
 * Compute quantities related to the intersection between an ellipse and a plane
 * 
 * @author Giovanni Brandani
 *
 */
public class PlaneEllipsoidIntersec {

	/**
	 * Formula from:
	 * Klein, Peter Paul. "On the Ellipsoid and Plane Intersection Equation." Applied Mathematics 3.11 (2012).
	 *
	 * @param  k distance of the plane from the orifin of the interface
	 * @param  n the unit vector normal to the plane
	 * @param  l the axis of the ellipse
	 * @return the area of the ellipse intersection
	 */
	public static double area(double k, Vector3d n, Vector3d l) {
		double kt = Math.sqrt(n.a[0]*n.a[0]*l.a[0]*l.a[0] + n.a[1]*n.a[1]*l.a[1]*l.a[1] + n.a[2]*n.a[2]*l.a[2]*l.a[2]);
		double area = Math.PI * (1.-k*k/kt/kt) * l.a[0]*l.a[1]*l.a[2]/kt;
		if(area>0.) return area;
		else        return 0.;
	}

}

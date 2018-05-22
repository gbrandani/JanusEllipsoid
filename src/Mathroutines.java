/**
 * Mathroutines
 * 
 * @author Giovanni Brandani
 *
 */
public class Mathroutines {

	// Gaussian-distributed random numbers, mean = 0, sigma = 1
	public static double gaussrand() {
		double x1, x2, w;
		do {
			x1 = 2.0 * Math.random() - 1.0;
			x2 = 2.0 * Math.random() - 1.0;
			w = x1*x1 + x2*x2;
		} while (w >= 1.0);
		w = Math.sqrt((-2.0 * Math.log(w)) / w);
		x1 *= w;
		return x1;
	}

}

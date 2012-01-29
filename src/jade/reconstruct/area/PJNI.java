package jade.reconstruct.area;

public class PJNI {
	public native double [] matrixExp(double [] mat, int size);
	static {
		System.loadLibrary("matrixExp");
	}
}

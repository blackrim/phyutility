package jebl.math;


public class MatrixCalcException extends Exception{
	public MatrixCalcException() { super(); }
	public MatrixCalcException(String message) { super(message); }
	
	public static class NotSquareMatrixException extends MatrixCalcException {
		public NotSquareMatrixException() { super(); }
		public NotSquareMatrixException(String message) { super(message); }
	}
	public static class PositiveDefiniteException extends MatrixCalcException {
		public PositiveDefiniteException() { super(); }
		public PositiveDefiniteException(String message) { super(message); }
	}
}

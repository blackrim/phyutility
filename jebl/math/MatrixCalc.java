package jebl.math;



/**
 * @author Stephen A. Smith
 *
 */

public final class MatrixCalc {
	/**
	 * Cholesky factorization (aka Cholesky Decomposition)
	 * 
	 * This factorization can be used when square matrix is symmetric and positive definite.
	 * It is much faster than other methods where symmetry is ignored (LU Decomposition).
	 * 
	 * @param inMatrix square symmetric matrix to perform cholesky factorization
	 * @return resulting matrix
	 */
	public static double [][] choleskyFactor(double [][] inMatrix) 
	throws MatrixCalcException.NotSquareMatrixException,
	MatrixCalcException.PositiveDefiniteException{
		int j;
		if ( inMatrix.length != inMatrix[0].length ){
			throw new MatrixCalcException.NotSquareMatrixException("error in CholeskyFactor, not a square matrix");
		}
		//should be ok because it has to be a symmetrical square matrix	
		double [][] inMatrix_ent = inMatrix;
		for (int k=0; k<inMatrix.length; k++ )
		{	
			double sum = inMatrix_ent[k][k];
			double [] inMatrix_piv = inMatrix_ent[k];
			for (j=0; j<k; j++ )
			{
				double tmp = inMatrix_ent[k][j];
				sum -= tmp*tmp;
			}
			if ( sum <= 0.0 ){
				throw new MatrixCalcException.PositiveDefiniteException("error in CholeskyFactor, sum");
			}
			inMatrix_ent[k][k] = Math.sqrt(sum);
			for (int i=k+1; i<inMatrix.length; i++ ){
				sum = inMatrix_ent[i][k];
				inMatrix_piv = inMatrix_ent[k];
				double [] inMatrix_row = inMatrix_ent[i];
				for (j=0; j<k; j++ ){
					sum -= inMatrix_ent[i][j]*inMatrix_ent[k][j];
					sum -= (inMatrix_row[j])*(inMatrix_piv[j]);
				}
				inMatrix_ent[j][i] = inMatrix_ent[i][j] = sum/inMatrix_ent[k][k];
			}
		}
		return inMatrix;
	}
	
	/**
	 * Cholesky solve
	 * 
	 * Once the matrix is decomposed with the above routine, one can solve the triangular factor with backsubstitution.
	 * The forward (lowerSolve) and backward (upperSolve) are used for this.
	 * 
	 * @param matrix matrix to perform cholesky solve (probably used after factorization)
	 * @param vector vector to solve matrix * vector = return
	 * @return the resulting vector
	 */
	public static double [] choleskySolve(double [][] matrix, double [] vector)
	throws MatrixCalcException.NotSquareMatrixException{
		if ( matrix.length != matrix[0].length || matrix[0].length != vector.length ){
			throw new MatrixCalcException.NotSquareMatrixException("error in CholeskySolve, not a square matrix");
		}
		double [] retVector = new double [vector.length];
		retVector = lowerSolve(matrix, vector, 0.0);
		retVector = upperSolve(matrix, retVector, 0.0);
		return retVector;
	}
	
	/**
	 * lower Solve
	 * forward elimination with (optional) default diagonal value 
	 * 
	 * @param matrix the matrix to perform the forward elimination
	 * @param vector 
	 * @param diag the default diagonal value
	 * @return the resulting vector
	 */
	public static double [] lowerSolve(double [][] matrix, double [] vector, double diag){
		int	dimension, i;
		double [] out = new double [vector.length];
		if(matrix.length<matrix[0].length)
			dimension = matrix.length;
		else
			dimension=matrix[0].length;
		if ( vector.length < dimension ){
			System.out.println("error in LSolve, problem with vector length");
			System.exit(0);
		}
		if (out.length < dimension )
			out = new double [matrix.length];
		double [][] mat_ent = matrix;
		double [] vector_ent = vector;	
		double [] out_ent = out;
		for ( i=0; i<dimension; i++ ){
			if ( vector_ent[i] != 0.0 )
			    break;
			else
			    out_ent[i] = 0.0;
		}
		int i_lim = i;
		for (  ; i<dimension; i++ ){
			double sum = vector_ent[i];
			for ( int j=i_lim; j<i; j++ ){
				sum -= mat_ent[i][j]*out_ent[j];
			}
			if ( diag==0.0 ){
				if ( mat_ent[i][i]==0.0 ){
					System.out.println("error in LSolve, error in matrix");
					System.exit(0);
				}
				else
					out_ent[i] = sum/mat_ent[i][i];
			}
			else
				out_ent[i] = sum/diag;
		}
		return out;
	}
	
	/**
	 * 
	 * upperSolve 
	 * back substitution with optional over-riding diagonal
	 * 
	 * @param matrix the matrix to perform the back substitution
	 * @param vector 
	 * @param diag the default diagonal value
	 * @return the resulting vector
	 */
	public static double [] upperSolve(double [][] matrix, double[] vector, double diag){
		int	dimension;
		int	i, ilim=0;
		double [] out = new double [matrix.length];
		if(matrix.length < matrix[0].length)
			dimension = matrix.length;
		else
			dimension=matrix[0].length;
		if ( vector.length < dimension ){
			System.out.println("error in upperSolve");
			System.exit(1);
		}
		if ( out==null || out.length < dimension )
			out = new double [matrix.length];
		if (out.length < dimension )
			out = new double [matrix.length];
		double [][] matrix_ent = matrix;	
		double [] vector_ent = vector;	
		double [] out_ent = out;
		for ( i=dimension-1; i>=0; i-- ){
			if ( vector_ent[i] != 0.0 )
				break;
			else
				out_ent[i] = 0.0;
		}
		ilim = i;
		//use i start value from the above loop
		for (; i>=0; i-- ){
			double sum = vector_ent[i];
			for (int j=i+1; j<=ilim; j++ ){
				sum -= matrix_ent[i][j]*out_ent[j];
			}
			if ( diag==0.0 ){
				if ( matrix_ent[i][i]==0.0 ){
					System.out.println("error in USolve");
					System.exit(1);
				}
				else
					out_ent[i] = sum/matrix_ent[i][i];
			}
			else
				out_ent[i] = sum/diag;
		}
		return out;
	}
	
	/**
	 *  innerProdect
	 *  calculates inner product of two vectors from i down 
	 *  
	 *  @param vector1 the first vector
	 *  @param vector2 the second vector
	 *  @param x the starting int 
	 *  @return the inner product of the two vectors starting from x
	 */  
	public static double innerProduct(double [] vector1, double [] vector2, int x)throws IndexOutOfBoundsException{
		int	length;
		double	sum = 0;
		if(vector1.length<vector2.length)
			length = vector1.length;
		else
			length=vector2.length;
		if ( x > length ){
			throw new IndexOutOfBoundsException("innerProduct int x out of vector bounds");
		}
		for (int i=x; i<length; i++ )
			sum += vector1[i]*vector2[i];
		return sum;
	}
	
	/**
	 * takes a matrix and gets a column, then returns it as a vector
	 * @param matrix the matrix from which the column will be returned
	 * @param column the number of the column to return
	 * @return the column as a vector from the input matrix
	 */
	public static double [] getColumn(double [][] matrix, int column){
		double [] x = new double [matrix.length];
		for(int i=0;i<x.length;i++){
			x[i] = matrix [i][column];
		}
		return x;
	}
	
	/**
	 * takes a matrix and deletes a row
	 * @param matrix the matrix from which to delete the row
	 * @param row the number of the row to delete
	 * @return the matrix with deleted row
	 */
	public static double [][] deleteMatrixRow(double [][] matrix, int row){
		double [][] x = new double [matrix.length-1][matrix[0].length];
		int j=0;
		for(int i=0;i<matrix.length;i++){
			if(i!=row){
                System.arraycopy(matrix[i], 0, x[j], 0, matrix[i].length);
				j++;
			}
		}
		return x;
	}
	
	/**
	 * takes a matrix and deletes a column
	 * @param matrix the matrix from which to delete the column
	 * @param column the number of the column to delete
	 * @return the matrix with deleted column
	 */
	public static double [][] deleteMatrixColumn(double [][] matrix, int column){
		double [][] x = new double [matrix.length][matrix[0].length-1];
		for(int i=0;i<matrix.length;i++){
			int j=0;
			for(int k=0;k<matrix[i].length;k++){
				if(k!=column){
					x[i][j]=matrix[i][k];
					j++;
				}
			}
		}
		return x;
	}
	
	/**
	 * reverse a vector
	 * @param vector the vector to reverse
	 * @return the reversed vector
	 */
	public static double [] reverseVector(double [] vector){
		double [] x = new double [vector.length];
		int j=0;
		for(int i=vector.length-1;i>=0;i--){
			x[i]=vector[j];
			j++;
		}
		return x;
	}
	
	/**
	 * reverse a matrix
	 * @param matrix the matrix to reverse
	 * @return the reversed matrix
	 */
	public static double [][] reverseMatrix(double [][] matrix){
		double [][] x = new double [matrix.length][matrix[0].length];
		int j=0;
		for(int i=matrix.length-1;i>=0;i--){
			int k=0;
			for(int h=matrix[0].length-1;h>=0;h--){
				x[j][k]=matrix[i][h];
				k++;
			}
			j++;
		}
		return x;
	}
	
	/**
	 * sum a vector
	 * @param vector the input vector
	 * @return the sum of the vector
	 */
	public static double sumVector(double [] vector){
		double sum = 0.0;
        for (double aVector : vector) sum += aVector;
		return sum;
			
	}
	 
	/**
	 * copy one matrix into another
	 * @param matrix the matrix to copy
	 * @return the copied matrix
	 */
	 public static double [][] copyMatrix(double [][] matrix){
		double [][] x = new double [matrix.length][matrix[0].length];
		for(int i=0;i<x.length;i++){
			for(int j=0;j<x[i].length;j++){
				x[i][j]=matrix[i][j];
			}
		}
		return x;
	 }
}

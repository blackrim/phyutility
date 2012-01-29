package jade.reconstruct.area;

import java.text.*;
//import org.netlib.lapack.*;
//import org.netlib.util.intW;

//import Jama.*;


public class PJAMA {
	public PJAMA(Q qin) {
		q=qin;
		//me = new MatrixExponential(q.getQ().length);
		//me.updateByRelativeRates(q.getQ());
	}

	public double getRateChangeProbability(int row, int column){
		//return me.getTransitionProbability(row, column);
		return P[row][column];
	}

	public void setBL(double b){
		 //me.setDistance(b);
		
		PJNI pjni = new PJNI();
		double [] tq = transform2d(q.getQ(), b);
		double [] tq2 = pjni.matrixExp(tq, q.getQ().length);
		P = transform1d(tq2);
	}
	
	private double [] transform2d(double [][] inm, double b){
		double [] ret = new double [inm.length*inm.length];
		int x = 0;
		for(int i=0;i<inm.length;i++){
			for(int j=0;j<inm.length;j++){
				ret[x] = inm[i][j]*b;
				x++;
			}
		}
		return ret;
	}
	
	private double [][] transform1d(double [] inm){
		double [][] ret = new double [q.getQ().length][q.getQ().length];
		int x = 0;
		for(int i=0;i<q.getQ().length;i++){
			for(int j=0;j<q.getQ().length;j++){
				ret[i][j] = inm[x];
				x++;
			}
		}
		return ret;
	}
	
	public void setBLOLD(double b){/*
		double [][]  qm = q.getQ();
		double [] eigva= new double [qm.length];
		double [] im_eigva =  new double [qm.length];
		double [][] l_eivec = new double [qm.length][qm.length];
		double [][] r_eivec=new double [qm.length][qm.length];
		intW info = new intW(0);
		double [] work = new double [10*qm.length];
		DGEEV.DGEEV("V","V",qm.length, qm, eigva, im_eigva, 
				l_eivec, r_eivec, work, 10*qm.length, info);
		System.out.println(jade.math.Utils.printDVec(eigva));
		System.out.println(jade.math.Utils.printDMat(r_eivec));
		for(int i=0;i<eigva.length;i++){eigva[i] = Math.exp(eigva[i]*b);}		
		double [][] exp_D = Matrix.identity(q.getQ().length, q.getQ().length).getArrayCopy();
		for(int i=0;i<exp_D.length;i++){
			for(int j=0;j<exp_D[i].length;j++){
				if(j==i)
					exp_D[i][j] =eigva[i];
				else
					exp_D[i][j] = 0.0;
			}
		}
		System.out.println(jade.math.Utils.printDMat(exp_D));
		double [][] C_inv = new Matrix(r_eivec).inverse().getArrayCopy();
		double [][] P = new double [r_eivec.length][r_eivec.length];
		for(int i=0;i<P.length;i++){
			for(int j=0;j<P[i].length;j++){
				double s = r_eivec[i][j] * exp_D[j][j];
				P[i][j] = s;
			}
		}
		System.out.println(jade.math.Utils.printDMat(P));
		System.out.println(jade.math.Utils.printDMat(C_inv));*/
	    //P = dot(dot(C, exp_D), C_inv)
	    //Psum = scipy.sum(P,1) # sum across rows, and
	    //P = P/Psum # divide by sum so that all elements are between 0 and 1
	    //return P
	}

	//private double [][]arr;
	//private double bl;
	private Q q;
	private MatrixExponential me;
	private double [][] P;
	//private Matrix P;
}

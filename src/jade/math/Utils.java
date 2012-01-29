/*
 * Utils.java
 *
 * Created on June 27, 2005, 2:02 PM
 * 
 *  This is just a reserve for utilities
 */

package jade.math;

import java.io.*;
import java.util.*;


/**
 * 
 * @author stephensmith
 */
public class Utils {

	/** Creates a new instance of Utils */
	// public Utils() {}
	public static double conditional_exp(double t, double lambd) {
		Random r = new Random();
		double U = r.nextDouble();
		double p = -(1.0 / lambd)
		        * Math.log(1.0 - U * (1.0 - Math.exp(-lambd * t)));
		return p;
	}

	/*
	 * public static Tree calibrate_tree(Tree intree, double cal){ Tree tempT =
	 * intree.getCopy(); double len2tip = 0.0; boolean t = true; Node tempN =
	 * tempT.getRoot(); len2tip = tempN.getNodeHeight();
	 * 
	 * double scale = cal/len2tip; System.out.println(scale); for(int i=0;i<tempT.getInternalNodeCount();i++){
	 * tempN=tempT.getInternalNode(i); double value = tempN.getBranchLength() *
	 * scale; if(tempN != tempT.getRoot()){ tempN.setBranchLength(value); }else
	 * tempN.setBranchLength(0.0); } for(int i=0;i<tempT.getExternalNodeCount();i++){
	 * tempN=tempT.getExternalNode(i); double value = tempN.getBranchLength() *
	 * scale; tempN.setBranchLength(value); } return tempT; } public static void
	 * printTree(Tree intree){ TreeUtils tu = new TreeUtils(); PrintWriter out =
	 * new PrintWriter(System.out); tu.report(intree, out); out.flush(); }
	 */
	// add to array functions
	public static int[][][] combineITER(int[][][] inA, int[][][] inB) {
		int[][][] retA = new int[inA.length + inB.length][][];
		int x = 0;
		for (int i = 0; i < inA.length; i++) {
			retA[x] = inA[i];
			x++;
		}
		for (int i = 0; i < inB.length; i++) {
			retA[x] = inB[i];
			x++;
		}
		return retA;
	}

	public static int[][][] addToArray(int[][][] inA, int[] leftin,
	        int[] rightin) {
		int[][][] tempA = new int[inA.length + 1][2][];
		for (int i = 0; i < inA.length; i++) {
			tempA[i] = inA[i];
		}
		tempA[inA.length][0] = leftin;
		tempA[inA.length][1] = rightin;
		inA = tempA;
		return inA;
	}

	public static String[] addToArray(String[] inArray, String inelement) {
		String[] tempA = new String[inArray.length + 1];
		for (int i = 0; i < inArray.length; i++) {
			tempA[i] = inArray[i];
		}
		tempA[inArray.length] = inelement;
		inArray = tempA;
		return inArray;
	}

	public static int[] addToArray(int[] inArray, int inelement) {
		int[] tempA = new int[inArray.length + 1];
		for (int i = 0; i < inArray.length; i++) {
			tempA[i] = inArray[i];
		}
		tempA[inArray.length] = inelement;
		inArray = tempA;
		return inArray;
	}

	public static double[] addToArray(double[] inArray, double inelement) {
		double[] tempA = new double[inArray.length + 1];
		for (int i = 0; i < inArray.length; i++) {
			tempA[i] = inArray[i];
		}
		tempA[inArray.length] = inelement;
		inArray = tempA;
		return inArray;
	}

	public static double[][] addToArray(double[][] inArray, double[] inelement) {
		double[][] tempA = new double[inArray.length + 1][];
		for (int i = 0; i < inArray.length; i++) {
			tempA[i] = inArray[i];
		}
		tempA[inArray.length] = inelement;
		inArray = tempA;
		return inArray;
	}

	public static int[][][] addToArray(int[][][] inArray, int[][] inelement) {
		int[][][] tempA = new int[inArray.length + 1][][];
		for (int i = 0; i < inArray.length; i++) {
			tempA[i] = inArray[i];
		}
		tempA[inArray.length] = inelement;
		inArray = tempA;
		return inArray;
	}

	public static int[][] addToArray(int[][] inArray, int[] inelement) {
		int[][] tempA = new int[inArray.length + 1][];
		for (int i = 0; i < inArray.length; i++) {
			tempA[i] = inArray[i];
		}
		tempA[inArray.length] = inelement;
		inArray = tempA;
		return inArray;
	}

	// linear algebra functions
	public static double[] divide(double[] ina, int num) {
		for (int i = 0; i < ina.length; i++) {
			ina[i] = ina[i] / num;
		}
		return ina;
	}
	public static double[][] multiply_mat_dou(double[][] a, double [][] b) {
		double [][] ret = new double [a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				ret [i][j] = a[i][j] * b[i][j];
			}
		}
		return ret;
	}
	public static int sum(int[] ina) {
		int sum = 0;
		for (int i = 0; i < ina.length; i++) {
			sum = sum + ina[i];
		}
		return sum;
	}

	public static double sum(double[] inA) {
		double tempD = 0;
		for (int i = 0; i < inA.length; i++) {
			tempD = tempD + inA[i];
			// System.out.println(inA[i]);
		}
		return tempD;
	}

	public static int[][][] iter_splitranges(int nareas) {
		int[][][] retA = new int[0][][];
		int[][] distributions = NChooseM.iterate_all_bv2small(nareas);
		for (int i = 0; i < distributions.length; i++) {
			retA = Utils.combineITER(retA, iter_splitranges(distributions[i]));
		}
		// for(int i=0;i<retA.length;i++){
		// for(int j=0;j<retA[i].length;j++){
		// for(int k=0;k<retA[i][j].length;k++){
		// System.out.print(retA[i][j][k]);
		// }System.out.print(" ");
		// }System.out.println();
		// }
		return retA;
	}

	public static int[][][] iter_splitranges(int[] rng) {
		int[][][] retA = new int[0][0][0];
		int numM = 0;
		for (int i = 0; i < rng.length; i++) {
			if (rng[i] == 1) {
				numM++;
			}
		}
		retA = new int[numM][2][];
		if (numM == 1) {
			retA[0][0] = rng;
			retA[0][1] = rng;
			// added for dispersal
			// int [][][] retB = iter_splitranges_dispersal(rng);
			// retA = Utils.combineITER(retA, retB);
			// end added
		} else {
			int[][][] retB = iter_splitranges_allo(rng);
			int[][][] retC = iter_splitranges_sym(rng);
			retA = Utils.combineITER(retB, retC);
			// added for dispersal
			// int [][][] retD = iter_splitranges_dispersal(rng);
			// retA = Utils.combineITER(retA, retD);
			// end added
		}
		return retA;
	}

	private static int[][][] iter_splitranges_allo(int[] rng) {
		int[][][] retA = new int[0][][];
		int numofones = 0;
		for (int i = 0; i < rng.length; i++) {
			if (rng[i] == 1)
				numofones++;
		}
		if (numofones == 2) {
			for (int i = 0; i < rng.length; i++) {
				int[] tempLA = new int[rng.length];
				int[] tempRA = new int[rng.length];
				if (rng[i] == 1) {
					for (int j = 0; j < rng.length; j++) {
						tempLA[j] = 0;
						tempRA[j] = rng[j];
						if (j == i) {
							tempLA[i] = 1;
							tempRA[i] = 0;
						}
					}
					retA = Utils.addToArray(retA, tempLA, tempRA);
				}
			}
		} else {
			for (int i = 0; i < rng.length; i++) {
				int[] tempLA = new int[rng.length];
				int[] tempRA = new int[rng.length];
				if (rng[i] == 1) {
					for (int j = 0; j < rng.length; j++) {
						tempLA[j] = 0;
						tempRA[j] = rng[j];
						if (j == i) {
							tempLA[i] = 1;
							tempRA[i] = 0;
						}
					}
					retA = Utils.addToArray(retA, tempLA, tempRA);
					retA = Utils.addToArray(retA, tempRA, tempLA);
				}
			}
		}
		return retA;
	}

	private static int[][][] iter_splitranges_sym(int[] rng) {
		int[][][] retA = new int[0][][];
		for (int i = 0; i < rng.length; i++) {
			int[] tempLA = new int[rng.length];
			int[] tempRA = new int[rng.length];
			if (rng[i] == 1) {
				for (int j = 0; j < rng.length; j++) {
					tempLA[j] = 0;
					tempRA[j] = rng[j];
					if (j == i) {
						tempLA[i] = 1;
						tempRA[i] = 1;
					}
				}
				retA = Utils.addToArray(retA, tempLA, tempRA);
				retA = Utils.addToArray(retA, tempRA, tempLA);
			}
		}
		return retA;
	}

	private static int[][][] iter_splitranges_dispersal(int[] rng) {
		int numofzeros = 0;
		for (int i = 0; i < rng.length; i++) {
			if (rng[i] == 0)
				numofzeros++;
		}
		int[][][] retA = new int[0][][];
		if (numofzeros > 0) {
			for (int i = 0; i < rng.length; i++) {
				int[] tempLA = new int[rng.length];
				int[] tempRA = new int[rng.length];
				if (rng[i] == 0) {
					tempRA = rng;
					for (int j = 0; j < rng.length; j++) {
						tempLA[j] = 0;
						if (j == i) {
							tempLA[j] = 1;
						}
					}
					retA = Utils.addToArray(retA, tempLA, tempRA);
					retA = Utils.addToArray(retA, tempRA, tempLA);
				}
			}
		}
		return retA;
	}

	/*
	 * this will return a vector of two with the first result being whether
	 * there is a dispersal from one distribution to the second and the second
	 * result being whether there is an extinction a null result means that
	 * there is too much a [0,0] results means that it is the same distribution
	 * 
	 */
	public static int[] getDistanceBtwDistributions(int[] dist1, int[] dist2) {
		int[] retV = new int[2];// 0 == dispersals, 1 == extinctions
		for (int i = 0; i < 2; i++) {
			retV[i] = 0;
		}
		for (int i = 0; i < dist1.length; i++) {
			if (dist1[i] == 0 && dist2[i] == 1) {
				retV[0]++;
			} else if (dist1[i] == 1 && dist2[i] == 0) {
				retV[1]++;
			}
		}
		if (retV[0] > 1 || retV[1] > 1)
			return null;
		if (retV[0] > 0 && retV[1] > 0)
			return null;
		return retV;
	}

	public static String printCombIVec(int [] vec1, int [] vec2){
		String retS = "";
		for (int i = 0; i < vec1.length; i++) {
			if(vec1[i] ==1  || vec2[i]==1)
				retS += String.valueOf(1);
			else
				retS += String.valueOf(0);
		}
		return retS;
	}
	
	public static String printIVec(int[] vec) {
		String retS = "";
		for (int i = 0; i < vec.length; i++) {
			retS += String.valueOf(vec[i]);
		}
		return retS;
	}
	
	public static String printDVec(double[] vec) {
		String retS = "";
		for (int i = 0; i < vec.length; i++) {
			retS += String.valueOf(vec[i])+"\t";
		}
		return retS;
	}
	
	public static String printDMat(double [][] m) {
		String retS = "";
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				retS += String.valueOf(m[i][j]+"\t");
			}retS += "\n";
		}
		return retS;
	}
	
	/*
	 * logical xor
	 * returns an array the length of the input arrays
	 * which has true or false corresonding to NON-matching 
	 * parts
	 */
	public static int [] logical_xor_int(int [] a, int [] b ){
		assert a.length == b.length;
		int [] ret = new int [a.length];
		for(int i=0;i<a.length;i++){
			if(a[i]==b[i])
				ret[i] = 0;//false
			else
				ret[i] = 1;//true
		}
		return ret;
	}
	
	/*
	 * returns an arraylist of the position nonzero elements
	 */
	public static ArrayList<Integer> nonzero_int(int [] a){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for(int i=0;i<a.length;i++){
			if(a[i] != 0)
				ret.add(i);
		}
		return ret;
	}
}

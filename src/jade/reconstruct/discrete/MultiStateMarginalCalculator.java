package jade.reconstruct.discrete;

import java.util.*;
import java.text.*;
import jade.tree.*;
import jade.data.*;


public class MultiStateMarginalCalculator {
	private HashMap<Node, double[]> ltgn;

	private HashMap<Node, double[]> conditionals;

	private P p;

	private AbstractAlignment aln;

	private Tree tree;

	private double rate;

	NumberFormat df = new DecimalFormat("0.00000");

	private boolean verbose;

	private int size;
	
	/** Creates a new instance of MultiStateMarginalCalculator */
	public MultiStateMarginalCalculator(AbstractAlignment aln, Tree tree, int size,
	        double rate, boolean verbose) {
		this.aln = aln;
		this.tree = tree;
		this.rate = rate;
		this.p = new P(size, rate, rate);
		this.size = size;
		this.verbose = verbose;
		// P.getP(0.1);
		// P.printP();
		conditionals = new HashMap<Node, double[]>();
		ltgn = new HashMap<Node, double[]>();
	}

	public void setRate(double rate) {
		this.rate = rate;
		this.p = new P(size, rate, rate);
		conditionals = new HashMap<Node, double[]>();
		ltgn = new HashMap<Node, double[]>();
	}

	public double calculate() {
		postOrder1(tree.getRoot());
		preOrder(tree.getRoot());
		double value = 0;
		for (int i = 0; i < size; i++) {
			value = value
			        + (conditionals.get(tree.getRoot())[i] * (1.0 / size));
		}

		// for(int i=0;i<tree.getInternalNodeCount();i++){
		// double [] tv = ltgn.get(tree.getInternalNode(i));
		// double [] prop = new double [2];
		// prop[0] = tv[0]/(tv[0]+tv[1]);
		// prop[1] = tv[1]/(tv[0]+tv[1]);
		// tree.getInternalNode(i).setIdentifier(new
		// Identifier(df.format(prop[0])+"_"+df.format(prop[1])));
		// if(tree.getInternalNode(i)==tree.getRoot()&&verbose==true){
		// System.out.println("root value:
		// "+df.format((conditionals.get(tree.getRoot())[0]*0.5)/value)+"_"+df.format((conditionals.get(tree.getRoot())[1]*0.5)/value));
		// }
		// }
		// if(verbose == true){
		// TreeUtils tu = new TreeUtils();
		// PrintWriter pw = new PrintWriter(System.out);
		// tu.reportshort(tree,pw);
		// pw.flush();
		// System.out.println("likelihood at root
		// "+Math.log((conditionals.get(tree.getRoot())[0]*0.5)+(conditionals.get(tree.getRoot())[1]*0.5)));
		// }
		System.out.println(-Math.log(value));
		return -Math.log(value);
		// System.out.println("likelihood at root
		// "+((conditionals.get(tree.getRoot())[0]*0.5)/value));
		// System.out.println("likelihood at root
		// "+((conditionals.get(tree.getRoot())[1]*0.5)/value));
	}

	private void postOrder1(Node node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			postOrder1(node.getChild(i));
		}
		if (node.isExternal()) {
			String name = node.getName();
			int alnn = 0;
			for (int i = 0; i < aln.getSequenceCount(); i++) {
				if (aln.getIdentifier(i).compareTo(name) == 0)
					alnn = i;
			}
			int x = Integer.valueOf(String.valueOf(aln.getData(alnn, 0)))
			        .intValue();
			double[] ar = new double[size];
			ar[x] = 1.0;
			for (int i = 0; i < size; i++) {
				if (i != x)
					ar[i] = 0.0;
			}
			conditionals.put(node, ar);
		} else {
			double[] ar = new double[size];
			for (int k = 0; k < size; k++) {
				double tempcon = 1;
				for (int i = 0; i < node.getChildCount(); i++) {
					double[][] p_ar = p.getP(node.getChild(i).getBL());
					double[] tar = conditionals.get(node.getChild(i));
					double temptempcon = 0;
					for (int j = 0; j < size; j++) {
						temptempcon = temptempcon + (tar[j] * p_ar[k][j]);
					}
					tempcon = tempcon * temptempcon;
				}
				ar[k] = tempcon;
			}
			conditionals.put(node, ar);
		}
	}

	private void preOrder(Node node) {
		if ((tree.getRoot() != node) && !node.isExternal()) {
			double[][] p_ar = new double[0][0];
			// conditional likelihood excluding X
			double[] clex = calcExclude(node.getParent(), node);
			// L(T|X)
			p_ar = p.getP(node.getBL());
			double[] x = new double[size];
			for (int i = 0; i < size; i++) {
				x[i] = conditionals.get(node)[i];
				double t = 0;
				for (int j = 0; j < size; j++) {
					t = t + (p_ar[j][i] * clex[j]);
				}
				x[i] = x[i] * t;
			}
			ltgn.put(node, x);
		}
		if ((tree.getRoot() != node)) {
			double[] x = new double[size];
			for (int i = 0; i < size; i++) {
				x[i] = 0.0;
			}
			ltgn.put(node, x);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			preOrder(node.getChild(i));
		}
	}

	private double[] calcExclude(Node node, Node excl) {
		double[][] p_ar = new double[0][0];
		double[] clex = new double[size];
		for (int i = 0; i < size; i++) {
			clex[i] = 1.0;
		}
		// conditional likelihood excluding X
		for (int i = 0; i < node.getChildCount(); i++) {
			if (node.getChild(i) != excl) {
				p_ar = p.getP(node.getChild(i).getBL());
				for (int j = 0; j < size; j++) {
					double t = 0;
					for (int k = 0; k < size; k++) {
						t = t
						        + (conditionals.get(node.getChild(i))[k] * p_ar[j][k]);
					}
					clex[j] = clex[j] * t;
				}

			}
		}
		Node mother = node.getParent();
		if (mother != null) {
			for (int i = 0; i < mother.getChildCount(); i++) {
				if (mother.getChild(i) == node) {
					p_ar = p.getP(mother.getChild(i).getBL());
				}
			}
			double[] clExclude = calcExclude(mother, node);
			for (int j = 0; j < size; j++) {
				double t = 0;
				for (int k = 0; k < size; k++) {
					t = t + (clExclude[k] * p_ar[j][k]);
				}
				clex[j] = clex[j] * t;
			}
		}

		double[] d = clex;
		return d;
	}

	public static void main(String[] args) {
		//new MultiStateMarginalCalculator(4);
	}
}

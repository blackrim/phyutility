package jade.tree;

import java.io.*;
import java.text.*;
import java.util.*;

public class TreePrinter {
	public TreePrinter() {
	}

	public void reportASCII(Tree tree, PrintWriter out) {
		printASCII(tree, out);
		out.println();
	}

	private double proportion;

	private int minLength;

	private boolean[] umbrella;

	private int[] position;

	private int numExternalNodes;

	private int numInternalNodes;

	private int numBranches;

	// private NumberFormat nf;

	// Print picture of current tree in ASCII
	private void printASCII(Tree tree, PrintWriter out) {
		tree.processRoot();

		numExternalNodes = tree.getExternalNodeCount();
		numInternalNodes = tree.getInternalNodeCount();
		numBranches = numInternalNodes + numExternalNodes - 1;

		umbrella = new boolean[numExternalNodes];
		position = new int[numExternalNodes];

		minLength = (Integer.toString(numBranches)).length() + 1;

		int MAXCOLUMN = 40;
		Node root = tree.getRoot();
		if (root.getDistanceToTip() == 0.0) {
			TreeUtils.setDistanceToTips(root);
		}
		proportion = (double) MAXCOLUMN / root.getDistanceToTip();
		// proportion = (double) MAXCOLUMN/0.1;

		for (int n = 0; n < numExternalNodes; n++) {
			umbrella[n] = false;
		}

		position[0] = 1;
		for (int i = root.getChildCount() - 1; i > -1; i--) {
			printNodeInASCII(out, root.getChild(i), 1, i, root.getChildCount());
			if (i != 0) {
				putCharAtLevel(out, 0, '|');
				out.println();
			}
		}
	}

	private void printNodeInASCII(PrintWriter out, Node node, int level, int m,
	        int maxm) {
		position[level] = (int) (node.getBL() * proportion);

		if (position[level] < minLength) {
			position[level] = minLength;
		}

		if (node.isExternal()) // external branch
		{
			if (m == maxm - 1) {
				umbrella[level - 1] = true;
			}

			printlnNodeWithNumberAndLabel(out, node, level);

			if (m == 0) {
				umbrella[level - 1] = false;
			}
		} else // internal branch
		{
			for (int n = node.getChildCount() - 1; n > -1; n--) {
				printNodeInASCII(out, node.getChild(n), level + 1, n, node
				        .getChildCount());

				if (m == maxm - 1 && n == node.getChildCount() / 2) {
					umbrella[level - 1] = true;
				}

				if (n != 0) {
					if (n == node.getChildCount() / 2) {
						printlnNodeWithNumberAndLabel(out, node, level);
					} else {
						for (int i = 0; i < level + 1; i++) {
							if (umbrella[i]) {
								putCharAtLevel(out, i, '|');
							} else {
								putCharAtLevel(out, i, ' ');
							}
						}
						out.println();
					}
				}

				if (m == 0 && n == node.getChildCount() / 2) {
					umbrella[level - 1] = false;
				}
			}
		}
	}

	private void printlnNodeWithNumberAndLabel(PrintWriter out, Node node,
	        int level) {
		for (int i = 0; i < level - 1; i++) {
			if (umbrella[i]) {
				putCharAtLevel(out, i, '|');
			} else {
				putCharAtLevel(out, i, ' ');
			}
		}

		putCharAtLevel(out, level - 1, '+');

		int branchNumber;
		if (node.isExternal()) {
			branchNumber = node.getNumber() + 1;
		} else {
			branchNumber = node.getNumber() + 1 + numExternalNodes;
		}

		String numberAsString = Integer.toString(branchNumber);

		int numDashs = position[level] - numberAsString.length();
		for (int i = 0; i < numDashs; i++) {
			out.print('-');
		}
		out.print(numberAsString);

		if (node.isExternal()) {
			out.println(" " + node.getName());
		} else {
			if (!node.getName().equals("")) {
				out.print("(" + node.getName() + ")");
			}
			out.println();
		}
	}

	private void putCharAtLevel(PrintWriter out, int level, char c) {
		int n = position[level] - 1;
		for (int i = 0; i < n; i++) {
			out.print(' ');
		}
		out.print(c);
	}
/*
	private void displayIntegerWhite(PrintWriter out, int maxNum) {
		int lenMaxNum = Integer.toString(maxNum).length();

		multiplePrint(out, ' ', lenMaxNum);
	}

	private int displayDecimalASCII(PrintWriter out, double number, int width) {
		String s = getDecimalStringASCII(number, width);

		out.print(s);

		return s.length();
	}

	private void displayLabel(PrintWriter out, String label, int width) {
		int len = label.length();

		if (len == width) {
			// Print as is
			out.print(label);
		} else if (len < width) {
			// fill rest with spaces
			out.print(label);
			multiplePrint(out, ' ', width - len);
		} else {
			// Print first width characters
			for (int i = 0; i < width; i++) {
				out.print(label.charAt(i));
			}
		}
	}
*/
	/**
	 * print integer, aligned to a reference number, (introducing space at the
	 * left side)
	 * 
	 * @param out
	 *            output stream
	 * @param num
	 *            number to be printed
	 * @param maxNum
	 *            reference number
	 */
	/*
	private void displayInteger(PrintWriter out, int num, int maxNum) {
		int lenNum = Integer.toString(num).length();
		int lenMaxNum = Integer.toString(maxNum).length();

		if (lenNum < lenMaxNum) {
			multiplePrint(out, ' ', lenMaxNum - lenNum);
		}
		out.print(num);
	}
	
	private void multiplePrint(PrintWriter out, char c, int num) {
		for (int i = 0; i < num; i++) {
			out.print(c);
		}
	}

	private synchronized String getDecimalStringASCII(double number, int width) {
		nf.setMinimumFractionDigits(width);
		nf.setMaximumFractionDigits(width);

		return nf.format(number);
	}
	*/
	/*
	 * 
	 * 
	 * 
	 * 
	 * printing newick format
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	private Tree inTree;

	private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

	private String nhString;

	public String printNH(Tree intree) {
		inTree = intree;
		nhString = "";
		PrintStream ps = new PrintStream(System.out);
		printNH(new PrintWriter(ps), inTree.getRoot(), true, true, 0, false);
		return nhString;
	}
	/*
	private void printNH(PrintWriter out, Node node, boolean printLengths,
	        boolean printInternalLabels) {
		printNH(out, node, printLengths, printInternalLabels, 0, true);
	}*/

	private int printNH(PrintWriter out, Node node, boolean printLengths,
	        boolean printInternalLabels, int column, boolean breakLines) {
		if (breakLines)
			column = breakLine(out, column);
		if (!node.isExternal()) {
			out.print("(");
			nhString = nhString + "(";
			column++;

			for (int i = 0; i < node.getChildCount(); i++) {
				if (i != 0) {
					out.print(",");
					nhString = nhString + ",";
					column++;
				}

				column = printNH(out, node.getChild(i), printLengths,
				        printInternalLabels, column, breakLines);
			}

			out.print(")");
			nhString = nhString + ")";
			column++;
		}
		if (!node.isTheRoot()) {
			if (node.isExternal() || printInternalLabels) {
				if (breakLines)
					column = breakLine(out, column);

				String id = node.getName();
				out.print(id);
				nhString = nhString + id;
				column += id.length();
			}
			if (printLengths) {
				out.print(":");
				nhString = nhString + ":";
				column++;
				if (breakLines)
					column = breakLine(out, column);
				column += displayDecimalNH(out, node.getBL(), 8);
			}
		}
		return column;
	}

	private int breakLine(PrintWriter out, int column) {
		if (column > 70) {
			out.println();
			nhString = nhString + "\n";
			column = 0;
		}

		return column;
	}

	private int displayDecimalNH(PrintWriter out, double number, int width) {
		String s = getDecimalStringNH(number, width);
		out.print(s);
		nhString = nhString + s;
		return s.length();
	}

	private synchronized String getDecimalStringNH(double number, int width) {
		nf.setMinimumFractionDigits(width);
		nf.setMaximumFractionDigits(width);
		return nf.format(number);
	}
}

/**
 * 
 */
package jade.tree;

/**
 * @author smitty
 *
 */
public class TreeReader {
	private String treeString;

	private Tree tree;

	/*
	 * constructor
	 */
	public TreeReader() {
	}

	/*
	 * read from the newick format
	 */
	public TreeReader(String tree) {
		treeString = tree;
	}

	public void setTree(String tree) {
		treeString = tree;
	}
	
	public Tree getTree(){return tree;}
	
	public Tree readTree() {
		tree = new Tree();
		String pb = treeString;
		int x = 0;
		char nextChar = pb.charAt(x);
		boolean start = true;
		boolean keepGoing = true;
		Node currNode = new Node();
		while (keepGoing == true) {
			if (nextChar == '(') {
				if (start == true) {
					Node root = new Node();
					tree.setRoot(root);
					currNode = root;
					start = false;
				} else {
					Node newNode = new Node(currNode);
					currNode.addChild(newNode);
					currNode = newNode;
				}
			} else if (nextChar == ',') {
				currNode = currNode.getParent();
			} else if (nextChar == ')') {
				currNode = currNode.getParent();
				x++;
				nextChar = pb.charAt(x);
				String nam = "";
				boolean goingName = true;
				if (nextChar == ',' || nextChar == ')' || nextChar == ':'
				        || nextChar == ';'|| nextChar == '[') {
					goingName = false;
				}
				while (goingName == true) {
					nam = nam + nextChar;
					x++;
					nextChar = pb.charAt(x);
					if (nextChar == ',' || nextChar == ')' || nextChar == ':'
					        || nextChar == ';'|| nextChar == '[') {
						goingName = false;
						break;
					}
				}// work on edge
				currNode.setName(nam);
				// currNode.getEdge(currNode.getParent()).setLength(Double.parseDouble(edgeL));
				// System.out.println(nam);
				// pb.unread(nextChar);
				x--;
				pb.charAt(x);
			} else if (nextChar == ';') {
				keepGoing = false;
			} else if (nextChar == ':') {
				x++;
				nextChar = pb.charAt(x);
				String edgeL = "";
				boolean goingName = true;
				while (goingName == true) {
					edgeL = edgeL + nextChar;
					x++;
					nextChar = pb.charAt(x);
					if (nextChar == ',' || nextChar == ')' || nextChar == ':'
					        || nextChar == ';'|| nextChar == '[') {
						goingName = false;
						break;
					}
				}// work on edge
				currNode.setBL(Double.parseDouble(edgeL));
				// currNode.getEdge(currNode.getParent()).setLength(Double.parseDouble(edgeL));
				// System.out.println(Double.parseDouble(edgeL));
				// pb.unread(nextChar);
				x--;
				pb.charAt(x);
			}
			//note
			else if (nextChar == '[') {
				x++;
				nextChar = pb.charAt(x);
				String note = "";
				boolean goingNote = true;
				while (goingNote == true) {
					note = note + nextChar;
					x++;
					nextChar = pb.charAt(x);
					if (nextChar == ']' ) {
						goingNote = false;
						break;
					}
				}// work on note
				//currNode.setBL(Double.parseDouble(edgeL));
				//x--;
				pb.charAt(x);
			} else if (nextChar == ' ') {

			}
			// external named node
			else {
				Node newNode = new Node(currNode);
				currNode.addChild(newNode);
				currNode = newNode;
				String nodeName = "";
				boolean goingName = true;
				while (goingName == true) {
					nodeName = nodeName + nextChar;
					x++;
					nextChar = pb.charAt(x);
					if (nextChar == ',' || nextChar == ')' || nextChar == ':' || nextChar == '[') {
						goingName = false;
						break;
					}
				}
				newNode.setName(nodeName);
				// System.out.println(nodeName);
				// pb.unread(nextChar);
				x--;
				pb.charAt(x);
			}
			if (x < pb.length() - 1)//added
				x++;
			//
			nextChar = pb.charAt(x);
			//System.out.println(nextChar);
		}
		tree.processRoot();
		return tree;
	}
}

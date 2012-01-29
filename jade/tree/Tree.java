package jade.tree;

import java.util.*;

public class Tree {
	/*
	 * private 
	 */
	private Node root;

	private ArrayList<Node> nodes;

	private ArrayList<Node> internalNodes;

	private ArrayList<Node> externalNodes;

	private ArrayList<TreeObject> assoc;

	private int internalNodeCount;

	private int externalNodeCount;

	/*
	 * constructors
	 */
	public Tree() {
		root = null;
		assoc = new ArrayList<TreeObject>();
		processRoot();
	}

	public Tree(Node root) {
		this.root = root;
		assoc = new ArrayList<TreeObject>();
		processRoot();
	}

	public void processRoot() {
		nodes = new ArrayList<Node>();
		internalNodes = new ArrayList<Node>();
		externalNodes = new ArrayList<Node>();
		internalNodeCount = 0;
		externalNodeCount = 0;
		if (root == null)
			return;
		postOrderProcessRoot(root);
	}

	public void addExternalNode(Node tn) {
		externalNodes.add(tn);
		externalNodeCount = externalNodes.size();
		nodes.add(tn);
	}

	public void addInternalNode(Node tn) {
		internalNodes.add(tn);
		internalNodeCount = internalNodes.size();
		//to nodes
		nodes.add(tn);
	}

	public void addExternalNode(Node tn, int num) {
		externalNodes.add(tn);
		externalNodeCount = externalNodes.size();
		//to nodes
		nodes.add(tn);
		tn.setNumber(num);
	}

	public void addInternalNode(Node tn, int num) {
		internalNodes.add(tn);
		internalNodeCount = internalNodes.size();
		nodes.add(tn);
		tn.setNumber(num);
	}

	public Node getExternalNode(int num) {
		return externalNodes.get(num);
	}

	public Node getExternalNode(String name) {
		Node retNode = null;
		Iterator go = externalNodes.iterator();
		while (go.hasNext()) {
			Node ne = (Node) go.next();
			if (ne.getName().compareTo(name) == 0) {
				retNode = ne;
				break;
			}
		}
		return retNode;
	}

	public Node getInternalNode(int num) {
		return internalNodes.get(num);
	}

	public Node getInternalNode(String name) {
		Node retNode = null;
		Iterator go = internalNodes.iterator();
		while (go.hasNext()) {
			Node ne = (Node) go.next();
			if (ne.getName().compareTo(name) == 0) {
				retNode = ne;
				break;
			}
		}
		return retNode;
	}
	
	public int getExternalNodeCount(){return externalNodes.size();}
	public int getInternalNodeCount(){return internalNodes.size();}
	
	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public void assocObject(String name, Object obj) {
		TreeObject no = new TreeObject(name, obj);
		assoc.add(no);
	}

	public Object getObject(String name) {
		Object a = null;
		for (int i = 0; i < assoc.size(); i++) {
			if (assoc.get(i).getName().compareTo(name) == 0) {
				a = assoc.get(i);
			}
		}
		return a;
	}

	//need to check
	public void unRoot(Node inRoot){
		processRoot();
		if (this.getRoot().getChildCount() < 3) {
			tritomyRoot(inRoot);
		}
		processRoot();
	}
	
	/*
	 * just need to verify that the rerooting treats the branch lengths correctly
	 */
	public void reRoot(Node inRoot) {
		processRoot();
		if (this.getRoot().getChildCount() < 3) {
			tritomyRoot(inRoot);
		}
		//System.out.println(inRoot.getBL());
		if (inRoot == this.getRoot()) {
			System.err.println("you asked to root at the current root");
		} else {
			Node tempParent = inRoot.getParent();
			Node newRoot = new Node(tempParent);
			newRoot.addChild(inRoot);
			inRoot.setParent(newRoot);
			tempParent.removeChild(inRoot);
			tempParent.addChild(newRoot);
			newRoot.setParent(tempParent);
			newRoot.setBL(inRoot.getBL() / 2);
			inRoot.setBL(inRoot.getBL() / 2);
			ProcessReRoot(newRoot);
			setRoot(newRoot);
			processRoot();
		}
	}

	public void tritomyRoot(Node toberoot) {
		Node curroot = this.getRoot();
		if (toberoot == null) {
			if (curroot.getChild(0).isInternal()) {
				Node currootCH = curroot.getChild(0);
				double nbl = currootCH.getBL();
				curroot.getChild(1).setBL(curroot.getChild(1).getBL() + nbl);
				curroot.removeChild(currootCH);
				for (int i = 0; i < currootCH.getChildCount(); i++) {
					curroot.addChild(currootCH.getChild(i));
					//currootCH.getChild(i).setParent(curroot);
				}
			} else {
				Node currootCH = curroot.getChild(1);
				double nbl = currootCH.getBL();
				curroot.getChild(0).setBL(curroot.getChild(0).getBL() + nbl);
				curroot.removeChild(currootCH);
				for (int i = 0; i < currootCH.getChildCount(); i++) {
					curroot.addChild(currootCH.getChild(i));
					//currootCH.getChild(i).setParent(curroot);
				}
			}
		} else {
			if (curroot.getChild(1) == toberoot) {
				Node currootCH = curroot.getChild(0);
				double nbl = currootCH.getBL();
				curroot.getChild(1).setBL(curroot.getChild(1).getBL() + nbl);
				curroot.removeChild(currootCH);
				for (int i = 0; i < currootCH.getChildCount(); i++) {
					curroot.addChild(currootCH.getChild(i));
					//currootCH.getChild(i).setParent(curroot);
				}
			} else {
				Node currootCH = curroot.getChild(1);
				double nbl = currootCH.getBL();
				curroot.getChild(0).setBL(curroot.getChild(0).getBL() + nbl);
				curroot.removeChild(currootCH);
				for (int i = 0; i < currootCH.getChildCount(); i++) {
					curroot.addChild(currootCH.getChild(i));
					//currootCH.getChild(i).setParent(curroot);
				}
			}
		}
	}

	public Node getMRCA(String [] innodes){
		Node mrca = null;
    	if(innodes.length == 1)
    		return this.getExternalNode(innodes[0]);
    	else{
    		ArrayList <String> outgroup = new ArrayList<String>();
    		for(int i=0;i<innodes.length;i++){outgroup.add(innodes[i]);}
    		Node cur1 = this.getExternalNode(outgroup.get(0));
    		outgroup.remove(0);
    		Node cur2 = null;
    		Node tempmrca = null;
    		while(outgroup.size()>0){
    			cur2 = this.getExternalNode(outgroup.get(0));
    			outgroup.remove(0);
    			tempmrca = getMRCATraverse(cur1,cur2);
    			cur1 = tempmrca;
    		}
    		mrca = cur1;
    	}
    	return mrca;
    }
	
	public Node getMRCA(ArrayList<String> innodes){
		Node mrca = null;
    	if(innodes.size() == 1)
    		return this.getExternalNode(innodes.get(0));
    	else{
    		ArrayList <String> outgroup = new ArrayList<String>();
    		for(int i=0;i<innodes.size();i++){outgroup.add(innodes.get(i));}
    		Node cur1 = this.getExternalNode(outgroup.get(0));
    		outgroup.remove(0);
    		Node cur2 = null;
    		Node tempmrca = null;
    		while(outgroup.size()>0){
    			cur2 = this.getExternalNode(outgroup.get(0));
    			outgroup.remove(0);
    			tempmrca = getMRCATraverse(cur1,cur2);
    			cur1 = tempmrca;
    		}
    		mrca = cur1;
    	}
    	return mrca;
    }
	
	private void ProcessReRoot(Node node) {
		if (node.isTheRoot() || node.isExternal()) {
			return;
		}
		if (node.getParent() != null) {
			ProcessReRoot(node.getParent());
		}
		// Exchange branch label, length et cetera
		exchangeInfo(node.getParent(), node);
		// Rearrange topology
		Node parent = node.getParent();
		node.addChild(parent);
		parent.removeChild(node);
		parent.setParent(node);
	}

	/*
	 * swap info
	 */
	private void exchangeInfo(Node node1, Node node2) {
		String swaps;
		double swapd;
		swaps = node1.getName();
		node1.setName(node2.getName());
		node2.setName(swaps);

		swapd = node1.getBL();
		node1.setBL(node2.getBL());
		node2.setBL(swapd);
	}

	private void postOrderProcessRoot(Node node) {
		if (node == null)
			return;
		if (node.getChildCount() > 0) {
			for (int i = 0; i < node.getChildCount(); i++) {
				postOrderProcessRoot(node.getChild(i));
			}
		}
		if (node.isExternal()) {
			addExternalNode(node, externalNodeCount);
		} else {
			addInternalNode(node, internalNodeCount);
		}
	}

	/*
	 * prune external node
	 */
	public void pruneExternalNode(Node node){
		if(node.isInternal()){
			return;
		}
		/*
		 * how this works
		 * 
		 * get the parent = parent
		 * get the parent of the parent = mparent
		 * remove parent from mparent
		 * add !node from parent to mparent
		 * 
		 * doesn't yet take care if node.parent == root
		 * or polytomy
		 */
		double bl = 0;
		Node parent = node.getParent();
		Node other = null;
		for(int i=0;i<parent.getChildCount();i++){
			if(parent.getChild(i)!=node){
				other = parent.getChild(i);
			}
		}
		bl = other.getBL()+parent.getBL();
		Node mparent = parent.getParent();
		if(mparent != null){
			mparent.addChild(other);
			other.setBL(bl);
			for(int i=0;i<mparent.getChildCount();i++){
				if(mparent.getChild(i)==parent){
					mparent.removeChild(parent);
					break;
				}
			}
		}
		this.processRoot();
	}
	
	/*
	 * get the MRCA of the array of strings
	 * 
	 */

	private Node getMRCATraverse(Node curn1, Node curn2) {
		Node mrca = null;
		//get path to root for first node
		ArrayList<Node> path1 = new ArrayList<Node>();
		Node parent = curn1;
		path1.add(parent);
		while (parent != null) {
			path1.add(parent);
			if (parent.getParent() != null)
				parent = parent.getParent();
			else
				break;
		}
		//find first match between this node and the first one
		parent = curn2;
		boolean x = true;
		while (x == true) {
			for (int i = 0; i < path1.size(); i++) {
				if (parent == path1.get(i)) {
					mrca = parent;
					x = false;
					break;
				}
			}
			parent = parent.getParent();
		}
		return mrca;
	}
}

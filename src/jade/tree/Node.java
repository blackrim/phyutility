package jade.tree;

import java.util.*;

public class Node {
	/*
	 * common associations
	 */
	private double BL;//branch lengths
	private double distance_to_tip;
	private double distance_from_tip;
	private int number;
	private String name;
	private Node parent;
	private ArrayList<Node> children;
	private ArrayList<NodeObject> assoc;
	
	/*
	 * constructors
	 */
	public Node(){
		BL = 0.0;
		distance_to_tip = 0.0;
		distance_from_tip = 0.0;
		number = 0;
		name = "";
		parent = null;
		children = new ArrayList<Node> ();
		assoc = new ArrayList<NodeObject>();
	}
	
	public Node(Node parent){
		BL = 0.0;
		distance_to_tip = 0.0;
		distance_from_tip = 0.0;
		number = 0;
		name = "";
		this.parent = parent;
		children = new ArrayList<Node> ();
		assoc = new ArrayList<NodeObject>();
	}
	
	public Node(double BL, int number, String name, Node parent){
		this.BL = BL;
		distance_to_tip = 0.0;
		distance_from_tip = 0.0;
		this.number = number;
		this.name = name;
		this.parent = parent;
		children = new ArrayList<Node> ();
		assoc = new ArrayList<NodeObject>();
	}

	/*
	 * public methods
	 */
	
	public Node [] getChildrenArr(){return (Node[])children.toArray();}
	
	public ArrayList<Node> getChildren(){return children;}
	
    public boolean isExternal(){
    	if(children.size()<1)
    		return true;
    	else
    		return false;
    }
    
    public boolean isInternal(){
    	if(children.size()<1)
    		return false;
    	else
    		return true;
    }
    
    public boolean isTheRoot(){
    	if(parent == null)
    		return true;
    	else
    		return false;
    }
    
    public boolean hasParent(){
    	if(parent == null)
    		return false;
    	else
    		return true;
    }
    
    public void setParent(Node p){this.parent = p;}
    
    public int getNumber(){return number;}
    
    public void setNumber(int n){number = n;}
    
    public double getBL(){return BL;}
    
    public void setBL(double b){BL=b;}
    
    public boolean hasChild(Node test){
    	return children.contains((Node)test);
    }
    
    public boolean addChild(Node c){
    	if(hasChild(c)==false){
    		children.add(c);
    		c.setParent(this);
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean removeChild(Node c){
    	if(hasChild(c)==true){
    		children.remove(c);
    		return true;
    	}else{
    		return false;
    	}
    }

    public Node getChild(int c){return children.get(c);}
    
    public void setName(String s){name = s;}
    
    public String getName(){
    	//if(name != ""){
    		return name;
    	//}else{
    	//	return this.getNewick(false);
    	//}
    }
    
    public String getNewick(boolean bl){
    	String ret = "";
    	for(int i=0;i<this.getChildCount();i++){
    		if(i==0)
    			ret = ret+"(";
    		ret = ret+this.getChild(i).getNewick(bl);
    		if(bl==true)
    			ret = ret +":"+this.getChild(i).getBL();
    		if(i == this.getChildCount()-1)
    			ret =ret +")";
    		else
    			ret = ret+",";
    	}
    	if(name!=null)
    		ret = ret + name;
    	return ret;
    }
    
    public Node getParent(){return parent;}
    
    public int getChildCount(){return children.size();}
    
    public double getDistanceFromTip(){return distance_from_tip;}
    
    public void setDistanceFromTip(double inh){distance_from_tip = inh;}
    
    public double getDistanceToTip(){return distance_to_tip;}
    
    public void setDistanceToTip(double inh){distance_to_tip = inh;}
    
    public void assocObject(String name, Object obj){
    	boolean test = false;
    	for(int i=0;i<assoc.size();i++){
    		if(assoc.get(i).getName().compareTo(name)==0){
    			test = true;
    			assoc.get(i).setObject(obj);
    		}
    	}
    	if(test == false){
    		NodeObject no = new NodeObject(name, obj);
    		assoc.add(no);
    	}
    }
    
    public Object getObject(String name){
    	Object a = null;
    	for(int i=0;i<assoc.size();i++){
    		if(assoc.get(i).getName().compareTo(name)==0){
    			a = assoc.get(i).getObject();
    		}
    	}
    	return a;
    }
}

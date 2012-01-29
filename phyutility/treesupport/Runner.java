package phyutility.treesupport;

import java.util.*;

import jade.tree.*;

public class Runner {
	
	private ArrayList<ArrayList<String>> fixedtreenames = new ArrayList<ArrayList<String>>();
	private Tree fixedtree;
	private HashMap<Node, Double> incounts = new HashMap<Node, Double>();
	private ArrayList<Tree> intrees = new ArrayList<Tree> ();
	
	public Runner(ArrayList<Tree> intrees, Tree fixedtree){
		this.intrees = intrees;
		this.fixedtree = fixedtree;
	}
	
	public Tree run(){
		initializeRun();
		for(int i=0;i<intrees.size();i++){
			ArrayList<ArrayList<String>> treenames = new ArrayList<ArrayList<String>>();
			for(int j=0;j<intrees.get(i).getInternalNodeCount();j++){
				getAllTipNodesFromInternalNode(intrees.get(i).getInternalNode(j));
				treenames.add(tempNs);
			}
			compareTrees(treenames);
		}
		for(int i=0;i<fixedtree.getInternalNodeCount();i++){
			if(incounts.get(fixedtree.getInternalNode(i))!=null){
				if(fixedtree.getInternalNode(i).getName().length() >= 1){
					fixedtree.getInternalNode(i).setName(fixedtree.getInternalNode(i).getName()+"_"+(incounts.get(fixedtree.getInternalNode(i))/intrees.size()));
				}else{
					fixedtree.getInternalNode(i).setName(String.valueOf(incounts.get(fixedtree.getInternalNode(i))/intrees.size()));
				}
			}
		}
		return fixedtree;
	}
	
	private void initializeRun(){
		for(int i=0;i<fixedtree.getInternalNodeCount();i++){
			getAllTipNodesFromInternalNode(fixedtree.getInternalNode(i));
			fixedtreenames.add(tempNs);
		}
	}
	
	ArrayList<String> tempNs;
	private void getAllTipNodesFromInternalNode(Node intree){
		tempNs  = new ArrayList<String>();
		poGATNFIN(intree);
	}
	
	private void poGATNFIN(Node innode){
		for(int i=0;i<innode.getChildCount();i++){
			poGATNFIN(innode.getChild(i));
		}
		if(innode.isExternal()==true){
			tempNs.add(innode.getName().trim());
		}
	}
	
	private void compareTrees(ArrayList<ArrayList<String>> intreestrings){
		for(int i=0;i<fixedtreenames.size();i++){
			boolean test = false;
			for(int j=0;j<intreestrings.size();j++){
				if(intreestrings.get(j).size() != fixedtreenames.get(i).size()){
					continue;
				}else{
					test = testNames(fixedtreenames.get(i),intreestrings.get(j));
					//System.out.println(test);
					//printNames(fixedtreenames.get(i), fixedtreenames.get(i).size()+"+");
					//printNames(intreestrings.get(j), intreestrings.get(j).size()+"-");
					if(test == true)
						break;
				}
			}
			if(test ==true){
				//printNames(fixedtreenames.get(i), fixedtreenames.get(i).size()+"=");
				Node a = fixedtree.getMRCA(fixedtreenames.get(i));
				if(incounts.get(a) == null){
					incounts.put(a, 1.0);
				}else{
					incounts.put(a, incounts.get(a)+1);
				}
			}
		}
	}
	
	private boolean testNames(ArrayList<String> one, ArrayList<String> two){
		boolean ret = false;
		int count = 0;
		for(int i=0;i<one.size();i++){
			boolean match = false;
			for(int j=0;j<two.size();j++){
				if(one.get(i).compareTo(two.get(j))==0){
					match = true;
					break;
				}
			}
			if(match == true){
				count++;
			}else{
				count = 0;
				break;
			}
		}
		
		if(count == one.size())
			ret = true;
		return ret;
	}
	
	private void printNames(ArrayList<String> names, String add){
		System.out.print(add+" ");
		for(int i=0;i<names.size();i++){
			System.out.print(names.get(i)+"\t");
		}System.out.println();
	}
	
	public static void main(String [] args){
		
	}
}

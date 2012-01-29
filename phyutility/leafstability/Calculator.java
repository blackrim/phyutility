package phyutility.leafstability;

import jade.tree.*;
import java.util.*;

public class Calculator {
	public Calculator(){}
	public int calc(String [] inner, String [] outer, Tree tree){
		int ret = 0;
		Node inn = tree.getMRCA(inner);
		Node out = tree.getMRCA(outer);
		Node temp = inn;
		ArrayList<Node> path = new ArrayList<Node>();
		while(inn.getParent()!=null){
			path.add(inn.getParent());
			inn = inn.getParent();
		}
		if(path.contains((Node)out)){
			ret ++;
		}
		return ret;
	}
}

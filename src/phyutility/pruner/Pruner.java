package phyutility.pruner;
import jade.tree.TreeReader;
import java.util.*;
public class Pruner {
	private ArrayList<jade.tree.Tree> intrees;
	private ArrayList<String>names;
	public Pruner(ArrayList<jade.tree.Tree> intrees, ArrayList<String>names){
		this.intrees = intrees;
		this.names = names;
	}
	public ArrayList<jade.tree.Tree> go(){
		for(int i=0;i<intrees.size();i++){ 
			for(int j=0;j<names.size();j++){
			/*
			 * added for nodes whose parents are the root
			 * when that happens
			 * just get the nodes other than the one you are trying to root
			 */
				if (intrees.get(i).getExternalNode(names.get(j)).getParent().isTheRoot()){
					if(intrees.get(i).getRoot().getChildCount()>2){
						intrees.get(i).getRoot().removeChild(intrees.get(i).getExternalNode(names.get(j)));
						intrees.get(i).processRoot();
					}else{
						TreeReader tr = new TreeReader();
						String ts = "";
						for (int k = 0; k < intrees.get(i).getRoot().getChildCount(); k++) {
							if (intrees.get(i).getRoot().getChild(k) != intrees.get(i).getExternalNode(names.get(j))) {
								ts = intrees.get(i).getRoot().getChild(k).getNewick(true)+";";
							}
						}
						tr.setTree(ts);
						intrees.set(i, tr.readTree());
					}
				}
				/*
				 * end added
				 */
				else{
					intrees.get(i).pruneExternalNode(intrees.get(i).getExternalNode(names.get(j)));
				}
			}
		}
		return intrees;
	}
}

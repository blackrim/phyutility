package jade.simulate;

import jade.tree.*;

import java.util.*;
/*
 * 
 * for use with BioGeoTree
 * 
 * this is a node with area relationships
 */
public class Lineage {
	public Lineage(Node node, int numareas){
		ran = new Random();
		this.node = node;
		this.numareas = numareas;
		curareas = new int [this.numareas];
		curnumareas = 0;
	}
	public void addArea(int index){
		curareas[index] = 1;
		curnumareas++;
	}
	public void removeArea(int index){
		curareas[index] = 0;
		curnumareas--;
	}
	//return the removed area
	public int randomRemoveArea(){
		int rem = ran.nextInt(curnumareas);
		int cur = 0;
		int removed = 0;
		for(int i=0;i<numareas;i++){
			if(curareas[i]==1){
				if(cur==rem){
					curareas[i] = 0;
					removed = i;
					curnumareas--;
				}
				cur++;
			}
		}
		return removed;
	}
	public void randomAddArea(){
		if(curnumareas<numareas){
			int add = ran.nextInt(numareas-curnumareas);
			int cur = 0;
			for(int i=0;i<numareas;i++){
				if(curareas[i]==0){
					if(cur==add){
						curareas[i] = 1;
						curnumareas++;
					}
					cur++;
				}
			}
		}
	}
	//return a random area
	public int getRandomArea(){
		int get = ran.nextInt(curnumareas);
		int cur = 0;
		int gotten = 0;
		for(int i=0;i<numareas;i++){
			if(curareas[i]==1){
				if(cur==get){
					gotten = i;
				}
				cur++;
			}
		}
		return gotten;	
	}
	public String getString(){
		String ret= "";
		for(int i=0;i<numareas;i++){
			ret += curareas[i];
		}
		return ret;
	}
	public void setAreas(int [] areas){
		curareas = areas;
		curnumareas = 0;
		for(int i=0;i<numareas;i++){
			if(curareas[i]==1){
				curnumareas++;
			}
		}
	}
	
	public int getCurNumAreas(){return curnumareas;}
	public int [] getAreas(){return curareas;}
	public Node getNode(){return node;}
	
	private int [] curareas;
	private int numareas;//the total number of areas
	private int curnumareas=0;//the number of areas
	private Node node;
	private Random ran;
}

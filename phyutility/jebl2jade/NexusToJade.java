package phyutility.jebl2jade;

import jade.tree.TreeReader;

import java.io.*;
import java.util.*;

import phyutility.drb.WwdEmbedded;

import jebl.evolution.io.ImportException;

public class NexusToJade {
	public static ArrayList<jade.tree.Tree> getJadeFromJeblNexus(String filename){
		ArrayList<jade.tree.Tree> ret = new ArrayList<jade.tree.Tree>();
		try {
			jebl.evolution.io.NexusImporter ni = new jebl.evolution.io.NexusImporter(new FileReader(filename));
			List<jebl.evolution.trees.Tree> li = ni.importTrees();
			for(int i=0;i<li.size();i++){
				String st = jebl.evolution.trees.Utils.toNewick((jebl.evolution.trees.RootedTree)li.get(i));
				TreeReader tr = new TreeReader();
				BufferedReader br;
				br = new BufferedReader (new StringReader(st));
				String str = "";
				while((str = br.readLine())!=null){
					tr.setTree(str+";");
					ret.add(tr.readTree());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	public static WwdEmbedded getJadeFromJeblNexusDerby(String filename){
		WwdEmbedded ret = new WwdEmbedded("temp");
		ret.connectToDB();
		System.out.println("table made = "+ret.makeTable(true));
		try {
			jebl.evolution.io.NexusImporter ni = new jebl.evolution.io.NexusImporter(new FileReader(filename));
			while(ni.hasTree()){
				String st = jebl.evolution.trees.Utils.toNewick((jebl.evolution.trees.RootedTree)ni.importNextTree());
				TreeReader tr = new TreeReader();
				BufferedReader br;
				br = new BufferedReader (new StringReader(st));
				String str = "";
				while((str = br.readLine())!=null){
					if(str.length()>1){
						tr.setTree(str+";");
						ret.addTree(tr.readTree().getRoot().getNewick(true)+";");
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}

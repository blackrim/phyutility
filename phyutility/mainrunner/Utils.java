package phyutility.mainrunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import phyutility.drb.*;

public class Utils {
	public static void newickToNexus(String outfile, ArrayList<String> nw){
		FileWriter outw;
		try {
			outw = new FileWriter(outfile);
			outw.write("#NEXUS\n");
			outw.write("BEGIN TREES;\n");
			for(int i=0;i<nw.size();i++){
				outw.write("\tTREE tree"+i+" = "+nw.get(i)+"\n");
			}
			outw.write("END;\n\n");
			outw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void newickToNexusDerby(String outfile, WwdEmbedded derb){
		FileWriter outw;
		try {
			outw = new FileWriter(outfile);
			outw.write("#NEXUS\n");
			outw.write("BEGIN TREES;\n");
			for(int i=0;i<derb.getTableTreeSize();i++){
				jade.tree.Tree x = derb.getJadeTree(i);
				outw.write("\tTREE tree"+i+" = "+x.getRoot().getNewick(true)+";"+"\n");
			}
			outw.write("END;\n\n");
			outw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void newickToNexusRerootDerby(String outfile, WwdEmbedded derb, ArrayList<String> names){
		FileWriter outw;
		try {
			outw = new FileWriter(outfile);
			outw.write("#NEXUS\n");
			outw.write("BEGIN TREES;\n");
			for(int i=0;i<derb.getTableTreeSize();i++){
				jade.tree.Tree x = derb.getJadeTree(i);
				x.reRoot(x.getMRCA(names));
				outw.write("\tTREE tree"+i+" = "+x.getRoot().getNewick(true)+";"+"\n");
			}
			outw.write("END;\n\n");
			outw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void newickToNexusUnrootDerby(String outfile, WwdEmbedded derb){
		FileWriter outw;
		try {
			outw = new FileWriter(outfile);
			outw.write("#NEXUS\n");
			outw.write("BEGIN TREES;\n");
			for(int i=0;i<derb.getTableTreeSize();i++){
				jade.tree.Tree x = derb.getJadeTree(i);
				x.unRoot(x.getRoot());
				outw.write("\tTREE tree"+i+" = "+x.getRoot().getNewick(true)+";"+"\n");
			}
			outw.write("END;\n\n");
			outw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

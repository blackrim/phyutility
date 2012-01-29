package jade.runners;

import java.util.ArrayList;
import java.io.*;
import jade.data.*;

public class GBParserMain { 
	public GBParserMain(String []args){
		if(args.length<2){
			printInfo();
			System.exit(0);
		}
		else{
			try{
				if(Integer.valueOf(args[0]) == 8){//formatting for bioorganizer requires a region definition
					if(args.length==3){
						GBParser ca = new GBParser(args[1], args[2]);
						ArrayList<Sequence> seqs = ca.getSeqs();
						for(int i=0;i<seqs.size();i++){
							System.out.println(">"+seqs.get(i).getID());
							System.out.println(seqs.get(i).getSeq());
							System.out.println();
						}
					}else{
						System.out.println("you need to use the format java -jar gbparser.jar 8 file region");
						System.exit(0);
					}
				}else{
					GBParser ca = new GBParser(args[1], Integer.valueOf(args[0]));
					ArrayList<Sequence> seqs = ca.getSeqs();
					for(int i=0;i<seqs.size();i++){
						System.out.println(">"+seqs.get(i).getID());
						System.out.println(seqs.get(i).getSeq());
						System.out.println();
					}
				}
			}catch(IOException ioe){}
		}
	}
	private void printInfo(){
		System.out.println("usage: java -jar gbparser.jar # file");
		System.out.println("gbparser v.01 by Stephen Andrew Smith (blackrim.org)");
		System.out.println("\tthis assumes that the file is a genbank fasta file\n" +
				"\tthe numbers correspond to these options for the names\n" +
				"\t1) gi number\n" +
				"\t2) gb number\n" +
				"\t3) taxon name\n" +
				"\t4) taxon_name\n" +
				"\t5) T_name\n" +
				"\t6) taxon_name_ginumber\n" +
				"\t7) taxon_name_gbnumber\n" +
				"\t8) formatting for bioorganizer so type java -jar gbparser.jar 8 file regionname\n"+
				"\n" +
				"NOTE 1: if there are duplicate names, this will add numbers to the end\n" +
		"NOTE 2: some genbank entries are poorly formed and therefore do not get formatted correctly here, so double check!");
	}
	public static void main(String [] args){
		new GBParserMain(args);
	}
}

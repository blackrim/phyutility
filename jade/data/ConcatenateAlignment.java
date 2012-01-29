package jade.data;

import java.util.*;
import java.io.*;

/*
 * this takes fasta files and will concatenate them
 * 
 * the algorithm works as such
 * 1 take each file (assumes that you used muscle and each gene is in each file)
 * 2 store the names from each file for a union
 */

public class ConcatenateAlignment {
	public ConcatenateAlignment(ArrayList <String> fastaFiles, boolean verbose, String outfile){
		this.fastaFiles = fastaFiles;
		finalNames = new ArrayList <String>();
		geneLengths = new ArrayList<Integer>();
		allSeqs = new ArrayList <ArrayList<Sequence>>();
		finalSeqs = new ArrayList <Sequence> ();
		run();
		System.out.println("#NEXUS");
		printtofile(outfile);
		if(verbose == true){
			System.out.println("[total number of taxa: "+finalNames.size());
			System.out.println("taxa names");
			System.out.println("-----------");
			for(int i=0;i<finalSeqs.size();i++){
				System.out.println(finalSeqs.get(i).getID());
			}
			System.out.println("]");
		}
	}

	private void run(){
		/*
		 * this should union the taxa names
		 * and make sure that genelengths are correct
		 */
		for(int i=0;i<fastaFiles.size();i++){
			try{
				FastaReader fr = new FastaReader((String)fastaFiles.get(i));
				ArrayList<Sequence> seqs = fr.getSeqs();
				allSeqs.add(seqs);
				union(seqs);
				if(testSeqLengths(seqs)==true){
					geneLengths.add(seqs.get(0).getSeq().length());
				}else{
					System.err.println("you have different lengths in file number "+(i+1));
				}
			}catch(IOException ioe){
				System.out.println("there was a problem reading one of your files");
			}
		}
		/*
		 * this creates a new sequence (the final one) from the union which will be used to make the concatenated sequences
		 */
		for(int i=0;i<finalNames.size();i++){
			Sequence seq = new Sequence(finalNames.get(i),"");
			finalSeqs.add(seq);
		}
		/*
		 * this should make the strings for each taxa that will be used 
		 * for nexus file
		 */
		for(int i=0;i<allSeqs.size();i++){
			for(int j=0;j<finalSeqs.size();j++){
				boolean here = false;
				for(int k=0;k<allSeqs.get(i).size();k++){
					if(finalSeqs.get(j).getID().compareTo(allSeqs.get(i).get(k).getID())==0){
						finalSeqs.get(j).seqSeq(finalSeqs.get(j).getSeq()+allSeqs.get(i).get(k).getSeq());
						here = true;
					}
				}
				if(here == false){
					for(int k = 0;k<geneLengths.get(i);k++){
						finalSeqs.get(j).seqSeq(finalSeqs.get(j).getSeq()+"-");
					}
				}
			}
		}
	}
	
	private void union(ArrayList<Sequence> seqs){
		if(finalNames.size()<1){
			for(int i=0;i< seqs.size();i++){
				finalNames.add(((Sequence)seqs.get(i)).getID().trim());
			}
		}else{
			for(int i=0;i<seqs.size();i++){
				boolean b = false;
				for(int j = 0;j<finalNames.size();j++){
					if(((Sequence)seqs.get(i)).getID().trim().compareTo(finalNames.get(j))==0){
						b = true;
					}
				}
				if(b == false){
					finalNames.add(((Sequence)seqs.get(i)).getID().trim());
				}
			}
		}
	}
	
	private boolean testSeqLengths(ArrayList<Sequence> seqs){
		boolean ret = true;
		int j = seqs.get(0).getSeq().length();
		for(int i=1;i<seqs.size();i++){
			if(seqs.get(i).getSeq().length()!=j)
				ret = false;
		}
		return ret;
	}
	
	
	private void printtofile(String outfile){
		//System.out.println("#NEXUS");
		System.out.println("BEGIN DATA;");
		System.out.print("\t[");
		int cur = 1;
		for(int i=0;i<geneLengths.size();i++){
			System.out.print("gene"+(i+1)+" "+cur+"-"+(cur+geneLengths.get(i)-1)+" ");
			cur += geneLengths.get(i);
		}System.out.print("]\n");
		System.out.println("\tDIMENSIONS NTAX="+finalSeqs.size()+" NCHAR="+finalSeqs.get(0).getSeq().length()+";");
		System.out.println("\tFORMAT DATATYPE = DNA GAP = - MISSING = ?;");
		System.out.println("\tMATRIX");
		for(int i=0;i<finalSeqs.size();i++){
			System.out.println("\t"+finalSeqs.get(i).getID()+"\t"+finalSeqs.get(i).getSeq());
		}
		System.out.println(";");
		System.out.println("END;");
		System.out.println();
	}
	
	public static void main (String [] args){
		ArrayList<String> ff = new ArrayList<String>();
		ff.add("/home/smitty/programming/working_copy/PhyJ/stuff/test.fasta");
		ff.add("/home/smitty/programming/working_copy/PhyJ/stuff/test2.fasta");
		new ConcatenateAlignment(ff, false,"/home/smitty/programming/working_copy/PhyJ/stuff/test.out");
	}
	private ArrayList <String> fastaFiles;
	
	private ArrayList <String> finalNames;
	private ArrayList <ArrayList<Sequence>> allSeqs;
	private ArrayList <Sequence> finalSeqs;
	private ArrayList <Integer> geneLengths;
	private String outfile;
}

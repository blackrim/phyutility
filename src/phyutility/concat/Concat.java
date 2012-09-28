package phyutility.concat;

import java.io.*;
import java.util.ArrayList;

import jebl.evolution.io.*;
import jebl.evolution.sequences.*;
import jebl.evolution.taxa.Taxon;

public class Concat {
	private ArrayList <String> files;
	private ArrayList <File> refiles;
	private ArrayList <String> finalNames;
	private ArrayList <ArrayList<Sequence>> allSeqs;
	private ArrayList <Sequence> finalSeqs;
	private ArrayList <Integer> geneLengths;
	private String seqtype;
	
	
	public Concat(ArrayList <String> files, String seqtype){
		this.files = files;
		this.seqtype = seqtype;
		finalNames = new ArrayList <String>();
		geneLengths = new ArrayList<Integer>();
		allSeqs = new ArrayList <ArrayList<Sequence>>();
		finalSeqs = new ArrayList <Sequence> ();
		refiles = new ArrayList<File>();
		run();
	}

	private void run(){
		/*
		 * this should union the taxa names
		 * and make sure that genelengths are correct
		 */
		SequenceType usetype = null;
		if (seqtype.compareTo("test") == 0)
			usetype = testForSeqType(files.get(0));
		else if(seqtype.compareTo("nucleotide") == 0)
			usetype = SequenceType.NUCLEOTIDE;
		else if(seqtype.compareTo("aa") == 0)
			usetype = SequenceType.AMINO_ACID;
		for(int i=0;i<files.size();i++){
			String filename = (String)files.get(i);
			File file = new File(filename);
			refiles.add(file);
			if(testForNexus(filename) == false){
				try {
					FastaImporter fi = new FastaImporter(file,usetype);
					ArrayList<Sequence> seqs = (ArrayList<Sequence>)fi.importSequences();
					if(testDups(seqs) == true){
						System.err.println("you have duplicate taxa in file "+filename);
						System.exit(0);
					}
					allSeqs.add(seqs);
					union(seqs);
					if(testSeqLengths(seqs)==true){
						geneLengths.add(seqs.get(0).getLength());
					}else{
						System.err.println("you have different lengths in file number "+(i+1));
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				try {
					FileReader fr = new FileReader(file);
					NexusImporter fi = new NexusImporter(fr);
					ArrayList<Sequence> seqs = (ArrayList<Sequence>)fi.importSequences();
					allSeqs.add(seqs);
					union(seqs);
					if(testSeqLengths(seqs)==true){
						geneLengths.add(seqs.get(0).getLength());
					}else{
						System.err.println("you have different lengths in file number "+(i+1));
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		/*
		 * this creates a new sequence (the final one) from the union which will be used to make the concatenated sequences
		 */
		for(int i=0;i<finalNames.size();i++){
			BasicSequence seq = new BasicSequence(usetype,Taxon.getTaxon(finalNames.get(i)),"");
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
					if(finalSeqs.get(j).getTaxon().getName().compareTo(allSeqs.get(i).get(k).getTaxon().getName())==0){
						finalSeqs.set(j,new BasicSequence(usetype,finalSeqs.get(j).getTaxon(),
								(finalSeqs.get(j).getString()+allSeqs.get(i).get(k).getString())));
						here = true;
					}
				}
				if(here == false){
					String tst = "";
					for(int k = 0;k<geneLengths.get(i);k++){
						tst = tst+"-";
					}
					finalSeqs.set(j,new BasicSequence(usetype,finalSeqs.get(j).getTaxon(),
							(finalSeqs.get(j).getString()+tst)));
				}
			}
		}
	}

	
	
	private void union(ArrayList<Sequence> seqs){
		if(finalNames.size()<1){
			for(int i=0;i< seqs.size();i++){
				finalNames.add(((Sequence)seqs.get(i)).getTaxon().getName().trim());
			}
		}else{
			for(int i=0;i<seqs.size();i++){
				boolean b = false;
				for(int j = 0;j<finalNames.size();j++){
					if(((Sequence)seqs.get(i)).getTaxon().getName().trim().compareTo(finalNames.get(j))==0){
						b = true;
					}
				}
				if(b == false){
					finalNames.add(((Sequence)seqs.get(i)).getTaxon().getName().trim());
				}
			}
		}
	}

	private boolean testSeqLengths(ArrayList<Sequence> seqs){
		boolean ret = true;
		int j = seqs.get(0).getLength();
		for(int i=1;i<seqs.size();i++){
			if(seqs.get(i).getLength()!=j)
				ret = false;
		}
		return ret;
	}

	private boolean testDups(ArrayList<Sequence> seqs){
		ArrayList<String> names = new ArrayList<String>();
		for(int i=0;i<seqs.size();i++){
			boolean b = false;
			for(int j = 0;j<names.size();j++){
				if(((Sequence)seqs.get(i)).getTaxon().getName().trim().compareTo(names.get(j))==0){
					b = true;
				}
			}
			if(b == false){
				names.add(((Sequence)seqs.get(i)).getTaxon().getName().trim());
			}else{
				return true;
			}
		}
		return false;
	}


	public void printtofileNEXUS(String outfile){
		try {
			FileWriter fw = new FileWriter(outfile, false);
			fw.write("#NEXUS\n");
			fw.write("BEGIN DATA;\n");
			fw.write("\t[");
			int cur = 1;
			for(int i=0;i<geneLengths.size();i++){
				fw.write(refiles.get(i).getName()+"_gene"+(i+1)+" "+cur+"-"+(cur+geneLengths.get(i)-1)+" ");
				cur += geneLengths.get(i);
			}fw.write("]\n");
			fw.write("\tDIMENSIONS NTAX="+finalSeqs.size()+" NCHAR="+finalSeqs.get(0).getLength()+";\n");
			fw.write("\tFORMAT DATATYPE = DNA GAP = - MISSING = ?;\n");
			fw.write("\tMATRIX\n");
			for(int i=0;i<finalSeqs.size();i++){
				fw.write("\t"+finalSeqs.get(i).getTaxon().getName().replaceAll(" ", "_")+"\t"+finalSeqs.get(i).getString()+"\n");
			}
			fw.write(";\n");
			fw.write("END;\n\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printtoscreenNEXUS(){
		System.out.print("#NEXUS\n");
		System.out.print("BEGIN DATA;\n");
		System.out.print("\t[");
		int cur = 1;
		for(int i=0;i<geneLengths.size();i++){
			System.out.print(refiles.get(i).getName()+"_gene"+(i+1)+" "+cur+"-"+(cur+geneLengths.get(i)-1)+" ");
			cur += geneLengths.get(i);
		}System.out.print("]\n");
		System.out.print("\tDIMENSIONS NTAX="+finalSeqs.size()+" NCHAR="+finalSeqs.get(0).getLength()+";\n");
		System.out.print("\tFORMAT DATATYPE = DNA GAP = - MISSING = ?;\n");
		System.out.print("\tMATRIX\n");
		for(int i=0;i<finalSeqs.size();i++){
			System.out.print("\t"+finalSeqs.get(i).getTaxon().getName().replaceAll(" ","_")+"\t"+finalSeqs.get(i).getString()+"\n");
		}
		System.out.print(";\n");
		System.out.print("END;\n\n");
	}

	public void printtofileFASTA(String outfile){
		try {
			FileWriter fw = new FileWriter(outfile, false);
			fw.write("#NEXUS\n");
			fw.write("BEGIN DATA;\n");
			fw.write("\t[");
			int cur = 1;
			for(int i=0;i<geneLengths.size();i++){
				fw.write("gene"+(i+1)+" "+cur+"-"+(cur+geneLengths.get(i)-1)+" ");
				cur += geneLengths.get(i);
			}fw.write("]\n");
			fw.write("\tDIMENSIONS NTAX="+finalSeqs.size()+" NCHAR="+finalSeqs.get(0).getLength()+";\n");
			fw.write("\tFORMAT DATATYPE = DNA GAP = - MISSING = ?;\n");
			fw.write("\tMATRIX\n");
			for(int i=0;i<finalSeqs.size();i++){
				fw.write("\t"+finalSeqs.get(i).getTaxon().getName()+"\t"+finalSeqs.get(i).getString()+"\n");
			}
			fw.write(";\n");
			fw.write("END;\n\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean testForNexus(String filename){
		boolean ret = false;
		String str = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			str = br.readLine();
			if(str.toUpperCase().trim().compareTo("#NEXUS")==0)
				ret = true;
			br.close();
		}catch(IOException ioe){}
		return ret;
	}
	
	private SequenceType testForSeqType(String filename){
		SequenceType ret = SequenceType.NUCLEOTIDE;
		String str = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			str = br.readLine();
			while(str.length()<5 || str.startsWith(">")==true)
				str = br.readLine();
			if(str.length()>5 && str.startsWith(">") ==false)
				ret = jebl.evolution.sequences.Utils.guessSequenceType(str);
			br.close();
		}catch(IOException ioe){}
		return ret;
	}
}

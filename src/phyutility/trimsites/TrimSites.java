package phyutility.trimsites;
import java.io.*;
import java.util.*;

import jebl.evolution.alignments.*;
import jebl.evolution.io.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;

public class TrimSites {

	private BasicAlignment origaln;
	private BasicAlignment trimedalign;

	public TrimSites(String filename, String seqtype){
		/*
		 * test for nucleotide vs amino acid
		 */
		SequenceType usetype = null;
		if (seqtype.compareTo("test") == 0)
			usetype = testForSeqType(filename);
		else if(seqtype.compareTo("nucleotide") == 0)
			usetype = SequenceType.NUCLEOTIDE;
		else if(seqtype.compareTo("aa") == 0)
			usetype = SequenceType.AMINO_ACID;
		//System.out.println(usetype.toString());
		if(testForNexus(filename) == false){
			File file = new File(filename);
			try {
				FastaImporter fi = new FastaImporter(file,usetype);
				
				List<Sequence> seqs = fi.importSequences();
				origaln = new BasicAlignment(seqs);
				System.out.println("origsitecount "+origaln.getSiteCount());
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
			File file = new File(filename);
			try {
				FileReader fr = new FileReader(file);
				NexusImporter fi = new NexusImporter(fr);
				List<Sequence> seqs = fi.importSequences();
				origaln = new BasicAlignment(seqs);
				System.out.println("origsitecount "+origaln.getSiteCount());
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

	public BasicAlignment trimAln(double perc){
		ArrayList<Integer> sites = new ArrayList<Integer>();
		for(int i=0;i<origaln.getSiteCount();i++){
			double count = 0;
			Iterator <Sequence> seqs = origaln.getSequences().iterator();
			while(seqs.hasNext()){
				boolean gap = seqs.next().getState(i).isGap();
				if(gap == false){
					count ++;
				}
			}
			if(count/origaln.getSequences().size()>perc){
				sites.add(i);
			}
		}
		ResampledAlignment ra = new ResampledAlignment();
		int [] iar = new int [sites.size()];
		for(int i=0;i<iar.length;i++){
			iar[i] = sites.get(i);
		}
		ra.init(origaln, iar);
		trimedalign = new BasicAlignment (ra.getSequences());
		return trimedalign;
	}

	/*
	 * do after the trimAln
	 */
	public BasicAlignment trimAlnCleanMessy(double perc){
		ArrayList<Integer> sites = new ArrayList<Integer>();
		for(int i=0;i<trimedalign.getSiteCount();i++){
			ArrayList<String> sitestates = new ArrayList<String> ();
			Iterator <Sequence> seqs = trimedalign.getSequences().iterator();
			while(seqs.hasNext()){
				State tstate = seqs.next().getState(i);
				if (sitestates.contains(tstate.getName()) == false){
					sitestates.add(tstate.getName());
				}
			}
			if((sitestates.size()/(double)(trimedalign.getSequences().size()))<(1-perc)){
				sites.add(i);
			}
		}
		System.out.println(sites.size());
		ResampledAlignment ra = new ResampledAlignment();
		int [] iar = new int [sites.size()];
		for(int i=0;i<iar.length;i++){
			iar[i] = sites.get(i);
		}
		ra.init(trimedalign, iar);
		trimedalign = new BasicAlignment (ra.getSequences());
		return trimedalign;
	}
	
	public void printNexusOutfile(String outfile){
		try {
			FileWriter pw = new FileWriter(outfile);
			NexusExporter ne = new NexusExporter(pw);
			ne.exportAlignment(trimedalign);
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printFastaOutfile(String outfile){
		try {
			FileWriter pw = new FileWriter(outfile);
			FastaExporter ne = new FastaExporter(pw);
			ne.exportSequences(trimedalign.getSequences());
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		TrimSites ts = new TrimSites("/Users/smitty/programming/RELEASES/phyutility/examples/test.fasta","nucleotide");
		BasicAlignment test = ts.trimAln(0.49);
		System.out.println("trimed length = "+test.getSiteCount());
		/*Iterator<Sequence> seqs = test.getSequences().iterator();
		while(seqs.hasNext()){
			System.out.println(seqs.next().getString());
		}*/
		try {
			FileWriter pw = new FileWriter("/Users/smitty/Desktop/some2.nex");
			NexusExporter ne = new NexusExporter(pw);
			ne.exportAlignment(test);
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

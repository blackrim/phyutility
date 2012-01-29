package jade.data;

import java.io.*;
import java.util.*;

public class FastaReader {
	public FastaReader(String filename)throws IOException{
		seqs = new ArrayList<Sequence>();
		input = openFile(filename);
		read();
		//System.out.println(seqs.size());
		input.close();
	}
	public FastaReader(File infile)throws IOException{
		seqs = new ArrayList<Sequence>();
		input = new BufferedReader(new FileReader(infile));
		read();
		System.out.println(seqs.size());
		input.close();
	}
	public ArrayList<Sequence> getSeqs(){return seqs;}
	private BufferedReader openFile(String file) throws FileNotFoundException{
		return new BufferedReader(new FileReader(file));
	}
	private void read(){
		try{
			String str = "";
			String seqstr = "";
			Sequence curseq = new Sequence();
			while((str=input.readLine())!=null){
				if(str.startsWith(">")){
					if(seqstr.compareTo("")!=0&&curseq!=null){
						curseq.seqSeq(seqstr);
						seqstr = "";
						seqs.add(curseq);
					}
					curseq = new Sequence();
					str = str.substring(1).trim();
					curseq.setID(str);
				}else{
					seqstr += str.trim();
				}
			}
			//this would be for the last seq
			if(seqstr.compareTo("")!=0&&curseq!=null){
				curseq.seqSeq(seqstr);
				seqs.add(curseq);
			}
		}catch(IOException ioe){
			System.out.println("there was a problem reading your file (FastaReader.java)");
		}
	}
	public static void main(String [] args){
		try{
			new FastaReader("/home/smitty/programming/working_copy/PhyJ/stuff/test.fasta");
		}catch(IOException ioe){}
	}
	private ArrayList<Sequence> seqs;
	private BufferedReader input;
}

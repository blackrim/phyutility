package jade.data;
/*
 * This should parse a gb fasta file with 1 or more fasta sequences 
 * The idea is to use this to prepare the file for use with concat after muscle
 */

import java.util.*;
import java.io.*;
import java.util.regex.*;

public class GBParser{
	/*
	 * after the filename this requires an int representing
	 * 1 = gi
	 * 2 = gb (with decimal)
	 * 3 = taxon
	 * 4 = taxon with first name_last name
	 * 5 = taxon with one letter from first name uppercase_last name
	 * 6 = taxon with first name_last name and gb (with decimal)
	 */
	public GBParser(String filename, int num) throws IOException{
		this.num = num;
		seqs = new ArrayList<Sequence>();
		same = new HashMap<String,Integer>();
		input = openFile(filename);
		read();
		input.close();
		//for(int i =0 ;i <seqs.size();i++){
		//	System.out.println(seqs.get(i).getID());
		//}
	}
        
        public GBParser(String filename, String region) throws IOException{//for bioorganizer
		this.region = region;
		seqs = new ArrayList<Sequence>();
		same = new HashMap<String,Integer>();
		input = openFile(filename);
		readBioorganizer();
		input.close();
        }
        
	/*
	 * use just for bioorganizer
	 */
	private void readBioorganizer(){
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
					/*
					 * this is where I edit the genbank line given the int
					 */
					str=str.replace('|','\'');
					String [] astr = str.split("\'");
					String gi = astr[1];
					str = astr[4].trim();
					astr = str.split(" ");
					if(badForm(astr[0]) == true)
						str = astr[1]+"_"+astr[2]+"_"+region+"__"+gi;
					else
						str = astr[0]+"_"+astr[1]+"_"+region+"__"+gi;
					/*
					 * this is for checking for doubles
					 */
					boolean b = false;
					int in = 1;
					for(int i=0;i<seqs.size();i++){
						if(str.compareTo(seqs.get(i).getID())==0){
							if(b==false){
								b=true;
								in++;
								same.put(str,in);
							}
						}else if(same.containsKey(str)){
							in = same.get(str);
							in++;
							same.put(str,in);
						}
					}
					if(b == false)
						curseq.setID(str);
					else
						curseq.setID(str+String.valueOf(in));
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
			System.out.println("there was a problem reading your file (GBParser.java)");
		}
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
					/*
					 * this is where I edit the genbank line given the int
					 */
					str=str.replace('|','\'');
					if(num == 1){//gi
						String [] astr = str.split("\'");
						str = astr[1];
					}else if(num == 2){//gb
						String [] astr = str.split("\'");
						str = astr[3];
					}else if(num == 3){//taxon
						String [] astr = str.split("\'");
						str = astr[4].trim();
						astr = str.split(" ");
						int st = 0;
						while(allCaps(astr[st]) == true){
							st++;
						}
						str = astr[st]+" "+astr[st+1];
						/*
						if(badForm(astr[0]) == true)
							str = astr[1]+" "+astr[2];
						else
							str = astr[0]+" "+astr[1];
						*/
					}else if(num == 4){//taxon with name_name
						String [] astr = str.split("\'");
						str = astr[4].trim();
						astr = str.split(" ");
						int st = 0;
						while(allCaps(astr[st]) == true){
							st++;
						}
						str = astr[st]+"_"+astr[st+1];
						/*
						if(badForm(astr[0]) == true)
							str = astr[1]+"_"+astr[2];
						else
							str = astr[0]+"_"+astr[1];
						*/
					}else if(num == 5){//taxon with n_name
						String [] astr = str.split("\'");
						str = astr[4].trim();
						astr = str.split(" ");
						int st = 0;
						while(allCaps(astr[st]) == true){
							st++;
						}
						str = astr[st].substring(0,1)+"_"+astr[st+1];
						/*
						if(badForm(astr[0]) == true)
							str = astr[1].substring(0,1)+"_"+astr[2];
						else
							str = astr[0].substring(0,1)+"_"+astr[1];
						*/
					}else if(num == 6){
						String [] astr = str.split("\'");
						String gi = astr[1];
						str = astr[4].trim();
						astr = str.split(" ");
						int st = 0;
						while(allCaps(astr[st]) == true){
							st++;
						}
						str = astr[st]+"_"+astr[st+1]+"_"+gi;
						/*
						if(badForm(astr[0]) == true)
							str = astr[1]+"_"+astr[2]+"_"+gi;
						else
							str = astr[0]+"_"+astr[1]+"_"+gi;
						*/
					}else if(num == 7){
						String [] astr = str.split("\'");
						String gb = astr[3];
						str = astr[4].trim();
						astr = str.split(" ");
						int st = 0;
						while(allCaps(astr[st]) == true){
							st++;
						}
						str = astr[st]+"_"+astr[st+1]+"_"+gb;
						/*
						if(badForm(astr[0]) == true)
							str = astr[1]+"_"+astr[2]+"_"+gb;
						else
							str = astr[0]+"_"+astr[1]+"_"+gb;
						*/
					}
					/*
					 * this is for checking for doubles
					 */
					boolean b = false;
					int in = 1;
					for(int i=0;i<seqs.size();i++){
						if(str.compareTo(seqs.get(i).getID())==0){
							if(b==false){
								b=true;
								in++;
								same.put(str,in);
							}
						}else if(same.containsKey(str)){
							in = same.get(str);
							in++;
							same.put(str,in);
						}
					}
					if(b == false)
						curseq.setID(str);
					else
						curseq.setID(str+String.valueOf(in));
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
			System.out.println("there was a problem reading your file (GBParser.java)");
		}
	}
	public ArrayList<Sequence> getSeqs(){return seqs;}
	private BufferedReader openFile(String file) throws FileNotFoundException{
		return new BufferedReader(new FileReader(file));
	}
	public static void main (String [] args){
		try{
			new GBParser("/home/smitty/programming/working_copy/gbparser/stuff/test.fasta",3);
		}catch(IOException ioe){}
	}
	//private
	private ArrayList<Sequence> seqs;
	private BufferedReader input;
	private int num;
	private HashMap<String,Integer> same;
	private String region;
	/*
	 * test for malformed genbank taxon form
	 */
	private boolean badForm(String ins){
		boolean ret = false;
		Pattern pattern = Pattern.compile("\\d");
		Matcher matcher = pattern.matcher(ins);
		boolean found = false;
		while (matcher.find()) {
                found = true;
		}
		if(found == true)
			ret = true;
		return ret;
	}
	private boolean allCaps(String ins){
		boolean ret = true;
		for(int i=0;i<ins.length();i++){
			if(Character.isLowerCase(ins.charAt(i))){
				ret = false;
			}
		}
		return ret;
	}
}

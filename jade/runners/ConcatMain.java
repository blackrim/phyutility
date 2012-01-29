package jade.runners;

import java.util.*;

import jade.data.*;

public class ConcatMain {
	public ConcatMain(String [] args){
		if(args.length<1){
			printInfo();
			System.exit(0);
		}
		else{
			ArrayList <String> files = new ArrayList<String>();
			for(int i=0;i<args.length;i++){
				files.add(args[i]);
			}
			ConcatenateAlignment ca = new ConcatenateAlignment(files, false, "");
		}
	}
	private void printInfo(){
		System.out.println("usage: java -jar concat file1 file2 ...");
		System.out.println("concat v.01 by Stephen Smith");
		System.out.println("\tthis assumes that the files are aligned fasta (like from muscle) \n" +
				"\tand that the taxa names are equivalent between files\n" +
				"\tand that each file is a different gene");
	}
	public static void main(String [] args){
		new ConcatMain(args);
	}
}

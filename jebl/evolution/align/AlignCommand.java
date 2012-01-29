package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.ScoresFactory;

import java.io.*;

/**
 * A command line interface for the algorithms in jebl.evolution.align.
 * Imports from FASTA files.
 * 
 * @author Richard Moir
 *
 * @version $Id: AlignCommand.java 185 2006-01-23 23:03:18Z rambaut $
 *
 */
public class AlignCommand {
	
	private boolean relaunch = true;	//run command interface again
	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public AlignCommand() {
		System.out.println("Usage: AlignCommand <sequence1 file name> <sequence2 file name>\n(FASTA format files)");
	}
	
	public AlignCommand(String sq1File, String sq2File) {
		
		//import sequences from files.
		String sq1 = "";
		String sq2 = "";
		try {
			BufferedReader br1 = new BufferedReader(new FileReader(sq1File));
			BufferedReader br2 = new BufferedReader(new FileReader(sq2File));
			
			String line = br1.readLine();
			if(!((line.substring(0,1)).equals(">")))
				sq1 += line;
			line = br1.readLine();
			while(line != null) {
				if(line.substring(0,1).equals(">")) break;
				sq1 = sq1.concat(line.trim());
				line = br1.readLine();
			}
			
			line = br2.readLine();
			if(!((line.substring(0,1)).equals(">")))
				sq2 += line;
			line = br2.readLine();
			while(line != null) {
				if(line.substring(0,1).equals(">")) break;
				sq2 = sq2.concat(line.trim());
				line = br2.readLine();
			}
			
		}
		catch(Exception e) {
			System.out.println("error reading from sequence file\n" + e);
		}
		
		while(relaunch)
			command(sq1, sq2);
	}
	
	private void command(String sq1, String sq2) {
		
		//choose algorithm
		System.out.println(	"Choose algorithm:");
		System.out.println( "1.NeedlemanWunsch");
		System.out.println( "2.NeedlemanWunschAffine");
		System.out.println( "3.NeedlemanWunschLinearSpace");
		System.out.println(	"4.OverlapAlign");
		System.out.println( "5.RepeatAlign");
		System.out.println( "6.SmithWaterman");
		System.out.println( "7.SmithWatermanLinearSpace");
		System.out.println( "8.SmithWatermanLinearSpaceAffine");
		System.out.println( "9.NonOverlapMultipleAlign");
        System.out.println("10.NeedlemanWunschLinearSpaceAffine");

        int input = 0;
		try {
			input = Integer.parseInt(readInput("? "));
			if(input < 1 || input > 10) throw new Exception("must be 1 to 10");
		}
		catch(Exception e) {
			System.out.println("invalid entry\n" + e);
			System.exit(-1);
		}
		
		//construct the alignment
		AlignSimple as = null;
		AlignAffine aa = null;
		AlignRepeat ar = null;
		AlignRepeatAffine ara = null;
		
		switch(input) {
			case 1:
				as = new NeedlemanWunsch(null, 0);
				break;
			case 2:
				aa = new NeedlemanWunschAffine(null, 0, 0);
				break;
			case 3:
				as = new NeedlemanWunschLinearSpace(null, 0);
				break;
			case 4:
				as = new OverlapAlign(null, 0);
				break;
			//case 5:
			//	ar = new RepeatAlign(null, 0, 0);
			//	break;
			case 6:
				as = new SmithWaterman(null, 0);
				break;
			case 7:
				as = new SmithWatermanLinearSpace(null, 0);
				break;
			case 8:
				aa = new SmithWatermanLinearSpaceAffine(null, 0, 0);
				break;
			case 9:
				ara = new NonOverlapMultipleLocalAffine(null, 0, 0, 0);
				break;
            case 10:
                aa = new NeedlemanWunschLinearSpaceAffine(null, 0, 0);
                break;
        }
		
		//choose alignment parameters
		System.out.println("Enter gap open penalty:");
		float gapOpen = 0;
		try {
			gapOpen = Float.parseFloat(readInput("? "));
		}
		catch(Exception e) {
			System.out.println("invalid entry\n" + e);
			System.exit(-1);
		}
		
		float gapExtend = 0;
		if(aa != null || ara != null) {
			System.out.println("Enter gap extend penalty:");
			try {
				gapExtend = Float.parseFloat(readInput("? "));
			}
			catch(Exception e) {
				System.out.println("invalid entry\n" + e);
				System.exit(-1);
			}
		}
		
		int threshold = 0;
		if(ar != null || ara != null) {
			System.out.println("Enter threshold T:");
			try {
				threshold = Integer.parseInt(readInput("? "));
			}
			catch(Exception e) {
				System.out.println("invalid entry\n" + e);
				System.exit(-1);
			}
		}
		
		//choose substitution matrix to use
		System.out.println("Enter substitution matrix type(1.BLOSUM, 2.PAM, 3.JUKESCANTOR):");
		String subType = null;
		String subVal = null;
		float dist = 0;		//for calculating nucleotide substitution matrix.
		
		try {
			subType = readInput("? ");
			if(subType.equals("BLOSUM") || subType.equals("1")) {
				System.out.println("Enter BLOSUM value(45 - 90):");
				subVal = readInput("? ");
				subType = "Blosum";
			}
			else if(subType.equals("PAM") || subType.equals("2")) {
				System.out.println("Enter PAM value(100 - 250):");
				subVal = readInput("? ");
				subType = "Pam";
			}
			else if(subType.equals("JUKESCANTOR") || subType.equals("3")) {
				System.out.println("Enter evolutionary distance d:");
				dist = Float.parseFloat(readInput("? "));
				subType = "JukesCantor";
			}
			else throw new Exception("must be 1 to 3");
		}
		catch(Exception e) {
			System.out.println("invalid entry\n" + e);
			System.exit(-1);
		}
		
		Scores sub; //substitution matrix
		
		if(subVal != null) {
			sub = ScoresFactory.generateScores(subType + subVal);	//subType, Integer.parseInt(subVal));
		}
		else {
			sub = ScoresFactory.generateScores(subType + dist);		//subType, dist);
		}
		
		//choose shuffling
		boolean shuffle = false;
		int numShuffles = 0;
		int numRepeats = 1;
		try {
			System.out.println("Do you wish to perform shuffling for the alignment(y/n):");
			String shuff = readInput("? ");
			if((shuff.toLowerCase()).equals("y")) {
				System.out.println("How many times do you wish to shuffle:");
				numShuffles = Integer.parseInt(readInput("? "));
				shuffle = true;
			}
			else {
				System.out.println("How many times do you wish to run the alignment:");
				numRepeats = Integer.parseInt(readInput("? "));
				if(numRepeats < 1) {
					System.out.println("Must run atleast once, exiting..");
					System.exit(-1);
				}
			}
		}
		catch(Exception e) {

        }
		
		long start;			    //time algorithm started
		long fin;			    //time algorithm finished
		String outPut = "";		//string to print to command line and logfile
		
		String match;	        //string representation of alignment
		String algoName;	    //name of algorithm used
		float score;		    //alignment score
		
		if(!shuffle) {		//no shuffling
			if(as != null) {	//simple alignment
				start = System.currentTimeMillis();
				for(int i = 0; i < numRepeats; i++) {
					as.setGapOpen(gapOpen);
					as.setScores(sub);
					as.doAlignment(sq1, sq2);
				}
				fin = System.currentTimeMillis();
				match = chopSequence(as.getMatch());
				algoName = formatAlgoName((as.getClass()).getName());
				score = as.getScore();
			}
			else if(aa != null) {	//affine alignment
				start = System.currentTimeMillis();
				for(int i = 0; i < numRepeats; i++) {
					aa.setGapExtend(gapOpen);
					aa.setGapOpen(gapOpen);
					aa.setScores(sub);
					aa.doAlignment(sq1, sq2);
				}
				fin = System.currentTimeMillis();
				match = chopSequence(aa.getMatch());
				algoName = formatAlgoName((aa.getClass()).getName());
				score = aa.getScore();
			}
			else if(ar != null) {	//repeated alignment
				start = System.currentTimeMillis();
				for(int i = 0; i < numRepeats; i++) {
					ar.setGapOpen(gapOpen);
					ar.setScores(sub);
					ar.setThreshold(threshold);
					ar.doAlignment(sq1, sq2);
				}
				fin = System.currentTimeMillis();
				match = chopSequence(ar.getMatch());
				algoName = formatAlgoName((ar.getClass()).getName());
				score = ar.getScore();
			}
			else {					//repeated affine alignment
				start = System.currentTimeMillis();
				for(int i = 0; i < numRepeats; i++) {
					ara.setGapExtend(gapExtend);
					ara.setGapOpen(gapOpen);
					ara.setThreshold(threshold);
					ara.setScores(sub);
					ara.doAlignment(sq1, sq2);
				}
				fin = System.currentTimeMillis();
				match = chopSequence(ara.getMatch());
				algoName = formatAlgoName((ara.getClass()).getName());
				score = ara.getScore();
			}
			
			//print output
			long timeTaken = fin - start;
			outPut = outPut.concat(numRepeats + " repeat(s) of " + algoName + " took " + timeTaken + "ms.\n\n");
			if(ara == null)
				outPut = outPut.concat("Alignment (Score " + score + "):\n\n");
			else
				outPut = outPut.concat("Alignment (Total Score " + score + "):\n\n");
			outPut = outPut.concat(match + "\n" + "\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\\n");
			System.out.println(outPut);
		}
		
		else { //use shuffling
			SequenceShuffler shuff = new SequenceShuffler();
			if(as != null) {
				start = System.currentTimeMillis();
				as.setGapOpen(gapOpen);
				as.setScores(sub);
				shuff.shuffle(as, sq1, sq2, numShuffles);
				as.doAlignment(sq1, sq2);
				fin = System.currentTimeMillis();
				match = chopSequence(as.getMatch());
				algoName = formatAlgoName((as.getClass()).getName());
				score = as.getScore();
			}
			else if(aa != null) {
				start = System.currentTimeMillis();
				aa.setGapExtend(gapOpen);
				aa.setGapOpen(gapOpen);
				aa.setScores(sub);
				shuff.shuffle(aa, sq1, sq2, numShuffles);
				aa.doAlignment(sq1, sq2);
				fin = System.currentTimeMillis();
				match = chopSequence(aa.getMatch());
				algoName = formatAlgoName((aa.getClass()).getName());
				score = aa.getScore();
			}
			else if(ar != null) {
				start = System.currentTimeMillis();
				ar.setGapOpen(gapOpen);
				ar.setScores(sub);
				ar.setThreshold(threshold);
				shuff.shuffle(ar, sq1, sq2, numShuffles);
				ar.doAlignment(sq1, sq2);
				fin = System.currentTimeMillis();
				match = chopSequence(ar.getMatch());
				algoName = formatAlgoName((ar.getClass()).getName());
				score = ar.getScore();
			}
			else { //repeated affine alignment
				start = System.currentTimeMillis();
				ara.setGapExtend(gapExtend);
				ara.setGapOpen(gapOpen);
				ara.setThreshold(threshold);
				ara.setScores(sub);
				shuff.shuffle(ara, sq1, sq2, numShuffles);
				ara.doAlignment(sq1, sq2);
				fin = System.currentTimeMillis();
				match = chopSequence(ara.getMatch());
				algoName = formatAlgoName((ara.getClass()).getName());
				score = ara.getScore();
			}
			
			//print output
			long timeTaken = fin - start;
			outPut = outPut.concat(numShuffles + " shuffle(s) of " + algoName + " took " + timeTaken + "ms.\n\n");
			if(ara == null)
				outPut = outPut.concat("Alignment (Score " + score + "):\n\n");
			else
				outPut = outPut.concat("Alignment (Total Score " + score + "):\n\n");
			outPut = outPut.concat(match + "\n");
			outPut = outPut.concat("Shuffling " + numShuffles + " times:\tMean: " + shuff.getMean() + "\tstdev: " + shuff.getStdev() + "\n\n\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\\n");
			System.out.println(outPut);
		}
		
		//print output to logfile
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("alignment_log.txt", true));
			pw.print(outPut + "\n\n");
			pw.close();
			System.out.println("result logged in alignment_log.txt.\n");
		}
		catch(Exception e) {
			System.out.println("error writing logfile\n" + e);
		}
		
		//choose to relaunch command
		try {
			relaunch = false;
			System.out.println("Do you wish to perform another alignment with these sequences(y/n):");
			String another = readInput("? ");
			if((another.toLowerCase()).equals("y")) {
				relaunch = true;
			}
		}
		catch(Exception e) {}
	}
	
	/**
	 * Reads input from keyboard.
	 * 
	 * @param s mesage printed before input
	 * @return keyboard input
	 */
	private String readInput(String s) {
		System.out.print(s);
		try {
			return in.readLine();
		}
		catch(IOException e) {
			System.out.println("error reading from keyboard!\n" + e);
		}
		return null;
	}
	
	/**
	 * @param algoName 
	 * @return	substring of algoName from after index of the last '.'
	 */
	private String formatAlgoName(String algoName) {
		String currentChar = "";
		int cc = algoName.length();
		while(!currentChar.equals(".") && cc >= 0) {
			currentChar = algoName.substring(cc - 1, cc);
			cc --;
		}
		return algoName.substring(cc + 1);
	}
	
	/**
	 * Chops the two sequences into 100 character lengths and puts them on new lines.
	 * The two sequences must be the same length (aligned in some way).
	 * 
	 * @param sq array of aligned sequences (from method getMatch()).
	 * @return	chopped sequences
	 */
	private String chopSequence(String[] sq) {
		if(sq[0].length() != sq[1].length()) {
			return "error! cannot chop sequences of different lengths with this method";
		}
		String s = "";
		int size = sq[1].length();
		for(int i = 100; i < size + 100; i += 100) {
			if(i > size) {
				s = s.concat(sq[0].substring(i - 100, size) + "\n");
				s = s.concat(sq[1].substring(i - 100, size) + "\n\n");
			}
			else {
				s = s.concat(sq[0].substring(i - 100, i) + "\n");
				s = s.concat(sq[1].substring(i - 100, i) + "\n\n");
			}
		}
		
		return s;
	}
	
	public static void main(String args[]) {
		try {
			new AlignCommand(args[0], args[1]);
		}
		catch(Exception e) {
			System.out.println(e);
			new AlignCommand();
		}
	}
}
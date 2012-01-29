/*
 * Alignment.java
 *
 * Created on April 18, 2005, 8:22 PM
 */

package jade.data;
import java.util.*;
/**
 *
 * @author stephensmith
 * 
 * currently uses some of the PAL routines
 */
public class Alignment extends AbstractAlignment{

    public Alignment() {}
        
//    public Alignment(AbstractAlignment a) {
//        this(a,(LabelMapping)null);
//    }

//    public Alignment(AbstractAlignment a, int sequenceToIgnore) {
//        int numberOfOriginalSequences = a.getIdCount();
//        setDataType(a.getDataType());
//        String[] sequences =
//                (
//                sequenceToIgnore<0||sequenceToIgnore>=numberOfOriginalSequences ?
//                    new String[numberOfOriginalSequences] :
//                    new String[numberOfOriginalSequences-1]
//                );
//        int index = 0;
//        for (int i = 0; i < numberOfOriginalSequences ; i++) {
//            if(i!=sequenceToIgnore) {
//                sequences[index++] = a.getAlignedSequenceString(i);
//            }
//        }
//        
//        init(new SimpleIdGroup(a,sequenceToIgnore), sequences, GAPS);
//    }
    
    public Alignment(ArrayList<String> seqNames, ArrayList<String> sequences, String gaps, DataType dt) {
        setDataType(dt);
        init(seqNames, sequences, gaps);
    }
    public Alignment(ArrayList<String> seqNames, ArrayList<String> sequences, DataType dt) {
        this(seqNames,sequences,null,dt);
    }
    
    private void init(ArrayList<String> seqNames, ArrayList<String> sequences, String gaps) {
        sequences = getPadded(sequences);
        numSeqs = sequences.size();
        numSites = sequences.get(0).length();
        seqIDs = seqNames;
        this.sequences = sequences;
        if (gaps != null) {
            convertGaps(gaps);
        }
    }
    
    /**
     * Constructor taking single identifier and sequence.
     */
    public Alignment(String seqName, String sequence, DataType dataType) {
        
        setDataType(dataType);
        numSeqs = 1;
        numSites = sequence.length();
        
        sequences = new ArrayList<String>();
        sequences.set(0,sequence);
        
        seqIDs = new ArrayList<String>();
        seqIDs.set(0, seqName);
    }
    
    /**
     * This constructor combines to alignments given two guide strings.
     */
//    public Alignment(AbstractAlignment a, AbstractAlignment b,
//            String guide1, String guide2, char gap) {
//        
//        sequences = new String[a.getSequenceCount() + b.getSequenceCount()];
//        numSeqs = sequences.length;
//        
//        for (int i = 0; i < a.getSequenceCount(); i++) {
//            sequences[i] = getAlignedString(a.getAlignedSequenceString(i), guide1, gap, GAP);
//        }
//        for (int i = 0; i < b.getSequenceCount(); i++) {
//            sequences[i + a.getSequenceCount()] =
//                    getAlignedString(b.getAlignedSequenceString(i), guide2, gap, GAP);
//        }
//        
//        numSites = sequences[0].length();
//        idGroup = new SimpleIdGroup(a, b);
//    }
//    
    /** sequence alignment at (sequence, site) */
    public char getData(int seq, int site) {
        return sequences.get(seq).charAt(site);
    }
    
    /**
     * Returns a string representing a single sequence (including gaps)
     * from this alignment.
     */
    public String getAlignedSequenceString(int seq) {
        return sequences.get(seq);
    }
    
    // PRIVATE STUFF
    
//    private String getAlignedString(String original, String guide, char guideChar, char gapChar) {
//        StringBuffer buf = new StringBuffer(guide.length());
//        int seqcounter = 0;
//        for (int j = 0; j < guide.length(); j++) {
//            if (guide.charAt(j) != guideChar) {
//                buf.append(original.charAt(seqcounter));
//                seqcounter += 1;
//            } else {
//                buf.append(gapChar);
//            }
//        }
//        return new String(buf);
//    }
    private static final String getPadded(String s, int length) {
        StringBuffer sb = new StringBuffer();
        sb.append(s);
        for(int i = s.length() ; i < length ; i++) {
            sb.append(Alignment.GAP);
        }
        return sb.toString();
    }
    private static final ArrayList<String> getPadded(ArrayList<String> sequences) {
        ArrayList<String> padded = new ArrayList<String>();
        int maxLength = 0;
        for(int i = 0 ; i < sequences.size() ; i++) {
            maxLength = Math.max(maxLength,sequences.get(i).length());
        }
        for(int i = 0 ; i < sequences.size() ; i++) {
            padded.add(getPadded(sequences.get(i),maxLength));
        }
        return padded;
        
    }
    /**
     * Converts all gap characters to Alignment.GAP
     */
    private void convertGaps(String gaps) {
        for (int i = 0; i < sequences.size(); i++) {
            for (int j = 0; j < gaps.length(); j++) {
                sequences.set(i,sequences.get(i).replace(gaps.charAt(j), Alignment.GAP));
            }
        }
    }
    
    ArrayList<String> sequences;
}

package jebl.evolution.align;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matt Kearse
 * @version $Id: Profile.java 706 2007-05-09 02:47:48Z matt_kearse $
 *
 * Represents a profile of a number of sequences to be used in a
 * multiple sequence alignment.
 */
class Profile {
    ProfileCharacter[] profile;
    private int alphabetSize;
//    int length;
    int sequenceCount;
    private boolean automaticallyCalculatedAlphabetSize = false;
    private Map<Integer, String> paddedSequences = new HashMap<Integer, String>();
    private boolean supportsFreeEndGaps=false;

    public String getSequence( int sequenceNumber) {
        return paddedSequences.get(sequenceNumber);
    }


    public Profile(int alphabetSize) {
        this.alphabetSize = alphabetSize;
    }

    /**
     * Should only be used for constructing a profile that will not
     * have other sequences added.
     *
     * Correct usage in other cases is to do
     * new Profile( calculateAlphabetSize(sequences));
     *
     * @param sequenceNumber
     * @param sequence
     */
    public Profile(int sequenceNumber,String sequence) {
        this(calculateAlphabetSize(new String[] {sequence}));
        addSequence(sequenceNumber,sequence);
        automaticallyCalculatedAlphabetSize = true;
    }

    public Profile(Alignment alignment, int alphabetSize) {
        this(alignment, alphabetSize , 0);
    }

    public Profile(Alignment alignment, int alphabetSize, int offset) {
        this.alphabetSize = alphabetSize;
        final List<Sequence> sequenceList = alignment.getSequenceList();
        for(int i = 0; i < sequenceList.size(); ++i) {
            addSequence(i + offset, sequenceList.get(i).getString());
        }
    }

    /**
     * Get the number of sequences in this profile.
     * @return the number of sequences in this profile.
     */
    public int getNumberOfSequences() {
        return sequenceCount;
    }
    /**
     * Get the number of residues in each sequence in the profile
     * @return the number of residues in each sequence in the profile
     */
    public int length () {
        return profile.length;
    }

    private static ProfileCharacter[] createProfile(String sequence, int alphabetSize) {
        int length = sequence.length();
        ProfileCharacter results[] = new ProfileCharacter[length];
        for (int i = 0; i < length; i++) {
            ProfileCharacter profile = new ProfileCharacter(alphabetSize);
            profile.addCharacter(sequence.charAt(i), 1);
            results[i] = profile;
        }
        return results;
    }

    void addSequence(int sequenceNumber,String sequence) {
        sequence=sequence.toUpperCase();
        if (automaticallyCalculatedAlphabetSize)
            throw new IllegalStateException("if the constructor 'public Profile(int sequenceNumber,String sequence)'  is used, it's not safe to add new sequences");
        if (supportsFreeEndGaps) sequence=supportFreeEndGaps( sequence);
        sequenceCount++;
        if (sequenceCount == 1) {
            profile = createProfile(sequence, alphabetSize);
        }
        else {
            assert(profile.length == sequence.length());
            for (int i = 0; i < profile.length; i++) {
                ProfileCharacter character = profile[i];
                character.addCharacter(sequence.charAt(i), 1);
            }
        }
        paddedSequences.put(sequenceNumber, sequence);

//        length = profile.length;
    }

    public void remove(Profile remove) {
        int size = length();
        assert(size == remove.length());
        for (int i = 0; i < size; i++) {
            profile[i].removeProfileCharacter(remove.profile[i]);
        }
        sequenceCount-= remove.sequenceCount;
        for (Integer sequenceNumber : remove.paddedSequences.keySet()) {
            paddedSequences.remove(sequenceNumber);
        }

        trim();

    }

    /* used after a sequence has been removed from a profile to remove profile characters
    that are all gap characters in the remaining sequences profiled.
    */
    private void trim() {
        int gapCount = 0;
        int count = 0;
        for (ProfileCharacter character : profile) {
            if(character.isAllGaps ())
                gapCount ++;
            else
                count ++;
        }
//        System.out.println("gaps =" + gapCount+ "," + count);
        if(gapCount== 0) return;
        ProfileCharacter characters[]=new ProfileCharacter[count];
        char [][] sequences = new char[sequenceCount][];
        char[][] newSequences = new char[sequenceCount][];
        int[] sequenceNumbers =new int[sequenceCount];
        for (int i = 0; i < sequenceCount; i++) {
            newSequences[i]=new char[count];
        }
        int i = 0;
        for (Map.Entry<Integer, String> entry : paddedSequences.entrySet()) {
            sequenceNumbers[i]= entry.getKey();
            sequences[i++] = entry.getValue ().toCharArray();
        }
        assert(i== sequenceCount);
        int index = 0;
        int sourceIndex = 0;
        for (ProfileCharacter character : profile) {
            if (character.isAllGaps()) {
                sourceIndex++;
                continue;
            }
            characters [ index  ] = character;
            for (int j = 0; j < sequenceCount; j++) {
                newSequences[j][ index ] = sequences [j][ sourceIndex ];
            }
            sourceIndex++;
            index ++;
        }
        for (int j = 0; j < sequenceCount; j++) {
            String sequence = new String(newSequences[j]);
            assert(sequence.length()== count);
            paddedSequences.put(sequenceNumbers[j], sequence);
        }
        profile= characters;
    }

    public static Profile combine(Profile profile1, Profile profile2, AlignmentResult result1, AlignmentResult result2) {
        int size = result1.size;
        int alphabetSize = Math.max(profile1.alphabetSize,profile2.alphabetSize);
        Profile result = new Profile(alphabetSize);
        result.profile = new ProfileCharacter[size];
        int index1= 0;
        int index2= 0;
        for (int i = 0; i < size; i++) {
            ProfileCharacter character = new ProfileCharacter(alphabetSize);
            if(result1.values[i]) {
                character.addProfileCharacter(profile1.profile[index1++]);
            }
            else {
                character.addGaps(profile1.sequenceCount);
            }
            if(result2.values[i]) {
                character.addProfileCharacter(profile2.profile[index2++]);
            }
            else {
                character.addGaps(profile2.sequenceCount);
            }
            result.profile[i]= character;
        }
        for (Map.Entry<Integer, String> entry : profile1.paddedSequences.entrySet()) {
            String sequence = entry.getValue();
            sequence = buildAlignmentString(sequence, result1);
            result.paddedSequences.put(entry.getKey(), sequence);
        }
        for (Map.Entry<Integer, String> entry : profile2.paddedSequences.entrySet()) {
            String sequence = entry.getValue();
            sequence = buildAlignmentString(sequence, result2);
            result.paddedSequences.put(entry.getKey(), sequence);
        }
        result.sequenceCount= profile1.sequenceCount + profile2.sequenceCount;
        assert(result.sequenceCount == result.paddedSequences.size());
//        result.length = size;
        return result;
    }

    public static int calculateAlphabetSize(String[] sequences) {
        int total = 0;
        boolean found[] = new boolean[127];
        for (String sequence : sequences) {
            for (char character : sequence.toCharArray()) {
                if(! found [ character ]) total ++;
                found [ character ] = true;
            }
        }
        return total;
    }

    public static String buildAlignmentString(String sequence, AlignmentResult result) {

        StringBuilder builder = new StringBuilder();
//        System.out.println("sequence =" + sequence);
//        result.print ();
//        if(true) return "";
        int index = 0;
        for (int i = 0; i < result.size; i++) {
            if(result.values[i]) {
                builder.append(sequence.charAt(index ++));
            }
            else {
                builder.append('-');
            }
        }
        assert(index == sequence.length());
        return builder.toString();
    }

    public void print(boolean displaySequences) {
        if(displaySequences) {
            int maximum = 0;
            for (int k = 0; k < paddedSequences.size(); k++) {
                String sequence = paddedSequences.get(k);
                maximum = Math.max(maximum, sequence.length ());
                System.out.println(sequence);
            }
            for (int i = 0; i < maximum; i++) {
                System.out.print(i % 10);
            }
        }
        System.out.println ();
        int count = 0;
        int index = 0;
        for (ProfileCharacter character : profile) {
            System.out.print(" " +(index ++) + ":");
            count +=character.print ();
            if(count> 800000) {
                count = 0;
                System.out.println ();
            }
        }
        System.out.println();
    }


    /**
     *
     * @param offset in the range 0 to n - 1
     * @param count number of characters to include
     * @return string of the characters starting at offset
     */
    public String toString(int offset, int count) {
        StringBuilder result =new StringBuilder();
        for (int i = 0; i < count; i++) {
            ProfileCharacter character = profile[ offset +i];
            result.append(character.toString());
        }
        return result.toString();
    }

    static String supportFreeEndGaps( String sequence) {
        char[] characters =sequence.toCharArray();
        int count = characters.length;
        int highestNonGapIndex = 0;
        int lowestNonGapIndex = count;
        for (int i = 0; i<count;i++) {
            if ( characters [i] !='-') {
                lowestNonGapIndex=i;
                break;
            }
        }
        for (int i = count-1; i >=0 ; i--) {
            if (characters[i] != '-') {
                highestNonGapIndex = i;
                break;
            }
        }
        StringBuilder result =new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            if (i<lowestNonGapIndex || i>highestNonGapIndex)
                result.append('_');
            else
                result.append (characters[i]);
        }
         return result.toString();
    }

    /**
     * Return a copy of this profile that supports free end gaps.
     * This means that gaps at either end of the sequence are represented as "_" instead of "-".
     * The score matrix used should have 0 cost for anything compared to "_".
     * @return a copy of this profile that supports free end gaps or
     * this  profile of that already supports free end gaps.
     */
    public Profile supportFreeEndGaps() {
        if (supportsFreeEndGaps) return this;
        if (sequenceCount<2) return this;
        Profile result =new Profile(alphabetSize+1);
        result.supportsFreeEndGaps=true;
        for (Map.Entry<Integer, String> entry : paddedSequences.entrySet()) {
             String sequence =entry.getValue();
             result.addSequence(entry.getKey(), sequence);
        }
        return result;
    }
}

package jade.data;

import java.io.*;
import java.util.*;

public class AlignmentReader {
    /** read from stream */
    public AlignmentReader(PushbackReader input)throws IOException {
        readFile(input);
    }
    
    /** read from file */
    public AlignmentReader(String file)throws IOException {
        PushbackReader input = openFile(file);
        readFile(input);
        input.close();
    }
    
    public Alignment getAlignment(){
        guessDataType();
        DataType dt = dataType;
        FinalAlignment = new Alignment(seqIDs,sequences, dt);
        return FinalAlignment;
    }
    private void guessDataType() {
        // count A, C, G, T, U, N
        long numNucs = 0;
        long numChars = 0;
        long numBins = 0;
        for (int i = 0; i < numSeqs; i++) {
            for (int j = 0; j < numSites; j++) {
                char c = getData(i, j);
                
                if (c == 'A' || c == 'C' || c == 'G' ||
                        c == 'T' || c == 'U' || c == 'N') numNucs++;
                
                if (c != '-' && c != '?') numChars++;
                
                if (c == '0' || c == '1') numBins++;
            }
        }
        
        if (numChars == 0) numChars = 1;
        
        // more than 85 % frequency advocates nucleotide data
        if ((double) numNucs / (double) numChars > 0.85) {
            dataType = new NucleotideDataType();
        } else if ((double) numBins / (double) numChars > 0.2) {
            dataType =  new BinaryDataType();
        }
    }
    // Implementation of abstract Alignment method

    /** sequence alignment at (sequence, site) */
    public char getData(int seq, int site) {
        return data[seq][site];
    }
    /** number of sequences */
    protected int numSeqs;
    
    /** length of each sequence */
    protected int numSites;
    
    /** sequence identifiers */
    protected ArrayList<String> seqIDs;
    protected ArrayList<String> sequences;
    
    /** data type */
    private DataType dataType;
    
    private int lineLength;
    //private Vector names, seqs, sites;
    
    //Alignment to send back in the getAlignment method
    private Alignment FinalAlignment;

    // Raw sequence alignment [sequence][site]
    private char[][] data = null;
    
//    private final boolean isType(PushbackReader in, String id) throws IOException {
//        
//        for (int i = 0; i < id.length(); i++)	{
//            int c = readNextChar(in);
//            if (c != id.charAt(i) )	{
//                in.unread(c);
//                return false;
//            }
//        }
//        return true;        
//    }
    
    private void readFile(PushbackReader in) throws IOException	{
        readPHYLIP(in);
        
        // Capitalize
        for (int i = 0; i < numSeqs; i++){
            for (int j = 0; j < numSites; j++){
                data[i][j] = Character.toUpperCase(data[i][j]);
            }
        }
        // Estimate data type
        //guessDataType();
        processSeqs();
    }
    private void processSeqs(){
        sequences = new ArrayList<String>();
        for(int i=0;i<numSeqs;i++){
            sequences.add("");
        }
        for(int i=0;i<numSeqs;i++){
            for(int j=0;j<numSites;j++){
                sequences.set(i, sequences.get(i)+data[i][j]);
            }
        }
    }
    private void readPHYLIP(PushbackReader in){
        
        int c, pos = 0, seq = 0;
        
        try {
            // Parse PHYLIP header line
            numSeqs = readInt(in);
            numSites = readInt(in);
            
            // Reserve memory
            seqIDs = new ArrayList<String>();
            for (seq = 0; seq < numSeqs; seq++) {seqIDs.add("");};
            data = new char[numSeqs][numSites];
            
            
            // Determine whether sequences are in INTERLEAVED
            // or in sequential format
            String header = readLine(in, false);
            
            boolean interleaved = true;
            
            if (header.length() > 0) {
                if (header.charAt(0) == 'S') {
                    interleaved = false;
                }
            }
            
            if (interleaved) // PHYLIP INTERLEAVED
            {
                //System.out.println("PHYLIP INTERLEAVED");
                
                
                // Reading data
                while (pos < numSites) {
                    // Go to next block
                    c = readNextChar(in);
                    in.unread(c);
                    
                    for (seq = 0; seq < numSeqs; seq++) {readSeqLineP(in, seq, pos, numSites);
                    }
                    pos += lineLength;
                }
            } else // PHYLIP SEQUENTIAL
            {
                //System.out.println("PHYLIP SEQUENTIAL");
                
                for (seq = 0; seq < numSeqs; seq++) {
                    // Go to next block
                    c = readNextChar(in);
                    in.unread(c);
                    
                    // Read label
                    seqIDs.set(seq,readLabel(in, 10));
                    
                    // Read sequences
                    for (pos = 0; pos < numSites; pos++) {
                        data[seq][pos] = (char) readNextChar(in);
                        
                        if (data[0][pos] == '.') {
                            if (seq == 0) {
                            } else {
                                data[seq][pos] = data[0][pos];
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {}
    }
    
    private void readSeqLineP(PushbackReader in, int s, int pos, int maxPos) throws IOException {
        
        if (pos == 0) {
            seqIDs.set(s,readLabel(in, 10));
        }
        
        if (s == 0) {
            String thisLine = readLine(in, false);
            
            if (thisLine.length() > maxPos - pos) {
                lineLength = maxPos - pos;
            } else {
                lineLength = thisLine.length();
            }
            
            for (int i = 0; i < lineLength; i++) {
                data[0][pos + i] = thisLine.charAt(i);
                if (data[0][pos + i] == '.') {

                }
            }
        } else {
            for (int i = 0; i < lineLength; i++) {
                data[s][pos + i] = (char) readNextChar(in);
                if (data[s][pos + i] == '.') {
                    data[s][pos + i] = data[0][pos + i];
                }
            }
            nextLine(in);
        }
    }
    private PushbackReader openFile(String name) throws FileNotFoundException {
        return new PushbackReader(new BufferedReader(new FileReader(name)));
    }
    private static boolean isWhite(int c) {
        return Character.isWhitespace((char) c);
    }
    
    private static boolean isNewlineCR(int c) {
        if (c == '\n' || c == 'r') {
            return true;
        } else {
            return false;
        }
    }
    /**
     * go to the beginning of the next line.
     * Recognized line terminators:
     * Unix: \n, DOS: \r\n, Macintosh: \r
     *
     * @param in input stream
     */
    public void nextLine(PushbackReader in)throws IOException {
        readLine(in, false);
    }
    
    /**
     * read a whole line
     *
     * @param in              input stream
     * @param keepWhiteSpace  keep or drop white space
     *
     * @return string with content of line
     */
    public String readLine(PushbackReader in, boolean keepWhiteSpace)throws IOException {
        StringBuffer buffer = new StringBuffer();
        
        int EOF = -1;
        int c;
        
        c = in.read();
        while (c != EOF && c != '\n' && c != '\r') {
            if (!isWhite(c) || keepWhiteSpace) {
                buffer.append((char) c);
            }
            c = in.read();
        }
        
        if (c == '\r') {
            c = in.read();
            if (c != '\n') {
                in.unread(c);
            }
        }
        
        return buffer.toString();
    }
    
    /**
     * go to first non-whitespace character
     *
     * @param in input stream
     *
     * @return character or EOF
     */
    public int skipWhiteSpace(PushbackReader in) throws IOException {
        int EOF = -1;
        int c;
        
        // search for first non-whitespace character
        do
        {
            c = in.read();
        }
        while (c != EOF && isWhite(c));
        
        return c;
    }
    
    /**
     * read next character from stream
     * (EOF does not count as character but will throw exception)
     *
     * @param input input stream
     *
     * @return character
     */
    public int readNextChar(PushbackReader input) throws IOException {
        int EOF = -1;
        
        int c = skipWhiteSpace(input);
        
        if (c == EOF) {
            new IOException("End of file/stream");
        }
        return c;
    }
    
    /**
     * read word from stream
     *
     * @param input stream
     *
     * @return word read from stream
     */
    public String readWord(PushbackReader in) throws IOException {
        StringBuffer buffer = new StringBuffer();
        
        int EOF = -1;
        int c;
        
        c = skipWhiteSpace(in);
        
        // search for last non-whitespace character
        while (c != EOF && !isWhite(c)) {
            buffer.append((char) c);
            c = in.read();
        }
        
        if (c != EOF) {
            in.unread(c);
        }
        
        return buffer.toString();
    }
    
    /**
     * read sequence label from stream
     *
     * A sequence label is not allowed to contain
     * whitespace and either of :,;()[]{}.  Note
     * that newline/cr is NOT counted as white space!!
     *
     * @param in input stream
     * @param maxLength maximum allowed length of label
     *        (if negative any length is permitted)
     *
     * @return label
     */
    public String readLabel(PushbackReader in, int maxLength)throws IOException {
        StringBuffer buffer = new StringBuffer();
        
        int EOF = -1;
        int c;
        //int len = 0;
        
        
        c = skipWhiteSpace(in);
        
        // search for last label character
        //while (c != EOF && buffer.length() != maxLength &&
        while (c != EOF &&
                !(
                (isWhite(c) && c != '\n' && c != '\r') ||
                c == ':' || c == ',' || c == ';' ||
                c == '(' || c == ')' ||
                c == '[' || c == ']' ||
                c == '{' || c == '}')) {
            // read over newline/cr
            if (c != '\n' && c != '\r') buffer.append((char) c);
            c = in.read();
        }
        
        if (c != EOF) {
            in.unread(c);
        }
        
        return buffer.toString();
    }
    
        /**
         * read next number from stream
         *
         * @param in input stream
         * @param ignoreNewlineCR  ignore newline/cr as separator
         *
         * @return number (as string)
         **/
    public String readNumber(PushbackReader in, boolean ignoreNewlineCR) throws IOException {
        StringBuffer buffer = new StringBuffer();
        
        int EOF = -1;
        int c;
        
        // search for first number character
        do
        {
            c = in.read();
        }
        while (c != EOF &&
                !(c == '-' || c == '.' || Character.isDigit((char) c)));
        
        // search for last number character
        while (c != EOF &&
                (c == '-' || c == '.' || c == 'e'
                || c == 'E' || Character.isDigit((char) c))
                || (isNewlineCR(c) && ignoreNewlineCR) ) {
            if (!(isNewlineCR(c) && ignoreNewlineCR))
                buffer.append((char) c);
            c = in.read();
        }
        
        if (c != EOF) {
            in.unread(c);
        }
        
        return buffer.toString();
    }
    
    /**
     * read next number from stream and convert it to a double
     * (newline/cr are treated as separators)
     *
     * @param in input stream
     *
     * @return double
     */
    public double readDouble(PushbackReader in)throws IOException, NumberFormatException {
        return readDouble(in, false);
    }
    
    /**
     * read next number from stream and convert it to a double
     *
     * @param in input stream
     * @param ignoreNewlineCR  ignore newline/cr as separator
     *
     * @return double
     */
    public double readDouble(PushbackReader in, boolean ignoreNewlineCR) throws IOException, NumberFormatException {
        String w = readNumber(in, ignoreNewlineCR);
        if (w.length() == 0) {
            throw new IOException("End of file/stream");
        }
        
        return Double.valueOf(w).doubleValue();
    }
    
    
    /**
     * read next number from stream and convert it to a int
     * (newline/cr are treated as separators)
     *
     * @param in input stream
     *
     * @return integer
     */
    public int readInt(PushbackReader in) throws IOException, NumberFormatException {
        return readInt(in, false);
    }
    
    /**
     * read next number from stream and convert it to a int
     *
     * @param in input stream
     * @param ignoreNewlineCR  ignore newline/cr as separator
     *
     * @return integer
     */
    public int readInt(PushbackReader in, boolean ignoreNewlineCR) throws IOException, NumberFormatException {
        String w = readNumber(in, ignoreNewlineCR);
        if (w.length() == 0) {
            throw new IOException("End of file/stream");
        }
        
        return Integer.valueOf(w).intValue();
    }
}
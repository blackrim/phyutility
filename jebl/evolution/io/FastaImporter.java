/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.Utils;
import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for importing Fasta sequential file format.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 * @version $Id: FastaImporter.java 704 2007-05-08 03:42:09Z matt_kearse $
 */
public class FastaImporter implements SequenceImporter, ImmediateSequenceImporter {

    /**
     * Name of Jebl sequence property which stores sequence description (i.e. anything after sequence name in fasta
     * file), so this data is available and an export to fasta can preserves the original data.
     * This is stored some attribute of the sequence  and of the taxon for backwards compatibility.
     * Generally, attributes on taxon should not be used, as they are unsafe
     * when dealing with objects that share the same taxon.
     */
    public static final String descriptionPropertyName = "description";

    private final ImportHelper helper;
    private final SequenceType sequenceType;
    private final boolean closeReaderAtEnd;

    private final Reader reader;
    private IllegalCharacterPolicy illegalCharacterPolicy = IllegalCharacterPolicy.abort;

    /**
     * Use this constructor if you are reading from a file. The advantage over the
     * other constructor is that a) the input size is known, so read() can report
     * meaningful progress, and b) the file is closed at the end.
     *
     * WARNING: You cannot reuse the FastaImporter thus constructed to import sequences
     * from the same file again. 
     *
     * @param file
     * @param sequenceType
     * @throws FileNotFoundException
     */
    public FastaImporter(File file, SequenceType sequenceType) throws FileNotFoundException {
        this(new BufferedReader(new FileReader(file)), sequenceType, true);
        helper.setExpectedInputLength(file.length());
    }

    public void setIllegalCharacterPolicy(IllegalCharacterPolicy newPolicy) {
        this.illegalCharacterPolicy = newPolicy;
    }

    /**
     * This constructor should normally never be needed because usually we
     * want to import from a file. Then, the constructor expecting a file
     * should be used. Therefore, this constructor is deprecated for now.
     * @param reader       holds sequences data
     * @param sequenceType pre specified sequences type. We should try and guess them some day.
     */
    @Deprecated
    public FastaImporter(Reader reader, SequenceType sequenceType) {
        this(reader, sequenceType, false);
    }

    private FastaImporter(final Reader reader, final SequenceType sequenceType, final boolean closeReaderAtEnd) {
        this.reader = reader;
        this.sequenceType = sequenceType;
        this.closeReaderAtEnd = closeReaderAtEnd;
        helper = new ImportHelper(reader);
        helper.setCommentDelimiters(';');
    }

    /**
     * @param callback Optional callback to report imported sequences to.
     * @param progressListener Listener to report progress to. Must not be null.
     * @return null if a callback was specified; otherwise, return list of sequences from file.
     * @throws IOException
     * @throws ImportException
     */
    private List<Sequence> read(ImmediateSequenceImporter.Callback callback,
                                ProgressListener progressListener)
            throws IOException, ImportException
    {
        final List<Sequence> sequences = (callback == null) ? new ArrayList<Sequence>() : null;
        final char fastaFirstChar = '>';
        final String fasta1stCharAsString = new String(new char[]{fastaFirstChar});
        final SequenceType seqtypeForGapsAndMissing = sequenceType != null ? sequenceType : SequenceType.NUCLEOTIDE;
        final AtomicReference<IllegalCharacterPolicy> illegalCharacterPolicyForThisImport
                = new AtomicReference<IllegalCharacterPolicy>(illegalCharacterPolicy);
        try {
            // find fasta line start
            while (helper.read() != fastaFirstChar) {
            }

            do {
                final String line = helper.readLine();
                final StringTokenizer tokenizer = new StringTokenizer(line, " \t");
                /*
                 * edited by me
                 */
                String name = ImportHelper.convertControlsChars(tokenizer.nextToken()).replace('-', '_');

                final String description = tokenizer.hasMoreElements() ?
                        ImportHelper.convertControlsChars(tokenizer.nextToken("")) : null;
                StringBuilder seq = new StringBuilder();


//                Runtime s_runtime = Runtime.getRuntime();
//                s_runtime.gc();
//                System.out.println("before read " + (s_runtime.totalMemory() - s_runtime.freeMemory())/1000 + " / " + s_runtime.totalMemory()/1000);

                /*
                 * We pass the StringBuilder in here instead of returning a string so
                 * that we need to hold the sequence string in memory only once. So
                 * althought this code seems a little unintuitive at first, please leave
                 * it that way :)
                 */
                helper.readSequence(seq, seqtypeForGapsAndMissing, fasta1stCharAsString, Integer.MAX_VALUE, "-", "?", "", null, progressListener);

//                s_runtime.gc();
//                System.out.println("after reading sequence " + (s_runtime.totalMemory() - s_runtime.freeMemory())/1000 + " / " + s_runtime.totalMemory()/1000);

                if (progressListener.isCanceled()) break;

                final Taxon taxon = Taxon.getTaxon(name);
                if (description != null && description.length() > 0) {
                    taxon.setAttribute(descriptionPropertyName, description);
                }

                // fixed guessSequenceType so it does not allocate anything
                // note: guessSequenceType can take a fair while if the sequence is long, but doesn't report
                // progress; ImportHelper has already reported all progress for this sequence complete when it
                // finished reading it, although really there is still some work to do...
                SequenceType type = ( sequenceType != null ) ? sequenceType : Utils.guessSequenceType(seq);

                // todo: We don't normally want to pop up dialogs in an importer, but I don't see
                // another clean way of handling this case. Note that the dialog only appears
                // if illegalCharacterPolicy has been set to a non-default value.
                if( type == null ) {
                    final String errorMessage = "Illegal sequence characters encountered on or before line " + helper.getLineNumber() + ".";
                    if (illegalCharacterPolicyForThisImport.get().equals(IllegalCharacterPolicy.askUser)) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    IllegalCharacterPolicy[] options = {IllegalCharacterPolicy.abort, IllegalCharacterPolicy.strip};
                                    int choice = JOptionPane.showOptionDialog(null,errorMessage +  " What do you want to do?", "Illegal characters in sequences",
                                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                                    illegalCharacterPolicyForThisImport.set(options[choice]);
                                }
                            });

                            if (illegalCharacterPolicyForThisImport.equals(IllegalCharacterPolicy.abort)) {
                                // user was presented warning and chose to abort -> abort without an exception
                                return sequences;
                            }
                        } catch (InterruptedException e) {
                            illegalCharacterPolicyForThisImport.set(IllegalCharacterPolicy.abort);
                        } catch (InvocationTargetException e) {
                            illegalCharacterPolicyForThisImport.set(IllegalCharacterPolicy.abort);
                        }
                    }
                    switch (illegalCharacterPolicyForThisImport.get()) {
                        case strip:
                            removeNonAminoAcidOrNucleotideCharacters(seq);
                            type = Utils.guessSequenceType(seq);
                            if (type==null) {
                                throw new ImportException("The file contains both residues that are only nucleotides (e.g. U) and residues that are only amino acids (e.g. E). You should import the file using file type 'Fasta (nucleotide)' or 'Fasta (amino acid)' instead of 'Fasta (auto-detect)'.");                                  
                            }
                            assert(type != null);
                            break;
                        default:
                            throw new ImportException(errorMessage);
                    }
                }

                // now we need more again
                BasicSequence sequence = new BasicSequence(type, taxon, seq);

                // get rid of memory used by the builder
                seq.setLength(0); seq.trimToSize(); // System.gc();

                if (description != null && description.length() > 0) {
                    sequence.setAttribute(descriptionPropertyName, description);
                }
                if( callback != null ) {
                    // this may use more memeory by getting the string from the jebl seq yet again
                    callback.add(sequence);
                } else {
                    sequences.add(sequence);
                }
                // helper.getProgress currently assumes each character to be
                // one byte long, but this should be ok for fasta files.
            } while (!progressListener.isCanceled() && (helper.getLastDelimiter() == fastaFirstChar));
        } catch (EOFException e) {
            // catch end of file the ugly way.
        } catch (NoSuchElementException e) {
            throw new ImportException("Incorrectly formatted fasta file (near line " + helper.getLineNumber() + ")");
        } finally {
            if (closeReaderAtEnd && reader != null) {
                reader.close();
            }
        }
        return sequences;
    }

    /**
     * @param sequence A nucleotide or amino acid sequence, possibly containing some illegal characters.
     * @return sequence with all characters that are neither a valid nucleotide nor amino acid symbol
     *         replaced.
     */
    static void removeNonAminoAcidOrNucleotideCharacters(StringBuilder sequence) {
        StringBuilder result = new StringBuilder();
        int writeIndex = 0;
        for (int readIndex = 0; readIndex < sequence.length(); readIndex++) {
            char c = sequence.charAt(readIndex);
            if (SequenceType.AMINO_ACID.getState(c)!= null || SequenceType.NUCLEOTIDE.getState(c) != null) {
                sequence.setCharAt(writeIndex, c);
                writeIndex++;
            }
        }
        sequence.setLength(writeIndex);
    }


    /**
     * @return sequences from file.
     * @throws IOException
     * @throws ImportException
     */
    public final List<Sequence> importSequences() throws IOException, ImportException {
        return read(null, ProgressListener.EMPTY);
    }

    public void importSequences(Callback callback, ProgressListener progressListener) throws IOException, ImportException {
        read(callback, progressListener);
    }
}

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for importing PHYLIP sequential file format
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: PhylipSequentialImporter.java 553 2006-12-04 21:46:56Z twobeers $
 */
public class PhylipSequentialImporter implements SequenceImporter {

    /**
     * Constructor
     */
    public PhylipSequentialImporter(Reader reader, SequenceType sequenceType, int maxNameLength) {
        helper = new ImportHelper(reader);

        this.sequenceType = sequenceType;
        this.maxNameLength = maxNameLength;
    }

    /**
     * importSequences.
     */
    public List<Sequence> importSequences() throws IOException, ImportException {

        List<Sequence> sequences = new ArrayList<Sequence>();

        try {

            int taxonCount = helper.readInteger();
            int siteCount = helper.readInteger();

            String firstSeq = null;

            for (int i = 0; i < taxonCount; i++) {
                StringBuilder name = new StringBuilder();

                char ch = helper.read();
                int n = 0;
                while (!Character.isWhitespace(ch) && (maxNameLength < 1 || n < maxNameLength)) {
                    name.append(ch);
                    ch = helper.read();
                    n++;
                }

                StringBuilder seq = new StringBuilder(siteCount);
                helper.readSequence(seq, sequenceType, "", siteCount, "-", "?", ".", firstSeq);

                if (firstSeq == null) {
                    firstSeq = seq.toString();
                }
                sequences.add(new BasicSequence(sequenceType, Taxon.getTaxon(name.toString()), seq.toString()));
            }
        } catch (EOFException e) {
        }

        return sequences;
    }

    private final ImportHelper helper;
    private final SequenceType sequenceType;
    private int maxNameLength = 10;
}

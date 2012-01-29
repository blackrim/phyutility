package jebl.evolution.align;

import jebl.evolution.align.scores.Blosum60;
import jebl.evolution.align.scores.NucleotideScores;
import jebl.evolution.align.scores.Scores;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.TreeBuilderFactory;
import jebl.evolution.trees.Utils;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Kearse
 * @version $Id: BartonSternberg.java 645 2007-03-02 01:29:18Z richardmoir $
 *
 * Implements the BartonSternberg multiple sequence alignment algorithm.
 *
 * Note: this is not yet complete, it does not create an initial ordering
 * in which to add sequences to the profile.
 *
 * Also, after creating the profile, it just removes and adds each sequence back into
 * the profile a fixed number of times(currently two).
 */
public class BartonSternberg implements MultipleAligner {

    Scores scores;
    NeedlemanWunschLinearSpaceAffine aligner;
    private int refinementIterations;
    private float gapOpen,gapExtend;
    private boolean freeGapsAtEnds;
    private boolean fastGuide;
    // if not null, scores are from estimate
    private Scores origScores = null;

    private void establishScores(Scores scores) {
        this.scores = scores;
        this.scores = Scores.includeGaps(scores, -gapExtend, 0);
        aligner = new NeedlemanWunschLinearSpaceAffine(this.scores, gapOpen, gapExtend, freeGapsAtEnds);
    }

    public Scores getEstimatedScores() {
        return origScores != null ? scores : null;
    }

    public BartonSternberg(Scores scores, float gapOpen, float gapExtend, int refinementIterations,
                           boolean freeGapsAtEnds, boolean fastGuide) {
//        if (true) throw new RuntimeException("testing");
       this.gapOpen = gapOpen;
       this.gapExtend = gapExtend;
       this.freeGapsAtEnds = freeGapsAtEnds;

        this.fastGuide = fastGuide;

        this.refinementIterations = refinementIterations;
        establishScores(scores);
    }

    CompoundAlignmentProgressListener compoundProgress;
    /*private ProgressListener progress;
    private boolean cancelled = false;
    private int sectionsCompleted;
    private int totalSections;
    ProgressListener minorProgress = new ProgressListener() {
        public boolean setProgress(double fractionCompleted) {
            double totalProgress = (sectionsCompleted + fractionCompleted)/totalSections;
            if(progress.setProgress(totalProgress)) cancelled = true;
            return cancelled;
        }
    };*/

    // on entry from top (non recursive), compoundProgress should have allocated #tips - 1 utins of work

    private Profile align(RootedTree tree, Node node, List<Sequence> seqs,
                          CompoundAlignmentProgressListener compoundProgress) {
        if( tree.isExternal(node) ) {
            final Taxon tax = tree.getTaxon(node);
            final int iSeq = Integer.parseInt(tax.getName());

            final Profile profile = new Profile(scores.getAlphabet().length());
            profile.addSequence(iSeq, seqs.get(iSeq).getString());
            return profile;
        }

        List<Node> children = tree.getChildren(node);                            assert( children.size() == 2 );
        final Profile left = align(tree, children.get(0), seqs, compoundProgress);
        if( compoundProgress.isCanceled() ) return null;

        final Profile right = align(tree, children.get(1), seqs, compoundProgress);
        if( compoundProgress.isCanceled() ) return null;

        compoundProgress.setSectionSize(1);
        AlignmentResult results[] = aligner.doAlignment(left, right, compoundProgress.getMinorProgress(), false);
        compoundProgress.incrementSectionsCompleted(1);
        if(compoundProgress.isCanceled()) return null;
        return Profile.combine(left, right, results[0], results[1]);
    }


    /**
     *
     * @param sourceSequences
     * @param progress
     * @param refineOnly if specified, then the input sequences are assumed to be aligned already,
     * and this function will only refine the alignment.
     */
    public String[] align(List<Sequence> sourceSequences, ProgressListener progress, boolean refineOnly,
                          boolean estimateMatchMismatchCosts) {
        if( origScores != null ) {
            establishScores(origScores);
        }

        final int count = sourceSequences.size();

        Profile[] sequenceProfilesWithoutGaps = new Profile[count];
        String[] sequencesWithoutGaps = new String[count];
        for (int i = 0; i < count; i++) {
            sequencesWithoutGaps[i] = Align.strip(sourceSequences.get(i).getString(), scores.getAlphabet(), false);
            sequenceProfilesWithoutGaps[i] = new Profile(i, sequencesWithoutGaps[i]);
        }

        int treeWork = refineOnly ? 0 : (fastGuide ? count : count*(count - 1)/2);
        int alignmentWork = refineOnly ? 0 : count - 1;
        int refinementWork = count * refinementIterations;

        compoundProgress = new CompoundAlignmentProgressListener(progress,treeWork + refinementWork + alignmentWork);

        Profile profile = null;
        if( refineOnly ) {
            String[] sequencesWithGaps = new String[count];
            for (int i = 0; i < count; i++) {
                sequencesWithGaps[i] = Align.strip(sourceSequences.get(i).getString(), scores.getAlphabet(), true);

            }
            profile = new Profile(Profile.calculateAlphabetSize(sequencesWithGaps));
            for (int i = 0; i < count; i++) {
                assert(sequencesWithGaps[i].length() == sequencesWithGaps [0].length ());
                profile.addSequence(i, sequencesWithGaps[i]);
            }
        } else {
            List<Sequence> sequencesForGuideTree = new ArrayList<Sequence>(sourceSequences.size());
            for (int i = 0; i < count; i++) {
                Sequence s = sourceSequences.get(i);
                sequencesForGuideTree.add(new BasicSequence(s.getSequenceType(), Taxon.getTaxon("" + i), sequencesWithoutGaps[i]));
            }
            compoundProgress.setSectionSize(treeWork);
            // We want a binary rooted tree

            //long start = System.currentTimeMillis();
            final boolean estimateMatchCost = estimateMatchMismatchCosts && scores instanceof NucleotideScores;

            final AlignmentTreeBuilderFactory.Result unrootedGuideTree =
                    fastGuide ?
                            AlignmentTreeBuilderFactory.build(sequencesForGuideTree, TreeBuilderFactory.Method.NEIGHBOR_JOINING,
                                    this, compoundProgress.getMinorProgress()) :
                            AlignmentTreeBuilderFactory.build(sequencesForGuideTree, TreeBuilderFactory.Method.NEIGHBOR_JOINING,
                                    aligner, compoundProgress.getMinorProgress());
            if (compoundProgress.isCanceled()) return null;
            //long duration = System.currentTimeMillis() - start;
            //System.out.println("took " + duration +  " for " + (fastGuide ? " fast" : "normal") + " guide tree");

            RootedTree guideTree = Utils.rootTreeAtCenter(unrootedGuideTree.tree);
            compoundProgress.incrementSectionsCompleted(treeWork);

            if( estimateMatchCost ) {
                final DistanceMatrix distanceMat = unrootedGuideTree.distance;
                final double[][] distances = distanceMat.getDistances();
                double sum = 0.0;
                final int n = distances.length;
                for(int k = 0; k < n; ++k) {
                    for(int j = k+1; j < n; ++j) {
                        // ignore infinity and high values
                        sum += Math.min(5.0, distances[k][j]);
                    }
                }
                final double avg = sum / ((n * (n - 1)/2));

                final double percentmatches = 1 - (3.0/4.0) * (1 - Math.exp(-4.0 * avg / 3.0));

                origScores = scores;
                final NucleotideScores nucleotideScores = new NucleotideScores(scores, percentmatches);
                establishScores(nucleotideScores);
            }
            progress.setMessage("Building alignment");
            profile = align(guideTree, guideTree.getRootNode(), sequencesForGuideTree, compoundProgress);
            if (compoundProgress.isCanceled()) return null;
        }

        //now remove a single sequence, and we
        for (int j = 0; j < refinementIterations; j++) {
            String message = "Refining alignment";
            if(refinementIterations> 1) {
                message = message + " (iteration " +(j+1) + " of " + refinementIterations+ ")";
            }
            progress.setMessage(message);
            for (int i = 0; i < count; ++i) {
//                if(j> 0&& i!= 8) continue;
//                Profile sequenceProfile = sequenceProfiles[i];
                boolean display = false;

                String sequence = profile.getSequence(i);
                if(j>= 0 && i== 8) {
//                    display = true;
                }
                if(display) {
                    System.out.println("remove sequence =" + sequence);
                    profile.print (true);
                }
                Profile sequenceProfile = new Profile(i, sequence);
                profile.remove(sequenceProfile);
//                aligner.setDebug(display);

                AlignmentResult results[] = aligner.doAlignment(profile, sequenceProfilesWithoutGaps[i], compoundProgress.getMinorProgress(), false);
//                aligner.setDebug(false);
                if (compoundProgress.isCanceled()) return null;
                compoundProgress.incrementSectionsCompleted(1);
                if(display){
                    profile.print(false);

                    System.out.println("result =" + results[0].size + "," + results[1].size + " from " + profile.length() + "," + sequenceProfile.length());
                }
                profile = Profile.combine(profile, sequenceProfilesWithoutGaps[i], results[0], results[1]);
                if(display) {
                    profile.print(true);
                }
            }
        }

        String[] results = new String[count];
        for (int i = 0; i < count; i++) {
            results[i]= profile.getSequence(i);
        }
        return results;
    }

    public static void main(String[] arguments) throws IOException, ImportException {
        File file = new File(arguments[0]);
        SequenceType sequenceType = SequenceType.AMINO_ACID;

        FastaImporter importer = new FastaImporter(file, sequenceType);
        List<Sequence> xsequences = importer.importSequences();
        List<String> sequenceStrings = new ArrayList<String>();
        int count = 0;
        int maximum = 10;
        for (Sequence sequence : xsequences) {
            BasicSequence basic = (BasicSequence) sequence;
            String string = basic.getCleanString();
            sequenceStrings.add(string);
            System.out.println(string);
            if(count++ >= maximum) break;
        }
        System.out.println ();
        count = 0;
        for (Sequence sequence : xsequences) {
            BasicSequence basic = (BasicSequence) sequence;
            String string = basic.getString();
            System.out.println(string);
            if (count++ >= maximum) break;
        }
        long start = System.currentTimeMillis();
        BartonSternberg alignment = new BartonSternberg( new Blosum60(), 20, 1, 2, true, false);
        String[] sequences = sequenceStrings.toArray(new String[0]);
        System.out.println("aligning " + sequences.length);
        String results[] = alignment.align(xsequences, null, false, false);
        for (String result : results) {
            System.out.println(result);
        }
        System.out.println ("took " +(System.currentTimeMillis() - start) + " milliseconds");
    }

    public Alignment doAlign(List<Sequence> seqs, RootedTree guideTree, ProgressListener progress) {
        final int count = seqs.size();
        final CompoundAlignmentProgressListener p = new CompoundAlignmentProgressListener(progress, count - 1);

        Profile profile = align(guideTree, guideTree.getRootNode(), seqs, p);
        if (p.isCanceled()) {
            return null;
        }

        List<Sequence> aSeqs = new ArrayList<Sequence>(count);
        for (int i = 0; i < count; i++) {
            String seq = profile.getSequence(i);
            final Sequence s = seqs.get(i);
            aSeqs.add(new BasicSequence(s.getSequenceType(), s.getTaxon(), seq));
        }
        return new BasicAlignment(aSeqs);
    }


    public Alignment doAlign(Alignment a1, Alignment a2, ProgressListener progress) {
        List<Sequence> seqs1 = a1.getSequenceList();
        List<Sequence> seqs2 = a2.getSequenceList();

        final int size1 = seqs1.size();
        final int size2 = seqs2.size();

        final Profile profile1 = new Profile(a1, scores.getAlphabet().length());
        final Profile profile2 = new Profile(a2, scores.getAlphabet().length(), size1);

        AlignmentResult results[] = aligner.doAlignment(profile1, profile2, progress, false);
        if (progress.isCanceled()) {
            return null;
        }
        Profile profile = Profile.combine(profile1, profile2, results[0], results[1]);

        final int count = size1 + size2;
        List<Sequence> aSeqs = new ArrayList<Sequence>(count);
        for (int i = 0; i < count; i++) {
            final String seq = profile.getSequence(i);
            final Sequence s = (i < size1) ? seqs1.get(i) : seqs2.get(i - size1);
            aSeqs.add(new BasicSequence(s.getSequenceType(), s.getTaxon(), seq));
        }
        return new BasicAlignment(aSeqs);
    }

    public Alignment doAlign(Alignment alignment, Sequence sequence, ProgressListener progress) {

        for (Sequence seq : alignment.getSequenceList()) {
            if (seq.getTaxon().getName().equals(sequence.getTaxon().getName())) {
                throw new IllegalArgumentException("Sequence taxon " + sequence.getTaxon().getName() + " appears in alignment and sequence.");
            }
        }

        final Profile aprofile = new Profile(alignment, scores.getAlphabet().length(),1);

        final Profile sprofile = new Profile(scores.getAlphabet().length());
        sprofile.addSequence(0, sequence.getString());

        AlignmentResult results[] = aligner.doAlignment(aprofile, sprofile, progress, false);
        Profile profile = Profile.combine(aprofile, sprofile, results[0], results[1]);

        List<Sequence> seqs1 = alignment.getSequenceList();
        List<Sequence> seqs2 = new ArrayList<Sequence>(); seqs2.add(sequence);

        final int size1 = seqs1.size();
        final int size2 = seqs2.size();

        final int count = size1 + size2;
        List<Sequence> aSeqs = new ArrayList<Sequence>(count);
        for (int i = 0; i < count; i++) {
            final String seq = profile.getSequence(i);
            final Sequence s = (i < count-1) ? seqs1.get(i) : seqs2.get(0);
            aSeqs.add(new BasicSequence(s.getSequenceType(), s.getTaxon(), seq));
        }
        return new BasicAlignment(aSeqs);
    }

    public double getScore() {
        return aligner.getScore();
    }
}

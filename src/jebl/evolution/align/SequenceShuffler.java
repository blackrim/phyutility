package jebl.evolution.align;

import javax.swing.*;
import java.util.Random;

/**
 * Shuffles a sequence and aligns it again multiple times to give mean and variance of
 * alignments on random sequences.
 * 
 * @author Richard Moir
 * @author Alexei Drummond
 *
 * @version $Id: SequenceShuffler.java 185 2006-01-23 23:03:18Z rambaut $
 */
public class SequenceShuffler {

    private float max, min;
    private float mean;			//mean score for the set of shuffled alignments
    private float variance;		//variance of the scores for the set of shuffled alignments

    private ProgressMonitor monitor = null;

    public SequenceShuffler() {}

    public void shuffle(Align algorithm, String sq1, String sq2, final int numShuffles) {

        if (monitor != null) {
            monitor.setMinimum(0);
            monitor.setMaximum(numShuffles);
        }

        float[] scores = new float[numShuffles];
        float sumScores = 0;
        for(int i = 0; i < numShuffles; i++) {
            String shuffled2 = shuffleSeq(sq2);
            algorithm.doAlignment(sq1,shuffled2);
            scores[i] = algorithm.getScore();
            sumScores += algorithm.getScore();
            final int j = i;
            if (monitor != null) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        monitor.setProgress(j);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }
        if (monitor != null) {
            monitor.setProgress(monitor.getMaximum());
        }

        mean = sumScores / numShuffles;
        min = Float.MAX_VALUE;
        max = -Float.MAX_VALUE;
        float sqDiffSum = 0;
        for(int i = 0; i < numShuffles; i++) {
            sqDiffSum += Math.pow(scores[i] - mean, 2);
            if (scores[i] < min) min = scores[i];
            if (scores[i] > max) max = scores[i];
        }
        variance = sqDiffSum / numShuffles;
    }

    /**
     * Note: not to sure how good this shuffling algorithm is.
     *
     * @param s string to shuffle
     * @return shuffled string
     */
    private String shuffleSeq(String s) {
        char[] a = s.toCharArray();
        char swap;
        for (int i = 0; i < a.length-1; i++) {
            int r = random.nextInt(a.length-i-1) + 1;
            swap = a[r];
            a[r] = a[i];
            a[i] = swap;
        }
        return String.valueOf(a);
    }

    /**
     *
     * @return the mean score of the shuffled alignments.
     */
    public float getMean() {
        return mean;
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    /**
     *
     * @return the standard deviation of scores for the shuffled alignments.
     */
    public double getStdev() {
        return Math.sqrt(variance);
    }

    public void setProgressMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    private static Random random = new Random();
}
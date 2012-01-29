package jebl.evolution.alignments;

import jebl.math.Random;

/**
 * Date: 15/01/2006
 * Time: 10:13:50
 *
 * @author Joseph Heled
 * @version $Id: BootstrappedAlignment.java 585 2006-12-15 15:48:59Z twobeers $
 *
 */
public class BootstrappedAlignment extends ResampledAlignment {

    public BootstrappedAlignment(Alignment srcAlignment) {
        final int nSites = srcAlignment.getSiteCount();
        int[] sites = new int[nSites];

        for(int n = 0; n < nSites; ++n) {
            sites[n] = Random.nextInt(nSites);
        }

        init(srcAlignment, sites);
    }
}

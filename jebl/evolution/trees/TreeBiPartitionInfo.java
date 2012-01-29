package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.FixedBitSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Work in progress
 * @author Joseph Heled
 * @version $Id: TreeBiPartitionInfo.java 531 2006-11-17 00:59:01Z pepster $
 */
public class TreeBiPartitionInfo {
    class BiPartiotionInfo {
        BiPartiotionInfo(Node n) {
            this.n = n;
        }

        Node n;
        public boolean has;
    }

    final List<Taxon> taxa;
    final RootedTree t;
    final int        nTips;
    HashMap<FixedBitSet, BiPartiotionInfo> all;

    public TreeBiPartitionInfo(RootedTree t, List<Taxon> taxa) {
        this.t = t;
        this.taxa = taxa;
        nTips = t.getExternalNodes().size();
        all = new HashMap<FixedBitSet, BiPartiotionInfo>();
        forNode(t.getRootNode());
    }


//    BiPartiotionInfo forNode(Node n) {
//        final BiPartiotionInfo p = new BiPartiotionInfo(n);
//        if( t.isExternal(n) ) {
//            final int pos = taxa.indexOf(t.getTaxon(n));
//            p.partition.set(pos);
//
//        } else {
//
//            for( Node c : t.getChildren(n) ) {
//                final TreeBiPartitionInfo.BiPartiotionInfo info = forNode(c);
//                p.partition.union(info.partition);
//            }
//        }
//        if( ! p.partition.contains(0)  ) {
//            p.partition.complement();
//        }
//        all.put(p.partition, n);
//        return p;
//    }
//

    private FixedBitSet forNode(Node n) {
        final FixedBitSet p = new FixedBitSet(nTips);
        if( t.isExternal(n) ) {
            final int pos = taxa.indexOf(t.getTaxon(n));
            p.set(pos);

        } else {

            for( Node c : t.getChildren(n) ) {
                final FixedBitSet info = forNode(c);
                p.union(p);
            }
        }
        if( t.getParent(n) != t.getRootNode() && ! p.contains(0)  ) {
            p.complement();
        }
        all.put(p, new BiPartiotionInfo(n));
        return p;
    }

    public enum DistanceNorm {
        NORM1,
        NORM2
    }

    public static double distance(TreeBiPartitionInfo t1, TreeBiPartitionInfo t2, DistanceNorm norm) {
        double d = 0.0;
        for( BiPartiotionInfo k : t2.all.values() ) {
            k.has = false;
        }
        double din = 0;
        double dout = 0;

        for( Map.Entry<FixedBitSet, BiPartiotionInfo> k : t1.all.entrySet() ) {
            final BiPartiotionInfo info = t2.all.get(k.getKey());
            final double b1 = t1.t.getLength(k.getValue().n);
            double dif;
            if( info != null ) {

                final double b2 = t2.t.getLength(info.n);
                info.has = true;

                dif = Math.abs(b1 - b2);
            } else {
                dif = b1;
            }
            if( norm == DistanceNorm.NORM1 ) {
                //d += dif;
                din += dif;
            } else {
                //d += dif * dif;
                din += dif * dif;
            }
        }

        for( BiPartiotionInfo info : t2.all.values() ) {
            if( !info.has ) {
                final double dif = t2.t.getLength(info.n);
                if( norm == DistanceNorm.NORM1 ) {
                    //d += dif;
                    dout += dif;
                } else {
                    //d += dif * dif;
                    dout += dif * dif;
                }
            }
        }
        d = din + dout;
        return ( norm == DistanceNorm.NORM1 ) ? d : Math.sqrt(d);
    }
}

package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.util.Attributable;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;

/**
 * Export sequences and trees to Nexus format.
 *
 * @author Joseph Heled
 *
 * @version $Id: NexusExporter.java 730 2007-06-17 22:11:34Z matt_kearse $
 */

public class NexusExporter implements AlignmentExporter, SequenceExporter, TreeExporter {
    /**
     *
     * @param writer where export text goes
     */
    public NexusExporter(Writer writer) {
        this.writer = new PrintWriter(writer);
        this.writer.println("#NEXUS");
    }

    /**
     * exportAlignment.
     */
    public void exportAlignment(Alignment alignment) throws IOException {
    	exportSequences(alignment.getSequences());
    }

    /**
     * export alignment.
     */
    public void exportSequences(Collection<? extends Sequence> sequences) throws IOException, IllegalArgumentException {

        establishSequenceTaxa(sequences);

        SequenceType seqType = null;

        int maxLength = 0;
        for (Sequence sequence : sequences) {
            if (sequence.getLength() > maxLength) {
                maxLength = sequence.getLength();
            }
            if (seqType == null) {
                seqType = sequence.getSequenceType();
            } else if( seqType != sequence.getSequenceType() ) {
               throw new IllegalArgumentException("All seqeuences must have the same type");
            }
        }

        writer.println("begin characters;");
        writer.println("\tdimensions nchar=" + maxLength + ";");
        if( seqType != null ) {
            writer.println("\tformat datatype=" + seqType.getNexusDataType() +
                    " missing=" + seqType.getUnknownState().getName() +
                    " gap=" + seqType.getGapState().getCode() + ";");

            writer.println("\tmatrix");
            for (Sequence sequence : sequences) {


                if( sequence.getSequenceType() != seqType ) {
                    throw new IllegalArgumentException("SequenceTypes of sequences in collection do not match");
                }
                StringBuilder builder = new StringBuilder("\t");
                appendTaxonName(sequence.getTaxon(), builder);
                builder.append("\t").append(sequence.getString());
                int shortBy = maxLength - sequence.getLength();
                if (shortBy > 0) {
                    for (int i = 0; i < shortBy; i++) {
                        builder.append(seqType.getGapState().getCode());
                    }
                }
                writer.println(builder);
            }
            writer.println(";\nend;");
        }
    }

    /**
     * Export a single tree
     *
     * @param tree
     * @throws java.io.IOException
     */
    public void exportTree(Tree tree) throws IOException {
        List<Tree> trees = new ArrayList<Tree>();
        trees.add(tree);
        exportTrees(trees);
    }

    private void writeTrees(Collection<? extends Tree> trees, boolean checkTaxa) throws IOException {

        int nt = 0;
        for( Tree t : trees ) {
            if( checkTaxa && establishTreeTaxa(t) ) {
                throw new IllegalArgumentException();
            }
            final boolean isRooted = t instanceof RootedTree;
            final RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);

            final Object name = t.getAttribute(treeNameAttributeKey);

            ++nt;
            final String treeName = (name != null) ? name.toString() : "tree_" + nt;

            StringBuilder builder = new StringBuilder("\ttree [&");

            // TREE & UTREE are depreciated in the NEXUS format in favour of a metacomment
            // [&U] or [&R] after the TREE command. Andrew.
            builder.append(isRooted && !rtree.conceptuallyUnrooted() ? "r] " : "u] ");
            builder.append(treeName);
            builder.append(" = ");

            appendAttributes(rtree, treeNameAttributeKey, builder);

            appendTree(rtree, rtree.getRootNode(), builder);
            builder.append(";");

            writer.println(builder);
        }
    }
    /**
     * export trees
     */
    public void exportTrees(Collection<? extends Tree> trees) throws IOException {
        // all trees in a set should have the same taxa
        establishTreeTaxa(trees.iterator().next());
        writer.println("begin trees;");
        writeTrees(trees, true);
        writer.println("end;");
    }

    public void exportTreesWithTranslation(Collection<? extends Tree> trees, Map<String, String> t) throws IOException {
        writer.println("begin trees;");
        writer.println("\ttranslate");
        boolean first = true;
        for( Map.Entry<String, String> e : t.entrySet() ) {
            writer.print((first ? "" : ",\n") + "\t\t" + safeName(e.getKey()) + " " + safeName(e.getValue()));
            first = false;
        }
        writer.println("\n\t;");

        writeTrees(trees, false);
        writer.println("end;");
    }

    public void exportMatrix(final DistanceMatrix distanceMatrix) {
        final List<Taxon> taxa = distanceMatrix.getTaxa();
        establishTaxa(taxa);
        writer.println("begin distances;");
        // assume distance matrix is symetric, so save upper part. no method to guarantee this yet
        final double[][] distances = distanceMatrix.getDistances();
        writer.println(" format triangle = upper nodiagonal;");
        writer.println(" matrix ");
        for(int i = 0; i < taxa.size(); ++i) {
            StringBuilder builder = new StringBuilder("\t");
            appendTaxonName(taxa.get(i), builder);
            for(int j = i+1; j < taxa.size(); ++j) {
                builder.append(" ");
                builder.append(distances[i][j]);
            }
            writer.println(builder);
        }
        writer.println(";");
        writer.println("end;");
    }

    /**
     * Write a new taxa block and record them for later reference.
     * @param taxons
     */
    private void setTaxa(Taxon[] taxons) {
        taxa = new HashSet<Taxon>();

        writer.println("begin taxa;");
        writer.println("\tdimensions ntax=" + taxons.length + ";");
        writer.println("\ttaxlabels");

        for (Taxon taxon : taxons) {
            taxa.add(taxon);

            StringBuilder builder = new StringBuilder("\t");
            appendTaxonName(taxon, builder);
            appendAttributes(taxon, null, builder);
            writer.println(builder);
        }
        writer.println(";\nend;\n");
    }

    final private String nameRegex = "^(\\w|-)+$";

    /**
     * Name suitable as token - quotes if necessary
     * @param name to check
     * @return the name
     */
    private String safeName(String name) {
        // allow dash in names

        if (!name.matches(nameRegex)) {
            name = name.replace("\'", "\'\'");
            return "\'" + name + "\'";
        }
        return name;
    }

    /**
     * name suitable for printing - quotes if necessary
     * @param taxon
     * @param builder
     * @return
     */
    private StringBuilder appendTaxonName(Taxon taxon, StringBuilder builder) {
        String name = taxon.getName();
        if (!name.matches(nameRegex)) {
            // JEBL way of quoting the quote character
            name = name.replace("\'", "\'\'");
            builder.append("\'").append(name).append("\'");
            return builder;
        }
        return builder.append(name);
    }

    /**
     * Prepare for writing an alignment. If a taxa block exists and is suitable for alignment,
     * do nothing. If not, write a new taxa block.
     * @param sequences
     */
    private void establishSequenceTaxa(Collection<? extends Sequence> sequences) {
        if( taxa != null && taxa.size() == sequences.size() ) {
            boolean hasAll = true;
            for( Sequence s : sequences ) {
                if( taxa.contains(s.getTaxon()) ) {
                    hasAll = false;
                    break;
                }
            }
            if( hasAll ) {
                return;
            }
        }

        List<Taxon> t = new ArrayList<Taxon>(sequences.size());
        for (Sequence sequence : sequences) {
            t.add(sequence.getTaxon());
        }
        setTaxa(t.toArray(new Taxon[]{}));
    }

    private boolean establishTreeTaxa(Tree tree) {
        return establishTaxa(tree.getTaxa());
    }

    private boolean establishTaxa(Collection<? extends Taxon> ntaxa) {
        if( taxa != null && taxa.size() == ntaxa.size()  && taxa.containsAll(ntaxa)) {
            return false;
        }

        setTaxa(ntaxa.toArray(new Taxon[]{}));
        return true;
    }

    /**
     * Prepare for writing a tree. If a taxa block exists and is suitable for tree,
     * do nothing. If not, write a new taxa block.
     * @param tree
     * @param node
     * @param builder
     */
    private void appendTree(RootedTree tree, Node node, StringBuilder builder) {
        if (tree.isExternal(node)) {
            appendTaxonName(tree.getTaxon(node), builder);

            appendAttributes(node, null, builder);

            if( tree.hasLengths() ) {
                builder.append(':');
                builder.append(tree.getLength(node));
            }
        } else {
            builder.append('(');
            List<Node> children = tree.getChildren(node);
            final int last = children.size() - 1;
            for (int i = 0; i < children.size(); i++) {
                appendTree(tree, children.get(i), builder);
                builder.append(i == last ? ')' : ',');
            }

            appendAttributes(node, null, builder);

            Node parent = tree.getParent(node);
            // Don't write root length. This is ignored elsewhere and the nexus importer fails
            // whet it is present.
            if (parent != null) {
                if (tree.hasLengths()) {
                    builder.append(":").append(tree.getLength(node));
                }
            }
        }
    }

    private StringBuilder appendAttributes(Attributable item, String excludeKey, StringBuilder builder) {
        boolean first = true;
        for( String key : item.getAttributeNames() ) {
            // we should replace the explicit check for name by something more general.
            // Like a reserved character at the start (here &). however we have to worry about backward
            // compatibility so no change yet with name.
            if( (excludeKey == null || !key.equals(excludeKey)) && !key.startsWith("&") ) {
                if (first) {
                    builder.append("[&");
                    first = false;
                } else {
                    builder.append(",");
                }

                if( key.indexOf(' ') < 0 ) {
                    builder.append(key);
                } else {
                    builder.append("\"").append(key).append("\"");
                }

                builder.append('=');

                Object value = item.getAttribute(key);
                appendAttributeValue(value, builder);
            }
        }
        if (!first) {
            builder.append("]");
        }

        return builder;
    }

    private StringBuilder appendAttributeValue(Object value, StringBuilder builder) {
        if (value instanceof Object[]) {
            builder.append("{");
            Object[] elements = ((Object[])value);

            if (elements.length > 0) {
                appendAttributeValue(elements[0], builder);
                for (int i = 1; i < elements.length; i++) {
                    builder.append(",");
                    appendAttributeValue(elements[i], builder);
                }
            }
            return builder.append("}");
        }

        if (value instanceof Color) {
            return builder.append("#").append(((Color)value).getRGB());
        }

        if (value instanceof String) {
            return builder.append("\"").append(value).append("\"");
        }

        return builder.append(value);
    }

    static public String treeNameAttributeKey = "name";

    static public boolean isGeneratedTreeName(String name) {
        return name != null && name.matches("tree_[0-9]+");
    }

    private Set<Taxon> taxa = null;
    protected final PrintWriter writer;
}

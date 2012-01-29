package jebl.evolution.io;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.evolution.trees.RootedTree;

import java.io.Writer;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.Collection;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: NewickExporter.java 429 2006-08-26 18:17:39Z rambaut $
 */
public class NewickExporter implements TreeExporter {
    public NewickExporter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Export a single tree
     *
     * @param tree
     * @throws java.io.IOException
     */
    public void exportTree(Tree tree) throws IOException {
        writeTree(tree);
    }

    /**
     * Export a collection of trees
     *
     * @param trees
     * @throws java.io.IOException
     */
    public void exportTrees(Collection<? extends Tree> trees) throws IOException {
        for (Tree tree : trees) {
        	writeTreeMine(tree);
        }
    }

    private void writeTree(Tree tree) throws IOException {
        writer.write(Utils.toNewick(Utils.rootTheTree(tree)));
    }

    /*
     * edit for FileWriter
     */
    private void writeTreeMine(Tree tree) throws IOException {
        writer.write(Utils.toNewick(Utils.rootTheTree(tree))+";\n");
    }
    
    private final Writer writer;
}

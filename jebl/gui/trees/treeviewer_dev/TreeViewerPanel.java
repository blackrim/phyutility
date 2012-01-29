package jebl.gui.trees.treeviewer_dev;

import jebl.gui.trees.treeviewer_dev.painters.*;
import jebl.evolution.io.NewickImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpalettes.BasicControlPalette;
import org.virion.jam.controlpalettes.ControlPalette;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;

/**
 * This is a panel that has a TreeViewer and a BasicControlPalette with
 * the default Controllers and Painters.
 *
 * @author Andrew Rambaut
 * @version $Id: TreeViewerPanel.java 536 2006-11-21 16:10:24Z rambaut $
 */
public class TreeViewerPanel extends JPanel {

    public TreeViewerPanel(TreeViewer treeViewer, ControlPalette controlPalette) {

        this.treeViewer = treeViewer;
        this.controlPalette = controlPalette;

        controlPalette.getPanel().setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        controlPalette.getPanel().setBackground(new Color(231, 237, 246));
        controlPalette.getPanel().setOpaque(true);

        controlPalette.addController(new TreeViewerController(treeViewer));

        controlPalette.addController(new TreeAppearanceController(treeViewer));

        controlPalette.addController(new TreesController(treeViewer));

        // Create a tip label painter and its controller
        BasicLabelPainter tipLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.TIP);
        controlPalette.addController(new LabelPainterController("Tip Labels", "tipLabels", tipLabelPainter));
        treeViewer.setTipLabelPainter(tipLabelPainter);

        // Create a node label painter and its controller
        BasicLabelPainter nodeLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.NODE);
        nodeLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Node Labels", "nodeLabels", nodeLabelPainter));
        treeViewer.setNodeLabelPainter(nodeLabelPainter);

        // Create a node shape painter and its controller
        NodeBarPainter nodeBarPainter = new NodeBarPainter();
        nodeBarPainter.setForeground(new Color(24, 32, 228, 128));
        nodeBarPainter.setVisible(false);
        controlPalette.addController(new NodeBarController("Node Bars", nodeBarPainter));
        treeViewer.setNodeBarPainter(nodeBarPainter);

        // Create a branch label painter and its controller
        BasicLabelPainter branchLabelPainter = new BasicLabelPainter(BasicLabelPainter.PainterIntent.BRANCH);
        branchLabelPainter.setVisible(false);
        controlPalette.addController(new LabelPainterController("Branch Labels", "branchLabels", branchLabelPainter));
        treeViewer.setBranchLabelPainter(branchLabelPainter);

        // Create a scale bar painter and its controller
        ScaleBarPainter scaleBarPainter = new ScaleBarPainter();
        controlPalette.addController(new ScaleBarPainterController(scaleBarPainter));
        treeViewer.setScaleBarPainter(scaleBarPainter);

        /*
         * testing
         */
     // Create a node shape painter and its controller
        NodeHistPainter nodeHistPainter = new NodeHistPainter();
        nodeHistPainter.setForeground(new Color(24, 32, 228, 128));
        nodeHistPainter.setVisible(false);
        controlPalette.addController(new NodeHistController("Node Hists", nodeHistPainter));
        treeViewer.setNodeHistPainter(nodeHistPainter);
        
        /*
         * end testing
         */
        setLayout(new BorderLayout());

        add(treeViewer, BorderLayout.CENTER);
        add(controlPalette.getPanel(), BorderLayout.WEST);

    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public ControlPalette getControlPalette() {
        return controlPalette;
    }

    private final TreeViewer treeViewer;
    private final ControlPalette controlPalette;

    static public void main(String[] args) {

        JFrame frame = new JFrame("TreeViewer Test");

        TreeViewer treeViewer = new DefaultTreeViewer();
        ControlPalette controlPalette = new BasicControlPalette(200, BasicControlPalette.DisplayMode.ONLY_ONE_OPEN);

        frame.getContentPane().add(new TreeViewerPanel(treeViewer, controlPalette), BorderLayout.CENTER);

        try {
            File inputFile = null;

            if (args.length > 0) {
                inputFile = new File(args[0]);
            }

            if (inputFile == null) {
                // No input file name was given so throw up a dialog box...
                java.awt.FileDialog chooser = new java.awt.FileDialog(frame, "Select NEXUS Tree File",
                        java.awt.FileDialog.LOAD);
                chooser.setVisible(true);
                inputFile = new java.io.File(chooser.getDirectory(), chooser.getFile());
                chooser.dispose();
            }

            if (inputFile == null) {
                throw new RuntimeException("No file specified");
            }

//        TreeImporter importer = new NewickImporter(new FileReader(inputFile));
            Reader reader = new BufferedReader(new FileReader(inputFile));
            TreeImporter importer = new NewickImporter(reader, false);
            java.util.List<Tree> trees = importer.importTrees();
            reader.close();
            treeViewer.setTrees(trees);
        } catch (Exception ie) {
            ie.printStackTrace();
            System.exit(1);
        }

        frame.setSize(640, 480);
        frame.setVisible(true);
    }
}

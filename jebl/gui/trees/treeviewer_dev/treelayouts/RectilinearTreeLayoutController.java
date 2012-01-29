package jebl.gui.trees.treeviewer_dev.treelayouts;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: RectilinearTreeLayoutController.java 485 2006-10-25 15:24:54Z rambaut $
 */
public class RectilinearTreeLayoutController extends AbstractController {

    private static final String RECTILINEAR_LAYOUT_KEY = "rectilinearLayout";

    private static final String ROOT_LENGTH_KEY = "rootLength";
    private static final String CURVATURE_KEY = "curvature";
    private static final String ALIGN_TIP_LABELS_KEY = "alignTipLabels";

    public RectilinearTreeLayoutController(final RectilinearTreeLayout treeLayout) {
        this.treeLayout = treeLayout;

        titleLabel = new JLabel("Rectangular Layout");
        optionsPanel = new OptionsPanel();

        final int sliderMax = 10000;
        rootLengthSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
        rootLengthSlider.setOpaque(false);
        rootLengthSlider.setValue((int) (treeLayout.getRootLength() * sliderMax));
        //rootLengthSlider.setMajorTickSpacing(rootLengthSlider.getMaximum() / 5);
//        rootLengthSlider.setPaintTicks(true);

        rootLengthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = rootLengthSlider.getValue();
                treeLayout.setRootLength(value / sliderMax);
            }
        });
        optionsPanel.addComponentWithLabel("Root Length:", rootLengthSlider, true);

        curvatureSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
        curvatureSlider.setOpaque(false);
        curvatureSlider.setValue((int) (treeLayout.getCurvature() * sliderMax));
        //curvatureSlider.setMajorTickSpacing(curvatureSlider.getMaximum() / 5);
  //      curvatureSlider.setPaintTicks(true);

        curvatureSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                double value = curvatureSlider.getValue();
                treeLayout.setCurvature(value / sliderMax);
            }
        });
        optionsPanel.addComponentWithLabel("Curvature:", curvatureSlider, true);

        alignTipLabelsCheck = new JCheckBox("Align Tip Labels");
        alignTipLabelsCheck.setOpaque(false);

        alignTipLabelsCheck.setSelected(treeLayout.isAlignTipLabels());
        alignTipLabelsCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                treeLayout.setAlignTipLabels(alignTipLabelsCheck.isSelected());
            }
        });
        optionsPanel.addComponent(alignTipLabelsCheck);
    }

    public JComponent getTitleComponent() {
        return titleLabel;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
        rootLengthSlider.setValue((Integer) settings.get(RECTILINEAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY));
        curvatureSlider.setValue((Integer) settings.get(RECTILINEAR_LAYOUT_KEY + "." + CURVATURE_KEY));
        alignTipLabelsCheck.setSelected((Boolean) settings.get(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY));
    }

    public void getSettings(Map<String, Object> settings) {
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ROOT_LENGTH_KEY, rootLengthSlider.getValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + CURVATURE_KEY, curvatureSlider.getValue());
        settings.put(RECTILINEAR_LAYOUT_KEY + "." + ALIGN_TIP_LABELS_KEY, alignTipLabelsCheck.isSelected());
    }

    private final JLabel titleLabel;
    private final OptionsPanel optionsPanel;

    private final JSlider rootLengthSlider;
    private final JSlider curvatureSlider;
    private final JCheckBox alignTipLabelsCheck;

    private final RectilinearTreeLayout treeLayout;

}

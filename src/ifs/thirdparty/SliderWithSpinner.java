package ifs.thirdparty;

import com.alee.laf.slider.WebSlider;

import java.awt.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * This class represents a slider with a spinner. The spinner and the slider
 * are synchronized: user can use either of them to adjust the value in the text
 * field. When the value in the editor is modified using the spinner, the
 * state of the slider is changed accordingly.
 *
 * @author Jevgeny Jonas
 * --this version butchered for this program
 */
public final class SliderWithSpinner extends JPanel {

    private final JSpinner spinner;
    public final WebSlider slider;
    public final SliderWithSpinnerModel model;
    private final int orientation;

	/*
	 * MVC in Swing: Models
	 *
	 * JSpinner uses SpinnerModel, JSlider uses BoundedRangeModel.
	 * The first idea was to create a class that would implement both
	 * interfaces. But it turned out to be impossible because of these methods:
	 *
	 *		int BoundedRangeModel.getValue()
	 *		Object SpinnerModel.getValue()
	 *
	 * Instead, an inner class is used. Thus, the spinner and the slider
	 * "contain a reference to one main model with data"
	 * (http://www.developer.com/java/ent/article.php/10933_3336761_2/Creating-Interactive-GUIs-with-Swings-MVC-Architecture.htm)
	 *
	 * When user adjusts a component (the slider or the spinner), this component
	 * updates the model and sends a notification to all change listeners. The
	 * model then notifies the spinner and the slider. The spinner then updates
	 * the editor and also sends a notification to its change listeners. The
	 * slider also sends a notification to its change listeners.
	 *
	 * The SliderWithSpinner registers its change listeners as the spinner's
	 * change listeners, but not as the slider's change listeners. This helps
	 * ensure that the listeners of class SliderWithSpinner will not receive
	 * redundant notifications when user adjusts the slider or the spinner.
	 *
	 */

    /**
     * Creates a new slider with spinner which uses the supplied model.
     *
     * @param model non-null pointer (ownership: callee)
    */

    public SliderWithSpinner(SliderWithSpinnerModel model) {
        boolean drawLabels=false;
        int orientation = SwingConstants.HORIZONTAL;

        this.model = model;
        this.orientation = orientation;

        setAlignmentY(Component.TOP_ALIGNMENT);

        // using inner objects as models
        spinner = createSpinner(model.getSpinnerModel());
        slider = createSlider(model.getBoundedRangeModel(), orientation, drawLabels);

        if (orientation == SwingConstants.VERTICAL) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(slider);
            add(spinner);
        }
        else {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(spinner);
            add(slider);
        }

        this.setPreferredSize(new Dimension(20, 30));
        this.setMinimumSize(new Dimension(20, 30));
        this.setMaximumSize(new Dimension(20, 30));
    }



    public int getValue(){
        return this.model.getValue();
    }

    /**
     * Changes the current value of the slider with spinner.
     * @param value the new value (must be in range [minvalue..maxvalue])
     */
    public void setValue(int value) {
        assert model.getBoundedRangeModel().getMinimum() <= value && value <= model.getBoundedRangeModel().getMaximum();
        model.getBoundedRangeModel().setValue(value);
//		model.getSpinnerModel().setValue(value);
    }

    /**
     * Adds a listener to the list that is notified each time the spinner with
     * slider changes its current value.
     *
     * @param listener the <code>ChangeListener</code> to add
     */
    public void addChangeListener(ChangeListener listener) {
        spinner.addChangeListener(listener);
    }

    /**
     * Removes a <code>ChangeListener</code> from this spinner.
     *
     * @param listener the <code>ChangeListener</code> to remove
     */
    public void removeChangeListener(ChangeListener listener) {
        spinner.removeChangeListener(listener);
    }

    private static WebSlider createSlider(BoundedRangeModel model, int orientation, boolean drawLabels) {
        WebSlider slider = new WebSlider(model);

        slider.setOrientation(orientation);
        slider.setAlignmentY(Component.TOP_ALIGNMENT);

        if (drawLabels) {
            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put(new Integer((Integer) model.getMinimum()), new JLabel(((Integer) model.getMinimum()).toString()));
            labelTable.put(new Integer((Integer) model.getMaximum()), new JLabel(((Integer) model.getMaximum()).toString()));
            slider.setLabelTable(labelTable);
            slider.setPaintLabels(true);
        }

//		slider.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));

        return slider;
    }

    private static JSpinner createSpinner(SpinnerModel model) {

        JSpinner spinner = new JSpinner(model);

        JFormattedTextField hor_ftf = ((JSpinner.DefaultEditor) (spinner.getEditor())).getTextField();
        hor_ftf.setEditable(true);

        return spinner;
    }
}
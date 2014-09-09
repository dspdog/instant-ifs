package ifs.thirdparty;

import javax.swing.event.*;
import javax.swing.*;

/**
 * This class represents the model for the {@link SliderWithSpinner}.
 * The model's state is defined by the minimal, the maximal and the current position.
 *
 * @author Jevgeny Jonas
 */
public class SliderWithSpinnerModel {

    private BoundedRangeModel boundedRangeModel;
    private SpinnerModel spinnerModel = new SpinnerModelImpl();

    private final class SpinnerModelImpl implements SpinnerModel {
        private SpinnerModelImpl() { }
        public Object getValue() { return boundedRangeModel.getValue(); }
        public void setValue(Object value) { boundedRangeModel.setValue((Integer) value); }
        public Object getNextValue() { return boundedRangeModel.getValue() + 1; }
        public Object getPreviousValue() { return boundedRangeModel.getValue() - 1; }
        public void addChangeListener(ChangeListener l) { boundedRangeModel.addChangeListener(l); }
        public void removeChangeListener(ChangeListener l) { boundedRangeModel.removeChangeListener(l); }
    }

    /**
     * Constructs a new model. Precondition: <code>minimum &lt;= value &lt;= maximum</code>.
     */
    public SliderWithSpinnerModel(int value, int minimum, int maximum) {
        assert minimum <= value && value <= maximum;
        boundedRangeModel = new DefaultBoundedRangeModel(value, 0, minimum, maximum);
    }

    SpinnerModel getSpinnerModel() {
        return spinnerModel;
    }

    BoundedRangeModel getBoundedRangeModel() {
        return boundedRangeModel;
    }

    /**
     * Returns the current position of the slider and the spinner.
     */
    public int getValue() {
        return boundedRangeModel.getValue();
    }
}
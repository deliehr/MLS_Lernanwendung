package Comprehensive;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import it.liehr.mls_app.R;

/**
 * Extends the button widget.
 * Used for the selection of assessments as a category button on the left side.
 * Stores, if already pressed. So the user can disable the selected source.
 */
public class ExtendedToggleButton extends ToggleButton {
    // region object variables
    private boolean alreadyPressed = false;
    // endregion

    // region constructors

    public ExtendedToggleButton(Context context) {
        super(context);
    }

    public ExtendedToggleButton(Context context, String buttonText) {
        super(context);

        Activity activity = (Activity) context;

        /*
        this.setText(buttonText + ": " + activity.getString(R.string.label_off));
        this.setTextOn(buttonText + ": " + activity.getString(R.string.label_on));
        this.setTextOff(buttonText + ": " + activity.getString(R.string.label_off));
        */

        this.setText(buttonText);
        this.setTextOn(buttonText);
        this.setTextOff(buttonText);
    }

    public ExtendedToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // endregion



    // region getters & setters

    public boolean isAlreadyPressed() {
        return alreadyPressed;
    }

    public void setAlreadyPressed(boolean alreadyPressed) {
        this.alreadyPressed = alreadyPressed;
    }

    // endregion
}
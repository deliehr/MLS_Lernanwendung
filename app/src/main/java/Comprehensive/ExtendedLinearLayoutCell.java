package Comprehensive;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * Extensions for the Linear Layout widget.
 * Acts as a cell in a Table Row view.
 * Stores the related correct identifier for drag'n'drop purporses.
 * When the user pressed the check solution buttion, the correct identifier stored in the dragged button, will be compared with the identifier stored in this linear layout extension.
 */
public class ExtendedLinearLayoutCell extends LinearLayout {
    private String identifier = null;

    public ExtendedLinearLayoutCell(Context context) {
        super(context);
    }

    public ExtendedLinearLayoutCell(Context context, String identifier) {
        super(context);
        this.setIdentifier(identifier);
    }

    // region getters & setters

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    // endregion
}

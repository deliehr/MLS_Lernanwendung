package Comprehensive;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Extension of the EditText gui widget.
 * Contains additional string-value field (hiddenValue), which the user must type in.
 * The response of the user (the typed in value) will be compared (via Levenshtein distance) with this additional field.
 */
public class ExtendedEditText extends AppCompatEditText {
    // region object variables
    private String hiddenValue = "";
    // endregion

    // region constructors
    public ExtendedEditText(Context context) {
        super(context);
    }
    // endregion

    // region getters & setters

    public String getHiddenValue() {
        return hiddenValue;
    }

    public void setHiddenValue(String hiddenValue) {
        this.hiddenValue = hiddenValue;
    }

    // endregion
}

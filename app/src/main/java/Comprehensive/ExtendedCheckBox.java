package Comprehensive;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;

import Components.Assessment;

public class ExtendedCheckBox extends AppCompatCheckBox {
    // region object variables
    private String identifier = "";
    private Assessment assessmentObject = null;
    // endregion

    // region constructors
    public ExtendedCheckBox(Context context) {
        super(context);
    }

    // endregion

    // region getters and setters

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Assessment getAssessmentObject() {
        return assessmentObject;
    }

    public void setAssessmentObject(Assessment assessmentObject) {
        this.assessmentObject = assessmentObject;
    }

    // endregion
}

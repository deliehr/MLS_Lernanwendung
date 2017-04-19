package Comprehensive;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;

import Components.Assessment;
import Components.Support;

public class ExtendedButton extends AppCompatButton {
    // region object variables
    private String identifier = "";
    private Assessment assessmentObject = null;
    // endregion

    // region constructors
    public ExtendedButton(Context context) {
        super(context);
    }

    public ExtendedButton(Context context, String identifier, Assessment assessmentObject) {
        super(context);
        this.setIdentifier(identifier);
        this.setAssessmentObject(assessmentObject);
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

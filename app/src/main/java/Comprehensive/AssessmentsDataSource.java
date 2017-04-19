package Comprehensive;

import java.util.ArrayList;
import java.util.List;

import Components.Assessment;

/**
 * Class for representing a source mapping to assessments.
 * Used in ActivitySelection: One source (for example Button "All assessments" as source), have multiple assessments.
 */
public class AssessmentsDataSource {
    // region object variables
    String sourceName = "";
    List<Assessment> relatedAssessments = new ArrayList<Assessment>();  // related to source
    // endregion

    // region constructors
    public AssessmentsDataSource(String sourceName) {
        this.setSourceName(sourceName);
    }
    // endregion

    // region object methods
    public void addAssessment(Assessment assessment) {
        if(assessment != null) {
            this.relatedAssessments.add(assessment);
        }
    }
    // endregion

    // region getters & setters


    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public List<Assessment> getRelatedAssessments() {
        return relatedAssessments;
    }

    public void setRelatedAssessments(List<Assessment> relatedAssessments) {
        this.relatedAssessments = relatedAssessments;
    }

    // endregion
}

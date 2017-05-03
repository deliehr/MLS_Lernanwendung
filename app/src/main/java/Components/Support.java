package Components;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for supporting elements
 *
 * @author Dominik Liehr
 * @version 0.01
 */
public abstract class Support {
    // region object variables
    private String uuid;
    private String assessmentUuid;
    private int creationTimestamp;
    private String identifier;
    // endregion

    // region getter & setter

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String id) {
        this.uuid = id;
    }

    public String getAssessmentUuid() {
        return assessmentUuid;
    }

    public void setAssessmentUuid(String assessmentUuid) {
        this.assessmentUuid = assessmentUuid;
    }

    public int getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(int creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    // endregion
}
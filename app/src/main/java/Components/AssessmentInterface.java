package Components;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import Comprehensive.UsersAssessmentResponse;

/**
 * Interface for assessments
 *
 * @author Dominik Liehr
 * @version 0.01
 */
public interface AssessmentInterface {
    // region object methods
    void displayAssessment(Context context, LinearLayout targetLayout);
    void handleUserResponse(View view);
    void handleUserResponse(UsersAssessmentResponse response, View clickedView);
    void completeStatisticEntry(UsersAssessmentResponse response);
    // endregion

}

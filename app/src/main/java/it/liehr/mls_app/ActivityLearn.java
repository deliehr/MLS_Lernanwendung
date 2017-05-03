package it.liehr.mls_app;

import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Comprehensive.App;
import Components.Assessment;
import Components.DragAssessment;
import Components.HotspotAssessment;
import Components.MultipleChoiceAssessment;
import Components.SingleChoiceAssessment;
import Components.TableAssessment;
import Comprehensive.DatabaseHelper;

import static Comprehensive.App.convertDpToPx;

public class ActivityLearn extends AppCompatActivity {
    // region object variables
    public List<Assessment> assessments = new ArrayList<Assessment>();
    String[] assessmentIds;
    private int currentAssessmentIndex = 0;
    public static int assessmentsToProcess = 0;
    // endregion

    // region object methods
    public void loadAssessments() {
        // load from database
        DatabaseHelper helper = new DatabaseHelper(this);

        // iterate through ids
        for(String id:this.assessmentIds) {
            try {
                // get assessment type
                String assessmentIdentifier = helper.getAssessmentIdentifierById(Long.valueOf(id));

                // id
                long aId = Long.valueOf(id);

                // distinguish between types
                switch (assessmentIdentifier) {
                    case "choice": {
                        SingleChoiceAssessment singleChoiceAssessment = helper.getSingleChoiceAssessment(aId);
                        singleChoiceAssessment.setContext(this);
                        this.assessments.add(singleChoiceAssessment);
                        break;
                    }
                    case "choiceMultiple": {
                        MultipleChoiceAssessment multipleChoiceAssessment = helper.getMultipleChoiceAssessment(aId);
                        multipleChoiceAssessment.setContext(this);
                        this.assessments.add(multipleChoiceAssessment);
                        break;
                    }
                    case "positionObjects": {
                        HotspotAssessment hotspotAssessment = helper.getHotspotAssessment(aId);
                        hotspotAssessment.setContext(this);
                        this.assessments.add(hotspotAssessment);
                        break;
                    }
                    case "table": {
                        TableAssessment tableAssessment = helper.getTableAssessment(aId);
                        tableAssessment.setContext(this);
                        this.assessments.add(tableAssessment);
                        break;
                    }
                    case "dragndropTable": {
                        DragAssessment dragAssessment = helper.getDragAssessment(aId);
                        dragAssessment.setContext(this);
                        this.assessments.add(dragAssessment);
                        break;
                    }
                }
            } catch (SQLException se) {
                Log.e("Error", "SQL Error in Activity Learn: " + se.getMessage());
                Log.e("Error", "SQL Error in Activity Learn: (File / Class / Method / Linenumber): " + se.getStackTrace()[0].getFileName() + " / " + se.getStackTrace()[0].getClassName() + " / " + se.getStackTrace()[0].getMethodName() + " / " + se.getStackTrace()[0].getLineNumber());
            } catch (Exception e) {
                Log.e("Error", "Error in Activity Learn: " + e.getMessage());
                Log.e("Error", "Error in Activity Learn: (File / Class / Method / Linenumber): " + e.getStackTrace()[0].getFileName() + " / " + e.getStackTrace()[0].getClassName() + " / " + e.getStackTrace()[0].getMethodName() + " / " + e.getStackTrace()[0].getLineNumber());
            }
        }
    }
    // endregion

    // region button onclick methods
    public void btnPrevAssessmentOnClick(View view) {
        // check next index position
        if (this.currentAssessmentIndex - 1 >= 0) {
            // on next prev position is an assessment
            if(this.currentAssessmentIndex - 1 == 0) {
                // next assessment will be the first
                this.findViewById(R.id.btnPrevAssessment).setEnabled(false);
                this.findViewById(R.id.btnPrevAssessmentBottom).setEnabled(false);
            }

            this.currentAssessmentIndex -= 1;
            this.findViewById(R.id.btnNextAssessment).setEnabled(true);
            this.findViewById(R.id.btnNextAssessmentBottom).setEnabled(true);
            this.displayAssessment(this.currentAssessmentIndex);
            //this.assessments.get(this.currentAssessmentIndex).setHowSolved(UsersAssessmentResponse.Wrong);
            ((LinearLayout) this.findViewById(R.id.layoutAssessmentHandling)).removeAllViews();

        } else {
            // already reached end
        }

        // check index == 0
        if (this.currentAssessmentIndex == 0) {
            Button prevButton = (Button) this.findViewById(R.id.btnPrevAssessment);
            Button prevButtonBottom = (Button) this.findViewById(R.id.btnPrevAssessmentBottom);
            prevButton.setEnabled(false);
            prevButtonBottom.setEnabled(false);
        }
    }

    public void btnNextAssessmentOnClick(View view) {
        // check next index position
        if(this.currentAssessmentIndex + 1 <= this.assessments.size()-1) {
            // on next position is an assessment
            if(this.currentAssessmentIndex + 1 == this.assessments.size()-1) {
                // next position is last assessment
                this.findViewById(R.id.btnNextAssessment).setEnabled(false);
                this.findViewById(R.id.btnNextAssessmentBottom).setEnabled(false);
            }

            this.currentAssessmentIndex += 1;
            this.findViewById(R.id.btnPrevAssessment).setEnabled(true);
            this.findViewById(R.id.btnPrevAssessmentBottom).setEnabled(true);
            this.displayAssessment(this.currentAssessmentIndex);
            //this.assessments.get(this.currentAssessmentIndex).setHowSolved(UsersAssessmentResponse.Wrong);
            ((LinearLayout) this.findViewById(R.id.layoutAssessmentHandling)).removeAllViews();

        } else {
            // already reached end
        }

        // check prev index position
        if(this.currentAssessmentIndex + 1 >= 1) {
            Button prevButton = (Button) this.findViewById(R.id.btnPrevAssessment);
            Button prevButtonBottom = (Button) this.findViewById(R.id.btnPrevAssessmentBottom);
            prevButton.setEnabled(true);
            prevButtonBottom.setEnabled(true);
        }

        // check index == 0
        if(this.currentAssessmentIndex == 0) {
            Button prevButton = (Button) this.findViewById(R.id.btnPrevAssessment);
            Button prevButtonBottom = (Button) this.findViewById(R.id.btnPrevAssessmentBottom);
            prevButton.setEnabled(false);
            prevButtonBottom.setEnabled(false);
        }
    }
    // endregion

    private void displayAssessment(int index) {
        // check if index is in range
        if(!(currentAssessmentIndex >= 0 && currentAssessmentIndex <= assessments.size()-1)) {
            // not in range
            currentAssessmentIndex = 0;
        }

        // index dot
        GradientDrawable dotBackground = new GradientDrawable();
        dotBackground.setShape(GradientDrawable.RECTANGLE);
        dotBackground.setStroke(1, Color.rgb(0, 0, 0));
        dotBackground.setColor(Color.rgb(255, 255, 255));

        GradientDrawable dotCurrentBackground = new GradientDrawable();
        dotCurrentBackground.setShape(GradientDrawable.RECTANGLE);
        dotCurrentBackground.setStroke(1, Color.rgb(0, 0, 0));
        dotCurrentBackground.setColor(Color.rgb(89, 166, 238));

        LinearLayout linearLayoutAssessmentIndex = (LinearLayout) findViewById(R.id.linearLayoutAssessmentIndex);
        LinearLayout linearLayoutAssessmentIndexBottom = (LinearLayout) findViewById(R.id.linearLayoutAssessmentIndexBottom);
        for(int i=0;i < linearLayoutAssessmentIndex.getChildCount();i++) {
            if(i == currentAssessmentIndex) {
                TextView tv = (TextView) linearLayoutAssessmentIndex.getChildAt(i);
                tv.setId(View.generateViewId());
                tv.setBackground(dotCurrentBackground);

                TextView tvBottom = (TextView) linearLayoutAssessmentIndexBottom.getChildAt(i);
                tvBottom.setId(View.generateViewId());
                tvBottom.setBackground(dotCurrentBackground);
            } else {
                TextView tv = (TextView) linearLayoutAssessmentIndex.getChildAt(i);
                tv.setId(View.generateViewId());
                tv.setBackground(dotBackground);

                TextView tvBottom = (TextView) linearLayoutAssessmentIndexBottom.getChildAt(i);
                tvBottom.setId(View.generateViewId());
                tvBottom.setBackground(dotBackground);
            }
        }

        String assessmentType = this.assessments.get(index).getIdentifier();
        LinearLayout targetLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutAssessmentContent);
        switch (assessmentType) {
            case "choice": ((SingleChoiceAssessment) this.assessments.get(index)).displayAssessment(this, targetLinearLayout); break;
            case "choiceMultiple": ((MultipleChoiceAssessment) this.assessments.get(index)).displayAssessment(this, targetLinearLayout); break;
            case "positionObjects": ((HotspotAssessment) this.assessments.get(index)).displayAssessment(this, targetLinearLayout); break;
            case "table": ((TableAssessment) this.assessments.get(index)).displayAssessment(this, targetLinearLayout); break;
            case "dragndropTable": ((DragAssessment) this.assessments.get(index)).displayAssessment(this, targetLinearLayout); break;
        }
    }

    private void showAssessmentIndex() {
        // clear views
        ((LinearLayout) findViewById(R.id.linearLayoutAssessmentIndex)).removeAllViews();
        ((LinearLayout) findViewById(R.id.linearLayoutAssessmentIndexBottom)).removeAllViews();

        if(this.assessments.size() >= 1) {
            // dot background
            GradientDrawable dotBackground = new GradientDrawable();
            dotBackground.setShape(GradientDrawable.RECTANGLE);
            dotBackground.setStroke(1, Color.rgb(0, 0, 0));
            dotBackground.setColor(Color.rgb(255, 255, 255));

            // dot layout params
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int dotSize = 30;
            int dotMargin = 9;
            dotParams.width = convertDpToPx(this, dotSize);
            dotParams.height = convertDpToPx(this, dotSize);
            dotParams.setMargins(convertDpToPx(this, (dotMargin/2)), convertDpToPx(this, dotMargin), convertDpToPx(this, (dotMargin/2)), convertDpToPx(this, dotMargin));

            // show dots
            for(int i=0;i < this.assessments.size();i++) {
                TextView dotTextView = new TextView(this);
                dotTextView.setBackground(dotBackground);
                dotTextView.setLayoutParams(dotParams);
                ((LinearLayout) findViewById(R.id.linearLayoutAssessmentIndex)).addView(dotTextView);

                TextView dotTextViewBottom = new TextView(this);
                dotTextViewBottom.setBackground(dotBackground);
                dotTextViewBottom.setLayoutParams(dotParams);
                ((LinearLayout) findViewById(R.id.linearLayoutAssessmentIndexBottom)).addView(dotTextViewBottom);
            }
        }
    }

    // region override methods
    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // get assessment ids
        Intent intent = getIntent();
        String ids = intent.getStringExtra("assessment_ids");

        // seperate ids
        this.assessmentIds = ids.split(";");

        // load assessments
        this.loadAssessments();

        // assessments existing?
        if(this.assessments.size() > 1) {
            findViewById(R.id.btnNextAssessment).setEnabled(true);
            findViewById(R.id.btnNextAssessmentBottom).setEnabled(true);
        }

        // asssessment dots
        this.showAssessmentIndex();

        // show first assessment
        this.displayAssessment(0);

        // show start info summary
        ActivityLearn.assessmentsToProcess = this.assessments.size();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // region menu (dot points)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_learn_assessments, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSelection:
                this.finish();
                return true;

            case R.id.menuItemExitApplication:
                this.finishAffinity();
                return true;

            case R.id.menuItemMainMenu:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // endregion
    // endregion
}

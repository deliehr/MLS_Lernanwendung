package it.liehr.mls_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import Comprehensive.App;
import Components.Assessment;
import Comprehensive.AssessmentsDataSource;
import Comprehensive.ExtendedToggleButton;
import Comprehensive.DatabaseHelper;
import Comprehensive.DatabaseHelper.TableNames;
import Comprehensive.DatabaseHelper.ForeignKeyNames;
import Comprehensive.ExtendedOnClickListener;

public class ActivitySelection extends AppCompatActivity {
    // region class variables
    public static Context mContext;
    public static ActivitySelection mActivitySelectionObject;
    // endregion

    // region object variables
    private List<String> availableTags = new ArrayList<String>();
    private List<String> selectedTags = new ArrayList<String>();
    private List<Assessment> assessmentsToLearn = new ArrayList<Assessment>();
    private List<AssessmentsDataSource> assessmentsSources = new ArrayList<AssessmentsDataSource>();
    // endregion

    // region onclick listeners
    private View.OnClickListener availableTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Button clickedButton = (Button) view;
                ActivitySelection.mActivitySelectionObject.moveTagToSelected(clickedButton);
                ActivitySelection.mActivitySelectionObject.calculateAssessmentSet();
            } catch (Exception e) {
                Log.e("Error", "Activity Selection: onclick listener available: " + e.getMessage());
            }
        }
    };

    private View.OnClickListener selectedTagClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Button clickedButton = (Button) view;
                ActivitySelection.mActivitySelectionObject.moveTagToAvailables(clickedButton);
                ActivitySelection.mActivitySelectionObject.calculateAssessmentSet();
            } catch (Exception e) {
                Log.e("Error", "Activity Selection: onclick listener selected" + e.getMessage());
            }

        }
    };
    // endregion

    // region object methods
    private void moveTagToSelected(Button clickedButton) {
        // remove tag and button
        FlexboxLayout flexboxAvailables = (FlexboxLayout) findViewById(R.id.fbAvailableTags);
        flexboxAvailables.removeView(clickedButton);

        // remove tag from list
        this.availableTags.remove(clickedButton.getText().toString());

        // add tag
        this.selectedTags.add(clickedButton.getText().toString());

        // add tag as data source
        this.addTagAsAssessmentDataSource(clickedButton.getText().toString());

        // sort list
        java.util.Collections.sort(this.selectedTags);

        // add buttons
        FlexboxLayout flexboxSelected = (FlexboxLayout) findViewById(R.id.fbSelectedTags);
        flexboxSelected.removeAllViews();
        for(String tag:this.selectedTags) {
            Button b = new Button(this);
            b.setId(View.generateViewId());
            b.setText(tag);
            b.setOnClickListener(this.selectedTagClickListener);

            flexboxSelected.addView(b);
        }
    }

    private void moveTagToAvailables(Button clickedButton) {
        // remove tag and button
        FlexboxLayout flexboxSelected = (FlexboxLayout) findViewById(R.id.fbSelectedTags);
        flexboxSelected.removeView(clickedButton);

        // remove tag from list
        this.selectedTags.remove(clickedButton.getText().toString());

        // remove related data source
        this.removeAssessmentSource(clickedButton.getText().toString());

        // add tag
        this.availableTags.add(clickedButton.getText().toString());

        // sort list
        java.util.Collections.sort(this.availableTags);

        // add buttons
        FlexboxLayout flexboxAvailables = (FlexboxLayout) findViewById(R.id.fbAvailableTags);
        flexboxAvailables.removeAllViews();
        for(String tag:this.availableTags) {
            Button b = new Button(this);
            b.setId(View.generateViewId());
            b.setText(tag);
            b.setOnClickListener(this.availableTagClickListener);
            flexboxAvailables.addView(b);
        }
    }

    private void userWantsRelatedAssessments(Cursor result, DatabaseHelper helper) {
        try {
            // assessment can be part of multiple groups => do while
            do {
                long fk_t_related_id = result.getLong(0);

                if(fk_t_related_id >= 0) {
                    // search for other assessments
                    Cursor resultOtherAssessments = helper.getReadableDatabase().query(false, TableNames.RELATED_ITEM, new String[] {"assessment_uuid"}, ForeignKeyNames.RELATED + " = ?", new String[] {String.valueOf(fk_t_related_id)}, null, null, null, null);
                    resultOtherAssessments.moveToFirst();

                    if(resultOtherAssessments.getCount() > 0) {
                        do {
                            // get uuid
                            String relatedAssessmentUuid = resultOtherAssessments.getString(0);

                            // get database id
                            Cursor resultDatabaseId = helper.getReadableDatabase().query(false, TableNames.ASSESSMENT_ITEM, new String[] {"id", "uuid", "title"}, "uuid = ?", new String[] {relatedAssessmentUuid}, null, null, null, null);
                            resultDatabaseId.moveToFirst();

                            // add assessments to assessments to learn
                            // only if not already existing
                            boolean found = false;
                            for(Assessment a:this.assessmentsToLearn) {
                                if(a.getUuid().equals(relatedAssessmentUuid)) {
                                    found = true;
                                    break;
                                }
                            }

                            if(!found) {
                                Assessment assessmentToAdd = new Assessment();
                                assessmentToAdd.setId(resultDatabaseId.getInt(0));
                                assessmentToAdd.setUuid(resultDatabaseId.getString(1));
                                assessmentToAdd.setTitle(resultDatabaseId.getString(2));
                                this.assessmentsToLearn.add(assessmentToAdd);
                            }
                        } while (resultOtherAssessments.moveToNext());
                    }
                }
            } while (result.moveToNext());
        } catch (Exception e) {
            Log.e("Error", "ActivitySelection userWantsRelatedAssessments(): " + e.getMessage());
        }
    }

    private void handleCategorySourceButtonClick(String dataSourceName, ExtendedToggleButton currentButton, String query) {
        // check button state
        if(currentButton.isAlreadyPressed()) {
            // already pressed, remove
            currentButton.setAlreadyPressed(false);
            this.removeAssessmentSource(dataSourceName);
        } else {
            // not pressed, get data source
            currentButton.setAlreadyPressed(true);

            // disable buttons
            this.disableAllCategoryButtons(currentButton);

            // get all assessments
            DatabaseHelper helper = new DatabaseHelper(this);
            Cursor result = helper.getReadableDatabase().rawQuery(query, null);
            result.moveToFirst();

            // new assessment data source
            AssessmentsDataSource dataSource = new AssessmentsDataSource(dataSourceName);

            // result not empty?
            if(result.getCount() > 0) {
                do {
                    // attention: every query must deliver the columns in the same order!
                    Assessment assessment = new Assessment();
                    assessment.setId(result.getInt(0));
                    assessment.setUuid(result.getString(1));
                    assessment.setTitle(result.getString(2));
                    assessment.setCreationTimestamp(result.getInt(3));
                    assessment.setIdentifier(result.getString(4));

                    dataSource.addAssessment(assessment);
                } while(result.moveToNext());

                // add data source to list
                this.addAssessmentSource(dataSource);
            }
        }
    }

    /**
     * Serializes the assessments ids to learn for the next activity.
     * Ids will be comma separated transferred
     * @return
     */
    private String serializeAssessmentIds() {
        StringBuilder ids = new StringBuilder();

        for(int i=0;i < this.assessmentsToLearn.size();i++) {
            // current assessment
            Assessment currentAssessment = this.assessmentsToLearn.get(i);

            if(i == this.assessmentsToLearn.size()-1) {
                ids.append(String.valueOf(currentAssessment.getId()));
            } else {
                ids.append(String.valueOf(currentAssessment.getId()));
                ids.append(";");
            }
        }

        return ids.toString();
    }
    // endregion

    // region button clicks
    public void btnLearnOnClick(View view) {
        // check if assessments selected
        if(this.assessmentsToLearn.size() > 0) {
            // user wants related assessments?
            LinearLayout layoutRelatedButton = (LinearLayout) this.findViewById(R.id.linearLayoutRelatedButton);

            // layout has button?
            if(layoutRelatedButton.getChildCount() > 0) {
                // get child
                View childView = layoutRelatedButton.getChildAt(0);

                if(childView instanceof ExtendedToggleButton) {
                    ExtendedToggleButton toggleButton = (ExtendedToggleButton) childView;

                    if(toggleButton.isAlreadyPressed()) {
                        // user wants related assessments
                        for(Assessment assessment:this.assessmentsToLearn) {
                            DatabaseHelper helper = new DatabaseHelper(this);
                            Cursor result = helper.getReadableDatabase().query(false, TableNames.RELATED_ITEM, new String[] {ForeignKeyNames.RELATED}, "assessment_uuid = ?", new String[] {String.valueOf(assessment.getUuid())}, null, null, null, null);
                            result.moveToFirst();
                            this.userWantsRelatedAssessments(result, helper);
                            break;
                        }

                    } else {
                        // no related assessments, start learn activity
                    }
                } else {
                    // no related assessments, start learn activity

                }
            } else {
                // no related assessments, start learn activity
            }

            // start learn activity
            Intent intent = new Intent(this, ActivityLearn.class);
            intent.putExtra("assessment_ids", this.serializeAssessmentIds());
            startActivity(intent);
        } else {
            // no assessmments selected
            Toast.makeText(this, "Bitte Aufgaben auswählen", Toast.LENGTH_SHORT).show();
        }
    }
     // endregion

    private void calculateAssessmentSet() {
        // remove views
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutCalculatedAssessments);
        linearLayout.removeAllViews();

        // new complete source list
        List<Assessment> calculatedAssessments = new ArrayList<Assessment>();

        // calculate assessments with sources
        for(AssessmentsDataSource existingSource:this.assessmentsSources) {
            // get assessments from existing source
            List<Assessment> tmpAssessmentList = existingSource.getRelatedAssessments();

            // check, if assessments are not in calculated assessment list
            for(Assessment newAssessment:tmpAssessmentList) {
                boolean found = false;
                for(Assessment existingAssessment:calculatedAssessments) {
                    if(newAssessment.getUuid().equals(existingAssessment.getUuid())) {
                        found = true;
                    }
                }

                // if not found, add
                if(!found) {
                    calculatedAssessments.add(newAssessment);
                }
            }
        }

        // calculated assessments existing?
        if(calculatedAssessments.size() > 0) {
            // add calculated assessments to activity list
            this.assessmentsToLearn.clear();
            this.assessmentsToLearn.addAll(calculatedAssessments);

            // add assessments to linear layout
            for(Assessment assessment:calculatedAssessments) {
                this.addAssessmentToDisplay(assessment, linearLayout);
            }

            // enable related toggle button?
            // search for each assessment
            for(Assessment assessment:this.assessmentsToLearn) {
                // get foreign keys
                DatabaseHelper helper = new DatabaseHelper(this);
                Cursor result = helper.getReadableDatabase().query(false, TableNames.RELATED_ITEM, new String[] {ForeignKeyNames.RELATED}, "assessment_uuid = ?", new String[] {String.valueOf(assessment.getUuid())}, null, null, null, null);
                result.moveToFirst();

                // is part of group?
                if(result.getCount() > 0) {
                    LinearLayout layoutButton = (LinearLayout) this.findViewById(R.id.linearLayoutRelatedButton);
                    layoutButton.removeAllViews();

                    ExtendedToggleButton toggleButton = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_toggle_related_button));
                    toggleButton.setOnClickListener(new ExtendedOnClickListener(this) {
                        @Override
                        public void onClick(View view) {
                            super.onClick(view);

                            ExtendedToggleButton button = (ExtendedToggleButton) view;

                            if(button.isAlreadyPressed()) {
                                button.setAlreadyPressed(false);
                            } else {
                                button.setAlreadyPressed(true);
                            }
                        }
                    });
                    toggleButton.setId(View.generateViewId());
                    layoutButton.removeAllViews();
                    layoutButton.addView(toggleButton);

                    break;
                } else {
                    ((LinearLayout) this.findViewById(R.id.linearLayoutRelatedButton)).removeAllViews();
                }
            }

            // scroll down
            final ScrollView scrollView = (ScrollView) (this.findViewById(R.id.scrollViewSelection));
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        } else {
            ((LinearLayout) this.findViewById(R.id.linearLayoutRelatedButton)).removeAllViews();
        }
    }

    private void addAssessmentSource(AssessmentsDataSource assessmentSource) {
        // only add, if not already added
        boolean assessmentSourceFound = false;

        // iterate
        for(AssessmentsDataSource source:this.assessmentsSources) {
            if(source.getSourceName().equals(assessmentSource.getSourceName())) {
                assessmentSourceFound = true;
                break;
            }
        }

        // if not found, add
        if(!assessmentSourceFound) {
            this.assessmentsSources.add(assessmentSource);
        }

        // calculate sources
        this.calculateAssessmentSet();
    }

    private void addTagAsAssessmentDataSource(String tag) {
        // get all assessments related to this tag
        DatabaseHelper helper = new DatabaseHelper(this);
        Cursor result = helper.getReadableDatabase().rawQuery("SELECT t_assessment_item.id, t_assessment_item.uuid, t_assessment_item.creation_timestamp, t_assessment_item.identifier, t_assessment_item.title FROM t_assessment_item, t_category_tags WHERE t_category_tags.tag_name = '" + tag + "' AND t_category_tags.fk_t_assessment_item_id = t_assessment_item.id", null);
        result.moveToFirst();

        // results existing
        if(result.getCount() > 0) {
            // new assessment data source
            AssessmentsDataSource dataSource = new AssessmentsDataSource(tag);

            // iterate assessments
            do {
                Assessment assessment = new Assessment();
                assessment.setId(result.getInt(0));
                assessment.setUuid(result.getString(1));
                assessment.setCreationTimestamp(result.getInt(2));
                assessment.setIdentifier(result.getString(3));
                assessment.setTitle(result.getString(4));

                dataSource.addAssessment(assessment);
            } while(result.moveToNext());

            // add data source to list
            this.addAssessmentSource(dataSource);
        }
    }

    private void removeAssessmentSource(String sourceName) {
        // remove source from sources list
        for(int i=0;i < this.assessmentsSources.size();i++) {
            if(this.assessmentsSources.get(i).getSourceName().equals(sourceName)) {
                // found, remove
                this.assessmentsSources.remove(i);
            }
        }

        // calculate new sources
        this.calculateAssessmentSet();
    }

    private void addAssessmentToDisplay(Assessment assessment, LinearLayout targetLinearLayout) {
        // textview params
        // layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // timestamp parse
        String timestampString = "";
        try {
            Date timestampDate = new Date(assessment.getCreationTimestamp() * 1000L);
            SimpleDateFormat timestampDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            timestampDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-0"));
            timestampString = timestampDateFormat.format(timestampDate);
        } catch (Exception e) {
            Log.e("Error", "ActivitySelection addAssessmentToDisplay(): " + e.getMessage());
        }

        // new textview
        TextView tv = new TextView(this);
        tv.setTextSize(16);
        tv.setLayoutParams(params);
        tv.setBackgroundResource(R.drawable.activity_selection_assessment_item);
        tv.setPadding(10,10,10,10);

        // textview text
        switch (assessment.getIdentifier()) {
            case "choice": {
                tv.setText(assessment.getTitle() + " / " + "Singlechoice" + " / " + timestampString);
                break;
            }
            case "choiceMultiple": {
                tv.setText(assessment.getTitle() + " / " + "Multiplechoice" + " / " + timestampString);
                break;
            }
            case "positionObjects": {
                tv.setText(assessment.getTitle() + " / " + "Hotspot" + " / " + timestampString);
                break;
            }
            case "table": {
                tv.setText(assessment.getTitle() + " / " + this.getString(R.string.activity_selection_textview_assessment_type_table) + " / " + timestampString);
                break;
            }
            default: {
                tv.setText(assessment.getTitle() + " / " + "Drag'n'Drop" + " / " + timestampString);
                break;
            }
        }

        targetLinearLayout.addView(tv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        // linear layout
        LinearLayout targetLayout = (LinearLayout) this.findViewById(R.id.linearLayoutCategories);

        // define buttons
        ExtendedToggleButton btnLastImported = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_last_imported));
        ExtendedToggleButton btnForRepeating = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_for_repeating));
        ExtendedToggleButton btnSolvedCorrect = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_solved_corred));
        ExtendedToggleButton btnAllAssessments = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_all_assessments));
        ExtendedToggleButton btnSolvedNotCorrect = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_not_solved_corred));

        // click events
        btnLastImported.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // current activity
                ActivitySelection activity = (ActivitySelection) this.getObjects()[0];

                // get max insert or update timestamp
                DatabaseHelper helper = new DatabaseHelper(activity);
                Cursor result = helper.getReadableDatabase().rawQuery(App.getStringContentFromRawFile(activity, R.raw.assessments_last_imported), null);
                result.moveToFirst();

                // results existing?
                if(result.getCount() > 0) {
                    // max timestamp
                    int max_timestamp = result.getInt(0);

                    // get assessments by new query
                    String query = App.getStringContentFromRawFile(activity, R.raw.assessments_last_imported2);
                    query = query.replace("[max_timestamp]", String.valueOf(max_timestamp));

                    activity.handleCategorySourceButtonClick("ForRepeating", (ExtendedToggleButton) view, query);
                }
            }
        });

        btnForRepeating.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                ActivitySelection activity = (ActivitySelection) this.getObjects()[0];

                // get assessments, which are solved partly_correct and wrong, and which are solved correctly
                DatabaseHelper helper = new DatabaseHelper(activity);
                Cursor result = helper.getReadableDatabase().rawQuery(App.getStringContentFromRawFile(activity, R.raw.assessments_for_repeating), null);
                result.moveToFirst();

                if(result.getCount() > 0) {
                    // check for assessments with specified id, if they are solved correctly three times after another
                    List<Integer> assessmentIdsToExclude = new ArrayList<Integer>();
                    do {
                        int assessmentId = result.getInt(0);
                        int countCorrectly = result.getInt(1);

                        SharedPreferences preferences = activity.getSharedPreferences("repeatingThreshold", MODE_PRIVATE);
                        int userThreshold = preferences.getInt("repeatingThreshold", 3);

                        if(countCorrectly >= userThreshold) {
                            // check if assessments (x = userThreshold) times after another was solved correctly
                            String query2 = App.getStringContentFromRawFile(activity, R.raw.assessments_for_repeating2);
                            query2 = query2.replace("[id]", String.valueOf(assessmentId));
                            Cursor result2 = helper.getReadableDatabase().rawQuery(query2, null);
                            result2.moveToFirst();

                            boolean solved_not_correct = false;
                            for(int i=0;i < userThreshold;i++) {
                                int solved = result2.getInt(0);

                                if(solved != 0) {
                                    solved_not_correct = true;
                                    break;
                                }
                            }

                            if(!solved_not_correct) {
                                // exclude assessment
                                assessmentIdsToExclude.add(new Integer(assessmentId));
                            } else {
                                // do not exclude assessment
                            }
                        }
                    } while(result.moveToNext());

                    // are assessments existing to exclude?
                    StringBuilder excludeSqlSequence = new StringBuilder();
                    excludeSqlSequence.append("(");
                    if(assessmentIdsToExclude.size() > 0) {
                        for(int i=0;i < assessmentIdsToExclude.size();i++) {
                            if(i != assessmentIdsToExclude.size()-1) {
                                excludeSqlSequence.append(assessmentIdsToExclude.get(i));
                                excludeSqlSequence.append(",");
                            } else {
                                excludeSqlSequence.append(assessmentIdsToExclude.get(i));
                            }
                        }
                    }
                    excludeSqlSequence.append(")");

                    String query = App.getStringContentFromRawFile(activity, R.raw.assessments_for_repeating3);
                    query = query.replace("[exclude]", excludeSqlSequence.toString());

                    activity.handleCategorySourceButtonClick("ForRepeating", (ExtendedToggleButton) view, query);
                } else {
                    // not existing, show falsely solved assessments
                    activity.handleCategorySourceButtonClick("ForRepeating", (ExtendedToggleButton) view, App.getStringContentFromRawFile(activity, R.raw.assessments_solved_falsely));
                }

                //activity.handleCategorySourceButtonClick("ForRepeating", (ExtendedDataSourceButton) view, App.getStringContentFromRawFile(activity, R.raw.assessments_for_repeating));
            }
        });

        btnSolvedCorrect.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                ActivitySelection activity = (ActivitySelection) this.getObjects()[0];
                activity.handleCategorySourceButtonClick("CorrectlySolvedAssessments", (ExtendedToggleButton) view, App.getStringContentFromRawFile(activity, R.raw.assessments_solved_correctly));
            }
        });

        btnAllAssessments.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                ActivitySelection activity = (ActivitySelection) this.getObjects()[0];
                activity.handleCategorySourceButtonClick("AllAssessments", (ExtendedToggleButton) view, "SELECT id, uuid, title, creation_timestamp, identifier FROM t_assessment_item ORDER BY title ASC;");
            }
        });

        btnSolvedNotCorrect.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                ActivitySelection activity = (ActivitySelection) this.getObjects()[0];
                activity.handleCategorySourceButtonClick("FalselySolvedAssessments", (ExtendedToggleButton) view, App.getStringContentFromRawFile(activity, R.raw.assessments_solved_falsely));
            }
        });

        // button single line
        btnLastImported.setSingleLine(true);
        btnForRepeating.setSingleLine(true);
        btnSolvedCorrect.setSingleLine(true);
        btnAllAssessments.setSingleLine(true);
        btnSolvedNotCorrect.setSingleLine(true);

        // add buttons to layout
        targetLayout.addView(btnLastImported);
        targetLayout.addView(btnForRepeating);
        targetLayout.addView(btnSolvedCorrect);
        targetLayout.addView(btnAllAssessments);
        targetLayout.addView(btnSolvedNotCorrect);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // set own context
        ActivitySelection.mContext = this;

        // set object
        ActivitySelection.mActivitySelectionObject = this;

        // clear selected
        this.availableTags.clear();
        this.selectedTags.clear();
        this.assessmentsToLearn.clear();
        ((LinearLayout) findViewById(R.id.linearLayoutCalculatedAssessments)).removeAllViews();
        ((FlexboxLayout) findViewById(R.id.fbSelectedTags)).removeAllViews();

        try {
            // get available tags
            DatabaseHelper helper = new DatabaseHelper(this);
            FlexboxLayout flexboxAvailables = (FlexboxLayout) this.findViewById(R.id.fbAvailableTags);
            flexboxAvailables.removeAllViews();
            this.availableTags = helper.getTagList();

            String[] keywords = new String[] {"singlechoice", "multiplechoice", "hotspot", "table", "tabelle", "drag"};

            for (String tag : this.availableTags) {
                Button b = new Button(this);
                b.setId(View.generateViewId());
                b.setText(tag);

                for(String keyword:keywords) {
                    if(tag.toLowerCase().contains(keyword)) {
                        SpannableString span = new SpannableString(tag);
                        span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), 0);
                        //span.setSpan(new ForegroundColorSpan(Color.RED), 48, 55, 0);
                        b.setText(span);
                        break;
                    }
                }

                b.setOnClickListener(this.availableTagClickListener);
                flexboxAvailables.addView(b);
            }
        } catch (SQLiteException se) {
            Log.e("Error", "sqlite error: " + se.getMessage());
        } catch (Exception e) {
            Log.e("Error", "error: " + e.getMessage());
        }

        // reset data sources
        this.assessmentsSources = new ArrayList<AssessmentsDataSource>();
    }

    private void disableAllCategoryButtons(ExtendedToggleButton exceptButton) {
        // disable all category buttons
        LinearLayout categoryLayout = (LinearLayout) this.findViewById(R.id.linearLayoutCategories);

        try {
            if(categoryLayout.getChildCount() > 0) {
                for(int i=0;i < categoryLayout.getChildCount();i++) {
                    View childView = categoryLayout.getChildAt(i);

                    if(childView instanceof ExtendedToggleButton) {
                        ExtendedToggleButton currentButton = (ExtendedToggleButton) childView;

                        if(exceptButton != null) {
                            if(currentButton != exceptButton) {
                                if(currentButton.isAlreadyPressed() || currentButton.isChecked()) {
                                    currentButton.performClick();
                                    break;
                                }
                            }

                        } else {
                            if(currentButton.isAlreadyPressed() || currentButton.isChecked()) {
                                currentButton.performClick();
                                break;
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error", "ActivitySelection onResume(): " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ((LinearLayout) this.findViewById(R.id.linearLayoutRelatedButton)).removeAllViews();

        this.disableAllCategoryButtons(null);
    }

    // region menu (dot points)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
}
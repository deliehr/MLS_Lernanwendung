package it.liehr.mls_app;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import Comprehensive.App;
import Comprehensive.DatabaseHelper;
import Comprehensive.ExtendedToggleButton;
import Comprehensive.ExtendedOnClickListener;
import Comprehensive.DatePickerFragment;

public class ActivityStatistic extends AppCompatActivity {
    // region object variables
    private BarChart barChart;
    private List<ExtendedToggleButton> categoryToggleButtons = new ArrayList<ExtendedToggleButton>();
    private List<ExtendedToggleButton> tagToggleButtons = new ArrayList<ExtendedToggleButton>();
    private boolean enabledTimePeriod = false;
    public String datePeriodStart = "";
    public String datePeriodEnd = "";
    private List<String> tagList = new ArrayList<String>();
    // endregion

    // region object methods
    private void disableToggleButtons(ExtendedToggleButton exceptButton) {
        // disable category buttons
        for(ExtendedToggleButton toggleButton:this.categoryToggleButtons) {
            if(toggleButton.isAlreadyPressed() && !toggleButton.equals(exceptButton)) {
                toggleButton.performClick();
            }
        }

        // disable tag buttons
        for(ExtendedToggleButton toggleButton:this.tagToggleButtons) {
            if(toggleButton.isAlreadyPressed() && !toggleButton.equals(exceptButton)) {
                toggleButton.performClick();
            }
        }
    }
    // endregion

    // region listener
    public void btnAvailableTagsClick(View view) {
        FlexboxLayout flexboxLayout = (FlexboxLayout) this.findViewById(R.id.flexboxAvailableTags);

        if(flexboxLayout.getVisibility() == View.VISIBLE) {
            flexboxLayout.setVisibility(View.GONE);
            ((Button) view).setText(R.string.activity_statistic_button_show_layout);
        } else {
            flexboxLayout.setVisibility(View.VISIBLE);
            ((Button) view).setText(R.string.activity_statistic_button_hide_layout);
        }

    }

    public void btnTimePeriodClick(View view) {
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.linearLayoutDateToggle);

        if(linearLayout.getVisibility() == View.VISIBLE) {
            linearLayout.setVisibility(View.GONE);
            ((Button) view).setText(R.string.activity_statistic_button_show_layout);
        } else {
            linearLayout.setVisibility(View.VISIBLE);
            ((Button) view).setText(R.string.activity_statistic_button_hide_layout);
        }
    }
    // endregion

    @Override
    protected void onResume() {
        super.onResume();
    }

    // region activity methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // region chart
        // target layout
        LinearLayout targetLinearLayout = (LinearLayout) this.findViewById(R.id.linearLayoutChart);

        // layout params
        final LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        chartParams.setMargins(10,10,10,10);

        // bar chart
        this.barChart = new BarChart(this);
        this.barChart.setId(View.generateViewId());
        this.barChart.setLayoutParams(chartParams);
        this.barChart.setDescription(null);

        // config
        XAxis xAxis = barChart.getXAxis();
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1.0f);
        xAxis.setLabelRotationAngle((float) -45);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setGranularityEnabled(true);
        yAxisLeft.setGranularity(1.0f);
        yAxisLeft.setCenterAxisLabels(true);
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        // no data message
        this.barChart.setNoDataText(this.getString(R.string.activity_statistic_chart_message_no_data));
        Paint p = this.barChart.getPaint(this.barChart.PAINT_INFO);
        p.setTextSize(40);
        p.setColor(Color.rgb(0, 0, 0));
        //p.setTypeface(new Typeface());

        // add chart to layout
        targetLinearLayout.addView(this.barChart);
        // endregion

        // region category buttons
        LinearLayout layoutCategories = (LinearLayout) this.findViewById(R.id.layoutCategories);

        // layout parameters buttons
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 1;

        // define buttons
        ExtendedToggleButton btnLastImported = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_last_imported));
        ExtendedToggleButton btnForRepeating = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_for_repeating));
        ExtendedToggleButton btnSolvedCorrect = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_solved_corred));
        ExtendedToggleButton btnAllAssessments = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_all_assessments));
        ExtendedToggleButton btnSolvedNotCorrect = new ExtendedToggleButton(this, this.getString(R.string.activity_selection_button_not_solved_corred));

        // add buttons to list
        this.categoryToggleButtons.clear();
        this.categoryToggleButtons.add(btnLastImported);
        this.categoryToggleButtons.add(btnForRepeating);
        this.categoryToggleButtons.add(btnSolvedCorrect);
        this.categoryToggleButtons.add(btnAllAssessments);
        this.categoryToggleButtons.add(btnSolvedNotCorrect);

        // set id & layout params
        for(int i=0;i < this.categoryToggleButtons.size();i++) {
            this.categoryToggleButtons.get(i).setId(View.generateViewId());
            this.categoryToggleButtons.get(i).setLayoutParams(buttonParams);
            this.categoryToggleButtons.get(i).setSingleLine(true);
            this.categoryToggleButtons.get(i).setPadding(0,0,10,0);
        }

        // click events
        btnLastImported.setOnClickListener(new ExtendedOnClickListener(this, new Object[]{this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // current activity
                ActivityStatistic activity = (ActivityStatistic) this.getObjects()[0];

                // check button state
                ExtendedToggleButton currentButton = (ExtendedToggleButton) view;

                // check button state
                if(currentButton.isAlreadyPressed()) {
                    // set state to not clicked
                    currentButton.setAlreadyPressed(false);

                    // chart null
                    activity.barChart.setData(null);
                    activity.barChart.invalidate();
                } else {
                    // disable other buttons
                    activity.disableToggleButtons(currentButton);

                    // set state to clicked
                    currentButton.setAlreadyPressed(true);

                    try {
                        // database
                        DatabaseHelper helper = new DatabaseHelper(activity);
                        SQLiteDatabase database = helper.getReadableDatabase();

                        // get single days
                        String query = "";
                        if(activity.enabledTimePeriod) {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days_period);
                            query = query.replace("[period_start]", activity.datePeriodStart);
                            query = query.replace("[period_end]", activity.datePeriodEnd);
                        } else {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days);
                        }
                        Cursor resultDays = database.rawQuery(query, null);
                        resultDays.moveToFirst();

                        // days existing?
                        if(resultDays.getCount() > 0) {
                            // create lists
                            List<BarEntry> listSolvedCorrectly = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedPartlyCorrect = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedFalsely = new ArrayList<BarEntry>();

                            // date list
                            // the date on the x axis will not be auto generated
                            List<String> dateStringList = new ArrayList<String>();

                            // iterate
                            int indexDays = 0;
                            do {
                                // current date as string
                                String currentDateString = resultDays.getString(0);
                                dateStringList.add(currentDateString);

                                // query for data
                                String query2 = App.getStringContentFromRawFile(activity, R.raw.statistic_last_imported);
                                query2 = query2.replace("[date]", currentDateString);

                                // search in database for how_solved
                                Cursor resultSolved = database.rawQuery(query2, null);
                                resultSolved.moveToFirst();

                                // solved existing?
                                if(resultSolved.getCount() > 0) {
                                    boolean setCorrectly = false;
                                    boolean setPartlyCorrect = false;
                                    boolean setFalsely = false;

                                    for(int i=0;i < 3;i++) {
                                        int count_solved = 0;
                                        int how_solved = 0;

                                        try {
                                            count_solved = resultSolved.getInt(0);
                                            how_solved = resultSolved.getInt(3);
                                        } catch (Exception e) {
                                            count_solved = 0;
                                            how_solved = i;
                                        }

                                        if(i==0 && !setCorrectly) {
                                            if(how_solved == 0) {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, 0));
                                            }

                                            setCorrectly = true;
                                        }

                                        if(i==1 && !setPartlyCorrect) {
                                            if(how_solved == 1) {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, 0));
                                            }

                                            setPartlyCorrect = true;
                                        }

                                        if(i==2 && !setFalsely) {
                                            if(how_solved == 2) {
                                                listSolvedFalsely.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedFalsely.add(new BarEntry(indexDays, 0));
                                            }

                                            setFalsely = true;
                                        }
                                    }
                                }

                                indexDays++;
                            } while (resultDays.moveToNext());

                            // entries to chart?
                            BarDataSet set1 = new BarDataSet(listSolvedCorrectly, "Richtig");
                            BarDataSet set2 = new BarDataSet(listSolvedPartlyCorrect, "Teilw. richtig");
                            BarDataSet set3 = new BarDataSet(listSolvedFalsely, "Falsch");
                            set1.setColor(Color.rgb(25, 75, 125));
                            set2.setColor(Color.rgb(75, 125, 175));
                            set3.setColor(Color.rgb(125, 175, 225));

                            float groupSpace = 0.07f;
                            float barSpace = 0.02f; // x2 dataset
                            float barWidth = 0.29f; // x2 dataset

                            BarData data = new BarData(set1, set2, set3);
                            data.setValueFormatter(new chartValueFormatter());
                            data.setBarWidth(barWidth);
                            activity.barChart.setData(data);
                            activity.barChart.groupBars(0f, groupSpace, barSpace); // perform the "explicit" grouping
                            activity.barChart.getXAxis().setCenterAxisLabels(true);
                            activity.barChart.getXAxis().setLabelCount((resultDays.getCount() + 2));
                            activity.barChart.getXAxis().setValueFormatter(new AxisFormatterDateString(dateStringList));
                            activity.barChart.setVisibleXRangeMinimum((float) (resultDays.getCount() + 2));
                            activity.barChart.invalidate(); // refresh
                        }
                    } catch (Exception e) {
                        Log.e("Error", "ActivityStatistic onCreate() ExtendedOnClickListener (btnLastImported): " + e.getMessage());
                    }
                }
            }
        });

        btnForRepeating.setOnClickListener(new ExtendedOnClickListener(this, new Object[]{this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // TODO: 18.04.2017 implement repeating

                // current activity
                ActivityStatistic activity = (ActivityStatistic) this.getObjects()[0];

                // check button state
                ExtendedToggleButton currentButton = (ExtendedToggleButton) view;

                // check button state
                if(currentButton.isAlreadyPressed()) {
                    // set state to not clicked
                    currentButton.setAlreadyPressed(false);

                    // chart null
                    activity.barChart.setData(null);
                    activity.barChart.invalidate();
                } else {
                    // disable other buttons
                    activity.disableToggleButtons(currentButton);

                    // set state to clicked
                    currentButton.setAlreadyPressed(true);

                    try {
                        // database
                        DatabaseHelper helper = new DatabaseHelper(activity);
                        SQLiteDatabase database = helper.getReadableDatabase();

                        //
                        SharedPreferences preferences = activity.getSharedPreferences("repeatingThreshold", 3);
                        int userThreshold = preferences.getInt("repeatingThreshold", 3);

                        // get single days
                        String query = "";
                        if(activity.enabledTimePeriod) {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days_period);
                            query = query.replace("[period_start]", activity.datePeriodStart);
                            query = query.replace("[period_end]", activity.datePeriodEnd);
                        } else {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days);
                        }

                        Cursor resultDays = database.rawQuery(query, null);
                        resultDays.moveToFirst();

                        // days existing?
                        if(resultDays.getCount() > 0) {
                            // create lists
                            List<BarEntry> listSolvedCorrectly = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedPartlyCorrect = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedFalsely = new ArrayList<BarEntry>();

                            // date list
                            // the date on the x axis will not be auto generated
                            List<String> dateStringList = new ArrayList<String>();

                            // iterate
                            int indexDays = 0;
                            do {
                                // current date as string
                                String currentDateString = resultDays.getString(0);
                                dateStringList.add(currentDateString);

                                // search for related assessments
                                String query2 = App.getStringContentFromRawFile(activity, R.raw.statistic_for_repeating);
                                query2 = query2.replace("[date]", currentDateString);

                                Cursor resultRelated = database.rawQuery(query2, null);
                                resultRelated.moveToFirst();

                                String query3 = "";
                                if(resultRelated.getCount() > 0) {
                                    // iterate solved assessments
                                    List<Integer> assessmentIdsToExclude = new ArrayList<Integer>();

                                    // get assessments, which are solved partly_correct and wrong, and which are solved correctly
                                    Cursor result = helper.getReadableDatabase().rawQuery(App.getStringContentFromRawFile(activity, R.raw.assessments_for_repeating), null);
                                    result.moveToFirst();

                                    if(result.getCount() > 0) {
                                        // check for assessments with specified id, if they are solved correctly three times after another
                                        do {
                                            int assessmentId = result.getInt(0);
                                            int countCorrectly = result.getInt(1);

                                            if(countCorrectly >= userThreshold) {
                                                // check if assessments (x = userThreshold) times after another was solved correctly
                                                String query4 = App.getStringContentFromRawFile(activity, R.raw.assessments_for_repeating2);
                                                query4 = query4.replace("[id]", String.valueOf(assessmentId));
                                                Cursor result2 = helper.getReadableDatabase().rawQuery(query4, null);
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
                                    }

                                    // are assessments existing to exclude?
                                    StringBuilder excludeSqlSequence = new StringBuilder();
                                    if(assessmentIdsToExclude.size() > 0) {
                                        for(int i=0;i < assessmentIdsToExclude.size();i++) {
                                            if(i != assessmentIdsToExclude.size()-1) {
                                                excludeSqlSequence.append(assessmentIdsToExclude.get(i));
                                                excludeSqlSequence.append(",");
                                            } else {
                                                excludeSqlSequence.append(assessmentIdsToExclude.get(i));
                                            }
                                        }

                                        // prepare next query
                                        query3 = "SELECT COUNT(id) AS count, fk_t_assessment_item_id, date(started_timestamp, 'unixepoch', 'localtime') AS started, date(processed_timestamp, 'unixepoch', 'localtime') AS processed, how_solved FROM t_statistic WHERE started NOT NULL AND processed NOT NULL AND how_solved NOT NULL AND processed = '[date]' AND fk_t_assessment_item_id NOT IN([id_list]) GROUP BY how_solved ORDER BY how_solved ASC;";
                                        query3 = query3.replace("[id_list]", excludeSqlSequence.toString());
                                    } else {
                                        // prepare next query
                                        query3 = "SELECT COUNT(id) AS count, fk_t_assessment_item_id, date(started_timestamp, 'unixepoch', 'localtime') AS started, date(processed_timestamp, 'unixepoch', 'localtime') AS processed, how_solved FROM t_statistic WHERE started NOT NULL AND processed NOT NULL AND how_solved NOT NULL AND processed = '[date]' GROUP BY how_solved ORDER BY how_solved ASC;";
                                    }
                                } else {
                                    // prepare next query
                                    query3 = "SELECT COUNT(id) AS count, fk_t_assessment_item_id, date(started_timestamp, 'unixepoch', 'localtime') AS started, date(processed_timestamp, 'unixepoch', 'localtime') AS processed, how_solved FROM t_statistic WHERE started NOT NULL AND processed NOT NULL AND how_solved NOT NULL AND processed = '[date]' GROUP BY how_solved ORDER BY how_solved ASC;";
                                }

                                query3 = query3.replace("[date]", currentDateString);

                                // search in database for how_solved
                                Cursor resultSolved = database.rawQuery(query3, null);
                                resultSolved.moveToFirst();

                                // solved existing?
                                if(resultSolved.getCount() > 0) {
                                    boolean setCorrectly = false;
                                    boolean setPartlyCorrect = false;
                                    boolean setFalsely = false;

                                    for(int i=0;i < 3;i++) {
                                        int count_solved = 0;
                                        int how_solved = 0;

                                        try {
                                            count_solved = resultSolved.getInt(0);
                                            how_solved = resultSolved.getInt(4);
                                        } catch (Exception e) {
                                            count_solved = 0;
                                            how_solved = i;
                                        }

                                        if(i==0 && !setCorrectly) {
                                            if(how_solved == 0) {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, 0));
                                            }

                                            setCorrectly = true;
                                        }

                                        if(i==1 && !setPartlyCorrect) {
                                            if(how_solved == 1) {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, 0));
                                            }

                                            setPartlyCorrect = true;
                                        }

                                        if(i==2 && !setFalsely) {
                                            if(how_solved == 2) {
                                                listSolvedFalsely.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedFalsely.add(new BarEntry(indexDays, 0));
                                            }

                                            setFalsely = true;
                                        }
                                    }
                                }

                                indexDays++;
                            } while (resultDays.moveToNext());

                            // entries to chart?
                            BarDataSet set1 = new BarDataSet(listSolvedCorrectly, "Richtig");
                            BarDataSet set2 = new BarDataSet(listSolvedPartlyCorrect, "Teilw. richtig");
                            BarDataSet set3 = new BarDataSet(listSolvedFalsely, "Falsch");
                            set1.setColor(Color.rgb(25, 75, 125));
                            set2.setColor(Color.rgb(75, 125, 175));
                            set3.setColor(Color.rgb(125, 175, 225));

                            float groupSpace = 0.07f;
                            float barSpace = 0.02f; // x2 dataset
                            float barWidth = 0.29f; // x2 dataset

                            BarData data = new BarData(set1, set2, set3);
                            data.setBarWidth(barWidth);
                            data.setValueFormatter(new chartValueFormatter());
                            activity.barChart.setData(data);
                            activity.barChart.groupBars(0f, groupSpace, barSpace); // perform the "explicit" grouping
                            activity.barChart.getXAxis().setCenterAxisLabels(true);
                            activity.barChart.getXAxis().setLabelCount((resultDays.getCount() + 2));
                            activity.barChart.getXAxis().setValueFormatter(new AxisFormatterDateString(dateStringList));
                            activity.barChart.setVisibleXRangeMinimum((float) (resultDays.getCount() + 2));
                            activity.barChart.invalidate(); // refresh
                        }
                    } catch (Exception e) {
                        Log.e("Error", "ActivityStatistic onCreate() ExtendedOnClickListener (btnAllAssessments): " + e.getMessage());
                    }
                }
            }
        });

        btnSolvedCorrect.setOnClickListener(new ExtendedOnClickListener(this, new Object[]{this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // current activity
                ActivityStatistic activity = (ActivityStatistic) this.getObjects()[0];

                // check button state
                ExtendedToggleButton currentButton = (ExtendedToggleButton) view;

                // check button state
                if(currentButton.isAlreadyPressed()) {
                    // set state to not clicked
                    currentButton.setAlreadyPressed(false);

                    // chart null
                    activity.barChart.setData(null);
                    activity.barChart.invalidate();
                } else {
                    // disable other buttons
                    activity.disableToggleButtons(currentButton);

                    // set state to clicked
                    currentButton.setAlreadyPressed(true);

                    try {
                        // database
                        DatabaseHelper helper = new DatabaseHelper(activity);
                        SQLiteDatabase database = helper.getReadableDatabase();

                        // get single days
                        // get single days
                        String query = "";
                        if(activity.enabledTimePeriod) {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days_period);
                            query = query.replace("[period_start]", activity.datePeriodStart);
                            query = query.replace("[period_end]", activity.datePeriodEnd);
                        } else {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days);
                        }
                        Cursor resultDays = database.rawQuery(query, null);
                        resultDays.moveToFirst();

                        // days existing?
                        if(resultDays.getCount() > 0) {
                            // create lists
                            List<BarEntry> listSolvedCorrect = new ArrayList<BarEntry>();

                            // date list
                            // the date on the x axis will not be auto generated
                            List<String> dateStringList = new ArrayList<String>();

                            // iterate
                            int indexDays = 0;
                            do {
                                // current date as string
                                String currentDateString = resultDays.getString(0);
                                dateStringList.add(currentDateString);

                                // query for data
                                String query2 = "SELECT COUNT(id) AS count, fk_t_assessment_item_id, date(started_timestamp, 'unixepoch', 'localtime') AS started, date(processed_timestamp, 'unixepoch', 'localtime') AS processed, how_solved FROM t_statistic WHERE started NOT NULL AND processed NOT NULL AND how_solved NOT NULL AND how_solved = 0 AND processed = '[date]' GROUP BY how_solved ORDER BY how_solved ASC;";
                                query2 = query2.replace("[date]", currentDateString);

                                // search in database for how_solved
                                Cursor resultSolved = database.rawQuery(query2, null);
                                resultSolved.moveToFirst();

                                // solved existing?
                                if(resultSolved.getCount() > 0) {
                                    int count_solved = resultSolved.getInt(0);

                                    listSolvedCorrect.add(new BarEntry(indexDays, count_solved));
                                } else {
                                    listSolvedCorrect.add(new BarEntry(indexDays, 0));
                                }

                                indexDays++;
                            } while (resultDays.moveToNext());

                            // entries to chart?
                            BarDataSet set3 = new BarDataSet(listSolvedCorrect, "Richtig");
                            set3.setColor(Color.rgb(25, 75, 125));

                            float barWidth = 0.9f; // x2 dataset

                            BarData data = new BarData(set3);
                            data.setBarWidth(barWidth);
                            data.setValueFormatter(new chartValueFormatter());
                            activity.barChart.setData(null);
                            activity.barChart.setData(data);
                            activity.barChart.getXAxis().setCenterAxisLabels(false);
                            activity.barChart.getXAxis().setLabelCount((resultDays.getCount() + 2));
                            activity.barChart.getXAxis().setValueFormatter(new AxisFormatterDateString(dateStringList));
                            activity.barChart.setVisibleXRangeMinimum((float) (resultDays.getCount() + 2));
                            activity.barChart.invalidate(); // refresh
                        }
                    } catch (Exception e) {
                        Log.e("Error", "ActivityStatistic onCreate() ExtendedOnClickListener (btnSolvedCorrect): " + e.getMessage());
                    }
                }
            }
        });

        btnAllAssessments.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // current activity
                ActivityStatistic activity = (ActivityStatistic) this.getObjects()[0];

                // check button state
                ExtendedToggleButton currentButton = (ExtendedToggleButton) view;

                // check button state
                if(currentButton.isAlreadyPressed()) {
                    // set state to not clicked
                    currentButton.setAlreadyPressed(false);

                    // chart null
                    activity.barChart.setData(null);
                    activity.barChart.invalidate();
                } else {
                    // disable other buttons
                    activity.disableToggleButtons(currentButton);

                    // set state to clicked
                    currentButton.setAlreadyPressed(true);

                    try {
                        // database
                        DatabaseHelper helper = new DatabaseHelper(activity);
                        SQLiteDatabase database = helper.getReadableDatabase();

                        // get single days
                        String query = "";
                        if(activity.enabledTimePeriod) {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days_period);
                            query = query.replace("[period_start]", activity.datePeriodStart);
                            query = query.replace("[period_end]", activity.datePeriodEnd);
                        } else {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days);
                        }

                        Cursor resultDays = database.rawQuery(query, null);
                        resultDays.moveToFirst();

                        // days existing?
                        if(resultDays.getCount() > 0) {
                            // create lists
                            List<BarEntry> listSolvedCorrectly = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedPartlyCorrect = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedFalsely = new ArrayList<BarEntry>();

                            // date list
                            // the date on the x axis will not be auto generated
                            List<String> dateStringList = new ArrayList<String>();

                            // iterate
                            int indexDays = 0;
                            do {
                                // current date as string
                                String currentDateString = resultDays.getString(0);
                                dateStringList.add(currentDateString);

                                // query for data
                                String query2 = "SELECT COUNT(id) AS count, fk_t_assessment_item_id, date(started_timestamp, 'unixepoch', 'localtime') AS started, date(processed_timestamp, 'unixepoch', 'localtime') AS processed, how_solved FROM t_statistic WHERE  started NOT NULL AND processed NOT NULL AND how_solved NOT NULL AND processed = '[date]' GROUP BY how_solved ORDER BY how_solved ASC;";
                                query2 = query2.replace("[date]", currentDateString);

                                // search in database for how_solved
                                Cursor resultSolved = database.rawQuery(query2, null);
                                resultSolved.moveToFirst();

                                // solved existing?
                                if(resultSolved.getCount() > 0) {
                                    boolean setCorrectly = false;
                                    boolean setPartlyCorrect = false;
                                    boolean setFalsely = false;

                                    for(int i=0;i < 3;i++) {
                                        int count_solved = 0;
                                        int how_solved = 0;

                                        try {
                                            count_solved = resultSolved.getInt(0);
                                            how_solved = resultSolved.getInt(4);
                                        } catch (Exception e) {
                                            count_solved = 0;
                                            how_solved = i;
                                        }

                                        if(i==0 && !setCorrectly) {
                                            if(how_solved == 0) {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, 0));
                                            }

                                            setCorrectly = true;
                                        }

                                        if(i==1 && !setPartlyCorrect) {
                                            if(how_solved == 1) {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, 0));
                                            }

                                            setPartlyCorrect = true;
                                        }

                                        if(i==2 && !setFalsely) {
                                            if(how_solved == 2) {
                                                listSolvedFalsely.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedFalsely.add(new BarEntry(indexDays, 0));
                                            }

                                            setFalsely = true;
                                        }
                                    }
                                }

                                indexDays++;
                            } while (resultDays.moveToNext());

                            // entries to chart?
                            BarDataSet set1 = new BarDataSet(listSolvedCorrectly, "Richtig");
                            BarDataSet set2 = new BarDataSet(listSolvedPartlyCorrect, "Teilw. richtig");
                            BarDataSet set3 = new BarDataSet(listSolvedFalsely, "Falsch");
                            set1.setColor(Color.rgb(25, 75, 125));
                            set2.setColor(Color.rgb(75, 125, 175));
                            set3.setColor(Color.rgb(125, 175, 225));

                            float groupSpace = 0.07f;
                            float barSpace = 0.02f; // x2 dataset
                            float barWidth = 0.29f; // x2 dataset

                            BarData data = new BarData(set1, set2, set3);
                            data.setBarWidth(barWidth);
                            data.setValueFormatter(new chartValueFormatter());
                            activity.barChart.setData(data);
                            activity.barChart.groupBars(0f, groupSpace, barSpace); // perform the "explicit" grouping
                            activity.barChart.getXAxis().setCenterAxisLabels(true);
                            activity.barChart.getXAxis().setLabelCount((resultDays.getCount() + 2));
                            activity.barChart.getXAxis().setValueFormatter(new AxisFormatterDateString(dateStringList));
                            activity.barChart.setVisibleXRangeMinimum((float) (resultDays.getCount() + 2));
                            activity.barChart.invalidate(); // refresh
                        }
                    } catch (Exception e) {
                        Log.e("Error", "ActivityStatistic onCreate() ExtendedOnClickListener (btnAllAssessments): " + e.getMessage());
                    }
                }
            }
        });

        btnSolvedNotCorrect.setOnClickListener(new ExtendedOnClickListener(this, new Object[]{this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // current activity
                ActivityStatistic activity = (ActivityStatistic) this.getObjects()[0];

                // check button state
                ExtendedToggleButton currentButton = (ExtendedToggleButton) view;

                // check button state
                if(currentButton.isAlreadyPressed()) {
                    // set state to not clicked
                    currentButton.setAlreadyPressed(false);

                    // chart null
                    activity.barChart.setData(null);
                    activity.barChart.invalidate();
                } else {
                    // disable other buttons
                    activity.disableToggleButtons(currentButton);

                    // set state to clicked
                    currentButton.setAlreadyPressed(true);

                    try {
                        // database
                        DatabaseHelper helper = new DatabaseHelper(activity);
                        SQLiteDatabase database = helper.getReadableDatabase();

                        // get single days
                        String query = "";
                        if(activity.enabledTimePeriod) {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days_period);
                            query = query.replace("[period_start]", activity.datePeriodStart);
                            query = query.replace("[period_end]", activity.datePeriodEnd);
                        } else {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days);
                        }
                        Cursor resultDays = database.rawQuery(query, null);
                        resultDays.moveToFirst();

                        // days existing?
                        if(resultDays.getCount() > 0) {
                            // create lists
                            List<BarEntry> listSolvedFalsely = new ArrayList<BarEntry>();

                            // date list
                            // the date on the x axis will not be auto generated
                            List<String> dateStringList = new ArrayList<String>();

                            // iterate
                            int indexDays = 0;
                            do {
                                // current date as string
                                String currentDateString = resultDays.getString(0);
                                dateStringList.add(currentDateString);

                                // query for data
                                String query2 = "SELECT COUNT(id) AS count, fk_t_assessment_item_id, date(started_timestamp, 'unixepoch', 'localtime') AS started, date(processed_timestamp, 'unixepoch', 'localtime') AS processed, how_solved FROM t_statistic WHERE started NOT NULL AND processed NOT NULL AND how_solved NOT NULL AND how_solved = 2 AND processed = '[date]' GROUP BY how_solved ORDER BY how_solved ASC;";
                                query2 = query2.replace("[date]", currentDateString);

                                // search in database for how_solved
                                Cursor resultSolved = database.rawQuery(query2, null);
                                resultSolved.moveToFirst();

                                // solved existing?
                                if(resultSolved.getCount() > 0) {
                                    int count_solved = resultSolved.getInt(0);

                                    listSolvedFalsely.add(new BarEntry(indexDays, count_solved));
                                } else {
                                    listSolvedFalsely.add(new BarEntry(indexDays, 0));
                                }

                                indexDays++;
                            } while (resultDays.moveToNext());

                            // entries to chart?
                            BarDataSet set3 = new BarDataSet(listSolvedFalsely, "Falsch");
                            set3.setColor(Color.rgb(25, 75, 125));

                            float barWidth = 0.9f; // x2 dataset

                            BarData data = new BarData(set3);
                            data.setBarWidth(barWidth);
                            data.setValueFormatter(new chartValueFormatter());
                            activity.barChart.setData(null);
                            activity.barChart.setData(data);
                            activity.barChart.getXAxis().setCenterAxisLabels(false);
                            activity.barChart.getXAxis().setLabelCount((resultDays.getCount() + 2));
                            activity.barChart.getXAxis().setValueFormatter(new AxisFormatterDateString(dateStringList));
                            activity.barChart.setVisibleXRangeMinimum((float) (resultDays.getCount() + 2));
                            activity.barChart.invalidate(); // refresh
                        }
                    } catch (Exception e) {
                        Log.e("Error", "ActivityStatistic onCreate() ExtendedOnClickListener (btnSolvedNotCorrect): " + e.getMessage());
                    }
                }
            }
        });

        // add buttons to layout
        layoutCategories.addView(btnLastImported);
        layoutCategories.addView(btnForRepeating);
        layoutCategories.addView(btnSolvedCorrect);
        layoutCategories.addView(btnAllAssessments);
        layoutCategories.addView(btnSolvedNotCorrect);
        // endregion

        // region date time toggle
        TextView tvStartPeriod = new TextView(this);
        tvStartPeriod.setId(View.generateViewId());
        tvStartPeriod.setInputType(InputType.TYPE_CLASS_DATETIME);
        tvStartPeriod.setWidth(200);

        TextView tvEndPeriod = new TextView(this);
        tvEndPeriod.setId(View.generateViewId());
        tvEndPeriod.setInputType(InputType.TYPE_CLASS_DATETIME);
        tvEndPeriod.setWidth(200);

        Button btnDatePickerStartPeriod = new Button(this);
        btnDatePickerStartPeriod.setId(View.generateViewId());
        btnDatePickerStartPeriod.setText(this.getString(R.string.activity_statistic_btn_select_start_period));
        btnDatePickerStartPeriod.setEnabled(false);
        btnDatePickerStartPeriod.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {tvStartPeriod, this, "start"}) {
            @Override
            public void onClick(View v) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.initDatePickerFragment((TextView) this.getObjects()[0], (ActivityStatistic) this.getObjects()[1], (String) this.getObjects()[2]);
                fragment.show(getFragmentManager(), "datePickerStartPeriod");
            }
        });

        Button btnDatePickerEndPeriod = new Button(this);
        btnDatePickerEndPeriod.setId(View.generateViewId());
        btnDatePickerEndPeriod.setText(this.getString(R.string.activity_statistic_btn_select_end_period));
        btnDatePickerEndPeriod.setEnabled(false);
        btnDatePickerEndPeriod.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {tvEndPeriod, this, "end"}) {
            @Override
            public void onClick(View v) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.initDatePickerFragment((TextView) this.getObjects()[0], (ActivityStatistic) this.getObjects()[1], (String) this.getObjects()[2]);
                fragment.show(getFragmentManager(), "datePickerEndPeriod");
            }
        });

        ExtendedToggleButton dateToggleButton = new ExtendedToggleButton(this, this.getString(R.string.activity_statistic_button_period_selection));
        dateToggleButton.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {btnDatePickerStartPeriod, btnDatePickerEndPeriod}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // activity
                ActivityStatistic currentActivity = (ActivityStatistic) this.getContext();

                // check state
                ExtendedToggleButton currentButton = (ExtendedToggleButton) view;
                if(currentButton.isAlreadyPressed()) {
                    // update state
                    currentButton.setAlreadyPressed(false);

                    // disable
                    ((Button) this.getObjects()[0]).setEnabled(false);
                    ((Button) this.getObjects()[1]).setEnabled(false);

                    // disable time period
                    currentActivity.enabledTimePeriod = false;
                    currentActivity.datePeriodStart = "";
                    currentActivity.datePeriodEnd = "";
                } else {
                    // update state
                    currentButton.setAlreadyPressed(true);

                    // enable buttons
                    ((Button) this.getObjects()[0]).setEnabled(true);
                    ((Button) this.getObjects()[1]).setEnabled(true);

                    // enable time period
                    currentActivity.enabledTimePeriod = true;
                }
            }
        });


        //ActivityStatistic currentActivity = (ActivityStatistic) this.getContext();
        //DialogFragment fragment = new DatePickerFragment();
        //fragment.show(getFragmentManager(), "datePicker");

        // add button to view
        ((LinearLayout) this.findViewById(R.id.linearLayoutDateToggle)).addView(dateToggleButton);
        ((LinearLayout) this.findViewById(R.id.linearLayoutDateToggle)).addView(btnDatePickerStartPeriod);
        ((LinearLayout) this.findViewById(R.id.linearLayoutDateToggle)).addView(tvStartPeriod);
        ((LinearLayout) this.findViewById(R.id.linearLayoutDateToggle)).addView(btnDatePickerEndPeriod);
        ((LinearLayout) this.findViewById(R.id.linearLayoutDateToggle)).addView(tvEndPeriod);
        // endregion

        // region available tags
        FlexboxLayout flexboxLayoutTags = (FlexboxLayout) this.findViewById(R.id.flexboxAvailableTags);
        flexboxLayoutTags.removeAllViews();
        this.tagToggleButtons.clear();
        DatabaseHelper helper = new DatabaseHelper(this);
        this.tagList = helper.getTagList();

        for(String tag:this.tagList) {
            ExtendedToggleButton toggleButton = new ExtendedToggleButton(this, tag);
            toggleButton.setId(View.generateViewId());
            toggleButton.setOnClickListener(new ExtendedOnClickListener(this, new Object[] {this, tag}) {
                @Override
                public void onClick(View view) {
                    super.onClick(view);

                    // clicked button
                    ExtendedToggleButton currentButton = (ExtendedToggleButton) view;

                    // current activity
                    ActivityStatistic activity = (ActivityStatistic) this.getObjects()[0];

                    // selected tag
                    String selectedTag = (String) this.getObjects()[1];

                    // button already pressed?
                    if(currentButton.isAlreadyPressed()) {
                        // remove chart data
                        activity.barChart.setData(null);
                        activity.barChart.invalidate();

                        currentButton.setAlreadyPressed(false);
                    } else {
                        currentButton.setAlreadyPressed(true);

                        // disable category buttons
                        activity.disableToggleButtons(currentButton);

                        // get data from database
                        DatabaseHelper helper = new DatabaseHelper(activity);
                        SQLiteDatabase database = helper.getReadableDatabase();

                        // get single days
                        String query = "";
                        if(activity.enabledTimePeriod) {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days_period);
                            query = query.replace("[period_start]", activity.datePeriodStart);
                            query = query.replace("[period_end]", activity.datePeriodEnd);
                        } else {
                            query = App.getStringContentFromRawFile(activity, R.raw.assessments_solved_get_days);
                        }
                        Cursor resultDays = database.rawQuery(query, null);
                        resultDays.moveToFirst();

                        // days existing?
                        if(resultDays.getCount() > 0) {
                            // create lists
                            List<BarEntry> listSolvedCorrectly = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedPartlyCorrect = new ArrayList<BarEntry>();
                            List<BarEntry> listSolvedFalsely = new ArrayList<BarEntry>();

                            // date list
                            // the date on the x axis will not be auto generated
                            List<String> dateStringList = new ArrayList<String>();

                            // iterate
                            int indexDays = 0;
                            do {
                                // current date as string
                                String currentDateString = resultDays.getString(0);
                                dateStringList.add(currentDateString);

                                // query for data
                                String query2 = App.getStringContentFromRawFile(activity, R.raw.statistic_by_tag);
                                query2 = query2.replace("[date]", currentDateString);
                                query2 = query2.replace("[tag_name]", selectedTag);

                                // search in database for how_solved
                                Cursor resultSolved = database.rawQuery(query2, null);
                                resultSolved.moveToFirst();

                                // solved existing?
                                if(resultSolved.getCount() > 0) {
                                    boolean setCorrectly = false;
                                    boolean setPartlyCorrect = false;
                                    boolean setFalsely = false;

                                    for(int i=0;i < 3;i++) {
                                        int count_solved = 0;
                                        int how_solved = 0;

                                        try {
                                            count_solved = resultSolved.getInt(0);
                                            how_solved = resultSolved.getInt(3);
                                        } catch (Exception e) {
                                            count_solved = 0;
                                            how_solved = i;
                                        }

                                        if(i==0 && !setCorrectly) {
                                            if(how_solved == 0) {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedCorrectly.add(new BarEntry(indexDays, 0));
                                            }

                                            setCorrectly = true;
                                        }

                                        if(i==1 && !setPartlyCorrect) {
                                            if(how_solved == 1) {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedPartlyCorrect.add(new BarEntry(indexDays, 0));
                                            }

                                            setPartlyCorrect = true;
                                        }

                                        if(i==2 && !setFalsely) {
                                            if(how_solved == 2) {
                                                listSolvedFalsely.add(new BarEntry(indexDays, count_solved));

                                                resultSolved.moveToNext();
                                            } else {
                                                listSolvedFalsely.add(new BarEntry(indexDays, 0));
                                            }

                                            setFalsely = true;
                                        }
                                    }
                                }

                                indexDays++;
                            } while (resultDays.moveToNext());

                            // entries to chart?
                            BarDataSet set1 = new BarDataSet(listSolvedCorrectly, "Richtig");
                            BarDataSet set2 = new BarDataSet(listSolvedPartlyCorrect, "Teilw. richtig");
                            BarDataSet set3 = new BarDataSet(listSolvedFalsely, "Falsch");
                            set1.setColor(Color.rgb(25, 75, 125));
                            set2.setColor(Color.rgb(75, 125, 175));
                            set3.setColor(Color.rgb(125, 175, 225));

                            float groupSpace = 0.07f;
                            float barSpace = 0.02f; // x2 dataset
                            float barWidth = 0.29f; // x2 dataset

                            BarData data = new BarData(set1, set2, set3);
                            data.setBarWidth(barWidth);
                            data.setValueFormatter(new chartValueFormatter());
                            activity.barChart.setData(data);
                            activity.barChart.groupBars(0f, groupSpace, barSpace); // perform the "explicit" grouping
                            activity.barChart.getXAxis().setCenterAxisLabels(true);
                            activity.barChart.getXAxis().setLabelCount((resultDays.getCount() + 2));
                            activity.barChart.getXAxis().setValueFormatter(new AxisFormatterDateString(dateStringList));
                            activity.barChart.setVisibleXRangeMinimum((float) (resultDays.getCount() + 2));
                            activity.barChart.invalidate(); // refresh
                        }
                    }
                }
            });
            flexboxLayoutTags.addView(toggleButton);
            this.tagToggleButtons.add(toggleButton);
        }
        // endregion

        // on create start
        btnAllAssessments.performClick();
    }

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

    /**
     * AxisFormatter class. Takes a list of date string for x axis formatting.
     */
    public static class AxisFormatterDateString implements IAxisValueFormatter {
        private List<String> dateStrings = new ArrayList<String>();

        public AxisFormatterDateString(List<String> dateStrings) {
            this.dateStrings = dateStrings;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // return date string from list at position (int) value
            try {
                String[] dateParts = dateStrings.get((int) value).split("-");
                return dateParts[2] + "." + dateParts[1] + "." + dateParts[0];
            } catch (Exception e) {
                //Log.e("Error", "ActivityStatistic AxisFormatterDateString getFormattedValue(): " + e.getMessage());
            }

            return "";
        }
    }

    // old implementation axis formatter
    // generates date strings with starting timestamp (adds 1 day each value)
    /*
    public static class AxisFormatterDateString implements IAxisValueFormatter {
        private long startTimestamp = 0;

        public MyAxisFormat(long startTimestamp) {
            this.startTimestamp = startTimestamp;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // get new timestamp
            long newTimestamp = (this.startTimestamp * 1000L) + ((long) ((int) value) * (24*60*60) * 1000L);

            // calculate date
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

            return formatter.format(newTimestamp);
        }
    }
    */

    public static class chartValueFormatter implements IValueFormatter {
        public chartValueFormatter() {
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            String stringValue = String.valueOf(value);

            try {
                return stringValue.substring(0, stringValue.indexOf("."));
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }

            return String.valueOf(value);
        }
    }
}

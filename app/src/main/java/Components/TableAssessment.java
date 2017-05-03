package Components;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import Comprehensive.App;
import Comprehensive.ExtendedButton;
import Comprehensive.ExtendedEditText;
import Comprehensive.UsersAssessmentResponse;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;

/**
 * Assessment class TableAssessment.
 * Contains nested class Value (with identifier) for the response declaration (correct response).
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class TableAssessment extends Assessment {
    // region object variables
    private List<Value> valueList;
    private List<Table> tableList;
    private List<ExtendedEditText> inputEditTexts = new ArrayList<ExtendedEditText>();
    // endregion

    // region button onclick listener
    protected View.OnClickListener checkUserResponseOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // user wants to check his response
            ExtendedButton clickedButton = (ExtendedButton) view;
            TableAssessment relatedAssessment = (TableAssessment) clickedButton.getAssessmentObject();
            relatedAssessment.handleUserResponse(view);
        }
    };
    // endregion

    @Override
    public void handleUserResponse(View view) {
        ExtendedButton clickedButton = (ExtendedButton) view;
        TableAssessment relatedAssessment = (TableAssessment) clickedButton.getAssessmentObject();

        // user response
        UsersAssessmentResponse response = UsersAssessmentResponse.Wrong;

        // mart correct and false answerts
        for(int i=0;i < relatedAssessment.getInputEditTexts().size();i++) {
            // current element
            ExtendedEditText editText = relatedAssessment.getInputEditTexts().get(i);

            // user input text
            String userInput = editText.getText().toString().toLowerCase();

            // correct value
            String correctValue = editText.getHiddenValue().toLowerCase();

            // lower bound
            double lowerBound = 75.0;   // default lower bound
            switch (correctValue.length()) {
                case 1:
                case 2: lowerBound = 100.0; break;
                case 3:
                case 6:
                case 9: lowerBound = 66.6; break;
                case 4: lowerBound = 50.0; break;
                case 5: lowerBound = 60.0; break;
                case 7: lowerBound = 57.0; break;
                case 8: lowerBound = 62.5; break;
                case 10: lowerBound = 70.0; break;
                case 11: lowerBound = 72.5; break;
            }

            // compare
            double percentage = App.getLevenshteinPercentage(correctValue, userInput);

            if(percentage >= lowerBound) {
                // correct
                if(!correctValue.equals("")) {
                    editText.setBackgroundResource(R.drawable.summary_solved_correct);
                }
            } else {
                // false
                editText.setBackgroundResource(R.drawable.summary_solved_falsely);
            }
        }

        // iterate each edit text
        for(int i=0;i < relatedAssessment.getInputEditTexts().size();i++) {
            // current element
            ExtendedEditText editText = relatedAssessment.getInputEditTexts().get(i);

            // user input text
            String userInput = editText.getText().toString().toLowerCase();

            // correct value
            String correctValue = editText.getHiddenValue().toLowerCase();

            if(!correctValue.equals("")) {
                // lower bound
                double lowerBound = 75.0;   // default lower bound
                switch (correctValue.length()) {
                    case 1:
                    case 2: lowerBound = 100.0; break;
                    case 3:
                    case 6:
                    case 9: lowerBound = 66.6; break;
                    case 4: lowerBound = 50.0; break;
                    case 5: lowerBound = 60.0; break;
                    case 7: lowerBound = 57.0; break;
                    case 8: lowerBound = 62.5; break;
                    case 10: lowerBound = 70.0; break;
                    case 11: lowerBound = 72.5; break;
                }

                // compare
                double percentage = App.getLevenshteinPercentage(correctValue, userInput);

                if(i == 0) {
                    if(percentage >= lowerBound) {
                        // answer is correct
                        response = UsersAssessmentResponse.Correct;
                    }
                } else {
                    if(percentage >= lowerBound) {
                        if(response.equals(UsersAssessmentResponse.Wrong)) {
                            response = UsersAssessmentResponse.Partly_Correct;
                            break;
                        }
                    } else {
                        if(response.equals(UsersAssessmentResponse.Correct)) {
                            response = UsersAssessmentResponse.Partly_Correct;
                            break;
                        }
                    }
                }
            }
        }

        this.handleUserResponseEnd(response);

        // disable check button
        this.checkButton.setEnabled(false);
    }

    @Override
    public void handleUserResponse(UsersAssessmentResponse response, View clickedView) {

    }

    // region constructors
    /**
     * Constructor 1 of class TableAssessment.
     * Creates new lists of tables and values
     */
    public TableAssessment() {
        this.setIdentifier("table");
        this.valueList = new ArrayList<Value>();
        this.tableList = new ArrayList<Table>();
    }
    // endregion

    // region object methods
    public void displayAssessment(Context context, LinearLayout targetLayout) {
        this.displayAssessmentStart(context, targetLayout);

        LinearLayout linearLayoutTables = new LinearLayout(context);
        linearLayoutTables.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams layoutParamsLinearLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutTables.setLayoutParams(layoutParamsLinearLayout);

        // reset input edit texts
        if(this.inputEditTexts != null) {
            this.inputEditTexts.clear();
        }

        // tables
        // iterate each table
        for(Table table:this.getTableList()) {
            // add table layout to linear layout
            linearLayoutTables.addView(App.getTableLayoutByTable(this, table, this.getContext()));
        }

        targetLayout.addView(linearLayoutTables);

        // check button
        this.checkButton = App.getNewCheckButton(targetLayout, this, this.checkUserResponseOnClickListener);

        // show support
        App.addSupportToLayout(this.getContext(), this);
    }

    /**
     * Direct method for adding a single (non-null) table to the internal table list.
     * Avoids calling getTableList().
     * Does not create a new table.
     * Checks first, if list != null (creates new list).
     * Before adding a new table, first a new table with rows and cells must be generated.
     *
     * @param table A table object.
     * @throws Exception Throws an exception, if parameter table is an empty object.
     */
    public void addTable(Table table) throws Exception {
        if(table == null) {
            throw new Exception("Row object is null.");
        }

        if(this.tableList == null) {
            this.tableList = new ArrayList<Table>();
        }

        this.tableList.add(table);
    }
    // endregion

    // region getter & setter

    public List<Value> getValueList() {
        return valueList;
    }

    public void setValueList(List<Value> valueList) {
        this.valueList = valueList;
    }

    public List<Table> getTableList() {
        return tableList;
    }

    public void setTableList(List<Table> tableList) {
        this.tableList = tableList;
    }

    public List<ExtendedEditText> getInputEditTexts() {
        return inputEditTexts;
    }

    public void setInputEditTexts(List<ExtendedEditText> inputEditText) {
        this.inputEditTexts = inputEditText;
    }

    // endregion

    /**
     * Inner class Value for response declaration purposes.
     * Contains an identifier and a value content.
     */
    public static class Value {
        // region object variables
        private String cellIdentifier;
        private String valueContent;
        // endregion

        // region constructors

        /**
         * Constructor for inner class Value.
         * Sets only internal variables with delivered parameters.
         * @param identifier CellIdentifier.
         * @param content Content of value.
         */
        public Value(String identifier, String content) {
            this.setCellIdentifier(identifier);
            this.setValueContent(content);
        }
        // endregion

        // region getter & setter

        public String getCellIdentifier() {
            return cellIdentifier;
        }

        public void setCellIdentifier(String cellIdentifier) {
            this.cellIdentifier = cellIdentifier;
        }

        public String getValueContent() {
            return valueContent;
        }

        public void setValueContent(String valueContent) {
            this.valueContent = valueContent;
        }

        // endregion
    }
}
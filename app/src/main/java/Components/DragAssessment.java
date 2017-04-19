package Components;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import Comprehensive.Application;
import Comprehensive.DatabaseHelper;
import Comprehensive.ExtendedButton;
import Comprehensive.ExtendedDragButton;
import Comprehensive.ExtendedEditText;
import Comprehensive.ExtendedLinearLayoutCell;
import Comprehensive.ExtendedOnClickListener;
import Comprehensive.ExtendedOnDragListener;
import Comprehensive.ExtendedOnTouchListener;
import Comprehensive.UsersAssessmentResponse;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;

/**
 * Assessment class Drag for Drag'n'Drop exercises.
 *
 * @author Dominik Liehr
 * @version 0.04
 */
public class DragAssessment extends Assessment implements AssessmentInterface {
    // region object variables
    private DragMode dragMode;
    private List<DragItem> dragItemList;
    private List<Table> tableList;
    private ExtendedDragButton draggedButton = null;
    private FlexboxLayout dragItemLayout = null;
    private ExtendedDragButton currentDragButton = null;
    // endregion

    // region contructors
    /**
     * Constructor for DragAssessment class.
     * Initialises drag mode.
     * @param mode Drag'n'Drop mode. COL or ROW.
     */
    public DragAssessment(DragMode mode) {
        this.setDragItemList(new ArrayList<DragItem>());
        this.setTableList(new ArrayList<Table>());
        this.setDragMode(mode);
        this.setIdentifier("dragndropTable");
    }
    // endregion

    // region interface
    @Override
    public void displayAssessment(Context context, LinearLayout targetLayout) {
        // start statistic
        this.startStatisticEntry();

        // remove views
        targetLayout.removeAllViews();

        // create assessment
        TextView tvTitle = new TextView(context);
        tvTitle.setText(this.getTitle());
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        tvTitle.setId(View.generateViewId());
        targetLayout.addView(tvTitle);

        // paragraphs
        this.displayItemBodyParagraphs(targetLayout);

        // prompt
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText(this.getPrompt());
        tvPrompt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        tvPrompt.setId(View.generateViewId());
        targetLayout.addView(tvPrompt);

        // background for flexbox
        GradientDrawable flexboxBackground = new GradientDrawable();
        flexboxBackground.setStroke(1, Color.rgb(0, 0, 0));
        flexboxBackground.setColor(Color.rgb(255, 255, 255));

        // layout params drag items
        LinearLayout.LayoutParams flexboxLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        flexboxLayoutParams.bottomMargin = 10;

        // get drag items
        this.dragItemLayout = new FlexboxLayout(this.getContext());
        this.dragItemLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP);
        this.dragItemLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_STRETCH);
        this.dragItemLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_STRETCH);
        this.dragItemLayout.setLayoutParams(flexboxLayoutParams);
        this.dragItemLayout.setBackground(flexboxBackground);
        this.dragItemLayout.setMinimumHeight(100);

        // drag listener of flexbox drag items
        this.dragItemLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return false;
            }
        });

        // touch listener for drag items - to start dragging
        for(int i=0;i < this.getDragItemList().size();i++) {
            DragItem item = this.getDragItemList().get(i);
            ExtendedDragButton dragButton = new ExtendedDragButton(this.getContext(), item.getItemValue(), new Object[] {item.getIdentifier()});
            dragButton.setId(View.generateViewId());

            dragButton.setOnTouchListener(new ExtendedOnTouchListener(this.getContext(), new Object[] {dragItemLayout, this, dragButton}) {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipData data = ClipData.newPlainText("dragButton" + String.valueOf(((ExtendedDragButton) this.getObjects()[2]).getId()), "button" + String.valueOf(((ExtendedDragButton) this.getObjects()[2]).getId()));
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                        view.startDrag(data, shadowBuilder, null, 0);

                        // set current dragged button
                        ExtendedDragButton draggedButton = (ExtendedDragButton) this.getObjects()[2];

                        // corresponding assessment item
                        DragAssessment assessment = (DragAssessment) this.getObjects()[1];
                        assessment.draggedButton = draggedButton;

                        return true;
                    } else {
                        return false;
                    }
                }
            });
            dragItemLayout.addView(dragButton);
        }

        // add drag items to layout
        targetLayout.addView(dragItemLayout);

        // get tables
        for(Table table:this.getTableList()) {
            targetLayout.addView(Application.getTableLayoutByTable(this, table, this.getContext()));
        }

        // show check button
        this.checkButton = Application.getNewCheckButton(targetLayout, this, new ExtendedOnClickListener(this.getContext(), new Object[] {this}) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // current assessment
                DragAssessment assessment = (DragAssessment) this.getObjects()[0];

                // handle user input
                // check if no buttons are left)
                if(assessment.dragItemLayout.getChildCount() == 0) {
                    assessment.handleUserResponse(view);
                } else {
                    Toast.makeText(assessment.getContext(), this.getContext().getString(R.string.activity_label_drag_assessment_message_drag_all_buttons), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // show support
        Application.addSupportToLayout(this.getContext(), this);
    }

    @Override
    public void handleUserResponse(View view) {
        // all buttons must be sorted correctly
        UsersAssessmentResponse response = UsersAssessmentResponse.Wrong;
        Boolean firstChecked = false;

        // iterate tablelayouts
        LinearLayout linearLayout = (LinearLayout) ((Activity) this.getContext()).findViewById(R.id.linearLayoutAssessmentContent);
        for(int i=0;i < linearLayout.getChildCount();i++) {
            if(linearLayout.getChildAt(i) instanceof TableLayout) {
                // table layout found, check buttons
                try {
                    // current table
                    TableLayout currentTable = (TableLayout) linearLayout.getChildAt(i);

                    // iterate rows
                    for(int j=0;j < currentTable.getChildCount();j++) {
                        // is child tablerow?
                        if(currentTable.getChildAt(j) instanceof TableRow) {
                            TableRow currentRow = (TableRow) currentTable.getChildAt(j);

                            // iterate child views from row
                            for(int l=0;l < currentRow.getChildCount();l++) {
                                // is child linear layout?
                                if(currentRow.getChildAt(l) instanceof ExtendedLinearLayoutCell) {
                                    // there can be a button
                                    // contains linear layout a button?
                                    ExtendedLinearLayoutCell currentCell = (ExtendedLinearLayoutCell) currentRow.getChildAt(l);

                                    if(currentCell.getChildCount() > 0) {
                                        if(currentCell.getChildAt(0) instanceof ExtendedDragButton) {
                                            ExtendedDragButton currentExtendedDragButton = (ExtendedDragButton) currentCell.getChildAt(0);

                                            String correctIdentifier = currentCell.getIdentifier();
                                            String userInputIdentifier = (String) currentExtendedDragButton.getObjects()[0];

                                            if(correctIdentifier.equals(userInputIdentifier)) {
                                                if(!firstChecked) {
                                                    response = UsersAssessmentResponse.Correct;
                                                    firstChecked = true;
                                                } else {
                                                    if(response.equals(UsersAssessmentResponse.Wrong)) {
                                                        response = UsersAssessmentResponse.Partly_Correct;
                                                    }
                                                }
                                            } else {
                                                if(!firstChecked) {
                                                    if(response.equals(UsersAssessmentResponse.Correct)) {
                                                        response = UsersAssessmentResponse.Partly_Correct;
                                                    }
                                                    firstChecked = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Error", "DragAssessment handleUserResponse: " + e.getMessage());
                }
            }
        }

        // assessments to process
        if(!this.isAlreadySolved()) {
            ActivityLearn.assessmentsToProcess--;
        }

        // statistic
        this.completeStatisticEntry(response);

        // user response
        Application.showUserResponse(this.getContext(), (LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling), response);

        // mark solved
        this.setAlreadySolved(true);
        this.setHowSolved(response);

        // buttons
        if(ActivityLearn.assessmentsToProcess == 0) {
            // disable buttons
            Application.disableLearnButtons(this.getContext());

            // show summary button (only #assessments > 1)
            if(((ActivityLearn) this.getContext()).assessments.size() > 1) {
                Button b = new Button(this.getContext());
                b.setText(this.getContext().getString(R.string.activity_learn_message_all_assessments_solved));
                b.setOnClickListener(Application.getNewSummaryOnClickListener(this.getContext()));
                ((LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling)).addView(b);
            }
        }

        // disable check button
        this.checkButton.setEnabled(false);
    }

    @Override
    public void handleUserResponse(UsersAssessmentResponse response, View clickedView) {}

    // endregion

    // region object methods
    /**
     * Direct method for adding a single (non-null) drag item to the internal drag item list.
     * Avoids calling getDragItemList().
     * Does not create a new drag item.
     * Checks first, if list != null (creates new list).
     *
     * @param item A drag item object.
     * @throws Exception Throws an exception, if parameter item is an empty object.
     */
    public void addDragItem(DragItem item) throws  Exception {
        if(item == null) {
            throw new Exception("DragItem object is null.");
        }

        if(this.dragItemList == null) {
            this.dragItemList = new ArrayList<DragItem>();
        }

        this.dragItemList.add(item);
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

    public DragMode getDragMode() {
        return dragMode;
    }

    public void setDragMode(DragMode dragMode) {
        this.dragMode = dragMode;
    }

    public List<DragItem> getDragItemList() {
        return dragItemList;
    }

    public void setDragItemList(List<DragItem> dragItemList) {
        this.dragItemList = dragItemList;
    }

    public List<Table> getTableList() {
        return tableList;
    }

    public void setTableList(List<Table> tableList) {
        this.tableList = tableList;
    }

    public ExtendedDragButton getDraggedButton() {
        return draggedButton;
    }

    public void setDraggedButton(ExtendedDragButton draggedButton) {
        this.draggedButton = draggedButton;
    }

    public FlexboxLayout getDragItemLayout() {
        return dragItemLayout;
    }

    // endregion

    // region enumerations
    public enum DragMode {
        COL,
        ROW
    }
    // endregion

    /**
     * Inner class for representating a drag item.
     * Contains an identifier and an item value (label).
     */
    public static class DragItem {
        // region object variables
        private String identifier;
        private String itemValue;
        // endregion

        // region constructors

        /**
         * Initialises a drag item.
         *
         * @param identifier Identifier of the drag item.
         * @param itemValue Value / Label of the drag item.
         */
        public DragItem(String identifier, String itemValue) {
            this.setIdentifier(identifier);
            this.setItemValue(itemValue);
        }
        // endregion

        // region getter & setter

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getItemValue() {
            return itemValue;
        }

        public void setItemValue(String itemValue) {
            this.itemValue = itemValue;
        }

        // endregion
    }
}

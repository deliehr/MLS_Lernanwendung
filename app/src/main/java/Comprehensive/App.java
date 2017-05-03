package Comprehensive;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;/**/
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;


import Components.Assessment;
import Components.Cell;
import Components.DragAssessment;
import Components.DragCell;
import Components.MediaSupport;
import Components.Row;
import Components.SelectionSupport;
import Components.StandardCell;
import Components.Table;
import Components.TableAssessment;
import Components.TableSupport;
import Components.TextboxSupport;
import it.liehr.mls_app.ActivityLearn;
import it.liehr.mls_app.R;
import Comprehensive.DatabaseHelper.TableNames;

public class App {
    // region application configuration finals
    public final static String relativeExtractDataDirectory = "/extract/";
    public final static String relativeWorkingDataDirectory = "/work/";
    public final static String relativeTmpDownloadFile = "/tmp/download.zip";
    public final static int supportTextTextSize = 16;    // dp / dip
    public static String urlHashMapFilePath = "urls.hashmap";
    //public final static String regularExpressionPackageTitleAndUrl = "[a-zA-Z_-\\d]*:http(s?)://[a-zA-Z_./-\\d]*(.zip)";
    public final static String regularExpressionPackageTitleAndUrl = "[a-zA-Z_-{0-9}]*:http(s?)://[a-zA-Z_./-{0-9}]*(.zip)";
    // endregion

    public static int convertDpToPx(Activity activity, int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;

        return (int) Math.ceil(dp * logicalDensity);
    }

    public static void addSupportToLayout(Context context, Assessment assessment) {
        // scroll view for support
        ScrollView scrollView = (ScrollView) ((Activity) context).findViewById(R.id.scrollViewLearn);

        // target layout
        ScrollView.LayoutParams targetLinearLayoutParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        targetLinearLayoutParams.setMargins(10, 10, 10, 10);

        LinearLayout targetLinearLayout = new LinearLayout(context);
        targetLinearLayout.setOrientation(LinearLayout.VERTICAL);
        targetLinearLayout.setLayoutParams(targetLinearLayoutParams);

        // clear all views from layout
        scrollView.removeAllViews();
        targetLinearLayout.removeAllViews();

        // set null background
        scrollView.setBackgroundResource(0);

        // layout parameters
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(scrollViewParams);

        // read support from database
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor result = helper.getReadableDatabase().query(false, TableNames.SUPPORT, new String[] {"id", "uuid", "identifier"}, "assessment_uuid = ?", new String[]{assessment.getUuid()}, null, null, null, null);
        result.moveToFirst();

        // support existing?
        if(result.getCount() > 0) {
            // add linear layout to scroll view
            scrollView.addView(targetLinearLayout);

            // set background
            scrollView.setBackgroundResource(R.drawable.support_layout);

            // new layout params for scrollview
            scrollViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            scrollViewParams.weight = 2;
            scrollView.setLayoutParams(scrollViewParams);

            // title element
            // R.string.activity_learn_title_support
            String titleString = context.getResources().getText(R.string.activity_learn_title_support).toString();
            SpannableString spannableTitleString = new SpannableString(titleString);
            spannableTitleString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableTitleString.length(), 0);
            spannableTitleString.setSpan(new ForegroundColorSpan(Color.rgb(255, 255, 255)), 0, spannableTitleString.length(), 0);

            LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleLayout.setMargins(0, 0, 0, 10);

            TextView tvTitle = new TextView(context);
            tvTitle.setText(spannableTitleString);
            tvTitle.setBackgroundResource(R.drawable.support_title_textview);
            tvTitle.setLayoutParams(titleLayout);
            tvTitle.setGravity(Gravity.CENTER);
            tvTitle.setPadding(10, 10, 10, 10);
            targetLinearLayout.addView(tvTitle);

            // support identifier
            String supportId = result.getString(0);
            String supportUuid = result.getString(1);
            String supportIdentifier = result.getString(2);

            // distinguish by support type
            switch (supportIdentifier) {
                case "textbox":
                    try {
                        result = helper.getReadableDatabase().query(false, TableNames.SUPPORT_TEXTBOX, new String[] {"textbox_content"}, "fk_t_support_id = ?", new String[] {supportId}, null, null, null, null);
                        result.moveToFirst();

                        TextboxSupport textboxSupport = new TextboxSupport(supportUuid, result.getString(0));
                        textboxSupport.displaySupport(context, targetLinearLayout);
                    } catch (Exception e) {
                        Log.e("Error", "Method addSupportToLayout (textbox): " + e.getMessage());
                    }

                    break;

                case "image":
                case "video":
                    try {
                        result = helper.getReadableDatabase().query(false, TableNames.SUPPORT_MEDIA, new String[] {"media_type", "media_source", "prompt"}, "fk_t_support_id = ?", new String[] {supportId}, null, null, null, null);
                        result.moveToFirst();

                        MediaSupport mediaSupport = new MediaSupport(supportUuid, supportIdentifier);
                        mediaSupport.setMediaSource(result.getString(1));
                        mediaSupport.setPrompt(result.getString(2));
                        mediaSupport.displaySupport(context, targetLinearLayout);
                    } catch (Exception e) {
                        Log.e("Error", "Method addSupportToLayout (image/video): " + e.getMessage());
                    }

                    break;

                case "selection":
                    try {
                        result = helper.getReadableDatabase().query(false, TableNames.SUPPORT_SELECTION, new String[] {"id","prompt"}, "fk_t_support_id = ?", new String[] {supportId}, null, null, null, null);
                        result.moveToFirst();

                        String selectId = result.getString(0);
                        String selectPrompt = result.getString(1);

                        SelectionSupport selectionSupport = new SelectionSupport(supportUuid, selectPrompt);

                        Cursor selectionResult = helper.getReadableDatabase().query(false, TableNames.SELECTION_ITEM, new String[] {"select_value"}, "fk_t_support_selection_id = ?", new String[] {selectId}, null, null, null, null);
                        selectionResult.moveToFirst();

                        do {
                            selectionSupport.getSelections().add(selectionResult.getString(0));
                        } while (selectionResult.moveToNext());

                        selectionSupport.displaySupport(context, targetLinearLayout);
                    } catch (Exception e) {
                        Log.e("Error", "Method addSupportToLayout (selection): " + e.getMessage());
                    }

                    break;

                case "table":
                    try {
                        result = helper.getReadableDatabase().query(false, TableNames.SUPPORT_TABLE, new String[] {"id","prompt"}, "fk_t_support_id = ?", new String[] {supportId}, null, null, null, null);
                        result.moveToFirst();

                        String tableSupportId = result.getString(0);
                        String tablePrompt = result.getString(1);

                        TableSupport tableSupport = new TableSupport(supportUuid);
                        tableSupport.setPrompt(tablePrompt);

                        // region get table
                        result = helper.getReadableDatabase().query(false, TableNames.TABLE, new String[] {"id"}, "fk_t_support_table_id = ?", new String[] {tableSupportId}, null, null, null, null);
                        result.moveToFirst();

                        // new table
                        Table table = new Table();

                        // table data
                        String tableId = result.getString(0);

                        // get rows
                        Cursor resultRows = helper.getReadableDatabase().query(false, TableNames.ROW, new String[] {"id"}, "fk_t_table_id = ?", new String[] {tableId}, null, null, null, null);
                        resultRows.moveToFirst();

                        // iterate rows
                        do {
                            // new row
                            Row row = new Row();

                            // row data
                            String rowId = resultRows.getString(0);

                            // get cells
                            Cursor resultCells = helper.getReadableDatabase().query(false, TableNames.CELL, new String[] {"cell_identifier", "value", "head", "colspan", "writeable"}, "fk_t_row_id = ?", new String[] {rowId}, null, null, null, null);
                            resultCells.moveToFirst();

                            // iterate cells
                            do {
                                // new cell
                                StandardCell cell = new StandardCell();

                                // cell data
                                cell.setIdentifier(resultCells.getString(0));
                                cell.setCellValue(resultCells.getString(1));
                                cell.setHead(App.parseSQLLiteBoolean(resultCells.getInt(2)));
                                cell.setColspan(resultCells.getInt(3));
                                cell.setWriteable(App.parseSQLLiteBoolean(resultCells.getInt(4)));

                                // add cell
                                row.addCell(cell);
                            } while(resultCells.moveToNext());

                            // add row to table
                            table.getRowList().add(row);
                        } while(resultRows.moveToNext());

                        // add table to assessment
                        tableSupport.setTable(table);
                        // endregion

                        tableSupport.displaySupport(context, targetLinearLayout);
                    } catch (Exception e) {
                        Log.e("Error", "Method addSupportToLayout (table): " + e.getMessage());
                    }

                    break;
            }
        }
    }

    public static String getStringContentFromRawFile(Context context, int resourceId) {
        // try to read the raw file
        try {
            StringBuilder stringBuilder = new StringBuilder();
            InputStream is = context.getResources().openRawResource(resourceId);
            //byte[] buffer = new byte[is.available()];
            byte[] buffer = new byte[1];
            while(is.read(buffer) != -1) {
                stringBuilder.append(new String(buffer, "ISO-8859-1"));
            }

            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("Error", "Exception raw file: " + e.getMessage());
        }

        return "";
    }

    public static String[] getMultipleQueries(String singleQuery) {
        singleQuery = singleQuery.replace("\n", "");

        return singleQuery.split(";");
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        try {
            fileOrDirectory.delete();
        } catch (Exception e) {
            Log.e("Error", "Recursive delete: " + e.getMessage());
        }
    }

    public static double getLevenshteinPercentage(String correctValue, String inputValue) {
        int distance = StringUtils.getLevenshteinDistance(correctValue, inputValue);

        if(distance == 0) {
            return 100.0;
        }

        int remain = correctValue.length() - distance;

        return Double.valueOf(remain) / Double.valueOf(correctValue.length());
    }

    /**
     * Get a new check button programmatically.
     * @param targetLinearLayout The target linear layout to add the button to
     * @param assessmentObject The referenced calling assessment object
     * @param onClickListener The on click listener, defined in the assessment object (not public)
     * @return A new ExtendedButton
     */
    public static ExtendedButton getNewCheckButton(LinearLayout targetLinearLayout, Assessment assessmentObject, View.OnClickListener onClickListener) {
        // button params
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //buttonParams.width = 300;

        // new button
        ExtendedButton checkButton = new ExtendedButton(assessmentObject.getContext());
        checkButton.setId(View.generateViewId());
        checkButton.setAssessmentObject(assessmentObject);
        checkButton.setText(R.string.activity_learn_button_check_user_response);
        checkButton.setOnClickListener(onClickListener);
        checkButton.setLayoutParams(buttonParams);
        targetLinearLayout.addView(checkButton);

        return checkButton;
    }

    /**
     * General method for displaying a table
     * @param table
     * @param context
     * @return The parsed table as TableLayout
     */
    public static TableLayout getTableLayoutByTable(Object relatedObject, final Table table, Context context) {
        // cell properties
        int defaultCellWidth = App.convertDpToPx((Activity) context, 50);
        int defaultCellHeight = App.convertDpToPx((Activity) context, 20);

        // table in supprt?
        if(relatedObject instanceof TableAssessment || relatedObject instanceof DragAssessment) {
            defaultCellWidth = App.convertDpToPx((Activity) context, 200);
            defaultCellHeight = App.convertDpToPx((Activity) context, 50);
        }

        // new table
        TableLayout tmpTableLayout = new TableLayout(context);
        tmpTableLayout.setId(View.generateViewId());
        tmpTableLayout.setBackgroundResource(R.drawable.table_background);

        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams();
        tableLayoutParams.setMargins(0, 0, 0, 25);

        tmpTableLayout.setLayoutParams(tableLayoutParams);

        // search for drag identifiers, only search if drag assessment
        // identifiers
        String[] columnIdentifiers = new String[]{};
        String[] rowIdentifiers = new String[]{};

        if(relatedObject instanceof DragAssessment) {
            int maxCountColumns = 0;
            int maxCountRows = 0;

            if(((DragAssessment) relatedObject).getDragMode().equals(DragAssessment.DragMode.COL)) {
                // DragMode.COL
                // count max column identifiers
                for(Row currentRow:table.getRowList()) {
                    if(currentRow.getCellList().size() >= maxCountColumns) {
                        maxCountColumns = currentRow.getCellList().size();
                    }
                }

                // column identifiers
                columnIdentifiers = new String[maxCountColumns];

                // search for columns identifiers
                for(Row currentRow:table.getRowList()) {
                    for(int i=0;i < currentRow.getCellList().size();i++) {
                        Cell currentCell = currentRow.getCellList().get(i);

                        if(currentCell instanceof DragCell) {
                            DragCell dragCell = (DragCell) currentCell;

                            if(!dragCell.getDragIdentifier().equals("")) {
                                columnIdentifiers[i] = dragCell.getDragIdentifier();
                            }
                        }
                    }
                }
            }
        }

        // rows of tables
        for(Row currentRow:table.getRowList()) {
            //TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            //TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            //rowParams.weight = 1;

            TableRow row = new TableRow(context);
            //row.setLayoutParams(rowParams);
            row.setId(View.generateViewId());

            // add cells to row
            for(int i=0;i < currentRow.getCellList().size();i++) {
                // current cell
                Cell cell = currentRow.getCellList().get(i);

                // distinguish between cell types
                if(cell instanceof StandardCell) {
                    // region standard cell
                    StandardCell standardCell = (StandardCell) cell;
                    int width = defaultCellWidth * cell.getColspan();
                    int height = defaultCellHeight;

                    // TypedValue.COMPLEX_UNIT_DIP  //Device Independent Pixels
                    TableRow.LayoutParams cellParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    cellParams.span = cell.getColspan();
                    cellParams.weight = 1;

                    // head cell?
                    if(standardCell.isHead()) {
                        // head, not writeable, bold font
                        SpannableString spannedString = new SpannableString(standardCell.getCellValue());
                        spannedString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannedString.length(), 0);

                        TextView tvCell = new TextView(context);
                        tvCell.setId(View.generateViewId());
                        tvCell.setLayoutParams(cellParams);
                        tvCell.setMinHeight(height);
                        tvCell.setMinWidth(width);
                        tvCell.setText(spannedString);
                        tvCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        tvCell.setGravity(Gravity.CENTER);
                        tvCell.setBackgroundResource(R.drawable.table_cell_head);
                        row.addView(tvCell);
                    } else {
                        // not head, writeable?
                        if(standardCell.isWriteable() && relatedObject instanceof TableAssessment) {
                            // writeable
                            ExtendedEditText etCell = new ExtendedEditText(context);
                            etCell.setId(View.generateViewId());
                            etCell.setLayoutParams(cellParams);
                            etCell.setMinHeight(height);
                            etCell.setMinWidth(width);
                            etCell.setText(standardCell.getCellValue());
                            etCell.setBackgroundResource(R.drawable.table_cell_writeable);
                            etCell.setGravity(Gravity.CENTER);

                            // assessment type?
                            String hiddenValue = "";
                            if(relatedObject instanceof TableAssessment) {
                                // get value to type in
                                String identifier = standardCell.getIdentifier();
                                for(TableAssessment.Value value:((TableAssessment) relatedObject).getValueList()) {
                                    if(value.getCellIdentifier().equals(standardCell.getIdentifier())) {
                                        hiddenValue = value.getValueContent();
                                        break;
                                    }
                                }

                                ((TableAssessment) relatedObject).getInputEditTexts().add(etCell);
                            }

                            etCell.setHiddenValue(hiddenValue);
                            row.addView(etCell);
                        } else {
                            // not writeable
                            TextView tvCell = new TextView(context);
                            tvCell.setId(View.generateViewId());
                            tvCell.setLayoutParams(cellParams);
                            tvCell.setText(standardCell.getCellValue());
                            tvCell.setGravity(Gravity.CENTER);
                            tvCell.setBackgroundResource(R.drawable.table_cell_not_writeable);
                            tvCell.setMinHeight(height);
                            tvCell.setMinWidth(width);
                            row.addView(tvCell);
                        }
                    }
                    // endregion
                }

                // search in this row for row identifier, only if row mode
                String rowIdentifier = "";
                if(relatedObject instanceof DragAssessment) {
                    if(((DragAssessment) relatedObject).getDragMode() == DragAssessment.DragMode.ROW) {
                        for(Cell dragCell:currentRow.getCellList()) {
                            try {
                                DragCell tmpCell = (DragCell) dragCell;

                                if(!tmpCell.getDragIdentifier().equals("")) {
                                    rowIdentifier = tmpCell.getDragIdentifier();
                                    break;
                                }
                            } catch (Exception e) {
                                Log.e("Error", "App getTableLayoutByTable(): " + e.getMessage());
                            }
                        }
                    }
                }

                if(cell instanceof DragCell && relatedObject instanceof DragAssessment) {
                    // region drag cell
                    DragCell dragCell = (DragCell) cell;

                    // assessment
                    DragAssessment assessment = (DragAssessment) relatedObject;

                    //int width = defaultCellWidth * cell.getColspan();
                    int height = defaultCellHeight;

                    // TypedValue.COMPLEX_UNIT_DIP  //Device Independent Pixels
                    TableRow.LayoutParams cellParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    cellParams.span = cell.getColspan();
                    cellParams.weight = 1;
                    cellParams.setMargins(0,0,0,0);

                    // head cell?
                    if(dragCell.isHead()) {
                        // head
                        // not writeable
                        // bold font
                        SpannableString spannedString = new SpannableString(dragCell.getCellValue());
                        spannedString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannedString.length(), 0);

                        TextView tvCell = new TextView(context);
                        tvCell.setId(View.generateViewId());
                        tvCell.setMinHeight(height);
                        //tvCell.setMinWidth(width);
                        tvCell.setText(spannedString);
                        tvCell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        tvCell.setGravity(Gravity.CENTER);
                        tvCell.setLayoutParams(cellParams);
                        tvCell.setBackgroundResource(R.drawable.table_cell_head);
                        row.addView(tvCell);
                    } else {
                        // not writeable
                        // it's a target cell
                        ExtendedLinearLayoutCell linearCell = new ExtendedLinearLayoutCell(context);
                        linearCell.setId(View.generateViewId());
                        linearCell.setLayoutParams(cellParams);
                        linearCell.setGravity(Gravity.CENTER);
                        linearCell.setBackgroundResource(R.drawable.table_cell_not_writeable);
                        linearCell.setMinimumHeight(height);


                        if(((DragAssessment) relatedObject).getDragMode() == DragAssessment.DragMode.COL) {
                            // col
                            try {
                                linearCell.setIdentifier(columnIdentifiers[i]);
                            } catch (Exception e) {
                                Log.e("Error", "App getTableLayoutByTable(): " + e.getMessage());
                            }
                        } else {
                            // row
                            if(!rowIdentifier.equals("")) {
                                linearCell.setIdentifier(rowIdentifier);
                            }
                        }

                        linearCell.setOnDragListener(new ExtendedOnDragListener(context, new Object[] {(DragAssessment) relatedObject}) {
                            @Override
                            public boolean onDrag(View view, DragEvent dragEvent) {
                                try {
                                    if(dragEvent.getAction() == DragEvent.ACTION_DROP) {
                                        // target linear layout
                                        LinearLayout targetLinearLayout = (LinearLayout) view;

                                        // related assessment
                                        DragAssessment assessment = (DragAssessment) this.getObjects()[0];

                                        // check first, if target layout already contains a button
                                        if(targetLinearLayout.getChildCount() > 0) {
                                            // must contain button
                                            try {
                                                // try to get button
                                                ExtendedDragButton dragButton = (ExtendedDragButton) targetLinearLayout.getChildAt(0);

                                                // parent view linear layout => switch !
                                                View parentView = (View) dragButton.getParent();
                                                if(parentView instanceof LinearLayout) {

                                                }

                                                // remove all views from target layout
                                                targetLinearLayout.removeAllViews();

                                                // add button to available drag items
                                                assessment.getDragItemLayout().addView(dragButton);
                                            } catch (Exception e) {
                                                Log.e("Error", "App getLayoutTable ExtendedOnDragListener: " + e.getMessage());
                                            }
                                        }

                                        // add button to linear layout
                                        ExtendedDragButton dragButton = assessment.getDraggedButton();
                                        assessment.setDraggedButton(null);

                                        // check if drag button is child of parent
                                        if(dragButton.getParent() != null) {
                                            try {
                                                // parent view is probably a linear layout
                                                View parentView = (View) dragButton.getParent();

                                                if(parentView instanceof FlexboxLayout) {
                                                    FlexboxLayout sourceFlexboxLayout = (FlexboxLayout) dragButton.getParent();
                                                    sourceFlexboxLayout.removeView(dragButton);
                                                } else if(parentView instanceof LinearLayout) {
                                                    LinearLayout sourceLinearLayout = (LinearLayout) dragButton.getParent();
                                                    sourceLinearLayout.removeView(dragButton);
                                                } else {

                                                }
                                            } catch (Exception e) {
                                                Log.e("Error", "App getLayoutTable ExtendedOnDragListener: " + e.getMessage());
                                            }
                                        }

                                        // add current button to target view
                                        targetLinearLayout.addView(dragButton);
                                    }
                                } catch (Exception e) {
                                    Log.e("Error", "OnDragListener linearCell: " + e.getMessage());
                                    return false;
                                }

                                return true;
                            }
                        });

                        row.addView(linearCell);
                    }
                    // endregion
                }
            }

            // add row to table
            tmpTableLayout.addView(row);
        }

        return tmpTableLayout;
    }

    /**
     * Compares the bool-values stored as integers in sqlite databases and parses them as true or false
     * @param bool The bool value as integer: 0=false, 1=true
     * @return The real bool value
     */
    public static boolean parseSQLLiteBoolean(int bool) {
        return (bool == 1);
    }

    /**
     * Checks the connection to a specified url.
     *
     * @param url URL to specified download package
     * @return True, if a stream to the specified url can be opened. False, if not.
     */
    public static boolean checkConnection(URL url) {
        try {
            InputStream inputStream = url.openStream();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static String getStringContentFromFile(File file) {
        StringBuilder content = new StringBuilder();

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            while((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
        } catch (Exception e) {
            content.append("");
        }


        return content.toString();
    }

    /**
     * Nested class for error handling while parsing a xml file via Sax.
     * For testing a xml file with a specified dtd.
     * Not used in common import process.
     */
    private static class SAXErrorHandler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            Log.w("Warning", "SAXParseException: " + e.getMessage());
        }

        public void error(SAXParseException e) throws SAXException {
            Log.e("Error", "SAXParseException: " + e.getMessage());
        }

        public void fatalError(SAXParseException e) throws SAXException {
            Log.e("Error", "SAXParseException: " + e.getMessage());
        }
    }

    public static void showUserResponse(Context context, LinearLayout targetLinearLayout, UsersAssessmentResponse response) {
        // user response
        // params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // text view
        TextView responseTextView = new TextView(context);
        responseTextView.setGravity(Gravity.CENTER);
        responseTextView.setLayoutParams(params);
        responseTextView.setPadding(10,10,10,10);
        String responseText = "";

        switch (response.toString()) {
            case "Correct": {
                responseText = context.getString(R.string.activity_learn_response_label_correct);
                responseTextView.setBackgroundResource(R.drawable.correct_answer);
                break;
            }
            case "Partly_Correct": {
                responseText = context.getString(R.string.activity_learn_response_label_partly_correct);
                responseTextView.setBackgroundResource(R.drawable.partly_correct_answer);
                break;
            }
            default: {
                responseText = context.getString(R.string.activity_learn_response_label_wrong);
                responseTextView.setBackgroundResource(R.drawable.wrong_answer);
                break;
            }
        }

        // text size
        SpannableString spanString = new SpannableString(responseText);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        spanString.setSpan(new RelativeSizeSpan(1.5f), 0, spanString.length(), 0);

        responseTextView.setText(spanString);
        targetLinearLayout.addView(responseTextView);

        // scroll down
        final ScrollView scrollView = (ScrollView) ((Activity) context).findViewById(R.id.scrollViewLearnActivity);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public static void disableLearnButtons(Context context) {
        ((Button) ((Activity) context).findViewById(R.id.btnPrevAssessment)).setEnabled(false);
        ((Button) ((Activity) context).findViewById(R.id.btnNextAssessment)).setEnabled(false);
    }

    public static ExtendedOnClickListener getNewSummaryOnClickListener(final Context context) {
        ExtendedOnClickListener listener = new ExtendedOnClickListener(context) {
            @Override
            public void onClick(View view) {
                super.onClick(view);

                // remove views
                ((LinearLayout) ((Activity) this.getContext()).findViewById(R.id.layoutAssessmentHandling)).removeAllViews();
                //((ScrollView) ((Activity) this.getContext()).findViewById(R.id.scrollViewLearn)).removeAllViews();

                if(this.getContext() instanceof ActivityLearn) {
                    ActivityLearn activityLearn = (ActivityLearn) this.getContext();

                    LinearLayout summaryLayout = new LinearLayout(this.getContext());
                    summaryLayout.setId(View.generateViewId());
                    summaryLayout.setOrientation(LinearLayout.VERTICAL);

                    SpannableString spanString = new SpannableString(context.getString(R.string.activity_learn_summary_title));
                    spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                    spanString.setSpan(new RelativeSizeSpan(1.5f), 0, spanString.length(), 0);
                    spanString.setSpan(new ForegroundColorSpan(Color.rgb(255,255,255)), 0, spanString.length(), 0);

                    TextView tvTitle = new TextView(this.getContext());
                    tvTitle.setId(View.generateViewId());
                    tvTitle.setBackgroundResource(R.drawable.activity_selection_textview_title);
                    tvTitle.setGravity(Gravity.CENTER);
                    tvTitle.setText(spanString);
                    summaryLayout.addView(tvTitle);

                    LinearLayout.LayoutParams singleLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    cellParams.weight = 1;

                    int padding = 10;

                    for(Assessment assessment:activityLearn.assessments) {
                        LinearLayout singleLayout = new LinearLayout(this.getContext());
                        singleLayout.setLayoutParams(singleLayoutParams);
                        singleLayout.setId(View.generateViewId());
                        singleLayout.setOrientation(LinearLayout.HORIZONTAL);

                        // type
                        String assessmentType = "";
                        switch (assessment.getIdentifier()) {
                            case "choice": { assessmentType = "Singlechoice"; break; }
                            case "choiceMultiple": { assessmentType = "Multiplechoice"; break; }
                            case "positionObjects": { assessmentType = "Hotspot"; break; }
                            case "table": { assessmentType = this.getContext().getString(R.string.keyword_table_assessment_type); break; }
                            case "dragndropTable": { assessmentType = "Drag'n'Drop"; break; }
                        }

                        TextView tvAssessmentType = new TextView(this.getContext());
                        tvAssessmentType.setId(View.generateViewId());
                        //tvAssessmentType.setLayoutParams(cellParams);
                        tvAssessmentType.setMinWidth(400);
                        tvAssessmentType.setText(assessmentType);
                        tvAssessmentType.setPadding(padding,padding,padding,padding);
                        tvAssessmentType.setBackgroundResource(R.drawable.summary_cell);
                        singleLayout.addView(tvAssessmentType);

                        // title
                        TextView tvAssessmentTitle = new TextView(this.getContext());
                        tvAssessmentTitle.setLayoutParams(cellParams);
                        tvAssessmentTitle.setId(View.generateViewId());
                        tvAssessmentTitle.setText(assessment.getTitle());
                        tvAssessmentTitle.setPadding(padding,padding,padding,padding);
                        tvAssessmentTitle.setBackgroundResource(R.drawable.summary_cell);
                        singleLayout.addView(tvAssessmentTitle);

                        // how solved
                        // title
                        TextView tvHowSolved = new TextView(this.getContext());
                        tvHowSolved.setId(View.generateViewId());
                        tvHowSolved.setMinWidth(300);
                        //tvHowSolved.setLayoutParams(cellParams);
                        tvHowSolved.setPadding(padding,padding,padding,padding);

                        switch (assessment.getHowSolved().toString()) {
                            case "Correct": {
                                tvHowSolved.setText(this.getContext().getString(R.string.keyword_solved_correctly));
                                tvHowSolved.setBackgroundResource(R.drawable.summary_solved_correct);
                                break;
                            }
                            case "Partly_Correct": {
                                tvHowSolved.setText(this.getContext().getString(R.string.keyword_solved_partly_correct));
                                tvHowSolved.setBackgroundResource(R.drawable.summary_solved_partly_correct);
                                break;
                            }
                            case "Wrong": {
                                tvHowSolved.setText(this.getContext().getString(R.string.keyword_solved_falsely));
                                tvHowSolved.setBackgroundResource(R.drawable.summary_solved_falsely);
                                break;
                            }
                        }

                        singleLayout.addView(tvHowSolved);

                        // add single layout to summary layout
                        summaryLayout.addView(singleLayout);
                    }

                    LinearLayout.LayoutParams supportScrollViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    supportScrollViewParams.weight = 1;

                    ((ScrollView) ((Activity) this.getContext()).findViewById(R.id.scrollViewLearn)).removeAllViews();
                    ((ScrollView) ((Activity) this.getContext()).findViewById(R.id.scrollViewLearn)).setLayoutParams(supportScrollViewParams);
                    ((LinearLayout) ((Activity) this.getContext()).findViewById(R.id.linearLayoutAssessmentContent)).removeAllViews();
                    ((LinearLayout) ((Activity) this.getContext()).findViewById(R.id.linearLayoutAssessmentContent)).addView(summaryLayout);
                }
            }
        };

        return listener;
    }

    public static void createFile(String outputFile, Context context, int ressourceId) throws IOException {
        final OutputStream outputStream = new FileOutputStream(outputFile);

        Resources resources = context.getResources();
        byte[] largeBuffer = new byte[1024 * 4];
        int totalBytes = 0;
        int bytesRead = 0;

        InputStream inputStream = resources.openRawResource(ressourceId);
        while ((bytesRead = inputStream.read(largeBuffer)) > 0) {
            if (largeBuffer.length == bytesRead) {
                outputStream.write(largeBuffer);
            } else {
                byte[] shortBuffer = new byte[bytesRead];
                System.arraycopy(largeBuffer, 0, shortBuffer, 0, bytesRead);
                outputStream.write(shortBuffer);
            }
            totalBytes += bytesRead;
        }
        inputStream.close();

        outputStream.flush();
        outputStream.close();
    }
}
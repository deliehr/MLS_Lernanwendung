package Comprehensive;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import it.liehr.mls_app.ActivityStatistic;

/**
 * DatePickerFragment class.
 * Used in activity statistic for picking a date.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private TextView targetTextView;
    private ActivityStatistic activityStatistic;
    private String targetVariable;

    public DatePickerFragment() {
    }

    public void initDatePickerFragment(TextView targetTextView, ActivityStatistic activity, String targetVariable) {
        this.targetTextView = targetTextView;
        this.activityStatistic = activity;
        this.targetVariable = targetVariable;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // parse date
        String yearStr = String.valueOf(year);
        String monthStr = String.valueOf(month + 1);
        String dayStr = String.valueOf(day);

        if(monthStr.length() < 2) {
            monthStr = "0" + monthStr;
        }

        if(dayStr.length() < 2) {
            dayStr = "0" + dayStr;
        }

        String completeDateString = yearStr + "-" + monthStr + "-" + dayStr;

        // save to variable
        if(this.targetVariable.equals("start")) {
            this.activityStatistic.datePeriodStart = completeDateString;
        } else {
            this.activityStatistic.datePeriodEnd = completeDateString;
        }

        // Do something with the date chosen by the user
        this.targetTextView.setText(dayStr + "." + monthStr + "." + yearStr);
    }
}
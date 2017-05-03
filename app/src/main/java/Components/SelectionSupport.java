package Components;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Comprehensive.App;
import it.liehr.mls_app.R;

/**
 * Class for video support
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class SelectionSupport extends Support implements SupportInterface {
    // region object variables
    private List<String> selections;
    private String prompt;
    // endregion

    // region interface
    @Override
    public void displaySupport(Context context, LinearLayout targetLinearLayout) {
        // show prompt
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView prompt = new TextView(context);
        prompt.setText(this.getPrompt());
        prompt.setLayoutParams(tvParams);
        targetLinearLayout.addView(prompt);

        // spinner
        Spinner spinner = new Spinner(context);
        spinner.setMinimumHeight(App.convertDpToPx((Activity) context, 50));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, this.getSelections());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        targetLinearLayout.addView(spinner);
    }
    // endregion

    // region object methods
    public SelectionSupport(String uuid) {
        this.setPrompt("");
        this.setSelections(new ArrayList<String>());
        this.setUuid(uuid);
        this.setIdentifier("selection");
    }

    public SelectionSupport(String uuid, String prompt) {
        this.setPrompt(prompt);
        this.setSelections(new ArrayList<String>());
        this.setUuid(uuid);
        this.setIdentifier("selection");
    }

    public SelectionSupport(String uuid, String prompt, List<String> selections) {
        this.setUuid(uuid);
        this.setPrompt(prompt);
        this.setSelections(selections);
        this.setIdentifier("selection");
    }
    // endregion

    // region getter & setter
    public List<String> getSelections() {
        return selections;
    }

    public void setSelections(List<String> selections) {
        this.selections = selections;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    // endregion
}

package Components;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import Comprehensive.App;

/**
 * Class for table support
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class TableSupport extends Support implements SupportInterface {
    // region object variables
    private Table table;
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

        // show single table
        targetLinearLayout.addView(App.getTableLayoutByTable((Object) this, this.getTable(), context));
    }


    // endregion

    // region object methods
    public TableSupport(String uuid) {
        this.setPrompt("");
        this.setTable(null);
        this.setUuid(uuid);
        this.setIdentifier("table");
    }

    public TableSupport(String uuid, String prompt, Table table) {
        this.setPrompt(prompt);
        this.setTable(table);
        this.setUuid(uuid);
        this.setIdentifier("table");
    }

    // endregion

    // region getter & setter

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    // endregion
}

package Components;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import Comprehensive.Application;

/**
 * Class for textbox support
 *
 * @author Dominik Liehr
 * @version 0.02
 */
public class TextboxSupport extends Support implements SupportInterface {
    // region object variables
    private String textBoxContent;
    // endregion

    // region interface

    @Override
    public void displaySupport(Context context, LinearLayout targetLinearLayout) {
        TextView textView = new TextView(context);
        textView.setText(this.getTextBoxContent());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Application.supportTextTextSize);

        targetLinearLayout.addView(textView);
    }
    // endregion

    // region object methods

    public TextboxSupport(String uuid, String content) {
        this.setUuid(uuid);
        this.setTextBoxContent(content);
        this.setIdentifier("textbox");
    }
    // endregion

    // region getter & setter

    public String getTextBoxContent() {
        return textBoxContent;
    }

    public void setTextBoxContent(String textBoxContent) {
        this.textBoxContent = textBoxContent;
    }


    // endregion
}

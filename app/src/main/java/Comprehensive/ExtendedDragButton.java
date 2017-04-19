package Comprehensive;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.widget.Button;

import Components.Assessment;

public class ExtendedDragButton extends AppCompatButton {
    // region object variables
    private Object[] objects;
    // endregion

    // region constructors
    public ExtendedDragButton(Context context) {
        super(context);
    }

    public ExtendedDragButton(Context context, String buttonText) {
        super(context);
        this.setText(buttonText);
    }

    public ExtendedDragButton(Context context, String buttonText, Object[] objects) {
        super(context);
        this.setObjects(objects);
        this.setText(buttonText);
    }
    // endregion

    // region getters and setters

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }
// endregion
}

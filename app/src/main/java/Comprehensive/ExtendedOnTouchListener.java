package Comprehensive;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import Components.Assessment;

/**
 * Extension of the View.OnTouchListener.
 * Now the access to internal properties and methods is possible inclusive context property.
 */
public class ExtendedOnTouchListener extends View implements View.OnTouchListener {
    // region object variables
    private Object[] objects;
    // endregion

    // region constructors
    public ExtendedOnTouchListener(Context context) {
        super(context);
    }

    public ExtendedOnTouchListener(Context context, Object[] objects) {
        super(context);
        this.setObjects(objects);
    }
    // endregion

    // region interface implementation
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
    // endregion

    // region getter & setter

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    // endregion
}

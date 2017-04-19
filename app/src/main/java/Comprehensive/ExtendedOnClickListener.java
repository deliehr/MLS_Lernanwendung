package Comprehensive;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import Components.Assessment;

/**
 * Extension of the View.OnTouchListener.
 * Now the access to internal properties and methods is possible inclusive context property.
 */
public class ExtendedOnClickListener implements View.OnClickListener {
    // region object variables
    private Context context;
    private Object[] objects;
    // endregion

    // region constructors
    public ExtendedOnClickListener(Context context) {
        this.setContext(context);
    }

    public ExtendedOnClickListener(Context context, Object[] objects) {
        this.setContext(context);
        this.setObjects(objects);
    }
    // endregion

    // region interface implementation
    @Override
    public void onClick(View view) {
    }
    // endregion

    // region getter & setter

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    // endregion
}

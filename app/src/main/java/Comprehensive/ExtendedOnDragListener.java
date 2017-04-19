package Comprehensive;

import android.content.Context;
import android.view.DragEvent;
import android.view.View;

import Components.DragAssessment;

/**
 * Implements the OnDragListener for Drag'n'Drop purposes.
 * Saves a related (column or row) identifier as a cell in a drag'n'drop-table.
 *
 */
public class ExtendedOnDragListener implements View.OnDragListener {
    private Context context;
    private Object[] objects;

    public ExtendedOnDragListener(Context context, Object[] objects) {
        this.setContext(context);
        this.setObjects(objects);
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        return false;
    }

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

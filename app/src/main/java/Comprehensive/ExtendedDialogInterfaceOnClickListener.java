package Comprehensive;

import android.content.Context;
import android.content.DialogInterface;

/**
 * Own implementation of a DialogInterface.OnClickListener.
 * Addtionally, it stores a Context object and multiple other objects.
 */
public class ExtendedDialogInterfaceOnClickListener implements DialogInterface.OnClickListener {
    // region object variables
    private Context context;
    private Object[] objects;
    // endregion

    // region constructors
    public ExtendedDialogInterfaceOnClickListener(Context context) {
        this.setContext(context);
    }

    public ExtendedDialogInterfaceOnClickListener(Context context, Object[] objects) {
        this.setContext(context);
        this.setObjects(objects);
    }
    // endregion

    // region interface implementation
    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
    // endregion

    // region getters & setters
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }
    // endregion
}

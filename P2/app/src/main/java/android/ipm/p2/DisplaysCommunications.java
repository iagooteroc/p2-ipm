package android.ipm.p2;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public interface DisplaysCommunications {
    Context getContext();
    SparseBooleanArray getCheckedItemPositions();
    ArrayList<String> getElements();
    void setAdapter(ArrayAdapter<String> adapter);
    void setActionMode(ActionMode actionMode);
    void showEditDialog(int selected);
    void showRemoveDialog(SparseBooleanArray selected);
}

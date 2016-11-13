package android.ipm.p2;

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;


public class MultiChoiceListenerImpl implements AbsListView.MultiChoiceModeListener {
    private static int n_selected_rows = 0;     //numero de filas seleccionadas
    private static boolean is_edit_visible = true;
    private static boolean change_edit = false; // indica si hay que cambiar la visibilidad de edit
    private ArrayAdapter<String> adapter;
    private DisplaysCommunications parent;

    public MultiChoiceListenerImpl(DisplaysCommunications parent){
        this.parent = parent;
    }

    /**
     * Esta funcion se llama cada vez que una entrada se marca o se desmarca
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position,
                                          long id, boolean checked) {
        // Here you can do something when items are selected/de-selected,
        // such as update the title in the CAB

        if (checked) {
            n_selected_rows++;
            if ((n_selected_rows > 1) && (is_edit_visible)) {  // Si hay más de una fila seleccionada y edit está visible, hay que cambiarlo
                change_edit = true;
                mode.invalidate();
            }
        } else {
            n_selected_rows--;
            if ((n_selected_rows <= 1) && (!is_edit_visible)) {  // Si hay una fila o menos seleccionada y edit está oculto, hay que cambiarlo
                change_edit = true;
                mode.invalidate();
            }
        }
    }

    /**
     * Esta funcion se llama cada vez que se hace click en un boton del menu contextual
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        SparseBooleanArray selectedArray;
        switch (item.getItemId()) {
            case R.id.delete:
                selectedArray = parent.getCheckedItemPositions();
                parent.showRemoveDialog(selectedArray);
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.edit:
                selectedArray = parent.getCheckedItemPositions();
                int index = selectedArray.indexOfValue(true);
                int selected = selectedArray.keyAt(index);
                parent.showEditDialog(selected);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    /**
     * Esta funcion se llama cada vez que se crea el menu contextual
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        adapter = new ArrayAdapter<>(parent.getContext(),
                android.R.layout.simple_list_item_multiple_choice, android.R.id.text1,
                parent.getElements());
        parent.setAdapter(adapter);   // Cambiamos el adaptador a uno de selección múltiple
        n_selected_rows = 0;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_bar, menu);
        parent.setActionMode(mode);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Here you can make any necessary updates to the activity when
        // the CAB is removed. By default, selected items are deselected/unchecked.
        adapter = new ArrayAdapter<>(parent.getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1,
                parent.getElements());
        parent.setAdapter(adapter);   // Volvemos a cambiar el adaptador a uno simple
        change_edit = true;
    }

    /**
     * Esta funcion se llama cada vez que se "invalida" el menu contextual
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // Here you can perform updates to the CAB due to
        // an invalidate() request
        if (change_edit) { // Si hay que hacer cambios en el edit
            MenuItem item = menu.findItem(R.id.edit);
            if (n_selected_rows > 1) {  // Si hay más de una fila seleccionada, se oculta el edit
                item.setVisible(false);
                is_edit_visible = false;
            } else {                    // Si hay una o menos filas seleccionadas, se muestra el edit
                item.setVisible(true);
                is_edit_visible = true;
            }
            change_edit = false; // Ya se han ejecutado los cambios
        }
        return false;
    }
}

package android.ipm.p2;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AddDialogFragment.AddDialogListener,
        EditDialogFragment.EditDialogListener,
        RemoveDialogFragment.RemoveDialogListener {
    public static final String CATEGORIES = "Categories";
    public static final String MAIN_LIST = "MainList";
    private ArrayList<String> elements = new ArrayList<>();   // elementos de la lista principal
    private ArrayAdapter<String> adapter;
    private static int n_selected_rows = 0;     //numero de filas seleccionadas
    private static boolean is_edit_visible = true;
    private static boolean change_edit = false; // indica si hay que cambiar la visibilidad de edit
    private ActionMode actionMode;  // referencia al menú contextual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.mylist);

        SharedPreferences dataStorage = getPreferences(0);  // Obtener datos almacenados
        Set<String> storedCategories = dataStorage.getStringSet(CATEGORIES, null);
        if (savedInstanceState == null) {   // Si el diccionario está vacío se comprueba si hay datos almacenados
            if (storedCategories != null) {   // Si los hay, se añaden a la lista de elementos
                elements.addAll(storedCategories);
                Collections.sort(elements);
            }
        }
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                elements);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

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
                        selectedArray = listView.getCheckedItemPositions();
                        showRemoveDialog(selectedArray);
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.edit:
                        selectedArray = listView.getCheckedItemPositions();
                        int index = selectedArray.indexOfValue(true);
                        int selected = selectedArray.keyAt(index);
                        showEditDialog(selected);
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
                adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_multiple_choice, android.R.id.text1,
                        elements);
                listView.setAdapter(adapter);   // Cambiamos el adaptador a uno de selección múltiple
                n_selected_rows = 0;
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_bar, menu);
                actionMode = mode;
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                adapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1,
                        elements);
                listView.setAdapter(adapter);   // Volvemos a cambiar el adaptador a uno simple
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

        });
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        Set<String> categories = new HashSet<>();
        categories.addAll(elements);
        editor.putStringSet(CATEGORIES, categories);        // Se guardan los elementos actuales usando el editor
        // Commit the edits!
        editor.apply();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        ArrayList<String> oldElements = savedInstanceState.getStringArrayList(MAIN_LIST);   // Se obtiene la lista principal del diccionario
        if (oldElements != null) {  // Si no está vacía
            elements.addAll(oldElements);    // Se añaden los elementos
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // savedInstanceState guarda pares de valores
        // Save the user's current list data
        savedInstanceState.putStringArrayList(MAIN_LIST, elements);
        if (actionMode != null)     // Si el menú contextual está abierto, se cierra
            actionMode.finish();

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    /**
     * Función que se llama para crear la app bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Función a la que se llama cuando se hace click en un botón de la app bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                // User chose the "Add" action
                showAddDialog();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Función que añade un elemento a la lista
     */
    private void addElement(String value) {
        elements.add(value);
        Collections.sort(elements);
        adapter.notifyDataSetChanged();
    }

    /**
     * Función que muestra el diálogo de añadir
     */
    public void showAddDialog() {
        DialogFragment dialog = new AddDialogFragment();
        dialog.show(getFragmentManager(), "AddDialogFragment");
    }

    /**
     * Función que muestra el diálogo de editar
     */
    public void showEditDialog(int selected) {
        EditDialogFragment dialog = new EditDialogFragment();
        String text = adapter.getItem(selected);
        dialog.setText(text);
        dialog.setSelected(selected);
        dialog.show(getFragmentManager(), "EditDialogFragment");
    }

    /**
     * Función que muestra el diálogo de borrar
     */
    public void showRemoveDialog(SparseBooleanArray selected) {
        RemoveDialogFragment dialog = new RemoveDialogFragment();
        dialog.setSelected(selected);
        dialog.show(getFragmentManager(), "RemoveDialogFragment");
    }

    /**
     * Función que se llama en una respuesta positiva del diálogo de añadir
     * Recibe el texto introducido en el diálogo
     */
    @Override
    public void onAddPositiveClick(String value) {
        // Se comprueba si el nombre está vacío o es repetido
        if (value.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.error_empty_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if (isRepeated(value)) {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.error_repeated_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            addElement(value);
        }
    }

    /**
     * Función que se llama en una respuesta positiva del diálogo de editar
     * Recibe el texto introducido en el diálogo y la posición a editar
     */
    @Override
    public void onEditPositiveClick(String value, int selected) {
        // Se comprueba si el nombre está vacío o es repetido
        if (value.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.error_empty_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if (isRepeated(value)) {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.error_repeated_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            String oldValue = adapter.getItem(selected);
            adapter.remove(oldValue);
            elements.add(value);
            Collections.sort(elements);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Función que se llama en una respuesta positiva del diálogo de eliminar
     * Recibe las posiciones seleccionadas en forma de SparseBooleanArray
     */
    @Override
    public void onRemovePositiveClick(SparseBooleanArray selected) {
        int n;
        n = adapter.getCount();
        // Recorre todos los elementos, empezando por el final, borrando los seleccionados
        for (int i = (n - 1); i >= 0; i--) {
            if (selected.indexOfKey(i) >= 0) {  // Si el elemento está en el SparseBooleanArray
                if (selected.get(i)) {          // se comprueba si está seleccionado o no
                    adapter.remove(adapter.getItem(i));
                    adapter.notifyDataSetChanged();
                    selected.put(i, false);
                }
            }
        }
    }

    private boolean isRepeated(String value) {
        return (elements.contains(value));
    }

}

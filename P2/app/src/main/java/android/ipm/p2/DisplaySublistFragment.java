package android.ipm.p2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DisplaySublistFragment extends Fragment implements AddDialogFragment.AddDialogListener,
        EditDialogFragment.EditDialogListener, RemoveDialogFragment.RemoveDialogListener, DisplaysCommunications {
    private ArrayList<String> elements = new ArrayList<>();
    private ListView listView;
    private ActionMode actionMode;
    private ArrayAdapter<String> adapter;
    private String categoryName;
    private OnUpSelectedListener mCallback;

    public String getCategoryName() {
        return categoryName;
    }

    public interface OnUpSelectedListener {
        void onUpSelected();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnUpSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCategorySelectedListener");
        }
    }

    public ActionMode getActionMode() {
        return this.actionMode;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public SparseBooleanArray getCheckedItemPositions() {
        return listView.getCheckedItemPositions();
    }

    @Override
    public ArrayList<String> getElements() {
        return elements;
    }

    @Override
    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }

    @Override
    public void setAdapter(ArrayAdapter<String> adapter) {
        this.adapter = adapter;
        listView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_sublist, container, false);
        listView = (ListView) view.findViewById(R.id.mysublist);
        categoryName = getArguments().getString(MainActivity.SUBLIST_NAME);

        if (savedInstanceState == null) {   // Si el diccionario está vacío se comprueba si hay datos almacenados
            SharedPreferences dataStorage = getContext().getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
            Set<String> storedCategories = dataStorage.getStringSet(categoryName, null);
            if (storedCategories != null) {   // Si los hay, se añaden a la lista de elementos
                elements.addAll(storedCategories);
                Collections.sort(elements);
            }
        }
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, elements);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceListenerImpl(this));
        return view;
    }

    private void saveSharedPreferences() {
        System.out.println("Saving state..." + elements);
        SharedPreferences settings = getContext().getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        Set<String> items = new HashSet<>();
        items.addAll(elements);
        editor.putStringSet(categoryName, items);        // Se guardan los elementos actuales usando el editor
        // Commit the edits!
        editor.apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<String> oldElements = savedInstanceState.getStringArrayList(categoryName);   // Se obtiene la lista principal del diccionario
            if (oldElements != null) {  // Si no está vacía
                elements.addAll(oldElements);    // Se añaden los elementos
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // savedInstanceState guarda pares de valores
        // Save the user's current list data
        savedInstanceState.putStringArrayList(categoryName, elements);
        if (actionMode != null)     // Si el menú contextual está abierto, se cierra
            actionMode.finish();

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Función que se llama para crear la app bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar a = ((AppCompatActivity) getActivity()).getSupportActionBar();
        categoryName = getArguments().getString(MainActivity.SUBLIST_NAME);
        if (a != null) {
            a.setTitle(categoryName);
            if (MainActivity.mDualPane) {
                inflater.inflate(R.menu.app_bar_doublepane, menu);
            } else {
                inflater.inflate(R.menu.app_bar_sublist, menu);
                a.setDisplayHomeAsUpEnabled(true);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Función a la que se llama cuando se hace click en un botón de la app bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_sub:
                showAddDialog();
                return true;

            case android.R.id.home:
                mCallback.onUpSelected();
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
        saveSharedPreferences();
    }

    /**
     * Función que muestra el diálogo de añadir
     */
    public void showAddDialog() {
        AddDialogFragment dialog = new AddDialogFragment();
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "AddDialogFragment");
    }


    /**
     * Función que muestra el diálogo de editar
     */
    @Override
    public void showEditDialog(int selected) {
        EditDialogFragment dialog = new EditDialogFragment();
        dialog.setTargetFragment(this, 0);
        String text = adapter.getItem(selected);
        dialog.setText(text);
        dialog.setSelected(selected);
        dialog.show(getFragmentManager(), "EditDialogFragment");
    }

    /**
     * Función que muestra el diálogo de borrar
     */
    @Override
    public void showRemoveDialog(SparseBooleanArray selected) {
        RemoveDialogFragment dialog = new RemoveDialogFragment();
        dialog.setTargetFragment(this, 0);
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
            Context context = getContext();
            CharSequence text = getString(R.string.error_empty_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if (isRepeated(value)) {
            Context context = getContext();
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
            Context context = getContext();
            CharSequence text = getString(R.string.error_empty_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if (isRepeated(value)) {
            Context context = getContext();
            CharSequence text = getString(R.string.error_repeated_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            String oldValue = elements.get(selected);
            elements.remove(oldValue);
            elements.add(selected, value);
            Collections.sort(elements);
            adapter.notifyDataSetChanged();
            saveSharedPreferences();
        }
    }

    @Override
    public void onRemovePositiveClick(SparseBooleanArray selected) {
        int n;
        n = adapter.getCount();
        // Recorre todos los elementos, empezando por el final, borrando los seleccionados
        for (int i = (n - 1); i >= 0; i--) {
            if (selected.indexOfKey(i) >= 0) {  // Si el elemento está en el SparseBooleanArray
                if (selected.get(i)) {          // se comprueba si está seleccionado o no
                    elements.remove(adapter.getItem(i));
                    adapter.notifyDataSetChanged();
                    selected.put(i, false);
                }
            }
        }
        saveSharedPreferences();
    }

    private boolean isRepeated(String value) {
        return (elements.contains(value));
    }

}

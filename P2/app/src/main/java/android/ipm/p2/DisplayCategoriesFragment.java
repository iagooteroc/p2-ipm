package android.ipm.p2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DisplayCategoriesFragment extends Fragment
        implements AddDialogFragment.AddDialogListener,
        EditDialogFragment.EditDialogListener, RemoveDialogFragment.RemoveDialogListener,
        DisplaysCommunications {

    private ListView listView;
    private ArrayList<String> elements = new ArrayList<>();   // elementos de la lista principal
    private ArrayAdapter<String> adapter;
    private ActionMode actionMode;  // referencia al menú contextual
    private OnCategorySelectedListener mCallback;

    public interface OnCategorySelectedListener {
        void onCategorySelected(String categoryName);
        void onCategoryEdited(String categoryName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnCategorySelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCategorySelectedListener");
        }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_categories, container, false);

        listView = (ListView) view.findViewById(R.id.mylist);

        final Context context = getActivity();
        SharedPreferences dataStorage = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);  // Obtener datos almacenados
        Set<String> storedCategories = dataStorage.getStringSet(MainActivity.MAIN_LIST, null);
        //if (savedInstanceState == null) {   // Si el diccionario está vacío se comprueba si hay datos almacenados
            if (storedCategories != null) {   // Si los hay, se añaden a la lista de elementos
                elements.addAll(storedCategories);
                Collections.sort(elements);
            }
        //}
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1,
                elements);

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceListenerImpl(this));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v,
                                    int position, long id) {
                View contextMenu = getActivity().findViewById(R.id.delete);
                if (contextMenu != null && contextMenu.isShown()) {
                    DisplaySublistFragment fragment = (DisplaySublistFragment) getFragmentManager().findFragmentById(R.id.subfragment_container);
                    if (fragment != null) {
                        ActionMode a = fragment.getActionMode();
                        if (a != null)
                            a.finish();
                    }
                }
                String categoryName = elements.get(position);
                mCallback.onCategorySelected(categoryName);
            }
        });
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        saveSharedPreferences();
    }

    private void saveSharedPreferences() {
        Context context = getActivity();
        SharedPreferences settings = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        Set<String> categories = new HashSet<>();
        categories.addAll(elements);
        editor.putStringSet(MainActivity.MAIN_LIST, categories);        // Se guardan los elementos actuales usando el editor
        // Commit the edits!
        editor.apply();
    }

    /**
     * Función que se llama para crear la app bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar, menu);
        ActionBar a = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (a != null) {
            a.setDisplayHomeAsUpEnabled(false);
            a.setTitle("P2");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    /**
     * Función a la que se llama cuando se hace click en un botón de la app bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
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
            Context context = getActivity();
            SharedPreferences settings = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
            Set<String> storedList = settings.getStringSet(oldValue, null);
            SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
            editor.remove(oldValue);
            editor.putStringSet(value, storedList);
            editor.apply();
            elements.remove(oldValue);
            elements.add(selected, value);
            Collections.sort(elements);
            adapter.notifyDataSetChanged();
            saveSharedPreferences();
            mCallback.onCategoryEdited(value);
        }
    }

    private void removeElement(int i) {
        Context context = getActivity();
        SharedPreferences settings = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        String name = elements.get(i);
        editor.remove(name);
        editor.apply();
        elements.remove(elements.get(i));
        adapter.notifyDataSetChanged();
        // Enviar señal a la actividad principal por si tiene que recargar la sublista
        if (MainActivity.mDualPane) {
            DisplaySublistFragment fragment;
            try {
                fragment = (DisplaySublistFragment) getFragmentManager().findFragmentById(R.id.subfragment_container);
            } catch (java.lang.ClassCastException e) {
                return;
            }
            if (fragment != null) {
                String categoryName = fragment.getCategoryName();
                if (categoryName.equals(name)) {
                    if (!elements.isEmpty()) {
                        mCallback.onCategorySelected(elements.get(0));
                    } else {
                        mCallback.onCategorySelected(null);
                    }
                }
            }
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
                    removeElement(i);
                }
            }
        }
    }

    private boolean isRepeated(String value) {
        return (elements.contains(value));
    }

}

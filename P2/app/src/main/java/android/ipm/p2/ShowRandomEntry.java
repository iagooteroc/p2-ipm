package android.ipm.p2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class ShowRandomEntry extends Fragment {
    public static final String SHOWN = "Shown";
    public static final String CATEGORYNAME = "CategoryName";
    private String categoryName;
    private DisplaySublistFragment.OnUpSelectedListener mCallback;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            mCallback = (DisplaySublistFragment.OnUpSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCategorySelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_show_random_entry, container, false);
        String chosenElement = "";

        //Obtengo el nombre de la categoría
        categoryName = getArguments().getString(MainActivity.SUBLIST_NAME);

        //Cargo los datos del diccionario
        if (savedInstanceState == null) {   // Si el diccionario está vacío se buscan datos almacenados
            Context context = super.getActivity();
            SharedPreferences dataStorage = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
            ArrayList<String> elements = new ArrayList<>();
            if (categoryName.equals("")) {       // Si el nombre de la categoria está vacío se busca una categoría random
                Set<String> storedElements = dataStorage.getStringSet(MainActivity.MAIN_LIST, null); // Se obtienen los nombres de categorías
                ArrayList<String> categories = new ArrayList<>();
                categories.addAll(storedElements);
                Random r = new Random();    // Y se selecciona una categoría random
                int position = r.nextInt(categories.size());
                categoryName = categories.get(position);
                storedElements = dataStorage.getStringSet(categoryName, null);
                while (storedElements == null || storedElements.isEmpty()) {                // Mientras la categoría esté vacía
                    position = r.nextInt(categories.size());    // se elige otra
                    categoryName = categories.get(position);
                    storedElements = dataStorage.getStringSet(categoryName, null);
                }
                elements.addAll(storedElements);

            } else {
                Set<String> storedElements = dataStorage.getStringSet(categoryName, null);  // Se obtienen los elementos de esa categoría
                elements.addAll(storedElements);
            }
            Random r = new Random();    // Y se selecciona un elemento random de los elementos obtenidos
            int position = r.nextInt(elements.size());
            chosenElement = elements.get(position);
        }

        ActionBar a = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (a != null) {
            a.setDisplayHomeAsUpEnabled(true);
            a.setTitle(categoryName);
        }

        TextView textView = (TextView) view.findViewById(R.id.chosedElement);
        textView.setText(chosenElement);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                mCallback.onUpSelected();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        TextView textView = (TextView) getActivity().findViewById(R.id.chosedElement);
        savedInstanceState.putString(SHOWN, textView.getText().toString());
        savedInstanceState.putString(CATEGORYNAME, categoryName);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            TextView textView = (TextView) getActivity().findViewById(R.id.chosedElement);
            if (textView != null)
                textView.setText(savedInstanceState.get(SHOWN).toString());
            categoryName = savedInstanceState.get(CATEGORYNAME).toString();
            ActionBar a = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (a != null) {
                a.setDisplayHomeAsUpEnabled(true);
                a.setTitle(categoryName);
            }
        }
    }
}

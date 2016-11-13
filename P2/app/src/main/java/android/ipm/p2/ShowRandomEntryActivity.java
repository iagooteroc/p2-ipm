package android.ipm.p2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class ShowRandomEntryActivity extends AppCompatActivity {
    public static final String SHOWN = "Shown";
    public static final String CATEGORYNAME = "CategoryName";
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_random_entry);
        Intent intent = getIntent();
        String chosenElement = "";

        //Obtengo el nombre de la categoría
        categoryName = intent.getStringExtra(MainActivity.SUBLIST_NAME);

        //Cargo los datos del diccionario
        if (savedInstanceState == null) {   // Si el diccionario está vacío se buscan datos almacenados
            Context context = this;
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
                System.out.println(storedElements);
                elements.addAll(storedElements);

            } else {
                Set<String> storedElements = dataStorage.getStringSet(categoryName, null);  // Se obtienen los elementos de esa categoría
                elements.addAll(storedElements);
            }
            Random r = new Random();    // Y se selecciona un elemento random de los elementos obtenidos
            int position = r.nextInt(elements.size());
            chosenElement = elements.get(position);
        }

        //Con esto se muestra la cabecera
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(categoryName);
        }

        TextView textView = (TextView) findViewById(R.id.chosedElement);
        textView.setText(chosenElement);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        TextView textView = (TextView) findViewById(R.id.chosedElement);
        textView.setText(savedInstanceState.get(SHOWN).toString());
        categoryName = savedInstanceState.get(CATEGORYNAME).toString();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(categoryName);
        }
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        TextView textView = (TextView) findViewById(R.id.chosedElement);
        savedInstanceState.putString(SHOWN, textView.getText().toString());
        savedInstanceState.putString(CATEGORYNAME, categoryName);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}

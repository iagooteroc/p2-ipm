package android.ipm.p2;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AddDialogFragment.AddDialogListener,
        EditDialogFragment.EditDialogListener, RemoveDialogFragment.RemoveDialogListener,
        ActivityCommunications, SensorEventListener {
    public static final String CATEGORIES = "Categories";
    public static final String MAIN_LIST = "MainList"; // El diccionario usa este nombre para guardar los datos
    public static final String SUBLIST_NAME = "SublistName";
    private ListView listView;
    private ArrayList<String> elements = new ArrayList<>();   // elementos de la lista principal
    private ArrayAdapter<String> adapter;
    private ActionMode actionMode;  // referencia al menú contextual
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastUpdate = 0;
    private float last_x = 0;
    private float last_y = 0;
    private float last_z = 0;
    private static final int SHAKE_THRESHOLD = 1500;
    private boolean isFaceUp = true;
    private long timeFacedDown = 0;
    private long orientationChangedTime = 0;
    private boolean accelerometerAvaliable = false;

    @Override
    public Activity getActivity() {
        return this;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.mylist);

        Context context = getActivity();
        SharedPreferences dataStorage = context.getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);  // Obtener datos almacenados
        Set<String> storedCategories = dataStorage.getStringSet(MAIN_LIST, null);
        if (savedInstanceState == null) {   // Si el diccionario está vacío se comprueba si hay datos almacenados
            if (storedCategories != null)   // Si los hay, se añaden a la lista de elementos
                elements.addAll(storedCategories);
        }
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                elements);

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceListenerImpl(this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v,
                                    int position, long id) {
                String categoryName = elements.get(position);
                Intent intent = new Intent(MainActivity.this, DisplaySublistActivity.class);
                intent.putExtra(SUBLIST_NAME, categoryName);
                startActivity(intent);
            }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null)
            accelerometerAvaliable = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Context context = getActivity();
        SharedPreferences settings = context.getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        Set<String> categories = new HashSet<>();
        categories.addAll(elements);
        editor.putStringSet(MAIN_LIST, categories);        // Se guardan los elementos actuales usando el editor
        // Commit the edits!
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerAvaliable)
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerAvaliable)
            mSensorManager.unregisterListener(this);
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
    @Override
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
    @Override
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
            String oldValue = elements.get(selected);
            Context context = getActivity();
            SharedPreferences settings = context.getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
            Set<String> storedList = settings.getStringSet(oldValue, null);
            SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
            editor.remove(oldValue);
            editor.putStringSet(value, storedList);
            editor.commit();
            elements.remove(oldValue);
            elements.add(selected, value);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Función que se llama en una respuesta positiva del diálogo de eliminar
     * Recibe las posiciones seleccionadas en forma de SparseBooleanArray
     */
    @Override
    public void onRemovePositiveClick(SparseBooleanArray selected) {
        Context context = getActivity();
        int n;
        n = adapter.getCount();
        // Recorre todos los elementos, empezando por el final, borrando los seleccionados
        for (int i = (n - 1); i >= 0; i--) {
            if (selected.indexOfKey(i) >= 0) {  // Si el elemento está en el SparseBooleanArray
                if (selected.get(i)) {          // se comprueba si está seleccionado o no
                    SharedPreferences settings = context.getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
                    editor.remove(elements.get(i));
                    editor.commit();
                    elements.remove(elements.get(i));
                    adapter.notifyDataSetChanged();
                    selected.put(i, false);
                }
            }
        }
    }

    private boolean isRepeated(String value) {
        return (elements.contains(value));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastUpdate) > 100) {
                long diffTime = (currentTime - lastUpdate);
                lastUpdate = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                    launchRandomizer();
                }
                last_x = x;
                last_y = y;
                last_z = z;

                if (z > 9 && z < 10) {
                    if (!isFaceUp) {
                        isFaceUp = true;
                        timeFacedDown = currentTime - timeFacedDown;
                        if ((timeFacedDown > 1000) && (timeFacedDown < 2000)) {
                            Toast.makeText(this, "Faced down between 1 and 2 secs", Toast.LENGTH_SHORT).show();
                            launchRandomizer();
                        }
                    }
                } else if (z > -10 && z < -9) {
                    if (isFaceUp) {
                        isFaceUp = false;
                        timeFacedDown = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("ConfigurationChanged");
        long currentTime = System.currentTimeMillis();
        if ((currentTime - orientationChangedTime) < 1000) {
            Toast.makeText(this, "Orientation changed in less than 1 sec", Toast.LENGTH_SHORT).show();
            launchRandomizer();
        }
        orientationChangedTime = currentTime;
    }

    private void launchRandomizer() {
        Context context = this;
        SharedPreferences dataStorage = context.getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
        Map<String, Set<String>> allElements = (Map<String, Set<String>>) dataStorage.getAll();
        allElements.remove(MAIN_LIST);
        Set<String> keySet = allElements.keySet();  // Se obtienen todas las claves
        boolean isEmpty = true;
        for (String key : keySet) {
            if (!allElements.get(key).isEmpty()) {
                System.out.println(allElements.get(key));
                isEmpty = false;
                break;
            }
        }
        if (!isEmpty) {
            System.out.println("No vacío: " + allElements);
            Intent intent = new Intent(MainActivity.this, ShowRandomEntryActivity.class);
            intent.putExtra(SUBLIST_NAME, "");
            startActivity(intent);
        } else
            System.out.println("Vacío: " + allElements);
    }
}

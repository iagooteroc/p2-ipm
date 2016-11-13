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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DisplaySublistActivity extends AppCompatActivity implements AddDialogFragment.AddDialogListener,
        EditDialogFragment.EditDialogListener, RemoveDialogFragment.RemoveDialogListener, ActivityCommunications, SensorEventListener {
    private ArrayList<String> elements = new ArrayList<>();
    private ListView listView;
    private ActionMode actionMode;
    private ArrayAdapter<String> adapter;
    private String categoryName;
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
        setContentView(R.layout.activity_display_sublist);
        listView = (ListView) findViewById(R.id.mysublist);

        Intent intent = getIntent();
        categoryName = intent.getStringExtra(MainActivity.SUBLIST_NAME);
        ActionBar a = getSupportActionBar();
        if (a != null) {
            a.setDisplayHomeAsUpEnabled(true);
            a.setTitle(categoryName);
        }


        if (savedInstanceState == null) {   // Si el diccionario está vacío se comprueba si hay datos almacenados
            Context context = getActivity();
            SharedPreferences dataStorage = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
            Set<String> storedCategories = dataStorage.getStringSet(categoryName, null);
            if (storedCategories != null)   // Si los hay, se añaden a la lista de elementos
                elements.addAll(storedCategories);
        }
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                elements);

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceListenerImpl(this));
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null)
            accelerometerAvaliable = true;
    }


    @Override
    protected void onStop() {
        super.onStop();
        Context context = getActivity();
        SharedPreferences settings = context.getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        Set<String> items = new HashSet<>();
        items.addAll(elements);
        editor.putStringSet(categoryName, items);        // Se guardan los elementos actuales usando el editor
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
        ArrayList<String> oldElements = savedInstanceState.getStringArrayList(categoryName);   // Se obtiene la lista principal del diccionario
        if (oldElements != null) {  // Si no está vacía
            elements.addAll(oldElements);    // Se añaden los elementos
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
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
            elements.remove(oldValue);
            elements.add(selected, value);
            adapter.notifyDataSetChanged();
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
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if ((currentTime - orientationChangedTime) < 1000) {
                Toast.makeText(this, "Orientation changed in less than 1 sec", Toast.LENGTH_SHORT).show();
                launchRandomizer();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if ((currentTime - orientationChangedTime) < 1000) {
                Toast.makeText(this, "Orientation changed in less than 1 sec", Toast.LENGTH_SHORT).show();
                launchRandomizer();
            }
        }
        orientationChangedTime = currentTime;
    }

    private void launchRandomizer() {
        Intent intent = new Intent(this, ShowRandomEntryActivity.class);
        intent.putExtra(MainActivity.SUBLIST_NAME, categoryName);
        startActivity(intent);
    }
}

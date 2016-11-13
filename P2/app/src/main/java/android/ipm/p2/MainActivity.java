package android.ipm.p2;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DisplayCategoriesFragment.OnCategorySelectedListener,
        DisplaySublistFragment.OnUpSelectedListener, SensorEventListener {
    public static final String MAIN_LIST = "MainList"; // El diccionario usa este nombre para guardar los datos
    public static final String CATEGORIES = "Categories";
    public static final String SUBLIST_NAME = "SublistName";
    public static final String SHOWING_CATEGORY = "ShowingCategory";
    public static final String SHOWING = "Showing";
    public static boolean mDualPane = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean accelerometerAvaliable = false;
    private long lastUpdate = 0;
    private float last_x = 0;
    private float last_y = 0;
    private float last_z = 0;
    private static final int SHAKE_THRESHOLD = 1700;
    private boolean isFaceUp = true;
    private long timeFacedDown = 0;
    private long orientationChangedTime = 0;
    private OrientationEventListener mOrientationListener;
    private int oldRotation = Surface.ROTATION_0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null)
            accelerometerAvaliable = true;

        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                WindowManager wm = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                int rotation = display.getRotation();
                if (rotation != oldRotation) {
                    long currentTime = System.currentTimeMillis();
                    if ((currentTime - orientationChangedTime) < 1000) {
                        Toast.makeText(MainActivity.this, R.string.orientation_changed, Toast.LENGTH_SHORT).show();
                        launchRandomizer();
                    }
                    orientationChangedTime = currentTime;
                    oldRotation = rotation;
                }
            }
        };

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null && !mDualPane)
                return;
            mDualPane = false;
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment oldSubFragment = fragmentManager.findFragmentById(R.id.subfragment_container);
            if (oldSubFragment != null)
                fragmentTransaction.remove(oldSubFragment);
            DisplayCategoriesFragment categoriesFragment = new DisplayCategoriesFragment();
            fragmentTransaction.replace(R.id.fragment_container, categoriesFragment);
            fragmentTransaction.commit();

        } else if (findViewById(R.id.subfragment_container) != null) {
            mDualPane = true;
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment oldCategoriesFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (oldCategoriesFragment != null) {
                fragmentTransaction.remove(oldCategoriesFragment);
            }
            // Que abra la categoría que se estaba mostrando, o la primera
            String categoryName = getCategoryToShow();
            if (categoryName != null) {
                DisplaySublistFragment fragment = new DisplaySublistFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SUBLIST_NAME, categoryName);
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.subfragment_container, fragment);
            }
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onCategorySelected(String categoryName) {
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (categoryName != null) {
            DisplaySublistFragment fragment = new DisplaySublistFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SUBLIST_NAME, categoryName);
            fragment.setArguments(bundle);
            if (findViewById(R.id.fragment_container) != null) {
                fragmentTransaction.replace(R.id.fragment_container, fragment);
            } else if (findViewById(R.id.subfragment_container) != null) {
                fragmentTransaction.replace(R.id.subfragment_container, fragment);
            }
            SharedPreferences settings = getSharedPreferences(SHOWING, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
            editor.putString(SHOWING_CATEGORY, categoryName);   // Se guarda la categoría que se está mostrando, por si se cambia de orientación
            editor.apply();

        } else {
            Fragment fragment = fragmentManager.findFragmentById(R.id.subfragment_container);
            if (fragment != null)
                fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onCategoryEdited(String categoryName) {
        getSupportActionBar().setTitle(categoryName);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (categoryName != null) {
            if (findViewById(R.id.subfragment_container) != null) {
                DisplaySublistFragment fragment = new DisplaySublistFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SUBLIST_NAME, categoryName);
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.subfragment_container, fragment);
                SharedPreferences settings = getSharedPreferences(SHOWING, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
                editor.putString(SHOWING_CATEGORY, categoryName);   // Se guarda la categoría que se está mostrando, por si se cambia de orientación
                editor.apply();
            }
        }
    }

    @Override
    public void onUpSelected() {
        if (findViewById(R.id.fragment_container) != null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            DisplayCategoriesFragment fragment = new DisplayCategoriesFragment();
            Fragment oldSubFragment = fragmentManager.findFragmentById(R.id.subfragment_container);
            if (oldSubFragment != null)
                fragmentTransaction.remove(oldSubFragment);
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }
        SharedPreferences settings = getSharedPreferences(MainActivity.CATEGORIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();  // Se obtiene un editor de los datos almacenados
        editor.remove(SHOWING_CATEGORY);    // Ya no se está mostrando ninguna categoría
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerAvaliable)
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (mOrientationListener != null) {
            if (mOrientationListener.canDetectOrientation())
                mOrientationListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerAvaliable)
            mSensorManager.unregisterListener(this);
        if (mOrientationListener != null) {
            if (mOrientationListener.canDetectOrientation())
                mOrientationListener.disable();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        oldRotation = savedInstanceState.getInt("ROTATION");
        orientationChangedTime = savedInstanceState.getLong("ORIENTATION_TIME");
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("ROTATION", oldRotation);
        savedInstanceState.putLong("ORIENTATION_TIME", orientationChangedTime);
    }

    private String getCategoryToShow() {
        SharedPreferences dataStorage = getSharedPreferences(SHOWING, Context.MODE_PRIVATE);
        String showingCategory = dataStorage.getString(SHOWING_CATEGORY, null);
        if (showingCategory != null)
            return showingCategory;
        // Si no se estaba mostrando ninguna categoría, se obtiene la primera
        dataStorage = getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
        Set<String> storedCategories = dataStorage.getStringSet(MAIN_LIST, null);
        if (storedCategories == null)
            return null;
        ArrayList<String> categories = new ArrayList<>();
        categories.addAll(storedCategories);
        Collections.sort(categories);
        if (!categories.isEmpty())
            return categories.get(0);
        return null;    // Si no hay categorías, se devuelve null
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
                    Toast.makeText(this, R.string.shake_detected, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, R.string.faced_down, Toast.LENGTH_SHORT).show();
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

    private void launchRandomizer() {
        String categoryName = getSupportActionBar().getTitle().toString();
        if (categoryName.equals("P2")) {
            SharedPreferences dataStorage = getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
            Map<String, Set<String>> allElements = (Map<String, Set<String>>) dataStorage.getAll();
            allElements.remove(MAIN_LIST);
            Set<String> keySet = allElements.keySet();  // Se obtienen todas las claves
            boolean isEmpty = true;
            for (String key : keySet) {
                if (!allElements.get(key).isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ShowRandomEntry fragment = new ShowRandomEntry();
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.SUBLIST_NAME, "");
                fragment.setArguments(bundle);
                if (findViewById(R.id.fragment_container) != null) {
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                } else if (findViewById(R.id.subfragment_container) != null)
                    fragmentTransaction.replace(R.id.subfragment_container, fragment);
                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } else
                Toast.makeText(this, R.string.no_elements_found, Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferences dataStorage = getSharedPreferences(CATEGORIES, Context.MODE_PRIVATE);
            Set<String> sublist = dataStorage.getStringSet(categoryName, null);
            if (sublist != null && !sublist.isEmpty()) {
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ShowRandomEntry fragment = new ShowRandomEntry();
                Bundle bundle = new Bundle();
                bundle.putString(SUBLIST_NAME, categoryName);
                fragment.setArguments(bundle);
                if (findViewById(R.id.fragment_container) != null) {
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                } else if (findViewById(R.id.subfragment_container) != null)
                    fragmentTransaction.replace(R.id.subfragment_container, fragment);
                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } else
                Toast.makeText(this, R.string.no_elements_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}

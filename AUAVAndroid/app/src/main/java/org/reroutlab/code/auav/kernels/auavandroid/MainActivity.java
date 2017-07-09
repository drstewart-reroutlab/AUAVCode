package org.reroutlab.code.auav.kernels.auavandroid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.util.Log;
import android.support.v4.app.ActivityCompat;

import org.reroutlab.code.auav.drivers.AuavDrivers;

import java.util.HashMap;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AUAVAndroid";
    Level AUAVLEVEL = Level.ALL;


    HashMap n2p = new HashMap<String, String>();
    AuavDrivers[] ad = new AuavDrivers[128];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,Manifest.permission.VIBRATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.READ_PHONE_STATE,
                }
                , 1);

        DJISDKManager.getInstance().registerApp(this, mDJISDKManagerCallback);
        DJISDKManager.getInstance();
        DJISDKManager.getInstance().getProduct();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public <T> T instantiate(final String className, final Class<T> type) {
        try {
            Log.v(TAG, "Loading driver: " + className);
            return type.cast(Class.forName(className).newInstance());
        } catch (InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e) {
            Log.e(TAG, "Error:" + e.toString() + "\nStack" + e.getStackTrace().toString());
            throw new IllegalStateException(e);
        }
    }

    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {


        @Override
        public void onRegister(DJIError error) {
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                DJISDKManager.getInstance().startConnectionToProduct();
            } else {
            }
            Log.v(TAG, "Registration description was " +error.getDescription());


        }

        @Override
        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {

            Log.d(TAG, String.format("onProductChanged oldProduct:%s, newProduct:%s", oldProduct, newProduct));
            if (newProduct != null) {
                Thread t = new Thread() {
                    public void run() {
                        String jarList = "";
                        try {
                            InputStream is = getAssets().open("jarList");
                            BufferedReader br = new BufferedReader(new InputStreamReader(is));
                            jarList = br.readLine();
                            br.close();
                        } catch (Exception e) {
                            Log.v(TAG, e.toString());
                        }

                        String[] fullPath = jarList.split(".jar:");
                        String[] jarNames = new String[fullPath.length];
                        int countDrivers = 0;
                        for (int x = 0; x < fullPath.length; x++) {
                            String[] seps = fullPath[x].split("/");
                            if (seps[seps.length - 1].endsWith("Driver") == true) {
                                jarNames[countDrivers] = seps[seps.length - 1];
                                countDrivers++;
                            }
                        }

                        for (int x = 0; x < countDrivers; x++) {
                            System.out.println("Jar: " + jarNames[x]);
                            ad[x] = instantiate(jarNames[x], org.reroutlab.code.auav.drivers.AuavDrivers.class);
                            n2p.put(ad[x].getClass().getCanonicalName(),
                                    new String("Port:" + ad[x].getLocalPort() + "\n"));
                        }

                        // Printing the map object locally for logging
                        String mapAsString = "Active Drivers\n";
                        Set keys = n2p.keySet();
                        for (Iterator i = keys.iterator(); i.hasNext(); ) {
                            String name = (String) i.next();
                            String value = (String) n2p.get(name);
                            mapAsString = mapAsString + name + " --> " + value + "\n";
                        }
                        Log.v(TAG, mapAsString);

                        for (int x = 0; x < countDrivers; x++) {
                            // Send the map back to each object
                            ad[x].setDriverMap(n2p);
                            ad[x].setLogLevel(AUAVLEVEL);
                            ad[x].getCoapServer().start();
                        }
                    }
                };
                t.start();


            }
            else {
                try {
                    Thread.sleep(10000);
                }
                catch(Exception e) {

                }
                Log.w("onProduct note","Connect returns" +DJISDKManager.getInstance().startConnectionToProduct());
            }

        }
    };

}
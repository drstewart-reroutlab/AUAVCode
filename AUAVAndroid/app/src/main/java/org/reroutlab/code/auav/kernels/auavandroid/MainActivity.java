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
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET}, 0);

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

    public <T> T instantiate(final String className, final Class<T> type){
        try{
            Log.v(TAG,"Loading driver: " + className);
            return type.cast(Class.forName(className).newInstance());
        } catch(InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e){
            Log.e(TAG,"Error:" + e.toString() + "\nStack"+ e.getStackTrace().toString());
            throw new IllegalStateException(e);
        }
    }

}

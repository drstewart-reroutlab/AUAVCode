package org.reroutlab.code.auav.kernels.kernelforandroid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import org.reroutlab.code.auav.drivers.AuavDrivers;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlyKernel extends AppCompatActivity {

    private Level AUAVLEVEL = Level.FINE; // set AuavLEVEL
    private static Logger theLogger =
            Logger.getLogger(FlyKernel.class.getName());//get Logger object by calling getLogger receive the name of the LinuxKernal.class'name


    HashMap n2p = new HashMap<String, String>();
    AuavDrivers[] ad = new AuavDrivers[128];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_kernel);
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


        String jarList = System.getProperty("java.class.path");
        String[] fullPath = jarList.split(".jar:");
        String[] jarNames = new String[fullPath.length];
        int countDrivers = 0;
        for (int x =0; x < fullPath.length;x++){
            String[] seps = fullPath[x].split("/");
            if (seps[seps.length - 1].endsWith("Driver") == true) {
                jarNames[countDrivers] = seps[seps.length - 1];
                countDrivers++;
            }
        }
        theLogger.setLevel(AUAVLEVEL); //set logger's level

        for (int x = 0; x < countDrivers; x++) {
            System.out.println("Jar: "+jarNames[x]);
            ad[x] = instantiate(jarNames[x],org.reroutlab.code.auav.drivers.AuavDrivers.class);
            n2p.put(ad[x].getClass().getCanonicalName(),
                    new String("Port:"+ad[x].getLocalPort()+"\n" ) );
        }

        // Printing the map object locally for logging
        String mapAsString = "Active Drivers\n";
        Set keys = n2p.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            String value = (String) n2p.get(name);
            mapAsString = mapAsString + name + " --> " + value + "\n";
        }
        theLogger.log(Level.INFO,mapAsString);

        for (int x = 0; x < countDrivers; x++) {
            // Send the map back to each object
            ad[x].setDriverMap(n2p);
            ad[x].setLogLevel(AUAVLEVEL);
            ad[x].getCoapServer().start();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fly_kernel, menu);
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

    // Code taken from stackoverflow in May 2017
    // Thanks Sean Patrick Floyd
    // Documentation by Christopher Stewart
    public <T> T instantiate(final String className, final Class<T> type){
        try{
            return type.cast(Class.forName(className).newInstance());
        } catch(InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e){
            throw new IllegalStateException(e);
        }
    }

}

package org.segin.ttleditor;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;
import java.net.SocketException;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;


public class TTLEditor extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttleditor);

        enumerateNetworkInterfaces();
    }

    protected void enumerateNetworkInterfaces() {
        Spinner spinner = (Spinner) findViewById(R.id.ifList);
        List<String> ifnames = new ArrayList<String>();
        try {
            for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                ifaces.hasMoreElements();)
            {
                NetworkInterface i = ifaces.nextElement();
                ifnames.add(i.getDisplayName());
                Log.e("network_interfaces", "display name " + i.getDisplayName());
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, ifnames);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);
        } catch (SocketException e) {
            ifnames.add("none");
            Log.wtf("network_interfaces", "SocketException occurred getting names!", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ttleditor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

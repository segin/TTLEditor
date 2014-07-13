package org.segin.ttleditor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.net.SocketException;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;


public class TTLEditor extends Activity {

    private Spinner spinner;
    private Button btnSubmit;
    private TextView debugText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttleditor);

        enumerateNetworkInterfaces();
        makeButtonDoStuff();
    }

    private void doToast(String msg) {
        Toast.makeText(TTLEditor.this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void makeButtonDoStuff() {
        btnSubmit = (Button) findViewById(R.id.button);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText newttl = (EditText) findViewById(R.id.ttlValue);
                int ttl = Integer.parseInt(newttl.getText().toString());
                String msg;
                if (ttl > 255) {
                    msg = "TTL too high: " + newttl.getText().toString();
                } else if (ttl < 1) {
                    msg = "TTL too low: " + newttl.getText().toString();
                } else {
                    msg = "button pressed";
                }
                doToast(msg);
            }
        });
    }

    protected void enumerateNetworkInterfaces() {
        if (spinner == null) spinner = (Spinner) findViewById(R.id.ifList);
        if (debugText == null) debugText = (TextView) findViewById(R.id.debugText);
        List<String> ifnames = new ArrayList<String>();
        String dbg = "Debug: \n";
        debugText.setText(dbg);
        try {
            for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                ifaces.hasMoreElements();)
            {
                NetworkInterface i = ifaces.nextElement();
                ifnames.add(i.getDisplayName().toString());
                Log.i("network_interfaces", "Interface found: " + i.getDisplayName());
                dbg += "Interface found: " + i.getDisplayName() + "\n";
            }
        } catch (SocketException e) {
            ifnames.add("none");
            dbg += "Couldn't find interfaces! (Permissions issue?)\n";
            debugText.setText(dbg, TextView.BufferType.NORMAL);
            Log.e("network_interfaces", "SocketException occurred getting names!", e);
        } catch (NullPointerException e) {
            Log.e("TTLEditor", "NullPointerException occurred?", e);
            doToast("wtf? " + e.toString());
            dbg += "NullPointerException occurred!";
            debugText.setText(dbg, TextView.BufferType.NORMAL);
        }
        dbg += "Found " + String.valueOf(ifnames.size()) + "network interfaces.\n";
        debugText.setText(dbg);
        if (ifnames.size() == 0) ifnames.add("none");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ifnames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
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

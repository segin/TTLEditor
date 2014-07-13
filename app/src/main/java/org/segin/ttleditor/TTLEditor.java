package org.segin.ttleditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;


public class TTLEditor extends Activity {

    private Spinner spinner;
    private Button btnSubmit;
    private TextView debugText;
    private TextView ipText;
    private Enumeration<NetworkInterface> ifaces;
    private Resources res = getResources();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttleditor);

        enumerateNetworkInterfaces();
        makeButtonDoStuff();
        makeSpinnerDoStuff();
    }

    private void doToast(String msg) {
        Toast.makeText(TTLEditor.this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void makeButtonDoStuff() {
        if (btnSubmit == null) btnSubmit = (Button) findViewById(R.id.button);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                buildDialog();
            }
        });
    }

    protected void changeTTL() {
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

    protected void buildDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TTLEditor.this);
        alertDialogBuilder.setTitle(getString(R.string.dialog_name));
        alertDialogBuilder
                .setMessage(getString(R.string.dialog_text))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.dialog_yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        changeTTL();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    protected void updateIPAddress() {
        if (spinner == null) spinner = (Spinner) findViewById(R.id.ifList);
        if (ipText == null) ipText = (TextView) findViewById(R.id.ipText);
        NetworkInterface iface;
        List<InetAddress> addresses;
        try {
            iface = NetworkInterface.getByName(
                    spinner.getSelectedItem().toString());
            String msg = getString(R.string.ip_help);
            addresses = Collections.list(iface.getInetAddresses());
            if (addresses.size() < 1)
                ipText.setText(msg + getString(R.string.no_address));
            else {
                for (InetAddress address : addresses) {
                    msg += address.getHostAddress() + "\n";
                }
                ipText.setText(msg);
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ipText.setText(getString(R.string.ip_help) + getString(R.string.no_iface_access));
        }
    }

    protected void makeSpinnerDoStuff() {
        if (spinner == null) spinner = (Spinner) findViewById(R.id.ifList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateIPAddress();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    protected void enumerateNetworkInterfaces() {
        if (spinner == null) spinner = (Spinner) findViewById(R.id.ifList);
        if (debugText == null) debugText = (TextView) findViewById(R.id.debugText);
        List<String> ifnames = new ArrayList<String>();
        String dbg = res.getString(R.string.debug_help);
        debugText.setText(dbg);
        debugText.setMovementMethod(new ScrollingMovementMethod());
        try {
            for(ifaces = NetworkInterface.getNetworkInterfaces();
                ifaces.hasMoreElements();)
            {
                NetworkInterface i = ifaces.nextElement();
                ifnames.add(i.getDisplayName());
                Log.i("network_interfaces", "Interface found: " + i.getDisplayName());
                dbg += String.format(res.getString(R.string.if_found), i.getDisplayName());
            }
        } catch (SocketException e) {
            ifnames.add(getString(R.string.none));
            dbg += "Couldn't find interfaces! (Permissions issue?)\n";
            debugText.setText(dbg, TextView.BufferType.NORMAL);
            Log.e("network_interfaces", "SocketException occurred getting names!", e);
        } catch (NullPointerException e) {
            Log.e("TTLEditor", "NullPointerException occurred?", e);
            doToast("wtf? " + e.toString());
            dbg += "NullPointerException occurred!";
            debugText.setText(dbg, TextView.BufferType.NORMAL);
        }
        dbg += res.getQuantityString(R.plurals.iface_count, ifnames.size(), ifnames.size());
        debugText.setText(dbg);
        if (ifnames.size() == 0) ifnames.add(getString(R.string.none));
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

package org.segin.ttleditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.stericson.RootTools.*;

import java.io.IOException;
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
    private Resources res;
    private String dbgmsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttleditor);

        if (RootTools.isRootAvailable()) {
            if (RootTools.isBusyboxAvailable()) {
                Log.i("TTLEditor","Root and BusyBox are available.");
            } else {
                buildOfferBusyBoxDialog();
            }
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TTLEditor.this);
            alertDialogBuilder.setTitle(getString(R.string.rootalert_name));
            alertDialogBuilder
                    .setMessage(getString(R.string.rootalert_text))
                    .setCancelable(true)
                    .setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                TTLEditor.this.finalize();
                            } catch (Throwable e) {
                                Log.wtf("TTLEditor", "wtf, cannot Activity.finalize()?", e);
                            }
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        try {
            res = getResources();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        debugInit();
        enumerateNetworkInterfaces();
        makeButtonDoStuff();
        makeSpinnerDoStuff();

    }

    private void doToast(String msg) {
        Toast.makeText(TTLEditor.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void makeButtonDoStuff() {
        if (btnSubmit == null) btnSubmit = (Button) findViewById(R.id.button);
        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                validateTTL();
            }
        });
    }

    private void validateTTL() {
        EditText newttl = (EditText) findViewById(R.id.ttlValue);
        int ttl = Integer.parseInt(newttl.getText().toString());
        String msg = null;
        if (ttl > 255)
            msg = String.format(res.getString(R.string.ttl_high), newttl.getText().toString());
        else if (ttl < 1)
            msg = String.format(res.getString(R.string.ttl_low), newttl.getText().toString());

        if (msg == null)
            buildDialog();
        else
            doToast(msg);
    }

    private void changeTTL() {
        EditText newttl = (EditText) findViewById(R.id.ttlValue);
        String ttl = newttl.getText().toString();
        if (RootTools.isAccessGiven()) {
            String cmdLine = String.format(getString(R.string.iptables_cmdline),
                    spinner.getSelectedItem().toString(), newttl.getText().toString());
            Process exec = null;
            try {
                exec = Runtime.getRuntime().exec(new String[]{"su", "-c", cmdLine});
                exec.waitFor();
                if (exec.exitValue() != 0)
                    doToast(getString(R.string.ttl_failure));
                else
                    doToast(getString(R.string.ttl_success));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TLLEditor", "iptables failed.", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("TLLEditor", "iptables received signal before completing.", e);
            } finally {
                if (exec != null) exec.destroy();
            }
        } else
            doToast(getString(R.string.no_root_access));

    }

    private void buildDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TTLEditor.this);
        alertDialogBuilder.setTitle(getString(R.string.dialog_name));
        alertDialogBuilder
                .setMessage(getString(R.string.dialog_text))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        changeTTL();
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void buildAboutDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TTLEditor.this);
        alertDialogBuilder.setTitle(getString(R.string.about_name));
        alertDialogBuilder
                .setMessage(getString(R.string.about_text))
                .setCancelable(true)
                .setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void buildOfferBusyBoxDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TTLEditor.this);
        alertDialogBuilder.setTitle(getString(R.string.offer_name));
        alertDialogBuilder
                .setMessage(getString(R.string.offer_name))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RootTools.offerBusyBox(TTLEditor.this);
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void updateIPAddress() {
        if (spinner == null) spinner = (Spinner) findViewById(R.id.ifList);
        if (ipText == null) ipText = (TextView) findViewById(R.id.ipText);
        NetworkInterface iface;
        List<InetAddress> addresses;
        try {
            iface = NetworkInterface.getByName(
                    spinner.getSelectedItem().toString());
            addresses = Collections.list(iface.getInetAddresses());
            String msg = res.getQuantityString(R.plurals.ip_count,
                    addresses.size(), addresses.size());
            if (addresses.size() >= 1) {
                for (InetAddress address : addresses) {
                    msg += address.getHostAddress() + "\n";
                }
            }
            ipText.setText(msg);
        } catch (SocketException e) {
            e.printStackTrace();
            ipText.setText(getString(R.string.ip_help) + getString(R.string.no_iface_access));
        }
    }

    private void makeSpinnerDoStuff() {
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

    protected void debugInit() {
        if (dbgmsg == null)
            dbgmsg = res.getString(R.string.debug_help);
        if (debugText == null) debugText = (TextView) findViewById(R.id.debugText);
        debugText.setText(dbgmsg);
        debugText.setMovementMethod(new ScrollingMovementMethod());
    }

    private void debug(String msg) {



    }


    private void enumerateNetworkInterfaces() {
        if (spinner == null) spinner = (Spinner) findViewById(R.id.ifList);
        List<String> ifnames = new ArrayList<String>();
        try {
            for(ifaces = NetworkInterface.getNetworkInterfaces();
                ifaces.hasMoreElements();)
            {
                NetworkInterface i = ifaces.nextElement();
                ifnames.add(i.getDisplayName());
                Log.i("network_interfaces", "Interface found: " + i.getDisplayName());
                debug(String.format(res.getString(R.string.if_found), i.getDisplayName()));
            }
        } catch (SocketException e) {
            ifnames.add(getString(R.string.none));
            debug("Couldn't find interfaces! (Permissions issue?)\n");
            Log.e("network_interfaces", "SocketException occurred getting names!", e);
        } catch (NullPointerException e) {
            Log.e("TTLEditor", "NullPointerException occurred?", e);
            doToast("wtf? " + e.toString());
            debug("NullPointerException occurred!");
        }
        debug(res.getQuantityString(R.plurals.iface_count, ifnames.size(), ifnames.size()));
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
            Intent i = new Intent(TTLEditor.this, SettingsActivity.class);
            TTLEditor.this.startActivity(i);
            return true;
        }
        if (id == R.id.action_about) {
            buildAboutDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}

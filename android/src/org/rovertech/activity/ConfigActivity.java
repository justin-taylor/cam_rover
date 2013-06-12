package org.rovertech.activity;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;

import org.rovertech.R;

public class ConfigActivity
extends Activity
implements OnClickListener
{

    private static final String SETTINGS_PREFERENCES = "org.rovertech.activity.ConfigActivity.SETTINGS_PREFERENCES";
    private static final String IP_ADDRESS_SETTING = "org.rovertech.activity.ConfigActivity.IP_ADDRESS_SETTING";
    private static final String PORT_SETTING = "org.rovertech.activity.ConfigActivity.PORT_SETTING";

    private static final String DEFAULT_IP_ADDRESS = "192.168.42.1";
    private static final int DEFAULT_PORT = 9300;

    EditText _ipAddressField;
    EditText _portField;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        _ipAddressField = (EditText) findViewById(R.id.ip_address_field);
        _portField = (EditText) findViewById(R.id.port_field);

        Button button = (Button) findViewById(R.id.connect_button);
        button.setOnClickListener(this);

        _loadSettingsIntoUi();
    }


    @Override
    public void onPause()
    {
        super.onPause();

        _saveCurrentSettings();
    }



    public void onClick(View view)
    {
        int id = view.getId();
        switch(id)
        {
            case R.id.connect_button: _connectButtonAction(); break;
        }
    }


    /**
     * Called when the user clicks the connect button; Begins the connection to
     * the server using the values in the ip and port fields
     */
    private void _connectButtonAction()
    {
        String ip = _ipAddressField.getText().toString();
        int port = Integer.parseInt(_portField.getText().toString());

        Intent intent = new Intent(this, ControllerActivity.class);
        intent.putExtra(ControllerActivity.IP_ADDRESS_EXTRA, ip);
        intent.putExtra(ControllerActivity.PORT_EXTRA, port);
        startActivity(intent);
    }



    /**-------------------------------------------------------------------------
     *
     * Load/Save settings methods
     *
     *------------------------------------------------------------------------*/


    /**
     * Loads the last used settings from SharedPreferences and puts the
     * information into the form
     */
    private void _loadSettingsIntoUi()
    {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFERENCES, 0);

        String ip = prefs.getString(IP_ADDRESS_SETTING, DEFAULT_IP_ADDRESS);
        int port = prefs.getInt(PORT_SETTING, DEFAULT_PORT);

        _ipAddressField.setText(ip);
        _portField.setText(""+port);
    }


    /**
     * Saves the values from the form into SharedPreferences
     */
    private void _saveCurrentSettings()
    {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFERENCES, 0);
        SharedPreferences.Editor editor = prefs.edit();

        String ip = _ipAddressField.getText().toString();
        int port = Integer.parseInt(_portField.getText().toString());

        editor.putString(IP_ADDRESS_SETTING, ip);
        editor.putInt(PORT_SETTING, port);

        editor.commit();
    }
}

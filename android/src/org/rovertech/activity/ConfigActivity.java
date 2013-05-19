package org.rovertech.activity;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.rovertech.R;

public class ConfigActivity
extends Activity
implements OnClickListener
{

    EditText ipAddressField;
    EditText portField;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        ipAddressField = (EditText) findViewById(R.id.ip_address_field);
        portField = (EditText) findViewById(R.id.port_field);

        Button button = (Button) findViewById(R.id.connect_button);

        button.setOnClickListener(this);
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
        Intent intent = new Intent(this, ControllerActivity.class);
        startActivity(intent);
    }

}

package org.rovertech.activity;

import android.app.Activity;
import android.os.Bundle;

import org.rovertech.R;

/**
 * This activity handles user interactions and interprets them to messages sent
 * to the CamRover
 */
public class ControllerActivity
extends Activity
{

    public static final String IP_ADDRESS_EXTRA = "org.rovertech.activity.ControllerActivity.IP_ADDRESS_EXTRA";
    public static final String PORT_EXTRA = "org.rovertech.activity.ControllerActivity.PORT_EXTRA";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Bundle extras = getIntent().getExtras();
        if(extras == null)
        {
            throw new Exception("Activity expects an ip address and port");
        }

        String ip_address = extras.getString(IP_ADDRESS_EXTRA);
        int port = extras.getInt(PORT_EXTRA);
    }


    private void _initializeCamRover(String ipAddress, int port)
    {
        //TODO
    }

}

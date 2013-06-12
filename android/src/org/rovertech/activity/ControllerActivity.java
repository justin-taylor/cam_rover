package org.rovertech.activity;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickClickedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

import org.rovertech.R;
import org.rovertech.network.CamRover;

/**
 * This activity handles user interactions and interprets them to messages sent
 * to the CamRover
 */
public class ControllerActivity
extends Activity implements JoystickMovedListener, JoystickClickedListener
{

    public static final String IP_ADDRESS_EXTRA = "org.rovertech.activity.ControllerActivity.IP_ADDRESS_EXTRA";
    public static final String PORT_EXTRA = "org.rovertech.activity.ControllerActivity.PORT_EXTRA";


    private JoystickView _roverJoystick, _cameraJoystick;


    private CamRover _rover;
    private String _ipAddress;
    private int _port;

    private RoverLoop _roverLoop;

    private int _steerX, _steerY;
    private int _cameraX, _cameraY;
    private boolean _runCommunication = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        _roverJoystick = (JoystickView) findViewById(R.id.steering_joystick);
        _roverJoystick.setOnJoystickMovedListener(this);
        _roverJoystick.setOnJoystickClickedListener(this);
        
        _cameraJoystick = (JoystickView) findViewById(R.id.camera_joystick);
        _cameraJoystick.setOnJoystickMovedListener(this);
        _cameraJoystick.setOnJoystickClickedListener(this);

        Bundle extras = getIntent().getExtras();
        _ipAddress = extras.getString(IP_ADDRESS_EXTRA);
        _port = extras.getInt(PORT_EXTRA);

    }


    public void onResume()
    {
        super.onResume();
        _initializeCamRover(_ipAddress,_port);
        _runRoverLoop();
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _stopRoverLoop();
    }


    private void _initializeCamRover(String ipAddress, int port)
    {
        // only create a new instance if one has not already been created
        if(_rover == null)
        {
            _rover = new CamRover(ipAddress, port);
            if(_rover.startCommunication())
            {
                Log.e("Controller", "Did start comm");
            }
        }
    }


    private void _runRoverLoop()
    {
        if(_roverLoop == null)
        {
            _runCommunication = true;
            _roverLoop = new RoverLoop();
            Thread thread = new Thread(_roverLoop);
            thread.start();
        }
    }
    

    private void _stopRoverLoop()
    {
        _runCommunication = false;
    }



    /**-------------------------------------------------------------------------
     *
     * Joystick methods
     *
     *------------------------------------------------------------------------*/


    public void joystickMoved(JoystickView view, int pan, int tilt)
    {
        if(view == _cameraJoystick)
        {
            _cameraX = pan;
            _cameraY = tilt;
        }

        else if(view == _roverJoystick)
        {
            _steerX = pan;
            _steerY = tilt;
        }
    }


    public void joystickReleased(JoystickView view)
    {
        Log.e("Controller", "OnReleased");
    }

    public void joystickReturnedToCenter(JoystickView view)
    {
        Log.e("Controller", "OnReturnedToCenter");
    }



    public void joystickClicked(JoystickView view)
    {
        Log.e("Controller", "OnClicked");
    }







    /**-------------------------------------------------------------------------
     *
     * CamRover Looper
     *
     *------------------------------------------------------------------------*/


    /**
     * Continuously runs sending messages to the camrover
     */
    private class RoverLoop implements Runnable
    {
        public void run()
        {
            while(_runCommunication)
            {
                _sendSteeringValues();
                _sendCameraControls();

                try
                {
                    Thread.sleep(1000);
                }catch(Exception e){};
            }
        }


        private void _sendSteeringValues()
        {
            _rover.sendSpeed(_steerY, _steerX);
        }


        private void _sendCameraControls()
        {
            //TODO
        }
    }
 


}

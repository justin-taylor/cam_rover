package org.rovertech.activity;

import java.io.IOException;
import java.net.URI;

import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickClickedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

import org.rovertech.R;
import org.rovertech.network.CamRover;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.rovertech.mjpgstreamer.MjpegInputStream;
import org.rovertech.mjpgstreamer.MjpegView;


/**
 * This activity handles user interactions and interprets them to messages sent
 * to the CamRover
 */
public class ControllerActivity
extends Activity implements JoystickMovedListener, JoystickClickedListener
{

    public static final String IP_ADDRESS_EXTRA = "org.rovertech.activity.ControllerActivity.IP_ADDRESS_EXTRA";
    public static final String PORT_EXTRA = "org.rovertech.activity.ControllerActivity.PORT_EXTRA";
    public static final String VIDEO_PORT_EXTRA = "org.rovertech.activity.ControllerActivity.VIDEO_PORT_EXTRA";


    private JoystickView _roverJoystick, _cameraJoystick;


    private CamRover _rover;
    private String _ipAddress;
    private int _port;
    private int _videoPort;

    private RoverLoop _roverLoop;

    private int _steerX, _steerY;
    private int _cameraX, _cameraY;
    private boolean _runCommunication = false;
    private MjpegView mv;
    private DoRead _streamReader;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
        _videoPort = extras.getInt(VIDEO_PORT_EXTRA);

        mv = (MjpegView) findViewById(R.id.video_stream);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        _initializeCamRover(_ipAddress,_port);
        _runRoverLoop();
        _runStreamer();
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _stopRoverLoop();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mv.stopPlayback();
        _streamReader.cancel(true);
    }


    private void _runStreamer()
    {
        String URL = "http://"+_ipAddress+":"+_videoPort+"/?action=stream"; 
        _streamReader = new DoRead();
        _streamReader.execute(URL); 
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
                    Thread.sleep(100);
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
 

    private String cameraHtml()
    {
        return 
            "<img src='http://192.168.42.1:8080/?action=stream' height='300px' widt='200px' />"
        ;
    }



    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            String TAG = "CamStreamer";
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();     
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(true);
        }
    }

}

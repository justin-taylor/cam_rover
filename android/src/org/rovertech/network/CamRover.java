package org.rovertech.network;


import android.util.Log;


/**
 * This class implements the CamRover protocol and send messages to the
 * physical device through an Engine
 */
public class CamRover
{
    /**
     * Constants for communicating with and controlling the cam rover
     */
    private static final byte SPEED_COMMAND = 0x01;
    private static final byte STEERING_COMMAND = 0x02;

    private static final byte MAXIMUM_SPEED = 25;
    private static final byte NEUTRAL_SPEED = 12;

    private static final byte STEER_STRAIGHT_VALUE = 10;

    private Engine _communicationEngine;

    private String _ipAddress;
    private int _port;

    public CamRover(String ipAddress, int port)
    {
        _ipAddress = ipAddress;
        _port = port;
    }

    public boolean startCommunication()
    {
        //setup an engine instance for sending packets
        _communicationEngine = new Engine(_ipAddress, _port);
        return _communicationEngine.openSocket();
    }




    /**
     * Sends the Speed command to the physical cam rover. 
     *
     * @param speed the speed the motors should be set to
     * @param steer the value used to steer the physical rover
     */
    public void sendSpeed(int speed, int steer)
    {
        Log.e("SEND SPEED", "Speed: "+speed + " Steer: "+steer);
        int size = 5;
        byte[] msg = new byte[size];
        msg[0] = SPEED_COMMAND;

        int dirA, dirB;
        if(speed == 0)
        {
            dirA = 0x00;
            dirB = 0x00;
        }

        //forward
        else if(speed > 0)
        {
            dirA = 0x01;
            dirB = 0x00;
        }

        else
        {
            dirA = 0x00;
            dirB = 0x01;
        }

        // Limit the bounds of speed
        if(speed > MAXIMUM_SPEED)
        {
            speed = MAXIMUM_SPEED;
        }
        else if(speed < -MAXIMUM_SPEED)
        {
            speed = -MAXIMUM_SPEED;
        }

        // ensure speed is greater than 0
        speed *= speed > 0 ? 1 : -1;
        int val = NEUTRAL_SPEED + speed;

        msg[1] = (byte) val;
        msg[2] = (byte) dirA;
        msg[3] = (byte) dirB;

        val = steer;
        //TODO modify val to work with the proper pwm values
        msg[4] = (byte) val;


        _communicationEngine.write(msg);
    }

}

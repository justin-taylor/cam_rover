package org.rovertech.network;

public interface CamRoverListener
{
    public void camRoverDidSendMessage();
    public void camRoverDidReceiveMessage(byte[] message);
}

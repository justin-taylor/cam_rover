#!/bin/bash

#set static ip
sudo ifconfig wlan0 192.168.42.1
sudo service udhcpd start

#start server
cd /home/pi/cam_rover/raspberry/bin
sudo ./main 9300 2>&1 > /var/log/cam_server.log &

#start camera
cd /home/pi/webcam/
./run_streamer.sh 2>&1 > /dev/null &

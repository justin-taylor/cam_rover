/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <sys/socket.h>

#include <wiringPi.h>
#include <softPwm.h>

#define BUFFER_SIZE 512


#define MOTOR_CONTROLLER_SPEED_PIN 7 //physical pin 7
#define MOTOR_CONTROLLER_DXA_PIN 8 // physical pin 3
#define MOTOR_CONTROLLER_DXB_PIN 9 //physical pin 5
#define SERVO_PWN_PIN 0 //physical pin 11


/**
 * Prints the char[] to the console for logging
 */
void printBytes(char* message, int size)
{
	int i;
	for(i = 0; i < size; i++)
	{
		fprintf(stderr, "%d:", message[i]);
	}
}



/**
 * Handles messages received from the socket by reading the message values and
 * executing functions depending on the values.
 *
 * @param message the data received from the incoming socket
 * @param message_length the number of bytes in the message parameter
 * @param response the location to store the response to the message
 *
 * @return the number of bytes stored in the response
 */
int process_message(char *message, int message_length, char *response)
{
	
	if(message_length < 2)
		return -1;

	char command = message[0];

	//rover command
	if(command == 0x01)
	{
		int speed = message[1];
		softPwmWrite(MOTOR_CONTROLLER_SPEED_PIN, speed);

		int dirA = message[2] > 0 ? HIGH : LOW;
		int dirB = message[3] > 0 ? HIGH : LOW;

		digitalWrite(MOTOR_CONTROLLER_DXA_PIN, dirA);
		digitalWrite(MOTOR_CONTROLLER_DXB_PIN, dirB);

		int steer = message[4];
		softPwmWrite(SERVO_PWN_PIN, steer);
	}

	else
	{
		fprintf(stderr, "ELSE %d", command);
	}

	int i = 0;
	for(i; i < message_length; i++)
	{
		char c = message[i];
		// for now just echo the message back
		response[i] = c;
	}
	return message_length;
}



void initPins()
{
	if(wiringPiSetup() == -1)
		exit(1);

	int pins[] = {
		MOTOR_CONTROLLER_SPEED_PIN,
		MOTOR_CONTROLLER_DXA_PIN,
		MOTOR_CONTROLLER_DXB_PIN,
		SERVO_PWN_PIN
	};


	int i;
	int size = sizeof(pins) / sizeof(int);
	for(i = 0; i < size; i++)
	{
		int pin = pins[i];
		pinMode(pin, OUTPUT);
	}


	softPwmCreate(MOTOR_CONTROLLER_SPEED_PIN, 0, 100);
	softPwmCreate(SERVO_PWN_PIN, 0, 100);

	digitalWrite(MOTOR_CONTROLLER_DXA_PIN, LOW);
	digitalWrite(MOTOR_CONTROLLER_DXB_PIN, LOW);
}


/**
 * Prints an error message and exits
 *
 * @param errorMessage error message to be printed
 */
void errorExit(char *errorMessage)
{
	perror(errorMessage);
	/*exit(1);*/
}




/**
 * Initializes the local socket for receiving data. If an error occurs the
 * program will exit.
 *
 * @param port the port to bind too
 *
 * @return the socket file descriptor
 */
int setup_incoming_socket(int port)
{
	int sock;
	struct sockaddr_in echoserver;

	/* Create the UDP socket */
	if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0)
	{
		errorExit("Failed to create socket");
	}

	/* Construct the server sockaddr_in structure */
	memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
	echoserver.sin_family = AF_INET;                  /* Internet/IP */
	echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /* Any IP address */
	echoserver.sin_port = htons(port);       /* server port */

	/* Bind the socket */
	int serverlen = sizeof(echoserver);
	if (bind(sock, (struct sockaddr *) &echoserver, serverlen) < 0)
	{
		errorExit("Failed to bind server socket");
	}
         
	return sock;
}




/**
 * Receives data from the socket and stores the result in buffer. If an error
 * occurs the program will exit
 *
 * @return the number of bytes read from the socket
 */
int receive_message(int socket, char *buffer, struct sockaddr *client)
{
	unsigned int clientlen;
	clientlen = sizeof(client);
	int received = recvfrom(socket, buffer, BUFFER_SIZE, 0, client, &clientlen);
	if (received  < 0)
	{
		errorExit("Failed to receive message");
	}	

	return received;
}



int main(int argc, char *argv[])
{
	fprintf(stderr, "starting");

	if (argc != 2)
	{
		fprintf(stderr, "USAGE: %s <port>\n", argv[0]);
		exit(1);
	}

	initPins();
	fprintf(stderr, "inti pins");

	//setup the incoming socket
	int port = atoi(argv[1]);
	int socket = setup_incoming_socket(port);
	fprintf(stderr, "socket ready");

	// declare the variables needed for receiving and sending messages
	struct sockaddr_in client;
	char buffer[BUFFER_SIZE], process[BUFFER_SIZE];
	unsigned int received, processlen;
	unsigned int clientlen = sizeof(client);

	/* Run until cancelled */
	while (1)
	{
		// Read data from the client
		received = recvfrom(socket, buffer, BUFFER_SIZE, 0, (struct sockaddr *) &client, &clientlen);
		if (received  < 0)
		{
			errorExit("Failed to receive message");
		}	
		fprintf(stderr, "received\n");

		// log the bytes
		printBytes(buffer, received);

		// handle the message
		processlen = process_message(buffer, received, process);

		// Send a response back to client
		int sent = sendto(socket, process, processlen, 0, (struct sockaddr *) &client, clientlen);
		if (sent != processlen)
		{
			errorExit("Could not respond to client");
		}
		fprintf(stderr, "RESPONDED WITH %d\n", sent);
	}

	return 0;
}

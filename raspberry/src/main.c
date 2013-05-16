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

#define BUFFER_SIZE 512



/**
 * Prints an error message and exits
 *
 * @param errorMessage error message to be printed
 */
void errorExit(char *errorMessage)
{
	perror(errorMessage);
	exit(1);
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
	//TODO
	int i = 0;
	for(i; i < message_length; i++)
	{
		char c = message[i];
		// for now just echo the message back
		response[i] = c;
	}

	return message_length;
}



int main(int argc, char *argv[])
{
	if (argc != 2)
	{
		fprintf(stderr, "USAGE: %s <port>\n", argv[0]);
		exit(1);
	}


	//setup the incoming socket
	int port = atoi(argv[1]);
	int socket = setup_incoming_socket(port);

	struct sockaddr_in client;
	char buffer[BUFFER_SIZE], process[BUFFER_SIZE];
	int received, process_len;

	/* Run until cancelled */
	while (1)
	{
		received = receive_message(socket, buffer, (struct sockaddr *) &client);
		process_len = process_message(buffer, received, process);

		int size = sizeof(client);
		int sent = sendto(socket, buffer, received, 0, (struct sockaddr *) &client, size);

		/* Send the message back to client */
		if (sent != received)
		{
			errorExit("Could not respond to client");
		}
	}

	return 0;
}

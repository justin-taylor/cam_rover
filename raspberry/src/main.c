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
 * Initializes the local socket for receiving data
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



int main(int argc, char *argv[])
{
	if (argc != 2)
	{
		fprintf(stderr, "USAGE: %s <port>\n", argv[0]);
		exit(1);
	}


	int port = atoi(argv[1]);
	int socket = setup_incoming_socket(port);

	struct sockaddr_in echoclient;
	char buffer[BUFFER_SIZE];
	unsigned int echolen, clientlen;
	int received = 0;
	clientlen = sizeof(echoclient);

	/* Run until cancelled */
	while (1)
	{
		/* Receive a message from the client */
		received = recvfrom(socket, buffer, BUFFER_SIZE, 0, (struct sockaddr *) &echoclient, &clientlen);
		if (received  < 0)
		{
			errorExit("Failed to receive message");
		}

		//TODO process message


		fprintf(stderr, "Client connected: %s\n", inet_ntoa(echoclient.sin_addr));

		// TODO update to ACK
		/* Send the message back to client */
		if (sendto(socket, buffer, received, 0, (struct sockaddr *) &echoclient, sizeof(echoclient)) != received)
		{
			errorExit("Mismatch in number of echo'd bytes");
		}
	}

	return 0;
}

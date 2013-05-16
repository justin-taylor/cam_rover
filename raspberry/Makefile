CFLAGS=-Wall -g
OUTPUTDIR=bin/

all: main

main:
		#gcc src/main.c -I/usr/local/include -L/usr/local/lib -lwiringPi -o main
		mkdir -p $(OUTPUTDIR)
		gcc src/main.c -o $(OUTPUTDIR)main

clean:
		rm -rf $(OUTPUTDIR)
		rm -f main

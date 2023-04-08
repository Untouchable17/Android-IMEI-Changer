CC=g++
CFLAGS=-std=c++11 -Wall
LDFLAGS=-lstdc++

TARGET=IMEI-AndroidChanger.o

.PHONY: all clean

all: IMEI-AndroidChanger.cpp
	$(CC) $(CFLAGS) -c $< -o $(TARGET)

clean:
	rm -f $(TARGET)

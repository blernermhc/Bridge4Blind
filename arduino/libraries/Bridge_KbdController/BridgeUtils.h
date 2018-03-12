#ifndef BridgeUtils_h
#define BridgeUtils_h

#ifndef F_CPU
#include <avr/iom328p.h> /* rick@cs: NOTE: added for use in eclipse, so we see board-specific defines; REMOVE for use */
#define F_CPU 16000000L /* rick@cs: NOTE: added for use in eclipse, ... */
#endif // F_CPU

#include <HardwareSerial.h>
extern HardwareSerial Serial;

#include <stdint.h>
/**
 * \file
 * BridgeUtils class
 */

#define MESSAGE_START_STRING 0b11000000

void sendSerialString ();

#endif //BridgeUtils_h

/**************************************************************************/
/*! 
    @file     iso14443a_uid.pde
    @author   Adafruit Industries
	@license  BSD (see license.txt)

    This example will attempt to connect to an ISO14443A
    card or tag and retrieve some basic information about it
    that can be used to determine what type of card it is.   
   
    Note that you need the baud rate to be 115200 because we need to print
	out the data and read from the card at the same time!

This is an example sketch for the Adafruit PN532 NFC/RFID breakout boards
This library works with the Adafruit NFC breakout 
  ----> https://www.adafruit.com/products/364
 
Check out the links above for our tutorials and wiring diagrams 
These chips use SPI or I2C to communicate.

Adafruit invests time and resources providing this open source code, 
please support Adafruit and open-source hardware by purchasing 
products from Adafruit!

*/
/**************************************************************************/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_PN532.h>

// If using the breakout with SPI, define the pins for SPI communication.
//#define PN532_SCK  (2)
//#define PN532_MOSI (3)
//#define PN532_SS   (4)
//#define PN532_MISO (5)

#define PN532_SCK  (13)
#define PN532_MISO (12)
#define PN532_MOSI (11)
#define PN532_SS   (10)

// If using the breakout or shield with I2C, define just the pins connected
// to the IRQ and reset lines.  Use the values below (2, 3) for the shield!
#define PN532_IRQ   (2)
#define PN532_RESET (3)  // Not connected by default on the NFC Shield

// Uncomment just _one_ line below depending on how your breakout or shield
// is connected to the Arduino:

// Use this line for a breakout with a SPI connection:
Adafruit_PN532 nfc(PN532_SCK, PN532_MISO, PN532_MOSI, PN532_SS);

// Use this line for a breakout with a hardware SPI connection.  Note that
// the PN532 SCK, MOSI, and MISO pins need to be connected to the Arduino's
// hardware SPI SCK, MOSI, and MISO pins.  On an Arduino Uno these are
// SCK = 13, MOSI = 11, MISO = 12.  The SS line can be any digital IO pin.
//Adafruit_PN532 nfc(PN532_SS);

// Or use this line for a breakout or shield with an I2C connection:
//Adafruit_PN532 nfc(PN532_IRQ, PN532_RESET);

#if defined(ARDUINO_ARCH_SAMD)
// for Zero, output on USB Serial console, remove line below if using programming port to program the Zero!
// also change #define in Adafruit_PN532.cpp library file
   #define Serial SerialUSB
#endif

void setup(void)
{
  #ifndef ESP8266
    while (!Serial); // for Leonardo/Micro/Zero
  #endif
  Serial.begin(115200);
  Serial.println("Resetting Antenna");

  nfc.begin();

  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata)
  {
    Serial.print("Didn't find PN53x board");
    while (1); // halt
  }
  
  // Got ok data, print it out!
  Serial.print("Found chip PN5"); Serial.println((versiondata>>24) & 0xFF, HEX); 
  Serial.print("Firmware ver. "); Serial.print((versiondata>>16) & 0xFF, DEC); 
  Serial.print('.'); Serial.println((versiondata>>8) & 0xFF, DEC);
  
  // Set the max number of retry attempts to read from a card
  // This prevents us from waiting forever for a card, which is
  // the default behaviour of the PN532.  Documentation says
  // 0xFF means wait forever
  // nfc.setPassiveActivationRetries(0xFF);
  nfc.setPassiveActivationRetries(0x0F);
  
  // configure board to read RFID tags
  nfc.SAMConfig();
  
  Serial.println("Waiting for an ISO14443A card");
  Serial.println("Reset Complete");
}

#define CHAR_BUFFER_SIZE 15
char charBuffer0[] = { 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 };  // 14 hex chars + 1 for null
char charBuffer1[] = { 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0 };  // 14 hex chars + 1 for null

char* lastCardHex = &charBuffer0[0];
char* cardHex = &charBuffer1[0];

void loop(void)
{
  boolean success;
  uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };	// Buffer to store the returned UID
  uint8_t uidLength;				// Length of the UID (4 or 7 bytes depending on ISO14443A card type)
  
  // Wait for an ISO14443A type cards (Mifare, etc.).  When one is found
  // 'uid' will be populated with the UID, and uidLength will indicate
  // if the uid is 4 bytes (Mifare Classic) or 7 bytes (Mifare Ultralight)
  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, &uid[0], &uidLength);

  if (success)
  {
	  bytesToHex(uid, uidLength, cardHex, CHAR_BUFFER_SIZE);
	  
	  if (strcmp(cardHex, lastCardHex) != 0)
	  {
		  // new card is different than old one
		  Serial.print("CARD: 0x");
		  Serial.println(cardHex);

		  // swap buffers to remember the current card
		  char* tempHex = lastCardHex;
		  lastCardHex = cardHex;
		  cardHex = tempHex;
		  cardHex[0] = 0;
	  }
	  
	  // I don't see any reason to wait before continuing
	  // delay(250);
  }
  else
  {
	  // PN532 probably timed out waiting for a card

	  // notify controller (but not repeatedly)
	  if (lastCardHex[0] != 0) { Serial.println ("CARD REMOVED"); }

	  // clear remembered card
	  lastCardHex[0] = 0;
  }
}

char nibbleToHexChar (uint8_t p_nibble)
{
	switch (p_nibble)
	{
    case 0: return '0';
    case 1: return '1';
    case 2: return '2';
    case 3: return '3';
    case 4: return '4';
    case 5: return '5';
    case 6: return '6';
    case 7: return '7';
    case 8: return '8';
    case 9: return '9';
    case 10: return 'A';
    case 11: return 'B';
    case 12: return 'C';
    case 13: return 'D';
    case 14: return 'E';
    case 15: return 'F';
    default: return '0';		// should not be possible
	}
}

uint8_t bytesToHex (uint8_t* p_bytes, int p_numBytes, char* p_buffer, int p_bufSize)
{
	if ( ((p_numBytes * 2) + 1) > p_bufSize)
	{
		// input too long
		Serial.print("bytesToHex: input too long.  numBytes: ");
		Serial.print(p_numBytes, DEC);
		Serial.print(" buf char len: ");
		Serial.print(p_bufSize, DEC);
		Serial.println("");
		return 0;
	}
	
	int bufIdx = 0;
	for (uint8_t i=0; i < p_numBytes; ++i) 
	{
		uint8_t highNibble = (p_bytes[i] >> 4);
		uint8_t lowNibble = (p_bytes[i] & 0xF);
		p_buffer[bufIdx++] = nibbleToHexChar(highNibble);
	    	p_buffer[bufIdx++] = nibbleToHexChar(lowNibble);
	}
	p_buffer[bufIdx++] = 0;
}

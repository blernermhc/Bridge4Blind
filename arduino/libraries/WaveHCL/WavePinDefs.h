/**
 * \file
 * Pin definitions
 */
#include <ArduinoPins.h>
#ifndef WavePinDefs_h
#define WavePinDefs_h

//SPI pin definitions

/** SPI slave select pin. Warning: SS may be redefined as another pin
 but the hardware SS_PIN must be set to output mode before any calls to
 WaveHC functions. The SS_PIN can then be used as a general output pin */
#define SS   SS_PIN

/** SPI master output, slave input pin. */
#define MOSI MOSI_PIN

/** SPI master input, slave output pin. */
#define MISO MISO_PIN

/** SPI serial clock pin. */
#define SCK  SCK_PIN

//------------------------------------------------------------------------------
// DAC pin definitions

// LDAC may be connected to ground to save a pin
/** Set USE_MCP_DAC_LDAC to 0 if LDAC is grounded. */
#define USE_MCP_DAC_LDAC 1

// use arduino pins 5, 6, 7, 8 for DAC

// pin 5 is DAC chip select

/** Data direction register for DAC chip select. */
#define MCP_DAC_CS_DDR  PIN5_DDRREG
/** Port register for DAC chip select. */
#define MCP_DAC_CS_PORT PIN5_PORTREG
/** Port bit number for DAC chip select. */
#define MCP_DAC_CS_BIT  PIN5_BITNUM

// pin 6 is DAC serial clock
/** Data direction register for DAC clock. */
#define MCP_DAC_SCK_DDR  PIN6_DDRREG
/** Port register for DAC clock. */
#define MCP_DAC_SCK_PORT PIN6_PORTREG
/** Port bit number for DAC clock. */
#define MCP_DAC_SCK_BIT  PIN6_BITNUM

// pin 7 is DAC serial data in

/** Data direction register for DAC serial in. */
#define MCP_DAC_SDI_DDR  PIN7_DDRREG
/** Port register for DAC clock. */
#define MCP_DAC_SDI_PORT PIN7_PORTREG
/** Port bit number for DAC clock. */
#define MCP_DAC_SDI_BIT  PIN7_BITNUM

// pin 8 is LDAC if used
#if USE_MCP_DAC_LDAC
/** Data direction register for Latch DAC Input. */
#define MCP_DAC_LDAC_DDR  PIN8_DDRREG
/** Port register for Latch DAC Input. */
#define MCP_DAC_LDAC_PORT PIN8_PORTREG
/** Port bit number for Latch DAC Input. */
#define MCP_DAC_LDAC_BIT  PIN8_BITNUM
#endif // USE_MCP_DAC_LDAC

#endif // WavePinDefs_h

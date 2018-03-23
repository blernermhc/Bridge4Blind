#include <Wavemainpage.h>
#include <mcpDac.h>
#include <ArduinoPins.h>
#include <WavePinDefs.h>
#include <SdInfo.h>
#include <WaveUtil.h>
#include <WaveHCL.h>
#include <BridgeHand.h>
#include <Phrases.h>
#include <Eventlist.h>
#include <FatStructs.h>
#include <EEPROM.h>


SdReader card;    // This object holds the information for the card
FatVolume vol;    // This holds the information for the partition on the card
FatReader root;   // This holds the information for the filesystem on the card
FatReader f;      // This holds the information for the file we're play

WaveHCL wave;      // This is the only wave (audio) object, since we will only play one at a time
BridgeHand bridgeHand;
Phrases phrases;
EventList eventList;

/** Tells Game Controller to read up to next newline */
#define START_SEND_MSG 0b11000000

/** Tells Game Controller to read up to next "Ready!\n" */
#define RESTARTING_MSG 0b11000001

/** Tells Game Controller to resend state */
#define INITIATE_RESET_MSG 0b11000010

// options
#define SUIT_HIGH_LOW 1

// global state data
uint8_t s_options = 0;

uint8_t s_selectedSuitId = SUITID_NOT_SET;
uint8_t s_selectedCardId = CARDID_NOT_SET;  // 13: void; 0: two; 1: three, etc.

volatile uint8_t		Button    = 0;			// Required for 64 Button Shield (SPI Only)
volatile uint8_t		m_previousButtonId = 0;	// Used to detect pressing same button twice (e.g., State)

// Function button cycle
// NOTE: sendPosition() sets mode to the mode prior to MODE_SET_POSITION.
//       update that function if the mode order changes.
#define MODE_PLAY_HAND 0
#define MODE_UNDO 1
#define MODE_REDO 2
#define MODE_SET_POSITION 3
#define MODE_ENTER_CONTRACT 4
#define MODE_RESYNCHRONIZE 5
#define MODE_START_NEW_HAND 6
#define MODE_DEAL_HANDS 7
// #define MODE_SET_OPTIONS 6

#define SUBMODE_ENTER_CONTRACT_WINNER 0
#define SUBMODE_ENTER_CONTRACT_TRICKS 1
#define SUBMODE_ENTER_CONTRACT_SUIT 2

// Play must be pressed twice
#define SUBMODE_START_NEW_HAND_INITIAL 0
#define SUBMODE_START_NEW_HAND_WAIT_CONFIRM 1

// Play must be pressed twice
#define SUBMODE_UNDO_WAIT_CONFIRM 1

// indicates keyboard mode (determines what keys are active, etc.)
uint8_t s_mode = MODE_PLAY_HAND;

// for multi-step modes, indicates what step we are at (e.g., for setting contract)
uint8_t s_submode = 0;

// data to be sent using the various function modes

// playerId to send for setPosition mode
uint8_t s_setPosition_playerId;

// contract info to send for enter contract mode
uint8_t s_contract_winner;
uint8_t s_contract_tricks;
uint8_t s_contract_suit;

// indicates if undo mode is attempting an undo or a redo
uint8_t s_redoFlag;

#define DEBOUNCE 100  // button debouncer

// this handy function will return the number of bytes currently free in RAM, great for debugging!   
int freeRam(void)
{
  extern int  __bss_end; 
  extern int  *__brkval; 
  int free_memory; 
  if((int)__brkval == 0) {
    free_memory = ((int)&free_memory) - ((int)&__bss_end); 
  }
  else {
    free_memory = ((int)&free_memory) - ((int)__brkval); 
  }
  return free_memory; 
} 

void sdErrorCheck(void)
{
  if (!card.errorCode()) return;
  putstring("\n\rSD I/O error: ");
  Serial.print(card.errorCode(), HEX);
  putstring(", ");
  Serial.println(card.errorData(), HEX);
  while(1);
}

// Address we will use to store the position
#define NON_VOLATILE_POSITION_ADDR 0

//-------------------------------------------------------------
// Copies the position for non-volatile storage into variable storage.
// Sets variable to PLAYERID_NOT_SET if the non-volatile value is out of range.
//-------------------------------------------------------------
uint8_t loadNonVolatilePosition ()
{
	uint8_t val = EEPROM.read(NON_VOLATILE_POSITION_ADDR);
	if (val > PLAYERID_NOT_SET) val = PLAYERID_NOT_SET;
	bridgeHand.m_myPlayerId = val;
	return val;
}

//-------------------------------------------------------------
// Copies the current position into non-volatile storage, if different.
// Assumes the current value is "valid".
//-------------------------------------------------------------
void saveNonVolatilePosition ()
{
	uint8_t val = EEPROM.read(NON_VOLATILE_POSITION_ADDR);
	if (val != bridgeHand.m_myPlayerId)
	{
	  EEPROM.write(NON_VOLATILE_POSITION_ADDR, bridgeHand.m_myPlayerId);
	}
}

void printSerialMessagePrefix ()
{
	  Serial.print("Keyboard(");
	  Serial.print(bridgeHand.m_myPlayerId, DEC);
	  Serial.print("): ");
}

void setup()
{
	  // see if we know our position from an earlier run
	  loadNonVolatilePosition();

  // set up serial port
  Serial.begin(9600);
  Serial.write(RESTARTING_MSG);	// tell Game Controller we are restarting
  printSerialMessagePrefix();
  putstring_nl("Resetting");
  printSerialMessagePrefix();
  putstring_nl("v1.2");
  
  printSerialMessagePrefix();
  putstring("Free RAM: ");       // This can help with debugging, running out of RAM is bad
  Serial.println(freeRam());      // if this is under 150 bytes it may spell trouble!
  
  // Set the output pins for the DAC control. This pins are defined in the library
  /*
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(5, OUTPUT);
  */

  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(8, OUTPUT);
 
  // pin13 LED
  pinMode(13, OUTPUT);
 
  // enable pull-up resistors on switch pins (analog inputs)
  digitalWrite(14, HIGH);
  digitalWrite(15, HIGH);
  digitalWrite(16, HIGH);
  digitalWrite(17, HIGH);
  digitalWrite(18, HIGH);
  digitalWrite(19, HIGH);
 
  //  if (!card.init(true)) { //play with 4 MHz spi if 8MHz isn't working for you
  if (!card.init()) {         //play with 8 MHz spi (default faster!)  
	printSerialMessagePrefix();
    putstring_nl("Card init. failed!");  // Something went wrong, lets print out why
    sdErrorCheck();
    while(1);                            // then 'halt' - do nothing!
  }
  
  // enable optimize read - some cards may timeout. Disable if you're having problems
  card.partialBlockRead(true);
 
// Now we will look for a FAT partition!
  uint8_t part;
  for (part = 0; part < 5; part++) {     // we have up to 5 slots to look in
    if (vol.init(card, part)) 
      break;                             // we found one, lets bail
  }
  if (part == 5) {                       // if we ended up not finding one  :(
	printSerialMessagePrefix();
    putstring_nl("No valid FAT partition!");
    sdErrorCheck();      // Something went wrong, lets print out why
    while(1);                            // then 'halt' - do nothing!
  }
  
  // Lets tell the user about what we found
  printSerialMessagePrefix();
  putstring("Using partition ");
  Serial.print(part, DEC);
  putstring(", type is FAT");
  Serial.println(vol.fatType(),DEC);     // FAT16 or FAT32?
  
  // Try to open the root directory
  if (!root.openRoot(vol))
  {
	printSerialMessagePrefix();
    putstring_nl("Cannot open root dir!"); // Something went wrong,
    while(1);                             // then 'halt' - do nothing!
  }
  
  wave.setReader(root,f);
  phrases.setWave(&wave);
  bridgeHand.setPhrases(&phrases);
  
  // Whew! We got past the tough parts.
  printSerialMessagePrefix();
  putstring_nl("Ready!");

  //------------------------------------
  // Setup Button64 Shield
  //------------------------------------
  delay(1000);
  attachInterrupt(0, SPI64B, FALLING);                 // Required for 64 Button Shield (SPI Only)


  // restore current state, in case we rebooted during a game
  keyboardRestartInitiate();
}

void loop()
{
  //Serial.write(START_SEND_MSG);	// tell Game Controller to read to next newline
  //putstring_nl("in loop");            // uncomment this to see if the loop isnt running
  checkButton();
  processInput();
}

#define INPUT_FORMAT_MASK 0b10000000
#define INPUT_OPID2_MASK 0b01111000
#define INPUT_OPID2_SHIFT 3
#define INPUT_OPID1_MASK 0b00111111
#define INPUT_SUITID_MASK 0b00000111
#define INPUT_PLAYERID_MASK 0b11110000
#define INPUT_PLAYERID_SHIFT 4
#define INPUT_CARDID_MASK 0b00001111
#define INPUT_BUTTON_MASK 0b01000000

#define INPUT_UNDO_MASK 0b11111000
#define INPUT_UNDO_CONFIRM_FLAG_MASK 0b10000000
#define INPUT_UNDO_REDO_FLAG_MASK 0b01000000
#define INPUT_UNDO_EVENTID_MASK 0b00111111

//#define DEBUG_KB 1

/****************************************************************** 
 * Parse and process three-byte requests.
 ****************************************************************/
void processUndoAnnouncement (uint8_t p_input0, uint8_t p_input1, uint8_t p_input2, uint8_t p_repeat)
{
  uint8_t opId = ((p_input0 & INPUT_OPID2_MASK) >> INPUT_OPID2_SHIFT);
  uint8_t suitId = (p_input0 & INPUT_SUITID_MASK);
  uint8_t playerId = ((p_input1 & INPUT_PLAYERID_MASK) >> INPUT_PLAYERID_SHIFT);
  uint8_t cardId = (p_input1 & INPUT_CARDID_MASK);
  
  uint8_t confirmedFlag	= ((p_input2 & INPUT_UNDO_CONFIRM_FLAG_MASK) != 0);
  uint8_t redoFlag		= ((p_input2 & INPUT_UNDO_REDO_FLAG_MASK) != 0);
  uint8_t undoEventId	= (p_input2 & INPUT_UNDO_EVENTID_MASK);

  if (confirmedFlag)
  {
	  s_mode = MODE_PLAY_HAND;
  }
  else
  {
	  s_mode = MODE_UNDO;
	  s_submode = SUBMODE_UNDO_WAIT_CONFIRM;
	  s_redoFlag = redoFlag;
  }
  phrases.playUndoMode(confirmedFlag, redoFlag, undoEventId, playerId, cardId, suitId, NEW_AUDIO);
}

/****************************************************************** 
 * Parse and process two-byte requests.
 ****************************************************************/
void processInput2 (uint8_t p_input0, uint8_t p_input1, uint8_t p_repeat)
{
  uint8_t opId = ((p_input0 & INPUT_OPID2_MASK) >> INPUT_OPID2_SHIFT);
  uint8_t suitId = (p_input0 & INPUT_SUITID_MASK);
  uint8_t playerId = ((p_input1 & INPUT_PLAYERID_MASK) >> INPUT_PLAYERID_SHIFT);
  uint8_t cardId = (p_input1 & INPUT_CARDID_MASK);

#ifdef DEBUG_KB
  Serial.write(START_SEND_MSG);	// tell Game Controller to read to next newline
  putstring("in processInput2: input0: ");
  Serial.print(p_input0);
  putstring(" input1: ");
  Serial.print(p_input1);
  putstring(" opId: ");
  Serial.print(opId);
  putstring(" playerId: ");
  Serial.print(playerId);
  putstring(" cardId: ");
  Serial.print(cardId);
  putstring(" suitId: ");
  Serial.print(suitId);
  putstring_nl("");
#endif

  switch (opId)
  {
    case 0:	break;
    case 1:	bridgeHand.setPlayer(playerId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); saveNonVolatilePosition(); break;
    case 2:	bridgeHand.setDummy(playerId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 3:	bridgeHand.setNextPlayer(playerId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 4:	bridgeHand.setContract(playerId, suitId, cardId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 5:	resetKeyboard(playerId); bridgeHand.addCardToHand(playerId, suitId, cardId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 6:	resetKeyboard(playerId); bridgeHand.useCard(playerId, suitId, cardId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 7:	resetKeyboard(playerId); bridgeHand.unuseCard(playerId, suitId, cardId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 8:	bridgeHand.trickFinished(playerId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 9:	bridgeHand.cannotPlay(suitId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
    case 10:    setOptions(p_input0, p_input1, p_repeat); break;
    default:
      phrases.playNumber(SND_UNEXPECTED_OP_2, opId, NEW_AUDIO);
      eventList.addEvent(p_input0, p_input1, p_repeat);
  }    
}

/****************************************************************** 
 * Parse and process one-byte requests (except button simulations)
 ****************************************************************/
void processInput1 (uint8_t p_input0, uint8_t p_repeat)
{
  uint8_t opId = (p_input0 & INPUT_OPID1_MASK);

#ifdef DEBUG_KB
  Serial.write(START_SEND_MSG);	// tell Game Controller to read to next newline
  putstring("in processInput1: input0: ");
  Serial.print(p_input0);
  putstring(" opId: ");
  Serial.print(opId);
  putstring_nl("");
#endif

  switch (opId)
  {
    case 0:	break;
    case 1:	phrases.playMessage(SND_SCAN_HAND, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 2:	phrases.playMessage(SND_SCAN_DUMMY, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 4:	phrases.playMessage(SND_IN_HAND, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 5:	phrases.playMessage(SND_ON_BOARD, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 6:	bridgeHand.handComplete(p_repeat); eventList.addEvent(p_input0, p_repeat); break;
    case 9:	eventList.resetEventList(p_repeat); bridgeHand.newGame(); break;
    case 10:	eventList.resetEventList(p_repeat); bridgeHand.newHand(); break;
    case 11:	eventList.resetEventList(false); keyboardRestartInitiate(); break;
    case 12:	keyboardRestartComplete(); eventList.addEvent(p_input0, p_repeat); break;
    case 13:	phrases.playMessage(SND_ENTER_CONTRACT, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 14:	phrases.playMessage(SND_CARD_ALREADY_PLAYED, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 15:	phrases.playMessage(SND_CARD_NOT_IN_HAND, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 16:	sendPosition(p_repeat), eventList.addEvent(p_input0, p_repeat); break;
    default:
      phrases.playNumber(SND_UNEXPECTED_OP_1, opId, NEW_AUDIO);
      eventList.addEvent(p_input0, p_repeat);
  }    
}

/** 
 * Initiated from game controller and setup
 * Game Controller can reset keyboard controller by sending the start reset command.
 * The Game Controller then waits for the Keyboard Controller to send the reset command back to the Game Controller
 * so the Game Controller knows the Keyboard Controller is listening.
 *
 * When the Keyboard Controller runs its setup routine, either upon power-up or because of a fault restart, it
 * Sends the reset command to the Game Controller so that the Game Controller will send the current state.
 *
 * This function sends the reset command to the Game Controller.
 */
void keyboardRestartInitiate()
{
  Serial.write(START_SEND_MSG);		// tell Game Controller to read to next newline
  printSerialMessagePrefix();
  putstring_nl("Sending restart");

  phrases.playMessage(SND_RESET_STARTED, NEW_AUDIO); 
  phrases.setSilentMode(true);

  Serial.write(INITIATE_RESET_MSG);	// tell Game Controller to resend the state
}

void keyboardRestartComplete()
{
  phrases.setSilentMode(false);
  phrases.playMessage(SND_RESET_FINISHED, NEW_AUDIO);
}


/****************************************************************** 
 * Parse and process button press simulations
 ****************************************************************/
void processInputButton (uint8_t p_input0, uint8_t p_repeat)
{
  uint8_t buttonId = (p_input0 & INPUT_OPID1_MASK);

#ifdef DEBUG_KB
  Serial.write(START_SEND_MSG);	// tell Game Controller to read to next newline
  putstring("in processInputButton: input0: ");
  Serial.print(p_input0);
  putstring(" buttonId: ");
  Serial.print(buttonId);
  putstring_nl("");
#endif

  switch (buttonId)
  {
    case 0:	btn_play(); break;
    case 1:	btn_H1(); break;
    case 2:	btn_H2(); break;
    case 3:	btn_H3(); break;
    case 4:	btn_H4(); break;
    case 5:	btn_HC(); break;
    case 6:	btn_D1(); break;
    case 7:	btn_D2(); break;
    case 8:	btn_D3(); break;
    case 9:	btn_D4(); break;
    case 10:	btn_DC(); break;
    case 11:	btn_up(); break;
    case 12:	btn_down(); break;
    case 13:	btn_repeat(); break;
    case 14:	btn_state(buttonId == m_previousButtonId); break;
    case 15:	btn_function(); break;
    default:
      phrases.playNumber(SND_UNEXPECTED_BUTTON, buttonId, NEW_AUDIO);
  }
  m_previousButtonId = buttonId;
}

/****************************************************************** 
 * Read commands from the serial line
 ****************************************************************/
void processInput()
{
  if (Serial.available() <= 0) return;

  uint8_t input0 = Serial.read();

  if ((input0 & INPUT_FORMAT_MASK) != 0)
  {
    int maxAttempts = 10000;
    while (Serial.available() <= 0 && --maxAttempts > 0);	// wait for 2nd byte
    if (Serial.available() <= 0)
    {
      uint8_t opId = ((input0 & INPUT_OPID2_MASK) >> INPUT_OPID2_SHIFT);
      phrases.playNumber(SND_INCOMPLETE_MSG, opId, NEW_AUDIO);
      uint8_t code = 0b11100000 | opId;
      Serial.write(code);
      return;
    }
    uint8_t input1 = Serial.read();
    
    if ((input0 & INPUT_UNDO_MASK) == INPUT_UNDO_MASK)
    { // read third byte for undo/redo announcements
      int maxAttempts = 10000;
      while (Serial.available() <= 0 && --maxAttempts > 0);	// wait for 2nd byte
      if (Serial.available() <= 0)
      {
        uint8_t opId = ((input0 & INPUT_OPID2_MASK) >> INPUT_OPID2_SHIFT);
        phrases.playNumber(SND_INCOMPLETE_MSG, opId, NEW_AUDIO);
        uint8_t code = 0b11100000 | opId;
        Serial.write(code);
        return;
      }
      uint8_t input2 = Serial.read();
      processUndoAnnouncement(input0, input1, input2, 0);
    }
    else
    {
		processInput2(input0, input1, 0);
    }
  }
  else
  {
    if ((input0 & INPUT_BUTTON_MASK) == 0)
      processInput1(input0, 0);
    else
      processInputButton(input0, 0);
  }
}

void setOptions (uint8_t p_input0, uint8_t p_input1, uint8_t p_repeat)
{
	if (p_repeat) return;

	s_options = p_input1;
}

//----------------------------------------------------------------------
//----------------------------------------------------------------------
//----------------------------------------------------------------------

//----------------------------------------------------------------------
// 0: two; 1: three; ... 12: ace; 13: void
//----------------------------------------------------------------------
uint8_t lowCard (uint16_t p_bitmap)
{
  if (p_bitmap == 0) return 13;
  
  uint8_t r = 0;
  uint16_t t = 1;
  while ((p_bitmap & t) == 0) { t <<= 1; ++r;}
  return r;
}

//----------------------------------------------------------------------
// 0: two; 1: three; ... 12: ace; 13: void
//----------------------------------------------------------------------
uint8_t highCard (uint16_t p_bitmap)
{
  if (p_bitmap == 0) return 13;
  
  uint8_t r = 0;
  while (p_bitmap >>= 1) { ++r; }
  return r;
}

//----------------------------------------------------------------------
// 0: two; 1: three; ... 12: ace; 13: void
// if none higher, returns p_curCard
//----------------------------------------------------------------------
uint8_t nextCardUp (uint16_t p_bitmap, uint16_t p_curCard)
{
  if (p_bitmap == 0) return 13;
  if (p_curCard == 13) return 13;
  if (p_curCard == 12) return 12;
  
  p_bitmap >>= (p_curCard + 1);
  uint8_t next = lowCard(p_bitmap);
  if (next == 13) return p_curCard;
  return p_curCard + 1 + next;
}

//----------------------------------------------------------------------
// 0: two; 1: three; ... 12: ace; 13: void
// if none lower, returns p_curCard
//----------------------------------------------------------------------
uint8_t nextCardDown (uint16_t p_bitmap, uint16_t p_curCard)
{
  if (p_bitmap == 0) return 13;
  if (p_curCard == 13) return 13;
  if (p_curCard == 0) return 0;
  
  p_bitmap = p_bitmap & ((1 << p_curCard) - 1);

  uint8_t next = highCard(p_bitmap);
  if (next == 13) return p_curCard;
  return next;
}


//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_up()
{
    Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
    putstring("in btn_up: s_selectedSuitId: ");
    Serial.print(s_selectedSuitId);
    putstring(" s_selectedCardId: ");
    Serial.print(s_selectedCardId);
    putstring(" myPlayerId: ");
    Serial.print(bridgeHand.m_myPlayerId);
    putstring(" dummyPlayerId: ");
    Serial.print(bridgeHand.m_dummyPlayerId);
    putstring_nl("");

    if (s_mode == MODE_ENTER_CONTRACT && s_submode == SUBMODE_ENTER_CONTRACT_TRICKS)
    {
    		s_contract_tricks = s_contract_tricks + 1;
    		if (s_contract_tricks > 7) s_contract_tricks = 7;
    		phrases.playNumber(0, s_contract_tricks, NEW_AUDIO);
    		return;
    }
    else if (s_mode != MODE_PLAY_HAND)
    {
    		announce_mode();
    		return;
    }
    
  if (bridgeHand.m_myPlayerId == bridgeHand.m_dummyPlayerId)
  {
	phrases.playMessage(SND_YOU_ARE_DUMMY, NEW_AUDIO);
	return;
  }


  if (s_selectedSuitId == SUITID_NOT_SET) s_selectedSuitId = bridgeHand.getCurrentSuitId();
  if (s_selectedSuitId == SUITID_NOT_SET)
  {
    phrases.playMessage(SND_SELECT_SUIT, NEW_AUDIO);
    return;
  }
  
  if (s_selectedCardId == CARDID_NOT_SET)
  {
    s_selectedCardId = highCard(bridgeHand.getHand(bridgeHand.getCurrentHandId(), s_selectedSuitId));
    phrases.playSelectedCard (bridgeHand.getCurrentHandId(), s_selectedCardId, s_selectedSuitId, NEW_AUDIO);
  }
  else
  {
    s_selectedCardId = nextCardUp(bridgeHand.getHand(bridgeHand.getCurrentHandId(), s_selectedSuitId), s_selectedCardId);
    phrases.playSelectedCard (2, s_selectedCardId, s_selectedSuitId, NEW_AUDIO);  // skip player annoucement
  }
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_down()
{
    Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
    putstring("in btn_down: s_selectedSuitId: ");
    Serial.print(s_selectedSuitId);
    putstring(" s_selectedCardId: ");
    Serial.print(s_selectedCardId);
    putstring(" myPlayerId: ");
    Serial.print(bridgeHand.m_myPlayerId);
    putstring(" dummyPlayerId: ");
    Serial.print(bridgeHand.m_dummyPlayerId);
    putstring_nl("");

    if (s_mode == MODE_ENTER_CONTRACT && s_submode == SUBMODE_ENTER_CONTRACT_TRICKS)
    {
    		s_contract_tricks = s_contract_tricks - 1;
    		if (s_contract_tricks < 1) s_contract_tricks = 1;
    		phrases.playNumber(0, s_contract_tricks, NEW_AUDIO);
    		return;
    }
    else if (s_mode != MODE_PLAY_HAND)
    {
    		announce_mode();
    		return;
    }
    
  if (bridgeHand.m_myPlayerId == bridgeHand.m_dummyPlayerId)
  {
	phrases.playMessage(SND_YOU_ARE_DUMMY, NEW_AUDIO);
	return;
  }

  if (s_selectedSuitId == SUITID_NOT_SET) s_selectedSuitId = bridgeHand.getCurrentSuitId();
  if (s_selectedSuitId == SUITID_NOT_SET)
  {
    phrases.playMessage(SND_SELECT_SUIT, NEW_AUDIO);
    return;
  }

  if (s_selectedCardId == CARDID_NOT_SET)
  {
    s_selectedCardId = lowCard(bridgeHand.getHand(bridgeHand.getCurrentHandId(), s_selectedSuitId));
    phrases.playSelectedCard (bridgeHand.getCurrentHandId(), s_selectedCardId, s_selectedSuitId, NEW_AUDIO);
  }
  else
  {
    s_selectedCardId = nextCardDown(bridgeHand.getHand(bridgeHand.getCurrentHandId(), s_selectedSuitId), s_selectedCardId);
    phrases.playSelectedCard (2, s_selectedCardId, s_selectedSuitId, NEW_AUDIO);  // skip player annoucement
  }
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_repeat()
{
    if (s_mode != MODE_PLAY_HAND)
    {	// if in a function mode, restore normal PLAY HAND mode
    		s_mode = MODE_PLAY_HAND;
    		s_submode = 0;
    		announce_mode();
    		return;
    }

  uint8_t input0 = eventList.previousEvent();
  if ((input0 & INPUT_FORMAT_MASK) != 0)
  {
    uint8_t input1 = eventList.previousEvent();
    processInput2(input0, input1, 1);
  }
  else
  {
    if ((input0 & INPUT_BUTTON_MASK) == 0)
      processInput1(input0, 1);
    else
      processInputButton(input0, 1);
  }
}

//----------------------------------------------------------------------
// press a 2nd time to stop audio
//----------------------------------------------------------------------
void btn_state(uint8_t p_isSecondPress)
{
    if (s_mode != MODE_PLAY_HAND)
    {
    		announce_mode();
    		return;
    }

  if (p_isSecondPress &&  wave.isplaying)
    wave.stop();
  else
    bridgeHand.playState();
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_play()
{
	switch (s_mode)
	{
		case (MODE_UNDO):
		{
			Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
			putstring("CMD: UNDO");
			putstring_nl("");

			s_mode = MODE_PLAY_HAND;
			return;
		}
		break;

	    case (MODE_SET_POSITION):
		{
			  Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
			  putstring("CMD: KBDPOS ");
			  Serial.print(s_setPosition_playerId);
			  putstring_nl("");

			  s_mode = MODE_PLAY_HAND;
			  return;
		}
	    break;
	    
	    case (MODE_ENTER_CONTRACT):
		{
			s_submode = s_submode + 1;
			if (s_submode >= NUMCONTRACTMODES)
			{
				  Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
				  putstring("CMD: CONTRACT ");
				  Serial.print(s_contract_winner);
				  putstring(" ");
				  Serial.print(s_contract_tricks);
				  putstring(" ");
				  Serial.print(s_contract_suit);
				  putstring_nl("");

				  s_submode = 0;
				  s_mode = MODE_PLAY_HAND;
			}
			else
			{
				phrases.playContractMode(s_submode, NEW_AUDIO);
			}
			return;
		}
	    break;

	    case (MODE_RESYNCHRONIZE):
		{
			  Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
			  putstring("CMD: RESET");
			  putstring_nl("");

			  s_mode = MODE_PLAY_HAND;
			  return;
		}
	    break;
	    
	    case (MODE_START_NEW_HAND):
		{
			if (s_submode == SUBMODE_START_NEW_HAND_WAIT_CONFIRM)
			{
				  Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
				  putstring("CMD: NEWHAND");
				  putstring_nl("");

				  s_submode = 0;
				  s_mode = MODE_PLAY_HAND;
			}
			else
			{
				s_submode = s_submode + 1;
				phrases.playMessage(SND_CONFIRM_START_NEW_HAND, NEW_AUDIO);
			}
			return;
		}
	    break;

	    case (MODE_DEAL_HANDS):
		{
			  Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
			  putstring("CMD: DEAL");
			  putstring_nl("");

			  s_mode = MODE_PLAY_HAND;
			  return;
		}
	    break;

	    default: break;
	}

  if (bridgeHand.m_myPlayerId == bridgeHand.m_dummyPlayerId)
  {
	  phrases.playMessage(SND_YOU_ARE_DUMMY, NEW_AUDIO);
	return;
  }

  if (s_selectedCardId == CARDID_NOT_SET || s_selectedSuitId == SUITID_NOT_SET)
  {
	  phrases.playMessage(SND_SELECT_CARD, NEW_AUDIO);
	  return;
  }

  // uint8_t msg = (13 * s_selectedSuitId) + s_selectedCardId;	// 0-51, 0: two; 1: three; ... 12: ace; repeat
  // Serial.write(msg);

  Serial.write(START_SEND_MSG);  // tell Game Controller to read to next newline
  putstring("CMD: PLAY ");
  if (bridgeHand.m_currentHandId == PLAYER_DUMMY)
	  Serial.print(bridgeHand.m_dummyPlayerId);
  else
	  Serial.print(bridgeHand.m_myPlayerId);
  putstring(" ");
  Serial.print(s_selectedCardId);
  putstring(" ");
  Serial.print(s_selectedSuitId);
  putstring_nl("");
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_announceHand (uint8_t p_playerId, uint8_t p_buttonId)
{
	if (s_mode != MODE_PLAY_HAND && p_buttonId > 3)
	{	// current suit buttons are not used for function modes
		announce_mode();
		return;
	}
	
	switch (s_mode)
	{
		case (MODE_PLAY_HAND): break;
		
		case (MODE_SET_POSITION):
		{	// suit buttons correspond to player positions (e.g., NORTH, EAST).
			s_setPosition_playerId = p_buttonId;
			phrases.playPosition(s_setPosition_playerId, NEW_AUDIO);
		    return;
		}
		break;
		
		case (MODE_ENTER_CONTRACT):
		{	
			if (s_submode == SUBMODE_ENTER_CONTRACT_WINNER)
			{
				  s_contract_winner = p_buttonId;
				  phrases.playPosition(s_contract_winner, NEW_AUDIO);
			}
			else if (s_submode == SUBMODE_ENTER_CONTRACT_SUIT)
			{
				  s_contract_suit = p_buttonId;
				  phrases.playSuit(s_contract_suit, true, NEW_AUDIO);
			}
			else
			{	// only up/down buttons are be active
				announce_mode();
			}
		    return;
		}
		break;
		
		default:
		{
			announce_mode();
			return;
		}
		break;
	}

  //-------------------------------------------
  // If current suit is set, the handler for the current suit buttons translates
  // p_buttonId based on the current suit and p_buttonId will be <= 3.
  // If this is not set, p_buttonId will be > 3.
  //-------------------------------------------
  if (p_buttonId > 3)
  {
    // if current suit is not set yet, play "no cards played yet" message
    phrases.playMessage(SND_NO_CARDS_PLAYED, NEW_AUDIO);
    return;
  }
  
  uint8_t suitId;
  if ((s_options & SUIT_HIGH_LOW) != 0)
	  suitId = 3 - p_buttonId;
  else
	  suitId = p_buttonId;

  // reset selected card if changing suit
  if (suitId != s_selectedSuitId)
  {
    s_selectedSuitId = suitId;
    s_selectedCardId = CARDID_NOT_SET;
  }

  uint16_t hand = bridgeHand.getHand(p_playerId, suitId);
  phrases.playHandSuit(p_playerId, suitId, hand, NEW_AUDIO);
}

void btn_D1() { btn_announceHand (PLAYER_DUMMY, 0); }
void btn_D2() { btn_announceHand (PLAYER_DUMMY, 1); }
void btn_D3() { btn_announceHand (PLAYER_DUMMY, 2); }
void btn_D4() { btn_announceHand (PLAYER_DUMMY, 3); }
void btn_DC() { btn_announceHand (PLAYER_DUMMY, bridgeHand.getCurrentSuitId()); }

void btn_H1() { btn_announceHand (PLAYER_ME, 0); }
void btn_H2() { btn_announceHand (PLAYER_ME, 1); }
void btn_H3() { btn_announceHand (PLAYER_ME, 2); }
void btn_H4() { btn_announceHand (PLAYER_ME, 3); }
void btn_HC() { btn_announceHand (PLAYER_ME, bridgeHand.getCurrentSuitId()); }


/** if card was played by me or my dummy, clear currently selected suit and card */
void resetKeyboard(uint8_t p_playerId)
{
  uint8_t reset = 0;
  if (p_playerId == bridgeHand.m_myPlayerId)
    reset = 1;
  else if (p_playerId == bridgeHand.m_myPartnersId && bridgeHand.m_myPartnersId == bridgeHand.m_dummyPlayerId)
    reset = 1;

  if (reset)
  {
    s_selectedSuitId = SUITID_NOT_SET;
    s_selectedCardId = CARDID_NOT_SET;
  }
}

/** Handles request from game controller to enter the keyboard's position */
void sendPosition (uint8_t p_repeat)
{
	if (p_repeat)
	{
		phrases.playMode(MODE_SET_POSITION, NEW_AUDIO);
		return;
	}
	
	s_mode = MODE_REDO;		// mode prior to MODE_SET_POSITION, since btn_function advances s_mode
	btn_function();
}


//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_function ()
{
	s_mode = s_mode + 1;
	if (s_mode >= NUMMODES) s_mode = MODE_PLAY_HAND;

	s_submode = 0;
	
	announce_mode();
}

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void announce_mode ()
{
	phrases.playMode(s_mode, NEW_AUDIO);
	
	// play submodes, if required
	switch (s_mode)
	{
		case (MODE_ENTER_CONTRACT):
		{
			phrases.playContractMode(s_submode, APPEND_AUDIO);			
		}
		break;

		case (MODE_START_NEW_HAND):
		{
			if (s_submode == SUBMODE_START_NEW_HAND_WAIT_CONFIRM)
				phrases.playMessage(SND_CONFIRM_START_NEW_HAND, APPEND_AUDIO);
		}
		break;
		
		case (MODE_UNDO):
		{
			if (s_submode == SUBMODE_UNDO_WAIT_CONFIRM)
			{
				// says "Press Play to confirm undo"
				phrases.playUndoMode(false, false, UNDO_EVENTID_UNKNOWN, 0, 0, 0, APPEND_AUDIO);
			}
		}
		break;

		case (MODE_REDO):
		{
			if (s_submode == SUBMODE_UNDO_WAIT_CONFIRM)
			{
				// says "Press Play to confirm redo"
				phrases.playUndoMode(false, true, UNDO_EVENTID_UNKNOWN, 0, 0, 0, APPEND_AUDIO);
			}
		}
		break;

		default: break;
	}
}

/*******************************************************************
     Button IDs:
     
     40    48    56    64  |  25    17    09    01
     39    47    55    63  |  26    18    10    02
     38    46    54    62  |  27    19    11    03
     37    45    53    61  |  28    20    12    04

     Buttons Bindings:

     UP   --     D1    D2  |  D3    D4    --    --
     DN          H1    H2  |  H3    H4    --    PLAY
     --   --     HC    DC  |  --    --    --    PLAY
     RPT  State  Func  --  |  --    --    --    PLAY
********************************************************************/


// -------------------------------------------------------------------------------------------------------------
// Button 64 Shield Button Handler
// Processes button presses, but ignores buttons pressed before previous button has been released.
//   NOTE: The debounce code in the scanner can cause missed button release events, if the button is released quickly
//   NOTE: Holding down a button only generates one pressed event.
//   NOTE: This procedure must set Button to zero or we will keep seeing the button press or release
// -------------------------------------------------------------------------------------------------------------
void checkButton()
{
	//if(Button > 0 && Button != m_lastButtonId)  // If Button is > 0, then it was pressed or released (SPI only), ignore repeats
	
	if (Button == 0) return;					// no button pressed or released

	if (Button <= 128)						// Released button 
    {
		Serial.write(START_SEND_MSG);
		putstring("Button: ");
		Serial.print(Button, DEC);
		putstring_nl(" - Released");
		Button = 0;
		return;
    }
    
    // pressed button
	Button = Button - 128;              // A pressd button is the button number + 128
    
    Serial.write(START_SEND_MSG);
    putstring("Button: ");
    Serial.print(Button, DEC);
    putstring_nl(" - Pressed");

    switch (Button)
    {
      case 2:	btn_play(); break;	// Play button spans three buttons
      case 3:	btn_play(); break;
      case 4:	btn_play(); break;
      case 55:	btn_H1(); break;
      case 63:	btn_H2(); break;
      case 26:	btn_H3(); break;
      case 18:	btn_H4(); break;
      case 54:	btn_HC(); break;
      case 56:	btn_D1(); break;
      case 64:	btn_D2(); break;
      case 25:	btn_D3(); break;
      case 17:	btn_D4(); break;
      case 62:	btn_DC(); break;
      case 40:	btn_up(); break;
      case 39:	btn_down(); break;
      case 37:	btn_repeat(); break;
      case 45:	btn_state(Button == m_previousButtonId); break;
      //case 28:	btn_undo(); break;
      case 53:	btn_function(); break;
      //case 61:  keyboardRestartInitiate(); break;
      default:	phrases.playNumber(SND_UNEXPECTED_BUTTON, Button, NEW_AUDIO);
    }    
    
    m_previousButtonId = Button;		// remember button to detect 2nd push (e.g., to stop state audio)
    Button = 0;						// clear current button info
}

//*******************************************************************************************************************
// Required for 64 Button Shield (SPI Only)			                           Functions & Subroutines
//*******************************************************************************************************************
//
// This void is called if the Interrupt 0 is triggered (digital pin 2).
//

void SPI64B()
{
  static unsigned long last_button_time;
  unsigned long button_time = millis();

  // ignore interrup if ISR was previously called in the last 250 milliseconds (debounce)
  // TODO: is wrap-around a problem?
  if (button_time - last_button_time < 250) return;

  last_button_time = button_time;
  Button = 0;
  
  volatile uint8_t  val  = 0;
  volatile uint8_t  clk  = 0;
  
  #define  DataInPin   3
  #define  ClkPin      4

  clk = digitalRead(ClkPin);
  while(clk == LOW)
  {
    clk = digitalRead(ClkPin);
  }

  for(volatile int i =0; i<8;i++)
  {   
    val = digitalRead(DataInPin);
    clk = digitalRead(ClkPin); 
    
    while(clk == HIGH)
    {
      clk = digitalRead(ClkPin);
    }

    if(val == HIGH)
    {
      Button = Button +1;
    }
    
    if(i != 7)
    {
      Button = Button << 1;
    }
    else
    {
      break;
    }

    clk = digitalRead(ClkPin);
    
    while(clk == LOW)
    {
      clk = digitalRead(ClkPin);
    }

  }
}    // End  of SPI64B void
// -------------------------------------------------------------------------------------------------------------

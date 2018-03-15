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

volatile uint8_t   Button    = 0;                      // Required for 64 Button Shield (SPI Only)
volatile uint8_t m_lastButtonId = 0;		       // Used to detect pressing same button twice (e.g., State)


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

void setup() {
  // set up serial port
  Serial.begin(9600);
  Serial.write(RESTARTING_MSG);	// tell Game Controller we are restarting
  putstring_nl("Keyboard 002");
  
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
    putstring_nl("No valid FAT partition!");
    sdErrorCheck();      // Something went wrong, lets print out why
    while(1);                            // then 'halt' - do nothing!
  }
  
  // Lets tell the user about what we found
  putstring("Using partition ");
  Serial.print(part, DEC);
  putstring(", type is FAT");
  Serial.println(vol.fatType(),DEC);     // FAT16 or FAT32?
  
  // Try to open the root directory
  if (!root.openRoot(vol))
  {
    putstring_nl("Can't open root dir!"); // Something went wrong,
    while(1);                             // then 'halt' - do nothing!
  }
  
  wave.setReader(root,f);
  phrases.setWave(&wave);
  bridgeHand.setPhrases(&phrases);
  
  // Whew! We got past the tough parts.
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

//#define DEBUG_KB 1

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
    case 1:	bridgeHand.setPlayer(playerId, p_repeat); eventList.addEvent(p_input0, p_input1, p_repeat); break;
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
    case 3:	bridgeHand.clearDummy(p_repeat); eventList.addEvent(p_input0, p_repeat); break;
    case 4:	phrases.playMessage(SND_IN_HAND, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 5:	phrases.playMessage(SND_ON_BOARD, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 6:	phrases.playMessage(SND_HAND_IS_COMPLETE, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 7:	phrases.playMessage(SND_ALREADY_PLAYED, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
//TODO:    case 8:	phrases.playMessage(SND_CONFIRM_UNDO, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
    case 9:	eventList.resetEventList(p_repeat); bridgeHand.newGame(); break;
    case 10:	eventList.resetEventList(p_repeat); bridgeHand.newHand(); break;
    case 11:	eventList.resetEventList(false); keyboardRestartInitiate(); break;
    case 12:	keyboardRestartComplete(); eventList.addEvent(p_input0, p_repeat); break;
    case 13:	phrases.playMessage(SND_ENTER_CONTRACT, NEW_AUDIO); eventList.addEvent(p_input0, p_repeat); break;
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
  putstring_nl("Sending resetart");

  phrases.playMessage(SND_RESET_STARTED, NEW_AUDIO); 
  phrases.setSilentMode(true);

  Serial.write(INITIATE_RESET_MSG);	// tell Game Controller to resend the state
}

void keyboardRestartComplete()
{
  phrases.setSilentMode(false);
  phrases.playMessage(SND_RESET_FINISHED, NEW_AUDIO);
}


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
    case 14:	btn_state(buttonId == m_lastButtonId); break;
    case 15:	btn_undo(); break;
    case 16:	btn_masterUndo(); break;
    default:
      phrases.playNumber(SND_UNEXPECTED_BUTTON, buttonId, NEW_AUDIO);
  }    
  m_lastButtonId = buttonId;
}

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
    processInput2(input0, input1, 0);
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

byte check_switches()
{
  static byte previous[6];
  static long time[6];
  byte reading;
  byte pressed;
  byte index;
  pressed = 0;

  for (byte index = 0; index < 6; ++index) {
    reading = digitalRead(14 + index);
    if (reading == LOW && previous[index] == HIGH && millis() - time[index] > DEBOUNCE)
    {
      // switch pressed
      time[index] = millis();
      pressed = index + 1;
      break;
    }
    previous[index] = reading;
  }
  // return switch number (1 - 6)
  return (pressed);
}


//----------------------------------------------------------------------
//----------------------------------------------------------------------
//----------------------------------------------------------------------
/*
NOTE: add TRICK.WAV ("trick" singular)
NOTE: change "bid" to "contract" BIDIS.WAV, AGAINBID.WAV ENTERBID.WAV
NOTE: change DUMMY.WAV to DUMMYS.WAV ("dummy" -> "dummy's")
NOTE: fix VOID.WAV, NONE.WAVE, NOTRUMP.WAV
----------------------------------------------------------------------
*/


//----------------------------------------------------------------------
//----------------------------------------------------------------------
//----------------------------------------------------------------------

/*
     Buttons:

     *UP   *D1  *D2  *D3  *D4
     *DN   *H1  *H2  *H3  *H4  PLAY
     Repeat  *HC  *DC
     State

     UNDO
     MASTER_UNDO
 */


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
  if (p_isSecondPress &&  wave.isplaying)
    wave.stop();
  else
    bridgeHand.playState();
}  

#define UNDO_FLAG 0b10000000
#define UNDO_MSG 0b01000000
uint8_t s_cardPlayedMsg = NO_CARD_PLAYED;

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_undo()
{
  if (bridgeHand.m_myPlayerId == bridgeHand.m_dummyPlayerId)
  {
	phrases.playMessage(SND_YOU_ARE_DUMMY, NEW_AUDIO);
	return;
  }

  if (s_cardPlayedMsg == NO_CARD_PLAYED)
  {
	phrases.playMessage(SND_NOT_PLAYED, NEW_AUDIO);
	return;
  }

  uint8_t suitId = bridgeHand.cardSuit(s_cardPlayedMsg);
  uint8_t cardId = bridgeHand.cardNumber(s_cardPlayedMsg);

  if ((s_cardPlayedMsg & UNDO_FLAG) != 0)
  {
	phrases.playConfirmPickupCard(cardId, suitId, NEW_AUDIO);
	s_cardPlayedMsg &= ~UNDO_FLAG; // note undo has been pressed once
	return;
  }

  // send undo
  uint8_t msg = (13 * suitId) + cardId;	// 0-51, 0: two; 1: three; ... 12: ace; repeat
  Serial.write(UNDO_MSG | msg);

  s_cardPlayedMsg = NO_CARD_PLAYED;
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_masterUndo()
{
  //TODO: not yet implemented
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_play()
{
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

  uint8_t msg = (13 * s_selectedSuitId) + s_selectedCardId;	// 0-51, 0: two; 1: three; ... 12: ace; repeat
  Serial.write(msg);
  s_cardPlayedMsg = UNDO_FLAG | bridgeHand.encodeCard(s_selectedCardId, s_selectedSuitId);	// leading 1 indicates undo has not been pressed yet
}  

//----------------------------------------------------------------------
//----------------------------------------------------------------------
void btn_announceHand (uint8_t p_playerId, uint8_t p_buttonId)
{
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
  phrases.playSuit(p_playerId, suitId, hand, NEW_AUDIO);
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


// -------------------------------------------------------------------------------------------------------------
// Button 64 Shield Code
// -------------------------------------------------------------------------------------------------------------
void checkButton()
{
  if(Button > 0 && Button != m_lastButtonId)  // If Button is > 0, then it was pressed or released (SPI only), ignore repeats
  {
    Serial.write(START_SEND_MSG);
    putstring("Button: ");
    if(Button > 128)                          // Example of how to decode the button press
    {
      Button = Button - 128;                    // A pressd button is the button number + 128
      Serial.print(Button, DEC);
      putstring_nl(" - Pressed");
    }
    else
    {
      Serial.print(Button, DEC);                // A released button is from 1 to 64
      putstring_nl(" - Released");
      m_lastButtonId = 0;
      Button = 0;
    }

    switch (Button)
    {
      case 1:	btn_play(); break;
      case 47:	btn_H1(); break;
      case 55:	btn_H2(); break;
      case 63:	btn_H3(); break;
      case 26:	btn_H4(); break;
      case 46:	btn_HC(); break;
      case 48:	btn_D1(); break;
      case 56:	btn_D2(); break;
      case 64:	btn_D3(); break;
      case 25:	btn_D4(); break;
      case 54:	btn_DC(); break;
      case 40:	btn_up(); break;
      case 39:	btn_down(); break;
      case 37:	btn_repeat(); break;
      case 45:	btn_state(Button == m_lastButtonId); break;
      case 4:	btn_undo(); break;
      case 12:	btn_masterUndo(); break;
      case 61:  keyboardRestartInitiate(); break;
      default:
	phrases.playNumber(SND_UNEXPECTED_BUTTON, Button, NEW_AUDIO);
    }    
    
    m_lastButtonId = Button;
    Button = 0;
  }
  
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

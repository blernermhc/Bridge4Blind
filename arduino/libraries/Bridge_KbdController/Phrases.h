#ifndef Phrases_h
#define Phrases_h

#ifndef F_CPU
#include <avr/iom328p.h> /* rick@cs: NOTE: added for use in eclipse, so we see board-specific defines; REMOVE for use */
#define F_CPU 16000000L /* rick@cs: NOTE: added for use in eclipse, ... */
#endif // F_CPU

#include <stdint.h>
#include <WaveHCL.h>
#include <avr/pgmspace.h>
/**
 * \file
 * Phrases class
 */

// ------------------------------------------------
// put file names in FLASH memory, rather than SRAM
// ------------------------------------------------

#define FILE_NAME_LEN   9 // 8 + <null> (".WAV" is implied)
#define NEW_AUDIO 0
#define APPEND_AUDIO 1

const char SND_VOID[]   PROGMEM = "VOID";
const char SND_ZERO[]   PROGMEM = "ZERO";
const char SND_NO[]   PROGMEM = "NO";
const char SND_ONE[]    PROGMEM = "ONE";
const char SND_TWO[]    PROGMEM = "TWO";
const char SND_THREE[]    PROGMEM = "THREE";
const char SND_FOUR[]   PROGMEM = "FOUR";
const char SND_FIVE[]   PROGMEM = "FIVE";
const char SND_SIX[]    PROGMEM = "SIX";
const char SND_SEVEN[]    PROGMEM = "SEVEN";
const char SND_EIGHT[]    PROGMEM = "EIGHT";
const char SND_NINE[]   PROGMEM = "NINE";
const char SND_TEN[]    PROGMEM = "TEN";
const char SND_ELEVEN[]   PROGMEM = "ELEVEN";
const char SND_TWELVE[]   PROGMEM = "TWELVE";
const char SND_THIRTEEN[] PROGMEM = "THIRTEEN";

const char* const SND_NUMBERS[] PROGMEM = { SND_ZERO
              , SND_ONE
              , SND_TWO
              , SND_THREE
              , SND_FOUR
              , SND_FIVE
              , SND_SIX
              , SND_SEVEN
              , SND_EIGHT
              , SND_NINE
              , SND_TEN
              , SND_ELEVEN
              , SND_TWELVE
              , SND_THIRTEEN
              };


const char SND_JACK[]   PROGMEM = "JACK";
const char SND_QUEEN[]  PROGMEM = "QUEEN";
const char SND_KING[]   PROGMEM = "KING";
const char SND_ACE[]    PROGMEM = "ACE";

#define NUMCARDS 14
const char* const SND_CARDS[] PROGMEM = { SND_TWO
              , SND_THREE
              , SND_FOUR
              , SND_FIVE
              , SND_SIX
              , SND_SEVEN
              , SND_EIGHT
              , SND_NINE
              , SND_TEN
              , SND_JACK
              , SND_QUEEN
              , SND_KING
              , SND_ACE
              , SND_VOID
              };


const char SND_CLUB[]   PROGMEM = "CLUB";
const char SND_DIAMOND[]  PROGMEM = "DIAMOND";
const char SND_HEART[]    PROGMEM = "HEART";
const char SND_SPADE[]    PROGMEM = "SPADE";
const char SND_NOTRUMP[]  PROGMEM = "NOTRUMP";

const char SND_CLUBS[]    PROGMEM = "CLUBS";
const char SND_DIAMONDS[] PROGMEM = "DIAMONDS";
const char SND_HEARTS[]   PROGMEM = "HEARTS";
const char SND_SPADES[]   PROGMEM = "SPADES";

#define NUMSUITS 5
const char* const SND_SUIT[]  PROGMEM = { SND_CLUB
              , SND_DIAMOND
              , SND_HEART
              , SND_SPADE
              , SND_NOTRUMP
              };

const char* const SND_SUITS[] PROGMEM = { SND_CLUBS
              , SND_DIAMONDS
              , SND_HEARTS
              , SND_SPADES
              , SND_NOTRUMP
              };


const char SND_NORTH[]    PROGMEM = "NORTH";
const char SND_EAST[]   PROGMEM = "EAST";
const char SND_SOUTH[]    PROGMEM = "SOUTH";
const char SND_WEST[]   PROGMEM = "WEST";
const char SND_YOU[]   PROGMEM = "YOU";
const char SND_YOUR[]   PROGMEM = "YOUR";
const char SND_DUMMY[]   PROGMEM = "DUMMY";
const char SND_DUMMYS[]   PROGMEM = "DUMMYS";
const char SND_WE[]   PROGMEM = "WE";
const char SND_THEY[]   PROGMEM = "THEY";

#define NUMPLAYERS 6
const char* const SND_PLAYERS[] PROGMEM = { SND_NORTH
              , SND_EAST
              , SND_SOUTH
              , SND_WEST
              , SND_YOU
              , SND_DUMMY
              };

const char SND_TRICK[]    PROGMEM = "TRICK";
const char SND_TRICKS[]   PROGMEM = "TRICKS";

const char SND_DUMMY_HAS[]  PROGMEM = "DUMMYHAS";
const char SND_YOU_HAVE[] PROGMEM = "YOUHAVE";
const char SND_HAS_TAKEN[]  PROGMEM = "HASTAKEN";
const char SND_PLAYS_NEXT[] PROGMEM = "PLAYSNXT";
const char SND_WAITING_FOR[]  PROGMEM = "WAITFOR";
const char SND_TO_PLAY[]  PROGMEM = "TOPLAY";

const char SND_SELECT_CARD[]    PROGMEM = "SELCARD";
const char SND_NOT_PLAYED[]   PROGMEM = "NOTPLAY";
const char SND_ALREADY_PLAYED[]   PROGMEM = "ALREADY";
const char SND_AGAIN_PICK_UP_DUMMY[]  PROGMEM = "AGAINDUM";
const char SND_AGAIN_CLEAR_CONTRACT[]  PROGMEM = "AGAINCON";
const char SND_RESET_STARTED[]    PROGMEM = "RSTSTART";
const char SND_RESET_FINISHED[]   PROGMEM = "RSTDONE";
const char SND_SCAN_HAND[]    PROGMEM = "SCANHAND";
const char SND_SCAN_DUMMY[]   PROGMEM = "SCANDUMY";
const char SND_PICKUP_DUMMY[]   PROGMEM = "DMPKUP";
const char SND_ENTER_CONTRACT[]    PROGMEM = "ENTERCON";
const char SND_PRESS_AGAIN_TO_PLACE[] PROGMEM = "AGAIN";
const char SND_BACK_IN_HAND[]   PROGMEM = "BACKINHD";
const char SND_CANNOT_PLAY[]    PROGMEM = "CANTPLAY";
const char SND_PLAY_IS[]    PROGMEM = "PLAYIS";
const char SND_PRESS_AGAIN_FOR[]  PROGMEM = "PRAGAIN";
const char SND_TO_PICK_UP[]   PROGMEM = "TOPICKUP";
const char SND_CONTRACT_IS[]    PROGMEM = "CONTRACT";
const char SND_PLAYED[]     PROGMEM = "PLAYED";
const char SND_PICKED_UP[]    PROGMEM = "PKDUP";
const char SND_HAND_IS_COMPLETE[] PROGMEM = "HANDDONE";
const char SND_UNEXPECTED_BUTTON[] PROGMEM = "UNXBTN";
const char SND_UNEXPECTED_OP_1[] PROGMEM = "UNXOP1";
const char SND_UNEXPECTED_OP_2[] PROGMEM = "UNXOP2";
const char SND_YOU_ARE_PLAYING[] PROGMEM = "YOUPLAY";
const char SND_IS_DUMMY[] PROGMEM = "ISDUMMY";
const char SND_TOOK_TRICK[] PROGMEM = "TOOKTRK";
const char SND_HAVE_TAKEN[] PROGMEM = "HAVETAKE";
const char SND_IN_HAND[] PROGMEM = "INHAND";
const char SND_ON_BOARD[] PROGMEM = "ONBOARD";
const char SND_INCOMPLETE_MSG[] PROGMEM = "ERRBADMG";
const char SND_NO_CARDS_PLAYED[] PROGMEM = "NONEPLAY";
const char SND_YOU_ARE_DUMMY[] PROGMEM = "YOUDUMMY";
const char SND_SELECT_SUIT[] PROGMEM = "SELSUIT";
const char SND_SILENCE[] PROGMEM = "SILENCE";
const char SND_CARDWORD[] PROGMEM = "CARD";
const char SND_SUITWORD[] PROGMEM = "SUIT";
const char SND_PLAYERWORD[] PROGMEM = "PLAYER";
const char SND_IS_NOT_SET[] PROGMEM = "ISNOTSET";
const char SND_MY_POSITION[] PROGMEM = "MYPOS";
const char SND_CARD_ALREADY_PLAYED[] PROGMEM = "CARDPLYD";
const char SND_CARD_NOT_IN_HAND[] PROGMEM = "NOTINHND";

//------------------------------------------------------------------------------
/**
 * \class Phrases
 * \brief A bridge player's hand.
 *
 * Represents the state a keyboard controller managed for a hand.
 *
 */
class Phrases
{
 public:
	uint8_t s_silent = 0;
	WaveHCL *m_wave;
  
	Phrases(void);

	void setWave(WaveHCL *wave);
	void setSilentMode(uint8_t p_mode);
	uint8_t isSilentMode();

	void pNumber (uint8_t p_number);
	void pCard (uint8_t p_cardId);
	void pSuit (uint8_t p_cardId, uint8_t p_singular);
	void pPlayer (uint8_t p_cardId);

	void playNewAudio ();
	void playNext ();
	void playPause ();
	void playNumber (PGM_P p_input, uint8_t p_opId, uint8_t p_appendAudio);
	void playSuit (uint8_t p_playerId, uint8_t p_suitId, uint16_t p_handBitmap, uint8_t p_appendAudio);
	void playTricks (uint8_t p_playerId1, uint8_t p_playerId2, uint8_t p_numTricks, uint8_t p_appendAudio);
	void playNextPlayer (uint8_t p_playerId, uint8_t p_appendAudio);
	void playMessage (PGM_P p_msg, uint8_t p_appendAudio);
	void playImportantMessage (PGM_P p_msg, uint8_t p_appendAudio);
	void playWaitPlayer (uint8_t p_nextPlayerId, uint8_t p_appendAudio);
	void playMyPosition (uint8_t p_playerId, uint8_t p_appendAudio);
	void playDummyPosition (uint8_t p_playerId, uint8_t p_appendAudio);
	void playCurrentSuit (uint8_t p_suitId, uint8_t p_appendAudio);
	void playConfirmPickupCard (uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio);
	void playSelectedCard (uint8_t p_playerId, uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio);
	void playBadCard (uint8_t p_suitId, uint8_t p_appendAudio);
	void playConfirmPickupCardOther (uint8_t p_playerId, uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio);
	void playContract (uint8_t p_playerId, uint8_t p_suitId, uint8_t p_numTricks, uint8_t p_appendAudio);
	void playCardPlayed (uint8_t p_playerId, PGM_P p_action, uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio);
	void playHandComplete (uint8_t p_playerId, uint8_t p_appendAudio);
	void playTrickTaken(uint8_t p_playerId, uint8_t p_appendAudio);
	void playTricksWon(PGM_P p_players, uint8_t p_numTricks, uint8_t p_appendAudio);
};

#endif //Phrases_h

#ifndef BridgeHand_h
#define BridgeHand_h
#include <Phrases.h>
/**
 * \file
 * BridgeHand class
 */

#define PLAYER_ME 0
#define PLAYER_DUMMY 1

#define PLAYERID_NOT_SET 16
#define SUITID_NOT_SET 16
#define CARDID_NOT_SET 16
#define NO_CARD_PLAYED 0b11111111

//------------------------------------------------------------------------------
/**
 * \class BridgeHand
 * \brief A bridge player's hand.
 *
 * Represents the state a keyboard controller managed for a hand.
 *
 */
class BridgeHand
{
 public:
	uint8_t m_myPlayerId = PLAYERID_NOT_SET;
	uint8_t m_myPartnersId = PLAYERID_NOT_SET;
	uint8_t m_dummyPlayerId = PLAYERID_NOT_SET;
	uint8_t m_nextPlayerId = PLAYERID_NOT_SET;
	uint8_t m_contractPlayerId = PLAYERID_NOT_SET;
	uint8_t m_contractSuitId = SUITID_NOT_SET;
	uint8_t m_contractNumTricks = 0;
	uint8_t m_ourTricks = 0;
	uint8_t m_theirTricks = 0;

	uint16_t m_hands[2][4];

	uint8_t m_firstPlayerId = PLAYERID_NOT_SET;
	uint8_t m_currentSuitId = SUITID_NOT_SET;
	uint8_t m_currentHandId = PLAYER_ME;		// 0: My hand; 1: Dummy Hand
	uint8_t m_played[4];
  
	BridgeHand(void);
	void setPhrases (Phrases *p_phrases);
	void reset();
	void setPlayer(uint8_t p_playerId, uint8_t p_repeat);
	void setDummy(uint8_t p_playerId, uint8_t p_repeat);
	void setNextPlayer(uint8_t p_playerId, uint8_t p_repeat);
	void setContract(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_numTricks, uint8_t p_repeat);
	void addCardToHand(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat);
	uint8_t adjustPlayerId (uint8_t p_playerId);
	void useCard(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat);
	void unuseCard(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat);
	void clearDummy(uint8_t p_repeat);
	void cannotPlay(uint8_t p_suitId, uint8_t p_repeat);
	void trickFinished(uint8_t p_playerId, uint8_t p_repeat);
	void newGame();
	void newHand();
	void playState();
	uint16_t getHand(uint8_t playerId, uint8_t p_suitId);
	uint8_t getCurrentSuitId ();
	uint8_t getCurrentHandId ();

	uint8_t encodeCard (uint8_t p_cardId, uint8_t p_suitId);
	uint8_t cardNumber (uint8_t p_card);
	uint8_t cardSuit (uint8_t p_card);

};

#endif //BridgeHand_h

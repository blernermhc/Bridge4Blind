#include <BridgeHand.h>
#include <Phrases.h>
#include <WaveUtil.h>
#include <BridgeUtils.h>

Phrases *m_phrases;

/** create an instance of BridgeHand. */
BridgeHand::BridgeHand(void)
{
  reset();
}

void BridgeHand::setPhrases (Phrases *p_phrases)
{
  m_phrases = p_phrases;
}

/** Clears the current state */
void BridgeHand::reset()
{
  m_myPlayerId = PLAYERID_NOT_SET;
  m_myPartnersId = PLAYERID_NOT_SET;
  m_dummyPlayerId = PLAYERID_NOT_SET;
  m_nextPlayerId = PLAYERID_NOT_SET;
  m_contractPlayerId = PLAYERID_NOT_SET;
  m_contractSuitId = SUITID_NOT_SET;
  m_contractNumTricks = 0;
  m_ourTricks = 0;
  m_theirTricks = 0;
  m_firstPlayerId = PLAYERID_NOT_SET;
  m_currentSuitId = SUITID_NOT_SET;
  m_currentHandId = PLAYER_ME;

  for (uint8_t player = 0; player < 2; ++player)
    for (uint8_t suit = 0; suit < 4; ++suit)
      m_hands[player][suit] = 0;

  for (uint8_t player = 0; player < 4; ++player)
    m_played[player] = NO_CARD_PLAYED;
}

void BridgeHand::setPlayer(uint8_t p_playerId, uint8_t p_repeat)
{
  if (! p_repeat)
  {
    m_myPlayerId = p_playerId;
    m_myPartnersId = m_myPlayerId + 2;
    if (m_myPartnersId > 3) m_myPartnersId -= 4;
  }
  m_phrases->playMyPosition(p_playerId, NEW_AUDIO);
}

void BridgeHand::setDummy(uint8_t p_playerId, uint8_t p_repeat)
{
  if (p_repeat) return;
  m_dummyPlayerId = p_playerId;
  m_phrases->playDummyPosition(p_playerId, NEW_AUDIO);
}

void BridgeHand::setNextPlayer(uint8_t p_playerId, uint8_t p_repeat)
{
	if (!p_repeat)
	{
		m_nextPlayerId = p_playerId;
		if (m_firstPlayerId == PLAYERID_NOT_SET)
		{
			m_firstPlayerId = m_nextPlayerId;
			m_currentSuitId = SUITID_NOT_SET;
		}

		// if my partner is the dummy, switch m_currentHandId between my hand and dummy hand
		if (m_dummyPlayerId == m_myPartnersId)
		{
			if (p_playerId == m_myPartnersId)
				m_currentHandId = PLAYER_DUMMY;
			else if (p_playerId == m_myPlayerId)
				m_currentHandId = PLAYER_ME;
			else
			{
				uint8_t playerAfterMe = m_myPlayerId + 1;
				if (playerAfterMe > 3)
					playerAfterMe = 0;
				if (p_playerId == playerAfterMe)
					m_currentHandId = PLAYER_DUMMY;
				else
					m_currentHandId = PLAYER_ME;
			}
		}
	}

	if (p_playerId == m_myPlayerId)
	{
		m_phrases->playMessage(SND_IN_HAND, NEW_AUDIO);
	}
	else if (m_dummyPlayerId == m_myPartnersId && p_playerId == m_dummyPlayerId)
	{
		m_phrases->playMessage(SND_ON_BOARD, NEW_AUDIO);
	}

}

void BridgeHand::setContract(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_numTricks, uint8_t p_repeat)
{
  if (! p_repeat)
  {
    m_contractPlayerId = p_playerId;
    m_contractSuitId = p_suitId;
    m_contractNumTricks = p_numTricks;
  }
  m_phrases->playContract(p_playerId, p_suitId, p_numTricks, NEW_AUDIO);
}

void BridgeHand::addCardToHand(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat)
{
  if (! p_repeat)
  {
    uint16_t cardBit = 1 << p_cardId;
    uint16_t playerIdx = (p_playerId == m_myPlayerId ? 0 : 1);

    m_hands[playerIdx][p_suitId] |= cardBit;
  }

  m_phrases->playSelectedCard(2, p_cardId, p_suitId, NEW_AUDIO);	// 2: play only card, with no prefix
}

/** change NORHT, SOUTH, etc. to You or Dummy, if appropriate */
uint8_t BridgeHand::adjustPlayerId (uint8_t p_playerId)
{
	// if you are the dummy, say "Dummy played" rather than "You played"
	if (p_playerId == m_dummyPlayerId) p_playerId = 5;
	else if (p_playerId == m_myPlayerId) p_playerId = 4;
    return p_playerId;
}

void BridgeHand::useCard(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat)
{
  if (! p_repeat)
  {
    uint16_t cardBit = 1 << p_cardId;

    if (p_playerId == m_firstPlayerId) m_currentSuitId = p_suitId;
    m_played[p_playerId] = encodeCard(p_cardId, p_suitId);
    if (p_playerId == m_myPlayerId || p_playerId == m_dummyPlayerId)
    {
      uint16_t playerIdx = (p_playerId == m_myPlayerId ? 0 : 1);
      m_hands[playerIdx][p_suitId] &= ~cardBit;
    }
  }

  m_phrases->playCardPlayed(adjustPlayerId(p_playerId), SND_PLAYED, p_cardId, p_suitId, NEW_AUDIO);
}

/** reverse of useCard (triggered by an undo) typically after a setNextPlayer */
void BridgeHand::unuseCard(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat)
{
  if (! p_repeat)
  {
    uint16_t cardBit = 1 << p_cardId;

    m_played[p_playerId] = NO_CARD_PLAYED;
    if (p_playerId == m_myPlayerId || p_playerId == m_dummyPlayerId)
    {
      uint16_t playerIdx = (p_playerId == m_myPlayerId ? 0 : 1);
      m_hands[playerIdx][p_suitId] |= cardBit;
    }
  }

  m_phrases->playCardPlayed(adjustPlayerId(p_playerId), SND_PICKED_UP, p_cardId, p_suitId, NEW_AUDIO);
}

void BridgeHand::cannotPlay(uint8_t p_suitId, uint8_t p_repeat)
{
  m_phrases->playBadCard(p_suitId, NEW_AUDIO);
}

void BridgeHand::handComplete(uint8_t p_repeat)
{
	m_phrases->playNewAudio();

	m_phrases->playMessage(SND_HAND_IS_COMPLETE, APPEND_AUDIO);
	m_phrases->playPause();

	m_phrases->playTricksWon(SND_WE, m_ourTricks, APPEND_AUDIO);
	m_phrases->playPause();

	m_phrases->playTricksWon(SND_THEY, m_theirTricks, APPEND_AUDIO);
	m_phrases->playPause();

	if (m_contractPlayerId != PLAYERID_NOT_SET)
	{
		m_phrases->playContract(m_contractPlayerId, m_contractSuitId, m_contractNumTricks, APPEND_AUDIO);
		m_phrases->playPause();
	}

	m_phrases->playNext();
}

void BridgeHand::trickFinished(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_repeat)
{
  if (! p_repeat)
  {
    m_firstPlayerId = PLAYERID_NOT_SET;
    m_currentSuitId = SUITID_NOT_SET;
  }
  m_phrases->playTrickTaken(adjustPlayerId(p_playerId), p_suitId, p_cardId, NEW_AUDIO);
}

void BridgeHand::tricksTaken(uint8_t p_nsTricks, uint8_t p_ewTricks)
{
	if (m_myPlayerId == 0 || m_myPartnersId == 0)
	{
		m_ourTricks = p_nsTricks;
		m_theirTricks = p_ewTricks;
	}
    else
    {
		m_ourTricks = p_ewTricks;
		m_theirTricks = p_nsTricks;
    }
}

void BridgeHand::newGame()
{
  reset();
}

void BridgeHand::newHand()
{
  reset();
}

uint8_t BridgeHand::encodeCard (uint8_t p_cardId, uint8_t p_suitId) { return (p_suitId << 4) | p_cardId; }
uint8_t BridgeHand::cardNumber (uint8_t p_card) { return (p_card & 0b1111); }
uint8_t BridgeHand::cardSuit (uint8_t p_card) { return ((p_card >> 4) & 0b11); }

void BridgeHand::playState()
{
  m_phrases->playNewAudio();

  if (m_firstPlayerId != PLAYERID_NOT_SET)
  {
    uint8_t idx = m_firstPlayerId;
    while (idx != m_nextPlayerId && m_played[idx] != NO_CARD_PLAYED)
    {
      m_phrases->playCardPlayed(adjustPlayerId(idx), SND_PLAYED, cardNumber(m_played[idx]), cardSuit(m_played[idx]), APPEND_AUDIO);
      m_phrases->playPause();
      ++idx;
      if (idx > 3) idx = 0;
    }
  }
  if (m_nextPlayerId != PLAYERID_NOT_SET)
  {
	  m_phrases->playWaitPlayer(adjustPlayerId(m_nextPlayerId), APPEND_AUDIO);
      m_phrases->playPause();
  }

  if (m_currentSuitId != SUITID_NOT_SET)
  {
	  m_phrases->playCurrentSuit(m_currentSuitId, APPEND_AUDIO);
	  m_phrases->playPause();
  }

  if (m_contractPlayerId != PLAYERID_NOT_SET)
  {
	  m_phrases->playContract(m_contractPlayerId, m_contractSuitId, m_contractNumTricks, APPEND_AUDIO);
      m_phrases->playPause();
  }

  m_phrases->playTricksWon(SND_WE, m_ourTricks, APPEND_AUDIO);
  m_phrases->playPause();
  m_phrases->playTricksWon(SND_THEY, m_theirTricks, APPEND_AUDIO);
  m_phrases->playPause();
  m_phrases->playMyPosition(m_myPlayerId, APPEND_AUDIO);
  m_phrases->playPause();
  m_phrases->playDummyPosition(m_dummyPlayerId, APPEND_AUDIO);

  m_phrases->playNext();
}

uint16_t BridgeHand::getHand(uint8_t p_playerId, uint8_t p_suitId)
{
  return m_hands[p_playerId][p_suitId];
}

uint8_t BridgeHand::getCurrentSuitId()
{
	return m_currentSuitId;
}


uint8_t BridgeHand::getCurrentHandId()
{
	return m_currentHandId;
}

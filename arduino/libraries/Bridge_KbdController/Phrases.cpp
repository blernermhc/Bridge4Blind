#include <Phrases.h>
#include <BridgeUtils.h>

/** create an instance of Phrases. */
Phrases::Phrases(void)
{
	m_wave = 0;
}

/** sets silent mode if p_mode is non-zero; resets silent mode if p_mode is zero. */
void Phrases::setSilentMode(uint8_t p_mode)
{
  s_silent = p_mode;
}

void Phrases::setWave(WaveHCL *p_wave)
{
	m_wave = p_wave;
}

uint8_t Phrases::isSilentMode()
{
  return s_silent;
}

void Phrases::playNewAudio ()
{
  m_wave->clearSequence();
}

void Phrases::playNext ()
{
  m_wave->playNext();
}

void Phrases::playPause ()
{
  playMessage(SND_SILENCE, APPEND_AUDIO);
}


//----------------------------------------------------------------------
// playSequence: plays a sequence of wave files
//----------------------------------------------------------------------

void Phrases::playNumber (PGM_P p_message, uint8_t p_opId, uint8_t p_appendAudio)
{
  if (! p_appendAudio) m_wave->clearSequence();

  if (p_message != 0)
  {
	  m_wave->addSequence(p_message);
  }
  pNumber(p_opId);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// Brian Kernighan's bit count algorithm
//----------------------------------------------------------------------
int countOnes (uint16_t p_bitmap)
{
  int count = 0;
  for (; p_bitmap; ++count)
  {
    p_bitmap &= (p_bitmap - 1);  // clear the least significant bit set
  }
  return count;
}

//----------------------------------------------------------------------
// Inner methods which perform bounds checking on the array indices
// These always append and do not make the silent check
//----------------------------------------------------------------------
void Phrases::pNumber (uint8_t p_number)
{
	  if (p_number < 14)
	    m_wave->addSequence((char *)pgm_read_word(&(SND_NUMBERS[p_number])));
	  else
	  {
	    uint8_t d0 = p_number % 10; p_number /= 10;
	    uint8_t d1 = 0;
	    if (p_number > 0)
	    {
	      d1 = p_number % 10;
	      p_number /= 10;
	    }
	    uint8_t d2 = 0;
	    if (p_number > 0)
	    {
	      d2 = p_number % 10;
	      p_number /= 10;
	    }

	    if (d2 > 0) m_wave->addSequence((char *)pgm_read_word(&(SND_NUMBERS[d2])));
	    if (d2 > 0 && d1 == 0) m_wave->addSequence(SND_ZERO);
	    else if (d1 > 0) m_wave->addSequence((char *)pgm_read_word(&(SND_NUMBERS[d1])));
	    if ((d2 > 0 || d1 > 0) && d0 == 0) m_wave->addSequence(SND_ZERO);
	    else if (d0 > 0) m_wave->addSequence((char *)pgm_read_word(&(SND_NUMBERS[d0])));
	  }
}

void Phrases::pSuit (uint8_t p_suitId, uint8_t p_singular)
{
	if (p_suitId >= NUMSUITS)
	{
		playNumber(SND_SUITWORD, p_suitId, APPEND_AUDIO);
	}
	else
	{
		m_wave->addSequence(p_singular
				? (char *)pgm_read_word(&(SND_SUIT[p_suitId]))
				: (char *)pgm_read_word(&(SND_SUITS[p_suitId])));
	}
}

void Phrases::pCard (uint8_t p_cardId)
{
	if (p_cardId >= NUMCARDS)
	{
		playNumber(SND_CARDWORD, p_cardId, APPEND_AUDIO);
	}
	else
	{
		m_wave->addSequence((char *)pgm_read_word(&(SND_CARDS[p_cardId])));
	}
}

void Phrases::pPlayer (uint8_t p_playerId)
{
	if (p_playerId >= NUMPLAYERS)
	{
		playNumber(SND_PLAYERWORD, p_playerId, APPEND_AUDIO);
	}
	else
	{
		m_wave->addSequence((char *)pgm_read_word(&(SND_PLAYERS[p_playerId])));
	}
}

//----------------------------------------------------------------------
// 1  You have / Dummy has 2 Spades, King, Ten
//----------------------------------------------------------------------

void Phrases::playHandSuit (uint8_t p_playerId, uint8_t p_suitId, uint16_t p_handBitmap, uint8_t p_appendAudio)
{
	  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  int numCards = countOnes(p_handBitmap);
 
  m_wave->addSequence(p_playerId == 0 ? SND_YOU_HAVE : SND_DUMMY_HAS);
  if (numCards == 0)
	  m_wave->addSequence(SND_NO);
  else
	  pNumber(numCards);
  pSuit(p_suitId, numCards == 1);
  for (int bit = 12; bit >= 0; --bit)
  {
    if (((1<<bit) & p_handBitmap) != 0)
    {
    	  pCard(bit);
    }
  }

  if (! p_appendAudio) m_wave->playNext();
}


//----------------------------------------------------------------------
// 2  East West has taken Three Tricks
//----------------------------------------------------------------------

void Phrases::playTricks (uint8_t p_playerId1, uint8_t p_playerId2, uint8_t p_numTricks, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  pPlayer(p_playerId1);
  pPlayer(p_playerId2);
  m_wave->addSequence(SND_HAS_TAKEN);
  pNumber(p_numTricks);
  m_wave->addSequence(p_numTricks == 1 ? SND_TRICK : SND_TRICKS);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// 3  East plays next
//----------------------------------------------------------------------

void Phrases::playNextPlayer (uint8_t p_playerId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  pPlayer(p_playerId);
  m_wave->addSequence(SND_PLAYS_NEXT);

  if (! p_appendAudio) m_wave->playNext();
}


//----------------------------------------------------------------------
// 4  Select a card to play
// 7  You have not played a card
// 11 Another player has already played a card
// 13 Press again to pick up Dummy’s hand
// 14 Press again to clear bid
// 22 Resetting State
// 23 State Reset Complete
// 24 Scan your hand
// 24 Scan Dummy’s hand
// 26 Dummy picked up hand
// 27 Enter bid
//---------------------------------------------------------------------

// message without arguments (NOTE: argument must be a const char* in PROGMEM space)
void Phrases::playMessage (PGM_P p_msg, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(p_msg);

  if (! p_appendAudio) m_wave->playNext();
}

// like playMessage, but ignores s_silent flag
void Phrases::playImportantMessage (PGM_P p_msg, uint8_t p_appendAudio)
{
  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(p_msg);

  if (! p_appendAudio) m_wave->playNext();
}


//----------------------------------------------------------------------
// 5  Waiting for West to play
//----------------------------------------------------------------------

void Phrases::playWaitPlayer (uint8_t p_nextPlayerId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(SND_WAITING_FOR);
  pPlayer(p_nextPlayerId);
  m_wave->addSequence(SND_TO_PLAY);

  if (! p_appendAudio) m_wave->playNext();
}


//----------------------------------------------------------------------
// You are playing ...
//----------------------------------------------------------------------

void Phrases::playMyPosition (uint8_t p_playerId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  if (p_playerId >= NUMPLAYERS)
  {
	  m_wave->addSequence(SND_MY_POSITION);
	  m_wave->addSequence(SND_IS_NOT_SET);
  }
  else
  {
	  m_wave->addSequence(SND_YOU_ARE_PLAYING);
	  pPlayer(p_playerId);
  }

  if (! p_appendAudio) m_wave->playNext();
}


//----------------------------------------------------------------------
// ... is dummy
//----------------------------------------------------------------------

void Phrases::playDummyPosition (uint8_t p_playerId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  if (p_playerId >= NUMPLAYERS)
  {
	  m_wave->addSequence(SND_DUMMY);
	  m_wave->addSequence(SND_IS_NOT_SET);
  }
  else
  {
	  pPlayer(p_playerId);
	  m_wave->addSequence(SND_IS_DUMMY);
  }

  if (! p_appendAudio) m_wave->playNext();
}


void Phrases::playCurrentSuit (uint8_t p_suitId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(SND_PLAY_IS);
  pSuit(p_suitId, false);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// Various announcements regarding processing undo and redo commands
//----------------------------------------------------------------------
void Phrases::playUndoMode ( uint8_t p_confirmedFlag, uint8_t p_redoFlag, uint8_t p_undoEventId, uint8_t p_playerId, uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  if (! p_confirmedFlag)
  {
  	  m_wave->addSequence(SND_PRESS_AGAIN_TO);
  }

  if (p_redoFlag)
  {
  	  m_wave->addSequence(SND_REDO);
  }

  switch (p_undoEventId)
  {
  	  case UNDO_EVENTID_UNKNOWN:
  	  {
  	  	  m_wave->addSequence(SND_UNDO);
  	  }
  	  break;

  	  case UNDO_EVENTID_NEW_HAND:
  	  {
  		  if (p_confirmedFlag)
  			  m_wave->addSequence(SND_RESTORED);
  		  else
  			  m_wave->addSequence(SND_RESTORE);

  		  m_wave->addSequence(SND_PREVIOUS_HAND);
  	  }
  	  break;

  	  case UNDO_EVENTID_DEAL_HANDS:
  	  {
  		  if (p_confirmedFlag)
  			  m_wave->addSequence(SND_CLEARED);
  		  else
  			  m_wave->addSequence(SND_CLEAR);

  		  m_wave->addSequence(SND_ALL_HANDS);
  	  }
  	  break;

  	  case UNDO_EVENTID_SCAN_CARD:
  	  {
  		  if (p_confirmedFlag)
  			  m_wave->addSequence(SND_PICKED_UP);
  		  else
  			  m_wave->addSequence(SND_PICK_UP);
  		  pCard(p_cardId);
  		  pSuit(p_suitId, true);
  		  m_wave->addSequence(SND_BY);
  		  pPlayer(p_playerId);
  	  }
  	  break;

  	  case UNDO_EVENTID_SCAN_HAND:
  	  {
  		  if (p_confirmedFlag)
  			  m_wave->addSequence(SND_RETURNED);
  		  else
  			  m_wave->addSequence(SND_RETURN);
  		  m_wave->addSequence(SND_HAND);
  		  m_wave->addSequence(SND_BY);
  		  pPlayer(p_playerId);
  	  }
  	  break;

  	  case UNDO_EVENTID_SET_CONTRACT:
  	  {
  		  if (p_confirmedFlag)
  			  m_wave->addSequence(SND_REMOVED);
  		  else
  			  m_wave->addSequence(SND_REMOVE);
  		  m_wave->addSequence(SND_CONTRACT);
  	  }
  	  break;

  	  default:
  	  {
  		  playNumber(SND_UNKNOWN_EVENTID, p_undoEventId, p_appendAudio);
  	  }
  	  break;
  }

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// 8  (Your/Dummy's/<silent>) King Hearts (skip player annoucement if p_playerId > 1)
//----------------------------------------------------------------------

void Phrases::playSelectedCard (uint8_t p_playerId, uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  if (p_playerId < 2)
  {
    m_wave->addSequence(p_playerId == 0 ? SND_YOUR : SND_DUMMYS);
  }
  pCard(p_cardId);
  pSuit(p_suitId, true);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// 10 Cannot play Ten Diamond, Play is Hearts
//----------------------------------------------------------------------

void Phrases::playBadCard (uint8_t p_suitId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(SND_CANNOT_PLAY);
  m_wave->addSequence(SND_PLAY_IS);
  pSuit(p_suitId, false);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// 20 East Contract is Three Hearts
//----------------------------------------------------------------------

void Phrases::playContract (uint8_t p_playerId, uint8_t p_suitId, uint8_t p_numTricks, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  pPlayer(p_playerId);
  m_wave->addSequence(SND_CONTRACT_IS);
  pNumber(p_numTricks);
  pSuit(p_suitId, false);

  if (! p_appendAudio) m_wave->playNext();
}


//----------------------------------------------------------------------
// 21 West Played Ten Diamond (p_action: SND_PLAYED)
// 25 West picked up Four Diamonds (p_action: SND_PICKED_UP)
//----------------------------------------------------------------------

// (NOTE: argument must be a const char* in PROGMEM space)
void Phrases::playCardPlayed (uint8_t p_playerId, PGM_P p_action, uint8_t p_cardId, uint8_t p_suitId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  pPlayer(p_playerId);
  m_wave->addSequence(p_action);
  pCard(p_cardId);
  pSuit(p_suitId, true);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// 28 Your / Dummy’s hand is complete
//----------------------------------------------------------------------

void Phrases::playHandComplete (uint8_t p_playerId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(p_playerId == 0 ? SND_YOUR : SND_DUMMYS);
  m_wave->addSequence(SND_HAND_IS_COMPLETE);

  if (! p_appendAudio) m_wave->playNext();
}


void Phrases::playTrickTaken(uint8_t p_playerId, uint8_t p_suitId, uint8_t p_cardId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  pPlayer(p_playerId);
  m_wave->addSequence(SND_TOOK_TRICK_WITH);
  pCard(p_cardId);
  pSuit(p_suitId, true);

  if (! p_appendAudio) m_wave->playNext();
}

void Phrases::playTricksWon(PGM_P p_players, uint8_t p_numTricks, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  m_wave->addSequence(p_players);
  m_wave->addSequence(SND_HAVE_TAKEN);
  if (p_numTricks == 0)
    m_wave->addSequence(SND_NO);
  else
	pNumber(p_numTricks);
  m_wave->addSequence(p_numTricks == 1 ? SND_TRICK : SND_TRICKS);

  if (! p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// Keyboard Mode
//----------------------------------------------------------------------

void Phrases::playMode(uint8_t p_modeId, uint8_t p_appendAudio)
{
	if (s_silent) return;

	if (!p_appendAudio) m_wave->clearSequence();

	if (p_modeId >= NUMMODES)
	{
		playNumber(SND_MODEWORD, p_modeId, APPEND_AUDIO);
	}
	else
	{
		m_wave->addSequence(pgm_read_word(&(SND_MODES[p_modeId])));
	}

	if (!p_appendAudio) m_wave->playNext();
}

//----------------------------------------------------------------------
// Set Contract Mode
//----------------------------------------------------------------------

void Phrases::playContractMode(uint8_t p_modeId, uint8_t p_appendAudio)
{
	if (s_silent) return;

	if (!p_appendAudio) m_wave->clearSequence();

	if (p_modeId >= NUMCONTRACTMODES)
	{
		playNumber(SND_CONTRACTMODEWORD, p_modeId, APPEND_AUDIO);
	}
	else
	{
		m_wave->addSequence(pgm_read_word(&(SND_CONTRACTMODES[p_modeId])));
	}

	if (!p_appendAudio) m_wave->playNext();
}

void Phrases::playPosition (uint8_t p_playerId, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  if (p_playerId >= NUMPLAYERS)
  {
	  m_wave->addSequence(SND_IS_NOT_SET);
  }
  else
  {
	  pPlayer(p_playerId);
  }

  if (! p_appendAudio) m_wave->playNext();
}

void Phrases::playSuit (uint8_t p_suitId, uint8_t p_singular, uint8_t p_appendAudio)
{
  if (s_silent) return;

  if (! p_appendAudio) m_wave->clearSequence();

  if (p_suitId >= NUMSUITS)
  {
	  m_wave->addSequence(SND_IS_NOT_SET);
  }
  else
  {
	  pSuit(p_suitId, p_singular);
  }

  if (! p_appendAudio) m_wave->playNext();
}

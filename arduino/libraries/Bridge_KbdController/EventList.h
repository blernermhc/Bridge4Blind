#ifndef EventList_h
#define EventList_h
#include <stdint.h>
/**
 * \file
 * EventList class
 */

#define EVENT_LIST_SIZE 50

//------------------------------------------------------------------------------
/**
 * \class EventList
 * \brief Event list
 *
 * Represents the sequence of events received since last reset (wraps on overflow).
 *
 */
class EventList
{
 public:
	uint8_t m_eventList[EVENT_LIST_SIZE];
	uint8_t m_eventListNextIdx = 0;
	uint8_t m_eventListCurrentIdx = 0;
	uint8_t m_wrap = 0;

	EventList(void);
	void addEvent(uint8_t p_input, uint8_t p_repeat);
	void addEvent(uint8_t p_input0, uint8_t p_input1, uint8_t p_repeat);
	void resetEventList(uint8_t p_repeat);
	uint8_t previousEvent();
	void resetRepeatCursor();
};

#endif //EventList_h

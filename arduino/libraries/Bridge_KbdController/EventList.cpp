#include <Eventlist.h>

/** configure a new instance of Eventlist. */
EventList::EventList(void)
{
  m_eventListNextIdx = 0;
  m_eventListCurrentIdx = 0;
  m_wrap = 0;
}

void EventList::addEvent(uint8_t p_input, uint8_t p_repeat)
{
  if (p_repeat) return;

  m_eventList[m_eventListNextIdx++] = p_input;
  if (m_eventListNextIdx >= EVENT_LIST_SIZE)
  {
    m_eventListNextIdx = 0;
    m_wrap = 1;
  }
  m_eventListCurrentIdx = m_eventListNextIdx;
}

void EventList::addEvent(uint8_t p_input0, uint8_t p_input1, uint8_t p_repeat)
{
  addEvent(p_input1, p_repeat);	// put 2nd byte first, since we unroll backwards
  addEvent(p_input0, p_repeat);
}

void EventList::resetEventList(uint8_t p_repeat)
{
  if (p_repeat) return;

  m_eventListNextIdx = 0;
  m_eventListCurrentIdx = 0;
}

uint8_t EventList::previousEvent()
{
  --m_eventListCurrentIdx;
  if (m_eventListCurrentIdx < 0)
  {
    if (m_wrap)
    {
      m_eventListCurrentIdx = EVENT_LIST_SIZE - 1;
    }
    else
    {
      m_eventListCurrentIdx = 0;
      return 0;
    }
  }
  return m_eventList[m_eventListCurrentIdx];
}

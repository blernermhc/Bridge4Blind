#include <BridgeUtils.h>

void sendSerialString ()
{
  Serial.write(MESSAGE_START_STRING);
}

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

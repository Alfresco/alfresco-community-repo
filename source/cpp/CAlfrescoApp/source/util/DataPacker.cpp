/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

#include <string>
#include "util\DataPacker.h"
#include "util\ByteArray.h"

using namespace Alfresco;

/**
 * Unpack a short/16 bit value from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @return int
 */
int DataPacker::getShort(CBUFPTR buf, BUFPOS pos) {
	int sval = ( buf[pos] << 8) + buf[pos+1];
	return sval;
}

/**
 * Unpack an int/32 bit value from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @return int
 */
int DataPacker::getInt(CBUFPTR buf, BUFPOS pos) {
	int ival = (buf[pos] << 24) + (buf[pos+1] << 16) + (buf[pos+2] << 8) + buf[pos+3];
	return ival;
}

/**
 * Unpack a long/64 bit value from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @return LONG64
 */
LONG64 DataPacker::getLong(CBUFPTR buf, BUFPOS pos) {
	LONG64 lval = 0;
	BUFPTR pLval = (BUFPTR) &lval;

	for ( unsigned int i = 0; i < 8; i++) {
		pLval[7 - i] = buf[pos + i];
	}
	return lval;
}

/**
 * Unpack a short/16 bit value in Intel format from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @return int
 */
int DataPacker::getIntelShort(CBUFPTR buf, BUFPOS pos) {
	int sval = ( buf[pos+1] << 8) + buf[pos];
	return sval;
}

/**
 * Unpack an int/32 bit value in Intel format from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @return int
 */
int DataPacker::getIntelInt(CBUFPTR buf, BUFPOS pos) {
	int ival = (buf[pos+3] << 24) + (buf[pos+2] << 16) + (buf[pos+1] << 8) + buf[pos];
	return ival;
}

/**
 * Unpack a long/64 bit value in Intel format from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @return LONG64
 */
LONG64 DataPacker::getIntelLong(CBUFPTR buf, BUFPOS pos) {
	LONG64 lval = 0;
	BUFPTR pLval = (BUFPTR) &lval;

	for ( unsigned int i = 0; i < 8; i++) {
		pLval[i] = buf[pos + i];
	}
	return lval;
}

/**
 * Unpack a string from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @param maxLen const unsigned int
 * @param isUni const bool
 * @return String
 */
String DataPacker::getString(CBUFPTR buf, BUFPOS pos, const unsigned int maxLen, const bool isUni) {

	//	Check for a Unicode string

	if ( isUni)
		return getUnicodeString( buf, pos, maxLen);

	//  Search for the trailing null

  unsigned int maxpos = pos + maxLen;
  unsigned int endpos = pos;

  while (buf[endpos] != '\0' && endpos < maxpos)
    endpos++;
  return String((const char*) buf, pos, endpos - pos);
}

/**
 * Unpack a Unicode string from the buffer.
 * 
 * @param buf CBUFPTR
 * @param pos BUFPOS
 * @param maxLen const unsigned int
 * @return String
 */
String DataPacker::getUnicodeString(CBUFPTR buf, BUFPOS pos, const unsigned int maxLen) {

	//	Check for an empty string
	
	if ( maxLen == 0)
		return String();

	//	Word align the position

	pos = wordAlign( pos);

  //  Search for the trailing null

  int maxpos = pos + (maxLen * 2);
  int endpos = pos;

	std::wstring str;

	int cpos = 0;
  wchar_t curChar;

  do {

    //  Get a Unicode character from the buffer

		curChar = (wchar_t) DataPacker::getIntelShort(buf, endpos);

    //  Add the character to the string

		if ( curChar != 0)
			str += curChar;

    //  Update the buffer pointer

    endpos += 2;
    
  } while (curChar != 0 && endpos < maxpos);

  //  Return the string

  return String(str);
}

/**
 * Pack a short/16 bit value into the buffer.
 * 
 * @param val const int
 * @param buf BUFPTR
 * @param pos BUFPOS
 */
void DataPacker::putShort(const int val, BUFPTR buf, BUFPOS pos) {
	buf[pos]   = (unsigned char) (val >> 8) & 0xFF;
	buf[pos+1] = (unsigned char) (val & 0xFF);
}

/**
 * Pack an int/32 bit value into the buffer.
 * 
 * @param val const int
 * @param buf BUFPTR
 * @param pos BUFPOS
 */
void DataPacker::putInt(const int val, BUFPTR buf, BUFPOS pos) {
	buf[pos]   = (unsigned char) (val >> 24) & 0xFF;
	buf[pos+1] = (unsigned char) (val >> 16) & 0xFF;
	buf[pos+2] = (unsigned char) (val >> 8) & 0xFF;
	buf[pos+3] = (unsigned char) (val & 0xFF);
}

/**
 * Pack a long/64 bit value into the buffer.
 * 
 * @param val const LONG64
 * @param buf BUFPTR
 * @param pos BUFPOS
 */
void DataPacker::putLong(const LONG64 val, BUFPTR buf, BUFPOS pos) {
	BUFPTR pLval = (BUFPTR) &val;

	buf[pos]   = pLval[7];
	buf[pos+1] = pLval[6];
	buf[pos+2] = pLval[5];
	buf[pos+3] = pLval[4];
	buf[pos+4] = pLval[3];
	buf[pos+5] = pLval[2];
	buf[pos+6] = pLval[1];
	buf[pos+7] = pLval[0];
}

/**
 * Pack a short/16 bit value in Intel format into the buffer.
 * 
 * @param val const int
 * @param buf BUFPTR
 * @param pos BUFPOS
 */
void DataPacker::putIntelShort(const int val, BUFPTR buf, BUFPOS pos) {
	buf[pos+1] = (unsigned char) (val >> 8) & 0xFF;
	buf[pos]   = (unsigned char) (val & 0xFF);
}

/**
 * Pack an int/32 bit value in Intel format into the buffer.
 * 
 * @param val const int
 * @param buf BUFPTR
 * @param pos BUFPOS
 */
void DataPacker::putIntelInt(const int val, BUFPTR buf, BUFPOS pos) {
	buf[pos+3] = (unsigned char) (val >> 24) & 0xFF;
	buf[pos+2] = (unsigned char) (val >> 16) & 0xFF;
	buf[pos+1] = (unsigned char) (val >> 8) & 0xFF;
	buf[pos]   = (unsigned char) (val & 0xFF);
}

/**
 * Pack a long/64 bit value in Intel format into the buffer.
 * 
 * @param val const LONG64
 * @param buf BUFPTR
 * @param pos BUFPOS
 */
void DataPacker::putIntelLong(const LONG64 val, BUFPTR buf, BUFPOS pos) {
	BUFPTR pLval = (BUFPTR) &val;

	buf[pos+7] = pLval[7];
	buf[pos+6] = pLval[6];
	buf[pos+5] = pLval[5];
	buf[pos+4] = pLval[4];
	buf[pos+3] = pLval[3];
	buf[pos+2] = pLval[2];
	buf[pos+1] = pLval[1];
	buf[pos]   = pLval[0];
}

/**
 * Pack a string into the buffer.
 * 
 * @param str const String&
 * @param buf BUFPTR
 * @param pos BUFPOS
 * @param nullTerm const bool
 * @param isUni const bool
 * @return int
 */
unsigned int DataPacker::putString(const String& str, BUFPTR buf, BUFPOS pos, const bool nullTerm, const bool isUni) {
	
	//	Check if the string should be packed as Unicode or ASCII

	unsigned int newPos = pos;

	if ( isUni == true) {

		//	Pack the characters

		for ( unsigned int i = 0; i < str.length(); i++) {
			wchar_t ch = str.charAt(i);
			buf[newPos++] = (unsigned char) (ch & 0xFF);
			buf[newPos++] = (unsigned char) (ch >> 8) & 0xFF;
		}

		//	Add a null terminator, if required

		if ( nullTerm == true) {
			buf[newPos++] = '\0';
			buf[newPos++] = '\0';
		}
	}
	else {

		//	Get the string as ASCII characters

		ByteArray byts = str.getBytes();

		//	Pack the characters

		for ( unsigned int i = 0; i < str.length(); i++)
			buf[newPos++] = byts[i];

		//	Add a null terminator, if required

		if ( nullTerm == true)
			buf[newPos++] = '\0';
	}

	//	Return the new buffer position

	return newPos;
}

/**
 * Pack an ASCII string into the buffer
 * 
 * @param str const char*
 * @param buf BUFPTR
 * @param pos BUFPOS
 * @param nullTerm bool
 * @return unsigned int
 */
unsigned int DataPacker::putString(const char* str, BUFLEN len, BUFPTR buf, BUFPOS pos, bool nullTerm) {

	//	Copy the ASCII string to the buffer

	memcpy(buf + pos, str, len);

	BUFPOS endPos = pos + len;
	if ( nullTerm == true)
		buf[endPos] = '\0';

	//	Return the new buffer position

	return endPos;
}

/**
 * Pack a Unicode string into the buffer
 * 
 * @param str const wchar_t*
 * @param buf BUFPTR
 * @param pos BUFPOS
 * @param nullTerm bool
 * @return unsigned int
 */
unsigned int DataPacker::putString(const wchar_t* str, BUFLEN len, BUFPTR buf, BUFPOS pos, bool nullTerm) {

	//	Copy the Unicode string to the buffer

	BUFLEN uniLen = len * 2;
	BUFPOS endPos = pos + uniLen;

	memcpy(buf + pos, str, uniLen);
	if ( nullTerm == true) {
		buf[pos + uniLen + 1] = '\0';
		buf[pos + uniLen + 2] = '\0';
		endPos += 2;
	}

	//	Return the new buffer position

	return endPos;
}

/**
 * Pack a number of zero bytes into the buffer.
 * 
 * @param buf BUFPTR
 * @param pos BUFPOS
 * @param count const unsigned int
 */
void DataPacker::putZeros(BUFPTR buf, BUFPOS pos, const unsigned int count) {
  for (unsigned int i = 0; i < count; i++)
    buf[pos + i] = (unsigned char) 0;
}

/**
 * Determine the amount of buffer space required to pack the string with the specified settings.
 * 
 * @param str const String&
 * @param isUni const bool
 * @param nulTerm const bool
 * @return unsigned int
 */
unsigned int DataPacker::getStringLength(const String& str, const bool isUni, const bool nulTerm) {
	int len = str.length();
	if ( nulTerm == true)
		len += 1;
	if ( isUni == true)
		len *= 2;

	return len;		
}

/**
 * Calculate the buffer position after packing the string with the specified settings.
 * 
 * @param pos BUFPOS
 * @param str const String&
 * @param isUni const bool
 * @param nulTerm const bool
 * @return unsigned int
 */
unsigned int DataPacker::getBufferPosition(BUFPOS pos, const String& str, const bool isUni, const bool nulTerm) {
	unsigned int len = str.length();
	if ( nulTerm == true)
		len += 1;
	if ( isUni == true)
 	  len *= 2;

	return pos + len;		
}

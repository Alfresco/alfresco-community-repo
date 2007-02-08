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

#include "util\DataBuffer.h"
#include "util\Exception.h"

using namespace Alfresco;
using namespace std;

//	Use a macro for buffer overflow checks

#define CHECK_BUFFER(sz)					{if ((( m_buflen + m_offset) - m_pos) < sz) throw ArrayIndexOutOfBoundsException(__FILE__, __LINE__, L"DataBuffer overflow"); }
#define CHECK_BUFFER_POS(pos,sz)	{if ((( m_buflen + m_offset) - pos) < sz) throw ArrayIndexOutOfBoundsException(__FILE__, __LINE__, L"DataBuffer overflow"); }

#define EXTEND_CHECK(sz)					{if ((( m_buflen + m_offset) - m_pos) < sz) extendBuffer(); }
#define EXTEND_CHECK_POS(pos,sz)	{if ((( m_buflen + m_offset) - pos) < sz) extendBuffer(); }

/**
 * Class constructor
 * 
 * @param siz unsigned int
 */
DataBuffer::DataBuffer( unsigned int siz) {
	m_buf    = new unsigned char[siz];
	m_buflen = siz;

	m_owner = true;

	m_pos    = 0;
	m_endpos = 0;
	m_offset = 0;
}

/**
 * Class constructor
 * 
 * @param buf BUFPTR
 * @param off BUFPOS
 * @param len BUFLEN
 */
DataBuffer::DataBuffer( BUFPTR buf, BUFPOS off, BUFLEN len) {
	m_buf    = buf;
	m_buflen = len;

	m_owner = false;

	m_pos    = off;
	m_offset = off;
	m_endpos = off + len;
}

/**
 * Class destructor
 */
DataBuffer::~DataBuffer() {

	//	Delete the buffer, if owned by this object

	if ( m_owner == true && m_buf != NULL)
		delete[] m_buf;
}

/**
 * Return the buffer length
 * 
 * @return BUFLEN
 */
BUFLEN DataBuffer::getLength( void) const {
	if ( m_endpos != 0)
		return m_endpos - m_offset;
	return m_pos - m_offset;
}

/**
 * Return the length in words
 * 
 * @return unsigned int
 */
unsigned int DataBuffer::getLengthInWords( void) const {
	return getLength() / 2;
}

/**
 * Return the available buffer length
 * 
 * @return BUFLEN
 */
BUFLEN DataBuffer::getAvailableLength( void) const {
	if ( m_endpos == 0)
		return 0;
	return m_endpos - m_pos;
}

/**
 * Get a byte from the buffer and advance the buffer pointer
 * 
 * @return unsigned char
 */
unsigned char DataBuffer::getByte( void) {

	//	Check if there is enough space in the buffer for the data

	CHECK_BUFFER(1);

	// Return the data

	return (unsigned int) m_buf[m_pos++];
}

/**
 * Get a short/16bit value from the buffer and advance the buffer pointer
 * 
 * @return unsigned int
 */
unsigned int DataBuffer::getShort( void) {

	//	Check if there is enough space in the buffer for the data

	CHECK_BUFFER(2);

	//	Get a short value from the buffer

	unsigned int sval = DataPacker::getIntelShort( m_buf, m_pos);
	m_pos += 2;
	return sval;
}

/**
 * Get an integer from the buffer and advance the buffer pointer
 * 
 * @return unsigned int
 */
unsigned int DataBuffer::getInt( void) {

	//	Check if there is enough space in the buffer for the data

	CHECK_BUFFER(4);

	//	Get a short value from the buffer

	unsigned int ival = DataPacker::getIntelInt( m_buf, m_pos);
	m_pos += 4;
	return ival;
}

/**
 * Get a long from the buffer and advance the buffer pointer
 * 
 * @return LONG64
 */
LONG64 DataBuffer::getLong( void) {

	//	Check if there is enough space in the buffer for the data

	CHECK_BUFFER(8);

	//	Get a long value from the buffer

	LONG64 lval = DataPacker::getIntelLong( m_buf, m_pos);
	m_pos += 8;
	return lval;
}

/**
 * Get a string from the buffer and advance the buffer pointer
 * 
 * @param uni bool
 * @return String
 */
String DataBuffer::getString( bool uni) {
	return getString( 255, uni);
}

/**
 * Get a string from the buffer and advance the buffer pointer
 * 
 * @param maxlen unsigned int
 * @param uni bool
 * @return String
 */
String DataBuffer::getString( unsigned int maxlen, bool uni) {

	//	Check for Unicode or ASCII
	
	String ret;
	unsigned int availLen = 0;
	
	if ( uni) {
		
		//	Word align the current buffer position, calculate the available length
		
		m_pos = DataPacker::wordAlign(m_pos);
		availLen = (m_endpos - m_pos) / 2;
		if ( availLen < maxlen)
			maxlen = availLen;
		
		ret = DataPacker::getUnicodeString(m_buf, m_pos, maxlen);
		if ( ret.length() < maxlen)
			m_pos += (ret.length() * 2) + 2;
		else
			m_pos += maxlen * 2;
	}
	else {

		//	Calculate the available length
		
		availLen = m_endpos - m_pos;
		if ( availLen < maxlen)
		  maxlen = availLen;
		
		//	Unpack the ASCII string
		
		ret = DataPacker::getString(m_buf, m_pos, maxlen);
		if ( ret.length() < maxlen)
			m_pos += ret.length() + 1;
		else
			m_pos += maxlen;
	}
	
	//	Return the string
	
	return ret;
}

/**
 * Get a short value at the specified buffer position
 * 
 * @param idx unsigned int
 * @return unsigned int
 */
unsigned int DataBuffer::getShortAt( unsigned int idx) {

	//	Check if there is enough data in the buffer

	BUFPOS pos = m_offset + (idx * 2);
	CHECK_BUFFER_POS(pos, 2);
		
	//	Unpack the short value
	
	return DataPacker::getIntelShort(m_buf, pos);
}

/**
 * Get an integer value at the specified buffer position
 * 
 * @param idx unsigned int
 * @return unsigned int
 */
unsigned int DataBuffer::getIntAt( unsigned int idx) {

	//	Check if there is enough data in the buffer

	BUFPOS pos = m_offset + (idx * 4);
	CHECK_BUFFER_POS(pos, 4);
		
	//	Unpack the integer value
	
	return DataPacker::getIntelInt(m_buf, pos);
}

/**
 * Get a long value at the specified buffer position
 * 
 * @param idx unsigned int
 * @return LONG64
 */
LONG64 DataBuffer::getLongAt( unsigned int idx) {

	//	Check if there is enough data in the buffer

	BUFPOS pos = m_offset + (idx * 8);
	CHECK_BUFFER_POS(pos, 8);
		
	//	Unpack the long value
	
	return DataPacker::getIntelLong(m_buf, pos);
}

/**
 * Append a byte to the buffer and advance the buffer pointer
 * 
 * @param byt unsigned char
 */
void DataBuffer::putByte( unsigned char byt) {

	//	Check if the buffer needs extending

	EXTEND_CHECK(1);

	//	Pack the data, update the buffer pointer

	m_buf[m_pos++] = byt;
}

/**
 * Append a short to the buffer and advance the buffer pointer
 * 
 * @param sval unsigned int
 */
void DataBuffer::putShort( unsigned int sval) {

	//	Check if the buffer needs extending

	EXTEND_CHECK(2);

	//	Pack the data, update the buffer pointer

	DataPacker::putIntelShort( sval, m_buf, m_pos);
	m_pos += 2;
}

/**
 * Append an integer to the buffer and advance the buffer pointer
 * 
 * @param ival unsigned int
 */
void DataBuffer::putInt( unsigned int ival) {

	//	Check if the buffer needs extending

	EXTEND_CHECK(4);

	//	Pack the data, update the buffer pointer

	DataPacker::putIntelInt( ival, m_buf, m_pos);
	m_pos += 4;
}

/**
 * Append a long to the buffer and advance the buffer pointer
 * 
 * @param lval LONG64
 */
void DataBuffer::putLong( LONG64 lval) {

	//	Check if the buffer needs extending

	EXTEND_CHECK(8);

	//	Pack the data, update the buffer pointer

	DataPacker::putIntelLong( lval, m_buf, m_pos);
	m_pos += 8;
}

/**
 * Put a short value into the buffer at the specified position
 * 
 * @param idx unsigned int
 * @param sval unsigned int
 */
void DataBuffer::putShortAt( unsigned int idx, unsigned int sval) {
	  
	//	Check if there is enough space in the buffer
	
	BUFPOS pos = m_offset + (idx * 2);
	EXTEND_CHECK_POS(pos,2);
	  
	//	Pack the short value
	
	DataPacker::putIntelShort(sval, m_buf, pos);
}

/**
 * Put an integer value into the buffer at the specified position
 * 
 * @param idx unsigned int
 * @param ival unsigned int
 */
void DataBuffer::putIntAt( unsigned int idx, unsigned int ival) {
	  
	//	Check if there is enough space in the buffer
	
	BUFPOS pos = m_offset + (idx * 4);
	EXTEND_CHECK_POS(pos,4);
	  
	//	Pack the integer value
	
	DataPacker::putIntelInt(ival, m_buf, pos);
}

/**
 * Put a long value into the buffer at the specified position
 * 
 * @param idx unsigned int
 * @param lval LONG64
 */
void DataBuffer::putLongAt( unsigned int idx, LONG64 lval) {
	  
	//	Check if there is enough space in the buffer
	
	BUFPOS pos = m_offset + (idx * 8);
	EXTEND_CHECK_POS(pos,8);
	  
	//	Pack the long value
	
	DataPacker::putIntelLong(lval, m_buf, pos);
}

/**
 * Append a string to the buffer and advance the buffer pointer
 * 
 * @param str const String&
 * @param uni bool
 * @param nulTerm bool
 */
void DataBuffer::putString( const String& str, bool uni, bool nulTerm) {
		
	//	Check for Unicode or ASCII
	
	if ( uni) {
		
		//	Check if there is enough space in the buffer		  

		unsigned int bytLen = str.length() * 2;
		if ( m_buflen - m_pos < bytLen)
		  extendBuffer(bytLen + 4);
		  
		//	Word align the buffer position, pack the Unicode string
		
		m_pos = DataPacker::wordAlign(m_pos);
		DataPacker::putString(str, m_buf, m_pos, nulTerm, true);
		m_pos += (str.length() * 2);
		if ( nulTerm)
			m_pos += 2;
	}
	else {
		
		//	Check if there is enough space in the buffer		  

		if ( m_buflen - m_pos < str.length())
		  extendBuffer(str.length() + 2);
		  
		//	Pack the ASCII string
		
		DataPacker::putString(str, m_buf, m_pos, nulTerm);
		m_pos += str.length();
		if ( nulTerm)
			m_pos++;
	}
}

/**
 * Append a fixed length string to the buffer and advance the buffer pointer
 * 
 * @param str const String&
 * @param len unsigned int
 */
void DataBuffer::putFixedString( const String& str, unsigned int len) {
		
	//	Check if there is enough space in the buffer		  

	if ( m_buflen - m_pos < str.length())
	  extendBuffer(str.length() + 2);
	  
	//	Pack the ASCII string
	
	DataPacker::putString(str, m_buf, m_pos);
	m_pos += len;

	//	Pad the string to the required length

	while ( len > str.length()) {
		m_buf[m_pos++] = 0;
		len--;
	}
}

/**
 * Put a string into the buffer at the specified position
 * 
 * @param str const String&
 * @param pos BUFPOS
 * @param uni bool
 * @param nulTerm bool
 * @return BUFPOS
 */
BUFPOS DataBuffer::putStringAt( const String& str, BUFPOS pos, bool uni, bool nulTerm) {

	//	Check for Unicode or ASCII

	BUFPOS retPos = 0;
			
	if ( uni) {
		
		//	Check if there is enough space in the buffer		  

		unsigned int bytLen = str.length() * 2;
		if ( m_buflen - pos < bytLen)
		  extendBuffer(bytLen + 4);
		  
		//	Word align the buffer position, pack the Unicode string
		
		pos = DataPacker::wordAlign(pos);
		retPos = DataPacker::putString(str, m_buf, pos, nulTerm);
	}
	else {
		
		//	Check if there is enough space in the buffer		  

		if ( m_buflen - pos < str.length())
		  extendBuffer(str.length() + 2);
		  
		//	Pack the ASCII string
		
		retPos = DataPacker::putString(str, m_buf, pos, nulTerm);
	}
	
	//	Return the end of string buffer position
	
	return retPos;
}

/**
 * Put a fixed length string into the buffer at the specified position
 * 
 * @param str const String&
 * @param len unsigned int
 * @param pos BUFPOS
 * @return BUFPOS
 */
BUFPOS DataBuffer::putFixedStringAt( const String& str, unsigned int len, BUFPOS pos) {
		
	//	Check if there is enough space in the buffer		  

	if ( m_buflen - pos < str.length())
	  extendBuffer(str.length() + 2);
	  
	//	Pack the ASCII string
	
	pos = DataPacker::putString(str, m_buf, pos);

	//	Pad the string

	while ( len > str.length()) {
		m_buf[pos++] = 0;
		len--;
	}

	//	Return the end of string buffer position

	return pos;
}

/**
 * Put a string pointer into the buffer
 * 
 * @param off unsigned int
 */
void DataBuffer::putStringPointer( unsigned int off) {
		
	//	Calculate the offset from the start of the data buffer to the string position
	
	DataPacker::putIntelInt(off - m_offset, m_buf, m_pos);
	m_pos += 4;
}

/**
 * Append a block of nulls to the buffer and advance the buffer pointer
 * 
 * @param cnt unsigned int
 */
void DataBuffer::putZeros( unsigned int cnt) {

	//	Check if there is enough space in the buffer		  

	if ( m_buflen - m_pos < cnt)
	  extendBuffer(cnt);

	//	Pack the zero bytes

	for ( unsigned int i = 0; i < cnt; i++)
		m_buf[m_pos++] = 0;		
}

/**
 * Word align the buffer pointer
 * 
 */
void DataBuffer::wordAlign( void) {
	m_pos = DataPacker::wordAlign(m_pos);
}

/**
 * Longword align the buffer pointer
 * 
 */
void DataBuffer::longwordAlign( void) {
	m_pos = DataPacker::longwordAlign(m_pos);
}

/**
 * Append a block of byte data to the buffer and advance the buffer pointer
 * 
 * @param buf BUFPTR
 * @param off BUFPOS
 * @param len BUFLEN
 */
void DataBuffer::appendData( BUFPTR buf, BUFPOS off, BUFLEN len) {

	//	Check if there is enough space in the buffer		  

	if ( m_buflen - m_pos < len)
	  extendBuffer(len);

	//	Copy the data to the buffer and update the current write position

	memcpy( m_buf + m_pos, buf + off, len);
	m_pos += len;
}

/**
 * Copy data to the user buffer and advance the buffer pointer
 * 
 * @param buf BUFPTR
 * @param pos BUFPOS
 * @param cnt unsigned int
 */
unsigned int DataBuffer::copyData( BUFPTR buf, BUFPOS pos, unsigned int cnt) {
		
	//	Check if there is any more data to copy
	
	if ( m_pos == m_endpos)
		return 0;
		
	//	Calculate the amount of data to copy
	
	unsigned int siz = m_endpos - m_pos;
	if ( siz > cnt)
		siz = cnt;
		
	//	Copy the data to the user buffer and update the current read position

	memcpy( buf + pos, m_buf + m_pos, siz);
	m_pos += siz;
	
	//	Return the amount of data copied
	
	return siz;
}

/**
 * Advance the buffer pointer by the specified amount
 * 
 * @param len unsigned int
 */
void DataBuffer::skipBytes( unsigned int len) {
		  
	//	Check if there is enough data in the buffer

	CHECK_BUFFER(len);
		
	//	Skip bytes
	
	m_pos += len;
}

/**
 * Set the end of buffer position
 */
void DataBuffer::setEndOfBuffer( void) {
	m_endpos = m_pos;
	m_pos    = m_offset;
}

/**
 * Set the buffer length
 * 
 * @param len BUFLEN
 */
void DataBuffer::setLength( BUFLEN len) {
	m_pos = m_offset + len;
}

/**
 * Extend the buffer by the specified amount by reallocating the buffer and copying the existing
 * data to the new buffer
 * 
 * @param ext BUFLEN
 */
void DataBuffer::extendBuffer( BUFLEN ext) {
	  
	//	Create a new buffer of the required size

	BUFLEN newlen = m_buflen + ext;
	BUFPTR newBuf = new unsigned char[newlen];
	
	//	Copy the data from the current buffer to the new buffer

	memcpy( newBuf, m_buf, m_buflen);

	//	Check if the previous buffer was owned by this object

	if ( m_owner)
		delete[] m_buf;

	//	Set the new buffer to be the main buffer
	
	m_buf    = newBuf;
	m_buflen = newlen;
	m_owner  = true;
}

/**
 * Extend the buffer doubling the current size by reallocating the buffer and copying the existing
 * data to the new buffer
 * 
 */
void DataBuffer::extendBuffer( void) {
	extendBuffer( m_buflen * 2);
}

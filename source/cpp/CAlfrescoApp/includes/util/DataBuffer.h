/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

#ifndef _DataBuffer_H
#define _DataBuffer_H

//	Includes

#include "util\DataPacker.h"

//	Classes defined in this header file

namespace Alfresco {
	class DataBuffer;
	typedef std::auto_ptr<DataBuffer> PTR_DataBuffer;
}

//	Constants

namespace Alfresco {

	//	Default data buffer size

	#define DataBufferDefaultSize	256
}

/**
 * Data Buffer Class
 * 
 * Dynamic buffer for getting/setting data blocks.
 */
class Alfresco::DataBuffer {
public:
	//	Class constructors

	DataBuffer( unsigned int siz = DataBufferDefaultSize);
	DataBuffer( BUFPTR buf, BUFPOS off, BUFLEN len);

	//	Class destructor

	~DataBuffer();

	//	Getter methods

	inline BUFPTR getBuffer( void) { return m_buf; }

	BUFLEN getLength( void) const;
	unsigned int getLengthInWords( void) const;
	BUFLEN getAvailableLength( void) const;
	inline BUFLEN getBufferLength(void) const { return m_buflen; }

	inline unsigned int getDisplacement( void) const { return m_pos - m_offset; }
	inline BUFPOS getOffset( void) { return m_offset; }
	inline BUFPOS getPosition( void) { return m_pos; }

	//	Get data items from the buffer

	unsigned char getByte( void);
	unsigned int getShort( void);
	unsigned int getInt( void);
	LONG64 getLong( void);
	String getString( bool uni = true);
	String getString( unsigned int maxLen, bool uni = true);

	unsigned int getShortAt( unsigned int idx);
	unsigned int getIntAt( unsigned int idx);
	LONG64 getLongAt( unsigned int idx);

	//	Put data items into the buffer

	void putByte( unsigned char byt);
	void putShort( unsigned int sval);
	void putInt( unsigned int ival);
	void putLong( LONG64 lval);
	
	void putShortAt( unsigned int idx, unsigned int sval);
	void putIntAt( unsigned int idx, unsigned int ival);
	void putLongAt( unsigned int idx, LONG64 lval);

	void putString( const String& str, bool uni = true, bool nulTerm = true);
	void putFixedString( const String& str, unsigned int len);
	BUFPOS putStringAt( const String& str, BUFPOS pos, bool uni = true, bool nulTerm = true);
	BUFPOS putFixedStringAt( const String& str, unsigned int len, BUFPOS pos);

	void putStringPointer( unsigned int off);
	void putZeros( unsigned int cnt);

	//	Align the buffer position

	void wordAlign( void);
	void longwordAlign( void);

	//	Append a raw data block to the buffer

	void appendData( BUFPTR buf, BUFPOS off, BUFLEN len);

	//	Copy the data to the user buffer and update the read position

	unsigned int copyData( BUFPTR buf, BUFPOS pos, unsigned int cnt);

	//	Skip data items in the buffer

	void skipBytes( unsigned int len);

	//	Setter methods

	inline void setPosition( BUFPOS pos) { m_pos = pos; }
	void setEndOfBuffer( void);
	void setLength( BUFLEN len);

private:
	//	Extend the buffer

	void extendBuffer( BUFLEN ext);
	void extendBuffer( void);

protected:
	//	Instance variables
	//
	//	Data buffer

	BUFPTR m_buf;
	unsigned int m_buflen;

	//	Flag to indicate if the buffer is owned by this object

	bool m_owner;

	//	Buffer positions/offsets

	BUFPOS m_pos;
	BUFPOS m_endpos;
	BUFPOS m_offset;
};

#endif

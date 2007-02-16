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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

#ifndef _ByteArray_H
#define _ByteArray_H

//	Includes

#include <memory>
#include <string>

#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class ByteArray;

	typedef std::auto_ptr<ByteArray> PTR_ByteArray;
}

/**
 * Byte Array Class
 * 
 * Provides a byte array object similar to Javas byte[].
 */
class Alfresco::ByteArray {
public:
	//	Constructors

	ByteArray( BUFLEN len = 0, bool clearMem = false);
	ByteArray( CBUFPTR data, BUFLEN len);
	ByteArray( const char* data, BUFLEN len);

	//	Copy constructor

	ByteArray( const ByteArray& byts);

	//	Class destructor

	~ByteArray();

	//	Return the data/length

	inline CBUFPTR getData( void) const { return m_data; }
	inline BUFPTR getData( void) { return m_data; }
	inline BUFLEN getLength( void) const { return m_length; }

	//	Set the array length

	void setLength( BUFLEN len, bool clearMem = false);

	//	Set a byte

	void setByte( unsigned int idx, unsigned char val);

	//	Subscript operator

	unsigned char& operator[](const unsigned int idx);

	//	Assignment operator

	ByteArray& operator=( const ByteArray& byts);
	ByteArray& operator=( std::string& byts);

	//	Equality operator

	bool operator== ( const ByteArray& byts);

	//	Return the start address of the byte array

	operator const unsigned char* ( void) { return m_data; }

protected:
	//	Set the byte array and length

	void setData( CBUFPTR data, BUFLEN len);

private:
	//	Instance variables
	//
	//	Byte data and length

	BUFPTR m_data;
	BUFLEN m_length;
};

#endif

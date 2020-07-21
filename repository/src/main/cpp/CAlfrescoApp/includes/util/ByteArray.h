/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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

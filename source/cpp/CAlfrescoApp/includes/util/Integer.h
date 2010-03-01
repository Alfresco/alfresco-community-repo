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

#ifndef _JavaInteger_H
#define _JavaInteger_H

//	Includes

#include "util\String.h"
#include "util\Exception.h"
#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class Integer;
}

/**
 * Java-like Integer Class
 *
 * Provides static methods to convert integer values to strings.
 */
class Alfresco::Integer {
public:
	//	Convert an integer to a hexadecimal string

	static String toHexString( const unsigned int ival);
	static String toHexString( BUFPTR ptr);

	//	Convert an integer value to a string

	static String toString( unsigned int ival, unsigned int radix = 10);

	//	Parse a string to generate an integer value

	static unsigned int parseInt( const String& str, unsigned int radix = 10);

private:
	//	Hide constructors, static only class

	Integer( void);
	Integer(Integer& ival);
};

#endif
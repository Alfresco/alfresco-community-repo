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

#include "util\Integer.h"

using namespace Alfresco;

/**
 * Convert an integer value to a hexadecimal string
 * 
 * @param ival const unsigned int
 * @return String
 */
String Integer::toHexString( const unsigned int ival) {
	char buf[32];
	_itoa(ival, buf, 16);
	return String(buf);
}

/**
* Convert an buffer pointer to a hexadecimal string
* 
* @param ptr BUFPTR
* @return String
*/
String Integer::toHexString( BUFPTR ptr) {
	char buf[32];
	sprintf( buf, "%p", ptr);
	return String(buf);
}

/**
 * Convert an integer to a string
 * 
 * @param ival unsigned int
 * @param radix unsigned int
 * @return String
 */
String Integer::toString( unsigned int ival, unsigned int radix) {
	char buf[32];
	_itoa(ival, buf, radix);
	return String(buf);
}

/**
 * Parse a string to generate an integer value
 * 
 * @param str const String&
 * @param radix unsigned int
 * @return unsigned int
 */
unsigned int Integer::parseInt( const String& str, unsigned int radix) {
	wchar_t* pEndPtr = NULL;
	return (unsigned int) wcstoul( str.data(), &pEndPtr, radix);
}

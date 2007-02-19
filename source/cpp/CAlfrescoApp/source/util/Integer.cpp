/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

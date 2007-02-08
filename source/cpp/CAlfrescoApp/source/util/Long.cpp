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

#include "util\Long.h"

using namespace Alfresco;

/**
 * Convert a long/64 bit integer value to a hexadecimal string
 * 
 * @param lval const LONG64
 * @return String
 */
String Long::toHexString( const LONG64 lval) {
	char buf[32];
	sprintf( buf, "%I64x", lval);
	return String(buf);
}

/**
* Convert a long/64 bit integer value to a decimal string
* 
* @param lval const LONG64
* @return String
*/
String Long::toString( const LONG64 lval) {
	char buf[32];
	sprintf( buf, "%I64d", lval);
	return String(buf);
}

/**
 * Make a long/64bit value from the low/high 32bit values
 * 
 * @param lowPart unsigned int
 * @param highPart unsigned int
 * @return LONG64
 */
LONG64 Long::makeLong( unsigned int lowPart, unsigned int highPart) {
	LONG64 lVal = (LONG64) lowPart + (((LONG64) highPart) << 32);
	return lVal;
}

/**
* Make a long/64bit value from the low/high 32bit values of the FILETIME structure
* 
* @param fTime FILETIME
* @return LONG64
*/
LONG64 Long::makeLong( FILETIME fTime) {
	LONG64 lVal = (LONG64) fTime.dwLowDateTime + (((LONG64) fTime.dwHighDateTime) << 32);
	return lVal;
}

/**
 * Parse a string to generate a long/64 bit integer value
 * 
 * @param str const String&
 * @param radix unsigned int
 * @return LONG64
 */
LONG64 Long::parseLong( const String& str, unsigned int radix) {
	wchar_t* pEndPtr = NULL;
	return _wcstoui64( str.data(), &pEndPtr, radix);
}

/**
 * Copy a long/64bit value to a FILETIME structure
 * 
 * @param lval LONG64
 * @param ftime FILETIME&
 */
void Long::copyTo( LONG64 lval, FILETIME& ftime) {
	memcpy( &ftime, &lval, sizeof( LONG64));
}

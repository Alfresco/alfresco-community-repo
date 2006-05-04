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

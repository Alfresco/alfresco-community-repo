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

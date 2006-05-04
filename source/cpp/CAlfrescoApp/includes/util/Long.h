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

#ifndef _JavaLong_H
#define _JavaLong_H

//	Includes

#include <windows.h>

#include "util\String.h"
#include "util\Exception.h"
#include "util\JavaTypes.h"

//	Classes defined in this header file

namespace Alfresco {
	class Long;
}

/**
 * Java-like Long Class
 * 
 * Provides static methods to convert long/64 bit values to strings.
 */
class Alfresco::Long {
public:
	//	Convert a long/64 bit integer to a hexadecimal string

	static String toHexString( const LONG64 lval);

	// Convert a long/64 bit integer to a decimal string

	static String toString( const LONG64 lval);

	//	Make a long/64bit value from the low/high 32bit values

	static LONG64 makeLong( unsigned int lowPart, unsigned int highPart);
	static LONG64 makeLong( FILETIME fTime);

	//	Get the low/high 32bit values from a 64bit value

	static bool hasHighPart( LONG64 lval) { return ( lval > 0xFFFFFFFF) ? true : false; }

	static unsigned int getLowPart( LONG64 lval) { return (unsigned int) lval & 0xFFFFFFFF; }
	static unsigned int getHighPart( LONG64 lval) { return (unsigned int) ((lval >> 32) & 0xFFFFFFFF); }

	//	Parse a string to generate a long/64 bit integer value

	static LONG64 parseLong( const String& str, unsigned int radix = 10);

	//	Copy a long/64bit value to a FILETIME structure

	static void copyTo( LONG64 lval, FILETIME& ftime);

private:
	//	Hide constructors, static only class

	Long( void);
	Long(Long& ival);
};

#endif
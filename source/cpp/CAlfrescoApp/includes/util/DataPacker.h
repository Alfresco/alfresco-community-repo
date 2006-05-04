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

#ifndef _DataPacker_H
#define _DataPacker_H

//	Includes

#include "util\String.h"
#include "util\Types.h"
#include "util\JavaTypes.h"

//	Classes defined in this header file

namespace Alfresco {
	class DataPacker;
}

/**
 * DataPacker Class
 *
 * The DataPacker class provides methods for packing and unpacking of various data types from a buffer.
 */
class Alfresco::DataPacker {
private:
	//	Hide constructors

	DataPacker( void) {};
	DataPacker(const DataPacker& dp) {};

public:
	//	Unpack data types from a buffer

	static int getShort(CBUFPTR buf, BUFPOS pos);
	static int getInt(CBUFPTR buf, BUFPOS pos);
	static LONG64 getLong(CBUFPTR buf, BUFPOS pos);

	static int getIntelShort(CBUFPTR buf, BUFPOS pos);
	static int getIntelInt(CBUFPTR buf, BUFPOS pos);
	static LONG64 getIntelLong(CBUFPTR buf, BUFPOS pos);

	static String getString(CBUFPTR buf, BUFPOS pos, const unsigned int maxLen, const bool isUni = false);
	static String getUnicodeString(CBUFPTR buf, BUFPOS pos, const unsigned int maxLen);

	//	Pack data types into a buffer

	static void putShort(const int val, BUFPTR buf, BUFPOS pos);
	static void putInt(const int val, BUFPTR buf, BUFPOS pos);
	static void putLong(const LONG64 val, BUFPTR buf, BUFPOS pos);

	static void putIntelShort(const int val, BUFPTR buf, BUFPOS pos);
	static void putIntelInt(const int val, BUFPTR buf, BUFPOS pos);
	static void putIntelLong(const LONG64 val, BUFPTR buf, BUFPOS pos);

	static unsigned int putString(const String& str, BUFPTR buf, BUFPOS pos, bool nullTerm = true, bool isUni = false);
	static unsigned int putString(const char* str, BUFLEN len, BUFPTR buf, BUFPOS pos, bool nullTerm = true);
	static unsigned int putString(const wchar_t* str, BUFLEN len, BUFPTR buf, BUFPOS pos, bool nullTerm = true);

	static void putZeros(BUFPTR buf, BUFPOS pos, const unsigned int count);

	//	Calculate buffer positions

	static unsigned int getStringLength(const String& str, const bool isUni = false, const bool nulTerm = false);
	static unsigned int getBufferPosition(BUFPOS pos, const String& str, const bool isUni = false, const bool nulTerm = false);

	//	Align a buffer offset

	static inline BUFPOS longwordAlign( BUFPOS pos) { return ( pos + 3) & 0xFFFFFFFC; }
	static inline BUFPOS wordAlign( BUFPOS pos) { return ( pos + 1) & 0xFFFFFFFE; }
};

#endif

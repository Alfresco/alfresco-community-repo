/*
* Copyright (C) 2005 Alfresco, Inc.
*
* Licensed under the Alfresco Network License. You may obtain a
* copy of the License at
*
*   http://www.alfrescosoftware.com/legal/
*
* Please view the license relevant to your network subscription.
*
* BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
* READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
* YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
* ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
* THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
* AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
* TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
* BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
* HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
* SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
* TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
* CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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

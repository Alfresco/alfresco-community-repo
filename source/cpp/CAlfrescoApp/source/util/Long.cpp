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

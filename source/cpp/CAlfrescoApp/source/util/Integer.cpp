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
	itoa(ival, buf, 16);
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
	itoa(ival, buf, radix);
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

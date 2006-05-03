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
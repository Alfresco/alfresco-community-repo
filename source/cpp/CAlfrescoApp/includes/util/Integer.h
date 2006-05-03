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

#ifndef _JavaInteger_H
#define _JavaInteger_H

//	Includes

#include "util\String.h"
#include "util\Exception.h"
#include "util\Types.h"

//	Classes defined in this header file

namespace Alfresco {
	class Integer;
}

/**
 * Java-like Integer Class
 *
 * Provides static methods to convert integer values to strings.
 */
class Alfresco::Integer {
public:
	//	Convert an integer to a hexadecimal string

	static String toHexString( const unsigned int ival);
	static String toHexString( BUFPTR ptr);

	//	Convert an integer value to a string

	static String toString( unsigned int ival, unsigned int radix = 10);

	//	Parse a string to generate an integer value

	static unsigned int parseInt( const String& str, unsigned int radix = 10);

private:
	//	Hide constructors, static only class

	Integer( void);
	Integer(Integer& ival);
};

#endif
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

#ifndef _AlfrescoTypes_H
#define _AlfrescoTypes_H

//	Includes

#include "util\JavaTypes.h"

namespace Alfresco {

	//	Type definitions
	//
	//	Data buffer pointer, position and length

	typedef unsigned char* BUFPTR;
	typedef unsigned int BUFPOS;
	typedef unsigned int BUFLEN;

	typedef const unsigned char* CBUFPTR;
	typedef const unsigned int CBUFPOS;
	typedef const unsigned int CBUFLEN;

	//	File position and length

	typedef LONG64	FILEPOS;
	typedef LONG64	FILELEN;

	//	Date/time

	typedef LONG64	DATETIME;
	#define NULL_DATETIME ((DATETIME) 0)
}

#endif

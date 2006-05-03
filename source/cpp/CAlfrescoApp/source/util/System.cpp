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

#include "util\System.h"

#include <sys\timeb.h>

using namespace Alfresco;

/**
 * Return the current system time in milliseconds since Jan 1 1970
 * 
 * @return DATETIME
 */
DATETIME System::currentTimeMillis( void) {

	//	Get the current system time

	struct __timeb64 timeNow;
	_ftime64( &timeNow);

	//	Build the milliseconds time

	DATETIME timeNowMillis = ( timeNow.time * 1000L) + (DATETIME) timeNow.millitm;
	return timeNowMillis;
}
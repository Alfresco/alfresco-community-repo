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

#include "util\Exception.h"

using namespace Alfresco;
using namespace std;

//	Define standard Java-like exceptions

EXCEPTION_CLASS(Alfresco, IOException);
EXCEPTION_CLASS(Alfresco, NullPointerException);
EXCEPTION_CLASS(Alfresco, ArrayIndexOutOfBoundsException);
EXCEPTION_CLASS(Alfresco, NumberFormatException);

/**
* Class constructor
* 
* @param moduleName const char*
* @param lineNum unsigned int
* @param msg const wchar_t*
* @param msg2 const wchar_t*
* @param msg3 const wchar_t*
* @param msg4 const wchar_t*
* @param msg5 const wchar_t*
*/
Exception::Exception( const char* moduleName, unsigned int lineNum, const wchar_t* msg, const wchar_t* msg2,
										  const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) {

	//	Prefix the message string with the module name and line number

	m_msg = moduleName;
	if ( lineNum != 0) {
		m_msg += " (";
		m_msg += lineNum;
		m_msg += ")";
	}
	m_msg += ": ";

	//	Add the messages parts

	if ( msg)
		m_msg += msg;

	if ( msg2) {
		m_msg += " ";
		m_msg += msg2;
	}

	if ( msg3) {
		m_msg += " ";
		m_msg += msg3;
	}

	if ( msg4) {
		m_msg += " ";
		m_msg += msg4;
	}

	if ( msg5) {
		m_msg += " ";
		m_msg += msg5;
	}
}

/**
 * Class constructor
 * 
 * @param msg const wchar_t*
 * @param msg2 const wchar_t*
 * @param msg3 const wchar_t*
 * @param msg4 const wchar_t*
 * @param msg5 const wchar_t*
 */
Exception::Exception( const wchar_t* msg, const wchar_t* msg2, const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) {
	if ( msg)
		m_msg = msg;

	if ( msg2) {
		m_msg += " ";
		m_msg += msg2;
	}

	if ( msg3) {
		m_msg += " ";
		m_msg += msg3;
	}

	if ( msg4) {
		m_msg += " ";
		m_msg += msg4;
	}

	if ( msg5) {
		m_msg += " ";
		m_msg += msg5;
	}
}

/**
 * Copy constructor
 * 
 * @param ex const Exception&
 */
Exception::Exception( const Exception& ex) {
	m_msg = ex.getMessage();
}

/**
 * Class destructor
 * 
 */
Exception::~Exception() {
}



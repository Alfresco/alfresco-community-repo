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

#ifndef _JavaException_H
#define _JavaException_H

//	Includes

#include "util\String.h"

//	Classes defined in this header file

namespace Alfresco {
	class Exception;
	class IOException;
}

//	Macro to check for null and throw a null pointer exception

#define NULL_POINTER_CHECK(p,m)	if(p==NULL) throw NullPointerException(m)

/**
 * Java-like Exception Class
 * 
 * Used as a base class for all Java-like exception classes.
 */
class Alfresco::Exception {
public:
	//	Constructors

	Exception( const wchar_t* msg = NULL, const wchar_t* msg2 = NULL, const wchar_t* msg3 = NULL, const wchar_t* msg4 = NULL, const wchar_t* msg5 = NULL);
	Exception( const char* moduleName, unsigned int lineNum, const wchar_t* msg = NULL, const wchar_t* msg2 = NULL, const wchar_t* msg3 = NULL, const wchar_t* msg4 = NULL, const wchar_t* msg5 = NULL);

	//	Copy constructor

	Exception( const Exception& ex);

	//	Class destructor

	~Exception();

	//	Return the exception message

	inline const String& getMessage( void) const { return m_msg; }

	//	Return the exception as a string

	inline const String& toString( void) const { return m_msg; }

private:
	//	Instance variables
	//
	//	Exception message

	String m_msg;
};

//	Macros to declare an exception class

#define DEFINE_EXCEPTION(ns,ex) namespace ns { class ex : public Exception { \
public: \
	ex( const char* modName, unsigned int lineNum, const wchar_t* msg = NULL, const wchar_t* msg2 = NULL, const wchar_t* msg3 = NULL, const wchar_t* msg4 = NULL, const wchar_t* msg5 = NULL); \
	ex( const wchar_t* msg = NULL, const wchar_t* msg2 = NULL, const wchar_t* msg3 = NULL, const wchar_t* msg4 = NULL, const wchar_t* msg5 = NULL); }; }

#define DEFINE_IOEXCEPTION(ns,ex) namespace ns { class ex : public IOException { \
public: \
	ex( const char* modName, unsigned int lineNum, const wchar_t* msg = NULL, const wchar_t* msg2 = NULL, const wchar_t* msg3 = NULL, const wchar_t* msg4 = NULL, const wchar_t* msg5 = NULL); \
	ex( const wchar_t* msg = NULL, const wchar_t* msg2 = NULL, const wchar_t* msg3 = NULL, const wchar_t* msg4 = NULL, const wchar_t* msg5 = NULL); }; }

//	Macros to define new exception class code, should be used in a module not a header

#define EXCEPTION_CLASS(ns,ex) \
	ex :: ex( const char* modName, unsigned int lineNum, const wchar_t* msg, const wchar_t* msg2, const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) : \
	  Exception(modName,lineNum,msg,msg2,msg3,msg4,msg5) {} \
	ex :: ex( const wchar_t* msg, const wchar_t* msg2, const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) : \
	  Exception(msg,msg2,msg3,msg4,msg5) {}

//	Define the IOException class

DEFINE_EXCEPTION(Alfresco,IOException);

//	Define the macro create new IOException based exceptions

#define IOEXCEPTION_CLASS(ns,ex) \
	ex :: ex( const char* modName, unsigned int lineNum, const wchar_t* msg, const wchar_t* msg2, const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) : \
	  IOException(modName,lineNum,msg,msg2,msg3,msg4,msg5) {} \
	ex :: ex( const wchar_t* msg, const wchar_t* msg2, const wchar_t* msg3, const wchar_t* msg4, const wchar_t* msg5) : \
		IOException(msg,msg2,msg3,msg4,msg5) {}

//	Define standard exceptions

DEFINE_EXCEPTION(Alfresco,NullPointerException);
DEFINE_EXCEPTION(Alfresco,ArrayIndexOutOfBoundsException);
DEFINE_EXCEPTION(Alfresco,NumberFormatException);

DEFINE_IOEXCEPTION(Alfresco, AccessDeniedException);
DEFINE_IOEXCEPTION(Alfresco, DirectoryNotEmptyException);
DEFINE_IOEXCEPTION(Alfresco, DiskFullException);
DEFINE_IOEXCEPTION(Alfresco, FileExistsException);
DEFINE_IOEXCEPTION(Alfresco, FileOfflineException);
DEFINE_IOEXCEPTION(Alfresco, FileSharingException);
DEFINE_IOEXCEPTION(Alfresco, FileNotFoundException);
DEFINE_IOEXCEPTION(Alfresco, PathNotFoundException);
DEFINE_IOEXCEPTION(Alfresco, FileLockException);
DEFINE_IOEXCEPTION(Alfresco, FileUnlockException);
DEFINE_IOEXCEPTION(Alfresco, LockConflictException);
DEFINE_IOEXCEPTION(Alfresco, NotLockedException);

#endif
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

#ifndef _Alfresco_H
#define _Alfresco_H

//	Includes

#include <windows.h>
#include <WinIOCtl.h>

#include <vector>
#include <algorithm>
#include "util\Exception.h"
#include "util\String.h"
#include "util\DataBuffer.h"

//	Classes defined in this header file

namespace Alfresco {
	class AlfrescoInterface;
	class AlfrescoFileInfo;
	class AlfrescoFileInfoList;
	typedef std::auto_ptr<AlfrescoFileInfo> PTR_AlfrescoFileInfo;
}

// Constants

namespace Alfresco {

	// Alfresco I/O control codes

	#define FSCTL_ALFRESCO_PROBE	CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x800, METHOD_BUFFERED, FILE_ANY_ACCESS)
	#define FSCTL_ALFRESCO_FILESTS 	CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x801, METHOD_BUFFERED, FILE_ANY_ACCESS)
	#define FSCTL_ALFRESCO_CHECKOUT	CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x802, METHOD_BUFFERED, FILE_WRITE_DATA)
	#define FSCTL_ALFRESCO_CHECKIN	CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x803, METHOD_BUFFERED, FILE_WRITE_DATA)

	// Request signature bytes

	#define IOSignature "ALFRESCO"
	#define IOSignatureLen 8

	// Path prefixes/components

	#define UNCPathPrefix L"\\\\"
	#define PathSeperator L"\\"

	// I/O control status codes

	#define StsSuccess          0

	#define StsError            1
	#define StsFileNotFound     2
	#define StsAccessDenied     3
	#define StsBadParameter		4
	#define StsNotWorkingCopy	5

	// Boolean field values

	#define True                1
	#define False               0

	// File status field values
	//
	// Node type

	#define TypeFile            0
	#define TypeFolder          1

	// Lock status

	#define LockNone            0
	#define LockRead            1
	#define LockWrite           2
}

// Define Alfresco interface exceptions

DEFINE_EXCEPTION(Alfresco, BadInterfaceException);

/**
* Alfresco API Class
* 
* Provides the interface to an Alfresco CIFS server to perform Alfresco specific functions
* not available via the normal file I/O functions.
*/
class Alfresco::AlfrescoInterface {
public:
	//	Class constructors

	AlfrescoInterface(String& path);

	// Class destructor

	~AlfrescoInterface();

	// Return the UNC path and root path

	inline const String& getUNCPath( void) const { return m_uncPath; }
	inline const String& getRootPath( void) const { return m_rootPath; }

	// Check if the application is running from a mapped drive, return the drive path

	inline bool isMappedDrive( void) const { return m_mappedDrive.length() > 0 ? true : false; }
	inline const String& getDrivePath( void) const { return m_mappedDrive; }

	// Check if the path is on an Alfresco CIFS server

	bool isAlfrescoFolder( void);

	// Return the Alfresco file information for a file/folder within the current folder

	PTR_AlfrescoFileInfo getFileInformation(const wchar_t* fileName);

	// Check in a working copy file

	void checkIn( const wchar_t* fileName, bool keepCheckedOut = false);

	// Check out a file

	void checkOut( const wchar_t* fileName, String& workingCopy);

private:
	// Send an I/O control request, receive and validate the response

	void sendIOControl( const unsigned int ctlCode, DataBuffer& reqbuf, DataBuffer& respbuf);

private:
	// Hide the copy constructor

	AlfrescoInterface(const AlfrescoInterface& alfresco) {};

private:
	// Instance variables
	//
	// UNC path and root path

	String m_uncPath;
	String m_rootPath;

	// Local path letter if running from a mapped drive

	String m_mappedDrive;

	// Handle to folder

	HANDLE m_handle;

};

/**
 * Alfresco File Information Class
 *
 * Contains Alfresco specific file information for a file/folder on an Alfresco CIFS server.
 */
class Alfresco::AlfrescoFileInfo {
public:
	// Class constructor

	AlfrescoFileInfo( const wchar_t* fName);

	// Return the file/folder name

	inline const String& getName( void) const { return m_name; }

	// Determine if the file is a file or folder

	inline unsigned int isType( void) const { return m_type; }

	// Return the working copy status, owner, copied from

	inline bool isWorkingCopy( void) const { return m_workingCopy; }
	inline const String& getCopyOwner( void) const { return m_workOwner; }
	inline const String& getCopiedFrom( void) const { return m_copiedFrom; }

	// Return the lock status

	inline unsigned int getLockType( void) const { return m_lockType; }
	inline const String& getLockOwner( void) const { return m_lockOwner; }

	// Return the content details

	inline bool hasContent( void) const { return m_hasContent; }
	inline LONG64 getContentLength( void) const { return m_contentLen; }
	inline const String& getContentType( void) const { return m_contentMimeType; }

	// Set the file/folder type

	inline void setType( unsigned int typ) { m_type = typ; }

	// Set the working copy owner and copied from

	void setWorkingCopy( const wchar_t* owner, const wchar_t* copiedFrom);

	// Set the lock type and owner

	void setLockType( unsigned int typ, const wchar_t* owner = L"");

	// Set the content length and type

	void setContent( LONG64 siz, const wchar_t* mimeType);

	// Operators

	bool operator==( const AlfrescoFileInfo& finfo);
	bool operator<( const AlfrescoFileInfo& finfo);

private:
	// Hide the copy constructor

	AlfrescoFileInfo(const AlfrescoFileInfo& aInfo) {};

private:
	// Instance variables
	//
	// File/folder name

	String m_name;
	unsigned int m_type;

	// Working copy flag, owner and copied from

	bool m_workingCopy;
	String m_workOwner;
	String m_copiedFrom;

	// Lock type and owner

	unsigned int m_lockType;
	String m_lockOwner;

	// Content mime-type and length

	bool m_hasContent;
	LONG64 m_contentLen;
	String m_contentMimeType;
};

/**
 * Alfresco File Info List Class
 */
class Alfresco::AlfrescoFileInfoList {
public:
	//	Class constructor

	AlfrescoFileInfoList( void) {};

	//	Add a file information object to the list

	inline void addInfo( AlfrescoFileInfo* pInfo) { m_list.push_back( pInfo); }
	inline void addInfo( PTR_AlfrescoFileInfo pInfo) { if ( pInfo.get() != NULL) m_list.push_back( pInfo.release()); }

	//	Return the number of objects in the list

	inline size_t size( void) const { return m_list.size(); }

	//	Return the specified file information

	inline const AlfrescoFileInfo* getInfoAt( unsigned int idx) const { return m_list[idx]; }

	//	Assignment operator

	inline AlfrescoFileInfo*& operator[] ( const unsigned int idx) { return m_list[idx]; }

	//	Remove all objects from the list

	inline void clear( void) { for ( unsigned int i = 0; i < m_list.size(); delete m_list[i++]); m_list.clear(); }

	// Return the vector

	std::vector<AlfrescoFileInfo*> getList( void) { return m_list; }

private:
	//	Instance variables
	//
	//	List of file information objects

	std::vector<AlfrescoFileInfo*>	m_list;
};

#endif
